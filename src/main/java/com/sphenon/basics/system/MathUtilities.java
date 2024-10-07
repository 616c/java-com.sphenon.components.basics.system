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

import java.util.*;

public class MathUtilities {

    protected MathUtilities (CallContext context) {
    }

    static public Number size(CallContext context, Object object) {
        int count = 0;
        if (object instanceof Iterable) {
            for (Object item : ((Iterable) object)) {
                count++;
            }
        } else if (object instanceof String) {
            count = ((String) object).length();
        }
        return count;
    }

    static public Number average(CallContext context, Object object) {
        int count = 0;
        double sum = 0;
        if (object instanceof Iterable) {
            for (Object item : ((Iterable) object)) {
                sum += getNumber(context, item).doubleValue();
                count++;
            }
        }
        return sum / count;
    }

    static public Number product(CallContext context, Object object) {
        double product = 1;
        if (object instanceof Iterable) {
            for (Object item : ((Iterable) object)) {
                product *= getNumber(context, item).doubleValue();
            }
        }
        return product;
    }

    static public Number sum(CallContext context, Object object) {
        double sum = 0;
        if (object instanceof Iterable) {
            for (Object item : ((Iterable) object)) {
                sum += getNumber(context, item).doubleValue();
            }
        }
        return sum;
    }

    static public Number minimum(CallContext context, Object object) {
        Double minimum = null;
        if (object instanceof Iterable) {
            for (Object item : ((Iterable) object)) {
                double n = getNumber(context, item).doubleValue();
                if (minimum == null || n < minimum.doubleValue()) { minimum = n; }
            }
        }
        return minimum;
    }

    static public Number maximum(CallContext context, Object object) {
        Double maximum = null;
        if (object instanceof Iterable) {
            for (Object item : ((Iterable) object)) {
                double n = getNumber(context, item).doubleValue();
                if (maximum == null || n > maximum.doubleValue()) { maximum = n; }
            }
        }
        return maximum;
    }

    static public Number getNumber(CallContext context, Object object) {
        if (object instanceof Number) {
            return (Number) object;
        } else if (object instanceof String) {
            String s = (String) object;
            if (s.matches("[0-9]+")) {
                return Long.parseLong(s);
            } else if (s.matches("[0-9]+\\.[0-9]+")) {
                return Double.parseDouble(s);
            }
        }
        return 0;
    }
}
