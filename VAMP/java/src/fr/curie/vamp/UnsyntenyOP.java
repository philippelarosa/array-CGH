
/*
 *
 * UnsyntenyOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

// 27/05/05
// No more necessary: operation merged with SyntenyOP

/*
import java.util.*;

class UnsyntenyOP extends GraphElementListOperation {
   
    static final String NAME = "CGH Unsynteny";

    String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
    }

    String getReturnedType() {
	return VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE;
    }

    UnsyntenyOP() {
	super(NAME, SHOW_MENU);
    }

    boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {

	int size = graphElements.size();

	for (int m = 0; m < size; m++) {
	    GraphElement dataSet =
		((GraphElement)graphElements.get(m)).asDataSet();

	    if (dataSet == null)
		return false;

	    if (dataSet.getPropertyValue(VAMPConstants.SyntenyReferenceProp) == null)
		return false;
	}

	return true;
    }

    Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	try {
	    //if (view == null) return null;

	    Vector rGraphElements = new Vector();
	    int size = graphElements.size();

	    for (int m = 0; m < size; m++) {
		DataSet dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
		if (dataSet == null) return null;
		DataSet rDataSet =
		    (DataSet)dataSet.getPropertyValue(VAMPConstants.SyntenyReferenceProp);
		if (rDataSet == null)
		    return null;

		rGraphElements.add((DataSet)rDataSet.clone());
	    }

	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println(e);
	    return null;
	}
    }
}
*/
