
/*
 *
 * GraphElementIDBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

abstract class GraphElementIDBuilder {

    String name;
    boolean menuable;
    static private Vector builders = new Vector();

    GraphElementIDBuilder(String name, boolean menuable) {
	this.name = name;
	this.menuable = menuable;
	builders.add(this);
    }

    boolean isMenuable() {return menuable;}

    String getName() {return name;}

    static Vector getIDBuilders() {return builders;}

    static GraphElementIDBuilder get(String name) {
	int size = builders.size();
	for (int n = 0; n < size; n++) {
	    GraphElementIDBuilder builder = (GraphElementIDBuilder)builders.get(n);
	    if (builder.getName().equals(name))
		return builder;
	}

	return null;
    }

    abstract String buildID(GraphElement graphElement);
}
