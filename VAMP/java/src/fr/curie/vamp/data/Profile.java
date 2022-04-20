
package fr.curie.vamp.data;

import fr.curie.vamp.*;
import fr.curie.vamp.gui.optim.*;
import fr.curie.vamp.data.serial.*;

import java.io.*;
import java.util.*;

public class Profile extends GraphElement {

    public static final int NONE = 0;
    public static final int CACHE_PROBES = 1;
    public static final int MAX_CHR_CNT = 40;

    private String name;
    private int pangen_probe_cnt;
    private int probe_cnt;
    private long minX = Long.MAX_VALUE;
    private long maxX = Long.MIN_VALUE;
    //    private double minY = Double.MAX_VALUE;
    //private double maxY = Double.MIN_VALUE;
    private double minY = Double.POSITIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;
    private HashMap<Integer, String> typeMap;
    private int bkp[];
    private int out[];
    private int smt[];
    private int unserialPolicy = NONE;
    private int loaded_probe_cnt = 0;
    private int max_probe_cnt = Integer.MAX_VALUE;
    private boolean full_imported;

    private long chrPosMap[] = new long[MAX_CHR_CNT];
    private int chrBegProbeNumMap[] = new int[MAX_CHR_CNT];
    private int chrEndProbeNumMap[] = new int[MAX_CHR_CNT];
    private boolean chrNums[] = new boolean[MAX_CHR_CNT];
    private boolean isWholePanGen = false;

    private int chr_cnt = 0;

    private ProfileUnserializer unserializer;

    private int probe_offset = 0;
    private int chrNum = 0;
    private boolean isChr = false;
    private Probe probes[];
    private String filename;

    public Profile(String name, int probe_cnt) throws Exception {
	this(name, probe_cnt, probe_cnt, true);
    }

    public Profile(String name, int probe_cnt, int pangen_probe_cnt) throws Exception {
	this(name, probe_cnt, pangen_probe_cnt, true);
    }

    public Profile(String name, int probe_cnt, boolean full_imported) throws Exception {
	this(name, probe_cnt, probe_cnt, full_imported);
    }

    public Profile(String name, int probe_cnt, int pangen_probe_cnt, boolean full_imported) throws Exception {
	this.name = name;
	this.probe_cnt = probe_cnt;
	this.pangen_probe_cnt = pangen_probe_cnt;
	this.full_imported = full_imported; // NOT YET IMPLEMENTED

	for (int n = 0; n < chrNums.length; n++) {
	    chrNums[n] = true;
	}

	for (int n = 0; n < chrBegProbeNumMap.length; n++) {
	    chrBegProbeNumMap[n] = -1;
	    chrEndProbeNumMap[n] = -1;
	}

	isWholePanGen = isWholePanGen();

	release();
    }

    private void displayChrProbe(String msg) {
	System.out.println("msg: " + msg);
	for (int n = 0; n < chrBegProbeNumMap.length; n++) {
	    System.out.println("chrProbe: " + chrBegProbeNumMap[n] + " " + chrEndProbeNumMap[n]);
	}
    }

    private void clone(Profile profile, boolean dup) throws Exception {
	profile.minX = minX;
	profile.maxX = maxX;
	profile.minY = minY;
	profile.maxY = maxY;
	profile.typeMap = typeMap;

	profile.bkp = bkp;
	profile.out = out;
	profile.smt = smt;

	profile.unserialPolicy = unserialPolicy;

	if (dup) {
	    profile.unserializer = unserializer.cloneRealize();
	}
	else {
	    profile.unserializer = unserializer;
	}

	profile.full_imported = full_imported;

	profile.chrPosMap = chrPosMap;
	profile.chrBegProbeNumMap = chrBegProbeNumMap;
	profile.chrEndProbeNumMap = chrEndProbeNumMap;

	//profile.cloneProperties(this);
	super.clone(profile);
    }

    public GraphElement dupSerializer() throws Exception {
	return cloneRealize(true);
    }

    public Profile cloneRealize() throws Exception {
	return cloneRealize(false);
    }

