
/*
 *
 * Margins.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

public class Margins {

    private int marginNorth, marginWest, marginSouth, marginEast;

    public Margins() {
	this(0, 0, 0, 0);
    }

    public Margins(int marginNorth, int marginWest, int marginSouth, int marginEast) {
	this.marginNorth = marginNorth;
	this.marginWest = marginWest;
	this.marginSouth = marginSouth;
	this.marginEast = marginEast;
    }

    public int getMarginNorth() {
	return marginNorth;
    }

    public int getMarginWest() {
	return marginWest;
    }

    public int getMarginSouth() {
	return marginSouth;
    }

    public int getMarginEast() {
	return marginEast;
    }

    public int getMarginWidth() {
	return marginWest + marginEast;
    }

    public int getMarginHeight() {
	return marginNorth + marginSouth;
    }

    public void setMarginNorth(int marginNorth) {
	this.marginNorth = marginNorth;
    }

    public void setMarginWest(int marginWest) {
	this.marginWest = marginWest;
    }

    public void setMarginEast(int marginEast) {
	this.marginEast = marginEast;
    }

    public void setMarginSouth(int marginSouth) {
	this.marginSouth = marginSouth;
    }

}
