
/*
 *
 * SortOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;

class SortOP extends GraphElementListOperation {

    Property filter_prop;

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    SortOP(String name, Property filter_prop) {
	super(name, SHOW_MENU | ON_ALL_AUTO);
	this.filter_prop = filter_prop;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	SortDialog.pop(view.getGlobalContext(), getName(), view, panel,
		       filter_prop);
	return graphElements;
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean supportProfiles() {return true;}
}
