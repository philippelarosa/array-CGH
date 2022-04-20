
package fr.curie.vamp.data;

import fr.curie.vamp.properties.*;
import fr.curie.vamp.gui.ProfileDisplayer;
import fr.curie.vamp.*;

import java.io.*;
import java.util.*;
import java.awt.*;

public class Probe extends LightPropertyElement implements fr.curie.vamp.RWDataElementProxy {

    private int pos; // should be long, but int pos + chr is enough
    private int size; // size + type (2 bits) => size_type
    private float posy; // short ? in this case, the Profile coef is needed
    private float ratio; // short ? in this case, the Profile coef is needed
    private float smt;
    private int typ_out_bkp_gnl_chr;

    private int prop_off;

    public static int ALLOC_PROBE_CNT = 0;

    private static final int INVALID_PROPERTY_OFFSET = -1;

    public static final int GNL_OFFSET = 3;
    /*
      Following information must be added:
      type
      smoothing
      breakpoint
      outlier
      centromere ?

      float smt;
      short chr_gnl_bkp_out_type; // short or int does not change size because of alignement
      chr: 6 bytes
      gnl: 3 bytes
      bkp: 1 byte
      out: 1 byte
      ctm: 1 byte
      type: 4 (16 - 12) bytes
      missing: 1
    */

    private static int mask(int size) {
	return (1 << size) - 1;
    }

    private static final int CHR_SIZE = 6;
    private static final int GNL_SIZE = 3;
    private static final int BKP_SIZE = 1;
    private static final int OUT_SIZE = 1;
    private static final int CTM_SIZE = 1;
    private static final int TYP_SIZE = 4;
    private static final int MIS_SIZE = 1;
    private static final int USR_SIZE = 15;

    private static final int CHR_SHIFT = 0; // 0-5: 64 chr max
    private static final int GNL_SHIFT = (CHR_SHIFT + CHR_SIZE); // 6-9 (but 2 bits should be enough)
    private static final int BKP_SHIFT = (GNL_SHIFT + GNL_SIZE); // 10-10
    private static final int OUT_SHIFT = (BKP_SHIFT + BKP_SIZE); // 10-10
    private static final int CTM_SHIFT = (OUT_SHIFT + OUT_SIZE); // 11-11
    private static final int TYP_SHIFT = (CTM_SHIFT + CTM_SIZE); // 12-15
    private static final int MIS_SHIFT = (TYP_SHIFT + TYP_SIZE); // 16-16
    private static final int USR_SHIFT = (MIS_SHIFT + MIS_SIZE); // 17-<31

    private static final int CHR_MASK = mask(CHR_SIZE);
    private static final int GNL_MASK = mask(GNL_SIZE);
    private static final int BKP_MASK = mask(BKP_SIZE);
    private static final int OUT_MASK = mask(OUT_SIZE);
    private static final int CTM_MASK = mask(CTM_SIZE);
    private static final int TYP_MASK = mask(TYP_SIZE);
    private static final int MIS_MASK = mask(MIS_SIZE);
    private static final int USR_MASK = mask(USR_SIZE);

    private static final float INVALID_FLOAT =  Float.MAX_VALUE;
    private static final float IS_NA_VALUE   = Float.MIN_VALUE;

    public Probe() {
	pos = size = 0;
	posy = 0.F;
	ratio = INVALID_FLOAT;
	smt = 0.F;
	typ_out_bkp_gnl_chr = 0;
	prop_off = INVALID_PROPERTY_OFFSET;
	ALLOC_PROBE_CNT++;
	//setGnl(0);
    }

    public void setPos(int pos) {
	this.pos = pos;
    }

    public void setSize(int size) {
	this.size = size;
    }

    public void setTypOutBkpGnlChr(int typ_out_bkp_gnl_chr) {
	this.typ_out_bkp_gnl_chr = typ_out_bkp_gnl_chr;
    }

    public void setChr(int chr) {
	typ_out_bkp_gnl_chr &= ~(CHR_MASK << CHR_SHIFT);
	typ_out_bkp_gnl_chr |= chr << CHR_SHIFT;
    }

    public void setGnl(int gnl) {
	typ_out_bkp_gnl_chr &= ~(GNL_MASK << GNL_SHIFT);
	typ_out_bkp_gnl_chr |= (gnl + GNL_OFFSET) << GNL_SHIFT;
    }

    public void setBkp(int bkp) {
	typ_out_bkp_gnl_chr &= ~(BKP_MASK << BKP_SHIFT);
	typ_out_bkp_gnl_chr |= bkp << BKP_SHIFT;
    }

    public void setOut(int out) {
	typ_out_bkp_gnl_chr &= ~(OUT_MASK << OUT_SHIFT);
	typ_out_bkp_gnl_chr |= out << OUT_SHIFT;
    }

