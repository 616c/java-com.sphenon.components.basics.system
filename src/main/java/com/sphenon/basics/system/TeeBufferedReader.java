package com.sphenon.basics.system;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

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

public class TeeBufferedReader extends BufferedReader {

    protected BufferedReader buffered_reader;
    protected BufferedWriter buffered_writer;
    protected StringWriter   string_writer;
 
    public TeeBufferedReader(BufferedReader buffered_reader, BufferedWriter buffered_writer) {
        super(buffered_reader); // super class insists
        this.buffered_reader = buffered_reader;
        if (buffered_writer != null) {
            this.buffered_writer = buffered_writer;
        } else {
            this.string_writer = new StringWriter();
            this.buffered_writer = new BufferedWriter(this.string_writer);
        }
    }

    public String getString() {
        return this.string_writer.toString();
    }

    public void close() throws IOException {
        this.buffered_reader.close();
        this.buffered_writer.close();
    }

    public void mark(int readAheadLimit) throws IOException {
        this.buffered_reader.mark(readAheadLimit);
    }

    public boolean markSupported() {
        return this.buffered_reader.markSupported();
    }

    public int read() throws IOException {
        int c = this.buffered_reader.read();
        this.buffered_writer.write(c);
        return c;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int n = this.buffered_reader.read(cbuf, off, len);
        if (n != -1) { this.buffered_writer.write(cbuf, off, n); }
        return n;
    }

    public String readLine() throws IOException {
        String s = this.buffered_reader.readLine();
        if (s != null) {
            this.buffered_writer.write(s, 0, s.length());
            this.buffered_writer.newLine();
        }
        return s;
    }

    public boolean ready() throws IOException {
        return this.buffered_reader.ready();
    }

    public void reset() throws IOException {
        this.buffered_reader.reset();
    }

    public long skip(long n) throws IOException {
        return this.buffered_reader.skip(n);
    }
}