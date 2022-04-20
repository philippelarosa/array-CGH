
/*
 *
 * BooleanResourceBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class BooleanResourceBuilder extends ResourceBuilder {

    BooleanResourceBuilder() {
	super("Boolean");
    }

    Class getVClass() {
	return Boolean.class;
    }

    Object fromString(String s) {
	if (s.equalsIgnoreCase("true")) return new Boolean(true);
	if (s.equalsIgnoreCase("false")) return new Boolean(false);
	return null;
    }

    String toString(Object value) {
	return value.toString();
    }
}

