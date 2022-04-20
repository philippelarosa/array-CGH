
/*
 *
 * TranscriptomeUnaverageOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class TranscriptomeUnaverageOP extends GraphElementListOperation {
   
    static final String NAME = "Transcriptome Unaverage";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE};
    }

public String getReturnedType() {
	return VAMPConstants.TRANSCRIPTOME_TYPE;
    }

    TranscriptomeUnaverageOP() {
	super(NAME, SHOW_MENU);
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
