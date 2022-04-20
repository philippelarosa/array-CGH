
/*
 *
 * RelatedArraysOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.awt.*;

class RelatedArraysOP extends GraphElementListOperation {
   
    static final String NAME = "Related Arrays";
    boolean addSelf;

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    RelatedArraysOP(boolean addSelf) {
	super(NAME, SHOW_MENU);
	this.addSelf = addSelf;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	GraphElementListOperation op = panel.getAutoApplyDSLOP();
	if (op != null && op.getName().equals(MergeArrayOP.FRAGL_NAME)) {
	    return false;
	}

	int size = graphElements.size();
	if (size == 0)
	    return false;

	for (int n = 0; n < size; n++)
	    if (getRelatedArrays((GraphElement)graphElements.get(n)) == null)
		return false;

	return true;
    }

    Vector getRelatedArrays(GraphElement graphElement) {
	return (Vector)graphElement.getPropertyValue(VAMPProperties.ArraysRefProp);
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	try {	
	    int size = graphElements.size();
	    Vector rGraphElements = new Vector();
	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = (GraphElement)graphElements.get(m);
		if (addSelf)
		    rGraphElements.add(graphElement);
		addAll(rGraphElements, getRelatedArrays(graphElement));
	    }

	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static void addAll(Vector rGraphElements, Vector v) {
	int size = v.size();
	for (int n = 0; n < size; n++)
	    rGraphElements.add(((GraphElement)v.get(n)).clone());
    }
}
