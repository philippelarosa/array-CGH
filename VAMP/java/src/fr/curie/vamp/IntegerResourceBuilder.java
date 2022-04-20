
/*
 *
 * IntegerResourceBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class IntegerResourceBuilder extends ResourceBuilder {

    IntegerResourceBuilder() {
	super("Integer");
    }

    Class getVClass() {
	return Integer.class;
    }

    Object fromString(String s) {
	return new Integer(Utils.parseInt(s));
    }

    String toString(Object value) {
	return value.toString();
    }
}

