
/*
 *
 * ChangeSignalOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class ChangeSignalOP extends GraphElementListOperation {
   
    static final String NAME_LTOM = "Change LogIntensity to Intensity";
    static final String NAME_MTOL = "Change Intensity to LogIntensity";
    boolean LToM;

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.TRANSCRIPTOME_TYPE,
			    VAMPConstants.TRANSCRIPTOME_MERGE_TYPE,
			    VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.TRANSCRIPTOME_REL_TYPE,
			    VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE};
    }

public String getReturnedType() {
	return null;
    }

    ChangeSignalOP(boolean LToM) {
	/*
	super(LToM ? NAME_LTOM : NAME_MTOL,
	      ON_ALL|SHOW_MENU|(LToM ? 0 : ADD_SEPARATOR));
	*/
	super(LToM ? NAME_LTOM : NAME_MTOL,
	      SHOW_MENU|(LToM ? 0 : ADD_SEPARATOR));
	this.LToM = LToM;
    }

    static boolean hasSignalScaleProp(PropertyElement elem) {
	return elem.getPropertyValue(VAMPProperties.SignalScaleProp) != null;
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {

	int size = graphElements.size();

	for (int m = 0; m < size; m++) {
	    DataSet ds = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (ds == null) return false;
	    String signalScale = (String)ds.getPropertyValue(VAMPProperties.SignalScaleProp);
	    if (signalScale != null) {
		if (!check(signalScale)) return false;
	    }
	    else {
		for (int n = 0; n < ds.getData().length; n++) {
		    signalScale = (String)ds.getData()[n].getPropertyValue(VAMPProperties.SignalScaleProp);
		    if (signalScale == null || !check(signalScale)) return false;
		}
	    }
	}

	return true;
    }

    private boolean check(String signalScale) {
	if (signalScale.equals(VAMPConstants.SignalScale_L) && !LToM) return false;
	if (signalScale.equals(VAMPConstants.SignalScale_M) && LToM) return false;
	return true;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
	try {
	    Vector rGraphElements = new Vector();
	    int size = graphElements.size();
	    for (int m = 0; m < size; m++) {
		DataSet ods = ((GraphElement)graphElements.get(m)).asDataSet();
		if (ods == null)
		    return null;
		DataSet nds = (DataSet)ods.clone();
		//DataSet relDataSet = ((GraphElement)ds.getPropertyValue(VAMPConstants.ArrayRefProp)).asDataSet();
		if (hasSignalScaleProp(nds)) {
		    if (LToM)
			nds.setPropertyValue(VAMPProperties.SignalScaleProp,
					      VAMPConstants.SignalScale_M, false);
		    else
			nds.setPropertyValue(VAMPProperties.SignalScaleProp,
					      VAMPConstants.SignalScale_L, false);
		}

		Property sProp;
		if (VAMPUtils.getType(nds).equals(VAMPConstants.TRANSCRIPTOME_REL_TYPE))
		    sProp = VAMPProperties.RSignalProp;
		else
		    sProp = VAMPProperties.SignalProp;

		nds.setYInfo(0, 0);

		rGraphElements.add(nds);
		DataElement odata[] = ods.getData();
		DataElement ndata[] = nds.getData();
		for (int n = 0; n < nds.getData().length; n++) {
		    DataElement od = odata[n];
		    DataElement nd = ndata[n];
		    nd.copyPos(nds, od, ods);

		    if (LToM) {
			nd.setPosY(nds, Utils.pow(nd.getPosY(nds)));

			if (hasSignalScaleProp(nd))
			    nd.setPropertyValue(VAMPProperties.SignalScaleProp,
					       VAMPConstants.SignalScale_M, false);
		    }
		    else {
 			if (nd.getPosY(nds) != 0)
			    nd.setPosY(nds, Utils.log(nd.getPosY(nds)));

			if (hasSignalScaleProp(nd))
			    nd.setPropertyValue(VAMPProperties.SignalScaleProp,
					       VAMPConstants.SignalScale_L, false);
		    }

		    nd.setPropertyValue(sProp, Utils.toString(nd.getPosY(nds)), false);
		}
	    }

	    return rGraphElements;
	}
	catch(Exception e) {
	    System.err.println(e);
	    return null;
	}
    }
}
