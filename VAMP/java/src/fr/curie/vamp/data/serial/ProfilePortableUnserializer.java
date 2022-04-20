
package fr.curie.vamp.data.serial;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.utils.serial.*;
import fr.curie.vamp.Property;
import java.io.*;
import java.util.*;

public class ProfilePortableUnserializer extends ProfileUnserializer {

    private Profile profile;
    private UnserializingContext serialCtx;

    private int probe_off, probe_size;
    private int version;
    private String filename;
    private Object profileID;

    static private boolean PROPMAP_VERBOSE = false;

    public ProfilePortableUnserializer(String filename) throws Exception {
	this.filename = filename;
	serialCtx = new UnserializingContext(filename,
					     new String[]
	    {ProfileSerialUtils.DISPLAY_SUFFIX,
	     ProfileSerialUtils.PROP_SUFFIX});

	FileInputStream fis = getFISGraph();
	version = SerialUtils.readInt(fis);
    }

    public ProfilePortableUnserializer cloneRealize() throws Exception {
	ProfilePortableUnserializer profUnserial = new ProfilePortableUnserializer(filename);

	profUnserial.profile = profile;
	profUnserial.probe_off = probe_off;
	profUnserial.probe_size = probe_size;
	profUnserial.profileID = profileID;

	return profUnserial;
    }

    public int getVersion() {
	return version;
    }

    private FileInputStream getFISGraph() {
	return serialCtx.getFIS(ProfileSerialUtils.DISPLAY_INDEX);
    }

    private FileInputStream getFISProp() {
	return serialCtx.getFIS(ProfileSerialUtils.PROP_INDEX);
    }

    public Profile readProfile() throws Exception {
	return readProfile(true);
    }
	
    public Profile readProfile(boolean full_imported) throws Exception {
	FileInputStream fis = getFISGraph();

	SerialUtils.readLong(fis); // magic
	SerialUtils.readInt(fis); // version
	String name = SerialUtils.readString(fis);
	int probe_cnt = SerialUtils.readInt(fis);

	int type_map_off = SerialUtils.readInt(fis);
	int bkp_out_smt_off = SerialUtils.readInt(fis);

	// precompiled information for algorithms (future use)
	int tools_info_pos[] = new int[ProfileSerialUtils.TOOLS_INFO_CNT];
	for (int n = 0; n < tools_info_pos.length; n++) {
	    tools_info_pos[n] = SerialUtils.readInt(fis);
	}

	// extension info (future use)
	int ext_off = SerialUtils.readInt(fis);

	long minX = SerialUtils.readLong(fis);
	long maxX = SerialUtils.readLong(fis);
	double minY = SerialUtils.readDouble(fis);
	double maxY = SerialUtils.readDouble(fis);

	probe_off = SerialUtils.readInt(fis);
	probe_size = SerialUtils.readInt(fis);

	/*
	System.out.println("minX: " + minX);
	System.out.println("maxX: " + maxX);
	System.out.println("minY: " + minY);
	System.out.println("maxY: " + maxY);
	System.out.println("probe_off: " + probe_off);
	System.out.println("probe_size: " + probe_size);
	*/

	profile = new Profile(name, probe_cnt, full_imported);
	profile.setFileName(filename);

	HashMap<Property, Object> propMap = null;

	int prop_map_off = 0;

	prop_map_off = SerialUtils.readInt(fis);

	long[] chrPosMap;

	//chrPosMap = (long[])(SerialUtils.readObject(fis));
	chrPosMap = readLongArray(fis);

	int[] chrBegProbeNumMap = readIntArray(fis);
	int[] chrEndProbeNumMap = readIntArray(fis);

	profile.setMinX(minX);
	profile.setMaxX(maxX);
	profile.setMinY(minY);
	profile.setMaxY(maxY);

	//profile.setProperties(propMap);
	profile.setChrPosMap(chrPosMap);
	profile.setChrBegProbeNumMap(chrBegProbeNumMap);
	profile.setChrEndProbeNumMap(chrEndProbeNumMap);

	fis.getChannel().position(prop_map_off);
	propMap = readPropMap(fis, version);

	profile.setProperties(propMap);

	// new code begin
	fis.getChannel().position(type_map_off);
	HashMap<Integer, String> typeMap = readTypeMap(fis);
	profile.setTypeMap(typeMap);
	// new code end

	// new new code begin
	fis.getChannel().position(bkp_out_smt_off);
	int bkp[] = readIntArray(fis);
	int out[] = readIntArray(fis);
	int smt[] = readIntArray(fis);
	profile.setBkpOutSmt(bkp, out, smt);
	// new new code end
	
	profile.setUnserializer(this);

	profileID = profile.getID();

	return profile;
    }

    public Probe getProbe(int n) throws Exception {
	FileInputStream fis = getFISGraph();

	fis.getChannel().position(probe_off + n * probe_size);

	int pos = SerialUtils.readInt(fis);
	int size = SerialUtils.readInt(fis);
	int typ_out_bkp_gnl_chr;

	typ_out_bkp_gnl_chr = SerialUtils.readInt(fis);

	float posy = SerialUtils.readFloat(fis);
	float ratio = Float.MAX_VALUE;
	ratio = SerialUtils.readFloat(fis);
	float smt = SerialUtils.readFloat(fis);
	int prop_off = SerialUtils.readInt(fis);

	Probe p = new Probe();

	p.setPos(pos);
	p.setSize(size);
	p.setTypOutBkpGnlChr(typ_out_bkp_gnl_chr);
	p.setPosY(posy);
	p.setRatio(ratio);
	p.setSmoothing(smt);
	p.setPropertyOffset(prop_off);

	return p;
    }

    public void complete(Probe p) throws Exception {
	FileInputStream fisProp = getFISProp();

	if (!p.isCompletable()) {
	    throw new Exception("Probe not completable: invalid property offset");
	}

	fisProp.getChannel().position(p.getPropertyOffset());
	p.setPropMap(readPropMap(fisProp, version));
	if (p.getProp(fr.curie.vamp.VAMPProperties.ArrayProp) == null) {
	    p.addProp(fr.curie.vamp.VAMPProperties.ArrayProp, profileID);
	}
    }

    static private HashMap<Integer, String> readTypeMap(FileInputStream fis) throws Exception {
	return (HashMap<Integer, String>)SerialUtils.readMap(fis);
    }

    static private HashMap<Property, Object> readPropMap(FileInputStream fis, int version) throws Exception {
	return (HashMap<Property, Object>)SerialUtils.readMap(fis);
    }

    static int[] readIntArray(FileInputStream fis) throws Exception {
	int length = SerialUtils.readInt(fis);
	int arr[] = new int[length];
	for (int n = 0; n < length; n++) {
	    arr[n] = SerialUtils.readInt(fis);
	}

	return arr;
    }

    static long[] readLongArray(FileInputStream fis) throws Exception {
	int length = SerialUtils.readInt(fis);
	long arr[] = new long[length];
	for (int n = 0; n < length; n++) {
	    arr[n] = SerialUtils.readLong(fis);
	}

	return arr;
    }

    public void close() throws Exception {
	//System.out.println("closing unserializer " + this);
	if (serialCtx != null) {
	    serialCtx.close();
	    serialCtx = null;
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
