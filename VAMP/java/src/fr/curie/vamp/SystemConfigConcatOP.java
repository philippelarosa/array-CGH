
/*
 *
 * SystemConfigConcatOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class SystemConfigConcatOP extends SystemConfigBinOP {

    SystemConfigConcatOP() {
	super("concat");
    }

    String eval(GlobalContext globalContext, String data,
		String child_data[], PropertyElement elem) {
	String s = "";
	for (int n = 0; n < child_data.length; n++)
	    s += child_data[n];
	return s;
    }
}
