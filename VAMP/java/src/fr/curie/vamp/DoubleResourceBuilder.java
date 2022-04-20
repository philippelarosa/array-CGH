
/*
 *
 * DoubleResourceBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class DoubleResourceBuilder extends ResourceBuilder {

    DoubleResourceBuilder() {
	super("Double");
    }

    Class getVClass() {
	return Double.class;
    }

    Object fromString(String s) {
	return new Double(Utils.parseDouble(s));
    }

    String toString(Object value) {
	return value.toString();
    }
}

