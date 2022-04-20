
package fr.curie.vamp.data.serial;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.utils.serial.*;
import fr.curie.vamp.Property;

import java.io.*;
import java.util.*;

public class ProfileNativeJavaSerializer extends ProfileSerializer {

    //private Profile profile;
    private long chrPos[];
    //private SerializingContext serialCtx;
    private SerializingContext serialCtxGraph;
    private SerializingContext serialCtxProp;
    private int prop_map_off_pos;
    private int probe_cnt_off_pos;
    private int type_map_off_pos;
    private int bkp_out_smt_off_pos;
    private int tools_info_off_pos[] = new int[ProfileSerialUtils.TOOLS_INFO_CNT];
    private int tools_info_off[] = new int[ProfileSerialUtils.TOOLS_INFO_CNT];
    private int ext_info_off_pos;
    private int ext_info_off;
    private int minX_pos, maxX_pos, minY_pos, maxY_pos;
    private int probe_off_pos, probe_size_pos;
    private int chr_pos_map_pos, chr_probe_num_map_pos;

    private int probe_off, probe_size;
    private int last_chr;
    //private long chr_off_pos;
    private int last_probe_pos;
    private int probe_num;
    private int version;
    //private boolean pangen;

    private long maxX = Long.MIN_VALUE;

    /*
    public ProfileNativeJavaSerializer(Profile profile, long chrPos[]) throws Exception {
	this(profile, chrPos, profile.getName());
    }

    public ProfileNativeJavaSerializer(Profile profile, long chrPos[], String file) throws Exception {
	this(profile, chrPos, file, true);
    }
    */

    public ProfileNativeJavaSerializer(Profile profile, long chrPos[], String file, boolean pangen) throws Exception {
	super(profile, pangen, file);
	this.profile = profile;
	this.chrPos = chrPos;
	this.pangen = pangen;

	last_chr = 0;
	//chr_off_pos = 0;
	last_probe_pos = 0;
	probe_num = 0;
	version = ProfileSerialUtils.SERIAL_VERSION;

	serialCtxGraph = new SerializingContext(file, // profile.getName(),
						new String[]
	    {ProfileSerialUtils.DISPLAY_SUFFIX}, SerializingContext.BUFFERED);

	serialCtxProp = new SerializingContext(file, // profile.getName(),
					       new String[]
	    {ProfileSerialUtils.PROP_SUFFIX}, SerializingContext.BUFFERED);

	probe_off = 0;
	probe_size = 0;

	ObjectOutputStream oos = getOOSGraph();

	oos.writeInt(version);
    }

    public int getVersion() {
	return version;
    }

    private BufferedFileOutputStream getFOSGraph() {
	//	return serialCtxGraph.getBFOS(ProfileSerialUtils.DISPLAY_INDEX);
	return serialCtxGraph.getBFOS(0);
    }

    private BufferedFileOutputStream getFOSProp() {
	//return serialCtxProp.getFOS(ProfileSerialUtils.PROP_INDEX);
	return serialCtxProp.getBFOS(0);
    }

    private ObjectOutputStream getOOSGraph() {
	//return serialCtxGraph.getOOS(ProfileSerialUtils.DISPLAY_INDEX);
	return serialCtxGraph.getOOS(0);
    }

    private ObjectOutputStream getOOSProp() {
	//return serialCtxProp.getOOS(ProfileSerialUtils.PROP_INDEX);
	return serialCtxProp.getOOS(0);
    }

    private ObjectOutputStream getROpenOOSGraph() throws Exception {
	//return serialCtxGraph.getROpenOOS(ProfileSerialUtils.DISPLAY_INDEX);
	return serialCtxGraph.getROpenOOS(0);
    }

    private ObjectOutputStream getROpenOOSProp() throws Exception {
	//return serialCtxProp.getROpenOOS(ProfileSerialUtils.PROP_INDEX);
	return serialCtxProp.getROpenOOS(0);
    }

