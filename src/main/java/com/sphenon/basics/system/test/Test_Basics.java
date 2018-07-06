package com.sphenon.basics.system.test;

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

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.testing.*;

import com.sphenon.basics.system.*;

import java.io.File;
import java.nio.CharBuffer;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Test_Basics extends com.sphenon.basics.testing.classes.TestBase {
    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(RootContext.getInitialisationContext(), "com.sphenon.basics.system.test.Test_Basics"); };

    public Test_Basics (CallContext context) {
    }

    public String getId(CallContext context) {
        if (this.id == null) {
            this.id = "SystemBasics";
        }
        return this.id;
    }

    public TestResult perform (CallContext context, TestRun test_run) {

        try {

            long t11;
            long t21;
            long t12;
            long t22;
            File f1 = File.createTempFile("test01", ".txt");
            f1.deleteOnExit();
            File f2 = File.createTempFile("test02", ".txt");
            f2.deleteOnExit();

            {
                ComparingWriter cw = new ComparingWriter(context, f1);

                char       c1  = 'A';
                cw.append(c1);

                char[]     ca2 = { 'B', 'C', 'D' };
                CharBuffer cb2 = CharBuffer.wrap(ca2);
                cw.append(cb2);

                char[]     ca3 = { '*', '*', '*', 'E', 'F', 'G', '*' };
                CharBuffer cb3 = CharBuffer.wrap(ca3);
                cw.append(cb3, 3, 6);

                char[]     ca4 = { 'H', 'I', 'J' };
                cw.write(ca4);

                char[]     ca5 = { '*', '*', '*', 'K', 'L', 'M', '*' };
                cw.write(ca5, 3, 3);

                int        i6  = 'N';
                cw.write(i6);

                String     s7  = "OPQ";
                cw.write(s7);

                String     s8  = "***RST*";
                cw.write(s8, 3, 3);
            
                cw.close();

                t11 = f1.lastModified();
                if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "test01 : " + t11); }
            }

            {
                ComparingWriter cw = new ComparingWriter(context, f2);

                char       c1  = 'A';
                cw.append(c1);

                char[]     ca2 = { 'B', 'C', 'D' };
                CharBuffer cb2 = CharBuffer.wrap(ca2);
                cw.append(cb2);

                char[]     ca3 = { '*', '*', '*', 'E', 'F', 'G', '*' };
                CharBuffer cb3 = CharBuffer.wrap(ca3);
                cw.append(cb3, 3, 6);

                char[]     ca4 = { 'H', 'I', 'J' };
                cw.write(ca4);

                char[]     ca5 = { '*', '*', '*', 'K', 'L', 'M', '*' };
                cw.write(ca5, 3, 3);

                int        i6  = 'N';
                cw.write(i6);

                String     s7  = "OPQ";
                cw.write(s7);

                String     s8  = "***RST*";
                cw.write(s8, 3, 3);
            
                DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                cw.write(fmt.format(new Date()));

                cw.close();

                t21 = f2.lastModified();
                if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "test02 : " + t21); }
            }

            Thread.currentThread().sleep(2000);

            {
                ComparingWriter cw = new ComparingWriter(context, f1);

                char       c1  = 'A';
                cw.append(c1);

                char[]     ca2 = { 'B', 'C', 'D' };
                CharBuffer cb2 = CharBuffer.wrap(ca2);
                cw.append(cb2);

                char[]     ca3 = { '*', '*', '*', 'E', 'F', 'G', '*' };
                CharBuffer cb3 = CharBuffer.wrap(ca3);
                cw.append(cb3, 3, 6);

                char[]     ca4 = { 'H', 'I', 'J' };
                cw.write(ca4);

                char[]     ca5 = { '*', '*', '*', 'K', 'L', 'M', '*' };
                cw.write(ca5, 3, 3);

                int        i6  = 'N';
                cw.write(i6);

                String     s7  = "OPQ";
                cw.write(s7);

                String     s8  = "***RST*";
                cw.write(s8, 3, 3);
            
                cw.close();

                t12 = f1.lastModified();
                if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "test01 : " + t12); }
            }

            if (t11 == t12) {
                if ((notification_level & Notifier.CHECKPOINT) != 0) { NotificationContext.sendTrace(context, Notifier.CHECKPOINT, "File 1 is not modified, as expected"); }
                
            } else {
                return new TestResult_Failure(context, "File 1 is modified");
            }

            {
                ComparingWriter cw = new ComparingWriter(context, f2);

                char       c1  = 'A';
                cw.append(c1);

                char[]     ca2 = { 'B', 'C', 'D' };
                CharBuffer cb2 = CharBuffer.wrap(ca2);
                cw.append(cb2);

                char[]     ca3 = { '*', '*', '*', 'E', 'F', 'G', '*' };
                CharBuffer cb3 = CharBuffer.wrap(ca3);
                cw.append(cb3, 3, 6);

                char[]     ca4 = { 'H', 'I', 'J' };
                cw.write(ca4);

                char[]     ca5 = { '*', '*', '*', 'K', 'L', 'M', '*' };
                cw.write(ca5, 3, 3);

                int        i6  = 'N';
                cw.write(i6);

                String     s7  = "OPQ";
                cw.write(s7);

                String     s8  = "***RST*";
                cw.write(s8, 3, 3);
            
                DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                cw.write(fmt.format(new Date()));

                cw.close();

                t22 = f2.lastModified();
                if ((notification_level & Notifier.DIAGNOSTICS) != 0) { NotificationContext.sendTrace(context, Notifier.DIAGNOSTICS, "test02 : " + t22); }
            }

            if (t21 < t22) {
                if ((notification_level & Notifier.CHECKPOINT) != 0) { NotificationContext.sendTrace(context, Notifier.CHECKPOINT, "File 2 is modified, as expected"); }
                
            } else {
                return new TestResult_Failure(context, "File 2 is not modified");
            }

        } catch (Throwable t) {
            return new TestResult_ExceptionRaised(context, t);
        }
        
        return TestResult.OK;
    }
}
