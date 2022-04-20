
/*
 *
 * ProfileFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.File;

import fr.curie.vamp.data.Profile;
import fr.curie.vamp.data.Probe;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.gui.optim.*;

abstract class ProfileFactory extends GraphElementFactory {

    public static final int GRAPHIC_INFO = 0x1;

    protected Profile profile;
    protected int hints;
    protected GraphElement graphElementBase;

    protected String serialFile;
    protected Vector<Integer> bkp_v, out_v, smt_v;
    protected HashMap<String, Integer> typeMap;
    private int last_type;
    protected float last_smt;

    protected ProfileSerializer profSerial;

    protected ProfileFactory(GlobalContext globalContext, String serialFile, int hints, GraphElement graphElementBase) {
	super(globalContext);

	this.serialFile = serialFile;
	this.hints = hints;
	this.graphElementBase = graphElementBase;

	profile = null;
	bkp_v = new Vector<Integer>();
	out_v = new Vector<Integer>();
	smt_v = new Vector<Integer>();

	typeMap = new HashMap<String, Integer>();

	last_type = 0;
	last_smt = Float.MIN_VALUE;
    }

    public void init(String name, int data_cnt) throws Exception {
	init(name, data_cnt, null);
    }

    public void init(String name, int data_cnt, TreeMap properties) throws Exception {
	init(name, data_cnt, properties, true);
    }

    public void init(String name, int data_cnt, TreeMap properties, boolean pangen) throws Exception {
	profile = new Profile(name, data_cnt);
	if (properties != null) {
	    profile.setProperties(properties);
	}
	/*
	if (properties != null) {
	    HashMap<Property, Object> propMap = new HashMap();
	    Iterator it = properties.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		propMap.put(prop, entry.getValue());
	    }

	    profile.setPropMap(propMap);
	}
	*/

	long chrPos[] = VAMPUtils.getChrPos(globalContext, (String)profile.getPropertyValue(Property.getProperty("Organism")));

	for (int n = 0; n < chrPos.length; n++) {
	    profile.setChrPos(n+1, chrPos[n]);
	}

	//profSerial = new ProfileSerializer(profile, chrPos, serialFile, pangen);
	//profSerial = ProfileSerializer.getInstance(profile, chrPos, serialFile, pangen);
	profSerial = ProfileSerializerFactory.getInstance().getSerializer(ProfileSerializerFactory.NATIVE_JAVA, profile, chrPos, serialFile, pangen);
	//profSerial.writeHeader(); // for SequentialAccess only
    }

    public void setGraphElementProperties(TreeMap properties) throws Exception {
	profile.setProperties(properties);
    }

    public void setProbeCount(int probe_cnt, RODataElementProxy probe) {
	profile.setProbeCount(probe_cnt);
	profile.setProbe(probe_cnt - 1, (Probe)probe);
    }

    public GraphElement getGraphElement() throws Exception {
	/*
	ProfileUnserializer profUnserial = new ProfileUnserializer(serialFile);
	profile.setUnserializer(profUnserial);
	*/
	return profile;
    }

    protected int[] toArray(Vector<Integer> v) {
	int arr[] = new int[v.size()];
	for (int n = 0; n < arr.length; n++) {
	    arr[n] = v.get(n);
	}
	return arr;
    }

    protected int getType(HashMap<String, Integer> typeMap, String s) {
	Integer type = typeMap.get(s);
	if (type == null) {
	    type = new Integer(last_type++);
	    typeMap.put(new String(s), type);
	}
	return type.intValue();
    }

    protected int getSInt(String s) {
	if (s.equalsIgnoreCase("na")) {
	    return 0;
	}
	
	return Integer.parseInt(s);
    }

    protected int getUInt(String s) {
	int n = getSInt(s);
	if (n < 0) {
	    n = 0;
	}
	return n;
    }

    protected Probe makeProbe(int n, RODataElementProxy _d) {
	if (_d instanceof Probe) {
	    Probe probe = (Probe)_d;

	    bkp_v.add(probe.getBkp());
	    out_v.add(probe.getOut());

	    float smt = probe.getSmoothing();
	    if (smt != last_smt) {
		smt_v.add(n);
		last_smt = smt;
	    }

	    return probe;
	}

	DataElement d = (DataElement)_d;
	Probe probe = new Probe();

	if (VAMPUtils.getChr((DataElement)d) == null) {
	    System.out.println("NULL CHR: ");
	    d.display();
	}

	String chr = VAMPUtils.norm2Chr(VAMPUtils.getChr((DataElement)d));
	probe.setChr(Integer.parseInt(chr));

	if (VAMPUtils.isMergeChr(profile)) {
	    probe.setPanGenPos(profile, (long)d.getPosX(graphElementBase));
	}
	else {
	    probe.setPos((int)d.getPosX(graphElementBase));
	}

	String s = VAMPUtils.getGNL(d);
	int gnl;
	if (s.equalsIgnoreCase("na")) {
	    gnl = Probe.GNL_OFFSET;
	}
	else {
	    gnl = Integer.parseInt(s);
	}
	probe.setGnl(gnl);

	s = (String)d.getPropertyValue(VAMPProperties.BreakpointProp);
	int bkp = getUInt(s);
	probe.setBkp(bkp);
	bkp_v.add(bkp);

	s = (String)d.getPropertyValue(VAMPProperties.OutProp);

	int out = getSInt(s);
	probe.setOut(out);
	out_v.add(out);

	s = (String)d.getPropertyValue(VAMPProperties.SmoothingProp);
	float smt;
	if (s.equalsIgnoreCase("na")) {
	    smt = 0F;
	}
	else {
	    smt = Float.parseFloat(s);
	}

	probe.setSmoothing(smt);
	if (smt != last_smt) {
	    smt_v.add(n);
	    last_smt = smt;
	}

	probe.setType(getType(typeMap, VAMPUtils.getType(d)));

	if (VAMPUtils.isNA(d)) {
	    probe.setPosY(Float.MIN_VALUE);
	}
	else {
	    probe.setPosY((float)d.getPosY(graphElementBase));
	}

	probe.setSize((int)d.getPosSize(graphElementBase));

	TreeMap treeMap = d.getProperties();
	Iterator it = treeMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    probe.addProp(prop, entry.getValue());
	}

	return probe;
    }

    protected void writeFooter() throws Exception {
	TreeMap properties = profile.getProperties();
	HashMap<Property, Object> propMap = new HashMap();
	Iterator it = properties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    Object value = entry.getValue();

	    if (prop.isSerializable() && !prop.isTemporary()) {
		propMap.put(prop, value);
	    }		
	    else {
		System.out.println("property " + prop.getName() + " is not serializabled");
	    }
	}
	
	profile.setPropMap(propMap);

	profSerial.writeFooter(typeMap, toArray(bkp_v), toArray(out_v), toArray(smt_v));
	profSerial.close();
	profSerial = null;
    }

    protected void writeGraphicInfo() throws Exception {
	/*if ((hints & GRAPHIC_INFO) != 0)*/ {
	    GraphicInfoCapturer capturer = new GraphicInfoCapturer(profile, serialFile);
	    capturer.captureAndWrite();
	}
    }

    public RWDataElementProxy makeRWDataElementProxy() {
	return new Probe();
    }

    public void finalize() {
	if (profSerial != null) {
	    try {
		profSerial.close();
	    }
	    catch(Exception e) {
	    }
	}
    }

    public String serialFile() {
	return serialFile;
    }

    public void deleteSerialFiles() {
	File file;

	file = new File(serialFile + ProfileSerialUtils.DISPLAY_SUFFIX);
	file.delete();

	file = new File(serialFile + ProfileSerialUtils.PROP_SUFFIX);
	file.delete();

	file = new File(serialFile + GraphicInfo.GRINFO_SUFFIX);
	file.delete();
    }
}
