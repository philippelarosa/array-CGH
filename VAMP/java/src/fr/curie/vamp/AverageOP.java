
/*
 *
 * AverageOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class AverageOP extends GraphElementListOperation {
   
    int type;

    static final String CGH_NAME = "CGH Average";
    static final String CHIP_CHIP_NAME = "ChIp-chip Average";
    static final String SNP_NAME = "SNP Average";

    public String[] getSupportedInputTypes() {
	if (type == CGH_TYPE)
	    return new String[]{VAMPConstants.CGH_AVERAGE_TYPE,
				VAMPConstants.CGH_ARRAY_TYPE,
				VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
				VAMPConstants.CGH_ARRAY_MERGE_TYPE};
	if (type == CHIP_CHIP_TYPE)
	    return new String[]{VAMPConstants.CHIP_CHIP_AVERAGE_TYPE,
				VAMPConstants.CHIP_CHIP_TYPE,
				VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE,
				VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE};
	if (type == SNP_TYPE)
	    return new String[]{VAMPConstants.SNP_AVERAGE_TYPE,
				VAMPConstants.SNP_TYPE,
				VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE};

	return null;
    }

    public String getReturnedType() {
	if (type == CHIP_CHIP_TYPE)
	    return VAMPConstants.CHIP_CHIP_AVERAGE_TYPE;
	if (type == CGH_TYPE)
	    return VAMPConstants.CGH_AVERAGE_TYPE;
	if (type == SNP_TYPE)
	    return VAMPConstants.SNP_AVERAGE_TYPE;

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

    AverageOP(int type) {
	super(getName(type), SHOW_MENU);
	this.type = type;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	if (graphElements.size() < 2)
	    return false;;

	int size = graphElements.size();
	for (int m = 0; m < size; m++) {
	    DataSet dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (dataSet == null)
		return false;
	}
	/*
	  int size = graphElements.size();
	  DataSet dataSet = ((DataSet)graphElements.get(0)).asDataSet();
	  if (dataSet == null)
	  return false;
	  int len = dataSet.getData().length;

	  for (int m = 1; m < size; m++) {
	  dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
	  if (dataSet == null) return false;
	  int l = dataSet.getData().length;
	  if (l != len) return false;
	  }
	*/
	return true;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	try {
	    // 9/02/06
	    graphElements = NormalizeOP.normalize(view.getGlobalContext(), graphElements);

	    Vector rGraphElements = new Vector();
	    DataSet ref = ((GraphElement)graphElements.get(0)).asDataSet();
	    if (ref == null) return null;
	    DataElement data[] = new DataElement[ref.getData().length];
	    int size = graphElements.size();
	    String arr_cnt = (new Integer(size)).toString();
	    String array_name = "Array Average (" + arr_cnt + ")";

	    Property ratioProp;
	    if (type == SNP_TYPE)
		ratioProp = VAMPProperties.CopyNBProp;
	    else
		ratioProp = VAMPProperties.RatioProp;

	    DataSet dataSet = new DataSet(ref.isFullImported());

	    for (int n = 0; n < data.length; n++) {
		data[n] = (DataElement)ref.getData()[n].clone();
		data[n].copyPos(dataSet, ref.getData()[n], ref);
		TreeMap propMap = data[n].getProperties();

		double y = 0.;
		int cnt = 0;

		//System.out.println("PROPMAP BEFORE: " + propMap.size());
		/*
		  boolean isNA = true, isOut = true, isBkp = true;
		  Object gnl = null;
		*/
		boolean isNA = true;

		for (int m = 0; m < size; m++) {
		    DataSet ds = ((GraphElement)graphElements.get(m)).asDataSet();
		    if (ds == null)
			return null;
		    DataElement d = ds.getData()[n];
		    // if (d.getPropertyValue(VAMPConstants.IsNAProp) == null) {
		    if (!VAMPUtils.isNA(d)) {
			y += d.getVY(ds);
			isNA = false;
			cnt++;
		    }

		    /*
		      if (!VAMPUtils.isOutlier(d))
		      isOut = false;

		      if (!VAMPUtils.isBreakpoint(d))
		      isBkp = false;

		      Object gnl_n = d.getPropertyValue(VAMPConstants.GNLProp);
		      if (m == 0)
		      gnl = gnl_n;
		      else if (gnl != null && gnl_n != null) {
		      if (!gnl.equals(gnl_n))
		      gnl = null;
		      }
		    */

		    Vector toRemove = new Vector();
		    Iterator it = propMap.entrySet().iterator();
		    while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			Property prop = (Property)entry.getKey();
			Object value = d.getPropertyValue(prop);
			if (value == null || !value.equals(entry.getValue()))
			    toRemove.add(prop);
		    }

		    for (int k = 0; k < toRemove.size(); k++)
			propMap.remove(toRemove.get(k));
		}

		//System.out.println("PROPMAP AFTER: " + propMap.size());
		updateProperties(data[n], propMap);
		if (isNA) {
		    data[n].setPropertyValue(VAMPProperties.IsNAProp, "True");
		    data[n].setPosY(dataSet, 0);
		    // disconnected the 3/02/05
		    //data[n].setPropertyValue(VAMPConstants.RatioProp, VAMPConstants.NA);
		}
		else {
		    // added 31/01/05
		    data[n].setPropertyValue(VAMPProperties.IsNAProp, "False");
		    data[n].setPosY(dataSet, y/cnt);
		    // TBD: CopyNBProp in case of SNP
		    data[n].setPropertyValue(ratioProp,
					     (new Double(data[n].getVY(dataSet))).toString());
		}

		data[n].setPropertyValue(VAMPProperties.ArrayProp, array_name);

		//updateProperties(data[n], isNA, isOut, isBkp, gnl);
	    }

	    dataSet.setData(data);
	    //graphElement.setProperties((TreeMap)ref.getProperties().clone());
	    dataSet.cloneProperties(ref);

	    if (type == CHIP_CHIP_TYPE)
		VAMPUtils.setType(dataSet, VAMPConstants.CHIP_CHIP_AVERAGE_TYPE);
	    else if (type == SNP_TYPE)
		VAMPUtils.setType(dataSet, VAMPConstants.SNP_AVERAGE_TYPE);
	    else if (type == CGH_TYPE)
		VAMPUtils.setType(dataSet, VAMPConstants.CGH_AVERAGE_TYPE);
	    else
		VAMPUtils.setType(dataSet, null);

	    dataSet.setPropertyValue(VAMPProperties.ArrayCountProp, arr_cnt);
	    dataSet.setPropertyValue(VAMPProperties.NameProp, array_name);
	    dataSet.setPropertyValue(VAMPProperties.VectorArrayProp, graphElements);

	    rGraphElements.add(dataSet);
	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private void updateProperties(DataElement data,
				  boolean isNA, boolean isOut, boolean isBkp,
				  Object gnl) {
	if (!isNA) {
	    //if (data.getPropertyValue(VAMPConstants.IsNAProp) != null)
	    // changed 31/01/05
	    data.setPropertyValue(VAMPProperties.IsNAProp, "False");
	    //data.removeProperty(VAMPConstants.IsNAProp);
	}

	if (!isOut) {
	    if (data.getPropertyValue(VAMPProperties.OutProp) != null)
		data.setPropertyValue(VAMPProperties.OutProp, "0");
	}

	if (!isBkp) {
	    if (data.getPropertyValue(VAMPProperties.BreakpointProp) != null)
		data.setPropertyValue(VAMPProperties.BreakpointProp, "0");
	}

	data.removeProperty(VAMPProperties.SmoothingProp);

	if (gnl == null)
	    data.removeProperty(VAMPProperties.GNLProp);
	else
	    data.setPropertyValue(VAMPProperties.GNLProp, gnl);
    }

    private void updateProperties(DataElement data, TreeMap propMap) {
	data.removeAllProperties();
	Iterator it = propMap.entrySet().iterator();
	boolean hasGNL = false;
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    data.setPropertyValue(prop, entry.getValue());
	    if (!hasGNL && prop.equals(VAMPProperties.GNLProp))
		hasGNL = true;
	}

	/*
	  if (!hasGNL)
	  data.setPropertyValue(VAMPConstants.GNLProp, VAMPConstants.CLONE_UNKNOWN_STR);
	*/

	data.removeProperty(VAMPProperties.GNLProp);
	data.removeProperty(VAMPProperties.BreakpointProp);
	data.removeProperty(VAMPProperties.OutProp);
	data.removeProperty(VAMPProperties.SmoothingProp);
    }
}
