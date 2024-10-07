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
import java.util.Locale;

public class TeePrintStream extends PrintStream {
    private PrintStream ps1;
    private PrintStream ps2;

    public TeePrintStream(PrintStream ps1, PrintStream ps2) {
        super(new NullOutputStream());

        this.ps1 = ps1;
        this.ps2 = ps2;
    }

    public void flush() {
        this.ps1.flush();
        this.ps2.flush();
    }

    public void close() {
        this.ps1.close();
        this.ps2.close();
    }

    public boolean checkError() {
        if (this.ps1.checkError()) { return true; }
        if (this.ps2.checkError()) { return true; }
        return false;
    }

    public void write(int data) {
        this.ps1.write(data);
        this.ps2.write(data);
    }

    public void write(byte[] data,int offset,int length) {
        this.ps1.write(data, offset, length);
        this.ps2.write(data, offset, length);
    }

    public void print(boolean data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(char data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(int data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(long data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(float data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(double data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(char[] data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(String data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void print(Object data) {
        this.ps1.print(data);
        this.ps2.print(data);
    }

    public void println() {
        this.ps1.println();
        this.ps2.println();
    }

    public void println(boolean data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(char data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(int data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(long data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(float data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(double data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(char[] data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(String data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public void println(Object data) {
        this.ps1.println(data);
        this.ps2.println(data);
    }

    public PrintStream printf(String format, Object ... args) {
        this.ps1.printf(format, args);
        this.ps2.printf(format, args);
        return this;
    }

    public PrintStream printf(Locale locale, String format, Object ... args) {
        this.ps1.printf(locale, format, args);
        this.ps2.printf(locale, format, args);
        return this;
    }

    public PrintStream format(String format, Object ... args) {
        this.ps1.format(format, args);
        this.ps2.format(format, args);
        return this;
    }

    public PrintStream format(Locale locale, String format, Object ... args) {
        this.ps1.format(locale, format, args);
        this.ps2.format(locale, format, args);
        return this;
    }

    public PrintStream append(CharSequence data) {
        this.ps1.append(data);
        this.ps2.append(data);
        return this;
    }

    public PrintStream append(CharSequence data, int start, int end) {
        this.ps1.append(data, start, end);
        this.ps2.append(data, start, end);
        return this;
    }

    public PrintStream append(char data) {
        this.ps1.append(data);
        this.ps2.append(data);
        return this;
    }

    public void write(byte[] data) throws IOException {
        this.ps1.write(data);
        this.ps2.write(data);
    }
}
