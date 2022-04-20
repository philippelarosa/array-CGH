
/*
 *
 * DataSetChrComparator.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class DataSetChrComparator implements Comparable {

    GraphElement graphElement;

    DataSetChrComparator(GraphElement graphElement) {
	this.graphElement = graphElement;
    }
    
    private static String normalize(String OS, String ID, String chr) {
	/*
	if (chr.length() == 1 && chr.charAt(0) >= '0' && chr.charAt(0) <= '9')
	    chr = "0" + chr;
	*/
	chr = VAMPUtils.normChr(chr);
	if (OS == null)
	    return ID + "::" + chr;
	return OS + "::" + ID + "::" + chr;
    }

    public int compareTo(Object o) {
	String thisOS = VAMPUtils.getOS(graphElement);

	String thisChr = VAMPUtils.getChr(graphElement);
	String oChr = VAMPUtils.getChr(((DataSetChrComparator)o).graphElement);

	String oOS = VAMPUtils.getOS(((DataSetChrComparator)o).graphElement);

	String thisID = (String)graphElement.getID();
	String oID = (String)((DataSetChrComparator)o).graphElement.getID();

	if (thisChr.equals(oChr) && thisID.equals(oID))
	    return 0;

	return normalize(thisOS, thisID, thisChr).compareTo(normalize(oOS, oID, oChr));
    }
}
