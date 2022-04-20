
/*
 *
 * SystemConfigBinOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

abstract class SystemConfigBinOP extends SystemConfigOP {

    SystemConfigBinOP(String name) {
	super(name);
    }

    protected boolean check(GlobalContext globalContext, String data,
			    String child_data[]) {
	if (child_data.length != 2) {
	    InfoDialog.pop(globalContext, "XMLConfig: " + name +
			   " operation: 2 arguments expected, got " +
			   child_data.length);
	    return false;
	}
	try {
	    Utils.parseDouble(child_data[0]);
	    Utils.parseDouble(child_data[1]);
	}
	catch(NumberFormatException e) {
	    InfoDialog.pop(globalContext, "XMLConfig: " + name +
			   " operation: expected numeric arguments");
	    return false;
	}

	return true;
    }
}
