
/*
 *
 * MergeOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

// try to Factorize with MergeChr

abstract class MergeOP extends MergeSplitOP {
   
    MergeOP(String name, int type, int flags) {
	super(name, type, flags);
    }

    protected Vector applyPrologue(View view, GraphPanel panel,
				   Vector graphElements, boolean autoApply) {
	graphElements = (Vector)graphElements.clone();

	String splitArrayName, splitChrName;
	if (isChipChip()) {
	    splitArrayName = SplitArrayOP.CHIP_CHIP_NAME;
	    splitChrName = SplitChrOP.CHIP_CHIP_NAME;
	}
	else if (isTranscriptome()) {
	    splitArrayName = null;
	    splitChrName = SplitChrOP.TRANSCRIPTOME_NAME;
	}
	else if (isCGH()) {
	    splitArrayName = SplitArrayOP.CGH_NAME;
	    splitChrName = SplitChrOP.CGH_NAME;
	}
	else if (isSNP()) {
	    splitArrayName = null;
	    splitChrName = SplitChrOP.SNP_NAME;
	}
	else if (isLOH()) {
	    splitArrayName = null;
	    splitChrName = SplitChrOP.LOH_NAME;
	}
	else if (isGenomeAnnot()) {
	    splitArrayName = null;
	    splitChrName = SplitChrOP.GENOME_ANNOT_NAME;
	}
	else if (isFrAGL()) {
	    //splitArrayName = SplitArrayOP.FRAGL_NAME;
	    splitArrayName = null;
	    splitChrName = SplitChrOP.FRAGL_NAME;
	}
	else {
	    splitArrayName = null;
	    splitChrName = null;
	}

	GraphElementListOperation splitArrayOp =
	    splitArrayName == null ? null :
	    GraphElementListOperation.get(splitArrayName);

	GraphElementListOperation splitChrOp =
	    splitChrName == null ? null :
	    GraphElementListOperation.get(splitChrName);

	GraphElementListOperation splitTransOp =
	    GraphElementListOperation.get(SplitTranscriptomeOP.NAME);

	Object graphElements_arr[] = graphElements.toArray();
	for (int n = 0; n < graphElements_arr.length; n++) {
	    GraphElementListOperation splitOp;
	    GraphElement graphElement = (GraphElement)graphElements_arr[n];
	    String type = VAMPUtils.getType(graphElement);
	    if (isChipChip()) {
		if (type.equals(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE))
		    splitOp = splitArrayOp;
		else if (type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE))
		    splitOp = splitChrOp;
		else
		    continue;
	    }
	    else if (isCGH()) {
		if (type.equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE))
		    splitOp = splitArrayOp;
		else if (type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
		    splitOp = splitChrOp;
		else
		    continue;
	    }
	    else if (isTranscriptome()) {
		if (type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE))
		    splitOp = splitChrOp;
		else
		    continue;
	    }
	    else if (isSNP()) {
		if (type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE))
		    splitOp = splitChrOp;
		else
		    continue;
	    }
	    else if (isLOH()) {
		if (type.equals(VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE))
		    splitOp = splitChrOp;
		else
		    continue;
	    }
	    else if (isGenomeAnnot()) {
		if (type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE))
		    splitOp = splitChrOp;
		else
		    continue;
	    }
	    else if (isFrAGL()) {
		if (type.equals(VAMPConstants.FRAGL_ARRAY_MERGE_TYPE)) {
		    splitOp = splitArrayOp;
		}
		else if (type.equals(VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE)) {
		    splitOp = splitChrOp;
		}
		else {
		    continue;
		}
	    }
	    else if (type.equals(VAMPConstants.TRANSCRIPTOME_MERGE_TYPE))
		splitOp = splitTransOp;
	    else
		continue;

	    graphElements.remove(graphElement);
	    Vector v = new Vector();
	    v.add(graphElement);
	    v = splitOp.apply(view, panel, v, null, autoApply);
	    if (v == null) {
		return null;
	    }
	    graphElements.addAll(v);
	}

	return graphElements;
    }
}