    public void setCtm(int ctm) {
	typ_out_bkp_gnl_chr &= ~(CTM_MASK << CTM_SHIFT);
	typ_out_bkp_gnl_chr |= ctm << CTM_SHIFT;
    }

    public void setType(int type) {
	typ_out_bkp_gnl_chr &= ~(TYP_MASK << TYP_SHIFT);
	typ_out_bkp_gnl_chr |= type << TYP_SHIFT;
    }

    public void setIsMissing() {
	typ_out_bkp_gnl_chr &= ~(MIS_MASK << MIS_SHIFT);
	typ_out_bkp_gnl_chr |= 1 << MIS_SHIFT;
    }

    public void setUserVal(int offset, int size, int value) {
	int shift = USR_SHIFT + offset;
	assert shift + size < 32;
	int mask = mask(size);
	typ_out_bkp_gnl_chr &= ~(mask << shift);
	typ_out_bkp_gnl_chr |= value << shift;
    }

    public void setIsNA(GraphElement graphElement) {
	setPosY(IS_NA_VALUE);
    }

    public void setPosY(float posy) {
	this.posy = posy;
    }

    public void setRatio(float ratio) {
	this.ratio = ratio;
    }

    public void setSmoothing(float smt) {
	this.smt = smt;
    }

    public void setPropertyOffset(int prop_off) {
	this.prop_off = prop_off;
    }

    public int getPos() {
	return pos;
    }

    public int getPosMiddle() {
	return pos + size / 2;
    }

    public int getSize() {
	return size;
    }

    public int getChr() {
	return (typ_out_bkp_gnl_chr >> CHR_SHIFT) & CHR_MASK;
    }

    public String getSGnl() {
	return (new Integer(getGnl())).toString();
    }

    public Long getLPanGenPos(GraphElement graphElement) {
	return new Long((long)getPanGenPosX(graphElement));
    }

    public String getSSize() {
	if (size < 0) {
	    return "NA";
	}

	return (new Integer(getSize())).toString();
    }

    public String getSChr() {
	return (new Integer(getChr())).toString();
    }

