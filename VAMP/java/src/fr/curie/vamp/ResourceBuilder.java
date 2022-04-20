
/*
 *
 * ResourceBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

abstract class ResourceBuilder {

    private String name;

    ResourceBuilder(String name) {
	this.name = name;
    }

    abstract Class getVClass();
    abstract Object fromString(String value);
    abstract String toString(Object value);
    String getName() {return name;}
}

