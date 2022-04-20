
/*
 *
 * SystemConfigPropertyOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class SystemConfigPropertyOP extends SystemConfigOP {

    SystemConfigPropertyOP() {
	super("property");
    }

    String eval(GlobalContext globalContext, String data,
		String child_data[], PropertyElement elem) {
	if (child_data.length != 0) {
	    InfoDialog.pop(globalContext,
			   "XMLConfig: no data expected in property tag");
	}

	//	Property prop = elem.getProperty(data);
	Property prop = Property.getProperty(data);
	String val = (String)elem.getPropertyValue(prop);
	if (val == null) {
	    InfoDialog.pop(globalContext,
			   "XMLConfig: invalid property: " + data);
	    return "<<property:" + data + ">>";
	}

	//return (String)elem.getPropertyValue(prop);
	return val;
    }
}
