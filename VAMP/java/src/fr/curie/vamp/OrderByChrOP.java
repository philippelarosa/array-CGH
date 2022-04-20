
/*
 *
 * OrderByChrOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class OrderByChrOP extends OrderByOP {
   
    static final String NAME = "Order by Chromosome / Name";

    OrderByChrOP() {
	super(NAME, ON_ALL_AUTO|SHOW_MENU);
	setComp(new OrderByChrComparator());
    }

    class OrderByChrComparator implements Comparator {

	private String getChr(GraphElement ds) {
	    String chr = VAMPUtils.getChr(ds);
	    String name = (String)ds.getPropertyValue(VAMPProperties.NameProp);

	    return normalize_chr(chr) + SEP +
		normalize_str(name) + SEP +
		ds.getOrder();
	}

	public int compare(Object o1, Object o2) {
	    GraphElement ds1 = (GraphElement)o1;
	    GraphElement ds2 = (GraphElement)o2;
	    String chr1 = getChr(ds1);
	    String chr2 = getChr(ds2);
	    return chr1.compareToIgnoreCase(chr2);
	}
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean supportProfiles() {return true;}
}

