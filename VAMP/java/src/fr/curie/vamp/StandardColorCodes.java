
/*
 *
 * StandardColorCodes.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

public class StandardColorCodes extends ColorCodes {

    private Color maxColors[];
    private Color minColors[];
    private double min, normal_min, normal_max, max, amplicon;
    private double incr_min, incr_max;

    private boolean continuous;
    private int minRGB, normalRGB, maxRGB, ampliconRGB;
    private Color minColor, normalColor, maxColor, ampliconColor;
    private boolean ok;
    private static final double MIN_S = 0.10;
    private static final String invalidMsg = "Error: invalid color code parameters";
    boolean isLog;

    static boolean check(GlobalContext globalContext, double min,
			 double normal_min, double normal_max,
			 double max, double amplicon) {
	boolean error = false;
	String msg = invalidMsg + ":";
	if (amplicon < max) {
	    msg += "\nAmplicon must be greater or equal than Max";
	    error = true;
	}

	if (min > normal_min) {
	    msg += "\nMin must be less or equal than Normal Min";
	    error = true;
	}

	if (normal_min > normal_max) {
	    msg += "\nNormal Min must be less or equal than Normal Max";
	    error = true;
	}

	if (normal_max > max) {
	    msg += "\nNormal Max must be less or equal than Max";
	    error = true;
	}

	if (error)
	    InfoDialog.pop(globalContext, msg);

	return !error;
    }
		       
    public boolean set(double min,
		       double normal_min, double normal_max,
		       double max, double amplicon,
		       int minRGB, int normalRGB, int maxRGB, int ampliconRGB,
		       boolean continuous) {
	if (!check(globalContext, min, normal_min, normal_max, max, amplicon)) {
	    ok = false;
	    return false;
	}

	ok = true;
	this.continuous = continuous;
	this.min = min;
	this.normal_min = normal_min;
	this.normal_max = normal_max;
	this.max = max;
	this.amplicon = amplicon;

	this.minRGB = minRGB;
	this.normalRGB = normalRGB;
	this.maxRGB = maxRGB;
	this.ampliconRGB = ampliconRGB;

	minColor = new Color(minRGB);
	normalColor = new Color(normalRGB);
	maxColor = new Color(maxRGB);
	ampliconColor = new Color(ampliconRGB);

	return true;
    }

    public StandardColorCodes(GlobalContext globalContext,
			      boolean isLog,
			      String codeName,
			      String name, double min,
			      double normal_min, double normal_max,
			      double max, double amplicon, int nbColors,
			      Color min_fg, Color normal_fg,
			      Color max_fg, Color amplicon_fg,
			      boolean cont_mode) {
	super(globalContext, codeName, name);
	this.isLog = isLog;
	if (!set(min, normal_min, normal_max, max, amplicon,
		 min_fg.getRGB(), normal_fg.getRGB(), max_fg.getRGB(),
		 amplicon_fg.getRGB(),
		 cont_mode)) return;

	register();
	int nbColors2 = nbColors/2;
	this.incr_max = (max - normal_max)/nbColors2;
	this.incr_min = (normal_min - min)/nbColors2;

	recompute();
    }

    public void recompute() {
	if (continuous)
	    computeContinuousColors();
	else
	    computeDiscontinuousColors();
    }

    private static float [] getHSB(int rgb) {
	return Color.RGBtoHSB((rgb & 0xff0000) >> 16,
			      (rgb & 0x00ff00) >> 8,
			      (rgb & 0x0000ff), null);
    }

    private static float [] getHSB(Color color) {
	return getHSB(color.getRGB());
    }

    private static void printHSB(Color color) {
	float[] hsb = getHSB(color);
	System.out.println("RGB " +
			   Integer.toHexString(color.getRGB() & 0xffffff) +
			   " = " + hsb[0] + " : " + hsb[1] + " : " + hsb[2]);
    }

    /*
    private void computeColors_old() {
	int cnt = (int)((max-normal_max)/incr_max);
	maxColors = new Color[cnt];
	int base = maxRGB;
	for (int n = 0; n < cnt; n++) {
	    int rgb = base + (((0xff*n)/cnt) << 8);
	    maxColors[n] = new Color(rgb);
	}

	cnt = (int)((normal_min-min)/incr_min);
	minColors = new Color[cnt];
	base = minRGB;
	for (int n = 0; n < cnt; n++) {
	    int rgb = base + (((0xff*n)/cnt) << 16);
	    minColors[n] = new Color(rgb);
	}

    }
    */

    void computeContinousGradients(double min, double normal_min,
				   double normal_max, double max,
				   int minRGB, int normalRGB, int maxRGB)
    {
    }

    private void computeContinuousColors() {
	float maxHSB[] = getHSB(maxRGB);
	float minHSB[] = getHSB(minRGB);
	float normalHSB[] = getHSB(normalRGB);

	int cnt = (int)((max-normal_max)/incr_max);
	maxColors = new Color[cnt];
	double H_offset = (normalHSB[0] - maxHSB[0])/cnt;
	double S_offset = (normalHSB[1] - maxHSB[1])/cnt;
	double B_offset = (normalHSB[2] - maxHSB[2])/cnt;
	float hsb[] = new float[3];
	hsb[0] = maxHSB[0];
	hsb[1] = maxHSB[1];
	hsb[2] = maxHSB[2];
	for (int n = 0; n < cnt; n++) {
	    maxColors[n] = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	    hsb[0] += H_offset;
	    hsb[1] += S_offset;
	    hsb[2] += B_offset;
	}

	cnt = (int)((normal_min-min)/incr_min);
	minColors = new Color[cnt];
	H_offset = (normalHSB[0] - minHSB[0])/cnt;
	S_offset = (normalHSB[1] - minHSB[1])/cnt;
	B_offset = (normalHSB[2] - minHSB[2])/cnt;
	hsb[0] = minHSB[0];
	hsb[1] = minHSB[1];
	hsb[2] = minHSB[2];

	for (int n = 0; n < cnt; n++) {
	    minColors[n] = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	    hsb[0] += H_offset;
	    hsb[1] += S_offset;
	    hsb[2] += B_offset;
	}

	//display();
    }

    private void computeDiscontinuousColors() {
	float maxHSB[] = getHSB(maxRGB);
	float minHSB[] = getHSB(minRGB);

	int cnt = (int)((max-normal_max)/incr_max);
	maxColors = new Color[cnt];
	double S_offset = (MIN_S - maxHSB[1])/cnt;
	float hsb[] = new float[3];
	hsb[0] = maxHSB[0];
	hsb[1] = maxHSB[1];
	hsb[2] = maxHSB[2];
	for (int n = 0; n < cnt; n++) {
	    maxColors[n] = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	    hsb[1] += S_offset;
	}

	cnt = (int)((normal_min-min)/incr_min);
	minColors = new Color[cnt];
	S_offset = (MIN_S - minHSB[1])/cnt;

	hsb[0] = minHSB[0];
	hsb[1] = minHSB[1];
	hsb[2] = minHSB[2];

	for (int n = 0; n < cnt; n++) {
	    minColors[n] = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	    hsb[1] += S_offset;
	}

	//display();
    }

    private String toHex(int n) {
	return "0x" + Integer.toHexString(n&0xffffff);
    }

    public void display() {
	System.out.println("ColorCodes " + getCodeName());
	System.out.println("min : " + min);
	System.out.println("normal_min : " + normal_min);
	System.out.println("incr_min : " + incr_min);
	System.out.println("normal_max : " + normal_max);
	System.out.println("incr_max : " + incr_max);
	System.out.println("max : " + max);
	for (int n = 0; n < maxColors.length; n++) {
	    float hsb[] = getHSB(maxColors[n]);
	    System.out.println("\tmax<" + n + "> -> " + toHex(maxColors[n].getRGB()) + " HSB=" + hsb[0] + ":" + hsb[1] + ":" + hsb[2]);
	}

	for (int n = 0; n < minColors.length; n++) {
	    float hsb[] = getHSB(minColors[n]);
	    System.out.println("\tmin<" + n + "> -> " + toHex(minColors[n].getRGB()) + " HSB=" + hsb[0] + ":" + hsb[1] + ":" + hsb[2]);
	}
    }

    public Color getColor(double value) {
	if (!ok) return Color.BLACK;
	if (value >= amplicon)
	    return ampliconColor;

	if (value >= normal_min && value <= normal_max)
	    return normalColor;

	if (value > normal_max) {
	    if (maxColors.length == 0) return maxColor;
	    int n = (int)((max-value)/incr_max);
	    if (n < 0) n = 0;
	    else if (n >= maxColors.length) n = maxColors.length-1;
	    return maxColors[n];
	}

	if (minColors.length == 0) return minColor;
	int n = (int)((value-min)/incr_min);
	if (n < 0) n = 0;
	else if (n >= minColors.length) n = minColors.length-1;
	return minColors[n];
    }

    public double getMin() {return min;}
    public double getNormalMin() {return normal_min;}
    public double getNormalMax() {return normal_max;}
    public double getMax() {return max;}
    public double getAmplicon() {return amplicon;}

    public boolean isContinuous() {return continuous;}

    public Color getMinColor() {return minColor;}
    public Color getMaxColor() {return maxColor;}
    public Color getNormalColor() {return normalColor;}
    public Color getAmpliconColor() {return ampliconColor;}

    public int getMinRGB() {return minRGB;}
    public int getMaxRGB() {return maxRGB;}
    public int getNormalRGB() {return normalRGB;}
    public int getAmpliconRGB() {return ampliconRGB;}

    public boolean isAmplicon(double ratio) {
	return ratio > amplicon;
    }

    public boolean isGained(double ratio) {
	return ratio > max && ratio <= amplicon;
    }

    public boolean isGainedOrAmplicon(double ratio) {
	return ratio > max;
    }

    public boolean isNormal(double ratio) {
	return ratio >= normal_min && ratio <= normal_max;
    }

    public boolean isLost(double ratio) {
	return ratio < min;
    }

    public boolean isLog() {return isLog;}

    public void setMin(double min) {this.min = min;}
    public void setNormalMin(double normal_min) {this.normal_min = normal_min;}
    public void setNormalMax(double normal_max) {this.normal_max = normal_max;}
    public void setMax(double max) {this.max = max;}

    public void setContinuous(boolean continuous) {
	this.continuous = continuous;
    }

    public void setMinRGB(int minRGB) {this.minRGB = minRGB;}
    public void setMaxRGB(int maxRGB) {this.maxRGB = maxRGB;}
    public void setNormalRGB(int normalRGB) {this.normalRGB = normalRGB;}

    public boolean isOK() {return ok;}
}
