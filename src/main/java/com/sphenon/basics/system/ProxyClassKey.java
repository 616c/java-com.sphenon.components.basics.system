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

public class ProxyClassKey {

    protected Class target_class;
    protected Class[] additional_interfaces;

    public ProxyClassKey(Class target_class, Class... additional_interfaces) {
        this.target_class = target_class;
        this.additional_interfaces = additional_interfaces;
    }

    public boolean equals(Object other_object) {
        ProxyClassKey other = (ProxyClassKey) other_object;
        if (other == null) { return false; }
        if (other == this) { return true; }
        if (this.target_class != other.target_class) { return false; }
        if ((this.additional_interfaces == null) != (other.additional_interfaces == null)) { return false; }
        if (this.additional_interfaces != null) {
            if (this.additional_interfaces.length != other.additional_interfaces.length) { return false; }
            for (int i=0; i<this.additional_interfaces.length; i++) {
                if (this.additional_interfaces[i] != other.additional_interfaces[i]) { return false; }
            }
        }
        return true;
    }

    public int hashCode() {
        int hc = this.target_class.hashCode();
        if (this.additional_interfaces != null) {
            for (Class additional_interface : this.additional_interfaces) {
                hc ^= additional_interface.hashCode();
            }
        }
        return hc;
    }
}
