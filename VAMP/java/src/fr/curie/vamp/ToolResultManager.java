
/*
 *
 * ToolResultManager.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.Vector;
import java.io.File;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;

import fr.curie.vamp.data.Profile;
import fr.curie.vamp.data.serial.ProfileUnserializer;
import fr.curie.vamp.data.serial.ProfileSerializerFactory;
import fr.curie.vamp.utils.serial.SerialUtils;
import fr.curie.vamp.gui.optim.GraphicProfile;

public class ToolResultManager {

    private enum Policy {
	DIRECT_POLICY(1),
        SERIAL_POLICY(2);

	private int policy;

	Policy(int policy) {
	    this.policy = policy;
	}
    };

    public static final int NEW_VIEW_IF_DIFFERENT_DISPLAYERS = 0x1;
    public static final int PRIVATE_DISPLAYERS_IF_DIFFERENT = 0x2;
    public static final int PRIVATE_DISPLAYERS = 0x4;

    public static final int VERSION = 1;

    private static ToolResultManager instance;

    public static ToolResultManager getInstance() {
	if (instance == null) {
	    instance = new ToolResultManager();
	}

	return instance;
    }

    // must choose between DataSet or Profile, based on what ?

    private Policy getPolicy(ToolResultContext toolResultContext) {
	GraphElement graphElementDiscrim = toolResultContext.getGraphElementDiscrim();
	if (graphElementDiscrim != null) {
	    return graphElementDiscrim.asDataSet() != null ? Policy.DIRECT_POLICY : Policy.SERIAL_POLICY;
	}

	Vector<GraphElement> graphElements = toolResultContext.getGraphElements();
	if (graphElements == null) {
	    GraphElement graphElementBase = toolResultContext.getGraphElementBase();
	    if (graphElementBase != null && graphElementBase.asDataSet() != null) {
		return Policy.DIRECT_POLICY;
	    }
	}
	else if (graphElements.get(0).asDataSet() != null) { // ??
	    return Policy.DIRECT_POLICY;
	}

	return Policy.SERIAL_POLICY;
    }

    static Vector<GraphElement> makeVector(GraphElement graphElement) {
	Vector<GraphElement> graphElements = new Vector();
	graphElements.add(graphElement);
	return graphElements;
    }

    public ToolResultContext prologue(GlobalContext globalContext, String opname, TreeMap params, GraphElement graphElement, GraphElement graphElementBase) throws Exception {
	return prologue(globalContext, opname, params, makeVector(graphElement), graphElementBase, null);
    }

    public ToolResultContext prologue(GlobalContext globalContext, String opname, TreeMap params, Vector<GraphElement> graphElements, GraphElement graphElementBase) throws Exception {
	return prologue(globalContext, opname, params, graphElements, graphElementBase, null);
    }

    private ToolResultContext prologue(GlobalContext globalContext, String opname, TreeMap params, Vector<GraphElement> graphElements, Vector<String> graphElementIDs, String chr, GraphElement graphElementBase, GraphElement graphElementDiscrim) throws Exception {

	ToolResultContext toolResultContext;
	if (graphElements != null) {
	    toolResultContext = new ToolResultContext(globalContext, opname, params, graphElements, graphElementBase, graphElementDiscrim);
	}
	else {
	    toolResultContext = new ToolResultContext(globalContext, opname, params, graphElementIDs, graphElementBase, graphElementDiscrim, chr);
	}

	Policy policy = getPolicy(toolResultContext);
	if (policy == Policy.DIRECT_POLICY) {
	    //System.out.println("ToolResultManager: Direct policy");
	}
	else if (policy == Policy.SERIAL_POLICY) {
	    //System.out.println("ToolResultManager: Serial policy");
	}

	if (policy == Policy.DIRECT_POLICY) {
	    GraphElementFactory factory = new DataSetFactory(globalContext, (graphElementBase != null ? graphElementBase.asDataSet() : null));
	    toolResultContext.setFactory(factory);
	    return toolResultContext;
	}

	String serialFile;
	if (graphElements != null) {
	    serialFile = getResultSerialFile(globalContext, opname, params, graphElements);
	}
	else {
	    serialFile = getResultSerialFile(globalContext, opname, params, graphElementIDs, chr);
	}

	File file = new File(serialFile + ".dsp");
	if (file.exists()) {
	    Profile profile = makeGraphElement(serialFile, true);
	    GraphicProfile graphicProfile;
	    file = new File(serialFile + ".gri");
	    if (file.exists()) {
		graphicProfile = new GraphicProfile(profile);
	    }
	    else {
		graphicProfile = new GraphicProfile(profile, null);
	    }
	    toolResultContext.setGraphElementResult(profile);
	    return toolResultContext;
	}

	GraphElementFactory factory = new ProfileSequentialAccessFactory(globalContext, serialFile, 0, graphElementBase);
	toolResultContext.setFactory(factory);
	return toolResultContext;
    }

    public ToolResultContext prologue(GlobalContext globalContext, String opname, TreeMap params, Vector<GraphElement> graphElements, GraphElement graphElementBase, GraphElement graphElementDiscrim) throws Exception {

	return prologue(globalContext, opname, params, graphElements, null, null, graphElementBase, graphElementDiscrim);
    }

    public ToolResultContext prologue(GlobalContext globalContext, String opname, TreeMap params, Vector<String> graphElementIDs, GraphElement graphElementBase, GraphElement graphElementDiscrim, String chr) throws Exception {

	return prologue(globalContext, opname, params, null, graphElementIDs, chr, graphElementBase, graphElementDiscrim);
    }

    public Profile makeGraphElement(String serialFile, boolean full) throws Exception {
	//ProfileUnserializer profUnserial = new ProfileUnserializer(serialFile);
	ProfileUnserializer profUnserial = ProfileSerializerFactory.getInstance().getUnserializer(serialFile);
	Profile profile = profUnserial.readProfile(full);
	profile.setUnserializingPolicy(Profile.CACHE_PROBES);
	profile.setPropertyValue(VAMPProperties.LargeProfileProp, new Boolean(true));
	if (full) {
	    profile.getProbe(0, true, true).addProp(VAMPProperties.LargeProfileProp, new Boolean(true));
	}
	GraphicProfile graphicProfile;
	File file = new File(serialFile + ".gri");
	if (file.exists()) {
	    graphicProfile = new GraphicProfile(profile);
	}
	else {
	    graphicProfile = new GraphicProfile(profile, null);
	}

	return profile;
    }

    public GraphElement epilogue(ToolResultContext toolResultContext) throws Exception {
	GraphElementFactory factory = toolResultContext.getFactory();
	if (factory != null) {
	    factory.getGraphElement().setPropertyValue(VAMPProperties.ToolResultInfoProp, toolResultContext.getInfo());
	    ImportDataDialog.needRefreshResults();
	    return factory.epilogue();
	}

	return toolResultContext.getGraphElementResult();
    }

    private static final String PAR_SEP = "___";
    private static final String CHR_SEP = ",";
    private static final String GRH_SEP = ":::";
    public static final String RESULT_DIR = "/results";

    static public File getResultDir(GlobalContext globalContext) {
	return ImportData.getDir(globalContext, VAMPUtils.getHomeDir(globalContext) + "/" + ImportData.VAMPDIR + RESULT_DIR);
    }

    static public String getResultSerialFile(GlobalContext globalContext, String opname, TreeMap params, Vector graphElements) throws Exception {
	File file = getResultDir(globalContext);
	return file.getAbsolutePath() + "/" + SerialUtils.DSPREFIX + computeSignature(opname, params, graphElements, null, null);
    }

    static public String getResultSerialFile(GlobalContext globalContext, String opname, TreeMap params, Vector<String> graphElementIDs, String chr) throws Exception {
	File file = getResultDir(globalContext);
	return file.getAbsolutePath() + "/" + SerialUtils.DSPREFIX + computeSignature(opname, params, null, graphElementIDs, chr);
    }

    public static String computeSignature(String opname, TreeMap params, Vector<GraphElement> graphElements, Vector<String> graphElementIDs, String chr) throws Exception {
	String s = opname + PAR_SEP;
	if (params != null) {
	    Iterator it = params.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		s += entry.getKey() + "=" + entry.getValue();
		if (it.hasNext()) {
		    s += ";";
		}
	    }
	}

	s += PAR_SEP;
	int size = (graphElements != null ? graphElements.size() : graphElementIDs.size());
	for (int n = 0; n < size; n++) {
	    if (graphElements != null) {
		GraphElement graphElement = graphElements.get(n);
		s += graphElement.getID();
		s += CHR_SEP;
		s += VAMPUtils.getChr(graphElement);
	    }
	    else {
		s += graphElementIDs.get(n);
		s += CHR_SEP;
		s += chr;
	    }
	    if (n < size - 1) {
		s += GRH_SEP;
	    }
	}

	return Utils.md5Crypt(s);
    }

    public static Vector<String> makeVectorID(Vector graphElements) {
	Vector<String> v = new Vector();

	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    v.add((String)((GraphElement)graphElements.get(n)).getID());
	}

	return v;
    }
}
