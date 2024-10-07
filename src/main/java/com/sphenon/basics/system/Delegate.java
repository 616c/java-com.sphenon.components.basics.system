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

import java.lang.reflect.*;
import java.util.Vector;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Delegate<TargetType> implements InvocationHandler {

    protected TargetType target;
    protected Interceptor[] interceptors;

    protected Delegate (TargetType target, Interceptor... interceptors) {
        this.target           = target;
        this.interceptors     = interceptors;
    }

    public TargetType getTarget() {
        return this.target;
    }

    /**
       Use with caution!

       Only instance is updated, but no additional analysis of interfaces
       etc. is performed (it is not possible after creation of Proxy).

       Replace target only with target with identical interface set.
     */
    public void setTarget(TargetType target) {
        this.target = target;
    }

    public Interceptor[] getInterceptors() {
        return this.interceptors;
    }

    static protected Vector<Class> addInterfaces(Class a_class, Vector<Class> interfaces, HashSet<Class> added) {
        if (a_class == null) { return interfaces; }
        if (interfaces == null) {
            System.err.println("aI !");
            interfaces = new Vector<Class>();
            added = new HashSet<Class>();
        } else {
            if (added.contains(a_class)) { return interfaces; }
        }
        if (a_class.isInterface()) {
            interfaces.add(a_class);
            added.add(a_class);
        }
        Class[] is = a_class.getInterfaces();
        for (Class i : is) {
            addInterfaces(i, interfaces, added);
        }
        addInterfaces(a_class.getSuperclass(), interfaces, added);
        return interfaces;
    }

    static protected Class[] empty_array = new Class[0];

    static protected long count;
    static protected Map<Object,Constructor> proxy_class_constructors;
    // synchronized
    static protected<TargetType> Constructor<TargetType> getProxyClassContructor(Class<TargetType> target_class, Class... additional_interfaces) {
        Constructor<TargetType> constructor = null;

        // TO BE IMPROVED
        // cache should work per combination of Class & additional_interfaces
        // i.e. create util-key-class with equals and hashcode
        // class ProxyClassKey { protected Class[] classes; ...
        if (proxy_class_constructors == null) {
            proxy_class_constructors = new HashMap<Object,Constructor>();
        }
        Object key = (additional_interfaces == null ? target_class : new ProxyClassKey(target_class, additional_interfaces));
        constructor = proxy_class_constructors.get(key);
        if (constructor == null) {
            constructor = getProxyConstructor(target_class, additional_interfaces);
            proxy_class_constructors.put(key, constructor);
        }
        return constructor;
    }

    static public<TargetType> TargetType create(TargetType target, Interceptor... interceptors) {
        Constructor<TargetType> constructor = getProxyClassContructor((Class<TargetType>) target.getClass());
        return createInstance(constructor, target, interceptors);
    }

    static public<TargetType> TargetType create(TargetType target, Class additional_interface, Interceptor... interceptors) {
        Constructor<TargetType> constructor = getProxyClassContructor((Class<TargetType>) target.getClass(), new Class[] { additional_interface });
        return createInstance(constructor, target, interceptors);
    }

    static public<TargetType> Constructor<TargetType> getProxyConstructor(Class<TargetType> target_class, Class... additional_interfaces) {
        Vector<Class> interfaces = null;
        HashSet<Class> added = null;
        if (additional_interfaces != null) {
            interfaces = new Vector<Class>();
            added = new HashSet<Class>();
            for (Class additional_interface : additional_interfaces) {
                interfaces.add(additional_interface);
                added.add(additional_interface);
            }
        }
        interfaces = addInterfaces(target_class, interfaces, added);
        Class proxy_class = Proxy.getProxyClass(target_class.getClassLoader(), interfaces.toArray(empty_array));
        try {
            return proxy_class.getConstructor(new Class[] { InvocationHandler.class });
        } catch (NoSuchMethodException nsme) {
            // should not happen
            throw new Error(nsme);
        }
    }
        
    static public<TargetType> TargetType createInstance(Constructor proxy_constructor, TargetType target, Interceptor... interceptors) {
        try {
            return (TargetType) proxy_constructor.newInstance(new Object[] { new Delegate<TargetType>(target, interceptors) });
        } catch (InstantiationException ie) {
            // should not happen
            throw new Error(ie);
        } catch (IllegalAccessException iae) {
            // should not happen
            throw new Error(iae);
        } catch (InvocationTargetException ite) {
            // should not happen
            throw new Error(ite);
        }
    }

    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        Interceptor interceptor = null;
        if (interceptors != null) {
            for (Interceptor interceptor_candidate : interceptors) {
                if (interceptor_candidate.matches(target, method, arguments)) {
                    interceptor = interceptor_candidate;
                }
            }
        }
        try {
            if (interceptor != null) {
                return interceptor.handleInvocation(proxy, this, target, method, arguments);
            } else {
                return method.invoke(target, arguments);
            }
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }
}
