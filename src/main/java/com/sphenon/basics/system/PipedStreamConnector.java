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
import com.sphenon.basics.configuration.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.function.*;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.concurrent.locks.*;

public class PipedStreamConnector implements ManagedResource {

    static final public Class _class = PipedStreamConnector.class;

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(_class); };

    static protected Configuration config;
    static { config = Configuration.create(RootContext.getInitialisationContext(), _class); };

    protected CallContext       context;
    protected String            info;

    protected Runner            runner;
    protected Thread            runner_thread;

    protected PipedInputStream  piped_input_stream;
    protected PipedOutputStream piped_output_stream;

    public PipedStreamConnector (CallContext context, Runner runner, String info) {
        this.runner = runner;
        this.info   = info;
    }

    public InputStream getInputStream(CallContext context) {
        return this.piped_input_stream;
    }

    public OutputStream getOutputStream(CallContext context) {
        return this.piped_output_stream;
    }

    public void start(CallContext context) {
        this.start(context, false);
    }

    public void start(CallContext context, boolean wait) {
        this.context = context;

        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "PipedStreamConnector, creating runner '%(info)'...", "info", this.info); }

        this.runner_thread = new Thread() {
                public void run () {
                    if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "PipedStreamConnector, runner thread '%(info)' is running...", "info", info); }

                    try {
                        runner.run(context);
                    } finally {
                        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "PipedStreamConnector, runner thread '%(info)' stopped.", "info", info); }
                    }
                }
            };
        
        if ((notification_level & Notifier.SELF_DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.SELF_DIAGNOSTICS, "PipedStreamConnector, starting runner '%(info)'...", "info", this.info); }
        
        try {
            this.piped_input_stream = new PipedInputStream();
            this.piped_output_stream = new PipedOutputStream(this.piped_input_stream);
        } catch (java.io.IOException ioe) {
            NotificationContext.sendError(context, "PipedStreamConnector, runner thread '%(info)', could not create piped stream pair: %(reason)", "info", info, "reason", ioe);
        }

        this.runner_thread.start();
    
        if (wait) {
            this.wait(context, 0);
        }
    }

    // to get this working, the thread needs to call
    // certain methods, like "Thread.currentThread().isInterrupted()"
    // and throw exception then; ideally this is placed somewhere
    // inside Step/Mechanism, probably within 
    // Class_ExecutionControl.java and/or Class_ExecutionInterceptor.java
    public void interrupt(CallContext context) {
        if (this.runner_thread != null) {
            this.runner_thread.interrupt();
        }
    }

    public void release(CallContext context) {
        this.interrupt(context);
        this.close(context);
    }

    public boolean wait(CallContext context, long timeout_ms) {
        if (this.runner_thread != null) {
            if (this.runner_thread.isAlive()) {
                try {
                    this.runner_thread.join(timeout_ms);
                } catch (java.lang.InterruptedException ie) {
                    NotificationContext.sendError(context, "PipedStreamConnector, runner thread '%(info)' terminated unsuccessfully: %(reason)", "info", info, "reason", ie);
                }
            }
            if (this.runner_thread.isAlive()) {
                NotificationContext.sendError(context, "PipedStreamConnector, could not interrupt runner thread '%(info)' (maybe due to timeout)", "info", info);
                return false;
            }
            this.runner_thread = null;
        }
        return true;
    }

    public boolean close(CallContext context) {
        boolean ok = true;
        ok = this.wait(context, config.get(context, "CleanupTimeout", 60000));
        try {
            if (this.piped_output_stream != null) {
                this.piped_output_stream.close();
                this.piped_output_stream = null;
            }
        } catch (java.io.IOException ioe) {
            NotificationContext.sendError(context, "PipedStreamConnector, runner thread '%(info)', could not close output stream: %(reason)", "info", info, "reason", ioe);
            ok = false;
        }
        try {
            if (this.piped_input_stream != null) {
                this.piped_input_stream.close();
                this.piped_input_stream = null;
            }
        } catch (java.io.IOException ioe) {
            NotificationContext.sendError(context, "PipedStreamConnector, runner thread '%(info)', could not close input stream: %(reason)", "info", info, "reason", ioe);
            ok = false;
        }
        return ok;
    }
}
