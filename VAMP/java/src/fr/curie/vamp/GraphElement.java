/*
 *
 * GraphElement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import fr.curie.vamp.data.Profile;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

public abstract class GraphElement extends PropertyElement
    implements Cloneable, Pasteable {

    private static long DSID = 1;
    private String url;
    private String srcUrl;
    private String srcType;
    private BufferedImage offScreen;
    private boolean offScreenValid = false;
    private boolean visible = false;
    private AxisDisplayer axisDisplayer;
    private GraphElementDisplayer graphElementDisplayer;

    private Rectangle2D.Double rBounds, vBounds, paintVBounds;
    private boolean selected;
    private boolean pinned_up;
    private String order = "";
    private Property objKeyProp;

    private double ycoef = 1, yoffset = 0;
    private boolean autoY = false;
    private boolean autoY2 = false;

    private long dsid;

    public GraphElement() {
	vBounds = new Rectangle2D.Double();
	rBounds = new Rectangle2D.Double();
	setPropertyValue(VAMPProperties.CommentProp, "");
	setPropertyValue(VAMPProperties.TagProp, "");
	selected = false;
	pinned_up = false;
	this.dsid = DSID++;
    }

    void setYInfo(double ycoef, double yoffset) {
	this.ycoef = ycoef;
	this.yoffset = yoffset;
    }

    double getYCoef() {
	return ycoef;
    }

    double getYOffset() {
	return yoffset;
    }

    double yTransform(double y) {
	if (ycoef == 0) return y;
	return y / ycoef + yoffset;
    }

    double yTransform_1(double y) {
	if (ycoef == 0) return y;
	return (y - yoffset) * ycoef;
    }

    boolean isAutoY() {
	return autoY;
    }

    void setAutoY(boolean autoY) {
	this.autoY = autoY;
	if (autoY)
	    autoY2 = false;
    }

    boolean isAutoY2() {
	return autoY2;
    }

    void setAutoY2(boolean autoY2) {
	this.autoY2 = autoY2;
	if (autoY2)
	    autoY = false;
    }

    void setVBounds(double x, double y, double width, double height) {
	vBounds.x = x;
	vBounds.y = y;
	vBounds.width = width;
	vBounds.height = height;
    }

    void setPaintVBounds(double x, double y, double width, double height) {
	if (paintVBounds == null)
	    paintVBounds = new Rectangle2D.Double();
	paintVBounds.x = x;
	paintVBounds.y = y;
	paintVBounds.width = width;
	paintVBounds.height = height;
    }

    void setRBounds(Rectangle2D.Double rect) {
	if (rBounds.width != rect.width ||
	    rBounds.height != rect.height)
	    setOffScreenValid(false);

	rBounds.x = rect.x;
	rBounds.y = rect.y;
	rBounds.width = rect.width;
	rBounds.height = rect.height;

	//System.out.println("setRBounds: " + rBounds);
	//(new Exception()).printStackTrace();
    }

    Rectangle2D.Double getRBounds() {return rBounds;}

    public void resetPaintVBounds() {
	paintVBounds = null;
    }

    public Rectangle2D.Double getVBounds() {return vBounds;}

    public Rectangle2D.Double getPaintVBounds() {
	return paintVBounds != null ? paintVBounds : vBounds;
    }

    public void setSelected(boolean selected, Object container) {
	this.selected = selected;
	assert container != null;

	if (selected) {
	    ((GraphCanvas)container).addSelectedGraphElement(this);
	}
	else {
	    ((GraphCanvas)container).removeSelectedGraphElement(this);
	}
    }

    public boolean isSelected() {return selected;}

    public void setPinnedUp(boolean pinned_up) {
	this.pinned_up = pinned_up;
    }

    public boolean isFullImported() {return true;}

    public boolean isPinnedUp() {return pinned_up;}

    protected void clone(GraphElement graphElement) {
	graphElement.cloneProperties(this);

	graphElement.vBounds = new Rectangle2D.Double();
	graphElement.paintVBounds = null;
	graphElement.rBounds = new Rectangle2D.Double();
	graphElement.selected = selected;
	graphElement.pinned_up = false;
	graphElement.offScreen = null;
	graphElement.offScreenValid = false;
	graphElement.order = order;
	graphElement.url = url;
	graphElement.srcUrl = srcUrl;
	graphElement.srcType = srcType;
	graphElement.dsid = DSID++;
	graphElement.objKeyProp = objKeyProp;

	graphElement.ycoef = ycoef;
	graphElement.yoffset = yoffset;
	graphElement.autoY = autoY;
	graphElement.autoY2 = autoY2;
    }

    public Object clone() /* throws CloneNotSupportedException */ {
	try {
	    GraphElement graphElement = (GraphElement)super.clone();
	    // 20/01/05
	    //graphElement.cloneProperties(getProperties());

	    clone(graphElement);
	    /*
	    graphElement.cloneProperties(this);

	    graphElement.vBounds = new Rectangle2D.Double();
	    graphElement.paintVBounds = null;
	    graphElement.rBounds = new Rectangle2D.Double();
	    graphElement.selected = selected;
	    graphElement.pinned_up = false;
	    graphElement.offScreen = null;
	    graphElement.offScreenValid = false;
	    graphElement.order = order;
	    graphElement.url = url;
	    graphElement.srcUrl = srcUrl;
	    graphElement.srcType = srcType;
	    graphElement.dsid = DSID++;
	    graphElement.objKeyProp = objKeyProp;

	    graphElement.ycoef = ycoef;
	    graphElement.yoffset = yoffset;
	    graphElement.autoY = autoY;
	    graphElement.autoY2 = autoY2;
	    */

	    return graphElement;
	}
	catch(CloneNotSupportedException e) {
	    return null;
	}
    }

    public long getDSID() {return dsid;}

    public Object clone_light() {
	return clone();
    }

    /*
      public String toString() {
      return "GraphElement[vBounds=" + vBounds + "]";
      }
    */

    public void prePaste() {
    }

    public void postClone() {
    }

    public boolean isPasteable(int action) {
	return isSelected();
    }

    public String getURL() {
	return url;
    }

    public void setURL(String url) {
	this.url = url;
    }

    public String getSourceURL() {
	return srcUrl != null ? srcUrl : "";
    }

    public void setSourceURL(String srcUrl) {
	this.srcUrl = srcUrl;
    }

    public String getSourceType() {
	return srcType;
    }

    public void setSourceType(String srcType) {
	this.srcType = srcType;
    }

    void cache() {
	if (srcUrl != null)
	    GraphElementCache.getInstance().put(srcUrl, this);
    }

    static Vector clone(Vector v) throws CloneNotSupportedException {
	Vector cv = new Vector();
	int size = v.size();
	for (int n = 0; n < size; n++) {
	    cv.add(((GraphElement)v.get(n)).clone());
	}

	return cv;
    }

    boolean isVisible() {
	return visible;
    }

    void setVisible(boolean visible) {
	this.visible = visible;
    }

    public DataSet asDataSet() {return null;}

    public Profile asProfile() {return null;}

    public String getOrder() {return order;}

    public void setOrder(int order) {
	this.order = Utils.toString(order);
	while (this.order.length() < 4)
	    this.order = "0" + this.order;
    }

    DendrogramGraphElement asDendrogramGraphElement() {return null;}

    public AxisDisplayer getAxisDisplayer() {return axisDisplayer;}

    public void setAxisDisplayer(AxisDisplayer axisDisplayer) {
	this.axisDisplayer = axisDisplayer;
    }

    public GraphElementDisplayer getGraphElementDisplayer() {return graphElementDisplayer;}

    public void setGraphElementDisplayer(GraphElementDisplayer graphElementDisplayer) {
	this.graphElementDisplayer = graphElementDisplayer;
    }

    boolean isOffScreenValid() {
	return offScreenValid;
    }

    void setOffScreenValid(boolean offScreenValid) {
	this.offScreenValid = offScreenValid;
	if (!offScreenValid) {
	    if (offScreen != null) {
		alloc_size -= offScreen.getWidth() *  offScreen.getHeight();
	    }
	    offScreen = null;
	    
	}
    }

    boolean offscreen_compute = false;

    boolean isOffScreenCompute() {
	return offscreen_compute;
    }

    void setOffScreenCompute(boolean offscreen_compute) {
	this.offscreen_compute = offscreen_compute;
    }

    // offscreen management
    static boolean SUPPORT_OFFSCREEN = true;
    static boolean FORCE_OFFSCREEN = false;
    //static final int MAX_DATA_LENGTH = 20000;
    //static final int MAX_DATA_LENGTH = 50000;
    static final int MAX_DATA_LENGTH = 200000000;
    static final int MAX_OFFSCREEN_SIZE = 20000000;
    static final int MAX_OFFSCREEN_ITEM_SIZE = 5000 * 150;
    static int alloc_size = 0;

    boolean isOffScreenable() {
	if (FORCE_OFFSCREEN)
	    return true;

	if (!SUPPORT_OFFSCREEN)
	    return false;

	if (asDataSet() != null &&
	    asDataSet().getData().length > MAX_DATA_LENGTH)
	    return false;

	//System.out.println("isOffScreenable " + alloc_size);

	if (rBounds.width * rBounds.height > MAX_OFFSCREEN_ITEM_SIZE)
	    return false;

	return alloc_size +
	    (rBounds.width * rBounds.height +
	     CommonDataSetDisplayer.OFFSCREEN_PADY) < MAX_OFFSCREEN_SIZE;
    }

    // could be overloaded
    BufferedImage getOffScreen(Component comp) {
	if (offScreen == null) {
	    offScreen =	new BufferedImage
		((rBounds.width >= 1 ?
		  (int)Math.round(rBounds.width) : 1),
		 (rBounds.height >= 1 ?
		  (int)Math.round(rBounds.height) +
		  CommonDataSetDisplayer.OFFSCREEN_PADY : 1),
		 BufferedImage.TYPE_INT_ARGB);
	    alloc_size += offScreen.getWidth() * offScreen.getHeight();
	}
	return offScreen;
    }

    void setObjKeyProp(Property objKeyProp) {
	this.objKeyProp = objKeyProp;
    }

    Property getObjKeyProp() {
	if (objKeyProp == null) {
	    String objKey = (String)getPropertyValue(VAMPProperties.ObjKeyProp);
	    if (objKey != null)
		objKeyProp = Property.getProperty(objKey);
	}

	if (objKeyProp == null)
	    objKeyProp = VAMPProperties.NmcProp;

	return objKeyProp;
    }

    double l_minX = Double.MAX_VALUE;
    double l_maxX = Double.MIN_VALUE;

    double l_minY = Double.MAX_VALUE;
    double l_maxY = Double.MIN_VALUE;

    void setMinMaxX(double x) {
	if (x == Double.MAX_VALUE || x == Double.MIN_VALUE)
	    return;

	if (x < l_minX) {
	    l_minX = x;
	}
	if (x > l_maxX) {
	    l_maxX = x;
	}
	//System.out.println("settingX -> " + x + " " + l_minX + " " + l_maxX);
    }

    void setMinMaxY(double y) {
	if (y == Double.MAX_VALUE || y == Double.MIN_VALUE)
	    return;

	if (y < l_minY) {
	    l_minY = y;
	}
	if (y > l_maxY) {
	    l_maxY = y;
	}
	//System.out.println("settingY -> " + y + " " + l_minY + " " + l_maxX);
    }

    double getLMinX() {return l_minX;}

    double getLMaxX() {return l_maxX;}

    double getLMinY() {return l_minY;}

    double getLMaxY() {return l_maxY;}

    public SmoothingLineEngine.SmoothingInfo getSmoothingInfo() {
	System.out.println("GraphElement.getSmoothingInfo()");
	return null;
    }

    public int getProbeCount() {return 0;}

    public RODataElementProxy getDataProxy(int n) throws Exception {return null;}

    public RODataElementProxy getDataProxy(int n, boolean load_props) throws Exception {return null;}

    public void release() {
    }

    public GraphElement dupSerializer() throws Exception {
	return this;
    }

    public static boolean hasProfile(Vector graphElements) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    if (((GraphElement)graphElements.get(n)).asProfile() != null) {
		return true;
	    }
	}

	return false;
    }
}
