
/*
 *
 * DataElementChrComparator.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class DataElementChrComparator implements Comparable {

    DataElement data;
    boolean add_ind;

    DataElementChrComparator(DataElement data, boolean add_ind) {
	this.data = data;
	this.add_ind = add_ind;
    }
    
    DataElementChrComparator(DataElement data) {
	this(data, false);
    }
    
    private static String normalize(String chr, long posx) {
	/*
	if (chr.length() == 1 && chr.charAt(0) >= '0' && chr.charAt(0) <= '9')
	    chr = "0" + chr;
	*/
	return VAMPUtils.normChr(chr) + "::" + posx;
    }

    private static String normalize(String chr, long posx, int ind) {
	return normalize(chr, posx) + "::" + ind;
    }

    public int compareTo(Object o) {
	DataElement odata = ((DataElementChrComparator)o).data;
	String thisChr = VAMPUtils.getChr(data);
	String oChr = VAMPUtils.getChr(odata);

	long thisPosX = (long)Utils.parseDouble
	    ((String)data.getPropertyValue(VAMPProperties.PositionProp));

	long oPosX = (long)Utils.parseDouble
	    ((String)odata.getPropertyValue(VAMPProperties.PositionProp));

	if (add_ind)
	    return normalize(thisChr, thisPosX, data.getInd()).
		compareTo(normalize(oChr, oPosX, odata.getInd()));

	return normalize(thisChr, thisPosX).compareTo(normalize(oChr, oPosX));
    }
}
