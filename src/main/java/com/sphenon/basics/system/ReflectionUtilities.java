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

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtilities {

    public ReflectionUtilities (CallContext context) {
    }

    static public Field getField(CallContext context, Class a_class, String name) {
        try {
            return a_class.getField(name);
        } catch (NoSuchFieldException nsme) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, nsme, "No such field '%(field)'", "field", name);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Field tryGetField(CallContext context, Class a_class, String name) {
        try {
            this.exception = null;
            return a_class.getField(name);
        } catch (NoSuchFieldException nsme) {
            this.exception = nsme;
        }
        return null;
    }

    static public Field getField(CallContext context, String class_name, String name) {
        try {
            return getField(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), name);
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, cnfe, "No such class");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Field tryGetField(CallContext context, String class_name, String name) {
        try {
            this.exception = null;
            return tryGetField(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), name);
        } catch (ClassNotFoundException cnfe) {
            this.exception = cnfe;
        }
        return null;
    }

    static public Object getValue(CallContext context, Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not access field '%(field)'", "field", field.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (IllegalArgumentException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not access field '%(field)'", "field", field.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Object tryGetValue(CallContext context, Field field, Object instance) {
        if (field == null) { return null; }
        try {
            this.exception = null;
            return field.get(instance);
        } catch (IllegalAccessException iae) {
            this.exception = iae;
        } catch (IllegalArgumentException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not access field '%(field)'", "field", field.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        }
        return null;
    }

    static public Object tryGetValue(CallContext context, String class_name, String field_name, Object instance) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        return ru.tryGetValue(context, ru.tryGetField(context, class_name, field_name), instance);
    }

    static public Object tryGetValue(CallContext context, Class a_class, String field_name, Object instance) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        return ru.tryGetValue(context, ru.tryGetField(context, a_class, field_name), instance);
    }

    static public void setValue(CallContext context, Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not modify field '%(field)'", "field", field.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (IllegalArgumentException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not modify field '%(field)'", "field", field.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public void trySetValue(CallContext context, Field field, Object instance, Object value) {
        if (field == null) { return; }
        try {
            this.exception = null;
            field.set(instance, value);
        } catch (IllegalAccessException iae) {
            this.exception = iae;
        } catch (IllegalArgumentException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not modify field '%(field)'", "field", field.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    static public void trySetValue(CallContext context, String class_name, String field_name, Object instance, Object value) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        ru.trySetValue(context, ru.tryGetField(context, class_name, field_name), instance, value);
    }

    static public void trySetValue(CallContext context, Class a_class, String field_name, Object instance, Object value) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        ru.trySetValue(context, ru.tryGetField(context, a_class, field_name), instance, value);
    }

    static public void trySetValue(CallContext context, String field_name, Object instance, Object value) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        ru.trySetValue(context, ru.tryGetField(context, instance.getClass(), field_name), instance, value);
    }

    static public Method getMethod(CallContext context, Class a_class, String name, Class... formalarguments) {
        try {
            return a_class.getMethod(name, formalarguments);
        } catch (NoSuchMethodException nsme) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, nsme, "No such method '%(method)'", "method", name);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Method tryGetMethod(CallContext context, Class a_class, String name, Class... formalarguments) {
        try {
            this.exception = null;
            return a_class.getMethod(name, formalarguments);
        } catch (NoSuchMethodException nsme) {
            this.exception = nsme;
        }
        return null;
    }

    static public Method getMethod(CallContext context, String class_name, String name, Class... formalarguments) {
        try {
            return getMethod(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), name, formalarguments);
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, cnfe, "No such class");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Method tryGetMethod(CallContext context, String class_name, String name, Class... formalarguments) {
        try {
            this.exception = null;
            return tryGetMethod(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), name, formalarguments);
        } catch (ClassNotFoundException cnfe) {
            this.exception = cnfe;
        }
        return null;
    }

    static public Method getMatchingMethod(CallContext context, Class a_class, String name, Class... formalarguments) {
        Method method = tryGetMatchingMethod(context, a_class, name, formalarguments);
        if (method == null) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "No such method '%(method)'", "method", name);
            throw (ExceptionConfigurationError) null; // compiler insists
        }
        return method;
    }

    static public Method tryGetMatchingMethod(CallContext context, Class a_class, String name, Class... formalarguments) {
        for (Method m : a_class.getDeclaredMethods()) {
            if (m.getName().equals(name) == false) {
                continue;
            }
            Class[] pts = m.getParameterTypes();
            int size1 = (formalarguments == null ? 0 : formalarguments.length);
            int size2 = (pts == null ? 0 : pts.length);
            if (size1 != size2) {
                continue;
            }
            for (int i=0; i<size1; i++) {
                if (formalarguments[i] != null && pts[i].isAssignableFrom(formalarguments[i]) == false) {
                    continue;
                }
            }
            return m;
        }
        if (a_class.getSuperclass() != null) {
            return tryGetMatchingMethod(context, a_class.getSuperclass(), name, formalarguments);
        }
        return null;
    }

    static public Method getMatchingMethod(CallContext context, String class_name, String name, Class... formalarguments) {
        try {
            return getMatchingMethod(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), name, formalarguments);
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, cnfe, "No such class");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Method tryGetMatchingMethod(CallContext context, String class_name, String name, Class... formalarguments) {
        try {
            this.exception = null;
            return tryGetMatchingMethod(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), name, formalarguments);
        } catch (ClassNotFoundException cnfe) {
            this.exception = cnfe;
        }
        return null;
    }

    static public Object invoke(CallContext context, Method method, Object instance, Object... arguments) {
        try {
            return method.invoke(instance, arguments);
        } catch (IllegalAccessException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not invoke method '%(method)'", "method", method.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (IllegalArgumentException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not invoke method '%(method)'", "method", method.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof java.lang.RuntimeException) {
                throw (java.lang.RuntimeException) ite.getTargetException();
            }
            if (ite.getTargetException() instanceof java.lang.Error) {
                throw (java.lang.Error) ite.getTargetException();
            }
            CustomaryContext.create((Context)context).throwConfigurationError(context, ite.getTargetException(), "Could not invoke method '%(method)'", "method", method.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (Throwable t) {
            CustomaryContext.create((Context)context).throwEnvironmentError(context, t, "Could not invoke method '%(method)' on instance '%(instance)'", "method", method.getName(), "instance", ""+instance);
            throw (ExceptionEnvironmentError) null; // compiler insists
        }
        
    }

    public Object tryInvoke(CallContext context, Method method, Object instance, Object... arguments) {
        if (method == null) { return null; }
        try {
            this.exception = null;
            return method.invoke(instance, arguments);
        } catch (IllegalAccessException iae) {
            this.exception = iae;
        } catch (InvocationTargetException ite) {
            this.exception = ite.getTargetException();
        }
        return null;
    }

    static public Object invoke(CallContext context, String class_name, String method_name, Object instance, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        Method method = ru.tryGetMatchingMethod(context, class_name, method_name, getClassesOfArguments(context, arguments));
        return ru.invoke(context, method, instance, arguments);
    }

    static public Object tryInvoke(CallContext context, String class_name, String method_name, Object instance, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        Method method = ru.tryGetMatchingMethod(context, class_name, method_name, getClassesOfArguments(context, arguments));
        return ru.tryInvoke(context, method, instance, arguments);
    }

    static public Object tryInvoke(CallContext context, Class a_class, String method_name, Object instance, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        Method method = ru.tryGetMatchingMethod(context, a_class, method_name, getClassesOfArguments(context, arguments));
        return ru.tryInvoke(context, method, instance, arguments);
    }

    static public Object tryInvoke(CallContext context, String method_name, Object instance, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        Method method = ru.tryGetMatchingMethod(context, instance.getClass(), method_name, getClassesOfArguments(context, arguments));
        return ru.tryInvoke(context, method, instance, arguments);
    }

    static public Constructor getConstructor(CallContext context, String class_name, Class... formalarguments) {
        try {
            return getConstructor(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), formalarguments);
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, cnfe, "No such class");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Constructor tryGetConstructor(CallContext context, String class_name, Class... formalarguments) {
        try {
            this.exception = null;
            return tryGetConstructor(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), formalarguments);
        } catch (ClassNotFoundException cnfe) {
            this.exception = cnfe;
        }
        return null;
    }

    static public Constructor getConstructor(CallContext context, Class a_class, Class... formalarguments) {
        try {
            return a_class.getConstructor(formalarguments);
        } catch (NoSuchMethodException nsme) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, nsme, "No such constructor");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Constructor tryGetConstructor(CallContext context, Class a_class, Class... formalarguments) {
        try {
            this.exception = null;
            return a_class.getConstructor(formalarguments);
        } catch (NoSuchMethodException nsme) {
            this.exception = nsme;
        }
        return null;
    }

    static public Constructor getMatchingConstructor(CallContext context, Class a_class, Class... formalarguments) {
        Constructor constructor = tryGetMatchingConstructor(context, a_class, formalarguments);
        if (constructor == null) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, "No such constructor");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
        return constructor;
    }

    static public Constructor tryGetMatchingConstructor(CallContext context, Class a_class, Class... formalarguments) {
        ConstructorMatchResult cmr = tryGetMatchingConstructor(context, false, a_class, formalarguments);
        return cmr == null ? null : cmr.constructor;
    }

    static protected class ConstructorMatchResult {
        public ConstructorMatchResult(Constructor constructor, boolean add_context) {
            this.constructor = constructor; this.add_context = add_context;
        }
        public Constructor constructor;
        public boolean     add_context;
        public Object[]    arguments;
    }

    static public ConstructorMatchResult tryGetMatchingConstructor(CallContext context, boolean optional_context, String class_name, Class... formalarguments) {
        try {
            return tryGetMatchingConstructor(context, optional_context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), formalarguments);
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, cnfe, "No such class");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    static public ConstructorMatchResult tryGetMatchingConstructor(CallContext context, boolean optional_context, Class a_class, Class... formalarguments) {
        for (Constructor c : a_class.getDeclaredConstructors()) {
            Class[] pts = c.getParameterTypes();
            int size1 = (formalarguments == null ? 0 : formalarguments.length);
            int size2 = (pts == null ? 0 : pts.length);
            int add_context = optional_context && size2 > 0 && pts[0].isAssignableFrom(CallContext.class) ? 1 : 0;
            if (size1 + add_context != size2) {
                continue;
            }
            for (int i=0; i<size1; i++) {
                if (pts[i + add_context].isAssignableFrom(formalarguments[i]) == false) {
                    continue;
                }
            }
            return new ConstructorMatchResult(c, add_context == 1 ? true : false);
        }
        if (a_class.getSuperclass() != null) {
            return tryGetMatchingConstructor(context, optional_context, a_class.getSuperclass(), formalarguments);
        }
        return null;
    }

    static public Constructor getMatchingConstructor(CallContext context, String class_name, Class... formalarguments) {
        try {
            return getMatchingConstructor(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), formalarguments);
        } catch (ClassNotFoundException cnfe) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, cnfe, "No such class");
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Constructor tryGetMatchingConstructor(CallContext context, String class_name, Class... formalarguments) {
        try {
            this.exception = null;
            return tryGetMatchingConstructor(context, com.sphenon.basics.cache.ClassCache.getClassForName(context, class_name), formalarguments);
        } catch (ClassNotFoundException cnfe) {
            this.exception = cnfe;
        }
        return null;
    }

    static public Object newInstance(CallContext context, Constructor constructor, Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (InstantiationException ia) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, ia, "Could not create new instance of class '%(class)'", "class", constructor.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (IllegalAccessException iae) {
            CustomaryContext.create((Context)context).throwConfigurationError(context, iae, "Could not create new instance of class '%(class)'", "class", constructor.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() instanceof java.lang.RuntimeException) {
                throw (java.lang.RuntimeException) ite.getTargetException();
            }
            if (ite.getTargetException() instanceof java.lang.Error) {
                throw (java.lang.Error) ite.getTargetException();
            }
            CustomaryContext.create((Context)context).throwConfigurationError(context, ite.getTargetException(), "Could not create new instance of class '%(class)'", "class", constructor.getName());
            throw (ExceptionConfigurationError) null; // compiler insists
        }
    }

    public Object tryNewInstance(CallContext context, Constructor constructor, Object... arguments) {
        if (constructor == null) { return null; }
        try {
            this.exception = null;
            return constructor.newInstance(arguments);
        } catch (InstantiationException ia) {
            this.exception = ia;
        } catch (IllegalAccessException iae) {
            this.exception = iae;
        } catch (InvocationTargetException ite) {
            this.exception = ite.getTargetException();
        }
        return null;
    }

    static public Object tryNewInstance(CallContext context, String class_name, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        return ru.tryNewInstance(context, ru.tryGetMatchingConstructor(context, class_name, getClassesOfArguments(context, arguments)), arguments);
    }

    static protected void prepareArguments(CallContext context, ConstructorMatchResult cmr, Object... arguments) {
        boolean add_context = cmr != null && cmr.add_context;
        int alen = arguments == null ? 0 : arguments.length;
        cmr.arguments = add_context ? new Object[alen + 1] : arguments;
        if (add_context) {
            cmr.arguments[0] = context;
            for (int i=0; i<alen; i++) {
                cmr.arguments[i+1] = arguments[i];
            }
        }
    }

    static public Object tryNewInstance(CallContext context, boolean optional_context, String class_name, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        ConstructorMatchResult cmr = ru.tryGetMatchingConstructor(context, optional_context, class_name, getClassesOfArguments(context, arguments));
        prepareArguments(context, cmr, arguments);
        return ru.tryNewInstance(context, cmr == null ? null : cmr.constructor, cmr == null ? null : cmr.arguments);
    }

    static public Object newInstance(CallContext context, boolean optional_context, String class_name, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        ConstructorMatchResult cmr = ru.tryGetMatchingConstructor(context, optional_context, class_name, getClassesOfArguments(context, arguments));
        prepareArguments(context, cmr, arguments);
        return ru.newInstance(context, cmr == null ? null : cmr.constructor, cmr == null ? null : cmr.arguments);
    }

    static public Object tryNewInstance(CallContext context, Class a_class, Object... arguments) {
        ReflectionUtilities ru = new ReflectionUtilities(context);
        return ru.tryNewInstance(context, ru.tryGetMatchingConstructor(context, a_class, getClassesOfArguments(context, arguments)), arguments);
    }

    static public Class[] getClassesOfArguments(CallContext context, Object... arguments) {
        Class[] classes = new Class[arguments.length];
        int i=0;
        for (Object argument : arguments) {
            classes[i++] = argument == null ? null : argument.getClass();
        }
        return classes;
    }

    static public String convertToSubClassOfObject (CallContext context, Class c) {
        if (c.isPrimitive()) {
            String cn = c.getCanonicalName();
            if (cn.equals("boolean")) { return "java.lang.Boolean"; }
            if (cn.equals("char"))    { return "java.lang.Char"; }
            if (cn.equals("int"))     { return "java.lang.Integer"; }
            if (cn.equals("long"))    { return "java.lang.Long"; }
            if (cn.equals("float"))   { return "java.lang.Float"; }
            if (cn.equals("double"))  { return "java.lang.Double"; }
            return "???";
        } else {
            return c.getCanonicalName();
        }
    }

    protected Throwable exception;

    public Throwable getThrowable (CallContext context) {
        return this.exception;
    }

    public void setThrowable (CallContext context, Throwable exception) {
        this.exception = exception;
    }
}
