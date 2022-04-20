
package fr.curie.vamp.data.serial;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.utils.serial.*;
import fr.curie.vamp.Property;
import fr.curie.vamp.ImportDataDialog;
import java.io.*;
import java.util.*;

public class ProfileNativeJavaUnserializer extends ProfileUnserializer {

    private Profile profile;
    private UnserializingContext serialCtx;

    private int probe_off, probe_size;
    private int version;
    private String serialFile;
    private Object profileID;

    static private boolean PROPMAP_VERBOSE = false;

    public ProfileNativeJavaUnserializer(String serialFile) throws Exception {
	this.serialFile = serialFile;
	ImportDataDialog.lockFile(serialFile);
	serialCtx = new UnserializingContext(serialFile,
					     new String[]
	    {ProfileSerialUtils.DISPLAY_SUFFIX,
	     ProfileSerialUtils.PROP_SUFFIX});

	ObjectInputStream ois = getOISGraph();
	version = ois.readInt();
    }

    public ProfileNativeJavaUnserializer cloneRealize() throws Exception {
	ProfileNativeJavaUnserializer profUnserial = new ProfileNativeJavaUnserializer(serialFile);

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

    private ObjectInputStream getOISGraph() {
	return serialCtx.getOIS(ProfileSerialUtils.DISPLAY_INDEX);
    }

    private ObjectInputStream getOISProp() {
	return serialCtx.getOIS(ProfileSerialUtils.PROP_INDEX);
    }

    public Profile readProfile() throws Exception {
	return readProfile(true);
    }
	
    public Profile readProfile(boolean full_imported) throws Exception {
	FileInputStream fis = getFISGraph();
	ObjectInputStream ois = getOISGraph();

	ois.readInt(); // version
	String name = (String)(ois.readObject());
	int probe_cnt = ois.readInt();

	int type_map_off = ois.readInt();
	int bkp_out_smt_off = ois.readInt();

	// precompiled information for algorithms (future use)
	int tools_info_pos[] = new int[ProfileSerialUtils.TOOLS_INFO_CNT];
	for (int n = 0; n < tools_info_pos.length; n++) {
	    tools_info_pos[n] = ois.readInt();
	}

	// extension info (future use)
	int ext_off = ois.readInt();

	long minX = ois.readLong();
	long maxX = ois.readLong();
	double minY = ois.readDouble();
	double maxY = ois.readDouble();

	probe_off = ois.readInt();
	probe_size = ois.readInt();

	/*
	System.out.println("minX: " + minX);
	System.out.println("maxX: " + maxX);
	System.out.println("minY: " + minY);
	System.out.println("maxY: " + maxY);
	System.out.println("probe_off: " + probe_off);
	System.out.println("probe_size: " + probe_size);
	*/

	profile = new Profile(name, probe_cnt, full_imported);
	profile.setFileName(serialFile);

	HashMap<Property, Object> propMap = null;

	int prop_map_off = 0;

	if (version > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    prop_map_off = ois.readInt();
	}
	else {
	    propMap = readPropMap(ois, version);
	}

	long[] chrPosMap;

	if (version <= ProfileSerialUtils.CHR_POS_MAP_SERIAL_VERSION) {
	    chrPosMap = (long[])(ois.readObject());
	}
	else {
	    chrPosMap = readLongArray(ois);
	}

	int[] chrBegProbeNumMap = readIntArray(ois);
	int[] chrEndProbeNumMap = readIntArray(ois);

	profile.setMinX(minX);
	profile.setMaxX(maxX);
	profile.setMinY(minY);
	profile.setMaxY(maxY);

	//profile.setProperties(propMap);
	profile.setChrPosMap(chrPosMap);
	profile.setChrBegProbeNumMap(chrBegProbeNumMap);
	profile.setChrEndProbeNumMap(chrEndProbeNumMap);

	if (version > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    // for testing
	    /*
	    fis.getChannel().position(bkp_out_smt_off);
	    int bkp2[] = readIntArray(ois);
	    int out2[] = readIntArray(ois);
	    int smt2[] = readIntArray(ois);
	    */

	    fis.getChannel().position(prop_map_off);
	    propMap = readPropMap(ois, version);
	}

	profile.setProperties(propMap);

	// new code begin
	fis.getChannel().position(type_map_off);
	HashMap<Integer, String> typeMap = readTypeMap(ois);
	profile.setTypeMap(typeMap);
	// new code end

	// new new code begin
	fis.getChannel().position(bkp_out_smt_off);
	int bkp[] = readIntArray(ois);
	int out[] = readIntArray(ois);
	int smt[] = readIntArray(ois);
	profile.setBkpOutSmt(bkp, out, smt);
	// new new code end
	
	profile.setUnserializer(this);

	profileID = profile.getID();

	return profile;
    }

    public Probe getProbe(int n) throws Exception {
	FileInputStream fis = getFISGraph();
	ObjectInputStream ois = getOISGraph();

	fis.getChannel().position(probe_off + n * probe_size);

	int pos = ois.readInt();
	int size = ois.readInt();
	int typ_out_bkp_gnl_chr;

	if (version > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    typ_out_bkp_gnl_chr = ois.readInt();
	}
	else {
	    typ_out_bkp_gnl_chr = ois.readShort();
	    typ_out_bkp_gnl_chr &= 0xFFFF;
	}

	float posy = ois.readFloat();
	float ratio = Float.MAX_VALUE;
	if (version > ProfileSerialUtils.PROPMAP_HEADER_SERIAL_VERSION) {
	    ratio = ois.readFloat();
	}
	float smt = ois.readFloat();
	int prop_off = ois.readInt();

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
	ObjectInputStream oisProp = getOISProp();

	if (!p.isCompletable()) {
	    throw new Exception("Probe not completable: invalid property offset");
	}

	fisProp.getChannel().position(p.getPropertyOffset());
	p.setPropMap(readPropMap(oisProp, version));
	if (p.getProp(fr.curie.vamp.VAMPProperties.ArrayProp) == null) {
	    p.addProp(fr.curie.vamp.VAMPProperties.ArrayProp, profileID);
	}
    }

    static private HashMap<Integer, String> readTypeMap(ObjectInputStream ois) throws Exception {
	short mapsize = ois.readShort();
	HashMap<Integer, String> typeMap = new HashMap();
	for (int n = 0; n < mapsize; n++) {
	    int key = ois.readInt();
	    String value = (String)(ois.readObject());
	    typeMap.put(key, value);
	}
	return typeMap;
    }

    static private HashMap<Property, Object> readPropMap(ObjectInputStream ois, int version) throws Exception {

	short mapsize = ois.readShort();
	HashMap<Property, Object> propMap = new HashMap();
	for (int n = 0; n < mapsize; n++) {
	    String key = (String)(ois.readObject());
	    Object value = ois.readObject();
	    propMap.put(Property.getProperty(key), value);
	}
	return propMap;
    }

    static int[] readIntArray(ObjectInputStream ois) throws Exception {
	int length = ois.readInt();
	int arr[] = new int[length];
	for (int n = 0; n < length; n++) {
	    arr[n] = ois.readInt();
	}

	return arr;
    }

    static long[] readLongArray(ObjectInputStream ois) throws Exception {
	int length = ois.readInt();
	long arr[] = new long[length];
	for (int n = 0; n < length; n++) {
	    arr[n] = ois.readLong();
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
