
/*
 *
 * SystemConfigTokenOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class SystemConfigTokenOP extends SystemConfigOP {

    SystemConfigTokenOP() {
	super("token");
    }

    String eval(GlobalContext globalContext, String data,
		String child_data[], PropertyElement elem) {
	if (child_data.length != 0) {
	    InfoDialog.pop(globalContext,
			   "XMLConfig: no data expected in token tag");
	}

	return BasicStringURLTemplate.eval(data, elem);
    }
}
