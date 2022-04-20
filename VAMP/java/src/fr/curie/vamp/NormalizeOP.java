
/*
 *
 * NormalizeOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import fr.curie.vamp.data.Probe;

class NormalizeOP extends GraphElementListOperation {
   
    static final String NAME = "Normalization";
    static final boolean USE_NEW_NORMALIZE = true;
    static final boolean APPLY_GC = true;

    public final static Property TmpObjKeyProp = Property.getHiddenProperty("TmpObjKeyProp", Property.TEMPORARY);

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    NormalizeOP() {
	super(NAME, SHOW_MENU);
    }

    static boolean haveDifferentSizes(Vector graphElements) {

	/*
	if (true) {
	    System.err.println("WARNING: skipping normalization test");
	    return true;
	}
	*/

	int size = graphElements.size();
	if (size == 0)
	    return false;
	int cnt = 0;
	for (int n = 0; n < size; n++) {
	    GraphElement dset = (GraphElement)graphElements.get(n);
	    if (n != 0 && cnt != dset.getProbeCount()) {
		return true;
	    }
	    cnt = dset.getProbeCount();
	}

	// size is 0 or all are count are the same
	return false;
    }

    static class DataComparator implements Comparator {

	GraphElement graphElement;

	DataComparator(GraphElement graphElement) {
	    this.graphElement = graphElement;
	}

	public int compare(Object o1, Object o2) {
	    DataElement d1 = (DataElement)o1;
	    DataElement d2 = (DataElement)o2;

	    if (d1.getPropertyValue(TmpObjKeyProp).equals
		(d2.getPropertyValue(TmpObjKeyProp))) {
		return 0;
	    }

	    return d1.getPosX(graphElement) -
		d2.getPosX(graphElement) > 0 ? 1 : -1;
	}
    }

    /*
    static class DataSetComparator implements Comparator {

	public int compare(Object o1, Object o2) {
	    DataSet dset1 = (DataSet)o1;
	    DataSet dset2 = (DataSet)o2;

	    // if we compared on the ObjKeyProp ?
	    if (dset1.getID().equals(dset2.getID()))
		return 0;

	    return ((String)dset1.getID()).compareTo((String)dset2.getID());
	}
    }
    */

    static GraphElement makeGraphElement(DataSet dset,
					 TreeSet dsetSet) {
	DataElement data[] = new DataElement[dsetSet.size()];
	Iterator it = dsetSet.iterator();
	for (int n = 0; it.hasNext(); n++)
	    data[n] = (DataElement)it.next();
	dset.setData(data);
	dset.setPropertyValue(VAMPProperties.CloneCountProp, new Integer(data.length));
	return dset;
    }
    
    static Vector normalize_old(GlobalContext globalContext, Vector graphElements) {
	if (!haveDifferentSizes(graphElements)) {
	    return graphElements;
	}

	try {
	    int size = graphElements.size();
	    Vector rGraphElements = new Vector();

	    TreeSet objKeySet = new TreeSet();
	    for (int m = 0; m < size; m++) {
		objKeySet.add(((GraphElement)graphElements.get(m)).getObjKeyProp());
	    }

	    HashMap mMap = new HashMap();
	    HashMap dsetMap[] = new HashMap[size];
	    TreeSet dsetSet[] = new TreeSet[size];

	    for (int m = 0; m < size; m++) {
		DataSet dset = ((GraphElement)graphElements.get(m)).asDataSet();
		DataElement data[] = dset.getData();
		for (int n = 0; n < data.length; n++) {
		    DataElement d = data[n];
		    d.setPropertyValue(TmpObjKeyProp, VAMPUtils.objKey_set(objKeySet, d), false);
		}
	    }

	    for (int m = 0; m < size; m++) {
		dsetMap[m] = new HashMap();
		DataSet dset = ((GraphElement)graphElements.get(m)).asDataSet();
		dsetSet[m] = new TreeSet(new DataComparator(dset));
		DataElement data[] = dset.getData();

		for (int n = 0; n < data.length; n++) {
		    DataElement d = data[n];
		    dsetMap[m].put(VAMPUtils.objKey(dset, d), d);
		    dsetSet[m].add(d);
		    mMap.put(VAMPUtils.objKey(dset, d), new Object[]{d, dset});
		}
	    }

	    Iterator it = mMap.entrySet().iterator();

	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		for (int m = 0; m < size; m++) {
		    DataSet dset = ((GraphElement)graphElements.get(m)).asDataSet();
		    if (dsetMap[m].get(entry.getKey()) == null) {
			Object o[] = (Object[])entry.getValue();
			DataElement od = (DataElement)o[0];
			DataSet odset = (DataSet)o[1];
			DataElement d = (DataElement)od.clone();
			d.copyPos(dset, od, odset);

			d.setPosY(dset, 0);
			d.removeProperty(VAMPProperties.RatioProp);
			d.removeProperty(VAMPProperties.BreakpointProp);
			d.removeProperty(VAMPProperties.FlagProp);
			d.removeProperty(VAMPProperties.NBPProp);
			d.removeProperty(VAMPProperties.OutProp);
			d.removeProperty(VAMPProperties.ScoreProp);
			d.removeProperty(VAMPProperties.SmoothingProp);
			d.setPropertyValue(VAMPProperties.IsNAProp, "true", false);
			d.setPropertyValue(VAMPProperties.MissingProp, "true", false);
			int osz = dsetSet[m].size();
			dsetSet[m].add(d);
		    }
		}
	    }

	    for (int m = 0; m < size; m++) {
		DataSet dset = ((GraphElement)graphElements.get(m)).asDataSet();
		rGraphElements.add(makeGraphElement(dset, dsetSet[m]));
	    }

	    checkSizes(globalContext, mMap.size(), graphElements);

	    return rGraphElements;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println(e);
	    return null;
	}
    }

    // new normalization 
    static boolean haveProfileSameDesign(Vector graphElements) {
	return !haveDifferentSizes(graphElements);
    }

    static class DataElementProxyComparator implements Comparator {

	GraphElement graphElement;

	DataElementProxyComparator(GraphElement graphElement) {
	    this.graphElement = graphElement;
	}

	public int compare(Object o1, Object o2) {
	    RODataElementProxy d1 = (RODataElementProxy)o1;
	    RODataElementProxy d2 = (RODataElementProxy)o2;

	    if (d1.getPropertyValue(TmpObjKeyProp).equals
		(d2.getPropertyValue(TmpObjKeyProp))) {
		return 0;
	    }
	    
	    return d1.getPanGenPosX(graphElement) -
		d2.getPanGenPosX(graphElement) > 0 ? 1 : -1;
	}
    }

    static Vector normalize_new(GlobalContext globalContext, Vector _graphElements) {
	if (haveProfileSameDesign(_graphElements)) {
	    return _graphElements;
	}

	try {
	    int size = _graphElements.size();

	    // duplicate Vector and Profiles
	    Vector graphElements = new Vector();
	    for (int n = 0; n < size; n++) {
		graphElements.add(((GraphElement)_graphElements.get(n)).dupSerializer());
	    }

	    Vector rGraphElements = new Vector();

	    /*
	    Utils.gc();
	    Utils.freeMemory();
	    */

	    TreeMap params = new TreeMap();
	    params.put("Profiles", makeVectorID(graphElements));

	    boolean mustPerform = false;
	    ToolResultContext toolResultContexts[] = new ToolResultContext[size];
	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = (GraphElement)graphElements.get(m);
		toolResultContexts[m] = ToolResultManager.getInstance().prologue(globalContext, "Normalization", params, graphElement, graphElement);

		if (!mustPerform && toolResultContexts[m].getGraphElementResult() == null) {
		    mustPerform = true;
		}
	    }

	    if (!mustPerform) {
		//System.out.println("Normalize: ALREADY DONE!");
		for (int m = 0; m < size; m++) {
		    rGraphElements.add(toolResultContexts[m].getGraphElementResult());
		}
		
		return rGraphElements;
	    }

	    long ms0 = System.currentTimeMillis();

	    //System.out.println("Normalize: must use perform");

	    //System.out.println("Step #1: Computing ObjKeySet");
	    TreeSet objKeySet = new TreeSet();
	    for (int m = 0; m < size; m++) {
		objKeySet.add(((GraphElement)graphElements.get(m)).getObjKeyProp());
		//System.out.println("ObjKey: " + (((GraphElement)graphElements.get(m)).getObjKeyProp()).getName());
	    }

	    HashMap mMap = new HashMap();
	    HashMap dsetMap[] = new HashMap[size];

	    //System.out.println("Step #2");
	    for (int m = 0; m < size; m++) {
		GraphElement dset = (GraphElement)graphElements.get(m);
		//System.out.println("Profile #" + m);
		//Utils.freeMemory(APPLY_GC);
		dsetMap[m] = new HashMap();
		int probe_cnt = dset.getProbeCount();
		for (int n = 0; n < probe_cnt; n++) {
		    RODataElementProxy d = dset.getDataProxy(n);
		    d.complete(dset);
		    String key = VAMPUtils.objKey_set(objKeySet, d);
		    d.release();

		    d.setTempPropertyValue(TmpObjKeyProp, key);
		    dsetMap[m].put(key, new Integer(n));
		    if (mMap.get(key) == null) {
			mMap.put(key, new Object[]{d, dset});
		    }
		}

		dset.release();
	    }

	    long ms01 = System.currentTimeMillis();
	    //System.out.println("Step #2 time: " + ((ms01 - ms0)/1000));

	    //System.out.println("Step #3");
	    //Utils.freeMemory(APPLY_GC);

	    GraphElementListOperation op = GraphElementListOperation.get(NAME);

	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = toolResultContexts[m].getGraphElementResult();
		if (graphElement != null) {
		    //System.out.println("Normalize: PARTIALLY DONE");
		    rGraphElements.add(graphElement);
		    continue;
		}

		long ms1 = System.currentTimeMillis();

		GraphElement dset = (GraphElement)graphElements.get(m);
		TreeSet dsetSet = new TreeSet(new DataElementProxyComparator(dset));

		Iterator it = mMap.entrySet().iterator();
		int norm_cnt[] = new int[size];
		while (it.hasNext()) {
		    Map.Entry entry = (Map.Entry)it.next();
		    Object o[] = (Object[])entry.getValue();
		    RODataElementProxy od = (RODataElementProxy)o[0];
		    GraphElement odset = (GraphElement)o[1];

		    RWDataElementProxy d;
		    if (dsetMap[m].get(entry.getKey()) == null) {
			d = (RWDataElementProxy)od.cloneToRWProxy(false);

			d.declare(dset);
			d.setIsNA(dset);
			d.setIsMissing();
			od.copyToPos(dset, d, odset);

			d.syncProperties(RODataElementProxy.TYPE_PROP |
					 RODataElementProxy.GNL_PROP |
					 RODataElementProxy.POS_PROP |
					 RODataElementProxy.SIZE_PROP |
					 RODataElementProxy.CHR_PROP,
					 odset);

			d.removeProperty(VAMPProperties.RatioProp);
			d.removeProperty(VAMPProperties.BreakpointProp);
			d.removeProperty(VAMPProperties.FlagProp);
			d.removeProperty(VAMPProperties.NBPProp);
			d.removeProperty(VAMPProperties.OutProp);
			d.removeProperty(VAMPProperties.ScoreProp);
			d.removeProperty(VAMPProperties.SmoothingProp);
			d.setPropertyValue(VAMPProperties.IsNAProp, "true");

			d.setPropertyValue(VAMPProperties.MissingProp, "true");
			
			norm_cnt[m]++;
		    }
		    else {
			Integer n = (Integer)dsetMap[m].get(entry.getKey());
			d = (RWDataElementProxy)dset.getDataProxy(n.intValue());
		    }

		    d.setTempPropertyValue(TmpObjKeyProp, entry.getKey());
		    dsetSet.add(d);
		}

		long ms2 = System.currentTimeMillis();
		//System.out.println("Before serialized Profile #" + m + " scan time: " + ((ms2 - ms1)/1000));

		//Utils.freeMemory(APPLY_GC);

		// dsetSet contient les probes ordonnees
		// => calcul de la signature
		// le fichier doit avoir comme nom, celui de cette signature
		GraphElementFactory factory = toolResultContexts[m].getFactory();
		factory.init("", mMap.size(), dset.getProperties());

		assert mMap.size() == dsetSet.size();
		Iterator it2 = dsetSet.iterator();
		while (it2.hasNext()) {
		    RWDataElementProxy p = (RWDataElementProxy)it2.next();

		    if (p.isCompletable()) {
			p.release();
			p.complete(dset);
			factory.write(p);
		    }
		    else {
			assert p.getPropertyValue(VAMPProperties.MissingProp) != null;
			factory.write(p);
		    }
		    p.release();
		}
		
		dset.setPropertyValue(VAMPProperties.CloneCountProp, new Integer(mMap.size()));
		factory.setGraphElementProperties(dset.getProperties());
		
		GraphElement dset_o = dset;
		dset = ToolResultManager.getInstance().epilogue(toolResultContexts[m]);
		
		rGraphElements.add(dset);
		//System.out.println("#" + m + " " + norm_cnt[m]);
		dset.release(); // ??

		dsetSet = null;
		dsetMap[m] = null;
		norm_cnt = null;

		long ms3 = System.currentTimeMillis();
		//System.out.println("Haved serialized Profile #" + m + " serial time: " + ((ms3 - ms2)/1000));

		//Utils.freeMemory(APPLY_GC);
	    }

	    long ms4 = System.currentTimeMillis();
	    System.out.println("Normalization duration: " + ((ms4 - ms0)/1000) + " seconds");

	    checkSizes(globalContext, mMap.size(), rGraphElements);

	    return rGraphElements;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println(e);
	    return null;
	}
    }

    static Vector normalize(GlobalContext globalContext, Vector graphElements) {
	if (USE_NEW_NORMALIZE) {
	    return normalize_new(globalContext, graphElements);
	}
	return normalize_old(globalContext, graphElements);
    }

    private static void checkSizes(GlobalContext globalContext, int mapSize, Vector graphElements) {
	int size = graphElements.size();

	String error = "";
	for (int m = 0; m < size; m++) {
	    GraphElement dset = (GraphElement)graphElements. get(m);
	    
	    if (dset.getProbeCount() != mapSize) {
		if (error.length() == 0)
		    error = "Normalization failed:\n";
		else
		    error += "\n";
		
		error += "invalid probe count " + dset.getProbeCount() +
		    " for profile " + dset.getID() + " expected " + mapSize;
	    }
	}

	if (error.length() > 0) {
	    InfoDialog.pop(globalContext, error);
	}
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	return haveDifferentSizes(graphElements);
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	return normalize(view.getGlobalContext(), graphElements);
    }

    public boolean useThread() {
	return true;
    }

    public boolean supportProfiles() {
	return true;
    }

    private static boolean normalize_ok;

    public static Vector normalizeOnDemand(GlobalContext globalContext, String opName, Vector graphElements) {

	if (GraphElement.hasProfile(graphElements) && !haveProfileSameDesign(graphElements)) {
	    normalize_ok = false;
	    ConfirmDialog.pop
		(globalContext, "Profiles have different designs: " + opName + " needs a normalization.\nDo you really want to normalize these profiles ?",
		 new Action() {
		     public void perform(Object arg) {
			 normalize_ok = true;
		     }
		 }, null, "Yes", "No");
	    
	    if (!normalize_ok) {
		return null;
	    }
	}

	return normalize(globalContext, graphElements);
    }
}
