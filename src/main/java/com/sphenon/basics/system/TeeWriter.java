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

public class TeeWriter extends PrintWriter {
    private final Writer other;

    public TeeWriter(Writer writer, Writer other) {
        super(writer);
        this.other = other;
    }

    public void write(char buf[], int off, int len) {
        super.write(buf, off, len);
        try {
            other.write(buf, off, len);
        } catch (IOException ioe) { }
    }

    public void flush() {
        super.flush();
        try {
            other.flush();
        } catch (IOException ioe) { }
    }

    public void close() {
        super.close();
        try {
            other.close();
        } catch (IOException ioe) { }
    }
}
