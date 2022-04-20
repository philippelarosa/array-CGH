
/*
 *
 * ZoomTemplate.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class ZoomTemplate {

    private int Xslide_min;
    private int Yslide_min;
    private int Xslide_max;
    private int Yslide_max;
    
    ZoomTemplate(int Xslide_min, int Xslide_max,
		 int Yslide_min, int Yslide_max) {
	this.Xslide_min = Xslide_min;
	this.Xslide_max = Xslide_max;

	this.Yslide_min = Yslide_min;
	this.Yslide_max = Yslide_max;
    }

    int getXSlideMin() {return Xslide_min;}
    int getXSlideMax() {return Xslide_max;}

    int getYSlideMin() {return Yslide_min;}
    int getYSlideMax() {return Yslide_max;}
}    

