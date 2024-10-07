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
import com.sphenon.basics.exception.*;
import com.sphenon.basics.context.classes.*;
import com.sphenon.basics.message.*;
import com.sphenon.basics.notification.*;
import com.sphenon.basics.customary.*;
import com.sphenon.basics.function.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Iterator;
import java.text.SimpleDateFormat;

public class StringUtilities {

    static public final String[] empty_array = new String[0];
    static public final String emptyString = "";
    static public final String nullString = null;

    static public String decorate(CallContext context, String string) {
        String[] parts = string.split("\\|", -6);
        return decorate(context, parts != null && parts.length > 0 ? parts[0] : "",
                                 parts != null && parts.length > 1 ? parts[1] : null,
                                 parts != null && parts.length > 2 ? parts[2] : null,
                                 parts != null && parts.length > 3 ? parts[3] : "false",
                                 parts != null && parts.length > 4 ? parts[4] : "false",
                                 parts != null && parts.length > 5 ? parts[5] : "");
    }

    static public String decorate(CallContext context, String string, String non_null_prefix, String non_null_postfix, String treat_null_as_empty, String treat_empty_as_null, String default_if_null) {
        return decorate(context, string, non_null_prefix, non_null_postfix, treat_null_as_empty != null && treat_null_as_empty.matches("true|yes|\\+"), treat_empty_as_null != null && treat_empty_as_null.matches("true|yes|\\+"), default_if_null);
    }

