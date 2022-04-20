
/*
 *
 * SplitArrayOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class SplitArrayOP extends GraphElementListOperation {
   
    int type;
    static final String CGH_NAME = "Split CGH Arrays";
    static final String CHIP_CHIP_NAME = "Split ChIP-chip Arrays";
    static final String FRAGL_NAME = "Split FrAGL";

    public String[] getSupportedInputTypes() {
	if (type == CHIP_CHIP_TYPE)
	    return new String[]{VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE};
	if (type == CGH_TYPE)
	    return new String[]{VAMPConstants.CGH_ARRAY_MERGE_TYPE};
	if (type == FRAGL_TYPE)
	    return new String[]{VAMPConstants.CGH_ARRAY_MERGE_TYPE};
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    static String getName(int type) {
	if (type == CHIP_CHIP_TYPE)
	    return CHIP_CHIP_NAME;
	if (type == CGH_TYPE)
	    return CGH_NAME;
	if (type == FRAGL_TYPE)
	    return FRAGL_NAME;
	return "";
    }

    SplitArrayOP(int type) {
	super(getName(type), ON_ALL|ADD_SEPARATOR);
	this.type = type;
    }

    public boolean mayApply(GraphElementListOperation op) {
	if (op == null)
	    return true;
	if (op.equals(this))
	    return false;

	if (type == CHIP_CHIP_TYPE)
	    return !op.equals
		(GraphElementListOperation.get(MergeArrayOP.CHIP_CHIP_NAME));

	if (type == CGH_TYPE)
	    return !op.equals
		(GraphElementListOperation.get(MergeArrayOP.CGH_NAME));
	
	if (type == FRAGL_TYPE)
	    return !op.equals
		(GraphElementListOperation.get(MergeArrayOP.FRAGL_NAME));

	return false;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	try {
	    Vector rGraphElements = new Vector();
	    int size = graphElements.size();
	    for (int n = 0; n < size; n++) {
		GraphElement graphElement = (GraphElement)graphElements.get(n);
		Vector v = (Vector)graphElement.getPropertyValue(VAMPProperties.VectorArrayProp);
		boolean allGraphElements = true;
		for (int m = 0; m < v.size(); m++) {
		    if (!(v.get(m) instanceof GraphElement)) {
			InfoDialog.pop(view.getGlobalContext(), "Cannot apply the split array operation on such a view");
			return null;
			/*
			allGraphElements = false;
			break;
			*/
		    }
		}

		if (!allGraphElements) {
		    rGraphElements.addAll(v);
		    break;
		}

		v = GraphElement.clone(v);
		rGraphElements.addAll(v);
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
