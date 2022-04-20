
/*
 *
 * TranscriptomeOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class TranscriptomeOP extends GraphElementListOperation {
   
    static final String NAME = "Load Transcriptome";
    static final String LIGHT_NAME = "Light Load Transcriptome";
    PropertyElementFilter filter;

    static GraphElementListOperation splitChrOp;
    static GraphElementListOperation mergeChrOp;
    boolean full;

    public String[] getSupportedInputTypes() {
	return new String[]{
	    VAMPConstants.CGH_ARRAY_TYPE,
	    VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE
	};
    }

    public String getReturnedType() {
	return VAMPConstants.TRANSCRIPTOME_TYPE;
    }

    TranscriptomeOP(boolean full) {
	super((full ? NAME : LIGHT_NAME), SHOW_MENU|ADD_SEPARATOR);
	this.full = full;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    GraphElement dset = (GraphElement)graphElements.get(n);
	    //String v = VAMPUtils.getType(dset);
	    //if (v == null || !v.equals(VAMPConstants.CGH_ARRAY_TYPE)) return false;
	    if (dset.getPropertyValue(VAMPProperties.NumHistoProp) == null)
		return false;
	    String team = (String)dset.getPropertyValue(VAMPProperties.TeamProp);
	    if (team == null)
		return false;
	}
	return true;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	GlobalContext globalContext = (view != null ?
				       view.getGlobalContext() :
				       (GlobalContext)params.get("GlobalContext"));
	return undoManage(panel,
			  apply(globalContext, graphElements, params, autoApply));
    }

    String error;
    DataSet lastErrorRef;

    public Vector apply(GlobalContext globalContext,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	error = null;
	lastErrorRef = null;
	try {

	    int size = graphElements.size();
	    Vector rGraphElements = new Vector();

	    for (int n = 0; n < size; n++) {
		DataSet ref = ((GraphElement)graphElements.get(n)).asDataSet();
		if (ref == null)
		    return null;

		String type = VAMPUtils.getType(ref);

		String team = (String)ref.getPropertyValue(VAMPProperties.TeamProp);
		rGraphElements.add(ref);
		TranscriptomeFactory factory =
		    TranscriptomeFactory.getDefaultFactory(globalContext);

		factory.setFilter(filter);
		
		LinkedList dset_list;
		if (type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
		    dset_list = cghChrMergePerform(factory, ref, params, autoApply);
		else if (type.equals(VAMPConstants.CGH_ARRAY_TYPE))
		    dset_list = cghArrayPerform(factory, ref, params, autoApply);
		else
		    dset_list = null;

		if (dset_list == null)
		    continue;

		int sz = dset_list.size();
		for (int i = 0; i < sz; i++) {
		    DataSet dset_trs = ((GraphElement)dset_list.get(i)).asDataSet();
		    if (dset_trs == null)
			return null;

		    dset_trs.setAutoY(true);
		    // EV change 15/04/04: was ref.clone() !
		    dset_trs.setPropertyValue(VAMPProperties.ArrayRefProp, ref);
		    /*
		      System.out.println("transOP: arrayRef of " +
		      dset_trs.getID() + ":" +
		      VAMPUtils.getChr(dset_trs) + " is " +
		      ref.getID() + ":" + VAMPUtils.getChr(ref));
		    */


		    dset_trs.setPropertyValue(VAMPProperties.ArrayRefNameProp,
					      (String)ref.getPropertyValue(VAMPProperties.NameProp));
		    if (type.equals(VAMPConstants.CGH_ARRAY_TYPE))
			dset_trs.setAxisDisplayer(Config.defaultTranscriptomeAxisDisplayer);
		    else
			dset_trs.setAxisDisplayer(Config.defaultTranscriptomeChrMergeAxisDisplayer);
		    rGraphElements.add(dset_trs);
		}
	    }
	    if (error != null)
		InfoDialog.pop(globalContext, error);
	    return rGraphElements;
	}
	catch(Exception e) {
	    System.err.println(e);
	    return null;
	}
    }

    void setFilter(PropertyElementFilter filter) {
	this.filter = filter;
    }

    
    void setError(DataSet ref) {
	if (lastErrorRef == ref)
	    return;

	lastErrorRef = ref;

	String chr = VAMPUtils.getChr(ref);
	if (error == null)
	    error = "";
	else
	    error += "\n";
	error += "Transcriptome of " + ref.getID() +
	    (chr != null ? " / " + chr : "") + " not found";
    }

    LinkedList cghChrMergePerform(TranscriptomeFactory factory,
				  DataSet ref, TreeMap params,
				  boolean autoApply) {
	Vector v = new Vector();
	v.add(ref);

	if (splitChrOp == null)
	    splitChrOp = GraphElementListOperation.get(SplitChrOP.CGH_NAME);
	if (mergeChrOp == null)
	    mergeChrOp = GraphElementListOperation.get(MergeChrOP.TRANSCRIPTOME_NAME);
	Vector splitDataSets =
	    splitChrOp.apply(null, null, v, null, autoApply);

	if (splitDataSets == null)
	    return null;

	LinkedList dset_list = new LinkedList();

	int sz = splitDataSets.size();
	for (int n = 0; n < sz; n++) {
	    DataSet sDS = ((GraphElement)splitDataSets.get(n)).asDataSet();
	    LinkedList list = factory.buildDataSets
		((params != null ? params.get("URL") : null), sDS,
		 true, full);

	    if (list == null) {
		continue;
	    }
	    dset_list.addAll(list);
	}

	if (dset_list.size() == 0) {
	    setError(ref);
	    return null;
	}

	Vector mergeDataSets = mergeChrOp.apply(null, null, Utils.listToVector(dset_list), null, autoApply);

	if (mergeDataSets == null)
	    return null;

	return Utils.vectorToList(mergeDataSets);
    }

    LinkedList cghArrayPerform(TranscriptomeFactory factory,
			       DataSet ref, TreeMap params,
			       boolean autoApply) {

	LinkedList dset_list = factory.buildDataSets
	    ((params != null ? params.get("URL") : null), ref,
	     true, full);

	if (dset_list == null)
	    setError(ref);

	return dset_list;
    }

    public boolean useThread() {
	return true;
    }

    public String getMessage() {
	return "Loading transcriptomes...";
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}
}
