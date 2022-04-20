
/*
 *
 * SystemConfigMulOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class SystemConfigMulOP extends SystemConfigBinOP {

    SystemConfigMulOP() {
	super("mul");
    }

    String eval(GlobalContext globalContext, String data,
		String child_data[], PropertyElement elem) {
	if (!check(globalContext, data, child_data)) return "";
	double dl = Utils.parseDouble(child_data[0]);
	double dr = Utils.parseDouble(child_data[1]);
	return Utils.toString(dl*dr);
    }
}
