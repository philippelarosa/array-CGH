
/*
 *
 * PrintArea.java
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
import java.util.*;

class PrintArea implements Cloneable {

    public static final String XSCALE = "Xscale";
    public static final String YSCALE = "Yscale";
    public static final String MINIMAP = "Minimap";
    public static final String GRAPHELEMENTS = "GraphElements";
    public static final String YANNOT = "Yannot";
    protected HashMap hints = new HashMap();

    protected String name;
    protected int panel_num;
    protected Rectangle2D.Double area;
    protected Color bgColor;
    protected Color bdColor = Color.BLACK;
    boolean hasBorder = false;
    protected boolean selected = false;
    PrintPageTemplate pageTemplate;

    PrintArea(String name, int panel_num, Rectangle2D.Double area) {
	this(name, panel_num, area, Color.WHITE);
    }

    PrintArea(String name, Rectangle2D.Double area) {
	this(name, 0, area);
    }

    PrintArea(String name, Rectangle2D.Double area, Color bgColor) {
	this(name, 0, area, bgColor);
    }

    PrintArea(String name, int panel_num,
	      Rectangle2D.Double area, Color bgColor) {
	this.name = name;
	this.panel_num = panel_num;
	this.area = area;
	this.bgColor = bgColor;
    }

    String getName() {return name;}
    int getPanelNum() {return panel_num;}
    Rectangle2D.Double getArea() {return area;}

    Color getBGColor() {return bgColor;}
    void setBGColor(Color bgColor) {this.bgColor = bgColor;}

    void hasBorder(boolean hasBorder) {
	this.hasBorder = hasBorder;
    }

    boolean hasBorder() {return hasBorder;}

    Color getBDColor() {return bdColor;}
    void setBDColor(Color bdColor) {this.bdColor = bdColor;}

    Dimension getSize() {
	return new Dimension((int)area.width, (int)area.height);
    }

    void move(int dx, int dy) {
	if (dx != 0 || dy != 0) {
	    area.x += dx;
	    area.y += dy;
	    pageTemplate.setModified();
	}
    }

    void setLocation(int x, int y) {
	int dx = x - (int)area.x;
	int dy = y - (int)area.y;
	move(dx, dy);
    }

    void resize(int dw, int dh) {
	if (dw != 0 || dh != 0) {
	    area.width += dw;
	    area.height += dh;
	    pageTemplate.setModified();
	}
    }

    void setSize(int w, int h) {
	int dw = w - (int)area.width;
	int dh = h - (int)area.height;
	resize(dw, dh);
    }

    void setSelected(boolean selected) {
	this.selected = selected;
    }

    boolean isSelected() {return selected;}

    public Object clone() {
	PrintArea a = new PrintArea(name, panel_num,
				    (Rectangle2D.Double)area.clone(), bgColor);
	a.bdColor = bdColor;
	a.hasBorder = hasBorder;
	return a;
    }

    public void setHint(Object key, Object value) {
	hints.put(key, value);
    }

    public Object getHint(Object key) {
	return hints.get(key);
    }

    public void removeHint(Object key) {
	hints.remove(key);
    }

    public JComponent[] getComponents() {return null;}

    void setPageTemplate(PrintPageTemplate pageTemplate) {
	this.pageTemplate = pageTemplate;
    }

}