    public void writeHeader() throws Exception {
	BufferedFileOutputStream fos = getFOSGraph();
	ObjectOutputStream oos = getOOSGraph();

	oos.writeObject(profile.getName());
	oos.flush();

	probe_cnt_off_pos = (int)fos.position();
	oos.writeInt(profile.getProbeCount());

	oos.flush();
	type_map_off_pos = (int)fos.position();
	oos.writeInt(0); // type_map_off

	oos.flush();
	bkp_out_smt_off_pos = (int)fos.position();
	oos.writeInt(0); // bkp_out_smt_off

	// precompiled information for algorithms (future use)
	for (int n = 0; n < tools_info_off_pos.length; n++) {
	    oos.flush();
	    tools_info_off_pos[n] = (int)fos.position();
	    oos.writeInt(0); // tools_info_off[n]
	}

	// extension info (future use)
	oos.flush();
	ext_info_off_pos = (int)fos.position();
	oos.writeInt(0); // ext_off

	oos.flush();
	minX_pos = (int)fos.position();
	oos.writeLong(0); // profile.getMinX());

	oos.flush();
	maxX_pos = (int)fos.position();
	oos.writeLong(0); // profile.getMaxX());

	oos.flush();
	minY_pos = (int)fos.position();
	oos.writeDouble(0); //profile.getMinY());

	oos.flush();
	maxY_pos = (int)fos.position();
	oos.writeDouble(0); // profile.getMaxY());

	oos.flush();
	probe_off_pos = (int)fos.position();
	oos.writeInt(0);

	oos.flush();
	probe_size_pos = (int)fos.position();
	oos.writeInt(0);

	oos.flush(); // ??

	if (ProfileSerialUtils.SERIAL_VERSION > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    prop_map_off_pos = (int)fos.position();
	    oos.writeInt(0); // prop_map_off
	    oos.flush();
	}
	else {
	    writePropMap(oos, version, profile.getPropMap());
	    oos.flush();
	}

	chr_pos_map_pos = (int)fos.position();
	//oos.writeObject(profile.getChrPosMap());
	write(oos, profile.getChrPosMap());
	oos.flush();
	//System.out.println("size " + (fos.position() - chr_pos_map_pos));

	chr_probe_num_map_pos = (int)fos.position();
	write(oos, profile.getChrBegProbeNumMap());
	write(oos, profile.getChrEndProbeNumMap());
	oos.flush();
    }

    public void writeFooter(HashMap<String, Integer> typeMap,
			    int bkp[], int out[], int smt[]) throws Exception {

	if (last_chr > 0) {
	    profile.setChrEndProbeNum(last_chr, probe_num - 1);
	}

	for (int n = 0; n < chrPos.length; n++) {
	    profile.setChrPos(n+1, chrPos[n]);
	}

	profile.setChrPos(chrPos.length+1, -1);

	//profile.setChrBegProbeNum(last_chr + 1, -1);

	BufferedFileOutputStream fos = getFOSGraph();
	ObjectOutputStream oos = getROpenOOSGraph();

	// new code begin
	int type_map_off = (int)fos.position();
	oos.flush();
	writeTypeMap(oos, typeMap);
	oos.flush();
	int prop_map_off = (int)fos.position();
	int bkp_out_smt_off = (int)fos.position(); // no more useful
	fos.position(type_map_off_pos);
	oos.writeInt(type_map_off);
	oos.flush();
	// new code end

	if (ProfileSerialUtils.SERIAL_VERSION > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    // new propmap begin
	    //oos.flush();
	    fos.position(prop_map_off);
	    writePropMap(oos, version, profile.getPropMap());
	    oos.flush();
	    bkp_out_smt_off = (int)fos.position(); // override value
	    fos.position(prop_map_off_pos);
	    oos.writeInt(prop_map_off);
	    oos.flush();
	    // new propmap end
	}

	// new code begin
	fos.position(probe_cnt_off_pos);
	oos.writeInt(profile.getProbeCount());
	oos.flush();
	// new code end

	// new new code begin
	fos.position(bkp_out_smt_off);
	write(oos, bkp);
	write(oos, out);
	write(oos, smt);
	oos.flush();
	// get position of 1st tool (future use, here just for example)
	tools_info_off[0] = (int)fos.position();
	fos.position(bkp_out_smt_off_pos);
	oos.writeInt(bkp_out_smt_off);
	oos.flush();
	// new new code end

	// tools (future use, here just for example)
	fos.position(tools_info_off_pos[0]);
	oos.writeInt(tools_info_off[0]);
	oos.flush();

	/*
	System.out.println("type_map_pos " + type_map_off_pos + " " + type_map_off);
	System.out.println("bkp_out_smt_off " + bkp_out_smt_off + ", " + bkp_out_smt);
	System.out.println("minX: " + profile.getMinX());
	System.out.println("maxX: " + profile.getMaxX());
	System.out.println("minY: " + profile.getMinY());
	System.out.println("maxY: " + profile.getMaxY());
	System.out.println("probe_off: " + probe_off);
	System.out.println("probe_size " + probe_size);
	*/

	fos.position(minX_pos);
	oos.writeLong(profile.getMinX());
	oos.flush();

	//System.out.println("maxX: " + profile.getMaxX() + " " + pangen);

	fos.position(maxX_pos);
	/*
	Probe p = profile.getProbe(profile.getProbeCount()-1);
	profile.setMaxX((pangen ? p.getPanGenPos(profile) : p.getPos()) + p.getSize());
	System.out.println("SETTING MAXX: " + profile.getProbeCount() + " " +
			   (pangen ? p.getPanGenPos(profile) : p.getPos()) + " " + p.getSize() + " vs. " + maxX); 
	*/

	oos.writeLong(profile.getMaxX());
	oos.flush();

	fos.position(minY_pos);
	oos.writeDouble(profile.getMinY());
	oos.flush();

	fos.position(maxY_pos);
	oos.writeDouble(profile.getMaxY());
	oos.flush();

	fos.position(probe_off_pos);
	oos.writeInt(probe_off);
	oos.flush();

	fos.position(probe_size_pos);
	oos.writeInt(probe_size);
	oos.flush();

	// MIND !!!!!!
	//fos.verbose(true);
	fos.position(chr_pos_map_pos);
	//oos.writeObject(profile.getChrPosMap());
	write(oos, profile.getChrPosMap());
	oos.flush();
	//System.out.println("size2 " + (fos.position() - chr_pos_map_pos) + " " + profile.getChrPosMap().length);

	fos.position(chr_probe_num_map_pos);
	write(oos, profile.getChrBegProbeNumMap());
	write(oos, profile.getChrEndProbeNumMap());
	oos.flush();
    }

