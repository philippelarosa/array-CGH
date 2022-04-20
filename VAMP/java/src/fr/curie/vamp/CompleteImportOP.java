
/*
 *
 * CompleteImportOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.util.*;

public class CompleteImportOP extends GraphElementListOperation {

    public static final String NAME = "Complete Import";

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    CompleteImportOP() {
	super(NAME, SHOW_MENU);
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	int size = graphElements.size();

	for (int n = 0; n < size; n++) {
	    if (((GraphElement)graphElements.get(n)).isFullImported())
		return false;
	    }

	return size > 0;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	int size = graphElements.size();

	XMLArrayDataFactory fact = new XMLArrayDataFactory(view.getGlobalContext(), null);
	XMLTranscriptomeFactory trsFact = new XMLTranscriptomeFactory(view.getGlobalContext());

	Vector rGraphElements = new Vector();
	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    if (graphElement.asProfile() != null) {
		graphElement.asProfile().setFullImported(true);
		try {
		    new fr.curie.vamp.gui.optim.GraphicProfile(graphElement.asProfile());
		    rGraphElements.add(graphElement);
		}
		catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	    else {
		LinkedList list;
		if (VAMPUtils.isTranscriptome(graphElement)) {
		    list = trsFact.buildDataSets(graphElement.getURL(),
						 (DataSet)graphElement.getPropertyValue(VAMPProperties.ArrayRefProp), true, true);
		    System.out.println("URL : '" + graphElement.getURL());
		}
		else {
		    list = fact.getData(graphElement.getURL());
		}
		Vector importedV = Utils.listToVector(list);
		if (VAMPUtils.isMergeChr(graphElement)) {
		    GraphElementListOperation mergeChrOP = ChrSwitchOP.getMergeChrOP
			(importedV);
		    if (mergeChrOP != null) {
			rGraphElements.addAll
			    (mergeChrOP.apply(view, panel, importedV, null));
		    }
		    else {
			rGraphElements.addAll(importedV);
		    }
		}
		else
		    rGraphElements.addAll(importedV);
	    }
	}

	return rGraphElements;
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}
}
