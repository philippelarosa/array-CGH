
/*
 *
 * CheckConsistencyOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;

class CheckConsistencyOP extends GraphElementListOperation {
   
    static final String NAME = "CheckConsistency";
    static final int MAX_LENGTH = 512;

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    CheckConsistencyOP() {
	super(NAME, SHOW_MENU);
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	String error = checkConsistency(graphElements);
	String msg;

	if (error.length() == 0)
	    msg = "Profile" + (graphElements.size() > 1 ? "s are" : " is") +
		" consistent";
	else
	    msg = "Profile" + (graphElements.size() > 1 ? "s are" : " is") +
		" not consistent:\n" + error;

	InfoDialog.pop(view.getGlobalContext(), msg);
	return graphElements;
    }

    static String checkConsistency(Vector graphElements) {
	String error = "";
	error += checkObjKeyConsistency(graphElements);
	error += checkChromosomeConsistency(graphElements);
	return error;
    }

    static String checkObjKeyConsistency(Vector graphElements) {
	int size = graphElements.size();
	String error = "";

	for (int m = 0; m < size; m++) {
	    DataSet dset = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (dset == null)
		continue;

	    DataElement data[] = dset.getData();
	    HashMap map = new HashMap();
	    for (int n = 0; n < data.length; n++) {
		String s = VAMPUtils.objKey(dset, data[n]);
		if (map.get(s) != null) {
		    error += "duplicate objkey " + s + " at #" + n +
			" in " + dset.getID() + "\n";
		    if (error.length() > MAX_LENGTH) {
			return error + "...\n";
		    }
		}
		map.put(s, new Boolean(true));
	    }
	}

	return error;
    }

    static String checkChromosomeConsistency(Vector graphElements) {

	String error = "";
	int size = graphElements.size();

	for (int m = 0; m < size; m++) {
	    DataSet dset = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (dset == null)
		continue;

	    String ochr = "";
	    HashMap map = new HashMap();
	    HashMap imap = new HashMap();
	    DataElement data[] = dset.getData();

	    for (int n = 0; n < data.length; n++) {
		DataElement d = data[n];
		String chr = VAMPUtils.getChr(d);

		if (!chr.equals(ochr)) {
		    if (map.get(chr) != null && imap.get(chr) == null) {
			error += "chromosome inconsistency at " +
			    d.getID() + " chr #" + chr + " in " +
			    dset.getID() + "\n";
			imap.put(chr, new Boolean(true));
			if (error.length() > MAX_LENGTH) {
			    return error + "...\n";
			}
		    }
		    
		    map.put(chr, new Boolean(true));
		    ochr = chr;
		}
	    }
	}
	    
	return error;
    }

    public boolean useThread() {
	return true;
    }
}
