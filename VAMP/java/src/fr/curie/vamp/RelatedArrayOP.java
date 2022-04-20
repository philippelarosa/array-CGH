
/*
 *
 * RelatedArrayOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class RelatedArrayOP extends GraphElementListOperation {
   
    static final String NAME = "Related CGH Array";
    boolean trans;

    public String[] getSupportedInputTypes() {
	return trans ?
	    new String[]{VAMPConstants.TRANSCRIPTOME_TYPE,
			 VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE,
			 VAMPConstants.TRANSCRIPTOME_MERGE_TYPE} :
	    new String[]{VAMPConstants.LOH_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    RelatedArrayOP(boolean trans) {
	super(NAME, SHOW_MENU);
	this.trans = trans;
    }

    GraphElement getRelatedArray(GraphElement graphElement) {
	return (GraphElement)graphElement.getPropertyValue(VAMPProperties.ArrayRefProp);
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    GraphElement dset = (GraphElement)graphElements.get(n);
	    if (getRelatedArray(dset) == null)
		return false;
	}

	return true;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	try {
	    int size = graphElements.size();
	    Vector rGraphElements = new Vector();
	    for (int n = 0; n < size; n++) {
		GraphElement dset = (GraphElement)graphElements.get(n);
		GraphElement ref = getRelatedArray(dset);
		rGraphElements.add(ref.clone());
		rGraphElements.add(dset);
	    }
	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    System.err.println(e);
	    return null;
	}
    }
}
