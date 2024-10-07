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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import java.util.Vector;

public class JarUtilities {

    static public Vector<String> tryReadResource(CallContext context, String resource_name) {
        return doReadResource(context, resource_name, false);
    }

    static public Vector<String> readResource(CallContext context, String resource_name) {
        return doReadResource(context, resource_name, true);
    }

    static public Vector<String> doReadResource(CallContext context, String resource_name, boolean throw_exception) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource_name);
                             // JarUtilities.class
            if (is == null) {
                if (throw_exception) {
                    CustomaryContext.create((Context)context).throwPreConditionViolation(context, "Cannot read resource '%(resourcename)'", "resourcename", resource_name);
                    throw (ExceptionPreConditionViolation) null; // compiler insists
                } else {
                    return null;
                }
            }
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            Vector<String> lines = new Vector<String>();
            String line;
            while ((line = br.readLine()) != null) {
                int len = line.length();
                lines.add((len > 0 && line.charAt(len - 1) == '\n') ? line.substring(0, len - 1) : line);
            }

            br.close();
            isr.close();
            is.close();

            return lines;

        } catch (UnsupportedEncodingException uee) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, uee, "Cannot read resource '%(resourcename)'", "resourcename", resource_name);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        } catch (IOException ioe) {
            if (throw_exception) {
                CustomaryContext.create((Context)context).throwPreConditionViolation(context, ioe, "Cannot read resource '%(resourcename)'", "resourcename", resource_name);
                throw (ExceptionPreConditionViolation) null; // compiler insists
            } else {
                return null;
            }
        }
    }
}

