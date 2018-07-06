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

public class TeeInputStream extends InputStream {

    protected InputStream input_stream;
    protected OutputStream[] active_output_streams;
    protected OutputStream[] all_output_streams;
 
    public TeeInputStream(InputStream input_stream, OutputStream... output_streams) {
        this.input_stream = input_stream;
        this.all_output_streams = output_streams;
        if (this.all_output_streams != null) {
            this.active_output_streams = new OutputStream[this.all_output_streams.length];
            for (int i=0; i<this.all_output_streams.length; i++) {
                this.active_output_streams[i] = this.all_output_streams[i];
            }
        }
    }

    public void setStreamState(int i, boolean enabled) {
        this.active_output_streams[i] = enabled ? this.all_output_streams[i] : null;
    }

    public int available() throws IOException {
        return this.input_stream.available();
    }
    public void close() throws IOException {
        this.input_stream.close();
    }
    public void mark(int readlimit) {
        this.input_stream.mark(readlimit);
    }
    public boolean markSupported() {
        return this.input_stream.markSupported();
    }
    public int read() throws IOException {
        int c = this.input_stream.read();
        if (c != -1) {
            if (this.active_output_streams != null) {
                for (OutputStream output_stream : active_output_streams) {
                    if (output_stream != null) {
                        output_stream.write(c);
                    }
                }
            }
        }
        return c;
    }
    public int read(byte[] b) throws IOException {
        int n = this.input_stream.read(b);
        if (n != -1) {
            if (this.active_output_streams != null) {
                for (OutputStream output_stream : active_output_streams) {
                    if (output_stream != null) {
                        output_stream.write(b, 0, n);
                    }
                }
            }
        }
        return n;
    }
    public int read(byte[] b, int off, int len) throws IOException {
        int n = this.input_stream.read(b, off, len);
        if (n != -1) {
            if (this.active_output_streams != null) {
                for (OutputStream output_stream : active_output_streams) {
                    if (output_stream != null) {
                        output_stream.write(b, off, n);
                    }
                }
            }
        }
        return n;
    }
    public void reset() throws IOException {
        this.input_stream.reset();
    }
    public long skip(long n) throws IOException {
        return this.input_stream.skip(n);
    }
    public void flushOutputStreams() throws IOException {
        if (this.active_output_streams != null) {
            for (OutputStream output_stream : active_output_streams) {
                if (output_stream != null) {
                    output_stream.flush();
                }
            }
        }
    }
}
