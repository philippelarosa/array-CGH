
package fr.curie.vamp.tools;

import fr.curie.vamp.data.*;
import fr.curie.vamp.properties.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.utils.*;
import fr.curie.vamp.gui.optim.*;
import fr.curie.vamp.Property;
import fr.curie.vamp.GlobalContext;
import fr.curie.vamp.Cytoband;
import fr.curie.vamp.MiniMapDataFactory;
import fr.curie.vamp.Chromosome;
import fr.curie.vamp.SystemConfig;
import fr.curie.vamp.VAMP;
import fr.curie.vamp.VAMPUtils;
import fr.curie.vamp.utils.serial.SerialUtils;

import java.net.*;
import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class ImportProfile {

    private static SAXParserFactory factory = SAXParserFactory.newInstance();

    static InputStream openStream(String filename) throws Exception {
	return new FileInputStream(filename);
    }

    ImportProfile(int serial_type, String syscfg, String url, boolean mergeChr) throws Exception {
	VAMP.init();
	GlobalContext globalContext = new GlobalContext(null, null);

	SystemConfig.build(globalContext, syscfg);

	long busyMem = Utils.busyMemory();

	SAXParser saxParser = factory.newSAXParser();
	Handler handler = new Handler(globalContext, serial_type, mergeChr, false);
	InputStream is = openStream(url);

	saxParser.parse(is, handler);

	/*
	System.out.println("bkp_v: " + handler.bkp_v.size());
	for (int n = 0; n < handler.bkp_v.size(); n++) {
	    System.out.print("#" + handler.bkp_v.get(n) + ", ");
	}

	System.out.print("\nout_v: " + handler.out_v.size());
	for (int n = 0; n < handler.out_v.size(); n++) {
	    System.out.print("#" + handler.out_v.get(n) + ", ");
	}

	System.out.print("\nsmt_v: " + handler.smt_v.size());
	for (int n = 0; n < handler.smt_v.size(); n++) {
	    System.out.print("#" + handler.smt_v.get(n) + ", ");
	}

	System.out.println("");
	*/

	//System.out.println("Memory Used: " + Utils.KB(Utils.busyMemory() - busyMem));
	// capture graphicInfo
	
	// DO NOT WORK !
	/*
	GraphicInfoCapturer capturer = new GraphicInfoCapturer(handler.profile);
	capturer.captureAndWrite();
	System.out.println("Total Memory Used: " + Utils.KB(Utils.busyMemory() - busyMem));
	*/
    }

    class Handler extends DefaultHandler {

	GlobalContext globalContext;
	String curData;
	boolean mergeChr;
	boolean verbose;
	Profile profile;
	Probe probe;
	HashMap<Property, Object> profileMap;
	boolean readingProfile;
	boolean readingProbe;
	int probe_cnt;
	int probe_num;
	HashMap<String, Integer> typeMap;
	int last_type;
	Vector<Integer> bkp_v, out_v, smt_v;
	float last_smt = Float.MIN_VALUE;
	int serial_type;

	ProfileSerializer profSerial;

	Handler(GlobalContext globalContext, int serial_type, boolean mergeChr, boolean verbose) {
	    curData = null;
	    profile = null;
	    probe = null;
	    profileMap = new HashMap<Property, Object>();
	    typeMap = new HashMap<String, Integer>();
	    last_type = 0;
	    readingProfile = true;
	    readingProbe = false;
	    probe_num = 0;
	    profSerial = null;
	    bkp_v = new Vector<Integer>();
	    out_v = new Vector<Integer>();
	    smt_v = new Vector<Integer>();
	    this.serial_type = serial_type;
	    this.mergeChr = mergeChr;
	    this.verbose = verbose;
	    this.globalContext = globalContext;
	}

	public void startElement(String namespaceURI,
				 String lName,
				 String qName,
				 Attributes attrs) throws SAXException	{

	    curData = null;

	    if (verbose)
		System.err.print("<" + qName + ">");

	    try {
		if (qName.equals("Obj")) {
		    readingProbe = true;
		    probe = new Probe();
		}

		if (readingProfile && qName.equals("Obj")) {
		    readingProfile = false;
		    String name = (String)profileMap.get(Property.getProperty("Name"));
		    long chrPos[] = getChrPos();
		    if (chrPos == null) {
			System.exit(1);
		    }

		    /*
		    for (int n = 0; n < chrPos.length; n++) {
			System.err.println(chrPos[n]);
		    }
		    */

		    profile = new Profile(name, probe_cnt);
		    //profSerial = new ProfileSerializer(profile, chrPos);
		    profSerial = ProfileSerializerFactory.getInstance().getSerializer(serial_type, profile, chrPos);
		    profile.setPropMap(profileMap);
		    profSerial.writeHeader();
		    System.out.println(name);
		    return;
		}
	    
	    }
	    catch(Exception e) {
		e.printStackTrace();
		System.exit(1);
		return;
	    }
		    
	}

	public void characters(char buf[], int offset, int len)
	    throws SAXException
	{
	    String s = new String(buf, offset, len);
	    if (curData == null)
		curData = s;
	    else
		curData += s;
	}

	public void endElement(String namespaceURI,
			       String sName,
			       String qName)
	    throws SAXException
	{
	    if (curData != null)
		curData = curData.trim();

	    if (verbose) {
		System.err.print(curData);
		System.err.println("</" + qName + ">");
	    }

	    try {
		if (qName.equals("Obj")) {
		    profile.setProbe(probe_num, probe);
		    probe_num++;
		    profSerial.writeProbe(probe);
		    readingProbe = false;
		}
		    
		if (qName.equals("ArraySet")) {
		    profSerial.writeFooter(typeMap, toArray(bkp_v), toArray(out_v), toArray(smt_v));
		    if (probe_num != probe_cnt) {
			System.err.println("ImportProfile error: " + probe_num + " written, " + probe_cnt + " expected (NbObj)");
		    }
		    //profile.release();
		    profSerial.close();
		}
		
		if (qName.equals("NbObj")) {
		    probe_cnt = Integer.parseInt(curData);
		    addProp(profileMap, "Clone Count", new Integer(probe_cnt));
		    return;
		}
		
		if (readingProfile) {
		    if (qName.equals("Type")) {
			if (mergeChr && !curData.endsWith(" Chromosome Merge")) {
			    curData = curData + " Chromosome Merge";
			}
			addProp(profileMap, qName, curData);
		    }
		    else if (qName.equals("PanGenomic")) {
			addProp(profileMap, "Chr", curData);
		    }
		    else if (qName.equals("Chr") && mergeChr) {
			addProp(profileMap, qName, "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24");
		    } else {
			addProp(profileMap, qName, curData);
		    }
		}
		else if (readingProbe) {
		    if (qName.equals("X")) {
			int pos = Integer.parseInt(curData);
			probe.setPos(pos);
			profile.setMinMaxX(pos);
			probe.addProp(Property.getProperty("Position"), new Integer(pos));
		    }
		    else if (qName.equals("Chr")) {
			int chr;
			if (curData.equals("X"))
			    chr = 23;
			else if (curData.equals("Y"))
			    chr = 24;
			else
			    chr = Integer.parseInt(curData);
			probe.setChr(chr);
			probe.addProp(Property.getProperty(qName), curData);
		    }
		    else if (qName.equals("Gnl")) {
			int gnl;
			if (curData.equalsIgnoreCase("na")) {
			    gnl = Probe.GNL_OFFSET;
			}
			else {
			    gnl = Integer.parseInt(curData);
			}
			probe.setGnl(gnl);
			probe.addProp(Property.getProperty(qName), curData);
		    }
		    else if (qName.equals("Bkp")) {
			int v = getUInt();
			probe.setBkp(v);
			probe.addProp(Property.getProperty(qName), curData);
			if (v != 0) {
			    bkp_v.add(probe_num);
			}
		    }
		    else if (qName.equals("Out")) {
			int v = getSInt();
			probe.setOut(v);
			probe.addProp(Property.getProperty(qName), curData);
			if (v != 0) {
			    out_v.add(probe_num);
			}
		    }
		    else if (qName.equals("Ctm")) {
		    }
		    else if (qName.equals("Smt")) {
			float smt;
			if (curData.equalsIgnoreCase("na")) {
			    smt = Float.MIN_VALUE;
			}
			else {
			    smt = Float.parseFloat(curData);
			}
			probe.setSmoothing(smt);
			if (smt != last_smt) {
			    smt_v.add(probe_num);
			    last_smt = smt;
			}
			probe.addProp(Property.getProperty(qName), curData);
		    }
		    else if (qName.equals("Type")) {
			probe.setType(getType());
			probe.addProp(Property.getProperty(qName), curData);
		    }
		    else if (qName.equals("Y") || qName.equals("CopyNb")) {
			float ratio;
			if (curData.equalsIgnoreCase("na")) {
			    ratio = Float.MIN_VALUE;
			    probe.addProp(fr.curie.vamp.VAMPProperties.IsNAProp, "True");
			}
			else {
			    ratio = Float.parseFloat(curData);
			}
			//probe.setRatio(ratio);
			probe.setPosY(ratio);
			probe.addProp(Property.getProperty("Ratio"), curData);
		    }
		    else if (qName.equals("Size")) {
			int size;
			if (curData.equalsIgnoreCase("na")) {
			    size = -1;
			}
			else {
			    size = Integer.parseInt(curData);
			}
			probe.setSize(size);
			probe.addProp(Property.getProperty(qName), curData);
		    }
		    else if (qName.equals("Name")) {
			if (curData.charAt(0) >= '0' && 
			    curData.charAt(0) <= '9') {
			    curData = "N-" + curData;
			}
			probe.addProp(Property.getProperty(qName), curData);
		    }
		    else if (curData != null && curData.length() > 0) {
			probe.addProp(Property.getProperty(qName), curData);
		    }
		}
		
		curData = null;
	    }
	    catch(Exception e) {
		e.printStackTrace();
		System.exit(1);
		return;
	    }
	}

	private int getSInt() {
	    if (curData.equalsIgnoreCase("na")) {
		return 0;
	    }

	    return Integer.parseInt(curData);
	}

	private int getUInt() {
	    int n = getSInt();
	    if (n < 0) {
		n = 0;
	    }
	    return n;
	}

	private int getType() {
	    Integer type = typeMap.get(curData);
	    if (type == null) {
		type = new Integer(last_type++);
		typeMap.put(new String(curData), type);
	    }
	    return type.intValue();
	}

	public void error(SAXParseException e)
	    throws SAXException {
	    System.err.println("ImportProfile: SAX Error at line #" + e.getLineNumber());
	}

	public void warning(SAXParseException e) {
	    System.err.println("ImportProfile: SAX Warning at line #" + e.getLineNumber());
	}

	public void ignorableWhitespace(char[] ch, int start, int length) {
	}


	public void fatalError(SAXParseException e)
	    throws SAXException {
	    System.err.println("ImportProfile: SAX Fatal Error at line #" + e.getLineNumber());
	}

	private void addProp(HashMap<Property, Object> profileMap, String propName, Object value) {
	    Property prop = Property.getProperty(propName);
	    if (profileMap.get(prop) == null) {
		profileMap.put(prop, value);
	    }
	}

	public long[] getChrPos() {
	    String os = (String)profileMap.get(Property.getProperty("Organism"));
	    return VAMPUtils.getChrPos(globalContext, os);
	    /*
	    if (os == null) {
		System.err.println("no organism found in profile");
		return null;
	    }

	    Cytoband cytoband = MiniMapDataFactory.getCytoband(globalContext, os);
	    if (cytoband == null) {
		System.err.println("cannot load cytoband for organism " + os);
		return null;
	    }

	    Vector v = cytoband.getChrV();
	    long[] chrPos = new long[v.size()];
	    for (int n = 0; n < chrPos.length; n++) {
		chrPos[n] = ((Chromosome)v.get(n)).getOffsetPos();
	    }

	    return chrPos;
	    */
	}
    }

    private static void usage() {
	System.out.println("usage: ImportProfile native|portable SYSCFG URL [-merge-chr");
	System.exit(1);
    }

    public static void main(String args[]) {
	
	try {
	    if (args.length < 3) {
		usage();
	    }

	    int serial_type = 0;
	    if (args[0].equalsIgnoreCase("native")) {
		serial_type = ProfileSerializerFactory.NATIVE_JAVA;
	    }
	    else if (args[0].equalsIgnoreCase("portable")) {
		serial_type = ProfileSerializerFactory.PORTABLE;
	    }
	    else {
		usage();
	    }

	    new ImportProfile(serial_type, args[1], args[2], args.length > 3 && args[3].equalsIgnoreCase("-merge-chr"));
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    public int[] toArray(Vector<Integer> v) {
	int arr[] = new int[v.size()];
	for (int n = 0; n < arr.length; n++) {
	    arr[n] = v.get(n);
	}
	return arr;
    }
}

