
/*
 *
 * ResourceItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class ResourceItem {
    String name;
    ResourceBuilder builder;
    Object defval;
    boolean is_param;

    ResourceItem(String name, boolean is_param,
		 ResourceBuilder builder, Object defval) {
	this.name = name;
	this.builder = builder;
	this.is_param = is_param;
	if (defval != null && !defval.getClass().equals(builder.getVClass()))
	    System.err.println("ERROR class for " + name);
	this.defval = defval;
    }

    boolean isParameter() {return is_param;}

    Object getDefaultValue() {return defval;}
}

