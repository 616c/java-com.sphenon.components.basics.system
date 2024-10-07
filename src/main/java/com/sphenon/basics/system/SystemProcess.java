package com.sphenon.basics.system;

/****************************************************************************
  Copyright 2001-2024 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;

import java.util.concurrent.locks.*;

public class SystemProcess implements ManagedResource {

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(RootContext.getInitialisationContext(), "com.sphenon.basics.system.SystemProcess"); };


    protected CallContext context;
    
    protected String command;
    protected String[] command_array;
    protected String working_folder;

    protected java.lang.Process process;

    protected Thread            process_supplier;

    protected Thread            process_output_listener;
    protected Thread            process_error_listener;
    protected long              output_listener_count;
    protected long              error_listener_count;

    protected InputStream       process_stdout;
    protected InputStream       process_stderr;
    protected OutputStream      process_stdin;

    protected InputStream       process_input;
    protected PrintWriter       process_stdin_writer;

    protected int               exit_value;

    protected OutputStream      process_output_pipe;
    protected PipedInputStream  output_piped_input_stream;
    protected InputStreamReader output_piped_reader;
    protected OutputStream      process_output_stdout;
    protected volatile StringBuilder     collected_output;
    protected TeeInputStream    output_tee_input_stream;
    protected InputStreamReader output_input_stream_reader;

    protected OutputStream      process_error_pipe;
    protected PipedInputStream  error_piped_input_stream;
    protected InputStreamReader error_piped_reader;
    protected OutputStream      process_error_stderr;
    protected volatile StringBuilder     collected_error;
    protected TeeInputStream    error_tee_input_stream;
    protected InputStreamReader error_input_stream_reader;

    protected volatile boolean feeder_active;
    protected volatile boolean process_active;

    protected boolean debug;

    public int getExitValue(CallContext context) {
        return this.exit_value;
    }

    public PipedInputStream getProcessOutputAsStream(CallContext context) {
        return this.output_piped_input_stream;
    }

    public InputStreamReader getProcessOutputAsReader(CallContext context) {
        if (this.output_piped_reader == null && this.output_piped_input_stream != null) {
            this.output_piped_reader = new InputStreamReader(this.output_piped_input_stream);
        }
        return this.output_piped_reader;
    }

    public String getProcessOutputAsString(CallContext context) {
        return this.getProcessOutputAsString(context, false);
    }

    public String getProcessOutputAsString(CallContext context, boolean clear) {
        if (collected_output != null) {
            synchronized (collected_output) {
                String result = collected_output.toString();
                if (clear) { collected_output.setLength(0); }
                return result;
            }
        } else {
            return null;
        }
    }

    public PipedInputStream getProcessErrorAsStream(CallContext context) {
        return this.error_piped_input_stream;
    }

    public InputStreamReader getProcessErrorAsReader(CallContext context) {
        if (this.error_piped_reader == null && this.error_piped_input_stream != null) {
            this.error_piped_reader = new InputStreamReader(this.error_piped_input_stream);
        }
        return this.error_piped_reader;
    }

    public String getProcessErrorAsString(CallContext context) {
        return this.getProcessOutputAsString(context, false);
    }

    public String getProcessErrorAsString(CallContext context, boolean clear) {
        if (collected_error != null) {
            synchronized (collected_error) {
                String result = collected_error.toString();
                if (clear) { collected_error.setLength(0); }
                return result;
            }
        } else {
            return null;
        }
    }

    public OutputStream getProcessInputAsStream(CallContext context) {
        return process_stdin;
    }

    public PrintWriter getProcessInputAsWriter(CallContext context) {
        if (process_stdin_writer == null && process_stdin != null) {
            process_stdin_writer = new PrintWriter(process_stdin);
        }
        return process_stdin_writer;
    }

    public void configure(CallContext context,
                          boolean write_stdout_to_stdout,
                          boolean write_stdout_to_piped_stream, 
                          boolean write_stdout_to_string_builder, 
                          boolean write_stderr_to_stderr,
                          boolean write_stderr_to_piped_stream, 
                          boolean write_stderr_to_string_builder) {
        this.output_tee_input_stream.setStreamState(0, write_stdout_to_piped_stream);
        this.output_tee_input_stream.setStreamState(1, write_stdout_to_stdout);
        this. error_tee_input_stream.setStreamState(0, write_stderr_to_piped_stream);
        this. error_tee_input_stream.setStreamState(1, write_stderr_to_stderr);

        if (write_stdout_to_string_builder) {
            if (this.collected_output == null) {
                this.collected_output = new StringBuilder();
            }
        } else {
            this.collected_output = null;
        }

        if (write_stderr_to_string_builder) {
            if (this.collected_error == null) {
                this.collected_error = new StringBuilder();
            }
        } else {
            this.collected_error = null;
        }
    } 

    public SystemProcess (CallContext context, String command, String working_folder) {
        this(context, command, working_folder, false);
    }

    public SystemProcess (CallContext context, String command, String working_folder, boolean debug) {
        this.command = command;
        this.command_array = null;
        this.working_folder = working_folder;
        this.explicitly_stopped = false;
        this.debug = debug;
    }

    public SystemProcess (CallContext context, String[] command_array, String working_folder) {
        this(context, command_array, working_folder, false);
    }

    public SystemProcess (CallContext context, String[] command_array, String working_folder, boolean debug) {
        this.command = null;
        this.command_array = command_array;
        this.working_folder = working_folder;
        this.explicitly_stopped = false;
        this.debug = debug;
    }

    public PipedInputStream start(CallContext context) {
        return start(context, null);
    }

    public PipedInputStream start(CallContext context, InputStream process_input_arg) {
        return this.start(context, process_input_arg, false, false);
    }

    public PipedInputStream start(CallContext context, InputStream process_input_arg, boolean dump_to_console, boolean wait) {
        this.start(context, process_input_arg, dump_to_console, true, false, dump_to_console, true, false, wait);
        return this.getProcessOutputAsStream(context);
    }

    public void start(CallContext call_context,
                      InputStream process_input_arg,
                      boolean write_stdout_to_stdout,
                      boolean write_stdout_to_piped_stream, 
                      boolean write_stdout_to_string_builder, 
                      boolean write_stderr_to_stderr,
                      boolean write_stderr_to_piped_stream, 
                      boolean write_stderr_to_string_builder, 
                      boolean wait) {
        this.context = call_context;
        this.explicitly_stopped = false;

        this.process_input = process_input_arg;

        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, starting process '%(command)'...", "command", (command != null ? command : StringUtilities.join(context, command_array, " ", true))); }

        try {
            if (command != null) {
                this.process = java.lang.Runtime.getRuntime().exec(command, null, working_folder == null ? null : new java.io.File(working_folder));
            } else {
                this.process = java.lang.Runtime.getRuntime().exec(command_array, null, working_folder == null ? null : new java.io.File(working_folder));
            }
        } catch (java.io.IOException ioe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ioe, "External process '%(command)' could not be started", "command", (command != null ? command : StringUtilities.join(context, command_array, " ", true)));
            throw (ExceptionConfigurationError) null; // compiler insists
        }

        this.process_active = true;
    
        this.process_stdout = this.process.getInputStream();
        this.process_stderr = this.process.getErrorStream();
        this.process_stdin  = this.process.getOutputStream();

        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, creating output streams..."); }
            
        if (write_stdout_to_stdout) {
            SystemContext sc = SystemContext.getOrCreate((Context) context);
            this.process_output_stdout = sc.getOutputStream(context);
        }

        if (write_stdout_to_piped_stream) {
            try {
                this.output_piped_input_stream = new PipedInputStream();
                this.process_output_pipe = new PipedOutputStream(this.output_piped_input_stream);
            } catch (java.io.IOException ioe) {
                CustomaryContext.create((Context)context).throwAssertionProvedFalse(context, ioe, "Piped Output Stream could not be created");
                throw (ExceptionAssertionProvedFalse) null; // compiler insists
            }
        }

        this.output_tee_input_stream = new TeeInputStream(this.process_stdout, this.process_output_pipe, this.process_output_stdout);
        this.output_input_stream_reader = new InputStreamReader(this.output_tee_input_stream);
        
        if (write_stdout_to_string_builder) {
            this.collected_output = new StringBuilder();
        }

        if (write_stderr_to_stderr) {
            SystemContext sc = SystemContext.getOrCreate((Context) context);
            this.process_error_stderr = sc.getErrorStream(context);
        }

        if (write_stderr_to_piped_stream) {
            try {
                this.error_piped_input_stream = new PipedInputStream();
                this.process_error_pipe = new PipedOutputStream(this.error_piped_input_stream);
            } catch (java.io.IOException ioe) {
                CustomaryContext.create((Context)context).throwAssertionProvedFalse(context, ioe, "Piped Output Stream could not be created");
                throw (ExceptionAssertionProvedFalse) null; // compiler insists
            }
        }

        this.error_tee_input_stream = new TeeInputStream(this.process_stderr, this.process_error_pipe, this.process_error_stderr);
        this.error_input_stream_reader = new InputStreamReader(this.error_tee_input_stream);
        
        if (write_stderr_to_string_builder) {
            this.collected_error = new StringBuilder();
        }

        this.startListeners(context);

        if (process_input != null) {
            if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, creating supplier thread..."); }

            this.process_supplier = new Thread() {
                    public void run () {
                        try {
                            feeder_active = true;
                            int c;
                            while ((c = process_input.read()) != -1) {
                                if (debug) { System.err.println("[I|" + ((char) c) + "]"); }
                                process_stdin.write(c);
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process supplier thread terminated unsuccessfully: %(reason)", "reason", ioe);
                        } finally {
                            try {
                                process_stdin.close();
                            } catch (java.io.IOException ioe) {
                                NotificationContext.sendError(context, "system process supplier thread could not close process stdin: %(reason)", "reason", ioe);
                            }
                            try {
                                process_input.close();
                            } catch (java.io.IOException ioe) {
                                NotificationContext.sendError(context, "system process supplier thread could not close process input stream: %(reason)", "reason", ioe);
                            }

                            feeder_active = false;
                        }
                        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, supplier thread terminated."); }
                    };
                };
        
            if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, starting supplier thread..."); }
        
            this.process_supplier.start();
        }

        if (wait) {
            try {
                this.process.waitFor();
            } catch (java.lang.InterruptedException ie) {
                NotificationContext.sendError(context, "system process thread terminated unsuccessfully: %(reason)", "reason", ie);
            }
        }
    }

    protected volatile boolean explicitly_stopped;

    public void stop(CallContext context) {
        this.explicitly_stopped = true;
        if (this.process != null) {
            this.process.destroy();
        }
    }

    public void release(CallContext context) {
        if (this.process_output_listener != null) {
            this.process_output_listener.interrupt();
        }
        if (this.process_error_listener != null) {
            this.process_error_listener.interrupt();
        }
        this.stop(context);
    }

    public void wait(CallContext context) {
        if (this.process != null) {
            try {
                this.process.waitFor();
            } catch (java.lang.InterruptedException ie) {
                NotificationContext.sendError(context, "system process thread terminated unsuccessfully: %(reason)", "reason", ie);
            }
        }
        if (this.isFinished(context) == false) {
            NotificationContext.sendError(context, "system process thread invalid state: no exit value available after waitFor terminated");
        }
        if (this.process_supplier != null) {
            try {
                this.process_supplier.join(/* millis */);
            } catch (java.lang.InterruptedException ie) {
                NotificationContext.sendError(context, "system process supplier thread terminated unsuccessfully: %(reason)", "reason", ie);
            }
        }
        if (this.process_output_listener != null) {
            try {
                this.process_output_listener.join(/* millis */);
            } catch (java.lang.InterruptedException ie) {
                NotificationContext.sendError(context, "system process output listener thread terminated unsuccessfully: %(reason)", "reason", ie);
            }
        }
        if (this.process_error_listener != null) {
            try {
                this.process_error_listener.join(/* millis */);
            } catch (java.lang.InterruptedException ie) {
                NotificationContext.sendError(context, "system process error listener thread terminated unsuccessfully: %(reason)", "reason", ie);
            }
        }
    }

    public void closeOutputAndErrorIO(CallContext context) {
        try {
            if (this.output_piped_reader != null) {
                this.output_piped_reader.close();
            }
        } catch (java.io.IOException ioe) {
            NotificationContext.sendError(context, "could not close output reader: %(reason)", "reason", ioe);
        }
        try {
            if (this.output_piped_input_stream != null) {
                this.output_piped_input_stream.close();
            }
        } catch (java.io.IOException ioe) {
            NotificationContext.sendError(context, "could not close output stream: %(reason)", "reason", ioe);
        }
        try {
            if (this.error_piped_reader != null) {
                this.error_piped_reader.close();
            }
        } catch (java.io.IOException ioe) {
            NotificationContext.sendError(context, "could not close error reader: %(reason)", "reason", ioe);
        }
        try {
            if (this.error_piped_input_stream != null) {
                this.error_piped_input_stream.close();
            }
        } catch (java.io.IOException ioe) {
            NotificationContext.sendError(context, "could not close error stream: %(reason)", "reason", ioe);
        }
    }

    public synchronized boolean isFinished(CallContext context) {
        if (process_active == false) {
            return true;
        }
        try {
            exit_value = process.exitValue();
            process_active = false;
            try {
                process_stdout.close();
            } catch (java.io.IOException ioe) {
                NotificationContext.sendError(context, "system process supplier thread could not close process stdout: %(reason)", "reason", ioe);
            }
            try {
                process_stderr.close();
            } catch (java.io.IOException ioe) {
                NotificationContext.sendError(context, "system process supplier thread could not close process stderr: %(reason)", "reason", ioe);
            }
            try {
                process_stdin.close();
            } catch (java.io.IOException ioe) {
                NotificationContext.sendError(context, "system process supplier thread could not close process stdin: %(reason)", "reason", ioe);
            }
            if (exit_value != 0) {
                if ((notification_level & Notifier.PRODUCTION) != 0) { NotificationContext.sendError(context, "system process failed, exit value: %(exitvalue), command '%(command)'", "exitvalue", t.s(exit_value), "command", (command != null ? command : StringUtilities.join(context, command_array, " ", true))); }
            } else {
                if ((notification_level & Notifier.VERBOSE) != 0) {
                    NotificationContext.sendTrace(context, Notifier.VERBOSE, "exit value: %(exitvalue)", "exitvalue", t.s(exit_value));
                }
            }
            return true;
        } catch (IllegalThreadStateException itse) {
            return false;
        }
    }

    public void startListeners(CallContext call_context) {
        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, creating listener threads..."); }

        this.output_listener_count = 0;
        this.error_listener_count = 0;

        this.process_output_listener = new Thread() {
                public void run () {
                    try {
                        char[] buf = new char[1024];
                        int count;
                        while((count = output_input_stream_reader.read(buf)) != -1) {
                            if (debug) { System.err.println("[O|" + new String(buf, 0, count) + "]"); }

                            output_listener_count += count;
                            if (collected_output != null) {
                                synchronized (collected_output) {
                                    collected_output.append(buf, 0, count);
                                }
                            }
                            output_tee_input_stream.flushOutputStreams();
                        }
                        try {
                            if (output_input_stream_reader != null) {
                                output_input_stream_reader.close();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stdout listener thread could not close isr: %(reason)", "reason", ioe);
                        }
                        try {
                            if (output_tee_input_stream != null) {
                                output_tee_input_stream.close();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stdout listener thread could not close tis: %(reason)", "reason", ioe);
                        }
                        try {
                            if (process_output_pipe != null) {
                                process_output_pipe.close();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stdout listener thread could not close process output pipe: %(reason)", "reason", ioe);
                        }
                        try {
                            if (process_output_stdout != null) {
                                process_output_stdout.flush();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stdout listener thread could not flush process output stdout: %(reason)", "reason", ioe);
                        }
                    } catch (java.io.IOException ioe) {
                        if (explicitly_stopped == false) {
                            NotificationContext.sendError(context, "system process stdout listener thread terminated unsuccessfully: %(reason)", "reason", ioe);
                        }
                    }
                    if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, output listener thread terminated."); }
                }
            };

        this.process_error_listener = new Thread() {
                public void run () {
                    try {
                        char[] buf = new char[1024];
                        int count;
                        while((count = error_input_stream_reader.read(buf)) != -1) {
                            if (debug) { System.err.println("[E|" + new String(buf, 0, count) + "]"); }

                            error_listener_count += count;
                            if (collected_error != null) {
                                synchronized (collected_error) {
                                    collected_error.append(buf, 0, count);
                                }
                            }
                            error_tee_input_stream.flushOutputStreams();
                        }
                        try {
                            if (error_input_stream_reader != null) {
                                error_input_stream_reader.close();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stderr listener thread could not close isr: %(reason)", "reason", ioe);
                        }
                        try {
                            if (error_tee_input_stream != null) {
                                error_tee_input_stream.close();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stderr listener thread could not close tis: %(reason)", "reason", ioe);
                        }
                        try {
                            if (process_error_pipe != null) {
                                process_error_pipe.close();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stderr listener thread could not close process error pipe: %(reason)", "reason", ioe);
                        }
                        try {
                            if (process_error_stderr != null) {
                                process_error_stderr.flush();
                            }
                        } catch (java.io.IOException ioe) {
                            NotificationContext.sendError(context, "system process stderr listener thread could not flush process error: %(reason)", "reason", ioe);
                        }
                    } catch (java.io.IOException ioe) {
                        if (explicitly_stopped == false) {
                            NotificationContext.sendError(context, "system process stderr listener thread terminated unsuccessfully: %(reason)", "reason", ioe);
                        }
                    }
                    if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, error listener thread terminated."); }
                }
            };
        
        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "SystemProcess, starting listener threads..."); }

        this.process_output_listener.start();
        this.process_error_listener.start();
    }

// that simply does not work
//     public void stopListeners(CallContext context) {
//         this.process_output_listener.interrupt();
//         this.process_error_listener.interrupt();
//     }

    public void waitForListeners(CallContext context) {
        long olc;
        long elc;
        boolean busy;
        do {
            busy = false;
            olc = output_listener_count;
            elc = error_listener_count;
            try {
                Thread.currentThread().sleep(50);
            } catch (InterruptedException ie) { }
            if (olc != output_listener_count || elc != error_listener_count) {
                busy = true;
                try {
                    Thread.currentThread().sleep(250);
                } catch (InterruptedException ie) { }
            }
        } while(busy);
    }

    static public int execute(CallContext context, String command, String working_folder) {
        SystemProcess sp = new SystemProcess(context, command, working_folder);
        sp.start(context, null, true, false, false, true, false, false, false);
        sp.wait(context);
        return sp.getExitValue(context);
    }

    static public int execute(CallContext context, String[] command_array, String working_folder) {
        SystemProcess sp = new SystemProcess(context, command_array, working_folder);
        sp.start(context, null, true, false, false, true, false, false, false);
        sp.wait(context);
        return sp.getExitValue(context);
    }

    static public int execute(CallContext context, InputStream process_input, String working_folder, String... command_array) {
        return execute(context, process_input, null, working_folder, command_array);
    }

    static public int execute(CallContext context, InputStream process_input, String[] outerr, String working_folder, String... command_array) {
        return execute(context, false, process_input, outerr, working_folder, command_array);
    }

    static public int execute(CallContext context, boolean debug, InputStream process_input, String[] outerr, String working_folder, String... command_array) {
        SystemProcess sp = new SystemProcess(context, command_array, working_folder, debug);
        sp.start(context, process_input, outerr == null ? true : false, false, outerr != null ? true : false, outerr == null ? true : false, false, outerr != null ? true : false, false);
        sp.wait(context);
        outerr[0] = sp.getProcessOutputAsString(context, true);
        outerr[1] = sp.getProcessErrorAsString(context, true);
        return sp.getExitValue(context);
    }

    static public InputStream createInputStreamFromText(CallContext context, String data) {
        return new ByteArrayInputStream(data.getBytes());
    }
}
