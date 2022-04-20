
/*
 *
 * SystemConfigAddOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class SystemConfigAddOP extends SystemConfigBinOP {

    SystemConfigAddOP() {
	super("add");
    }

    String eval(GlobalContext globalContext, String data,
		String child_data[], PropertyElement elem) {
	if (!check(globalContext, data, child_data)) return "";
	long dl =  Long.parseLong(child_data[0]);
	long dr =  Long.parseLong(child_data[1]);
	return (new Long(dl+dr)).toString();
    }
}
