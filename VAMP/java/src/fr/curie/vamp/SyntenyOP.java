
/*
 *
 * SyntenyOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class SyntenyOP extends GraphElementListOperation {
   
    static final String SWITCH_NAME = "CGH Synteny Switch";
    static final String REGION_NAME = "CGH Region Synteny";
    static final String NAME = "CGH Synteny";

    static final String REGION_LIST_STR_PARAM = "REGION_LIST";
    private static final String REGION_LIST_PARAM = "REGION_LIST_INT";

    public static final int SWITCH = 0x1;
    public static final int REGION = 0x2;
    private int flags;

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
    }

public String getReturnedType() {
	return VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE;
    }

    static String getName(int flags) {
	if (flags == REGION)
	    return REGION_NAME;
	if (flags == SWITCH)
	    return SWITCH_NAME;
	return NAME;
    }

    SyntenyOP(int flags) {
	super(getName(flags), SHOW_MENU);
	this.flags = flags;
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {

	int size = graphElements.size();

	for (int m = 0; m < size; m++) {
	    DataSet dataSet =
		((GraphElement)graphElements.get(m)).asDataSet();

	    if (dataSet == null)
		return false;

	    // added 27/05/05: unsynteny
	    if (isSwitch() &&
		dataSet.getPropertyValue(VAMPProperties.SyntenyReferenceProp) != null)
		continue;

	    String OS = VAMPUtils.getOS(dataSet);

	    String syntenyOS = (String)
		dataSet.getPropertyValue(VAMPProperties.SyntenyOrganismProp);

	    if (syntenyOS == null || syntenyOS.equals(OS))
		return false;
	    
	    DataElement data[] = dataSet.getData();
	    boolean found = false;
	    for (int n = 0; n < data.length; n++) {
		if (!isEligible(dataSet, data[n], panel, null, dataSet))
		    continue;
		if (data[n].getPropertyValue(VAMPProperties.SyntenyProp) != null) {
		    found = true;
		    break;
		}
	    }

	    if (!found)
		return false;
	}

	return true;
    }

    static DataElement[] makeData(DataElement data[]) {
	TreeSet treeSet = new TreeSet();
	for (int n = 0; n < data.length; n++)
	    treeSet.add(new DataElementChrComparator(data[n], true));

	Object o[] = treeSet.toArray();
	data = new DataElement[o.length];
	for (int n = 0; n < o.length; n++)
	    data[n] = ((DataElementChrComparator)o[n]).data;

	return data;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	try {
	    initParams(params);
	    GlobalContext globalContext = getGlobalContext(view, params);

	    GraphElementListOperation mergeChrOP =
		GraphElementListOperation.get(MergeChrOP.CGH_NAME);
	    GraphElementListOperation splitChrOP =
		GraphElementListOperation.get(SplitChrOP.CGH_NAME);

	    Vector rGraphElements = new Vector();
	    int size = graphElements.size();

	    AxisDisplayer defaultAxisDisplayer =
		(panel != null ? panel.getDefaultAxisDisplayer() : null);
	    AxisDisplayer chrAxisDisplayer =
		(panel != null ? 
		 new ChromosomeNameAxisDisplayer(VAMPUtils.getAxisName((GraphElement)graphElements.get(0)), 1., 0.1, false,
						 panel.getGraphElementIDBuilder()) :
		 null);

	    for (int m = 0; m < size; m++) {
		DataSet dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
		if (dataSet == null)
		    return null;

		// added 27/05/05
		if (isSwitch()) {
		    DataSet xDataSet =
			(DataSet)dataSet.getPropertyValue(VAMPProperties.SyntenyReferenceProp);
		    if (xDataSet != null) {
			rGraphElements.add(xDataSet.clone());
			continue;
		    }
		}
		// ....

		AxisDisplayer axisDisplayer = dataSet.getAxisDisplayer();
		if (axisDisplayer == null)
		    axisDisplayer = defaultAxisDisplayer;

		boolean isCGHArray;

		if (VAMPUtils.getType(dataSet).equals(VAMPConstants.CGH_ARRAY_TYPE)) {
		    isCGHArray = true;
		    dataSet = (DataSet)dataSet.clone();
		    dataSet.setAxisDisplayer(axisDisplayer);
		}
		else
		    isCGHArray = false;

		String OS = VAMPUtils.getOS(dataSet);
		String syntenyOS = (String)dataSet.getPropertyValue
		    (VAMPProperties.SyntenyOrganismProp);
		if (syntenyOS == null)
		    return null;

		Cytoband cytoband = MiniMapDataFactory.getCytoband
		    (globalContext, syntenyOS);

		if (cytoband == null) {
		    InfoDialog.pop(globalContext,
				   "Cannot find cytoband for organism " +
				   syntenyOS);
		    return null;
		}

		DataElement data[] = dataSet.getData();
		Vector rData_v = new Vector();
		DataSet rDataSet = new DataSet();

		for (int n = 0; n < data.length; n++) {
		    DataElement d = data[n];
		    d.removeProperty(VAMPProperties.LinkedDataProp);
		    if (!isEligible(dataSet, d, panel, params, dataSet))
			continue;

		    String synteny = (String)
			d.getPropertyValue(VAMPProperties.SyntenyProp);
		    if (synteny == null)
			continue;

		    String s[] = synteny.trim().split("\\|");
		    if (s.length != 2) {
			invalidSynteny(globalContext, d, synteny);
			return null;
		    }

		    String syntenyInfo_b[] =
			parseSynteny(globalContext, s[0], d,
				     synteny);

		    if (syntenyInfo_b == null)
			return null;

		    String syntenyInfo_e[] =
			parseSynteny(globalContext, s[1], d,
				     synteny);

		    if (syntenyInfo_e == null)
			return null;

		    DataElement nd = makeDataElement(rDataSet, d, dataSet,
						     OS, syntenyOS,
						     syntenyInfo_b, 
						     rData_v.size());
		    rData_v.add(nd);

		    if (syntenyInfo_b[CHR_IND].equals
			(syntenyInfo_e[CHR_IND])) {
			long pos_b = Long.parseLong(syntenyInfo_b[X_IND]);
			long pos_e = Long.parseLong(syntenyInfo_e[X_IND]);
			if (pos_e < pos_b) {
			    long pos = pos_e;
			    pos_e = pos_b;
			    pos_b = pos;
			}

			nd.setPropertyValue(VAMPProperties.SizeProp,
					    Utils.toString(pos_e - pos_b));
		    }
		    else {
			nd.setPropertyValue(VAMPProperties.SizeProp, "NA");
			if (!syntenyInfo_e[CHR_IND].equals("NA")) {
			    nd = makeDataElement(rDataSet, d, dataSet,
						 OS, syntenyOS,
						 syntenyInfo_e,
						 rData_v.size());
			    nd.setPropertyValue(VAMPProperties.SizeProp, "NA");
			    rData_v.add(nd);
			}
		    }
		}

		DataElement rData[] = new DataElement[rData_v.size()];
		for (int n = 0; n < rData.length; n++)
		    rData[n] = (DataElement)rData_v.get(n);

		//DataSet rDataSet = new DataSet(makeData(rData));
		rDataSet.setData(makeData(rData));
		rDataSet.cloneProperties(dataSet);
		rDataSet.setPropertyValue(VAMPProperties.OrganismProp, syntenyOS);
		rDataSet.removeProperty(ChromosomeNameAxisDisplayer.ChrCache2Prop);

		/*
		if (isLinked())
		    rDataSet.setPropertyValue(VAMPConstants.LinkedDataSetProp, dataSet);
		*/

		if (isCGHArray)
		    VAMPUtils.setType(rDataSet, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE);

		Vector v = new Vector();
		v.add(rDataSet);

		Vector rv = splitChrOP.apply(view, panel, v, null);
		String region_str = isRegionning() ? getRegionString(panel, params) : null;
		
		if (rv != null) {
		    rv = mergeChrOP.apply(view, panel, rv, null);
		    if (rv != null && rv.size() == 1) {
			rDataSet = (DataSet)rv.get(0);
			rDataSet.setPropertyValue(VAMPProperties.SyntenyReferenceProp,
						  dataSet);
			if (region_str != null)
			    rDataSet.setPropertyValue(VAMPProperties.SyntenyRegionProp,
						      region_str);
			
			rDataSet.setPropertyValue(VAMPProperties.SyntenyOPProp,
						  getSName());

			rDataSet.setPropertyValue
			    (VAMPProperties.SyntenyOrigProp, OS);
			if (isLinked())
			    rGraphElements.add(dataSet);
			rGraphElements.add(rDataSet);
			rDataSet.setAxisDisplayer(chrAxisDisplayer);
			if (isLinked()) {
			    rDataSet.setPropertyValue(VAMPProperties.LinkedDataSetProp, dataSet);
			    dataSet.setPropertyValue(VAMPProperties.LinkedDataSetProp, rDataSet);
			    // 3/01/05: disconnected
			    //setLinks(dataSet, rDataSet);
			}
		    }
		}
	    }

	    if (panel != null)
		panel.setDefaultAxisDisplayer(defaultAxisDisplayer);
	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println(e);
	    return null;
	}
    }

    static String [] invalidSynteny(GlobalContext globalContext, DataElement d,
			     String synteny) {
	String msg = "";
	if (d != null)
	    msg = d.getID() + ": ";

	InfoDialog.pop(globalContext, msg + "invalid synteny format: " +
		       synteny);
	return null;
    }


    static final int OS_IND = 0;
    static final int CHR_IND = 1;
    static final int X_IND = 2;

    static final String OS_PREFIX = "OS=";
    static final int OS_PREFIX_LEN = OS_PREFIX.length();

    static final String CHR_PREFIX = "Chr=";
    static final int CHR_PREFIX_LEN = CHR_PREFIX.length();

    static final String X_PREFIX = "X=";
    static final int X_PREFIX_LEN = X_PREFIX.length();

    static String [] parseSynteny(GlobalContext globalContext,
				  String is, DataElement d, String synteny) {
	if (is.equalsIgnoreCase("NA"))
	    return new String[]{"", "NA", "NA"};

	String nos = null;
	String nchr = null;
	String nposx = null;

	//System.out.println("parseSynteny: " + is + "@" + synteny);
	String rs[] = is.split(":");
	for (int n = 0; n < rs.length; n++) {
	    String s = rs[n];
	    if (s.startsWith(OS_PREFIX))
		nos = s.substring(OS_PREFIX_LEN, s.length());

	    else if (s.startsWith(CHR_PREFIX))
		nchr = s.substring(CHR_PREFIX_LEN, s.length());

	    else if (s.startsWith(X_PREFIX)) {
		nposx = s.substring(X_PREFIX_LEN, s.length());
		try {
		    long l = Long.parseLong(nposx);
		}
		catch(Exception e) {
		    return invalidSynteny(globalContext, d, synteny);
		}
	    }
	    else
		return invalidSynteny(globalContext, d, synteny);
	}

	if (nchr == null)
	    return invalidSynteny(globalContext, d, synteny);

	return new String[]{nos, nchr, nposx};
    }

    private boolean isEligible(DataSet dataSet, DataElement data, GraphPanel panel,
			       TreeMap params, GraphElement graphElement) {
	if (!isRegionning())
	    return true;

	if (panel == null && params == null)
	    return true;

	if (panel != null) {
	    int size = panel.getRegions().size();
	    for (int n = 0; n < size; n++) {
		Region region = (Region)panel.getRegions().get(n);
		/*
		double d_beginx = data.getPosX();
		double d_endx = beginx + data.getPosSize();
		double r_beginx = region.getBegin().getPosX();
		double r_endx = region.getEnd().getPosX();
		
		if ((d_beginx >= r_beginx && d_beginx <= r_endx) ||
		    (d_endx >= r_beginx && d_endx <= r_endx))
		    return true;
		*/
		if (data.crossRegion(region, graphElement))
		    return true;
	    }
	    
	    return false;
	}

	long regions[] = (long[])params.get(REGION_LIST_PARAM);
	if (regions == null)
	    return false;

	int length = regions.length / 2;
	for (int n = 0; n < length; n += 2) {
	    if (data.getPosX(dataSet) >= regions[2*n] &&
		data.getPosX(dataSet) <= regions[2*n+1])
		return true;
	}

	return false;
    }

    private void initParams(TreeMap params) {
	if (params == null)
	    return;

	String region_str = (String)params.get(REGION_LIST_STR_PARAM);
	if (region_str == null)
	    return;

	String regions[] = region_str.split("\\|");
	long region_pos[] = new long[regions.length*2];
	for (int n = 0; n < regions.length; n++) {
	    String str[] = regions[n].split(":");
	    region_pos[2*n] = Long.parseLong(str[0]);
	    region_pos[2*n+1] = Long.parseLong(str[1]);
	}

	params.put(REGION_LIST_PARAM, region_pos);
    }

    private boolean isLinked() {return (flags & SWITCH) == 0;}
    private boolean isRegionning() {return (flags & REGION) != 0;}
    private boolean isSwitch() {return (flags & SWITCH) != 0;}

    // 3/01/05: reverse link management disconnected
    /*
    private void setLinks(DataSet dataSet, DataSet rDataSet) {
	DataElement data[] = dataSet.getData();
	DataElement rData[] = rDataSet.getData();
	for (int n = 0; n < data.length; n++) {
	    Vector v = (Vector)data[n].getPropertyValue(VAMPConstants.LinkedDataProp);
	    if (v != null) {
		int size = v.size();
		Vector v1 = new Vector();
		for (int i = 0; i < size; i++) {
		    v1.add(rData[((Integer)v.get(i)).intValue()]);
		}
		data[n].setPropertyValue(VAMPConstants.LinkedDataProp, v1);
	    }
	}
    }
    */

    private Vector initLinkedDataProp(DataElement d) {
	Vector v = (Vector)d.getPropertyValue(VAMPProperties.LinkedDataProp);
	if (v == null) {
	    v = new Vector();
	    d.setPropertyValue(VAMPProperties.LinkedDataProp, v);
	}
	return v;
    }

    private DataElement makeDataElement(DataSet rDataSet, DataElement d,
					DataSet dataSet, String OS,
					String syntenyOS,
					String syntenyInfo[],
					int ind) throws Exception {
	DataElement nd = (DataElement)d.clone();
	nd.copyPos(rDataSet, d, dataSet);

	nd.removeProperty(VAMPProperties.LinkedDataProp);

	if (isLinked()) {
	    Vector v;

	    v = initLinkedDataProp(nd);
	    v.add(d);

	    // 3/01/05: disconnected reverse link
	    // v = initLinkedDataProp(d);
	    // v.add(new Integer(ind));
	}
	else
	    d.removeProperty(VAMPProperties.LinkedDataProp);

	nd.setPropertyValue(VAMPProperties.OrganismProp, syntenyOS);
		    
	//System.out.println("syntenyInfo: " + nd.getID() + ": " + syntenyInfo[CHR_IND]);
	nd.setPropertyValue(VAMPProperties.ChromosomeProp, syntenyInfo[CHR_IND]);

	nd.setPropertyValue
	    (VAMPProperties.SyntenyOrigProp,
	     "OS=" + OS +
	     ":Chr=" + VAMPUtils.getChr(d) +
	     ":X=" +
	     d.getPropertyValue(VAMPProperties.PositionProp));
		    
	if (syntenyInfo[X_IND] != null) {
	    nd.setPosX(rDataSet, Double.parseDouble(syntenyInfo[X_IND]));
	    nd.setPropertyValue(VAMPProperties.PositionProp, syntenyInfo[X_IND]);
	}


	return nd;
    }

    private String getSName() {
	if (flags == REGION)
	    return "RegionSynteny";

	if (flags == SWITCH)
	    return "SyntenySwitch";

	return "Synteny";
    }

    private String getRegionString(GraphPanel panel, TreeMap params) {
	if (panel != null)
	    return panel.getRegionString();
	return (String)params.get(REGION_LIST_STR_PARAM);
    }

    private void dump_pos(String tag, DataSet ds) {
	DataElement data[] = ds.getData();
	for (int j = 0; j < data.length; j++)
	    System.out.println(tag + " " + data[j].getID() +
			       " -> " + data[j].getPropertyValue
			       (VAMPProperties.PositionProp));
    }
}

