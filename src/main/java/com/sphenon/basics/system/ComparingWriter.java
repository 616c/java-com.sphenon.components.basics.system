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
import com.sphenon.basics.customary.*;
import com.sphenon.basics.exception.*;

import java.io.*;

import java.nio.CharBuffer;

import java.util.Arrays;

public class ComparingWriter extends Writer {

    protected boolean               comparing;
    protected File                  file;
    protected FileOutputStream      ostream;               
    protected Writer                writer;
    protected FileInputStream       istream;               
    protected Reader                reader;
    protected StringBuilder         buffer;
    protected CallContext           context;
    protected boolean               do_not_modify;
    protected boolean               keep_backup_if_modified;
    protected boolean               content_differs;

    public ComparingWriter (CallContext context, File file) {
        this(context, file, false, false);
    }

    public ComparingWriter (CallContext context, File file, boolean do_not_modify, boolean keep_backup_if_modified) {
        this.context                 = context;
        this.file                    = file;
        this.do_not_modify           = do_not_modify;
        this.keep_backup_if_modified = keep_backup_if_modified;
        if (this.file.exists()) {
            try {
                this.istream = new FileInputStream(this.file);
                this.reader = new InputStreamReader(this.istream, "UTF-8");
                this.writer = null;
                this.buffer = new StringBuilder();
                this.comparing = true;
                this.content_differs = false;
            } catch (FileNotFoundException fnfe) {
                CustomaryContext.create((Context)context).throwImpossibleState(context, fnfe, "this should not happen");
                throw (ExceptionImpossibleState) null; // compiler insists
            } catch (UnsupportedEncodingException uee) {
                CustomaryContext.create((Context)context).throwImpossibleState(context, uee, "this should not happen");
                throw (ExceptionImpossibleState) null; // compiler insists
            }
        } else {
            this.do_not_modify = false;
            this.reader = null;
            this.switchToWriteMode();
        }
    }

    public void setContext(CallContext context) {
        this.context = context;
    }

    public boolean isComparing(CallContext context) {
        return this.comparing;
    }

    public boolean isContentDiffering(CallContext context) {
        return this.content_differs;
    }

    protected char[] read_buffer;
    protected int    read_buffer_size;
    protected int    read_buffer_max_size;
    protected int    expected_size;

    protected void read(int amount) {
        this.expected_size = amount;
        if (read_buffer == null || read_buffer_max_size < amount) {
            this.read_buffer_max_size = ((amount / 1024) + 4) * 1024;
            this.read_buffer = new char[this.read_buffer_max_size];
        }
        try {
            this.read_buffer_size = this.reader.read(this.read_buffer, 0, amount);
        } catch (IOException ioe) {
            CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Failure while reading");
            throw (ExceptionEnvironmentFailure) null; // compiler insists
        }
    }

    protected void check(char c) {
        if (    this.read_buffer_size != 1
             || this.read_buffer[0] != c
           ) {
            this.switchToWriteMode();
        }
    }

    protected boolean equalArrays(char[] a1, char[] a2, int offset) {
        if (this.expected_size == 0 && (a1 == null || a1.length == 0) && (a2 == null || a2.length < offset)) { return true; }
        if (a1 == null || a2 == null) { return false; }
        if (a1.length < this.expected_size || a2.length + offset < this.expected_size) { return false; }
        for (int i=0, j=offset; i<this.expected_size; i++, j++) {
            if (a1[i] != a2[j]) { return false; }
        }
        return true;
    }

    protected void check(char[] ca, int offset) {
        if (    this.read_buffer_size != this.expected_size
                || equalArrays(this.read_buffer, ca, offset) == false
           ) {
            this.switchToWriteMode();
        }
    }

    protected void check(CharSequence cs, int offset) {
        if ((cs instanceof CharBuffer) && ((CharBuffer)cs).hasArray()) {
            this.check(((CharBuffer)cs).array(), offset);
        } else {
            this.check(cs.toString().toCharArray(), offset);
        }
    }

    protected void switchToWriteMode() {
        this.comparing = false;
        if (this.reader != null) {
            this.content_differs = true;
            try {
                this.reader.close();
            } catch (IOException ioe) {
                CustomaryContext.create((Context)context).throwEnvironmentFailure(context, ioe, "Failure while closing reader");
                throw (ExceptionEnvironmentFailure) null; // compiler insists
            }
            this.reader = null;
        }

        if (do_not_modify) {
            this.buffer = null;
        } else {
            if (keep_backup_if_modified) {
                FileUtilities.backupFile(context, this.file, ".autobackup");
            }
            try {
                this.ostream = new FileOutputStream(this.file);
                this.writer = new OutputStreamWriter(this.ostream, "UTF-8");
            } catch (IOException ioe) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Could not open writer");
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
            if (this.buffer != null) {
                try {
                    this.writer.append(this.buffer);
                } catch (IOException ioe) {
                    CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Could not write to file");
                    throw (ExceptionPreConditionViolation) null; // compiler insists
                }
                this.buffer = null;
            }
        }
    }

    public void close() throws IOException {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
            this.istream.close();
            this.istream = null;
        }
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
            this.ostream.close();
            this.ostream = null;
        }
        this.buffer = null;
    }

    public void flush() throws IOException {
        if (this.writer != null) {
            this.writer.flush();
        }
    }

    public Writer append(char c) throws IOException {
        if (this.comparing) {
            this.read(1);
            this.check(c);
        }
        if (this.comparing) {
            this.buffer.append(c);
        } else {
            this.writer.append(c);
        }
        return this;
    }

    public Writer append(CharSequence csq) throws IOException {
        if (this.comparing) {
            this.read(csq.length());
            this.check(csq, 0);
        }
        if (this.comparing) {
            this.buffer.append(csq);
        } else {
            if (this.writer != null) {
                this.writer.append(csq);
            }
        }
        return this;
    }

    public Writer append(CharSequence csq, int start, int end) throws IOException {
        if (this.comparing) {
            this.read(end-start);
            this.check(csq, start);
        }
        if (this.comparing) {
            this.buffer.append(csq, start, end);
        } else {
            if (this.writer != null) {
                this.writer.append(csq, start, end);
            }
        }
        return this;
    }

    public void write(char[] cbuf) throws IOException {
        if (this.comparing) {
            this.read(cbuf.length);
            this.check(cbuf, 0);
        }
        if (this.comparing) {
            this.buffer.append(cbuf);
        } else {
            if (this.writer != null) {
                this.writer.write(cbuf);
            }
        }
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if (this.comparing) {
            this.read(len);
            this.check(cbuf, off);
        }
        if (this.comparing) {
            this.buffer.append(cbuf, off, len);
        } else {
            if (this.writer != null) {
                this.writer.write(cbuf, off, len);
            }
        }
    }

    public void write(int c) throws IOException {
        this.append((char) c);
    }

    public void write(String str) throws IOException {
        this.write(str.toCharArray());
    }

    public void write(String str, int off, int len) throws IOException {
        this.write(str.toCharArray(), off, len);
    }
}
