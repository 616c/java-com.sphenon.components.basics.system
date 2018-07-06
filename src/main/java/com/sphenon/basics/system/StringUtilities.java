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
import com.sphenon.basics.exception.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;

import java.util.Vector;
import java.util.Iterator;
import java.text.SimpleDateFormat;

public class StringUtilities {

    static public final String[] empty_array = new String[0];

    static public String join(CallContext context, String[] strings, String separator) {
        return join(context, strings, null, separator, null, true, true, null, null);
    }

    static public String join(CallContext context, String[] strings, String prefix, String separator, String postfix, boolean include_empty) {
        return join(context, strings, prefix, separator, postfix, include_empty, true, null, null);
    }

    static public String join(CallContext context, String[] strings, String prefix, String separator, String postfix, boolean include_empty, boolean always_non_null, String include, String exclude) {
        return join(context, strings, prefix, separator, postfix, null, null, include_empty, always_non_null, include, exclude);
    }

    static public String join(CallContext context, String[] strings, String prefix, String separator, String postfix, String item_prefix, String item_postfix, boolean include_empty, boolean always_non_null, String include, String exclude) {
        StringBuffer sb = (always_non_null || prefix != null || postfix != null ? new StringBuffer() : null);
        if (prefix != null) { sb.append(prefix); }
        boolean first = true;
        if (strings != null) {
            for (String string : strings) {
                if (    (include_empty || (string != null && string.length() != 0))
                     && (include == null || string.matches(include) == true)
                     && (exclude == null || string.matches(exclude) == false)
                   ) {
                    if (sb == null) { sb = new StringBuffer(); }
                    if (first == false && separator != null) { sb.append(separator); }
                    first = false;
                    if (item_prefix != null) { sb.append(item_prefix); }
                    sb.append(string == null ? "" : string);
                    if (item_postfix != null) { sb.append(item_postfix); }
                }
            }
        }
        if (postfix != null) { sb.append(postfix); }
        return sb == null ? null : sb.toString();
    }

    static public String join(CallContext context, String[] strings, String separator, boolean include_empty) {
        return join(context, strings, null, separator, null, include_empty, true, null, null);
    }

    static public String join(CallContext context, String separator, boolean include_empty, String... strings) {
        return join(context, strings, null, separator, null, include_empty, true, null, null);
    }

    static public String join(CallContext context, String separator, boolean include_empty, boolean always_non_null, String... strings) {
        return join(context, strings, null, separator, null, include_empty, always_non_null, null, null);
    }

    static public String join(CallContext context, Object[] objects, String prefix, String separator, String postfix, boolean include_empty) {
        return join(context, objects, prefix, separator, postfix, null, null, include_empty, true);
    }

    static public String join(CallContext context, Object[] objects, String prefix, String separator, String postfix, String item_prefix, String item_postfix, boolean include_empty, boolean always_non_null) {
        StringBuffer sb = (always_non_null || prefix != null || postfix != null ? new StringBuffer() : null);
        if (prefix != null) { sb.append(prefix); }
        boolean first = true;
        if (objects != null) {
            for (Object object : objects) {
                String string = MessageAware.ToString.convert(context, object);
                if (include_empty || (string != null && string.length() != 0)) {
                    if (sb == null) { sb = new StringBuffer(); }
                    if (first == false && separator != null) { sb.append(separator); }
                    first = false;
                    if (item_prefix != null) { sb.append(item_prefix); }
                    sb.append(string == null ? "" : string);
                    if (item_postfix != null) { sb.append(item_postfix); }
                }
            }
        }
        if (postfix != null) { sb.append(postfix); }
        return sb == null ? null : sb.toString();
    }

    static public String join(CallContext context, Iterator iterator, String separator, boolean include_empty) {
        return join(context, iterator, null, separator, null, null, null, include_empty, true);
    }

    static public String join(CallContext context, Iterator iterator, String prefix, String separator, String postfix, String item_prefix, String item_postfix, boolean include_empty, boolean always_non_null) {
        StringBuffer sb = (always_non_null || prefix != null || postfix != null ? new StringBuffer() : null);
        if (prefix != null) { sb.append(prefix); }
        boolean first = true;
        while (iterator.hasNext()) {
            String string = MessageAware.ToString.convert(context, iterator.next());
            if (include_empty || (string != null && string.length() != 0)) {
                if (sb == null) { sb = new StringBuffer(); }
                if (first == false && separator != null) { sb.append(separator); }
                first = false;
                if (item_prefix != null) { sb.append(item_prefix); }
                sb.append(string == null ? "" : string);
                if (item_postfix != null) { sb.append(item_postfix); }
            }
        }
        if (postfix != null) { sb.append(postfix); }
        return sb == null ? null : sb.toString();
    }

