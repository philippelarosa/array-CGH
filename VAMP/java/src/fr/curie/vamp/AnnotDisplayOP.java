
/*
 *
 * AnnotDisplayOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;

class AnnotDisplayOP extends GraphElementListOperation {

    Property filter_prop;

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    AnnotDisplayOP(String name, Property filter_prop) {
	super(name, SHOW_MENU | ON_ALL_AUTO);
	this.filter_prop = filter_prop;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	AnnotDisplayDialog.pop(view.getGlobalContext(), getName(), view, panel,
			       filter_prop);
	return graphElements;
    }

    public boolean mayApplyOnReadOnlyPanel() {
	return true;
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean supportProfiles() {
	return filter_prop == null;
    }
}
