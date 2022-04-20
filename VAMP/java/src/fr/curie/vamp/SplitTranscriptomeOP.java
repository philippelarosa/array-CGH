
/*
 *
 * SplitTranscriptomeOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class SplitTranscriptomeOP extends GraphElementListOperation {
   
    static final String NAME = "Split Transcriptomes";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.TRANSCRIPTOME_MERGE_TYPE};
    }

public String getReturnedType() {
	return null;
    }

    SplitTranscriptomeOP() {
	super(NAME, SHOW_MENU|ADD_SEPARATOR);
    }

public boolean mayApply(GraphElementListOperation op) {
	if (op == null) return true;
	return !op.equals(this) &&
	    !op.equals(GraphElementListOperation.get
		       (MergeTranscriptomeOP.NAME));
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
	try {
	    Vector rGraphElements = new Vector();
	    int size = graphElements.size();
	    for (int n = 0; n < size; n++) {
		GraphElement graphElement = (GraphElement)graphElements.get(n);
		rGraphElements.addAll(GraphElement.clone((Vector)graphElement.getPropertyValue(VAMPProperties.VectorArrayProp)));
	    }
	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
