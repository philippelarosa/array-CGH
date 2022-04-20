
/*
 *
 * LOHColorCodes.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class LOHColorCodes extends StandardColorCodes {

    //static final double HOMOZ_VALUE = 1.;

    /*
    double min, loh, max;
    private Color min_fg, LOH_fg, max_fg;
    */

    public LOHColorCodes(GlobalContext globalContext,
			 String codeName, String name,
			 double min, double loh, double max,
			 int nbColors,
			 Color min_fg, Color LOH_fg,
			 Color max_fg) {
	super(globalContext, false, codeName, name, min, loh, loh, max,
	      Double.MAX_VALUE, nbColors, min_fg, LOH_fg, max_fg, Color.BLACK,
	      true);
	/*
	super(globalContext, codeName, name);
	register();
	this.min = min;
	this.loh = loh;
	this.max = max;
	this.min_fg = min_fg;
	this.max_fg = max_fg;
	this.LOH_fg = LOH_fg;
	*/
    }

    /*
    public Color getColor(double value) {
	// must take loh double value into account
	if (value >= min && value <= max)
	    return LOH_fg;

	if (value > max)
	    return max_fg;

	return min_fg;
    }
    */

    /*
    public double getMin() {return min;}
    public double getMax() {return max;}
    public double getLOH() {return loh;}

    public int getMinRGB() {return min_fg.getRGB();}
    public int getMaxRGB() {return max_fg.getRGB();}
    public int getLOHRGB() {return LOH_fg.getRGB();}
    */

    public double getLOH() {return getNormalMin();}
    public int getLOHRGB() {return getNormalRGB();}
}

