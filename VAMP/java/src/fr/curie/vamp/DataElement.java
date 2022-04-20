
/*
 *
 * DataElement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import fr.curie.vamp.properties.*;

public class DataElement extends PropertyElement implements Cloneable, RWDataElementProxy {

    private static final int NO_V = -1;

    static class Map {
	private static class Item {
	    GraphElement graphElement;
	    DataElementPosition pos;

	    Item(GraphElement graphElement, DataElementPosition pos) {
		this.graphElement = graphElement;
		this.pos = pos;
	    }
	}

	private Item [] items;
	
	Map() {
	    items = new Item[0];
	}

	DataElementPosition get(GraphElement graphElement) {
	    for (int n = 0; n < items.length; n++)
		if (items[n].graphElement == graphElement)
		    return items[n].pos;

	    return null;
	}
	
	void put(GraphElement graphElement, DataElementPosition pos) {
	    Item [] nitems = new Item[items.length+1];
	    for (int n = 0; n < items.length; n++)
		nitems[n] = items[n];
	    nitems[items.length] = new Item(graphElement, pos);
	    items = nitems;
	}

	void display() {
	    System.out.println("DataElement posmap size: " + items.length);
	    for (int n = 0; n < items.length; n++)
		System.out.println(items[n].graphElement.getID() + " " + items[n].graphElement);
	}
	
    }

    // NEW_PROP_MEM_POLICY
    static final byte NA_I = 126;
    static final byte NULL = 127;

    // type is a shared prop
    //private String type = null;

    private byte array[] = null;
    private byte flag[] = null;
    private byte is_na[] = null;
    private byte chr[] = null;

    private byte nbp = NULL, bkp = NULL, out = NULL, gnl = NULL, score = NULL;
    private float smt = Float.MAX_VALUE;
    private long position = Long.MAX_VALUE;
    private float ratio = Float.MAX_VALUE;

    private float merge_offset = Float.MAX_VALUE;

    private byte tag[] = null;
    private byte comment[] = null;

    // 

    private byte flags = 0;
    private int ind = -1;

    private static final int CENTERED = 0x1;
    private static final int PINNED_UP = 0x2;
    private static final int THRESHOLDED = 0x4;

    private static class DataElementPosition {
	// proper data element position
	long pos_x;
	double pos_y;
	long pos_sz;

	// virtual display coordinates
	long vx = NO_V;
	float vy = NO_V;
	long vsz = NO_V;

	// real display coordinates (pixel unit)
	float rx;
	float ry;
	float rsz;

	float rBounds_x, rBounds_y;
	float rBounds_r_x, rBounds_r_y, rBounds_r_width, rBounds_r_height;

	DataElementPosition() {
	    this.pos_x = 0;
	    this.pos_y = 0;
	    this.pos_sz = 0;
	    rx = ry = rsz = 0;

	    rBounds_x = rBounds_y = 0;
	    rBounds_r_x = rBounds_r_y = rBounds_r_width = rBounds_r_height = 0;
	}

	void setRBounds(double rx, double ry, double width, double height) {
	    rBounds_x = (float)rx - this.rx;
	    rBounds_y = (float)ry - this.ry;
	    rBounds_r_width = (float)width;
	    rBounds_r_height = (float)height;
	}
	    
	Rectangle2D.Double getRBounds() {
	    rBounds_r_x = this.rx + rBounds_x;
	    rBounds_r_y = this.ry + rBounds_y;
	    return new Rectangle2D.Double(rBounds_r_x, rBounds_r_y, 
					  rBounds_r_width, rBounds_r_height);
	}

	void clone(DataElementPosition dataPos) {
	    pos_x = dataPos.pos_x;
	    pos_y = dataPos.pos_y;
	    pos_sz = dataPos.pos_sz;
	    vx = dataPos.vx;
	    vy = dataPos.vy;
	    vsz = dataPos.vsz;
	    rx = dataPos.rx;
	    ry = dataPos.ry;
	    rsz = dataPos.rsz;
	    rBounds_x = dataPos.rBounds_x;
	    rBounds_y = dataPos.rBounds_y;
	    rBounds_r_x = dataPos.rBounds_r_x;
	    rBounds_r_y = dataPos.rBounds_r_y;
	    rBounds_r_width = dataPos.rBounds_r_width;
	    rBounds_r_height = dataPos.rBounds_r_height;
	}
    };

    Map dataPos_map = new Map();

    public DataElement() {
	super((TreeMap)null);

	setPropertyValue(VAMPProperties.CommentProp, "");
	setPropertyValue(VAMPProperties.TagProp, "");
    }

    public DataElement(LightPropertyElement elem) {
	convert(elem);
    }

    public void declare(GraphElement graphElem) {
	if (dataPos_map.get(graphElem) == null) {
	    dataPos_map.put(graphElem, new DataElementPosition());
	}
	/*
	if (dataPos_map.size() > 1) {
	    System.out.print("DUPLICATE: " + getID() + " ");
	    Iterator it = dataPos_map.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		System.out.print(entry.getKey() + " ");
		System.out.println("");
	    }
	}
	*/
    }

    static Property GpropList[] = new Property[] {
	//VAMPConstants.RatioProp,
	VAMPProperties.GNLProp,
	VAMPProperties.CopyNBProp,
	VAMPProperties.SignalProp,
	VAMPProperties.RSignalProp,
	VAMPProperties.ColorCodeProp
    };

    public void setGraphics(Graphics2D g, GraphElement graphElement) {
	VAMPProperties.RatioProp.setGraphics(g, getRatio(), this, graphElement);

	for (int n = 0; n < GpropList.length; n++) {
	    Object value = getPropertyValue(GpropList[n]);
	    if (value != null) {
		GpropList[n].setGraphics(g, value, this, graphElement);
	    }
	}
    }

    public void setRBounds(GraphElement graphElem, double rx, double ry, double width, double height) {
	getDataPos(graphElem).setRBounds(rx, ry, width, height);
    }

    Rectangle2D.Double getRBounds(GraphElement graphElem) {
	return getDataPos(graphElem).getRBounds();
    }

    protected Object clone() throws CloneNotSupportedException {
	DataElement data = (DataElement)super.clone();

	data.cloneProperties(this);

	data.dataPos_map = new Map();

	data.array = array;
	data.flag = flag;
	data.nbp = nbp;
	data.bkp = bkp;
	data.out = out;
	data.gnl = gnl;
	data.score = score;
	data.smt = smt;
	data.position = position;
	data.ratio = ratio;
	data.merge_offset = merge_offset;
	data.tag = tag;
	data.comment = comment;
	data.is_na = is_na;

	return data;
    }

    //    void copyPos(DataSet dataSet, DataElement odata, DataSet odataSet) {
    void copyPos(GraphElement dataSet, DataElement odata, GraphElement odataSet) {
	DataElementPosition odataPos = odata.getDataPos(odataSet);

	declare(dataSet);
	getDataPos(dataSet).clone(odataPos);
    }

    public void copyToPos(GraphElement dataSet, RODataElementProxy data, GraphElement odataSet) {
	if (!(data instanceof DataElement)) {
	    return;
	}

	((DataElement)data).copyPos(dataSet, this, odataSet);
    }


    boolean isCentered() {return (flags & CENTERED) != 0;}

    void setCentered(boolean centered) {
	if (centered)
	    this.flags |= CENTERED;
	else
	    this.flags &= ~CENTERED;
    }

    public boolean isPinnedUp() {return (flags & PINNED_UP) != 0;}

    public void setPinnedUp(boolean pinned_up) {
	if (pinned_up)
	    this.flags |= PINNED_UP;
	else
	    this.flags &= ~PINNED_UP;
    }

    boolean isThresholded() {return (flags & THRESHOLDED) != 0;}

    void setThresholded(boolean thresholded) {
	if (thresholded)
	    this.flags |= THRESHOLDED;
	else
	    this.flags &= ~THRESHOLDED;
    }

    static DataElement[] makeData(Collection v) {
	Object array[] = v.toArray();
	DataElement data[] = new DataElement[array.length];
	for (int i = 0; i < array.length; i++)
	    data[i] = (DataElement)array[i];
	return data;
    }

    public void setInd(int ind) {this.ind = ind;}
    public int getInd() {return ind;}

    // proper data element position
    public double getPosX(GraphElement graphElem) {
	return getDataPos(graphElem).pos_x;
    }

    public double getPanGenPosX(GraphElement graphElement) {
	return getPosX(graphElement);
    }

    public double getPosY(GraphElement graphElem) {
	return getDataPos(graphElem).pos_y;
    }

    public double getPosSize(GraphElement graphElem) {
	return getDataPos(graphElem).pos_sz;
    }

    public double getPosMiddle(GraphElement graphElem) {
	return getPosX(graphElem) + getPosSize(graphElem)/2;
    }

    public void setPosX(GraphElement graphElem, double pos_x) {
	getDataPos(graphElem).pos_x = (long)pos_x;
    }

    public void setPosY(GraphElement graphElem, double pos_y) {
	getDataPos(graphElem).pos_y = pos_y;
    }

    public void setPosSize(GraphElement graphElem, double pos_sz) {
	getDataPos(graphElem).pos_sz = (long)pos_sz;
    }

    // virtual display coordinates
    public double getVX(GraphElement graphElem) {
	return getDataPos(graphElem).vx != NO_V ? getDataPos(graphElem).vx :
	    getPosX(graphElem);
    }

    public double getVY(GraphElement graphElem) {
	return getDataPos(graphElem).vy != NO_V ? getDataPos(graphElem). vy :
	    getPosY(graphElem);
    }

    public double getVSize(GraphElement graphElem) {
	return getDataPos(graphElem).vsz != NO_V ?  getDataPos(graphElem).vsz :
	    getPosSize(graphElem);
    }

    public double getVMiddle(GraphElement graphElem) {
	return getVX(graphElem) + getVSize(graphElem)/2;
    }


    public void setVX(GraphElement graphElem, double vx) {
	getDataPos(graphElem).vx = (long)vx;
    }

    public void setVY(GraphElement graphElem, double vy) {
	getDataPos(graphElem).vy = (float)vy;
    }

    public void setVSize(GraphElement graphElem, double vsz) {
	getDataPos(graphElem).vsz = (long)vsz;
    }

    public void resetVX(GraphElement graphElem) {
	getDataPos(graphElem).vx = NO_V;
    }

    public void resetVY(GraphElement graphElem) {
	getDataPos(graphElem).vy = NO_V;
    }

    public void resetVSize(GraphElement graphElem) {
	getDataPos(graphElem).vsz = NO_V;
    }

    public void resetVXY(GraphElement graphElem) {
	resetVX(graphElem); resetVY(graphElem); resetVSize(graphElem);
    }

    // real display coordinates (pixel unit)
    public double getRX(GraphElement graphElem) {
	return getDataPos(graphElem).rx;
    }

    public double getRY(GraphElement graphElem) {
	return getDataPos(graphElem).ry;
    }

    public double getRSize(GraphElement graphElem) {
	return getDataPos(graphElem).rsz;
    }

    public double getRMiddle(GraphElement graphElem) {
	return getRX(graphElem) + getRSize(graphElem)/2;
    }

    public void setRX(GraphElement graphElem, double rx) {
	getDataPos(graphElem).rx = (float)rx;
    }

    public void setRY(GraphElement graphElem, double ry) {
	getDataPos(graphElem).ry = (float)ry;
    }

    public void setRSize(GraphElement graphElem, double rsz) {
	getDataPos(graphElem).rsz = (float)rsz;
    }

    private DataElementPosition getDataPos(GraphElement graphElem) {
	if (graphElem == null) {
	    System.out.println("graphElement is null");
	    return null;
	}

	DataElementPosition dataPos = (DataElementPosition)
	    dataPos_map.get(graphElem);

	if (dataPos == null) {
	    System.out.println("dataPos " + this + " is null for " + graphElem.getID() + " " + graphElem);
	    dataPos_map.display();
	}

	return dataPos;
    }

    // NEW_PROP_POLICY

    static Property propList[] = new Property[]{
	VAMPProperties.ArrayProp,
	VAMPProperties.FlagProp,
	VAMPProperties.ChromosomeProp,
	VAMPProperties.NBPProp,
	VAMPProperties.BreakpointProp,
	VAMPProperties.SmoothingProp,
	VAMPProperties.OutProp,
	VAMPProperties.GNLProp,
	VAMPProperties.IsNAProp,
	VAMPProperties.RatioProp,
	VAMPProperties.MergeOffsetProp,
	VAMPProperties.TagProp,
	VAMPProperties.CommentProp,
	VAMPProperties.PositionProp,
	VAMPProperties.ScoreProp
    };

    Vector<Property> rmPropList;

    private boolean isNA(Object value) {
	return ((String)value).equalsIgnoreCase(VAMPProperties.NA);
    }
				      
    public void setPropertyValue(Property prop, Object value, boolean mod) {
	if (!Property.NEW_PROP_MEM_POLICY) {
	    super.setPropertyValue(prop, value, mod);
	    return;
	}

	if (prop == VAMPProperties.ArrayProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    //	    array = (String)value;
	    array = (value != null ? ((String)value).getBytes() : null);
	    prop.addAfter(this, array);
	}
	else if (prop == VAMPProperties.FlagProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    flag = (value != null ? ((String)value).getBytes() : null);
	    prop.addAfter(this, flag);
	}
	/*
	else if (prop == VAMPConstants.TypeProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    type = (String)value;
	    prop.addAfter(this, value);
	}
	*/

	else if (prop == VAMPProperties.ChromosomeProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    chr = (value != null ? ((String)value).getBytes() : null);
	    prop.addAfter(this, chr);
	}

	else if (prop == VAMPProperties.NBPProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    if (isNA(value))
		nbp = NA_I;
	    else
		nbp = Byte.parseByte((String)value);
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.SmoothingProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    if (isNA(value))
		smt = NA_I;
	    else
		smt = Float.parseFloat((String)value);
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.MergeOffsetProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    merge_offset = (float)((Double)value).doubleValue();
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.TagProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    //tag = (String)value;
	    tag = (value != null ? ((String)value).getBytes() : null);
	    prop.addAfter(this, tag);
	}

	else if (prop == VAMPProperties.CommentProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    //comment = (String)value;
	    comment = (value != null ? ((String)value).getBytes() : null);
	    prop.addAfter(this, comment);
	}

	else if (prop == VAMPProperties.BreakpointProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    if (isNA(value))
		bkp = NA_I;
	    else
		bkp = Byte.parseByte((String)value);
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.OutProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    if (isNA(value))
		out = NA_I;
	    else
		out = Byte.parseByte((String)value);
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.IsNAProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    //is_na = (String)value;
	    is_na = (value != null ? ((String)value).getBytes() : null);
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.RatioProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    if (((String)value).equalsIgnoreCase("NA")) {
		ratio = 0.F;
	    }
	    else {
		ratio = Float.parseFloat((String)value);
	    }
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.GNLProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    if (isNA(value))
		gnl = NA_I;
	    else
		gnl = Byte.parseByte((String)value);
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.ScoreProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    if (isNA(value))
		score = NA_I;
	    else
		score = Byte.parseByte((String)value);
	    prop.addAfter(this, value);
	}

	else if (prop == VAMPProperties.PositionProp) {
	    setModProperties(prop, value, mod);

	    value = prop.addBefore(this, value);
	    position = Long.parseLong((String)value);
	    prop.addAfter(this, value);
	}
	else
	    super.setPropertyValue(prop, value, mod);
    }

    public Object getPropertyValue(Property prop) {
	if (!Property.NEW_PROP_MEM_POLICY)
	    return super.getPropertyValue(prop);

	if (prop == null)
	    return null;

	/*
	if (prop == VAMPConstants.TypeProp)
	    return type;
	*/

	if (prop == VAMPProperties.ArrayProp) {
	    return PropertyElement.b2s(array);
	}

	if (prop == VAMPProperties.FlagProp) {
	    return PropertyElement.b2s(flag);
	}

	if (prop == VAMPProperties.ChromosomeProp) {
	    return PropertyElement.b2s(chr);
	}

	if (prop == VAMPProperties.NBPProp) {
	    if (nbp == NULL)
		return null;
	    if (nbp == NA_I)
		return VAMPProperties.NA;
	    return Utils.toString(nbp);
	}

	if (prop == VAMPProperties.SmoothingProp) {
	    if (smt == Float.MAX_VALUE)
		return null;
	    if (smt == NA_I)
		return VAMPProperties.NA;
	    return Utils.toString(smt);
	}

	if (prop == VAMPProperties.MergeOffsetProp) {
	    if (merge_offset == Float.MAX_VALUE)
		return null;
	    return new Double(merge_offset);
	}

	if (prop == VAMPProperties.TagProp) {
	    return PropertyElement.b2s(tag);
	}

	if (prop == VAMPProperties.CommentProp) {
	    return PropertyElement.b2s(comment);
	}

	if (prop == VAMPProperties.BreakpointProp) {
	    if (bkp == NULL)
		return null;
	    if (bkp == NA_I)
		return VAMPProperties.NA;
	    return Utils.toString(bkp);
	}

	if (prop == VAMPProperties.OutProp) {
	    if (out == NULL)
		return null;
	    if (out == NA_I)
		return VAMPProperties.NA;
	    return Utils.toString(out);
	}

	if (prop == VAMPProperties.RatioProp) {
	    if (ratio == Float.MAX_VALUE)
		return null;

	    return Utils.toString(ratio);
	}

	if (prop == VAMPProperties.IsNAProp) {
	    return PropertyElement.b2s(is_na);
	}

	if (prop == VAMPProperties.GNLProp) {
	    if (gnl == NULL)
		return null;
	    if (gnl == NA_I)
		return VAMPProperties.NA;
	    return Utils.toString(gnl);
	}

	if (prop == VAMPProperties.ScoreProp) {
	    if (score == NULL)
		return null;
	    if (score == NA_I)
		return VAMPProperties.NA;
	    return Utils.toString(score);
	}

	if (prop == VAMPProperties.PositionProp) {
	    if (position == Long.MAX_VALUE)
		return null;
	    return Utils.toString(position);
	}

	return super.getPropertyValue(prop);
    }

    public TreeMap getProperties() {
	if (!Property.NEW_PROP_MEM_POLICY)
	    return super.getProperties();

	TreeMap properties = super.getProperties();
	if (!shouldMergeProperties())
	    properties = (TreeMap)properties.clone();

	for (int n = 0; n < propList.length; n++) {
	    Object value = getPropertyValue(propList[n]);
	    if (value != null && (rmPropList == null ||
				  !rmPropList.contains(propList[n])))
		properties.put(propList[n], value);
	}
	return properties;
    }

    public boolean removeProperty(Property prop) {
	if (!Property.NEW_PROP_MEM_POLICY) {
	    return super.removeProperty(prop);
	}

	if (prop == VAMPProperties.ArrayProp)
	    array = null;
	else if (prop == VAMPProperties.FlagProp)
	    flag = null;
	else if (prop == VAMPProperties.ChromosomeProp)
	    chr = null;
	else if (prop == VAMPProperties.NBPProp)
	    nbp = NA_I;
	else if (prop == VAMPProperties.BreakpointProp)
	    bkp = NA_I;
	else if (prop == VAMPProperties.SmoothingProp)
	    smt = NA_I;
	else if (prop == VAMPProperties.TagProp)
	    tag = null;
	else if (prop == VAMPProperties.CommentProp)
	    comment = null;
	else if (prop == VAMPProperties.OutProp)
	    out = NA_I;
	else if (prop == VAMPProperties.IsNAProp)
	    is_na = null;
	else if (prop == VAMPProperties.RatioProp)
	    ratio = 0.F;
	else if (prop == VAMPProperties.GNLProp)
	    gnl = NA_I;
	else if (prop == VAMPProperties.ScoreProp)
	    score = NA_I;
	else if (prop == VAMPProperties.PositionProp)
	    position = Long.MAX_VALUE;
	else
	    super.removeProperty(prop);

	if (rmPropList == null) {
	    rmPropList = new Vector();
	}

	if (!rmPropList.contains(prop)) {
	    rmPropList.add(prop);
	}

	return true;
    }

    //    String getTag() {return tag;}
    String getTag() {return PropertyElement.b2s(tag);}
    //    String getIsNA() {return is_na;}
    String getIsNA() {return PropertyElement.b2s(is_na);}
    float getRatio() {return ratio;}
    int getBkp() {return bkp;}

    // warning: these function should be called only from XMLArrayDataFactory
    // as setModProperties(), addBefore() and addAfter() are not called

    void setSmoothing(String value) {
	smt = Float.parseFloat((String)value);
    }

    void setBreakpoint(String value) {
	smt = Float.parseFloat((String)value);
    }

    boolean crossRegion(Region region, GraphElement graphElement) {
	double d_beginx = getPosX(graphElement);
	double d_endx = d_beginx + getPosSize(graphElement);
	double r_beginx = region.getBegin().getPosX();
	double r_endx = region.getEnd().getPosX();

	return (d_beginx >= r_beginx && d_beginx <= r_endx) ||
	    (d_endx >= r_beginx && d_endx <= r_endx);
    }

    public double getRX(GraphCanvas canvas, GraphElement graphElement) {
	return getRX(graphElement);
    }

    public double getRX(GraphCanvas canvas, GraphElement graphElement, boolean pangen) {
	return getRX(graphElement);
    }

    public double getRY(GraphCanvas canvas, GraphElement graphElement) {
	return getRY(graphElement);
    }

    public double getRY(GraphCanvas canvas, GraphElement graphElement, boolean pangen) {
	return getRY(graphElement);
    }

    public double getRSize(GraphCanvas canvas, GraphElement graphElement) {
	return getRSize(graphElement);
    }

    public DataElement cloneToDataElement(GraphElement profile) throws Exception {
	return (DataElement)clone();
    }

    public void complete(GraphElement graphElement) throws Exception {
    }

    public void release() {
    }

    public void release(GraphElement graphElement, int n) {
    }

    public String getSChr() {
	return VAMPUtils.getChr(this);
    }

    public void setTempPropertyValue(Property prop, Object value) throws Exception {
	if (!prop.isTemporary()) {
	    throw new Exception("setTempPropertyValue: property " + prop.getName() + " is not tagged temporary");
	}
	setPropertyValue(prop, value);
    }

    public RWDataElementProxy cloneToRWProxy(boolean clone_props) throws Exception {
	return (DataElement)clone();
    }

    public RWDataElementProxy cloneToRWProxy(LoadPropertiesCondition cond) throws Exception {
	return (DataElement)clone();
    }

    public boolean isCompletable() {
	return true;
    }

    public String getSType(GraphElement graphElement) {
	return VAMPUtils.getType(this);
    }

    public void syncProperties(int flags, GraphElement graphElement) {
    }

    public void setIsNA(GraphElement graphElement) {
	setPosY(graphElement, 0.);
	setPropertyValue(VAMPProperties.IsNAProp, "true");
    }

    public void setIsMissing() {
	setPropertyValue(VAMPProperties.MissingProp, "true");
    }

    public void setUserVal(int offset, int size, int value) {
    }

    public int getUserVal(int offset, int size) {
	return -1;
    }
}
