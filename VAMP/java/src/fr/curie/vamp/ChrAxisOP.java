
/*
 *
 * ChrAxisOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

public class ChrAxisOP extends GraphElementListOperation {
   
    public static final String NAME = "Chromosome Name";

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    ChrAxisOP() {
	super(NAME, ON_ALL);
    }

    public boolean mayApply(GraphElementListOperation op) {
	return true;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	if (view != null && MergeChrOP.areAllMerged(graphElements)) {
	    // 18/02/05: changed
	    if (!(panel.getDefaultAxisDisplayer() instanceof
		  YDendrogramAxisDisplayer) && 
		!(panel.getDefaultAxisDisplayer() instanceof
		  ChromosomeNameAxisDisplayer)) {
		ChromosomeNameAxisDisplayer axisDsp = 
		    new ChromosomeNameAxisDisplayer
		    (VAMPUtils.getAxisName((GraphElement)graphElements.get(0)), 1., 0.1, false);
		panel.setDefaultAxisDisplayer(axisDsp);
		panel.setGraphElementIDBuilder(Config.dataSetIDArrayBuilder);
	    }
	}
	return graphElements;
    }

    public boolean mayApplyOnReadOnlyPanel() {
	return true;
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean supportProfiles() {return true;}
}
