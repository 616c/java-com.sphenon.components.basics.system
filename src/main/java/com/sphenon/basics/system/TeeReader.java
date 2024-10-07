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

import java.io.*;
import java.nio.*;

public class TeeReader extends Reader {

    protected Reader       reader;
    protected Writer       writer;
    protected StringWriter string_writer;
 
    public TeeReader(Reader reader, Writer writer) {
        this.reader = reader;
        if (writer != null) {
            this.writer = writer;
        } else {
            this.string_writer = new StringWriter();
            this.writer = this.string_writer;
        }
    }

    public String getString() {
        return this.string_writer.toString();
    }

    public void close() throws IOException {
        this.reader.close();
        this.writer.close();
    }

    public void mark(int readAheadLimit) throws IOException {
        this.reader.mark(readAheadLimit);
    }

    public boolean markSupported() {
        return this.reader.markSupported();
    }

    public int read() throws IOException {
        int c = this.reader.read();
        this.writer.write(c);
        return c;
    }

    public int read(char[] cbuf) throws IOException {
        int n = this.reader.read(cbuf);
        if (n != -1) { this.writer.write(cbuf, 0, n); }
        return n;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int n = this.reader.read(cbuf, off, len);
        if (n != -1) { this.writer.write(cbuf, off, n); }
        return n;
    }

    public int read(CharBuffer cbuf) throws IOException {
        int s = cbuf.position();
        int n = this.reader.read(cbuf);
        int e = cbuf.position();
        if (n != -1) { this.writer.append(cbuf, s, e); }
        return n;
    }

    public boolean ready() throws IOException {
        return this.reader.ready();
    }

    public void reset() throws IOException {
        this.reader.reset();
    }

    public long skip(long n) throws IOException {
        return this.reader.skip(n);
    }
}