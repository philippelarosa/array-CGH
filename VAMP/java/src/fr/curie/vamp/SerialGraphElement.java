
/*
 *
 * SerialGraphElement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.Vector;

public class SerialGraphElement implements java.io.Serializable {
    private String id;
    private String chr;
    private long dsid;
    // any other info

    SerialGraphElement(GraphElement graphElement) {
	id = (String)graphElement.getID();
	chr = VAMPUtils.getChr(graphElement);
	dsid = graphElement.getDSID();
    }

    String getID() {
	return id;
    }

    String getChr() {
	return chr;
    }

    long getDSID() {
	return dsid;
    }

    static Vector<SerialGraphElement> convert(Vector graphElements) {
	Vector<SerialGraphElement> v = new Vector();
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    v.add(new SerialGraphElement((GraphElement)graphElements.get(n)));
	}

	return v;
    }
}
