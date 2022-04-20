
/*
 *
 * RelatedTranscriptomeOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class RelatedTranscriptomeOP extends GraphElementListOperation {
   
    int flags;
    static final int RELATED_TRANSCRIPTOME_ABS = 0x1;
    static final int RELATED_TRANSCRIPTOME_REF = 0x2;
    static final int RELATED_TRANSCRIPTOME_REL = 0x4;

    static final int RELATED_TRANSCRIPTOME_ABS_OP = 
	//(RELATED_TRANSCRIPTOME_ABS | RELATED_TRANSCRIPTOME_REL);
	RELATED_TRANSCRIPTOME_ABS;
    static final int RELATED_TRANSCRIPTOME_REF_OP = 
	(RELATED_TRANSCRIPTOME_REF | RELATED_TRANSCRIPTOME_REL);
    static final int RELATED_TRANSCRIPTOME_INFO_OP =
	(RELATED_TRANSCRIPTOME_ABS | RELATED_TRANSCRIPTOME_REF);

    static final String RELATED_TRANSCRIPTOME_ABS_NAME = "Transcriptome Absolute";
    static final String RELATED_TRANSCRIPTOME_REF_NAME = "Transcriptome Reference";
    static final String RELATED_TRANSCRIPTOME_INFO_NAME = "Related Transcriptome Info";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.TRANSCRIPTOME_REL_TYPE};
    }

public String getReturnedType() {
	return null;
    }

    static String getName(int flags) {
	if (flags == RELATED_TRANSCRIPTOME_REF_OP)
	    return RELATED_TRANSCRIPTOME_REF_NAME;

	if (flags == RELATED_TRANSCRIPTOME_ABS_OP)
	    return RELATED_TRANSCRIPTOME_ABS_NAME;

	if (flags == RELATED_TRANSCRIPTOME_INFO_OP)
	    return RELATED_TRANSCRIPTOME_INFO_NAME;

	return "RelatedTranscriptome: unknown flag " + flags;
    }

    RelatedTranscriptomeOP(int flags) {
	super(getName(flags),
	      (flags == RELATED_TRANSCRIPTOME_INFO_OP ? 0 : SHOW_MENU));
	this.flags = flags;
    }

    GraphElement getRelatedTranscriptome(GraphElement graphElement) {
	return (GraphElement)graphElement.getPropertyValue(VAMPProperties.TransProp);
    }

    GraphElement getRelatedTranscriptomeRef(GraphElement graphElement) {
	return (GraphElement)graphElement.getPropertyValue(VAMPProperties.TransRefProp);
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
	try {
	    int size = graphElements.size();
	    Vector rGraphElements = new Vector();
	    for (int n = 0; n < size; n++) {
		GraphElement dset = (GraphElement)graphElements.get(n);
		if ((flags & RELATED_TRANSCRIPTOME_ABS) != 0)
		    rGraphElements.add(getRelatedTranscriptome(dset).clone());
		if ((flags & RELATED_TRANSCRIPTOME_REF) != 0)
		    rGraphElements.add(getRelatedTranscriptomeRef(dset).clone());
		if ((flags &  RELATED_TRANSCRIPTOME_REL) != 0)
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
