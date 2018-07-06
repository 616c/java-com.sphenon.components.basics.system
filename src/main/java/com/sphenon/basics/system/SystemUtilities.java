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

import java.util.UUID;
import java.util.Random;
import java.security.SecureRandom;

public class SystemUtilities {

    protected SystemUtilities (CallContext context) {
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

    static public Date getDate(CallContext context, String format, String date) {
        java.text.SimpleDateFormat tsfmt = new java.text.SimpleDateFormat(format);
        ParsePosition pp = new ParsePosition(0);
        return tsfmt.parse(date, pp);
    }

    static public String reformatDate(CallContext context, String format1, String format2, String date) {
        return getDate(context, format2, getDate(context, format1, date));
    }

    static public String format(CallContext context, String format, Object value) {
        if (value instanceof Date) {
            return getDate(context, format, (Date) value);
        }
        return String.format(format, value);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // concerning random
    //
    // https://tersesystems.com/2015/12/17/the-right-way-to-use-securerandom/
    // https://www.2uo.de/myths-about-urandom/
    // https://docs.oracle.com/javase/7/docs/api/java/util/Random.html
    // https://docs.oracle.com/javase/7/docs/api/java/security/SecureRandom.html

    static public SecureRandom createRandom(CallContext context) {
        return new SecureRandom();
    }

    private final static char[] hex = "0123456789abcdef".toCharArray();

    static public String getRandom(CallContext context, int bytes, boolean uppercase, SecureRandom random) {
        byte[] ba = new byte[bytes];
        char[] ha = new char[bytes * 2];
        if (random == null) { random = new SecureRandom(); }
        random.nextBytes(ba);
        for (int j = 0; j < bytes; j++) {
            int v = ba[j] & 0xFF;
            ha[j * 2] = hex[v >>> 4];
            ha[j * 2 + 1] = hex[v & 0x0F];
        }
        String result = new String(ha);
        return uppercase ? result.toUpperCase() : result;
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // concerning UUID
    //
    // https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html
    // https://www.ietf.org/rfc/rfc4122.txt
    // https://news.ycombinator.com/item?id=10631806    
    //
    // http://toddfredrich.com/ids-in-rest-api.html
    // https://duckduckgo.com/?q=rest+uuid+unique+token&t=hz&ia=qa
    // https://security.stackexchange.com/questions/890/are-guids-safe-for-one-time-tokens
    //
    // see also: https://jwt.io/introduction/ (JSON Web Tokens)

    static public String getUUID(CallContext context, boolean uppercase, boolean dashes, int additional_random_bytes) {
        String uuid = UUID.randomUUID().toString();
        if (additional_random_bytes != 0) {
            uuid += '-' + getRandom(context, additional_random_bytes, false, null);
        }
        if (dashes == false) {
            uuid = uuid.replace("-","");
        }
        if (uppercase) {
            uuid = uuid.toUpperCase();
        }
        return uuid;
    }

    static public boolean checkUUID(CallContext context, String uuid, boolean uppercase, boolean dashes, int additional_random_bytes) {
        return checkUUID(context, uuid, uppercase, dashes, additional_random_bytes, false);
    }

    static public boolean checkUUID(CallContext context, String uuid, boolean uppercase, boolean dashes, int additional_random_bytes, boolean zero) {
        if (uuid == null) { return false; }
        String dash    = (dashes ? "-" : "");
        String digit   = (zero ? "0" : (uppercase ? "[0-9A-F]" : "[0-9A-Fa-f]"));
        String random  = (additional_random_bytes == 0 ? "" : (dash + digit + "{" + (2 * additional_random_bytes) + "}"));
        String pattern = "^"
                       + digit + "{8}" + dash
                       + digit + "{4}" + dash
                       + digit + "{4}" + dash
                       + digit + "{4}" + dash
                       + digit + "{12}"
                       + random
                       + "$";
        return (uuid.matches(pattern) ? true : false);
    }
}