    public Profile cloneRealize(boolean dup) throws Exception {
	Profile profile = new Profile(name, probe_cnt, pangen_probe_cnt);

	clone(profile, dup);
	/*
	profile.minX = minX;
	profile.maxX = maxX;
	profile.minY = minY;
	profile.maxY = maxY;
	profile.typeMap = typeMap;
	profile.bkp = bkp;
	profile.out = out;
	profile.smt = smt;
	profile.unserialPolicy = unserialPolicy;
	profile.chrPosMap = chrPosMap;
	profile.chrBegProbeNumMap = chrBegProbeNumMap;
	profile.chrEndProbeNumMap = chrEndProbeNumMap;
	profile.unserializer = unserializer;
	profile.full_imported = full_imported;
	*/

	profile.isChr = isChr;
	profile.chr_cnt = chr_cnt;
	profile.setChrNums(chrNums);
	//profile.isWholePanGen = isWholePanGen;

	//profile.cloneProperties(this);
	profile.probes = probes; // ??
	profile.loaded_probe_cnt = loaded_probe_cnt;

	//System.out.println("clone " + this + " -> " + profile);
	if (graphicProfile != null) {
	    new GraphicProfile(profile, graphicProfile.getGraphicInfo());
	}
	return profile;
    }

    public Object clone() {
	try {
	    return cloneRealize(true);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public Profile merge() throws Exception {
	boolean chrNums[] = new boolean[MAX_CHR_CNT];
	for (int n = 0; n < chrNums.length; n++) {
	    chrNums[n] = true;
	}
	return merge(chrNums);
    }

    public Profile merge(boolean _chrNums[]) throws Exception {
	Profile profile = new Profile(name, pangen_probe_cnt);

	clone(profile, false);

	profile.minX = profile.getProbe(0).getPos();
	profile.maxX = profile.getProbe(pangen_probe_cnt-1).getPos();

	/*
	profile.minY = minY;
	profile.maxY = maxY;
	profile.typeMap = typeMap;

	profile.bkp = bkp; // must be recomputed
	profile.out = out; // must be recomputed
	profile.smt = smt; // must be recomputed

	profile.unserialPolicy = unserialPolicy;
	profile.unserializer = unserializer;
	profile.full_imported = full_imported;

	profile.chrPosMap = chrPosMap;
	profile.chrBegProbeNumMap = chrBegProbeNumMap;
	profile.chrEndProbeNumMap = chrEndProbeNumMap;

	profile.cloneProperties(this);
	*/

	profile.probes = null;
	profile.loaded_probe_cnt = 0;

	profile.probe_offset = 0;

	profile.setChrNums(_chrNums);
	profile.chrNum = 0;
	profile.isChr = false;
	profile.chr_cnt = 0;
	profile.chr_cnt = profile.getChrCount();

	if (graphicProfile != null) {
	    new GraphicProfile(profile, graphicProfile.getGraphicInfo());
	}

	return profile;
    }

    public Profile split(int chr) throws Exception {
	if (chrBegProbeNumMap[chr] < 0 || !chrNums[chr]) {
	    return null;
	}
				       
	int chrCnt = getChrCount();

	int start = chrBegProbeNumMap[chr];
	int end = chrEndProbeNumMap[chr];

	Profile profile = new Profile(name, end - start + 1, pangen_probe_cnt);

	clone(profile, false);
	profile.minX = getProbe(start, false, true).getPos();
	profile.maxX = getProbe(end, false, true).getPos();

	/*
	profile.minY = minY; // not exactly, but OK
	profile.maxY = maxY; // not exactly, but OK
	profile.typeMap = typeMap;

	profile.bkp = bkp; // must be recomputed
	profile.out = out; // must be recomputed
	profile.smt = smt; // must be recomputed

	profile.unserialPolicy = unserialPolicy;
	profile.unserializer = unserializer;
	profile.full_imported = full_imported;

	profile.chrPosMap = chrPosMap; // must be changed: I don't think so..
	profile.chrBegProbeNumMap = chrBegProbeNumMap; // must be changed: I don't think so..
	profile.chrEndProbeNumMap = chrEndProbeNumMap; // must be changed: I don't think so..

	profile.cloneProperties(this);
	*/

	profile.probes = null;
	profile.loaded_probe_cnt = 0;

	profile.probe_offset = start;

	profile.chrNum = chr;

	// EV : 21/05/08
	for (int n = 0; n < chrNums.length; n++) {
	    profile.chrNums[n] = false;
	}
	profile.chrNums[chr] = true;
	
	profile.isWholePanGen = false;
	profile.isChr = true;
	profile.chr_cnt = 1;

	if (graphicProfile != null) {
	    new GraphicProfile(profile, graphicProfile.getGraphicInfo());
	}

	return profile;
    }

    public void print() {
	System.out.println("Profile " + name + " [" + probe_cnt + " probes]\n");
	System.out.println("  Name: " + name);
	System.out.println("  ProbeCount: " + probe_cnt);
	System.out.println("  MinX: " + minX);
	System.out.println("  MaxX: " + maxX);
	System.out.println("  MinY: " + minY);
	System.out.println("  MaxY: " + maxY);

	System.out.println("\n  Property Map {");
	//printMap("    ");
	display("    ");
	System.out.println("  }");

	System.out.println("\n  Chromomose Map {");
	printChrMaps("    ");
	System.out.println("  }");
    }

    public void printChrMaps(String indent) {
	if (chrPosMap == null) {
	    return;
	}

	for (int n = 0; n < chrPosMap.length; n++) {
	    if (chrPosMap[n] == -1) {
		break;
	    }
	    System.out.println(indent + "Chr #" + (n+1) + ": offset=" +  chrPosMap[n] + ", #probe=" + chrBegProbeNumMap[n] + ":" + chrEndProbeNumMap[n]);
	}
    }

    public void loadProbes() throws Exception {
	int o_policy = getUnserializingPolicy();
	for (int n = 0; n < probe_cnt; n++) {
	    getProbe(n);
	}
	setUnserializingPolicy(o_policy);
    }

    private int suspendBGLoadingProbes = 0;

    public void loadProbesInBackground() throws Exception {
	int o_policy = getUnserializingPolicy();
	for (int n = 0; n < probe_cnt; n++) {
	    boolean beenSuspended = false;
	    while (suspendBGLoadingProbes != 0) {
		if (!beenSuspended) {
		    System.out.println("Suspending loading probes for " + getName() + " at #" + n);
		}
		beenSuspended = true;
		try {
		    Thread.sleep(100);
		} catch(Exception e) {
		    e.printStackTrace();
		    throw e;
		}
	    }
	    if (beenSuspended) {
		System.out.println("Continue #" + n + " ...");
	    }
	    getProbe(n);
	}
	setUnserializingPolicy(o_policy);
    }

    public synchronized void incrSuspendLoadingProbesInBackground() {
	++suspendBGLoadingProbes;
	//System.out.println("incrSuspendLoadingProbesInBG " + getName() + " " + suspendBGLoadingProbes);
    }

    public synchronized void decrSuspendLoadingProbesInBackground() {
	--suspendBGLoadingProbes;
	//System.out.println("decrSuspendLoadingProbesInBG " + getName() + " " + suspendBGLoadingProbes);
	if (suspendBGLoadingProbes < 0) {
	    System.out.println("OUPS decrSuspendLoadingProbesInBG " + getName() + " " + suspendBGLoadingProbes);
	}
    }

    public boolean isWholePanGen() {
	if (isChr) {
	    return false;
	}

	for (int n = 0; n < chrNums.length; n++) {
	    if (!chrNums[n]) {
		return false;
	    }
	}

	return true;
    }

    public boolean isMasked(int n) {
	if (isWholePanGen) {
	    return false;
	}

	if (isChr) {
	    n += probe_offset;
	    return !(n >= chrBegProbeNumMap[chrNum] && n <= chrEndProbeNumMap[chrNum]);
	}

	for (int i = 0; i < chr_cnt; i++) {
	    int end = chrEndProbeNumMap[i];
	    //	    if (n >= chrBegProbeNumMap[i] && n < end) {
	    if (n >= chrBegProbeNumMap[i] && n <= end) {
		return !chrNums[i];
	    }
	}

	//assert false;
	//return false;
	return true;
    }

    public void setChrNums(boolean chrNums[]) {
	if (chrNums != null) {
	    this.chrNums = new boolean[chrNums.length];
	    for (int n = 0; n < chrNums.length; n++) {
		this.chrNums[n] = chrNums[n];
	    }
	}
	isWholePanGen = isWholePanGen();
    }

    public boolean[] getChrNums() {
	return chrNums;
    }

    public int getChrNum() {
	return chrNum;
    }

    public boolean isChr() {
	return isChr;
    }

    public synchronized Probe getProbe(int n, boolean load_props, boolean unmasked) throws Exception {
	if (!full_imported) {
	    return null;
	}

	if (!unmasked && isMasked(n)) {
	    //System.out.println("returning null #" + (n+probe_offset) + " is masked");
	    return null;
	}

	if (probes != null && probes[n] != null) {
	    if (load_props && probes[n].getPropMap() == null &&
		unserializer != null) {
		unserializer.complete(probes[n]);
	    }
	    return probes[n];
	}

	if (unserializer != null) {
	    Probe p = unserializer.getProbe(n + probe_offset);

	    if (load_props) {
		unserializer.complete(p);
	    }

	    if ((unserialPolicy & CACHE_PROBES) != 0) {
		cacheProbe(n, p);
	    }

	    return p;
	}

	System.out.println("returning null");
	return null;
    }

    public Probe getProbe(int n, boolean load_props) throws Exception {
	return getProbe(n, load_props, false);
    }

    public Probe getProbe(int n) throws Exception {
	return getProbe(n, false);
    }

    public Probe getProbeUnmasked(int n) throws Exception {
	return getProbe(n, false, true);
    }

    public Probe getProbeUnmasked(int n, boolean load_props) throws Exception {
	return getProbe(n, load_props, true);
    }

    synchronized public void complete(Probe probe) throws Exception {
	if (probe.getPropMap() == null && unserializer != null) {
	    unserializer.complete(probe);
	}
    }

    public int getProbeOffset() {
	return probe_offset;
    }

    public void setProperties(HashMap<Property, Object> map) {
	TreeMap tmap = new TreeMap();
	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    tmap.put(entry.getKey(), entry.getValue());
	}
	setProperties(tmap);
    }

    protected HashMap<Property, Object> pmap;

    public HashMap<Property, Object> getPropMap() {return pmap;}

    public void setPropMap(HashMap<Property, Object> pmap) {
	this.pmap = pmap;
    }

    public void setTypeMap(HashMap<Integer, String> typeMap) {
	this.typeMap = typeMap;
    }

    public void setBkpOutSmt(int bkp[], int out[], int smt[]) {
	this.bkp = bkp;
	this.out = out;
	this.smt = smt;
    }

    public int[] getBreakpoints() {
	return bkp;
    }

    public int[] getOutliers() {
	return out;
    }

    public int[] getSmoothings() {
	return smt;
    }

    public HashMap<Integer, String> getTypeMap() {
	return typeMap;
    }

    public String getType(int type) {
	return typeMap.get(type);
    }

    public void setProbe(int n, Probe p) {
	if (probes == null) {
	    init();
	}

	probes[n] = p;
    }

    private void cacheProbe(int n, Probe p) {
	if (probes == null) {
	    init();
	}

	if (loaded_probe_cnt < max_probe_cnt) {
	    probes[n] = p;
	    loaded_probe_cnt++;
	}
    }

    public void releaseProbe(int n) {
	if (probes != null && probes[n] != null) {
	    probes[n] = null;
	    --loaded_probe_cnt;
	}
    }

    public void release() {
	if (probes == null) {
	    return;
	}

	for (int n = 0; n < probes.length; n++) {
	    if (probes[n] != null) {
		probes[n].release();
	    }
	    probes[n] = null;
	}

	probes = null;
	loaded_probe_cnt = 0;
    }

    private void init() {
	probes = new Probe[probe_cnt];
	loaded_probe_cnt = 0;
    }

    public void setProbeCount(int probe_cnt) {
	if (this.probe_cnt != probe_cnt) {
	    this.probe_cnt = probe_cnt;
	    if (probes != null) {
		init();
	    }
	}
    }

    public int getLoadedProbeCount() {
	return loaded_probe_cnt;
    }

    public String getName() {
	return name;
    }

    public int getProbeCount() {
	return probe_cnt;
    }

    public long getMinX() {
	return minX;
    }

    public long getMaxX() {
	return maxX;
    }

    public double getMinY() {
	return minY;
    }

    public double getMaxY() {
	return maxY;
    }

    public void setMinX(long minX) {
	this.minX = minX;
    }

    public void setMaxX(long maxX) {
	this.maxX = maxX;
    }

    public void setMinY(double minY) {
	this.minY = minY;
    }

    public void setMaxY(double maxY) {
	this.maxY = maxY;
    }

    public void setMinMaxX(long x) {
	setMinMaxX(x, 0);
    }

    public void setMinMaxX(long x, long size) {
	if (x == Long.MAX_VALUE || x == Long.MIN_VALUE)
	    return;

	if (x < minX) {
	    minX = x;
	}
	if (x + size > maxX) {
	    maxX = x + size;
	}
    }

    public void setMinMaxY(double y) {
	if (y == Double.MAX_VALUE || y == Double.MIN_VALUE)
	    return;

	if (y < minY) {
	    minY = y;
	}
	if (y > maxY) {
	    maxY = y;
	}
    }
    
    public void setUnserializer(ProfileUnserializer unserializer) {
	this.unserializer = unserializer;
    }

    public ProfileUnserializer getUnserializer() {
	return unserializer;
    }

    public int setUnserializingPolicy(int unserialPolicy) {
	int o_unserialPolicy = this.unserialPolicy;
	this.unserialPolicy = unserialPolicy;
	return o_unserialPolicy;
    }

    public int setUnserializingPolicy(int unserialPolicy, int max_probe_cnt) {
	int o_unserialPolicy = this.unserialPolicy;
	this.unserialPolicy = unserialPolicy;
	this.max_probe_cnt = max_probe_cnt;
	return o_unserialPolicy;
    }

    public int getUnserializingPolicy() {
	return unserialPolicy;
    }

    public void setChrPos(int chr, long pos) {
	chrPosMap[chr-1] = pos;
    }

    public long getChrPos(int chr) {
	if (isChr) {
	    return 0;
	}

	return chrPosMap[chr-1];
    }

    public long[] getChrPosMap() {
	return chrPosMap;
    }

    public void setChrPosMap(long chrPosMap[]) {
	this.chrPosMap = chrPosMap;
	chr_cnt = getChrCount();
    }

    public int getChrCount() {
	if (chr_cnt != 0) {
	    return chr_cnt;
	}

	if (isChr) {
	    return 1;
	}

	int n = 0;
	for (; n < chrPosMap.length; n++) {
	    if (chrPosMap[n] == -1) {
		break;
	    }
	}

	return n;
    }

    public void setChrBegProbeNum(int chr, int num) {
	chrBegProbeNumMap[chr-1] = num;
    }

    public void setChrEndProbeNum(int chr, int num) {
	chrEndProbeNumMap[chr-1] = num;
    }

    public long getChrBegProbeNum(int chr) {
	if (isChr) {
	    return 0;
	}
	return chrBegProbeNumMap[chr-1];
    }

    public long getChrEndProbeNum(int chr) {
	if (isChr) {
	    return 0;
	}
	return chrEndProbeNumMap[chr-1];
    }

    public int[] getChrBegProbeNumMap() {
	return chrBegProbeNumMap;
    }

    public int[] getChrEndProbeNumMap() {
	return chrEndProbeNumMap;
    }

    public void setChrBegProbeNumMap(int chrBegProbeNumMap[]) {
	this.chrBegProbeNumMap = chrBegProbeNumMap;
    }

    public void setChrEndProbeNumMap(int chrEndProbeNumMap[]) {
	this.chrEndProbeNumMap = chrEndProbeNumMap;
    }

    public Profile asProfile() {
	return this;
    }

    public boolean isFullImported() {return full_imported;}

    public void setFullImported(boolean full_imported) {
	this.full_imported = full_imported;
    }

    public void setFileName(String filename) {
	this.filename = filename;
    }

    public String getFileName() {
	return filename;
    }

    public SmoothingLineEngine.SmoothingInfo getSmoothingInfo() {
	return new SmoothingLineEngine.SmoothingInfo(this);
    }

    // bad cyclic dependance
    GraphicProfile graphicProfile;

    public GraphicProfile getGraphicProfile() {
	return graphicProfile;
    }

    public void setGraphicProfile(GraphicProfile graphicProfile) {
	this.graphicProfile = graphicProfile;
    }

    int getDataCount() {return getProbeCount();}

    public RODataElementProxy getDataProxy(int n) throws Exception {return getDataProxy(n, false);}

    public RODataElementProxy getDataProxy(int n, boolean load_props) throws Exception {return getProbe(n, load_props);}

    public void close() {
	if (unserializer != null) {
	    try {
		unserializer.close();
		unserializer = null;
	    }
	    catch(Exception e) {
	    }
	}
    }
}

