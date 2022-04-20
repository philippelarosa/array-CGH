
/*
 *
 * UnaverageOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class UnaverageOP extends GraphElementListOperation {
   
    int type;

    static final String CGH_NAME = "CGH Unaverage";
    static final String CHIP_CHIP_NAME = "ChIP-chip Unaverage";
    static final String SNP_NAME = "SNP Unaverage";

public String[] getSupportedInputTypes() {
	if (type == CGH_TYPE)
	    return new String[]{VAMPConstants.CGH_AVERAGE_TYPE};
	if (type == CHIP_CHIP_TYPE)
	    return new String[]{VAMPConstants.CHIP_CHIP_AVERAGE_TYPE};
	if (type == SNP_TYPE)
	    return new String[]{VAMPConstants.SNP_AVERAGE_TYPE};
	return null;
    }

public String getReturnedType() {
	return null;
    }

    static String getName(int type) {
	if (type == CGH_TYPE)
	    return CGH_NAME;
	if (type == CHIP_CHIP_TYPE)
	    return CHIP_CHIP_NAME;
	if (type == SNP_TYPE)
	    return SNP_NAME;
	return null;
    }

    UnaverageOP(int type) {
	super(getName(type), SHOW_MENU);
	this.type = type;
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {
	return graphElements.size() == 1;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
	GraphElement graphElement = (GraphElement)graphElements.get(0);
	return undoManage(panel,
			  (Vector)graphElement.getPropertyValue(VAMPProperties.VectorArrayProp));
    }
}