    public void writeProbe(Probe p) throws Exception {
	BufferedFileOutputStream fos = getFOSGraph();
	ObjectOutputStream oos = getOOSGraph();
	oos.flush();

	if (probe_off == 0) {
	    probe_off = (int)fos.position();
	}
	else if (probe_size == 0) {
	    oos.flush();
	    int off = (int)fos.position();
	    probe_size = off - probe_off;
	}

	BufferedFileOutputStream fosProp = getFOSProp();
	ObjectOutputStream oosProp = getROpenOOSProp();

	long mX = (pangen ? p.getPanGenPos(profile) : p.getPos()) + p.getSize();
	if (mX > maxX) {
	    maxX = mX;
	}

	int prop_off = (int)fosProp.position();
	//assert prop_off == (int)fosProp.getChannel().position();
	p.setPropertyOffset(prop_off);

	writePropMap(oosProp, version, p.getPropMap());

	oos.writeInt(p.getPos());
	oos.writeInt(p.getSize());

	if (ProfileSerialUtils.SERIAL_VERSION > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    oos.writeInt(p.getTypOutBkpGnlChr());
	}
	else {
	    oos.writeShort(p.getTypOutBkpGnlChr());
	}

	oos.writeFloat(p.getPassThruPosY());

	if (ProfileSerialUtils.SERIAL_VERSION > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    oos.writeFloat(p.getPassThruRatio());
	}

	oos.writeFloat(p.getSmoothing());
	oos.writeInt(prop_off);

	if (p.getChr() != last_chr) {
	    if (last_chr != 0) {
		profile.setChrEndProbeNum(last_chr, probe_num-1);
	    }
	    profile.setChrBegProbeNum(p.getChr(), probe_num);
	    last_chr = p.getChr();
	}

	last_probe_pos = p.getPos();
	probe_num++;

	profile.setMinMaxX(p.getPos(), p.getSize());
	if (p.getPosY(profile) != Float.MIN_VALUE) {
	    profile.setMinMaxY(p.getPosY(profile));
	}
    }

    public void close() throws Exception {
	if (serialCtxGraph != null) {
	    serialCtxGraph.close();
	    serialCtxProp.close();

	    serialCtxGraph = null;
	    serialCtxProp = null;
	}
    }

    static private void writeTypeMap(ObjectOutputStream oos, HashMap<String, Integer> typeMap) throws Exception {
	short size = (short)typeMap.size();

	oos.writeShort(size);

	Iterator it = typeMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String, Integer> entry = (Map.Entry)it.next();
	    oos.writeInt(entry.getValue().intValue());
	    oos.writeObject(entry.getKey());
	}
    }

    static private void writePropMap(ObjectOutputStream oos, int version, HashMap<Property, Object> propMap) throws Exception {

	if (propMap == null) {
	    oos.writeShort((short)0);
	    return;
	}

	short size = (short)propMap.size();

	oos.writeShort(size);
	oos.reset();

	Iterator it = propMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<Property, Object> entry = (Map.Entry)it.next();
	    oos.writeObject(entry.getKey().getName());
	    oos.writeObject(entry.getValue());
	    oos.reset();
	}
    }

    static private void write(ObjectOutputStream oos, int arr[]) throws Exception {
	oos.writeInt(arr.length);
	for (int n = 0; n < arr.length; n++) {
	    oos.writeInt(arr[n]);
	}
    }

    static private void write(ObjectOutputStream oos, long arr[]) throws Exception {
	oos.writeInt(arr.length);
	for (int n = 0; n < arr.length; n++) {
	    oos.writeLong(arr[n]);
	}
    }

    public void finalize() {
	try {
	    close();
	}
	catch(Exception e) {
	}
    }
}
