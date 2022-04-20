
/*
 *
 * ChangeRatioOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class ChangeRatioOP extends GraphElementListOperation {
   
    static final String CGH_NAME_LTOM = "Change CGH LogRatio to Ratio";
    static final String CGH_NAME_MTOL = "Change CGH Ratio to LogRatio";

    static final String CHIP_CHIP_NAME_LTOM = "Change ChIP-chip LogRatio to Ratio";
    static final String CHIP_CHIP_NAME_MTOL = "Change ChIP-chip Ratio to LogRatio";

    static final String SNP_NAME_LTOM = "Change Affy-SNP LogCopyNb to CopyNb";
    static final String SNP_NAME_MTOL = "Change Affy-SNP CopyNb to LogCopyNb";

    boolean LToM;
    int type;

    public String[] getSupportedInputTypes() {
	if (type == CGH_TYPE)
	    return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
				VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
				VAMPConstants.CGH_AVERAGE_TYPE};

	if (type == CHIP_CHIP_TYPE)
	    return new String[]{VAMPConstants.CHIP_CHIP_TYPE,
				VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE,
				VAMPConstants.CHIP_CHIP_AVERAGE_TYPE};

	if (type == SNP_TYPE)
	    return new String[]{VAMPConstants.SNP_TYPE,
				VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE,
				VAMPConstants.SNP_AVERAGE_TYPE};
	return new String[]{};
    }

    public String getReturnedType() {
	return null;
    }

    public String getMenuName() {
	if (LToM)
	    return "Change LogRatio to Ratio";

	return "Change Ratio to LogRatio";
    }

    static String getName(int type, boolean LToM) {
	if (LToM) {
	    if (type == CGH_TYPE)
		return CGH_NAME_LTOM;
	    if (type == CHIP_CHIP_TYPE)
		return CHIP_CHIP_NAME_LTOM;
	    if (type == SNP_TYPE)
		return SNP_NAME_LTOM;
	    return null;
	}

	if (type == CGH_TYPE)
	    return CGH_NAME_MTOL;
	if (type == CHIP_CHIP_TYPE)
	    return CHIP_CHIP_NAME_MTOL;
	if (type == SNP_TYPE)
	    return SNP_NAME_MTOL;

	return null;
    }

    ChangeRatioOP(int type, boolean LToM) {
	super(getName(type, LToM), SHOW_MENU|(LToM ? 0 : ADD_SEPARATOR));
	this.LToM = LToM;
	this.type = type;
    }

    static boolean hasRatioScaleProp(PropertyElement elem) {
	return elem.getPropertyValue(VAMPProperties.RatioScaleProp) != null;
    }

    static double getSmoothingProp(PropertyElement elem) {
	String smt = (String)elem.getPropertyValue(VAMPProperties.SmoothingProp);
	if (smt == null || smt.equalsIgnoreCase(VAMPProperties.NA))
	    return Double.MAX_VALUE;
	return Utils.parseDouble(smt);
    }

    static void setSmoothingProp(PropertyElement elem, double smt_val) {
	elem.setPropertyValue(VAMPProperties.SmoothingProp,
			      Utils.toString(smt_val), false);
    }

    public boolean mayApplyP(View view,  GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	int size = graphElements.size();
	
	for (int m = 0; m < size; m++) {
	    DataSet ds = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (ds == null) return false;
	    String sRatio = (String)ds.getPropertyValue(VAMPProperties.RatioScaleProp);
	    if (sRatio != null) {
		if (!check(sRatio))
		    return false;
	    }
	    else {
		for (int n = 0; n < ds.getData().length; n++) {
		    sRatio = (String)ds.getData()[n].getPropertyValue(VAMPProperties.RatioScaleProp);
		    if (sRatio == null || !check(sRatio))
			return false;
		}
	    }
	}

	return true;
    }

    private boolean check(String sRatio) {
	if (sRatio.equals(VAMPConstants.RatioScale_L) && !LToM)
	    return false;
	if (sRatio.equals(VAMPConstants.RatioScale_M) && LToM)
	    return false;
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
		rGraphElements.add(nds);

		DataElement odata[] = ods.getData();
		DataElement ndata[] = nds.getData();

		for (int n = 0; n < ndata.length; n++) {
		    DataElement od = odata[n];
		    DataElement nd = ndata[n];
		    nd.copyPos(nds, od, ods);
		    boolean isNA = VAMPUtils.isNA(nd);
		    double smt = getSmoothingProp(nd);

		    if (LToM) {
			if (!isNA) {
			    nd.setPosY(nds, Utils.pow(nd.getVY(nds)));
			    if (smt != Double.MAX_VALUE)
				setSmoothingProp(nd, Utils.pow(smt));
			}

			if (hasRatioScaleProp(nd))
			    nd.setPropertyValue(VAMPProperties.RatioScaleProp, 
						VAMPConstants.RatioScale_M, false);
		    }
		    else {
			if (!isNA) {
			    if (nd.getVY(nds) != 0)
				nd.setPosY(nds, Utils.log(nd.getVY(nds)));
			    if (smt != Double.MAX_VALUE)
				setSmoothingProp(nd, Utils.log(smt));
			}

			if (hasRatioScaleProp(nd))
			    nd.setPropertyValue(VAMPProperties.RatioScaleProp,
						VAMPConstants.RatioScale_L, false);
		    }

		    if (!isNA) {
			Property prop;
			if (nd.getPropertyValue(VAMPProperties.RatioProp) != null)
			    prop = VAMPProperties.RatioProp;
			else if (nd.getPropertyValue(VAMPProperties.CopyNBProp) != null)
			    prop = VAMPProperties.CopyNBProp;
			else
			    return null;

			nd.setPropertyValue
			    (prop, Utils.toString(nd.getVY(nds)), false);
		    }
		}

		if (hasRatioScaleProp(nds)) {
		    if (LToM)
			nds.setPropertyValue(VAMPProperties.RatioScaleProp, 
					     VAMPConstants.RatioScale_M, false);
		    else
			nds.setPropertyValue(VAMPProperties.RatioScaleProp,
					     VAMPConstants.RatioScale_L, false);
		}
	    }

	    return rGraphElements;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
