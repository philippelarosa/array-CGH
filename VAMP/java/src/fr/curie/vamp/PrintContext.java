
/*
 *
 * PrintContext.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;

public class PrintContext {

    public static final int X_AXIS = 0x1;
    public static final int Y_AXIS = 0x2;
    public static final int Y_ANNOT = 0x4;

    private PrintableSet printableSet;
    private Rectangle2D.Double bounds;
    private PrintArea area;
    private double dx, dy, sx, sy;
    private int flags;
    private int which, start, end;

    PrintContext(PrintableSet printableSet,
		 double dx, double dy, double sx, double sy,
		 PrintArea area, int which, int start, int end, int flags) {
	this.printableSet = printableSet;
	this.dx = dx;
	this.dy = dy;
	this.sx = sx;
	this.sy = sy;
	this.area = area;
	this.bounds = area.getArea();
	this.flags = flags;
	this.which = which;
	this.start = start;
	this.end = end;

	//System.out.println("dx: " + dx + ", dy: " + dy + ", sx: " + sx + ", sy: " + sy);
    }

    // x axis
    public double getRX(double rx) {
	return dx + rx * sx;
    }

    public double getRW(double rw) {
	return rw * sx;
    }

    // y axis
    public double getRY(double ry) {
	return dy + ry * sy;
    }

    public double getRH(double rh) {
	return rh * sy;
    }

    Rectangle2D.Double getBounds() {return bounds;}
    PrintArea getArea() {return area;}

    PrintableSet getPrintableSet() {return printableSet;}

    int getFlags() {return flags;}

    int getWhich() {return which;}

    int getStart() {return start;}
    int getEnd() {return end;}

    boolean isFirst() {return which == start;}
    boolean isLast() {return which == end - 1;}

    void trace() {
	System.out.println(area.getName() + " dx: " + dx + ", dy: " + dy +
			   ", sx: " + sx + ", sy: " + sy);
    }
}
