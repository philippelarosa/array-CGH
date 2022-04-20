
/*
 *
 * OrderByOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

abstract class OrderByOP extends GraphElementListOperation {
   
    Comparator comp;

public String[] getSupportedInputTypes() {
	return null;
    }

public String getReturnedType() {
	return null;
    }

    OrderByOP(String name, int flags) {
	super(name, flags);
    }

    void setComp(Comparator comp) {
	this.comp = comp;
    }

public boolean mayApply(GraphElementListOperation op) {
	return true;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	graphElements = getGraphElements(panel, graphElements, autoApply);
	TreeSet tset = new TreeSet(comp);
	int sz = graphElements.size();
	for (int n = 0; n < sz; n++)
	    ((GraphElement)graphElements.get(n)).setOrder(n);

	tset.addAll(graphElements);
	Vector rGraphElements = new Vector();
	/*
	Iterator it = tset.iterator();
	while (it.hasNext()) {
	    rGraphElements.add((GraphElement)it.next());
	*/
	rGraphElements.addAll(tset);
	return undoManage(panel, rGraphElements);
    }

    static final int STR_LEN = 30;

    String normalize_str(String str) {
	if (str == null)
	    str = "?";

	while (str.length() < STR_LEN)
	    str = str + "_";

	return str;
    }

    String normalize_chr(String chr) {
	if (chr == null)
	    chr = "?";
	else if (chr.equals("X"))
	    chr = "23";
	else if (chr.equals("Y"))
	    chr = "24";
	else if (chr.length() == 1)
	    chr = "0" + chr;
	return chr;
    }

    static final String SEP = "::";
}

