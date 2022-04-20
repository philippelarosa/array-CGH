
/*
 *
 * SystemConfigOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

abstract class SystemConfigOP {
    String name;
    static Hashtable op_ht = new Hashtable();

    SystemConfigOP(String name) {
	this.name = name;
	name = name.toUpperCase();
	if (get(name) != null) {
	    System.err.println("SystemConfigOP: operation " + name +
			       " already in dictionary");
	    return;
	}
	op_ht.put(name, this);
    }

    static void init() {
	new SystemConfigTokenOP();
	new SystemConfigPropertyOP();

	new SystemConfigConcatOP();

	new SystemConfigAddOP();
	new SystemConfigMulOP();
	new SystemConfigDivOP();
	new SystemConfigSubOP();
    }

    abstract String eval(GlobalContext globalContext, String data,
			 String child_data[], PropertyElement elem);

    static SystemConfigOP get(String name) {
	return (SystemConfigOP)op_ht.get(name.toUpperCase());
    }
}
