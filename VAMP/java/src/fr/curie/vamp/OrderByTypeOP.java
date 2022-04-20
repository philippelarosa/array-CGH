
/*
 *
 * OrderByTypeOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class OrderByTypeOP extends OrderByOP {
   
    static final String NAME = "Order by Type / Name";

    OrderByTypeOP() {
	super(NAME, ON_ALL_AUTO|SHOW_MENU);
	setComp(new OrderByTypeComparator());
    }

    class OrderByTypeComparator implements Comparator {
	private String getType(GraphElement ds) {
	    String type = (String)ds.getPropertyValue(VAMPProperties.TypeProp);
	    String name = (String)ds.getPropertyValue(VAMPProperties.NameProp);
	    return normalize_str(type) + SEP +
		normalize_str(name) + SEP +
		ds.getOrder();
	}

	public int compare(Object o1, Object o2) {
	    GraphElement ds1 = (GraphElement)o1;
	    GraphElement ds2 = (GraphElement)o2;
	    String type1 = getType(ds1);
	    String type2 = getType(ds2);
	    return type1.compareToIgnoreCase(type2);
	}
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean supportProfiles() {return true;}
}

