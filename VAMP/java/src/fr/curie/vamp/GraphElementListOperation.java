
/*
 *
 * GraphElementListOperation.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;

import fr.curie.vamp.utils.serial.SerialUtils;

public abstract class GraphElementListOperation implements Task.Operation {
   
    // 4/10/06: WARNING when set to true, some operations does not work,
    // for instance SignalHistogramOP. One possible reason is that some
    // graphical operations are done in the Task thread ... which is not
    // the AWT thread. One must use invoke later on these graphical operations
    static boolean USE_THREAD = false;

    private String name;

    public static final Property ParamsProp = Property.getHiddenProperty("_params");

    public static final int ON_ALL = 0x1;
    public static final int SHOW_MENU = 0x2;
    public static final int ADD_SEPARATOR = 0x4;
    public static final int ON_ALL_AUTO = 0x8;
    public static TreeMap null_params = new TreeMap();

    public static final int CGH_TYPE = 1;
    public static final int TRANSCRIPTOME_TYPE = 2;
    public static final int LOH_TYPE = 3;
    public static final int CHIP_CHIP_TYPE = 4;
    public static final int SNP_TYPE = 5;
    public static final int GENOME_ANNOT_TYPE = 6;
    public static final int BREAKPOINT_FREQUENCY_TYPE = 7;
    public static final int FRAGL_TYPE = 8;
    public static final int DIFFANA_TYPE = 9;
    public static final int GTCA_TYPE = 10;

    protected int flags;
    static private HashMap opmap = new HashMap();

    public GraphElementListOperation(String name, int flags) {
	this.name = name;
	this.flags = flags;
	opmap.put(name, this);
	//System.out.println(name);
	if (name.equals("Chromosome Name")) // BWC
	    opmap.put("Chromosome Axis", this);
    }

    public String getName() {return name;}
    public String getMenuName() {return getName();}
    public boolean isOnAll() {return (flags & ON_ALL) != 0;}
    public boolean isOnAllAuto() {return (flags & ON_ALL_AUTO) != 0;}
    public boolean showMenu() {return (flags & SHOW_MENU) != 0;}
    public boolean addSeparator() {return (flags & ADD_SEPARATOR) != 0;}

    public static Vector getOperations() {
	return Utils.keyVector(opmap);
    }

    public static GraphElementListOperation get(String name) {
	GraphElementListOperation op = (GraphElementListOperation)opmap.get(name);
	if (op == null)
	    System.err.println("ERROR: operation " + name + " not found");
	return op;
    }

    public boolean mayApplyOnReadOnlyPanel() {
	return false;
    }

    public boolean mayApply(View view, GraphPanel panel,
			    Vector graphElements, boolean autoApply) {
	graphElements = getGraphElements(panel, graphElements, autoApply);

	if (graphElements.size() == 0)
	    return false;

	if (panel != null && panel.isReadOnly() && !mayApplyOnReadOnlyPanel()) {
	    return false;
	}

	if (view != null && !autoApply && isOnAll()) {
	    LinkedList list = panel.getGraphElements();
	    if (list.size() != graphElements.size())
		return false;
	}

	int size = graphElements.size();
	if (!supportProfiles()) {
	    for (int n = 0; n < size; n++) {
		if (((GraphElement)graphElements.get(n)).asProfile() != null) {
		    return false;
		}
	    }
	}

	if (!VAMPUtils.checkGraphElementType(Utils.vectorToList(graphElements),
					     getSupportedInputTypes())) {
	    return false;
	}

	if (!mayApplyOnLightImportedProfiles()) {
	    for (int n = 0; n < size; n++) {
		if (!((GraphElement)graphElements.get(n)).isFullImported())
		    return false;
	    }
	}

	return mayApplyP(view, panel, graphElements, autoApply);
    }

    public boolean supportProfiles() {
	return false;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	return true;
    }

    public boolean mayApply(View view, GraphPanel panel, Vector graphElements) {
	return mayApply(view, panel, graphElements, false);
    }

    public boolean mayApply(GraphElementListOperation autoApplyDSLOP) { return true; }

    public TreeMap getParams(View view, Vector graphElements) {return null_params;}
    public TreeMap getDefaultParams(View view, Vector graphElements) {return null_params;}

    public abstract Vector apply(View view, GraphPanel panel,
				 Vector graphElements, TreeMap params,
				 boolean autoApply);

    Vector apply(View view, GraphPanel panel, Vector graphElements,
		 TreeMap params) {
	return apply(view, panel, graphElements, params, false);
    }

    public boolean mustInitScale() {
	return false;
    }

    public abstract String[] getSupportedInputTypes();
    public abstract String getReturnedType();

    public Vector getGraphElements(GraphPanel panel, Vector graphElements, boolean autoApply) {
	if (!autoApply && isOnAllAuto())
	    return Utils.listToVector(panel.getGraphElements());
	return graphElements;
    }

    public LinkedList getGraphElements(GraphPanel panel, LinkedList graphElements,
			   boolean autoApply) {
	if (!autoApply && isOnAllAuto())
	    return panel.getGraphElements();

	return graphElements;
    }

    public GlobalContext getGlobalContext(View view, TreeMap params) {
	if (view != null)
	    return view.getGlobalContext();
	return params != null ? (GlobalContext)params.get("GlobalContext") :
	    null;
    }

    public Vector undoManage(GraphPanel panel, Vector graphElements) {
	if (panel != null) {
	    StandardVMStatement vmstat = new StandardVMStatement
		(VMOP.getVMOP(getName()), panel);
	    vmstat.beforeExecute();
	    UndoVMStack.getInstance(panel).push(vmstat);
	}
	return graphElements;
    }

    public static String codeParams(GraphElement graphElem) {
	TreeMap params = (TreeMap)graphElem.getPropertyValue(ParamsProp);
	if (params == null)
	    return "";

	String code = "";
	Iterator it = params.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Object value = entry.getValue();
	    if (value != null)
		code += " " + entry.getKey().toString() + "=\"" +
		    value.toString() + "\"";
	}

	return code;
    }

    public TreeMap makeParams(HashMap map) {
	return Utils.hashToTreeMap(map);
    }

    View _view;
    GraphPanel _panel;
    Vector _graphElements;
    TreeMap _params;
    boolean _autoApply;

    public void perform1() {
    }

    public void perform2() {
	Vector v = apply(_view, _panel, _graphElements, _params, _autoApply);
	if (v != null) {
	    if (mustInitScale())
		_view.reinitScale();
	    _panel.replaceGraphElements(_graphElements, v);
	}
    }

    public View getView() {
	return _view;
    }

    public GraphPanel getPanel() {
	return _panel;
    }

    public String getMessage() {
	return name + "...";
    }

    Vector apply_thread(View view, GraphPanel panel,
		      Vector graphElements, TreeMap params) {
	return apply_thread(view, panel, graphElements, params, false);
    }

    protected Task task;

    Vector apply_thread(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	if (useThread()) {
	    _view = view;
	    _panel = panel;
	    _graphElements = graphElements;
	    _params = params;
	    _autoApply = autoApply;

	    task = new Task(this);
	    task.start();
	    return null;
	}
	else
	    task = null;

	return apply(view, panel, graphElements, params, autoApply);
    }

    public boolean useThread() {
	return USE_THREAD;
    }

    static void toggleThread() {
	USE_THREAD = !USE_THREAD;
	System.out.println("Operations " + (USE_THREAD ? "" : "do not ") + "use thread");
    }

    public void postPerform(LinkedList graphElements) {
    }

    public boolean mayApplyOnLightImportedProfiles() {return false;}

    // move to ToolResultManager
    /*
    private static final String PAR_SEP = "___";
    private static final String GRH_SEP = ":::";
    public static final String RESULT_DIR = "/results";

    static public String getResultSerialFile(GlobalContext globalContext, GraphElementListOperation op, TreeMap params, Vector graphElements) throws Exception {
	return op.getResultSerialFile(globalContext, params, graphElements);
    }

    public String getResultSerialFile(GlobalContext globalContext, TreeMap params, Vector graphElements) throws Exception {
	File file = ImportData.getDir(globalContext, VAMPUtils.getHomeDir(globalContext) + "/" + ImportData.VAMPDIR + RESULT_DIR);
	return file.getAbsolutePath() + "/" + SerialUtils.DSPREFIX + computeSignature(params, graphElements);
    }

    public String getResultSerialFile(GlobalContext globalContext, TreeMap params, GraphElement graphElement) throws Exception {
	Vector graphElements = new Vector();
	graphElements.add(graphElement);
	return getResultSerialFile(globalContext, params, graphElements);
    }

    public String computeSignature(TreeMap params, Vector graphElements) throws Exception {
	String s = "";
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
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    s += ((GraphElement)graphElements.get(n)).getID();
	    if (n < size - 1) {
		s += GRH_SEP;
	    }
	}

	return Utils.md5Crypt(s);
    }

    static public String computeSignature(GraphElementListOperation op, TreeMap params, Vector graphElements) throws Exception {
	return op.computeSignature(params, graphElements);
    }
    */

    public static Vector<String> makeVectorID(Vector graphElements) {
	Vector<String> v = new Vector();

	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    v.add((String)((GraphElement)graphElements.get(n)).getID());
	}

	return v;
    }
}
