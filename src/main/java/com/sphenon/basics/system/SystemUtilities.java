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
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.function.*;

import java.io.File;
import java.io.FilePermission;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.Vector;

import java.util.Date;
import java.util.Calendar;
import java.text.*;

import java.util.regex.*;

import java.util.UUID;
import java.util.Random;
import java.security.SecureRandom;

import java.net.InetAddress;
import java.net.NetworkInterface;

import java.util.Base64;

public class SystemUtilities {

    static final public Class _class = SystemUtilities.class;

    static protected long notification_level;
    static public    long adjustNotificationLevel(long new_level) { long old_level = notification_level; notification_level = new_level; return old_level; }
    static public    long getNotificationLevel() { return notification_level; }
    static { notification_level = NotificationLocationContext.getLevel(_class); };

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

    // see also LocationContext.java for a more elaborate version of this
    static public String format(CallContext context, String format, Object value) {
        if (value instanceof Date) {
            return getDate(context, format, (Date) value);
        }
        return String.format(format, value);
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    static public<T> int compareArrays(CallContext context, T[] array1, T[] array2, Comparator<T> comparator) {
        if (array1 == null && array2 != null) { return -1; }
        if (array1 != null && array2 == null) { return 1; }
        if (array1 != null && array2 != null) {
            for (int i=0; i<array1.length && i<array2.length; i++) {
                int result = comparator.compare(context, array1[i], array2[i]);
                if (result != 0) { return result; }
            }
            if (array1.length < array2.length) { return -1; }
            if (array1.length > array2.length) { return 1; }
        }
        return 0;
    }

    static public<T> int compareObjects(CallContext context, T value1, T value2) {
        return compareObjects(context, value1, value2, (c, v1, v2) -> { return (v1 instanceof Comparable ? ((Comparable) v1).compareTo(v2) : v1 == v2 ? 0 : 1); });
    }

    static public<T> int compareObjects(CallContext context, T value1, T value2, Comparator<T> comparator) {
        if (value1 == null && value2 == null) { return 0; }
        if (value1 == null) { return -1; }
        if (value2 == null) { return 1; }
        int result = (comparator == null ? (value1 == value2 ? 0 : 1) : comparator.compare(context, value1, value2));
        return result;
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

    static public byte[] getRandom(CallContext context, int bytes) {
        byte[] ba = new byte[bytes];
        SecureRandom random = new SecureRandom();
        random.nextBytes(ba);
        return ba;
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

    static public String getIntegerBase64(CallContext context, int value) {
        byte[] bytes = new byte[4];

        for (int i=3; i>=0; i--) {
            bytes[i]   = (byte) (value & 0x000000FF);
            value  >>= 8;
        }

        Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encoded = encoder.encodeToString(bytes);

        return encoded;
    }

    static public String getLongBase64(CallContext context, long value) {
        byte[] bytes = new byte[8];

        for (int i=7; i>=0; i--) {
            bytes[i]   = (byte) (value & 0x00000000000000FFL);
            value  >>= 8;
        }

        Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encoded = encoder.encodeToString(bytes);

        return encoded;
    }

    static public String getUUIDHex(CallContext context, boolean uppercase, boolean dashes, int additional_random_bytes) {
        return getUUIDHex(context, uppercase, dashes, false, additional_random_bytes, 4, null);
    }

    static public String getUUIDHex(CallContext context, boolean uppercase, boolean dashes, boolean braces, int additional_random_bytes) {
        return getUUIDHex(context, uppercase, dashes, braces, additional_random_bytes, 4, null);
    }

    static public String getUUIDHex(CallContext context, boolean uppercase, boolean dashes, int additional_random_bytes, int type) {
        return getUUIDHex(context, uppercase, dashes, false, additional_random_bytes, type, null);
    }

    static public String getUUIDHex(CallContext context, boolean uppercase, boolean dashes, boolean braces, int additional_random_bytes, int type) {
        return getUUIDHex(context, uppercase, dashes, braces, additional_random_bytes, type, null);
    }

    static public String getUUIDHex(CallContext context, boolean uppercase, boolean dashes, int additional_random_bytes, int type, String type_3_name) {
        return getUUIDHex(context, uppercase, dashes, false, additional_random_bytes, getUUID(context, type, type_3_name));
    }

    static public String getUUIDHex(CallContext context, boolean uppercase, boolean dashes, boolean braces, int additional_random_bytes, int type, String type_3_name) {
        return getUUIDHex(context, uppercase, dashes, braces, additional_random_bytes, getUUID(context, type, type_3_name));
    }

    static public String getUUIDHex(CallContext context, boolean uppercase, boolean dashes, boolean braces, int additional_random_bytes, UUID uuid) {
        String uuid_string = uuid.toString();
        if (additional_random_bytes != 0) {
            uuid_string += '-' + getRandom(context, additional_random_bytes, false, null);
        }
        if (dashes == false) {
            uuid_string = uuid_string.replace("-","");
        }
        if (uppercase) {
            uuid_string = uuid_string.toUpperCase();
        }
        if (braces) {
            uuid_string = "{" + uuid_string + "}";
        }
        return uuid_string;
    }

    static public String getUUIDBase64(CallContext context, int additional_random_bytes) {
        return getUUIDBase64(context, additional_random_bytes, 4, null);
    }

    static public String getUUIDBase64(CallContext context, int additional_random_bytes, int type) {
        return getUUIDBase64(context, additional_random_bytes, type, null);
    }

    static public String getUUIDBase64(CallContext context, int additional_random_bytes, int type, String type_3_name) {
        UUID uuid = getUUID(context, type, type_3_name);
        return getUUIDBase64(context, additional_random_bytes, uuid);
    }

    static public String getUUIDBase64(CallContext context, UUID uuid) {
        return getUUIDBase64(context, 0, uuid);
    }

    static public String getUUIDBase64(CallContext context, int additional_random_bytes, UUID uuid) {
        byte[] bytes = new byte[16 + additional_random_bytes];

        long high_uuid = uuid.getMostSignificantBits();
        long low_uuid  = uuid.getLeastSignificantBits();
        for (int i=7; i>=0; i--) {
            bytes[i]   = (byte) (high_uuid & 0x00000000000000FFL);
            bytes[i+8] = (byte) (low_uuid  & 0x00000000000000FFL);
            high_uuid >>= 8;
            low_uuid  >>= 8;
        }

        if (additional_random_bytes != 0) {
            byte[] additional = new byte[additional_random_bytes];
            Random random = new SecureRandom();
            random.nextBytes(additional);
            for (int i=0; i<additional_random_bytes; i++) {
                bytes[i+16] = additional[i];
            }
        }

        Base64.Encoder encoder = java.util.Base64.getEncoder();
        String encoded = encoder.encodeToString(bytes);

        return encoded;
    }

    static public UUID getUUID(CallContext context, int type, String type_3_name) {
        UUID uuid;
        switch (type) {
            case 1:
                uuid = createTimeBasedUUID(context);
                break;
            case 3:
                uuid = UUID.nameUUIDFromBytes(type_3_name.getBytes());
                break;
            case 4:
                uuid = UUID.randomUUID();
                break;
            default:
                CustomaryContext.create((Context)context).throwLimitation(context, "UUID type '%(type)' not available", "type", type);
                throw (ExceptionLimitation) null; // compilernsists
        }
        return uuid;
    }

	protected static Object lock = new Object();
	protected static long   last_time;
	protected static long   clock_sequence = 0;
	protected static long   host_identifier;

    static protected UUID createTimeBasedUUID(CallContext context) {
        // millis since epoch, midnight, January 1, 1970 UTC
        long millis = System.currentTimeMillis();

        // nanos since arbitrary point in time, maybe system startup
        long nanos  = System.nanoTime();

        // 100-nanosecond intervals since midnight 15 October 1582
        // (we ignore the fixed interval between 1970/1582 here,
        // this is not correct according to spec, but should serve
        // purposes here; also, we simply add the 10000th fraction
        // of nanos, which should not hurt for the same reason)
        long n100 = millis * 10000 + (nanos % 10000);

		synchronized (lock) {
			if (n100 > last_time) {
				last_time = n100;
				clock_sequence = 0;
			} else  { 
				++clock_sequence; 
			}
		}

		long high_uuid =   ((n100 & 0x00000000FFFFFFFFL) << 32)
                         | ((n100 & 0x0000FFFF00000000L) >> 16)
                         | ((n100 & 0x0FFF000000000000L) >> 48)
                         |                      0x1000L;

		long low_uuid  =                      0x1000000000000000L
                         | ((clock_sequence & 0x0000000000003FFFL) << 48)
                         |                     getHostId(context);

        return new UUID(high_uuid, low_uuid);
	}

    static protected boolean use_random_mac = true;

	static protected long getHostId(CallContext context) {
        if (host_identifier == 0L) {
            byte[] mac = null;

            if (use_random_mac == false) {
                try {
                    InetAddress address = InetAddress.getLocalHost();
                    NetworkInterface ni = NetworkInterface.getByInetAddress(address);
                    if (ni != null) {
                        mac = ni.getHardwareAddress();
                    } 
                } catch (Exception e) {
                    if ((notification_level & Notifier.MONITORING) != 0) { NotificationContext.sendCaution(context, "Could not obtain MAC address as base for calculation of UUIDs, using random address instead; reason: '%(reason)'", "reason", e); }
                }
            }

            if (use_random_mac || mac == null) {
                mac = new byte[6];
                Random random = new SecureRandom();
                random.nextBytes(mac);
                mac[0] |= 0x01; // marks random mac as "broadcast", required by spec
            }

            host_identifier = 0;
            for (int i = 0; i < mac.length; i++) {					
                host_identifier <<= 8;
                host_identifier |= (long) mac[i] & 0xFF;
            }
        }

		return host_identifier;
	}

    static public boolean checkUUIDHex(CallContext context, String uuid, boolean uppercase, boolean dashes, int additional_random_bytes) {
        return checkUUIDHex(context, uuid, uppercase, dashes, false, additional_random_bytes, false);
    }

    static public boolean checkUUIDHex(CallContext context, String uuid, boolean uppercase, boolean dashes, int additional_random_bytes, boolean zero) {
        return checkUUIDHex(context, uuid, uppercase, dashes, false, additional_random_bytes, zero);
    }

    static protected String getUUIDPattern(CallContext context, boolean uppercase, boolean dashes, boolean braces, int additional_random_bytes, boolean zero) {
        String dash    = (dashes ? "-" : "");
        String opening = (braces ? "\\{" : "");
        String digit   = (zero ? "0" : (uppercase ? "[0-9A-F]" : "[0-9A-Fa-f]"));
        String random  = (additional_random_bytes == 0 ? "" : (dash + digit + "{" + (2 * additional_random_bytes) + "}"));
        String closing = (braces ? "\\}" : "");
        String gb      = "(";
        String ge      = ")";
        String pattern = "^"
                       + opening
                       + gb + digit + "{8}" + ge + dash
                       + gb + digit + "{4}" + ge + dash
                       + gb + digit + "{4}" + ge + dash
                       + gb + digit + "{4}" + ge + dash
                       + gb + digit + "{12}" + ge
                       + gb + random + ge
                       + closing
                       + "$";
        return pattern;
    }

    static public boolean checkUUIDHex(CallContext context, String uuid, boolean uppercase, boolean dashes, boolean braces, int additional_random_bytes, boolean zero) {
        if (uuid == null) { return false; }
        String pattern = getUUIDPattern(context, uppercase, dashes, braces, additional_random_bytes, zero);
        return (uuid.matches(pattern) ? true : false);
    }

    static public UUID getUUID(CallContext context, String uuid_string) {
        int l = uuid_string == null ? -1 : uuid_string.length();
        boolean dashes    = false;
        boolean braces    = false;
        boolean uppercase = false;
        switch (l) {
            case 32: dashes = false; braces = false; break;
            case 34: dashes = false; braces = true;  break;
            case 36: dashes = true;  braces = false; break;
            case 38: dashes = true;  braces = true;  break;
            default:
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, "UUID string '%(uuid)', unexpected length %(length), expected 32, 34, 36, or 38", "uuid", uuid_string, "length", l);
                throw (ExceptionPreConditionViolation) null; // compiler insists
        }
        return getUUID(context, uuid_string, uppercase, dashes, braces);
    }

    static public UUID getUUID(CallContext context, String uuid_string, boolean uppercase, boolean dashes, boolean braces) {
        String regexp = getUUIDPattern(context, uppercase, dashes, braces, 0, false);

        Pattern pattern = null;
        try {
            pattern = Pattern.compile(regexp);
        } catch (PatternSyntaxException pse) {
        }
        Matcher matcher = pattern.matcher(uuid_string);
        if ( ! matcher.find()) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "UUID string '%(uuid)' does not match '%(pattern)'", "uuid", uuid_string, "pattern", pattern);
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }

        try {
            UUID uuid = UUID.fromString(matcher.group(1) + "-" + matcher.group(2) + "-" + matcher.group(3) + "-" + matcher.group(4) + "-" + matcher.group(5));
            return uuid;
        } catch (IllegalArgumentException iae) {
            CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Could not parse UUID string '%(uuid)'", "uuid", "uuid_string");
            throw (ExceptionPreConditionViolation) null; // compiler insists
        }
    }

    static public String getObjectIdHex(CallContext context, Object object) {
        return Integer.toHexString(System.identityHashCode(object)).toUpperCase();
    }

    static public Object[] buildNamedItems(CallContext context, Object[] objects_1, Object... objects_2) {
        int length = 0;
        if (objects_1 != null) { length += objects_1.length; }
        if (objects_2 != null) { length += objects_2.length; }

        Object[] result = new Object[length];
        int i=0;
        if (objects_1 != null) {
            for (int i1=0; i1<objects_1.length; i1+=2) {
                result[i++] = objects_1[i1];
                result[i++] = objects_1[i1+1];
            }
        }
        if (objects_2 != null) {
            for (int i2=0; i2<objects_2.length; i2+=2) {
                result[i++] = objects_2[i2];
                result[i++] = objects_2[i2+1];
            }
        }
        return result;
    }
}
