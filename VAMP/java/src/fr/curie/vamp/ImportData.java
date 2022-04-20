
/*
 *
 * ImportData.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.JFileChooser;

import fr.curie.vamp.data.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.gui.*;
import fr.curie.vamp.gui.optim.*;

class ImportData {

    public static final int XML_IMPORT = 0x1;
    public static final int SERIAL_IMPORT = 0x2;
    public static final int GRAPHIC_OPTIM_IMPORT = 0x4;

    static final String VAMPDIR = "/.vamp";
    static final String DATADIR_FILE = VAMPDIR + "/DATADIR";
    static final String DEFAULT_DATADIR = VAMPDIR + "/data";
    static final String MD5_EXT = ".md5";

    static final boolean CHECK_MD5 = true;
    static final boolean USE_CACHE = false; // disabling cache is better for testing

    static File getDir(GlobalContext globalContext, String name) {
	File dir = new File(name);
	if (!dir.isDirectory()) {
	    if (!dir.mkdirs()) {
		InfoDialog.pop(globalContext, "cannot create directory " + dir.getAbsolutePath());
		return null;
	    }
	}

	return dir;
    }

    static File getDataDir(GlobalContext globalContext, String home) throws IOException {
	File dataDirFile = new File(home + DATADIR_FILE);

	if (dataDirFile.canRead()) {
	    FileReader reader = new FileReader(dataDirFile);
	    char buf[] = new char[1024];
	    int n = reader.read(buf, 0, buf.length);
	    reader.close();
	    return new File(new String(buf, 0, n));
	}

	Vector v = new Vector();

	ConfirmDialog.pop(globalContext, "The visualisation of these types of profiles implies the copy\nof these profiles to your local computer. Do you accept ?",
			  new Action() {
			      public void perform(Object arg) {
				  Vector v = (Vector)arg;
				  v.add(new Boolean(true));
			      }
			  }, v, "Yes", "No");

	if (v.size() == 0) {
	    return null;
	}

	if (getDir(globalContext, home + DEFAULT_DATADIR) == null) {
	    return null;
	}

	JFileChooser fileChooser = new JFileChooser(home + DEFAULT_DATADIR);
	fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	fileChooser.setBackground(VAMPResources.getColor
				  (VAMPResources.DIALOG_BG));

	fileChooser.setDialogTitle("Directory to copy files");
	fileChooser.setApproveButtonText("OK");
	//int returnVal = fileChooser.showOpenDialog(new java.awt.Frame());
	int returnVal = fileChooser.showSaveDialog(new java.awt.Frame());
	if (returnVal == JFileChooser.CANCEL_OPTION) {
	    return null;
	}

	File dataDir = fileChooser.getSelectedFile();
	if (dataDir == null) {
	    return null;
	}

	dataDir = getDir(globalContext, dataDir.getAbsolutePath());
	if (dataDir == null) {
	    return null;
	}
	
	FileWriter writer = new FileWriter(dataDirFile);
	writer.write(dataDir.getAbsolutePath(), 0, dataDir.getAbsolutePath().length());
	writer.close();
	return dataDir;
    }

    static File getDataDir(GlobalContext globalContext) {
	String home = VAMPUtils.getHomeDir(globalContext);

	if (home == null) {
	    return null;
	}

	try {
	    return getDataDir(globalContext, home);
	}
	catch(IOException e) {
	    InfoDialog.pop(globalContext, "", e);
	    return null;
	}
    }

    static File copyRealize(GlobalContext globalContext, View view, File file, String uri, String ext, boolean is_md5) {
	try {
	    uri = XMLUtils.makeURL(globalContext, uri + ext);

	    URL url = new URL(uri);
	    // PLR idea
	    if (url.getProtocol().equals("file")) {
		return new File(url.getPath());
	    }
	}
	catch(Exception e) {
	    InfoDialog.pop(globalContext, "Invalid URL " + uri);
	    return null;
	}

	try {
	    if (GraphCanvas.VERBOSE) {
		System.err.println("Copying file " + file.getAbsolutePath() + " from " + XMLUtils.makeURL(globalContext, uri) + "\n");
	    }

	    if (view != null) {
		view.setMessage("Copying file " + file.getName() + " to local computer...\n");
		view.repaint();
	    }

	    InputStream is = Utils.openStream(uri);
	    FileOutputStream os = new FileOutputStream(file.getAbsolutePath());
	    byte b[] = new byte[4096];
	    int n;
	    while ((n = is.read(b)) >= 0) {
		os.write(b, 0, n);
	    }
	    os.close();
	    is.close();
	}
	catch(Exception e) {
	    if (is_md5) {
		InfoDialog.pop(globalContext, "Warning: MD5 " + uri + " is not accessible");
	    }
	    else {
		InfoDialog.pop(globalContext, "Error: " + uri + " is not accessible");
	    }
	    return null;
	}

	return file;
    }

    static boolean md5Check(GlobalContext globalContext, View view, String uri, String name, String ext) {
	File md5File = new File(name + ext + MD5_EXT);

	if (!CHECK_MD5) {
	    if (md5File.canRead()) {
		return true;
	    }
	}

	try {
	    InputStream is = Utils.openStream(XMLUtils.makeURL(globalContext, uri + ext + MD5_EXT));
	    String md5_remote = "";
	    if (is == null) {
		return false;
	    }

	    InputStreamReader fis = new InputStreamReader(is);
	    char buf[] = new char[256];
	    int n = fis.read(buf, 0, buf.length);
	    md5_remote = new String(buf, 0, n);
	    is.close();
	    if (md5File.canRead()) {
		FileReader reader = new FileReader(md5File);
		n = reader.read(buf, 0, buf.length);
		reader.close();
		String md5_local = new String(buf, 0, n);
		if (md5_local.equals(md5_remote)) {
		    if (GraphCanvas.VERBOSE) {
			System.out.println(name + ext + " MD5 is identical");
		    }
		    return true;
		}
	    }
	}
	catch(Exception e) {
	    //	    InfoDialog.pop(globalContext, "Warning: URL " + uri + " is not accessible");
	    //return false;
	}

	copyRealize(globalContext, view, md5File, uri, ext + MD5_EXT, true);
	return false;
    }

    synchronized static File importFile(GlobalContext globalContext,
					View view,
					String uri,
					String name, String ext) {
	File file = new File(name + ext);

	boolean md5_check = md5Check(globalContext, view, uri, name, ext);
	if (!file.canRead() || !md5_check) {
	    file = copyRealize(globalContext, view, file, uri, ext, false);
	}

	return file;
    }

    static String makeDir(Vector<String> parents) {
	String s = "";
	for (int n = 0; n < parents.size(); n++) {
	    s = parents.get(n) + (s.length() > 0 ? "/" + s : "");
	}

	return s;
    }

    static String makeDir(String uri) {
	int idx = uri.lastIndexOf('/');
	if (idx < 0) {
	    return "";
	}
	String parentDir = uri.substring(1, idx);
	return parentDir;
    }

    synchronized static LinkedList importSerial(GlobalContext globalContext,
						View view, GraphPanel panel,
						int import_mode,
						String type, String uri,
						Vector<String> parents,
						String[] chrList,
						boolean pangen,
						boolean update, boolean full) {
	File dataDir = getDataDir(globalContext);
	if (dataDir == null) {
	    return null;
	}
	String orig_uri = uri;
	if (uri.startsWith("http://") || uri.startsWith("file://")) {
	    try {
		URL url = new URL(uri);
		uri = url.getPath();
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	}

	if (!uri.startsWith("/")) {
	    uri = "/" + uri;
	}

	if (GraphCanvas.VERBOSE) {
	    System.out.println("Loading serial profile " +  XMLUtils.makeURL(globalContext, uri + ".*"));
	}

	File file = new File(uri);

	//String parentDir = makeDir(parents);
	String parentDir = makeDir(uri);
	File serialDir = getDir(globalContext, dataDir + "/" + parentDir);
	if (serialDir == null) {
	    return null;
	}

	String fname = dataDir + "/" + parentDir + "/" + file.getName();

	if (USE_CACHE) {
	    GraphElement graphElem = GraphElementCache.getInstance().get(orig_uri);
	    if (graphElem != null) {
		//System.out.println("IN CACHE " + graphElem);
		LinkedList list = new LinkedList();
		try {
		    Profile profile = graphElem.asProfile().cloneRealize(true);
		    GraphicProfile graphicProfile = new GraphicProfile(profile, graphElem.asProfile().getGraphicProfile().getGraphicInfo());
		    list.add(profile);
		}
		catch(Exception e) {
		    e.printStackTrace();
		}
		return list;
	    }
	}

	File dspFile = importFile(globalContext, view, orig_uri, fname, ProfileSerialUtils.DISPLAY_SUFFIX);
	if (dspFile == null) {
	    return new LinkedList();
	}
	File prpFile = importFile(globalContext, view, orig_uri, fname, ProfileSerialUtils.PROP_SUFFIX);
	if (prpFile == null) {
	    return new LinkedList();
	}
	if ((import_mode & GRAPHIC_OPTIM_IMPORT) != 0) {
	    File griFile = importFile(globalContext, view, orig_uri, fname, GraphicInfo.GRINFO_SUFFIX);
	    if (griFile == null) {
		return new LinkedList();
	    }
	}
	int idx = dspFile.getAbsolutePath().lastIndexOf(".");
	return importSerialProfiles(globalContext, view, panel, import_mode, orig_uri, parentDir, dspFile.getAbsolutePath().substring(0, idx), chrList, pangen, full);
    }

    static private void setAxisDisplayer(GraphPanelSet panelSet, Profile profile) {
	if (VAMPUtils.isMergeChr(profile) &&
	    !(panelSet.getPanel(0).getDefaultAxisDisplayer() instanceof
	      ChromosomeNameAxisDisplayer)) {
	    panelSet.setDefaultAxisDisplayer
		(new ChromosomeNameAxisDisplayer
		 (VAMPUtils.getAxisName(profile), 1., 0.1, false,
		  DataSetIDArrayBuilder.getInstance()));
	}
    }

    static private void setThresholds(Profile profile) {
	String type = VAMPUtils.getType(profile);

	// code taken from XMLArrayDataFactory
	if (type.equals(VAMPConstants.CGH_ARRAY_TYPE) ||
	    type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.CGH_AVERAGE_TYPE) ||
	    type.equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_CGH, false);
	}
	else if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
	    type.equals(VAMPConstants.TRANSCRIPTOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_TRS, false);
	}
	else if (type.equals(VAMPConstants.TRANSCRIPTOME_REL_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_TRSREL, false);
	}
	else if (type.equals(VAMPConstants.SNP_TYPE) ||
		 type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE) ||
		 type.equals(VAMPConstants.SNP_AVERAGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_SNP, false);
	}
	else if (type.equals(VAMPConstants.DIFFANA_TYPE) ||
		 type.equals(VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_DIFFANA, false);
	}
	else if (type.equals(VAMPConstants.GTCA_TYPE) ||
		 type.equals(VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_GTCA, false);
	}
	else if (type.equals(VAMPConstants.CHIP_CHIP_TYPE) ||
		 type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE) ||
		 type.equals(VAMPConstants.CHIP_CHIP_AVERAGE_TYPE) ||
		 type.equals(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_CHIP_CHIP, false);
	}
	else if (type.equals(VAMPConstants.FRAGL_TYPE) ||
		 type.equals(VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_FRAGL, false);
	}
	else {
	    System.out.println("WARNING: unknown type " + type + " for setting thresholds");
	    profile.setPropertyValue(VAMPProperties.ThresholdsNameProp,
				     VAMPConstants.THR_CGH, false);
	}
    }

    static private void setColorCodes(Profile profile) {
	Object value = profile.getPropertyValue(VAMPProperties.RatioScaleProp);
	
	if (value == null) {
	    value = profile.getPropertyValue(VAMPProperties.RatioProp);
	    if (value == null) {
		return;
	    }

	    profile.setPropertyValue(VAMPProperties.RatioScaleProp, value);
	    profile.removeProperty(VAMPProperties.RatioProp);
	}

	String type = VAMPUtils.getType(profile);
	boolean is_log = value.equals(VAMPConstants.RatioScale_L);

	if (type.equals(VAMPConstants.CGH_ARRAY_TYPE) ||
	    type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.CGH_AVERAGE_TYPE) ||
	    type.equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_CGH, false);
	}
	else if (type.equals(VAMPConstants.CHIP_CHIP_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_AVERAGE_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_CHIP_CHIP, false);
	}
	else if (type.equals(VAMPConstants.GTCA_TYPE) ||
		 type.equals(VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_GTCA, false);
	}
	else if (type.equals(VAMPConstants.SNP_TYPE) ||
		 type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE) ||
		 type.equals(VAMPConstants.SNP_AVERAGE_TYPE)) {
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_SNP, false);
	}
	else if (type.equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
	}
	else {
	    System.out.println("WARNING: unknown type " + type + " for setting color codes");
	    profile.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_CGH, false);
	}
    }

    static Property parentDirProp = Property.getHiddenProperty("ParentDir");

    static LinkedList importSerialProfiles(GlobalContext globalContext, View view, GraphPanel panel, int import_mode, String orig_uri, String parentDir, String name, String chrList[], boolean pangen, boolean full) {
	long ms0 = System.currentTimeMillis();

	GraphPanelSet panelSet = (view != null ? view.getGraphPanelSet() : null);
	LinkedList profileList = new LinkedList();

	try {
	    //ProfileUnserializer unserialProf = new ProfileUnserializer(name);
	    ProfileUnserializer unserialProf = ProfileSerializerFactory.getInstance().getUnserializer(name);

	    Profile profile = unserialProf.readProfile(full);

	    String url = XMLUtils.makeURL(globalContext, orig_uri);
	    profile.setURL(url);
	    profile.setSourceURL(url);
	    profile.setPropertyValue(parentDirProp, parentDir);

	    GraphicProfile graphicProfile = (import_mode & GRAPHIC_OPTIM_IMPORT) != 0 ? new GraphicProfile(profile) : new GraphicProfile(profile, null);

	    profile.setUnserializingPolicy(Profile.CACHE_PROBES);
	    
	    profile.setPropertyValue(VAMPProperties.LargeProfileProp, new Boolean(true));
	    if (full) {
		profile.getProbe(0, true, true).addProp(VAMPProperties.LargeProfileProp, new Boolean(true));
	    }

	    if (USE_CACHE) {
		GraphElementCache.getInstance().put(orig_uri, profile);
	    }
	    
	    setColorCodes(profile);
	    setThresholds(profile);
	    
	    profile = make(profile, pangen, chrList);

	    if (profile != null) {
		if (panelSet != null) {
		    setAxisDisplayer(panelSet, profile);
		}
		// end test
	    
		profileList.add(profile);
	    }
	}
	
	catch(Exception e) {
	    e.printStackTrace();
	}

	//long ms1 = System.currentTimeMillis();
	//System.out.println(((ms1-ms0)/1000.) + " seconds");

	return profileList;
    }

    static private Profile make(Profile profile, boolean pangen, String chrList[]) throws Exception {

	if (!pangen) {
	    String chr_s = chrList[0];
	    int chr = Integer.parseInt(VAMPUtils.norm2Chr(chr_s))-1;

	    profile = profile.split(chr);
	    if (profile == null) {
		return null;
	    }
	    VAMPUtils.setType(profile, VAMPConstants.CGH_ARRAY_TYPE);
	    profile.setPropertyValue(VAMPProperties.ChromosomeProp, chr_s);
	}
	else if (chrList != null) {
	    String chr_s = "";
	    boolean chrNums[] = new boolean[Profile.MAX_CHR_CNT];
	    for (int n = 0; n < chrNums.length; n++) {
		chrNums[n] = false;
	    }

	    for (int n = 0; n < chrList.length; n++) {
		int chr = Integer.parseInt(VAMPUtils.norm2Chr(chrList[n]))-1;
		chrNums[chr] = true;
		if (n > 0) {
		    chr_s += ",";
		}
		chr_s += chrList[n];
	    }

	    profile = profile.merge(chrNums);
	    profile.setPropertyValue(VAMPProperties.ChromosomeProp, chr_s);
	}

	return profile;
    }

    synchronized static LinkedList importData(GlobalContext globalContext,
					      View view, GraphPanel panel,
					      String type, String url,
					      int import_mode,
					      Vector<String> parents,
					      String[] chrList,
					      Boolean pangen,
					      boolean update, boolean full) {
	LinkedList ilist;

	if ((import_mode & SERIAL_IMPORT) != 0) {
	    return importSerial(globalContext, view, panel, import_mode, type, url, parents, chrList, pangen, update, full);
	}

	if (type.equals("TRS_CHRMERGE") || type.equals("TRS_CHR")) {
	    XMLTranscriptomeFactory trsFactory =
		new XMLTranscriptomeFactory(globalContext);
	    ilist = trsFactory.buildDataSets(url, null, false, full);
	}
	else if (type.equals("LOH_CHRMERGE") || type.equals("LOH_CHR")) {
	    XMLLOHFactory lohFactory =
		XMLLOHFactory.getFactory(globalContext);
	    ilist = lohFactory.buildDataSets(url, null, false);
	}
	else {
	    XMLArrayDataFactory arrayFactory = 
		new XMLArrayDataFactory(globalContext, null);
	    ilist = arrayFactory.getData(url, update, full);
	}

	if (ilist != null) {
	    for (int n = 0; n < ilist.size(); n++)
		((GraphElement)ilist.get(n)).setSourceType(type);
	}

	ilist = applyOP(view, panel, ilist, type, full);
	return ilist;
    }

    static LinkedList applyOP(View view, GraphPanel panel,
			      LinkedList ilist, String type, boolean full) {
	if (type == null)
	    return ilist;

	String op;
	if (type.equals("CGH_CHRMERGE"))
	    op = full ? MergeChrOP.CGH_NAME : MergeChrOP.CGH_NAME_LIGHT;
	else if (type.equals("CHIP_CHRMERGE"))
	    op = MergeChrOP.CHIP_CHIP_NAME;
	else if (type.equals("TRS_CHRMERGE"))
	    op = MergeChrOP.TRANSCRIPTOME_NAME;
	else if (type.equals("SNP_CHRMERGE"))
	    op = MergeChrOP.SNP_NAME;
	else if (type.equals("GENOME_ANNOT_CHRMERGE"))
	    op = MergeChrOP.GENOME_ANNOT_NAME;

	// backward compatibility
	else if (type.equals("CHR_ARRAY"))
	    op = full ? MergeChrOP.CGH_NAME : MergeChrOP.CGH_NAME_LIGHT;
	else if (type.equals("CHIP_ARRAY"))
	    op = MergeChrOP.CHIP_CHIP_NAME;
	else if (type.equals("TRS_ARRAY"))
	    op = MergeChrOP.TRANSCRIPTOME_NAME;
	/*
	else if (type.equals("GENOME_ANNOTATION_ARRAY"))
	    op = MergeChrOP.GENOME_ANNOT_NAME;
	*/
	else
	    return ilist;

	GraphElementListOperation dslop = GraphElementListOperation.get(op);
	if (dslop == null)
	    return ilist;

	boolean isReadOnly = panel.isReadOnly();
	panel.setReadOnly(false);

	Vector iv = Utils.listToVector(ilist);
	if (!dslop.mayApply(view, panel, iv, true)) {
	    panel.setReadOnly(isReadOnly);
	    return ilist;
	}

	boolean active = UndoVMStack.getInstance(panel).setActive(false);

	Vector ov = dslop.apply(view, panel, iv, null, true);

	garbageInput(iv, ov, full);

	UndoVMStack.getInstance(panel).setActive(active);
	panel.setReadOnly(isReadOnly);
	return Utils.vectorToList(ov);
    }

    private static void garbageInput(Vector iv, Vector ov, boolean full) {
	if (iv == null)
	    return;

	// 5/5/06: this garbage is possible because the applied operation
	// does not keep the input dataset

	Vector torm = new Vector();
	int iv_size = iv.size();
	for (int m = 0; m < iv_size; m++) {
	    DataSet dataSet = (DataSet)iv.get(m);
	    if (dataSet == null)
		continue;

	    if (ov != null && ov.contains(dataSet)) {
		continue;
	    }
	    
	    String curURL = dataSet.getURL();
	    if (curURL != null && full)
		GraphElementCache.getInstance().remove(curURL);

	    dataSet.setData(null);
	    torm.add(dataSet);
	}

	int torm_size = torm.size();
	for (int m = 0; m < torm_size; m++) {
	    iv.remove(torm.get(m));
	}
    }
}