    public Object getID() {
	if (getPropMap() == null) {
	    throw new Error("Probe.getID: probe not completed");
	}

	Iterator it = getPropMap().entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<Property, Object> entry = (Map.Entry)it.next();
	    if (entry.getKey().isID()) {
		return entry.getValue();
	    }
	}
	return "<unknown>";
    }

    public int getGnl() {
	int gnl = (typ_out_bkp_gnl_chr >> GNL_SHIFT) & GNL_MASK;
	/*
	if (gnl == 0) {
	    return 0;
	}
	*/
	return gnl - GNL_OFFSET;
    }

    public int getBkp() {
	return (typ_out_bkp_gnl_chr >> BKP_SHIFT) & BKP_MASK;
    }

    public int getOut() {
	return (typ_out_bkp_gnl_chr >> OUT_SHIFT) & OUT_MASK;
    }

    public int getCtm() {
	return (typ_out_bkp_gnl_chr >> CTM_SHIFT) & CTM_MASK;
    }

    public int getType() {
	return (typ_out_bkp_gnl_chr >> TYP_SHIFT) & TYP_MASK;
    }

    public String getSType(Profile profile) {
	return profile.getType(getType());
    }

    public String getSType(GraphElement graphElement) {
	return getSType(graphElement.asProfile());
    }

    public boolean isMissing() {
	return ((typ_out_bkp_gnl_chr >> MIS_SHIFT) & MIS_MASK) != 0;
    }

    public int getUserVal(int offset, int size) {
	int shift = USR_SHIFT + offset;
	return (typ_out_bkp_gnl_chr >> shift) & mask(size);
    }

    public int getTypOutBkpGnlChr() {
	return typ_out_bkp_gnl_chr;
    }

    public float getRatio() {
	if (ratio != INVALID_FLOAT) {
	    return ratio;
	}
	return getPosY();
    }

    public float getPassThruPosY() {
	return posy;
    }

    public float getPassThruRatio() {
	return ratio;
    }

    public boolean isNA() {
	return posy == IS_NA_VALUE;
    }

    public float getSmoothing() {
	return smt;
    }

    public void setPanGenPos(Profile profile, long pos) {
	this.pos = (int)(pos - profile.getChrPos(getChr()));
    }

    public static boolean VERBOSE = false;

    public long getPanGenPos(Profile profile) {
	if (getChr() == 0) {
	    return getPos();
	}
	return profile.getChrPos(getChr()) + getPos();
    }

    public long getPanGenPosMiddle(Profile profile) {
	return profile.getChrPos(getChr()) + getPosMiddle();
    }

    public int getPropertyOffset() {
	return prop_off;
    }

    public boolean isCompletable() {
	return prop_off != INVALID_PROPERTY_OFFSET;
    }

    public void writeProps(ObjectOutputStream os) throws Exception {
	writeMap(os);
    }

    public void addProp(Property key, Object value) {
	super.addProp(key, value);
	// TBD: must complete for Out, Breakpoint, Smoothing, NA, GNL, Position
	if (key == VAMPProperties.RatioProp) {
	    if (((String)value).equalsIgnoreCase("NA")) {
		setRatio(IS_NA_VALUE);
	    }
	    else {
		setRatio(Float.parseFloat((String)value));
	    }
	}
    }

    public void readProps(ObjectInputStream ois) throws Exception {
	map = readMap(ois);
    }

    /*
    public void write(ObjectOutputStream os, int prop_off) throws Exception {
	os.writeInt(pos);
	os.writeInt(size);
	os.writeShort(typ_out_bkp_gnl_chr);
	os.writeFloat(posy);
	this.prop_off = prop_off;
	os.writeInt(prop_off);
    }

    public void read(ObjectInputStream is) throws Exception {
	pos = is.readInt();
	size = is.readInt();
	typ_out_bkp_gnl_chr = is.readShort();
	posy = is.readFloat();
	prop_off = is.readInt();
    }
    */

    public void print() {
	print(-1);
    }

    public void print(int num) {
	System.out.print("\nProbe");
	if (num >= 0) {
	    System.out.println(" #" + num);
	}
	else {
	    System.out.println("");
	}

	System.out.println("  IType: " + getType());
	System.out.println("  Pos: " + pos);
	System.out.println("  Size: " + size);
	System.out.println("  Chr: " + getChr());
	System.out.println("  GNL: " + getGnl());
	System.out.println("  Bkp: " + getBkp());
	System.out.println("  Out: " + getOut());
	System.out.println("  Smt: " + smt);
	System.out.println("  PosY: " + posy);
	System.out.println("  Ratio: " + ratio);
	printMap("  ");
    }

    private void writeMap(ObjectOutputStream os) throws Exception {
	if (map == null) {
	    os.writeShort((short)0);
	    return;
	}

	int size = map.size();

	os.writeShort((short)size);
	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    os.writeObject(entry.getKey());
	    os.writeObject(entry.getValue());
	}
    }

    private HashMap readMap(ObjectInputStream is) throws Exception {
	short mapsize = is.readShort();
	HashMap map = new HashMap();
	for (int n = 0; n < mapsize; n++) {
	    Object key = is.readObject();
	    Object value = is.readObject();
	    map.put(key, value);
	}
	return map;
    }

    public void finalize() {
	//System.out.println("probe finalizing: " + pos);
	ALLOC_PROBE_CNT--;
    }

    public double getRX(GraphCanvas canvas, GraphElement graphElement) {
	return VAMPUtils.getRX(canvas, graphElement.asProfile(), this, true);
    }

    public double getRX(GraphCanvas canvas, GraphElement graphElement, boolean pangen) {
	return VAMPUtils.getRX(canvas, graphElement.asProfile(), this, pangen);
    }

    public double getRY(GraphCanvas canvas, GraphElement graphElement) {
	return VAMPUtils.getRY(canvas, graphElement.asProfile(), this, true);
    }

    public double getRY(GraphCanvas canvas, GraphElement graphElement, boolean pangen) {
	return VAMPUtils.getRY(canvas, graphElement.asProfile(), this, pangen);
    }

    public double getRSize(GraphCanvas canvas, GraphElement graphElement) {
	return getSize() * canvas.getScale().getScaleX();
    }

    public Object getPropertyValue(Property prop) {
	if (getPropMap() == null) {
	    throw new Error("Probe.getPropertyValue: probe not completed");
	}
	return getProp(prop);
    }

    public void maskProperty(Property prop) {
	setPropertyValue(Property.getMaskedProperty(prop), new Boolean(true));
    }

    public void removeAllProperties() {
	removePropMap();
    }

    public double getPosX(GraphElement graphElement) {
	return getPos();
    }

    public double getPanGenPosX(GraphElement graphElement) {
	return getPanGenPos(graphElement.asProfile());
    }

    public double getPosSize(GraphElement graphElem) {
	return getSize();
    }

    private float getPosY() {
	return isNA() ? 0.F : posy;
    }

    public double getPosY(GraphElement graphElement) {
	return getPosY();
    }

    public double getVY(GraphElement graphElem) {
	return posy;
    }

    public double getVSize(GraphElement graphElem) {
	return getSize();
    }

    // 
    public void setRBounds(GraphElement graphElement, double rx, double ry, double width, double height) {
	//System.err.println("Probe.setRBounds not implemented");
    }

    public void setGraphics(Graphics2D g, GraphElement graphElement) {
 	ProfileDisplayer.GraphicSetter._setGraphics(g, this, graphElement.asProfile());
    }

    public void copyToPos(GraphElement graphElement, RODataElementProxy d, GraphElement ographElement) {

	if (!(d instanceof DataElement)) {
	    return;
	}

	DataElement dataElement = (DataElement)d;
	Profile profile = ographElement.asProfile();

	dataElement.declare(graphElement);
	//dataElement.setVX(graphElement, getPos());
	dataElement.setVX(graphElement, getPanGenPos(profile));
	dataElement.setVY(graphElement, getVY(profile));
	dataElement.setVSize(graphElement, getSize());

	//dataElement.setPosX(graphElement, getPos());
	dataElement.setPosX(graphElement, getPanGenPos(profile));
	dataElement.setPosY(graphElement, getPosY(profile));
	dataElement.setPosSize(graphElement, getSize());
    }

    private Probe cloneToRWProxy() throws Exception {
	Probe probe = new Probe();

	probe.pos = pos;
	probe.size = size;
	probe.posy = posy;
	probe.ratio = ratio;
	probe.smt = smt;
	probe.typ_out_bkp_gnl_chr = typ_out_bkp_gnl_chr;
	probe.prop_off = INVALID_PROPERTY_OFFSET;

	return probe;
    }


    public RWDataElementProxy cloneToRWProxy(LoadPropertiesCondition cond) throws Exception {
	Probe probe = cloneToRWProxy();

	if (cond.loadProperties(probe)) {
	    complete(cond.getGraphElement());
	    if (getPropMap() == null) {
		throw new Error("Probe.cloneToRWProxy: probe not completed");
	    }

	    probe.setPropMap((HashMap<Property, Object>)getPropMap().clone());
	}

	return probe;
    }

    public RWDataElementProxy cloneToRWProxy(boolean clone_props) throws Exception {
	Probe probe = cloneToRWProxy();

	/*
	Probe probe = new Probe();

	probe.pos = pos;
	probe.size = size;
	probe.posy = posy;
	probe.ratio = ratio;
	probe.smt = smt;
	probe.typ_out_bkp_gnl_chr = typ_out_bkp_gnl_chr;
	probe.prop_off = INVALID_PROPERTY_OFFSET;
	*/

	if (clone_props) {
	    if (getPropMap() == null) {
		throw new Error("Probe.cloneToRWProxy: probe not completed");
	    }

	    probe.setPropMap((HashMap<Property, Object>)getPropMap().clone());
	}

	return probe;
    }

    public DataElement cloneToDataElement(GraphElement profile) throws Exception {
	profile.asProfile().complete(this);
	DataElement dataElement = new DataElement(this);

	release();

	return dataElement;
    }

    public void complete(GraphElement profile) throws Exception {
	profile.asProfile().complete(this);
    }

    public void release(GraphElement profile, int n) {
	release();
	profile.asProfile().releaseProbe(n);
    }

    public void setTempPropertyValue(Property prop, Object value) throws Exception {
	if (!prop.isTemporary()) {
	    throw new Exception("Probe.setTempPropertyValue: property " + prop.getName() + " is not tagged temporary");
	}

	addProp(prop, value);
    }

    public void setPropertyValue(Property prop, Object value) {
	addProp(prop, value);
    }

    public TreeMap getProperties() {
	TreeMap tmap = new TreeMap();
	HashMap<Property, Object> map = getPropMap();

	if (map == null) {
	    return tmap;
	}

	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<Property, Object> entry = (Map.Entry)it.next();
	    tmap.put(entry.getKey(), entry.getValue());
	    //System.out.println(entry.getKey().getName() + " " + entry.getValue().getClass().getName());
	}

	return tmap;
    }

    public boolean removeProperty(Property prop) {
	rmvProp(prop);
	return true;
    }

    public void setPosX(GraphElement graphElem, double pos_x) {
	setPos((int)pos_x);
    }

    public void setPosY(GraphElement graphElement, double posy) {
	setPosY((float)posy);
    }

    public void setPosSize(GraphElement graphElem, double pos_sz) {
	setSize((int)pos_sz);
    }

    public void declare(GraphElement graphElement) {
    }

    public void syncProperties(int flags, GraphElement graphElement) {

	if ((flags & TYPE_PROP) != 0) {
	    setPropertyValue(VAMPProperties.TypeProp, getSType(graphElement));
	}

	if ((flags & POS_PROP) != 0) {
	    setPropertyValue(VAMPProperties.PositionProp, getLPanGenPos(graphElement));
	}

	if ((flags & SIZE_PROP) != 0) {
	    setPropertyValue(VAMPProperties.SizeProp, getSSize());
	}

	if ((flags & CHR_PROP) != 0) {
	    setPropertyValue(VAMPProperties.ChromosomeProp, getSChr());
	}

	if ((flags & GNL_PROP) != 0) {
	    setPropertyValue(VAMPProperties.GNLProp, getSGnl());
	}
    }
}

