
/*
 *
 * FontResourceBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class FontResourceBuilder extends ResourceBuilder {

    FontResourceBuilder() {
	super("Font");
    }

    Class getVClass() {
	return Font.class;
    }

    static Font _fromString(String s) {
	int idx = s.indexOf(':');
	if (idx < 0) return null;
	String family = s.substring(0, idx);
	int lidx = s.lastIndexOf(':');
	if (lidx < 0 || lidx == idx) return null;
	String style = s.substring(idx+1, lidx);
	String size = s.substring(lidx+1, s.length());
	return new Font(family, getStyle(style), Utils.parseInt(size));
    }

    Object fromString(String s) {
	return _fromString(s);
    }

    static int getStyle(String style) {
	if (style.equalsIgnoreCase("ITALIC"))
	    return Font.ITALIC;

	if (style.equalsIgnoreCase("BOLD"))
	    return Font.BOLD;

	if (style.equalsIgnoreCase("PLAIN"))
	    return Font.PLAIN;

	if (style.equalsIgnoreCase("ITALIC+BOLD") ||
	    style.equalsIgnoreCase("BOLD+ITALIC"))
	    return Font.ITALIC | Font.BOLD;

	return Font.PLAIN;
    }

    static String getStyle(Font font) {
	if (font.isBold() && font.isItalic()) return "BOLD+ITALIC";
	if (font.isPlain()) return "PLAIN";
	if (font.isBold()) return "BOLD";
	if (font.isItalic()) return "ITALIC";
	return "PLAIN";
    }

    static String _toString(Object value) {
	Font font = (Font)value;
	return font.getFamily() + ":" + getStyle(font) + ":" +
	    ((new Integer(font.getSize())).toString());
    }

    String toString(Object value) {
	return _toString(value);
    }
}

