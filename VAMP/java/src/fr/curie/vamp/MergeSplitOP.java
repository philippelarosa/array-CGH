
/*
 *
 * MergeSplitOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

abstract class MergeSplitOP extends GraphElementListOperation {

    int type;

    boolean isCGH() {return type == CGH_TYPE;}
    boolean isChipChip() {return type == CHIP_CHIP_TYPE;}
    boolean isTranscriptome() {return type == TRANSCRIPTOME_TYPE;}
    boolean isSNP() {return type == SNP_TYPE;}
    boolean isLOH() {return type == LOH_TYPE;}
    boolean isGenomeAnnot() {return type == GENOME_ANNOT_TYPE;}
    boolean isFrAGL() {return type == FRAGL_TYPE;}
    boolean isDifferentialAnalysis() {return type == DIFFANA_TYPE;}
    boolean isGTCAAnalysis() {return type == GTCA_TYPE;}

    MergeSplitOP(String name, int type, int flags) {
	super(name, flags);
	this.type = type;
    }


    public boolean isMerged(GraphElement graphElem) {

	String type = VAMPUtils.getType(graphElem);

	if (isChipChip()) {
	    return type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE);
	}

	if (isCGH()) {
	    return type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE);
	}

	if (isTranscriptome()) {
	    return type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE);
	}

	if (isDifferentialAnalysis()) {
	    return type.equals(VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE);
	}

	if (isGTCAAnalysis()) {
	    return type.equals(VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE);
	}

	if (isSNP()) {
	    return type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE);
	}

	if (isLOH()) {
	    return type.equals(VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE);
	}

	if (isGenomeAnnot()) {
	    return type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE);
	}

	if (isFrAGL()) {
	    return type.equals(VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE);
	}

	return false;
    }

    public boolean noOneMerged(Vector graphElements) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    if (isMerged((GraphElement)graphElements.get(n))) {
		return false;
	    }
	}

	return true;
    }
}
