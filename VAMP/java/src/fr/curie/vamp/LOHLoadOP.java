
/*
 *
 * LOHLoadOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class LOHLoadOP extends GraphElementListOperation {
   
    static final String NAME = "LOH Load";

    static GraphElementListOperation splitChrOp;
    static GraphElementListOperation mergeChrOp;

    DataSet lastErrorRef;
    String error = null;

public String[] getSupportedInputTypes() {
	return new String[]{
	    VAMPConstants.CGH_ARRAY_TYPE,
	    VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
    }

public String getReturnedType() {
	//return VAMPConstants.LOH_TYPE;
	return null;
    }

    LOHLoadOP() {
	super(NAME, SHOW_MENU|ADD_SEPARATOR);
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    GraphElement dset = (GraphElement)graphElements.get(n);
	    String v = (String)dset.getPropertyValue(VAMPProperties.TypeProp);
	    /*
	    if (v == null ||
		(!v.equals(VAMPConstants.CGH_ARRAY_TYPE) &&
		 !v.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE)))
		return false;
	    */
	    if (dset.getPropertyValue(VAMPProperties.NumHistoProp) == null)
		return false;
	    String team = (String)dset.getPropertyValue(VAMPProperties.TeamProp);
	    if (team == null)
		return false;
	    SystemConfig systemConfig = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);
	}
	return true;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
    return apply(view.getGlobalContext(), graphElements, params, autoApply);
    }

public Vector apply(GlobalContext globalContext,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	error = null;
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
		XMLLOHFactory factory = XMLLOHFactory.getFactory(globalContext);
		/*
		LinkedList dset_list = factory.buildDataSets
		    ((params != null ? params.get("URL") : null), ref, true);
		*/

		LinkedList dset_list;
		if (type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
		    dset_list = lohChrMergePerform(factory, ref, params, autoApply);
		else if (type.equals(VAMPConstants.CGH_ARRAY_TYPE))
		    dset_list = lohArrayPerform(factory, ref, params, autoApply);
		else
		    dset_list = null;

		if (dset_list == null) {
		    if (error == null)
			error = "";
		    else
			error += "\n";
		    String chr = VAMPUtils.getChr(ref);
		    error += "LOH of " + ref.getID() +
			(chr != null ? " / " + chr : "") + " not found";
		    continue;
		}

		int sz = dset_list.size();
		for (int i = 0; i < sz; i++) {
		    DataSet dset_loh = (DataSet)dset_list.get(i);

		    // EV : 5/02/07
		    dset_loh.setAutoY(true);

		    dset_loh.setPropertyValue(VAMPProperties.ArrayRefProp, ref);
		    dset_loh.setPropertyValue(VAMPProperties.ArrayRefNameProp,
					      (String)ref.getPropertyValue(VAMPProperties.NameProp));
		    /*
		    if (type.equals(VAMPConstants.CGH_ARRAY_TYPE))
			dset_loh.setAxisDisplayer(Config.defaultTranscriptomeAxisDisplayer);
		    else
			dset_loh.setAxisDisplayer(Config.defaultTranscriptomeChrMergeAxisDisplayer);
		    */

		    rGraphElements.add(dset_loh);
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

    void setError(DataSet ref) {
	if (lastErrorRef == ref)
	    return;

	lastErrorRef = ref;

	String chr = VAMPUtils.getChr(ref);
	if (error == null)
	    error = "";
	else
	    error += "\n";
	error += "LOH of " + ref.getID() +
	    (chr != null ? " / " + chr : "") + " not found";
    }

    LinkedList lohChrMergePerform(XMLLOHFactory factory,
				  DataSet ref, TreeMap params,
				  boolean autoApply) {
	Vector v = new Vector();
	v.add(ref);

	if (splitChrOp == null)
	    splitChrOp = GraphElementListOperation.get(SplitChrOP.CGH_NAME);
	if (mergeChrOp == null)
	    mergeChrOp = GraphElementListOperation.get(MergeChrOP.LOH_NAME);

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
		 true);

	    if (list == null) {
		//setError(ref);
		continue;
		//return null;
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

    LinkedList lohArrayPerform(XMLLOHFactory factory,
			       DataSet ref, TreeMap params,
			       boolean autoApply) {

	LinkedList dset_list = factory.buildDataSets
	    ((params != null ? params.get("URL") : null), ref,
	     true);

	if (dset_list == null)
	    setError(ref);

	return dset_list;
    }

public boolean useThread() {
	return true;
    }
}
