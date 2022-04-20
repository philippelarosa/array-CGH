
/*
 *
 * MPanel.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

class MPanel extends JPanel {

    static boolean DEBUG = false;
    protected Margins rmargins; // R (Real)
    protected Point2D.Double rorig;     // R (Real)
    protected Scale canon_scale; // Unity : R/V (Real/Virtual)
    protected Scale scale;      // Unity : R/V (Real/Virtual)
    protected Scale bfScale;    // Unity : R/V (Real/Virtual)
    protected Dimension2DDouble vSize; // V (Virtual)
    protected boolean fitInPage = false;
    protected Point2D.Double vcenter;   // V (Virtual)
    protected String name;
    boolean scaleXEnable;
    private boolean init = false;
    protected boolean rotated;
    protected boolean autoAdapt;
    private boolean _modified = false;

    public void display_info() {
	System.out.println("rorig: " + rorig);
	System.out.println("scale: " + scale);
	System.out.println("bfScale: " + bfScale);
	System.out.println("canonScale: " + canon_scale);
	System.out.println("vSize: " + vSize);
	System.out.println("modified: " + _modified);
    }

    protected MPanel(String name, boolean scaleXEnable, boolean rotated,
		     boolean autoAdapt, Margins rmargins) {
	this.name = name;
	this.scaleXEnable = scaleXEnable;
	this.rorig = new Point2D.Double(0, 0);
	this.rmargins = rmargins;
	this.canon_scale = new Scale();
	this.scale = new Scale();
	this.bfScale = new Scale();
	this.vSize = new Dimension2DDouble();
	this.vcenter = null;
	this.rotated = rotated;
	this.autoAdapt = autoAdapt;
    }

    public double minOrigX() {
	return computeOrigX(1.);
    }

    public double maxOrigX() {
	return computeOrigX(0.);
    }

    public double minOrigY() {
	return computeOrigY(1.);
    }

    public double maxOrigY() {
	return computeOrigY(0.);
    }

    private double computeOrigX(double percent) {
	Dimension rSize = getTSize();
	double rox = -(getRW(vSize.width) +
		       rmargins.getMarginWidth() -
		       rSize.width) * percent +
	    rmargins.getMarginWest();

	/*
	if ((new Double(rox)).isNaN())
	    System.out.println("oups j'ai failli faire NaN: " + rox);
	else if ((new Double(rox)).isInfinite())
	    System.out.println("oups j'ai failli faire Infinity: " + rox);
	else
	    System.out.println("rox OK: " + rox);
	*/

	if (percent == 0)
	    return rmargins.getMarginWest();
	return -(getRW(vSize.width) +
		 rmargins.getMarginWidth() -
		 rSize.width) * percent +
	    rmargins.getMarginWest();
    }

    public Point2D.Double getOrig() {
	return rorig;
    }

    public void setOrig(Point2D.Double rorig) {
	this.rorig.x = rorig.x;
	this.rorig.y = rorig.y;
	setUpdate(false);
    }

    public void setOrigX(double percent) {
	double rox = computeOrigX(percent);
	if (rorig.x != rox) {
	    rorig.x = rox;
	    //System.out.println("setOrigX(" + percent + ", " + rorig.x + ", check " + getPercentX() + ")");
	    setUpdate(false);
	    repaint();
	}
    }

    private double computeOrigY(double percent) {
	Dimension rSize = getTSize();
	if (percent == 0)
	    return rmargins.getMarginNorth();
	return -(getRH(vSize.height) +
		 rmargins.getMarginHeight() -
		 rSize.height) * percent +
	    rmargins.getMarginNorth();
    }

    public void setOrigY(double percent) {
	double roy = computeOrigY(percent);
	if (rorig.y != roy) {
	    rorig.y = roy;
	    //System.out.println("setOrigY(" + percent + ", " + rorig.y + ", check " + getPercentY() + ")");
	    setUpdate(false);
	    repaint();
	}
    }

    public double getPercentX() {
	Dimension rSize = getTSize();
	return (double)(rmargins.getMarginWest() - rorig.x) /
	    (getRW(vSize.width) +
	     rmargins.getMarginWidth() -
	     rSize.width);
    }

    public double getPercentX(double vx) {
	Dimension rSize = getTSize();
	return (double)(vx*scale.getScaleX() + rmargins.getMarginWest()) /
	    (getRW(vSize.width) +
	     rmargins.getMarginWidth() -
	     rSize.width);
    }

    public double getPercentY() {
	Dimension rSize = getTSize();
	return (double)(rmargins.getMarginNorth() - rorig.y) /
	    (getRH(vSize.height) +
	     rmargins.getMarginHeight() -
	     rSize.height);
    }

    public double getVX(double rx) {
	return ((double)(rx-rorig.x)/scale.getScaleX());
    }

    public double getVY(double ry) {
	return ((double)(ry-rorig.y)/scale.getScaleY());
    }

    public double getRX(double vx, double vy) {
	if (rotated)
	    return getRY(vy);
	return rorig.x + scale.getScaleX() * vx;
    }

    public double getRY(double vx, double vy) {
	if (rotated) {
	    Dimension size = getSize();
	    return getRX(vx);
	}
	return rorig.y + scale.getScaleY() * vy;
    }

    public double getRX(double vx) {
	return rorig.x + scale.getScaleX() * vx;
    }

    public double getRY(double vy) {
	return rorig.y + scale.getScaleY() * vy;
    }

    public boolean isVX_Visible(double vx) {
	int rx = (int)getRX(vx);
	if (rx < 0) return false;
	Dimension size = getSize();
	if (rx > size.width) return false;
	return true;
    }

    public boolean isVY_Visible(double vy) {
	int ry = (int)getRY(vy);
	if (ry < 0) return false;
	Dimension size = getSize();
	if (ry > size.height) return false;
	return true;
    }

    public boolean isVXY_Visible(double vx, double vy) {
	return isVX_Visible(vx) && isVY_Visible(vy);
    }

    /*
    public boolean isVR_Visible(Rectangle2D.Double vRect) {
	Dimension size = getSize();
	Rectangle2D.Double v1 = new Rectangle2D.Double
	    (0, 0, size.width, size.height);
	Rectangle2D.Double v2 = new Rectangle2D.Double
	    (getRX(vRect.x), getRY(vRect.y - vRect.height),
	     getRW(vRect.width),
	     getRH(vRect.height));
	return v1.intersects(v2);
    }
    */

    public boolean isVR_Visible(Rectangle2D.Double vRect) {
	return isRR_Visible(getRRect(vRect));
    }

    public boolean isRR_Visible(Rectangle2D.Double rRect) {
	Dimension size = getSize();
	Rectangle2D.Double r1 = new Rectangle2D.Double
	    (0, 0, size.width, size.height);
	Rectangle2D.Double r2 = new Rectangle2D.Double
	    (rRect.x, rRect.y - rRect.height,
	     rRect.width,
	     rRect.height);
	return r1.intersects(r2);
    }

    public boolean isVR_Visible_Y(Rectangle2D.Double vRect) {
	return isRR_Visible_Y(getRRect(vRect));
    }

    public boolean isRR_Visible_Y(Rectangle2D.Double rRect) {
	Dimension size = getSize();
	Rectangle2D.Double r1 = new Rectangle2D.Double
	    (0, 0, size.width, size.height);
	Rectangle2D.Double r2 = new Rectangle2D.Double
	    (0, rRect.y - rRect.height,
	     size.width,
	     rRect.height);
	return r1.intersects(r2);
    }

    private void recomputeCanonScale(Scale bfScaleO, Scale bfScale) {
	canon_scale.setScaleX(canon_scale.getScaleX() *
			      (bfScaleO.getScaleX() /
			       bfScale.getScaleX()));
	canon_scale.setScaleY(canon_scale.getScaleY() *
			      (bfScaleO.getScaleY() /
			       bfScale.getScaleY()));
	/*
	System.out.println("recomputeCanonScale: " +
			   bfScaleO + ", " +
			   bfScale + ", " +
			   canon_scale);
	*/
    }

    void computeBFScale() {
	Dimension rSize = getTSize();
	if (rSize.width == 0 && rSize.height == 0) return;

	Scale bfScaleN = new Scale();
	if (vSize.width == 0 || !scaleXEnable)
	    bfScaleN.setScaleX(1.);
	else
	    bfScaleN.setScaleX((rSize.width -
				 rmargins.getMarginWidth()) /
				vSize.width);
	if (vSize.height == 0)
	    bfScaleN.setScaleY(1.);
	else
	    bfScaleN.setScaleY((double)(rSize.height -
					 rmargins.getMarginHeight()) /
				vSize.height);
	if (!bfScale.equalsTo(bfScaleN)) {
	    bfScale.setScaleX(bfScaleN.getScaleX());
	    bfScale.setScaleY(bfScaleN.getScaleY());
	}
	init = true;
    }

    public void setVirtualSize(double width, double height) {
	//System.out.println(name + ": setVirtualSize(" + width + ", " + height + ")");
	vSize.width = width;
	vSize.height = height;
	computeBFScale();
	repaint();
    }

    public Dimension2DDouble getVirtualSize() {return vSize;}

    public void updateSize(boolean mustCompute) {
	if (autoAdapt) {
	    computeBFScale();
	    if (fitInPage)
		setScale(1, 1);
	    else
		setScale(canon_scale.getScaleX(), canon_scale.getScaleY());
	}
	else {
	    Scale bfScaleO;
	    if (!init) {
		computeBFScale();
		init = true;
		bfScaleO = new Scale(bfScale);
	    }
	    else {
		bfScaleO = new Scale(bfScale);
		computeBFScale();
	    }

	    if (fitInPage) {
		setScale(1, 1);
		return;
	    }

	    if (bfScaleO.getScaleX() != 0 && !bfScaleO.equalsTo(bfScale))
		recomputeCanonScale(bfScaleO, bfScale);

	    setScale(canon_scale.getScaleX(), canon_scale.getScaleY());
	}
    }

    public void setFitInPage(boolean value) {
	if (value) {
	    computeBFScale();
	    setScale(1, 1);
	}
	if (fitInPage != value) {
	    fitInPage = value;
	    setUpdate(true);
	}
    }

    public Point2D.Double centerPoint() {
	if (vcenter != null)
	    return vcenter;

	Dimension rSize = getTSize();
	return new Point2D.Double(getVX((double)rSize.width/2),
				  getVY((double)rSize.height/2));
    }

    public void center(Point2D.Double vp, boolean setModified) {
	// EV: 23/01/04 disconnected || !scaleXEnable !
	// if (vp == null || !scaleXEnable)
	if (vp == null) return;

	Dimension rSize = getTSize();
	int rx = (int)getRX(vp.x) - rSize.width/2;
	int ry = (int)getRY(vp.y) - rSize.height/2;
	
	rorig.x -= rx;
	rorig.y -= ry;

	if (rorig.x < minOrigX())
	    rorig.x = minOrigX();
	if (rorig.x > maxOrigX())
	    rorig.x = maxOrigX();
	
	if (rorig.y < minOrigY())
	    rorig.y = minOrigY();
	if (rorig.y > maxOrigY())
	    rorig.y = maxOrigY();

	/*
	System.out.println(name + ": center(" + vp + ") -> " +
			   "vSize.height: " + vSize.height + ", " +
			   "minOrigY: " + minOrigY() +
			   ", maxOrigY: " + maxOrigY() + ", rorig.y: " +
			   rorig.y);
	*/

	if (setModified)
	    setUpdate(false);

	adaptScroll();
    }

    public void setScaleX(double scaleX) {
	//System.out.println(name + ": setScaleX(" + scaleX + ")");
	if (!scaleXEnable) return;
	canon_scale.setScaleX(scaleX);
	scaleX *= bfScale.getScaleX();

	if (scale.getScaleX() != scaleX) {
	    Point2D.Double vp = centerPoint();
	    scale.setScaleX(scaleX);
	    center(vp, false);
	    setUpdate(true);
	    repaint();
	}
    }

    public void setScaleY(double scaleY) {
	/*
	System.out.println(name + ": setScaleY(" + scaleY + ")");
	System.out.println(name + ": bfScaleY (" + bfScale.getScaleY() + ")");
	*/
	canon_scale.setScaleY(scaleY);
	scaleY *= bfScale.getScaleY();

	if (scale.getScaleY() != scaleY) {
	    Point2D.Double vp = centerPoint();
	    scale.setScaleY(scaleY);
	    center(vp, false);
	    setUpdate(true);
	    repaint();
	}
    }

    public void setScale(double scaleX, double scaleY) {
	//System.out.println(name + ": setScale(" + scaleY + ")");
	scaleX *= bfScale.getScaleX();
	scaleY *= bfScale.getScaleY();

	if (!scaleXEnable)
	    scaleX = 1.;

	if (scale.getScaleX() != scaleX || scale.getScaleY() != scaleY) {
	    Point2D.Double vp = centerPoint();
	    scale.setScaleX(scaleX);
	    scale.setScaleY(scaleY);
	    center(vp, false);
	    setUpdate(true);
	    repaint();
	}
    }

    public void setVCenter(Point2D.Double vcenter) {
	//System.out.println(name + ": setVCenter(" + vcenter + ")");
	this.vcenter = vcenter;
	center(centerPoint(), false);
	setUpdate(false);
	repaint();
    }

    public Point2D.Double getVCenter() {
	return vcenter;
    }

    public Margins getRMargins() {
	return rmargins;
    }

    public void setRMargins(Margins rmargins) {
	this.rmargins = rmargins;
	setUpdate(false);
	repaint();
    }

    public Scale getScale() {
	return scale;
    }

    public Scale getBFScale() {
	return bfScale;
    }

    protected void setCanonScale(Scale canon_scale) {
	this.canon_scale.setScaleX(canon_scale.getScaleX());
	this.canon_scale.setScaleY(canon_scale.getScaleY());
    }

    public Scale getCanonScale() {
	return canon_scale;
    }

    public double getRW(double vw) {
	return vw * scale.getScaleX();
    }

    public double getRH(double vh) {
	return vh * scale.getScaleY();
    }

    public double getRW(double vw, double vh) {
	if (rotated)
	    return getRH(vh);
	return getRW(vw);
    }

    public double getRH(double vw, double vh) {
	if (rotated)
	    return getRW(vw);
	return getRH(vh);
    }

    public double getVW(double rw) {
	return rw / scale.getScaleX();
    }

    public double getVH(double rh) {
	return rh / scale.getScaleY();
    }

    public Rectangle2D.Double getRRect(Rectangle2D.Double vRect) {
	Rectangle2D.Double rRect = new Rectangle2D.Double();
	rRect.x = getRX(vRect.x, vRect.y - vRect.height);
	rRect.y = getRY(vRect.x + vRect.width, vRect.y);
	rRect.width = getRW(vRect.width, vRect.height);
	rRect.height = getRH(vRect.width, vRect.height);
	return rRect;
    }

    public int getTRX(int rx, int ry) {
	if (rotated)
	    return ry;

	return rx;
    }

    public int getTRY(int rx, int ry) {
	if (rotated)
	    return rx;

	return ry;
    }

    public boolean isScaleXEnable() {
	return scaleXEnable;
    }

    public int getAvailableRWidth() {
	return (int)(getRW(vSize.width)) + rmargins.getMarginWidth();
    }

    public int getAvailableRHeight() {
	return (int)(getRH(vSize.height)) + rmargins.getMarginHeight();
    }

    public int getVWidth() {
	Dimension rSize = getTSize();
	return (int)getVW(rSize.width);
    }

    public int getVHeight() {
	Dimension rSize = getTSize();
	return (int)getVH(rSize.height);
    }

    public boolean isFitInPage() {return fitInPage;}

    public Dimension getTSize() {
	Dimension size = getSize();
	if (rotated)
	    return new Dimension(size.height, size.width);
	return size;
    }

    void setRotated(boolean rotated) {
	this.rotated = rotated;
    }

    boolean isRotated() {
	return rotated;
    }

    // could be overloaded by inherited classes
    void update() {
    }

    void setModified(boolean modified) {
	this._modified = modified;
    }

    void setUpdate(boolean update) {
	setModified(true);
	if (update)
	    update();
    }

    boolean isModified() {
	return _modified;
    }


    Rectangle2D.Double makeVisibleRect(Rectangle2D rect) {
	Dimension size = getSize();
	Rectangle2D.Double bounds =
	    new Rectangle2D.Double(0, 0, size.width, size.height);
	Rectangle2D.Double dest = new Rectangle2D.Double();
	Rectangle2D.intersect(bounds, rect, dest);
	return dest;
    }

    void adaptScroll() { }
}
