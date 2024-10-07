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
import java.io.*;

abstract public class OnDemandByteArrayInputStream extends ByteArrayInputStream {

    protected CallContext context;
    static protected byte[] dummy = new byte[0];

    public OnDemandByteArrayInputStream(CallContext context) {
        super(dummy);
        this.context = context;
    }

    protected boolean prepared = false;

    protected void prepareBuffer() {
        if (this.prepared == false) {
            this.buf = this.getData(context);
            this.pos = 0;
            this.mark = 0;
            this.count = (this.buf == null ? 0 : this.buf.length);
            this.prepared = true;
        }
    }

    abstract protected byte[] getData(CallContext context);

    public int available() {
        this.prepareBuffer();
        return super.available();
    }

    public void close() throws IOException {
        this.prepareBuffer();
        super.close();
    }

    public void mark(int readAheadLimit) {
        this.prepareBuffer();
        super.mark(readAheadLimit);
    }

    public boolean markSupported() {
        this.prepareBuffer();
        return super.markSupported();
    }

    public int read() {
        this.prepareBuffer();
        return super.read();
    }

    public int read(byte[] b, int off, int len) {
        this.prepareBuffer();
        return super.read(b, off, len);
    }

    public void reset() {
        this.prepareBuffer();
        super.reset();
    }

    public long skip(long n) {
        this.prepareBuffer();
        return super.skip(n);
    }
}
