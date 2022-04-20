
/*
 *
 * SyntaxTreeURLTemplate.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class SyntaxTreeURLTemplate extends URLTemplate {

    XMLElement xml_elem;

    SyntaxTreeURLTemplate(GlobalContext globalContext, XMLElement xml_elem) {
	super(globalContext);
	this.xml_elem = xml_elem;
    }

    private String eval(XMLElement xml_elem, PropertyElement elem) {
	LinkedList child_list = xml_elem.getChildList();
	String name = xml_elem.getName();
	if (name.equals("MenuItem")) {
	    String s = "";
	    for (int n = 0; n < child_list.size(); n++)
		s += eval((XMLElement)child_list.get(n), elem);
	    return s;
	}

	SystemConfigOP op = SystemConfigOP.get(name);
	if (op == null) {
	    InfoDialog.pop(globalContext, "XMLConfig: unknown directive: " +
			   name);
	    return "<<directive:" + name + ">>";
	}

	String child_data[] = new String[child_list.size()];
	for (int n = 0; n < child_list.size(); n++)
	    child_data[n] = eval((XMLElement)child_list.get(n), elem);
	return op.eval(globalContext, xml_elem.getData(), child_data, elem);
    }

    public String eval(PropertyElement elem) {
	String s = eval(xml_elem, elem);
	//System.out.println("SyntaxTreeTemplate -> " + s);
	return s;
    }
}
