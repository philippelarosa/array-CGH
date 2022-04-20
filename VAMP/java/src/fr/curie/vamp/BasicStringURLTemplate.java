
/*
 *
 * BasicURLTemplate.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class BasicStringURLTemplate extends URLTemplate {

    static final String delim = "#";

    String template;

    BasicStringURLTemplate(GlobalContext globalContext, String template) {
	super(globalContext);
	this.template = template;
    }

    public String eval(PropertyElement elem) {
	return eval(template, elem);
    }

    public static String eval(String template, PropertyElement elem) {
	return elem.fromTemplate(template);
    }
}
