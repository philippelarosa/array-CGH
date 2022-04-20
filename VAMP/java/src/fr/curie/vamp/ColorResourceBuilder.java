
/*
 *
 * ColorResourceBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;

class ColorResourceBuilder extends ResourceBuilder {

    private static HashMap map, rvmap;

    static {
	map = new HashMap();
	rvmap = new HashMap();

	put("BLACK", Color.BLACK);
	put("BLUE", Color.BLUE);
	put("CYAN", Color.CYAN);
	put("DARK_GRAY", Color.DARK_GRAY);
	put("GRAY", Color.GRAY);
	put("GREEN", Color.GREEN);
	put("LIGHT_GRAY", Color.LIGHT_GRAY);
	put("MAGENTA", Color.MAGENTA);
	put("ORANGE", Color.ORANGE);
	put("PINK", Color.PINK);
	put("RED", Color.RED);
	put("WHITE", Color.WHITE);
	put("YELLOW", Color.YELLOW);
    }

    ColorResourceBuilder() {
	super("Color");
    }

    static void put(String name, Color color) {
	map.put(name, color);
	rvmap.put(color, name);
    }

    Class getVClass() {
	return Color.class;
    }

    Object fromString(String s) {
	s = s.toUpperCase();
	Color c = getColor(s);
	if (c != null) return c;
	int rgb = Utils.parseInt(s, 16);
	return new Color(rgb);
    }

    static Color getColor(String s) {
	return (Color)map.get(s);
    }

    static String getString(Color c) {
	return (String)rvmap.get(c);
    }

    String toString(Object value) {
	Color c = (Color)value;
	String s = getString(c);
	if (s != null) return s;
	return Integer.toHexString(c.getRGB() & 0xffffff);
    }

    static String RGBString(int rgb) {
	String s = Integer.toHexString(rgb & 0xffffff);
	while (s.length() < 6)
	    s = "00" + s;
	return s;
    }
}

