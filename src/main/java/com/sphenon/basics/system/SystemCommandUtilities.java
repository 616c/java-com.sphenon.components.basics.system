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

import com.sphenon.basics.context.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.exception.*;

import java.io.File;
import java.io.FilePermission;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.Vector;

import java.util.Date;
import java.util.Calendar;
import java.text.*;


/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


  slowly deprecating -> replace with SystemUtilities, enhancements go only there


~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
public class SystemCommandUtilities {

    protected SystemCommandUtilities (CallContext context) {
    }

    static public void ensureFolderExists(CallContext context, String folder_name) {
        ensureFolderExists(context, new File(folder_name));
    }

    static public void ensureFolderExists(CallContext context, File folder) {
        if (folder != null) { folder.mkdirs(); }
        if (folder == null || folder.isDirectory() == false) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "Could not ensure existance of folder '%(folder)' - does not exist and/or could not be created", "folder", folder);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    static public void ensureParentFolderExists(CallContext context, String file_name) {
        ensureParentFolderExists(context, new File(file_name));
    }

    static public void ensureParentFolderExists(CallContext context, File file) {
        File folder = file.getParentFile();
        if (folder != null) { folder.mkdirs(); }
        if (folder != null && folder.isDirectory() == false) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "Could not ensure existance of parent folder of '%(file)' - does not exist and/or could not be created", "file", file.getPath());
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    static public synchronized String getFileCounter(CallContext context, String file_name, String format) {
        Vector<String> counter_lines = FileUtilities.tryReadFile(context, file_name);
        long counter = 0;
        if (counter_lines != null && counter_lines.size() > 0) {
            counter = Long.parseLong(counter_lines.get(0));
        }
        // jdk 1.6   (new File(file_name)).setWritable(true);
        FileUtilities.modifyPermissions(context, file_name, null, "+w");
        try { Thread.sleep(250); } catch (Throwable t) { }
        FileUtilities.writeFile(context, file_name, (new Long(counter + 1)).toString());
        return String.format(format, counter);
    }

    static public String getDate(CallContext context, String format) {
        return getDate(context, format, null, null, null, null);
    }

    static public String getDate(CallContext context, String format, Date date) {
        return getDate(context, format, date, null, null, null);
    }

    static public String getDate(CallContext context, String format, String addD, String addM, String addY) {
        return getDate(context, format, null, addD, addM, addY);
    }

    static public String getDate(CallContext context, String format, Date date, String addD, String addM, String addY) {
        java.text.SimpleDateFormat tsfmt = new java.text.SimpleDateFormat(format);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date != null ? date : new Date());
        if (addD != null && addD.matches("[+-][0-9]+")) {
            int add = Integer.parseInt(addD.substring(1));
            if (add != 0) {
                cal.add(Calendar.DAY_OF_MONTH, (addD.charAt(0) == '-' ? -1 : 1) * add);
            }
        }
        if (addM != null && addM.matches("[+-][0-9]+")) {
            int add = Integer.parseInt(addM.substring(1));
            if (add != 0) {
                cal.add(Calendar.MONTH, (addM.charAt(0) == '-' ? -1 : 1) * add);
            }
        }
        if (addY != null && addY.matches("[+-][0-9]+")) {
            int add = Integer.parseInt(addY.substring(1));
            if (add != 0) {
                cal.add(Calendar.YEAR, (addY.charAt(0) == '-' ? -1 : 1) * add);
            }
        }
        return tsfmt.format(cal.getTime());
    }

    static public Date parseDate(CallContext context, String format, String date) {
        java.text.SimpleDateFormat tsfmt = new java.text.SimpleDateFormat(format);
        ParsePosition pp = new ParsePosition(0);
        return tsfmt.parse(date, pp);
    }

    static public String reformatDate(CallContext context, String format1, String format2, String date) {
        return getDate(context, format2, parseDate(context, format1, date));
    }

    static public String format(CallContext context, String format, Object value) {
        if (value instanceof Date) {
            return getDate(context, format, (Date) value);
        }
        return String.format(format, value);
    }
}