    static public String decorate(CallContext context, String string, String non_null_prefix, String non_null_postfix, boolean treat_null_as_empty, boolean treat_empty_as_null, String default_if_null) {
        if (treat_null_as_empty && string == null) { string = ""; }
        if (treat_empty_as_null && string != null && string.isEmpty()) { string = null; }
        return (string == null ? default_if_null : ((non_null_prefix == null ? "" : non_null_prefix) + string + (non_null_postfix == null ? "" : non_null_postfix)));
    }

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
        return join(context, strings, prefix, separator, postfix, item_prefix, item_postfix, include_empty, always_non_null, include, exclude, null);
    }

    static public String join(CallContext context, String[] strings, String prefix, String separator, String postfix, String item_prefix, String item_postfix, boolean include_empty, boolean always_non_null, String include, String exclude, Converter<String,String> converter) {
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
                    if (converter != null) {
                        string = converter.convert(context, string);
                    }
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
        return join(context, iterator, prefix, separator, postfix, item_prefix, item_postfix, include_empty, always_non_null, true, true);
    }

    static public String join(CallContext context, Iterator iterator, String prefix, String separator, String postfix, String item_prefix, String item_postfix, boolean include_empty, boolean always_non_null, boolean always_prefix, boolean always_postfix) {
        StringBuffer sb = (always_non_null || prefix != null || postfix != null ? new StringBuffer() : null);
        boolean not_empty = iterator.hasNext();
        if (prefix != null && (always_prefix || not_empty)) { sb.append(prefix); }
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
        if (postfix != null && (always_postfix || not_empty)) { sb.append(postfix); }
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

    static public boolean areObjectsEqual(CallContext context, Object object1, Object object2) {
        return areObjectsEqual(context, object1, object2, (ctx, o1, o2) -> {
                   return (o1 instanceof String && o2 instanceof String
                            ? ((String) o1).equals((String) o2)
                            : o1 == o2
                          ) ? 0 : 1;
               });
    }

    static public boolean areObjectsEqual(CallContext context, Object object1, Object object2, Comparator comparator) {
        return (    (object1 == null && object2 == null)
                 || (    (object1 != null && object2 != null)
                      && comparator.compare(context, object1, object2) == 0
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

    static public boolean contains(CallContext context, String[] a, String s1, boolean treat_null_as_empty) {
        if (a != null) {
            for (String s2 : a) {
                if (areEqual(context, s1, s2, treat_null_as_empty)) {
                    return true;
                }
            }
        }
        return false;
    }

    static public boolean contains(CallContext context, List<String> l, String s1, boolean treat_null_as_empty) {
        if (l != null) {
            for (String s2 : l) {
                if (areEqual(context, s1, s2, treat_null_as_empty)) {
                    return true;
                }
            }
        }
        return false;
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

    static public String getNonNull(String s) {
        return s == null ? "" : s;
    }

    static public String nonNull(String s) {
        return s == null ? "" : s;
    }

    static public String nullIfEmpty(String s) {
        return (s == null || s.isEmpty() ? null : s);
    }

    // https://de.wikipedia.org/wiki/Levenshtein-Distanz
    // http://www.levenshtein.de/
    static public int distance(String s1, String s2, boolean trim_head_and_tail, boolean trim_inner_whitespace, boolean ignore_case, boolean replace_non_alphanumeric) {
        if (s1 == null) { s1 = ""; }
        if (s2 == null) { s2 = ""; }
        if (trim_head_and_tail) {
            s1 = s1.trim();
            s2 = s2.trim();
        }
        if (trim_inner_whitespace) {
            s1 = s1.replaceAll("\\s+", " ");
            s2 = s2.replaceAll("\\s+", " ");
        }
        if (ignore_case) {
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
        }
        if (replace_non_alphanumeric) {
            s1 = s1.replaceAll("[^A-Za-z0-9]+", " ");
            s2 = s2.replaceAll("[^A-Za-z0-9]+", " ");
        }
        int [] costs = new int [s2.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                                  s1.charAt(i - 1) == s2.charAt(j - 1)
                                   ? nw
                                   : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }

    // call distance(..., "string11", "string12", "weight1"
    //                  , "string21", "string22", "weight2"
    //                  , "string31", "string32", "weight3"
    //                  ...)
    static public float distance(boolean trim_head_and_tail, boolean trim_inner_whitespace, boolean ignore_case, boolean replace_non_alphanumeric, String... data) {
        float sum = 0.0F;
        int count = 0;
        for (int i=0; i<data.length; i+=3) {
            int distance = distance(data[0], data[1], trim_head_and_tail, trim_inner_whitespace, ignore_case, replace_non_alphanumeric);
            float weight = Float.parseFloat(data[2]);
            sum += distance * weight;
            count++;
        }
        float average = count == 0 ? 0.0F : (sum / count);
        return average;
    }

    // https://de.wikipedia.org/wiki/Levenshtein-Distanz#Damerau-Levenshtein-Distanz
    // allows transpositions characters (permuted characters)
    // public static int distanceDamerauLevenshtein(CharSequence source, CharSequence target) {
    //     if (source == null || target == null) {
    //         throw new IllegalArgumentException("Parameter must not be null");
    //     }
    //     int sourceLength = source.length();
    //     int targetLength = target.length();
    //     if (sourceLength == 0) return targetLength;
    //     if (targetLength == 0) return sourceLength;
    //     int[][] dist = new int[sourceLength + 1][targetLength + 1];
    //     for (int i = 0; i < sourceLength + 1; i++) {
    //         dist[i][0] = i;
    //     }
    //     for (int j = 0; j < targetLength + 1; j++) {
    //         dist[0][j] = j;
    //     }
    //     for (int i = 1; i < sourceLength + 1; i++) {
    //         for (int j = 1; j < targetLength + 1; j++) {
    //             int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
    //             dist[i][j] = Math.min(Math.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);
    //             if (i > 1 &&
    //                     j > 1 &&
    //                     source.charAt(i - 1) == target.charAt(j - 2) &&
    //                     source.charAt(i - 2) == target.charAt(j - 1)) {
    //                 dist[i][j] = Math.min(dist[i][j], dist[i - 2][j - 2] + cost);
    //             }
    //         }
    //     }
    //     return dist[sourceLength][targetLength];
    // }

    static public Map<String,String> makeMap(CallContext context, boolean always_non_null, String... arguments) {
        if (always_non_null == false && arguments == null) {
            return null;
        }

        Map<String,String> parameters = new HashMap<String,String>();
        
        if (arguments != null) {
            if (arguments.length % 2 != 0) {
                CustomaryContext.create((Context) context).throwPreConditionViolation(context, "Setup of variable arguments failed, number of provided strings is uneven");
                throw (ExceptionPreConditionViolation) null; // compiler insists
            }
            for (int a = 0; a < arguments.length; a += 2) {
                String name  = arguments[a];
                String value = arguments[a + 1];
                if (value != null) {
                    parameters.put(name, value);
                }
            }
        }

        return parameters;
    }

    static public enum Requirement { MustNotHave, Optional, MustHave };

    static public String trimPath(CallContext context, String path, char separator, Requirement leading, Requirement trailing, Requirement if_empty) {
        if (path == null) { path = ""; }
        boolean starts = (path.length() > 0 && path.charAt(0) == separator ? true : false);
        if (starts) { path = path.substring(1); }
        boolean ends   = (path.length() > 0 && path.charAt(path.length()-1) == separator ? true : false);
        if (ends) { path = path.substring(0, path.length()-1); }
        String seps = (new Character(separator)).toString();
        return (path.length() == 0 ?
                   (   if_empty == Requirement.MustNotHave ? ""
                     : if_empty == Requirement.MustHave    ? seps
                     : ((starts || ends) ? seps : "")
                   )
                 : (   (   leading == Requirement.MustNotHave ? ""
                         : leading == Requirement.MustHave    ? seps
                         : (starts ? seps : "")
                       )
                     + path
                     + (   trailing == Requirement.MustNotHave ? ""
                         : trailing == Requirement.MustHave    ? seps
                         : (ends ? seps : "")
                       )
                   )
               );
    }

    static public String concatPathes(CallContext context, String s1, String s2) {
        return concatPathes(context, s1, s2, '/', false, false);
    }

    static public String concatPathes(CallContext context, String s1, String s2, char separator, boolean allow_adjacent_separators, boolean allow_trailing_separator) {
        boolean s1_is_non_empty          = (s1 != null && s1.isEmpty() == false);
        boolean s2_is_non_empty          = (s2 != null && s2.isEmpty() == false);
        int     s1_len                   = (s1_is_non_empty ? s1.length() : 0);
        int     s2_len                   = (s2_is_non_empty ? s2.length() : 0);
        boolean s1_ends_with_separator   = (s1_is_non_empty && s1.charAt(s1_len-1) == separator);
        boolean s2_starts_with_separator = (s2_is_non_empty && s2.charAt(0) == separator);
        boolean s2_ends_with_separator   = (s2_is_non_empty && s2_len > 1 && s2.charAt(s2_len-1) == separator);

        StringBuilder sb = new StringBuilder();

        if (s1_is_non_empty) {
            if (    s1_ends_with_separator
                 && (    (s2_is_non_empty == true  && allow_adjacent_separators == false)
                      || (s2_is_non_empty == false && allow_trailing_separator == false)
                    )
               ) {
                sb.append(s1.substring(0, s1_len-1));
            } else {
                sb.append(s1);
            }
        }

        if (    s1_is_non_empty
             && s2_is_non_empty
           ) {
            sb.append(separator);
        }

        if (s2_is_non_empty) {
            if (    s2_starts_with_separator
                 && (s1_is_non_empty == true  && allow_adjacent_separators == false)
               ) {
                if (s2_ends_with_separator && allow_trailing_separator == false) {
                    sb.append(s2.substring(1, s2_len-2));
                    } else {
                    sb.append(s2.substring(1));
                }
            } else {
                if (s2_ends_with_separator && allow_trailing_separator == false) {
                    sb.append(s2.substring(0, s2_len-1));
                } else {
                    sb.append(s2);
                }
            }
        }

        return sb.toString();
    }

    static public String preview(CallContext context, String s, int max_length, boolean single_line) {
        if (s == null) { return ""; }
        if (s.length() > max_length) {
            if (max_length < 3) { return "..."; }
            s = s.substring(0, max_length-3);
            return (single_line ? s.replaceAll("[\n\r]", " ") : s) + "...";
        }
        return (single_line ? s.replaceAll("[\n\r]", " ") : s);
    }

    static public List<List<String>> parseToListList(CallContext context, String string, String list_separator, String list_list_separator) {
        List<List<String>> list = new ArrayList<List<String>>();

        for (String list_string : string.split(list_separator)) {
            List<String> list_list = new ArrayList<String>();
            if (list_string.isEmpty() == false) {
                String[] parts = list_string.split(list_list_separator, -1);
                for (String part : parts) {
                    list_list.add(com.sphenon.basics.configuration.RootConfiguration.decode(context, part));
                }
            }
            list.add(list_list);
        }

        return list;
    }

    static public String times(String s, int count) {
        StringBuilder sb = new StringBuilder();
        if (count >= 0) {
            for (int i=0; i<count; i++) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    static public String getDigest(CallContext context, String data) {
        return getDigest(context, data.getBytes());
    }

    private static final char HEX_CHARS[] = new char[] {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    static public String convertToHexString(CallContext context, byte[] bytes) {
        int i, n;
        int l = bytes.length;
        char[] chars = new char[l*2];
        for (i = l - 1; i >= 0; i--) {
            n = (int)bytes[i] & 0xFF;
            chars[i*2]   = HEX_CHARS[n/16];
            chars[i*2+1] = HEX_CHARS[n%16];
        }
        return new String(chars);
    }

    static public String getDigest(CallContext context, byte[] data) {
        MessageDigest md;
        String algorithm = "MD5"; // "SHA-512" "SHA-256" "SHA1"
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException nsae) {
            CustomaryContext.create(Context.create(context)).throwConfigurationError(context, "MessageDigest algorithm '%(algorithm)' not available", "algorithm", algorithm);
            throw (ExceptionConfigurationError) null; // compiler insists
        }

        md.update(data);

        byte[] bytes = md.digest();

        String s = convertToHexString(context, bytes);

        return s;
    }
}
