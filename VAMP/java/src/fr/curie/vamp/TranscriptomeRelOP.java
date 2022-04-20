
/*
 *
 * TranscriptomeRelOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class TranscriptomeRelOP
    extends TranscriptomeCommonOP {
   
    static final int NO_ADD_REF = 0x1;
    int flags;

    static final String NO_ADD_REF_NAME = "Transcriptome Relative 2";
    static final String NAME = "Transcriptome Relative";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.TRANSCRIPTOME_TYPE,
			    VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE};
    }

public String getReturnedType() {
	return VAMPConstants.TRANSCRIPTOME_REL_TYPE;
    }

    static String getName(int flags) {
	if ((flags & NO_ADD_REF) != 0)
	    return NO_ADD_REF_NAME;
	return NAME;
    }

    TranscriptomeRelOP(int flags) {
	super(getName(flags), (flags == 0 ? SHOW_MENU : 0));
	this.flags = flags;
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {
	int size = graphElements.size();
	if (size < 2) return false;
	boolean foundRef = false;

	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    if (!VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_TYPE) &&
		!VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) &&
		!VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE))
		return false;

	    if (graphElement.getPropertyValue(VAMPProperties.ReferenceProp) != null) {
		if (foundRef) return false;
		foundRef = true;
	    }
	}
	return foundRef && VAMPUtils.isMonoChr(graphElements);
    }

    static final Property signalProp = VAMPProperties.SignalProp;
    static final Property rsignalProp = VAMPProperties.RSignalProp;

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
	try {
	    int size = graphElements.size();
	    DataSet ref = null;

	    HashMap maps[] = new HashMap[size];
	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = (GraphElement)graphElements.get(m);
		if (graphElement.getPropertyValue(VAMPProperties.ReferenceProp) != null) {
		    if (ref != null)
			return null;
		    ref = graphElement.asDataSet();
		}
		else
		    maps[m] = makeMap(graphElement);

	    }
		    
	    if (ref == null)
		return null;

	    Object scale = ref.getPropertyValue(VAMPProperties.SignalScaleProp);
	    boolean log_scale = scale.equals(VAMPConstants.SignalScale_L);

	    HashMap refmap = makeMap(ref);

	    DataElement refData[] = ref.getData();

	    Vector rDataSets = new Vector();
	    if ((flags & NO_ADD_REF) == 0)
		rDataSets.add(ref);
	    for (int n = 0; n < size; n++) {
		GraphElement graphElement = (GraphElement)graphElements.get(n);
		DataSet dataSet = graphElement.asDataSet();
		if (dataSet == null) return null;
		if (dataSet.getPropertyValue(VAMPProperties.ReferenceProp) != null)
		    continue;

		DataSet relDataSet = (DataSet)dataSet.getPropertyValue(VAMPProperties.ArrayRefProp);
		DataSet rDataSet = (DataSet)dataSet.clone();
		rDataSet.setPropertyValue(VAMPProperties.TransProp, dataSet.clone());
		rDataSet.setPropertyValue(VAMPProperties.TransRefProp, ref.clone());
		// NEW_YINFO:
		// Note: it is important to set autoY to false and
		// to setYInfo

		/*
		rDataSet.setAutoY(false);
		rDataSet.setYInfo(VAMPUtils.getTranscriptomeRelYCoef(relDataSet,
							       dataSet),
				  dataSet.getYOffset());
		*/
		rDataSet.setAutoY2(true);

		DataElement data[] = rDataSet.getData();

		for (int i = 0; i < data.length; i++) {
		    DataElement refd = getData(getID(data[i]), refmap);
		    double pos;
		    if (refd == null) {
			data[i].setPropertyValue(VAMPProperties.IsNAProp, "true");
			pos = 0;
		    }
		    else {
			double d_ref = signalProp.toDouble(refd);
			double d_data = signalProp.toDouble(data[i]);
			if (log_scale)
			    pos = d_data - d_ref;
			else
			    pos = d_data/d_ref;
		    }

		    data[i].setPropertyValue(rsignalProp,
					     Utils.toString(pos));
		    /*
		    System.out.println("PosY: " + pos + " -> " +
				       rDataSet.yTransform(pos));
		    */
		    data[i].setPosY(rDataSet, rDataSet.yTransform(pos));
		    data[i].removeProperty(signalProp);
		}

		rDataSet.setPropertyValue(VAMPProperties.SignalScaleProp,
					  log_scale ? VAMPConstants.SignalScale_L :
					  VAMPConstants.SignalScale_M);
		rDataSet.removeProperty(VAMPProperties.RatioScaleProp);
		rDataSet.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_TRSREL);
		rDataSet.setPropertyValue(VAMPProperties.ThresholdsNameProp, VAMPConstants.THR_TRSREL);
		VAMPUtils.setType(rDataSet, VAMPConstants.TRANSCRIPTOME_REL_TYPE);
		rDataSets.add(rDataSet);
	    }

	    return undoManage(panel, rDataSets);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
