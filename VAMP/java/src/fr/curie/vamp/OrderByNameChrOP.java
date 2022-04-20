
/*
 *
 * OrderByNameChrOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class OrderByNameChrOP extends OrderByOP {
   
    static final String NAME = "Order by Name / Chromosome";

    OrderByNameChrOP() {
	super(NAME, ON_ALL_AUTO|SHOW_MENU);
	setComp(new OrderByNameChrComparator());
    }

    class OrderByNameChrComparator implements Comparator {
	private String getName(GraphElement ds) {
	    String name = (String)ds.getPropertyValue(VAMPProperties.NameProp);
	    String chr = VAMPUtils.getChr(ds);

	    return normalize_str(name) + SEP +
		normalize_chr(chr) + SEP +
		ds.getOrder();
	}

	public int compare(Object o1, Object o2) {
	    GraphElement ds1 = (GraphElement)o1;
	    GraphElement ds2 = (GraphElement)o2;
	    String name1 = getName(ds1);
	    String name2 = getName(ds2);
	    return name1.compareToIgnoreCase(name2);
	}
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean supportProfiles() {return true;}
}

