
package fr.curie.vamp.data.serial;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.utils.serial.*;
import fr.curie.vamp.Property;

import java.io.*;
import java.util.*;

public class ProfilePortableSerializer extends ProfileSerializer {

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
    private int last_probe_pos;
    private int probe_num;
    private int version;

    private long maxX = Long.MIN_VALUE;

    public ProfilePortableSerializer(Profile profile, long chrPos[], String file, boolean pangen) throws Exception {
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

	BufferedFileOutputStream fos = getFOSGraph();

	SerialUtils.writeLong(fos, ProfileSerialUtils.PORTABLE_MAGIC);
	SerialUtils.writeInt(fos, version);
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

    public void writeHeader() throws Exception {
	BufferedFileOutputStream fos = getFOSGraph();

	SerialUtils.writeString(fos, profile.getName());

	probe_cnt_off_pos = (int)fos.position();
	SerialUtils.writeInt(fos, profile.getProbeCount());

	type_map_off_pos = (int)fos.position();
	SerialUtils.writeInt(fos, 0); // type_map_off

	bkp_out_smt_off_pos = (int)fos.position();
	SerialUtils.writeInt(fos, 0); // bkp_out_smt_off

	// precompiled information for algorithms (future use)
	for (int n = 0; n < tools_info_off_pos.length; n++) {
	    tools_info_off_pos[n] = (int)fos.position();
	    SerialUtils.writeInt(fos, 0); // tools_info_off[n]
	}

	// extension info (future use)
	ext_info_off_pos = (int)fos.position();
	SerialUtils.writeInt(fos, 0); // ext_off

	minX_pos = (int)fos.position();
	SerialUtils.writeLong(fos, 0); // profile.getMinX());

	maxX_pos = (int)fos.position();
	SerialUtils.writeLong(fos, 0); // profile.getMaxX());

	minY_pos = (int)fos.position();
	SerialUtils.writeDouble(fos, 0.); //profile.getMinY());

	maxY_pos = (int)fos.position();
	SerialUtils.writeDouble(fos, 0.); // profile.getMaxY());

	probe_off_pos = (int)fos.position();
	SerialUtils.writeInt(fos, 0);

	probe_size_pos = (int)fos.position();
	SerialUtils.writeInt(fos, 0);

	prop_map_off_pos = (int)fos.position();
	SerialUtils.writeInt(fos, 0); // prop_map_off

	chr_pos_map_pos = (int)fos.position();
	write(fos, profile.getChrPosMap());

	chr_probe_num_map_pos = (int)fos.position();
	write(fos, profile.getChrBegProbeNumMap());
	write(fos, profile.getChrEndProbeNumMap());
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

	// new code begin
	int type_map_off = (int)fos.position();
	writeTypeMap(fos, typeMap);
	int prop_map_off = (int)fos.position();
	int bkp_out_smt_off = (int)fos.position(); // no more useful
	fos.position(type_map_off_pos);
	SerialUtils.writeInt(fos, type_map_off);
	// new code end

	fos.position(prop_map_off);
	writePropMap(fos, version, profile.getPropMap());
	bkp_out_smt_off = (int)fos.position(); // override value
	fos.position(prop_map_off_pos);
	SerialUtils.writeInt(fos, prop_map_off);

	// new code begin
	fos.position(probe_cnt_off_pos);
	SerialUtils.writeInt(fos, profile.getProbeCount());
	// new code end

	// new new code begin
	fos.position(bkp_out_smt_off);
	write(fos, bkp);
	write(fos, out);
	write(fos, smt);
	// get position of 1st tool (future use, here just for example)
	tools_info_off[0] = (int)fos.position();
	fos.position(bkp_out_smt_off_pos);
	SerialUtils.writeInt(fos, bkp_out_smt_off);
	// new new code end

	// tools (future use, here just for example)
	fos.position(tools_info_off_pos[0]);
	SerialUtils.writeInt(fos, tools_info_off[0]);

	/*
	System.out.println("type_map_pos " + type_map_off_pos + " " + type_map_off);
	System.out.println("minX: " + profile.getMinX());
	System.out.println("maxX: " + profile.getMaxX());
	System.out.println("minY: " + profile.getMinY());
	System.out.println("maxY: " + profile.getMaxY());
	System.out.println("probe_off: " + probe_off);
	System.out.println("probe_size " + probe_size);
	*/

	fos.position(minX_pos);
	SerialUtils.writeLong(fos, profile.getMinX());

	//System.out.println("maxX: " + profile.getMaxX() + " " + pangen);

	fos.position(maxX_pos);
	/*
	Probe p = profile.getProbe(profile.getProbeCount()-1);
	profile.setMaxX((pangen ? p.getPanGenPos(profile) : p.getPos()) + p.getSize());
	System.out.println("SETTING MAXX: " + profile.getProbeCount() + " " +
			   (pangen ? p.getPanGenPos(profile) : p.getPos()) + " " + p.getSize() + " vs. " + maxX); 
	*/

	SerialUtils.writeLong(fos, profile.getMaxX());

	fos.position(minY_pos);
	SerialUtils.writeDouble(fos, profile.getMinY());

	fos.position(maxY_pos);
	SerialUtils.writeDouble(fos, profile.getMaxY());

	fos.position(probe_off_pos);
	SerialUtils.writeInt(fos, probe_off);

	fos.position(probe_size_pos);
	SerialUtils.writeInt(fos, probe_size);

	// MIND !!!!!!
	//fos.verbose(true);
	fos.position(chr_pos_map_pos);
	//oos.writeObject(profile.getChrPosMap());
	write(fos, profile.getChrPosMap());
	//System.out.println("size2 " + (fos.position() - chr_pos_map_pos) + " " + profile.getChrPosMap().length);

	fos.position(chr_probe_num_map_pos);
	write(fos, profile.getChrBegProbeNumMap());
	write(fos, profile.getChrEndProbeNumMap());
    }

    public void writeProbe(Probe p) throws Exception {
	BufferedFileOutputStream fos = getFOSGraph();

	if (probe_off == 0) {
	    probe_off = (int)fos.position();
	}
	else if (probe_size == 0) {
	    int off = (int)fos.position();
	    probe_size = off - probe_off;
	}

	BufferedFileOutputStream fosProp = getFOSProp();

	long mX = (pangen ? p.getPanGenPos(profile) : p.getPos()) + p.getSize();
	if (mX > maxX) {
	    maxX = mX;
	}

	int prop_off = (int)fosProp.position();
	//assert prop_off == (int)fosProp.getChannel().position();
	p.setPropertyOffset(prop_off);

	writePropMap(fosProp, version, p.getPropMap());

	SerialUtils.writeInt(fos, p.getPos());
	SerialUtils.writeInt(fos, p.getSize());

	SerialUtils.writeInt(fos, p.getTypOutBkpGnlChr());

	SerialUtils.writeFloat(fos, p.getPassThruPosY());

	SerialUtils.writeFloat(fos, p.getPassThruRatio());

	SerialUtils.writeFloat(fos, p.getSmoothing());
	SerialUtils.writeInt(fos, prop_off);

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

    static private void writeTypeMap(BufferedFileOutputStream fos, HashMap<String, Integer> typeMap) throws Exception {
	SerialUtils.writeMap(fos, typeMap);
	/*
	short size = (short)typeMap.size();

	oos.writeShort(size);

	Iterator it = typeMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String, Integer> entry = (Map.Entry)it.next();
	    oos.writeInt(entry.getValue().intValue());
	    oos.writeObject(entry.getKey());
	}
	*/
    }

    static private void writePropMap(BufferedFileOutputStream fos, int version, HashMap<Property, Object> propMap) throws Exception {

	SerialUtils.writeMap(fos, propMap);

	/*
	if (propMap == null) {
	    SerialUtils.writeShort(fos, 0);
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
	*/
    }

    static private void write(BufferedFileOutputStream fos, int arr[]) throws Exception {
	SerialUtils.writeInt(fos, arr.length);
	for (int n = 0; n < arr.length; n++) {
	    SerialUtils.writeInt(fos, arr[n]);
	}
    }

    static private void write(BufferedFileOutputStream fos, long arr[]) throws Exception {
	SerialUtils.writeInt(fos, arr.length);
	for (int n = 0; n < arr.length; n++) {
	    SerialUtils.writeLong(fos, arr[n]);
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
