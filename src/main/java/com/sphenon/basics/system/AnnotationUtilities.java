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

import java.lang.annotation.*;

import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;

public class AnnotationUtilities {

    static protected<A extends Annotation> A getAnnotation(Class from_class, Class<A> a_class) {
        Queue<Class> queue = new LinkedList<Class>();
        HashMap<Class,Integer> already_queued = new HashMap<Class,Integer>();
        queue.add(from_class);
        Class c;
        while ((c = queue.poll()) != null) {
            A a = getAnnotation(a_class, c, queue, already_queued); 
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    static protected<A extends Annotation> A getAnnotation(Class<A> a_class, Class c, Queue<Class> queue, HashMap<Class,Integer> already_queued) {
        A a = (A) c.getAnnotation(a_class);
        if (a != null) {
            return a;
        }
        Class sc = c.getSuperclass();
        if (sc != null && sc != Object.class && already_queued.get(sc) == null) {
            queue.add(sc);
            already_queued.put(sc, 1);
        }
        for (Class i : c.getInterfaces()) {
            if (already_queued.get(i) == null) {
                queue.add(i);
                already_queued.put(i, 1);
            }
        }
        return null;
    }
}

