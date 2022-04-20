
/*
 *
 * PatternItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class PatternItem {
    static final int BEGIN = 0x1;
    static final int END = 0x2;
    int pos; // combination of BEGIN, END or 0
    String substring;
    boolean case_sensitive;

    PatternItem(int pos, String substring, boolean case_sensitive) {
	this.pos = pos;
	this.case_sensitive = case_sensitive;
	this.substring = case_sensitive ? substring : substring.toUpperCase();
    }

    boolean matches(String input) {
	if (!case_sensitive)
	    input = input.toUpperCase();

	if ((pos & BEGIN) != 0 && (pos & END) != 0)
	    return substring.equals(input);

	if ((pos & BEGIN) != 0)
	    return input.indexOf(substring) == 0;

	if ((pos & END) != 0) {
	    int slen = substring.length();
	    int ilen = input.length();
	    return ilen >= slen &&
		input.substring(ilen - slen, ilen).equals(substring);
	}

	return input.indexOf(substring) != -1;
    }

    void display() {
	System.out.println("\t" + this.substring + " [" + pos + "]");
    }
}