    static public String join(CallContext context, Iterable iterable, String separator, boolean include_empty) {
        return join(context, iterable, null, separator, null, null, null, include_empty, true);
    }

    static public String join(CallContext context, Iterable iterable, String separator) {
        return join(context, iterable, null, separator, null, null, null, true, true);
    }

    static public String join(CallContext context, Iterable iterable, String prefix, String separator, String postfix) {
        return join(context, iterable, prefix, separator, postfix, null, null, true, true);
    }

    static public String join(CallContext context, Iterable iterable, String prefix, String separator, String postfix, String item_prefix, String item_postfix) {
        return join(context, iterable, prefix, separator, postfix, item_prefix, item_postfix, true, true);
    }

    static public String join(CallContext context, Iterable iterable, String prefix, String separator, String postfix, String item_prefix, String item_postfix, boolean include_empty, boolean always_non_null) {
        return join(context, iterable.iterator(), prefix, separator, postfix, item_prefix, item_postfix, include_empty, always_non_null);
    }

    static public String[] merge(CallContext context, boolean create_only_if_not_all_null, boolean create_only_if_not_empty, String[]... stringss) {
        int len = 0;
        boolean all_null = true;
        for (String[] strings : stringss) {
            if (strings != null) {
                all_null = false;
                len += strings.length;
            }
        }
        if (all_null) {
            if (create_only_if_not_all_null || create_only_if_not_empty) { return null; }
        } else if (len == 0) {
            if (create_only_if_not_empty) { return null; }
        }
        String[] result = new String[len];
        int i=0;
        for (String[] strings : stringss) {
            if (strings != null) {
                for (String string : strings) {
                    result[i++] = string;
                }
            }
        }
        return result;
    }

    static public String[] merge4JS(CallContext context, boolean create_only_if_not_all_null, boolean create_only_if_not_empty, String[] s1) {
        return merge(context, create_only_if_not_all_null, create_only_if_not_empty, s1);
    }

    static public String[] merge4JS(CallContext context, boolean create_only_if_not_all_null, boolean create_only_if_not_empty, String[] s1, String[] s2) {
        return merge(context, create_only_if_not_all_null, create_only_if_not_empty, s1, s2);
    }

    static public String[] merge4JS(CallContext context, boolean create_only_if_not_all_null, boolean create_only_if_not_empty, String[] s1, String[] s2, String[] s3) {
        return merge(context, create_only_if_not_all_null, create_only_if_not_empty, s1, s2, s3);
    }

    static public String[] array(CallContext context, String... strings) {
        return strings;
    }

    static public String[] array4JS(CallContext context, String s1) {
        return array(context, s1);
    }

    static public String[] array4JS(CallContext context, String s1, String s2) {
        return array(context, s1, s2);
    }

    static public String[] array4JS(CallContext context, String s1, String s2, String s3) {
        return array(context, s1, s2, s3);
    }

    static public boolean areEqual(CallContext context, String s1, String s2, boolean treat_null_as_empty) {
        return (    s1 == s2
                 || (    treat_null_as_empty
                      && (s1 == null || s1.isEmpty())
                      && (s2 == null || s2.isEmpty())
                     )
                 || (    (s1 != null && s2 != null)
                      && s1.equals(s2)
                    )
               );
    }

    static public boolean areEqual(CallContext context, String[] sa1, String[] sa2) {
        return areEqual(context, sa1, sa2, true);
    }

    static public boolean areEqual(CallContext context, String[] sa1, String[] sa2, boolean treat_null_as_empty) {
        if (sa1 == sa2) { return true; }
        if (sa1 == null || sa2 == null || sa1.length != sa2.length) { return false; }
        for (int i=0; i<sa1.length; i++) {
            if (areEqual(context, sa1[i], sa2[i], treat_null_as_empty) == false) { return false; }
        }
        return true;
    }

    static protected SimpleDateFormat date_format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static public String formatTime(CallContext context, long time) {
        return time == -1 ? "---" : date_format.format(time);
    }

    static public boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    static public boolean isNotEmpty(String s) {
        return s != null && s.isEmpty() == false;
    }
}
