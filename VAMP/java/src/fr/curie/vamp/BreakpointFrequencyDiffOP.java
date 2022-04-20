
/*
 *
 * BreakpointFrequencyDiffOP.java
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

class BreakpointFrequencyDiffOP extends GraphElementListOperation {
   
    static final String NAME = "Breakpoint Frequency Diff";

    String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
			    VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.BREAKPOINT_FREQUENCY_TYPE,
			    VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE};
    }

    String getReturnedType() {
	return null;
    }

    BreakpointFrequencyDiffOP() {
	super(NAME, SHOW_MENU);
    }

    boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {

	int size = graphElements.size();
	if (size == 0)
	    return false;
	for (int n = 0; n < size; n++)
	    if (getBreakpointFrequencyDiff((GraphElement)graphElements.get(n)) == null)
		return false;

	return true;
    }

    Vector getBreakpointFrequencyDiff(GraphElement graphElement) {
	return (Vector)graphElement.getPropertyValue(VAMPConstants.ArraysRefProp);
    }

    Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	try {	
	    int size = graphElements.size();
	    Vector rGraphElements = new Vector();
	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = (GraphElement)graphElements.get(m);
		rGraphElements.add(graphElement);
		addAll(rGraphElements, getBreakpointFrequencyDiff(graphElement));
	    }

	    return rGraphElements;
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


