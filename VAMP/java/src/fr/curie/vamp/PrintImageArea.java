
/*
 *
 * PrintImageArea.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;

class PrintImageArea extends PrintArea {

    String img_url;
    Dimension img_sz;

    PrintImageArea(String name, Rectangle2D.Double area, String img_url) {
	super(name, area, Color.WHITE);

	this.img_url = img_url;
	this.img_sz = null;
    }

    void setImageURL(String img_url) {
	this.img_url = img_url;
    }

    String getImageURL() {
	return img_url;
    }

    void setImageSize(Dimension img_sz) {
	this.img_sz = img_sz;
    }

    Dimension getImageSize() {
	return img_sz;
    }

    void adjust() {
	if (img_sz == null) return;
	setSize(img_sz.width, img_sz.height);
    }

    public Object clone() {
	PrintImageArea a = new PrintImageArea(name,
					      (Rectangle2D.Double)area.clone(),
					      null);
	a.img_url = img_url;
	a.img_sz = img_sz;
	return a;
    }
    
}


