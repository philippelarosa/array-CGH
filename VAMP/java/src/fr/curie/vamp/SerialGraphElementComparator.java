
/*
 *
 * SerialGraphElementComparator.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.*;

class SerialGraphElementComparator implements Comparable {

    SerialGraphElement serialGraphElement;
    boolean unique;

    SerialGraphElementComparator(SerialGraphElement serialGraphElement) {
	this(serialGraphElement, false);
    }

    SerialGraphElementComparator(SerialGraphElement serialGraphElement, boolean unique) {
	this.serialGraphElement = serialGraphElement;
	this.unique = unique;
    }
    
    private String normalize(SerialGraphElement serialGraphElement) {
	return normalize(serialGraphElement.getID(),
			 serialGraphElement.getChr(),
			 serialGraphElement.getDSID());
    }

    private String normalize(String ID, String chr, long dsid) {
	return VAMPUtils.normChr(chr) + "::" + ID + (unique ? "::" + dsid : "");
    }

    public int compareTo(Object o) {
	return normalize(serialGraphElement).compareTo(normalize(((SerialGraphElementComparator)o).serialGraphElement));
    }
}
