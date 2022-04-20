
/*
 *
 * Scale.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

public class Scale {

    private double scaleX;
    private double scaleY;

    Scale() {
	this(1., 1.);
    }

    Scale(double scaleX, double scaleY) {
	this.scaleX = scaleX;
	this.scaleY = scaleY;
    }

    Scale(Scale scale) {
	setScale(scale);
    }

    public double getScaleX() {return scaleX;}
    public double getScaleY() {return scaleY;}

    void setScaleX(double scaleX) {
	this.scaleX = scaleX;
    }

    void setScaleY(double scaleY) {
	this.scaleY = scaleY;
    }

    void setScale(Scale scale) {
	this.scaleX = scale.scaleX;
	this.scaleY = scale.scaleY;
    }

    public String toString() {
	return "Scale(" + scaleX + ", " + scaleY + ")";
    }

    boolean equalsTo(Scale scale) {
	return scaleX == scale.scaleX && scaleY == scale.scaleY;
    }
}
