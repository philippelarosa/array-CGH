
/*
 *
 * DataSetArrayComparator.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

// TBD: should be factorized with DataSetChrComparator:
/*
abstract class GraphElementChrArrayCompator {
  abstract protected ||static|| String normalize(String ID, String chr);
}

class DataSetChrComparator extends GraphElementChrArrayCompator {
  String normalize(String ID, String chr) { ... }
}

class DataSetArrayComparator extends GraphElementChrArrayCompator {
  String normalize(String ID, String chr) { ... }
}
*/

import java.util.*;

class DataSetArrayComparator implements Comparable {

    GraphElement graphElement;
    boolean unique;

    DataSetArrayComparator(GraphElement graphElement) {
	this(graphElement, false);
    }

    DataSetArrayComparator(GraphElement graphElement, boolean unique) {
	this.graphElement = graphElement;
	this.unique = unique;
    }
    
    private String normalize(GraphElement ge) {
	return normalize((String)ge.getID(), VAMPUtils.getChr(ge), ge.getDSID());
    }

    private String normalize(String ID, String chr, long dsid) {
	/*
	if (chr.length() == 1 && chr.charAt(0) >= '0' && chr.charAt(0) <= '9')
	    chr = "0" + chr;
	*/
	return VAMPUtils.normChr(chr) + "::" + ID + (unique ? "::" + dsid : "");
    }

    public int compareTo(Object o) {
	/*
	String thisChr = VAMPUtils.getChr(graphElement);
	String oChr = VAMPUtils.getChr(((DataSetArrayComparator)o).graphElement);

	String thisID = (String)graphElement.getID();
	String oID = (String)((DataSetArrayComparator)o).graphElement.getID();

	String thisDSID = graphElement.getDSIS();
	String oDSID = ((DataSetArrayComparator)o).graphElement.getDSID();
	//if (thisChr.equals(oChr) && thisID.equals(oID) &&
	//return 0;

	return normalize(thisID, thisChr, thisDSID).compareTo(normalize(oID, oChr, oDSID));
	*/
	    
	return normalize(graphElement).compareTo(normalize(((DataSetArrayComparator)o).graphElement));
    }
}
