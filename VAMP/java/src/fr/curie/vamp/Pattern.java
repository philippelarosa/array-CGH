
/*
 *
 * Pattern.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class Pattern {
    static final char WILDCAR_CHAR = '*';
    static final String WILDCAR_STRING = (new Character(WILDCAR_CHAR)).toString();
    String pattern;
    Vector items;

    Pattern(String pattern, boolean case_sensitive) {
	this.pattern = pattern;
	items = new Vector();
	String patterns[] = pattern.split("\\" + WILDCAR_STRING);

	if (patterns.length == 0) {
	    if (pattern.length() > 0 && pattern.charAt(0) == WILDCAR_CHAR)
		items.add(new PatternItem(0, "", case_sensitive));
	    return;
	}

	for (int i = 0; i < patterns.length; i++) {
	    if (patterns[i].length() != 0) {
		int pos = 0;
		if (i == 0)
		    pos |= PatternItem.BEGIN;
		if (i == patterns.length-1 &&
		    pattern.charAt(pattern.length()-1) != WILDCAR_CHAR)
		    pos |= PatternItem.END;
		items.add(new PatternItem(pos, patterns[i], case_sensitive));
	    }
	}
    }

    boolean matches(String input) {
	int size = items.size();
	if (size == 0) return false;
	for (int n = 0; n < size; n++) {
	    if (!((PatternItem)items.get(n)).matches(input))
		return false;
	}
	return true;
    }

    // debug function
    void display() {
	int cnt = items.size();
	System.out.println("PATTERN: " + pattern);
	for (int n = 0; n < cnt; n++)
	    ((PatternItem)items.get(n)).display();
    }
}
