
/*
 *
 * ImportDataDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004-2008
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DateFormat;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import fr.curie.vamp.data.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.gui.optim.*;

public class ImportDataDialog extends JDialog {

    private View view;
    private int nn;

    private GlobalContext globalContext;
    private JScrollPane scrollPane;
    private JLabel cntLabel;

    private boolean init_done = false;
    private JButton load, lightLoad;

    private static final int REMOTE_MODE = 1;
    private static final int LOCAL_MODE = 2;
    private static final int RESULT_MODE = 3;
    private int mode = REMOTE_MODE; // future use

    static final boolean SUPPORT_DISPLAYER = true;

    /*
    static DateFormat dateFormat;

    static {
	dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
	dateFormat.setTimeZone(TimeZone.getDefault());
    }
    */

    // trees
    private JTree remoteTree;
    private JTree localTree;
    private JTree resultTree;

    // menubar
    private JMenu fileMenu;
    private JMenu localProfilesMenu;
    private JMenu resultMenu;

    // file menu
    private JMenuItem remProfMenuItem;
    private JMenuItem locProfMenuItem;
    private JMenuItem resProfMenuItem;
    private JMenuItem refreshMenuItem;

    // local profile menu
    private JMenuItem delSelProfMenuItem;
    private JMenuItem delAllProfMenuItem;

    // result menu
    private JMenuItem delSelResMenuItem;
    private JMenuItem delAllResMenuItem;
    private JMenuItem expAllResMenuItem;
    private JMenuItem sortByDateAscResMenuItem;
    private JMenuItem sortByDateDescResMenuItem;
    private JMenuItem sortByOPAscResMenuItem;
    private JMenuItem sortByOPDescResMenuItem;

    private JButton cancelB;

    static final String IMPORT_DATA_DIALOG = "ImportDataDialog";
    static final String EXT = ProfileSerialUtils.DISPLAY_SUFFIX;
    static final int EXT_LEN = EXT.length();

    static final boolean USE_IMPORT_DATA_TASK = true;

    public static void init(GlobalContext globalContext) {
	globalContext.put(IMPORT_DATA_DIALOG,
			  new ImportDataDialog(globalContext));
    }

    public static void pop(GlobalContext globalContext, View view,
			   int n) {
	ImportDataDialog dialog =
	    (ImportDataDialog) globalContext.get(IMPORT_DATA_DIALOG);
	dialog.pop(view, n);
    }

    static void hide(GlobalContext globalContext, View view) {
	ImportDataDialog dialog =
	    (ImportDataDialog) globalContext.get(IMPORT_DATA_DIALOG);
	if (dialog.view == view) {
	    dialog.setVisible(false);
	}
    }

    private static boolean needRefreshResults = false;

    static void needRefreshResults() {
	needRefreshResults = true;
    }

    private ImportDataDialog(GlobalContext globalContext) {
	super(new Frame(), VAMPUtils.getTitle() + ": Import Data");

	this.globalContext = globalContext;
	view = null;
    }

    private void setSelected(int cnt) {
	String text = "    ";
	if (cnt == 0) {
	    text += "No";
	}
	else {
	    if (cnt < 10) {
		text += " ";
	    }
	    text += cnt;
	}
	
	cntLabel.setText(text + " profile" + (cnt != 1 ? "s" : "") +
			 " selected" + (cnt == 1 ? " " : ""));
    }

    private int getSelectedCount(TreePath paths[]) {
	if (paths == null) {
	    return 0;
	}

	int cnt = 0;
	for (int n = 0; n < paths.length; n++) {
	    DefaultMutableTreeNode path =
		(DefaultMutableTreeNode)paths[n].getLastPathComponent();
	    Object uobj = path.getUserObject();
	    if (uobj instanceof ImportDataItem || (uobj instanceof FileString && !((FileString)uobj).file.isDirectory())) {
		cnt++;
	    }
	}

	return cnt;
    }

    static int NN = 0;

    private void init() {

	JTree tree;

	if (mode == REMOTE_MODE) {
	    if (remoteTree == null) {
		remoteTree = makeRemoteTree();
	    }
	    tree = remoteTree;
	}
	else if (mode == LOCAL_MODE) {
	    if (localTree == null) {
		localTree = makeLocalTree();
	    }
	    tree = localTree;
	}
	else if (mode == RESULT_MODE) {
	    if (resultTree == null) {
		resultTree = makeResultTree();
	    }
	    tree = resultTree;
	}
	else {
	    tree = null;
	}

	if (tree == null) {
	    getToolkit().beep();
	    return;
	}

	if (init_done) {
	    scrollPane.setViewportView(tree);
	    setSelected(0);
	    if (mode == LOCAL_MODE) {
		TreePath p = localTree.getLeadSelectionPath();
		localTree.expandPath(p);
	    }
	    return;
	}

	setJMenuBar(makeMenuBar());

	scrollPane = new JScrollPane(tree);

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(scrollPane, BorderLayout.CENTER);

	JPanel buttonPanel = new JPanel(new FlowLayout());

	load = new JButton("Import data");
	load.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    importPerform(true);
		}
	    });

	buttonPanel.add(load);

	lightLoad = new JButton("Light Import data");
	lightLoad.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    importPerform(false);
		}
	    });

	buttonPanel.add(lightLoad);

	cancelB = new JButton("Cancel");
	cancelB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });

	buttonPanel.add(cancelB);

	cntLabel = new JLabel("");

	setSelected(0);

	cntLabel.setFont(new Font("MonoSpaced", Font.BOLD, 12));
	buttonPanel.add(cntLabel);

	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	pack();
	Utils.augment(this, 1.4, 1.5);
	Utils.centerOnScreen(this, false);

	init_done = true;
    }

    private ImportDataDialog getThis() {
	return this;
    }

    private JMenuBar makeMenuBar() {
	JMenuItem menuItem;
        JMenuBar menuBar = new JMenuBar();

        fileMenu = new JMenu("File");

        menuBar.add(fileMenu);

	// FUTURE
	/*
	menuItem = new JMenuItem("Select Profiles from File");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.out.println("Select Profiles from File");
		}
	    });

	fileMenu.add(menuItem);
	// for now
	menuItem.setEnabled(false);

	menuItem = new JMenuItem("Save Selected Profiles");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.out.println("Save Selected Profiles");
		}
	    });

	fileMenu.add(menuItem);
	// for now
	menuItem.setEnabled(false);
	*/

	/*
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
	*/

	remProfMenuItem = new JMenuItem("Remote Profiles");
	remProfMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    mode = REMOTE_MODE;
		    load.setEnabled(true);
		    lightLoad.setEnabled(true);
		    cntLabel.setEnabled(true);

		    remProfMenuItem.setEnabled(false);
		    locProfMenuItem.setEnabled(true);
		    resProfMenuItem.setEnabled(true);

		    localProfilesMenu.setEnabled(false);
		    delSelProfMenuItem.setEnabled(false);
		    delAllProfMenuItem.setEnabled(false);

		    resultMenu.setEnabled(false);
		    delSelResMenuItem.setEnabled(false);
		    delAllResMenuItem.setEnabled(false);

		    refreshMenuItem.setEnabled(true);
		    cancelB.setText("Cancel");

		    Cursor c = Utils.setWaitCursor(getThis());
		    init();
		    setCursor(c);

		    TreePath paths[] = remoteTree.getSelectionPaths();
		    setSelected(getSelectedCount(paths));
		}
	    });

	fileMenu.add(remProfMenuItem);

	locProfMenuItem = new JMenuItem("Local Profiles");
	locProfMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    localTree = null; // why
		    mode = LOCAL_MODE;
		    load.setEnabled(false);
		    lightLoad.setEnabled(false);

		    remProfMenuItem.setEnabled(true);
		    locProfMenuItem.setEnabled(false);
		    resProfMenuItem.setEnabled(true);

		    localProfilesMenu.setEnabled(true);
		    delSelProfMenuItem.setEnabled(true);
		    delAllProfMenuItem.setEnabled(true);

		    resultMenu.setEnabled(false);
		    delSelResMenuItem.setEnabled(false);
		    delAllResMenuItem.setEnabled(false);

		    refreshMenuItem.setEnabled(true);
		    cancelB.setText("Cancel");

		    Cursor c = Utils.setWaitCursor(getThis());
		    init();
		    setCursor(c);
		}
	    });

	fileMenu.add(locProfMenuItem);

	resProfMenuItem = new JMenuItem("Tool Results");
	resProfMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    //resultTree = null;
		    mode = RESULT_MODE;
		    load.setEnabled(true);
		    lightLoad.setEnabled(true);

		    remProfMenuItem.setEnabled(true);
		    locProfMenuItem.setEnabled(true);
		    resProfMenuItem.setEnabled(false);

		    localProfilesMenu.setEnabled(false);
		    delSelProfMenuItem.setEnabled(false);
		    delAllProfMenuItem.setEnabled(false);

		    resultMenu.setEnabled(true);
		    delSelResMenuItem.setEnabled(true);
		    delAllResMenuItem.setEnabled(true);

		    refreshMenuItem.setEnabled(true);
		    cancelB.setText("Cancel");

		    Cursor c = Utils.setWaitCursor(getThis());
		    init();
		    setCursor(c);
		}
	    });

	fileMenu.add(resProfMenuItem);

	fileMenu.addSeparator();

	refreshMenuItem = new JMenuItem("Refresh View");
	refreshMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (mode == REMOTE_MODE) {
			remoteTree = null;
		    }
		    else if (mode == LOCAL_MODE) {
			localTree = null;
		    }
		    else if (mode == RESULT_MODE) {
			resultTree = null;
		    }
		    Cursor c = Utils.setWaitCursor(getThis());
		    init();
		    setCursor(c);
		}
	    });
	fileMenu.add(refreshMenuItem);

        localProfilesMenu = new JMenu("Manage Local Profiles");

	localProfilesMenu.addMenuListener(new MenuListener() {
		public void menuCanceled(MenuEvent e) { }
		public void menuDeselected(MenuEvent e) { }
		public void menuSelected(MenuEvent e) {
		    delSelProfMenuItem.setEnabled(localTree != null && localTree.getSelectionPaths() != null && localTree.getSelectionPaths().length > 0);
		}
	    });

        menuBar.add(localProfilesMenu);

	delSelProfMenuItem = new JMenuItem("Delete Selected Profiles");
	delSelProfMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    localPerform(false);
		}
	    });
	localProfilesMenu.add(delSelProfMenuItem);

	delAllProfMenuItem = new JMenuItem("Delete All Profiles");
	delAllProfMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    localPerform(true);
		}
	    });
	localProfilesMenu.add(delAllProfMenuItem);

	localProfilesMenu.setEnabled(false);

        resultMenu = new JMenu("Manage Results");

	resultMenu.addMenuListener(new MenuListener() {
		public void menuCanceled(MenuEvent e) { }
		public void menuDeselected(MenuEvent e) { }
		public void menuSelected(MenuEvent e) {
		    delSelProfMenuItem.setEnabled(localTree != null && localTree.getSelectionPaths() != null && localTree.getSelectionPaths().length > 0);
		}
	    });

        menuBar.add(resultMenu);

	delSelResMenuItem = new JMenuItem("Delete Selected Results");
	delSelResMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    localPerform(false);
		}
	    });
	resultMenu.add(delSelResMenuItem);

	delAllResMenuItem = new JMenuItem("Delete All Results");
	delAllResMenuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    localPerform(true);
		}
	    });
	resultMenu.add(delAllResMenuItem);

	resultMenu.setEnabled(false);

	remProfMenuItem.setEnabled(false);
	locProfMenuItem.setEnabled(true);
	resProfMenuItem.setEnabled(true);

	delSelResMenuItem.setEnabled(false);
	delAllResMenuItem.setEnabled(false);

	return menuBar;
    }

    private JTree makeRemoteTree() {
	
	String importDataURL = (String)globalContext.get(VAMPConstants.IMPORT_DATA_URL);
	XMLImportDataFactory factory = new XMLImportDataFactory(globalContext);

	JTree tree = factory.makeTree(importDataURL);
	tree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    TreePath paths[] = remoteTree.getSelectionPaths();
		    setSelected(getSelectedCount(paths));
		}
	    });

	return tree;
    }

    static class FileString {
	File file;
	boolean rootpath;

	FileString(File file, boolean rootpath) {
	    this.file = file;
	    this.rootpath = rootpath;
	}

	FileString(File file) {
	    this(file, false);
	}

	public String toString() {
	    String str = (rootpath ? "Local Profiles (" + file.getAbsolutePath() + ")" : file.getName());
	    return str.endsWith(EXT) ?  str.substring(0, str.length()-EXT_LEN) : str;
	}
    }

    static class ResultString {
	File file;
	boolean rootpath;
	ToolResultInfo info;
	int which = 0;
	ResultString rs = null;
	String serialFile;

	static final int OPERATION = 1;
	static final int DATE = 2;
	static final int VIEWTYPE = 3;
	static final int PARAMS = 4;
	static final int PROFILES = 5;

	ResultString(File file, boolean rootpath) {
	    this.file = file;
	    this.rootpath = rootpath;
	    if (rootpath) {
		return;
	    }

	    try {
		serialFile = file.getAbsolutePath();
		serialFile = serialFile.substring(0, serialFile.length()-EXT_LEN);
		boolean isLocked = isLocked(serialFile);
		ProfileUnserializer profUnserial = ProfileSerializerFactory.getInstance().getUnserializer(serialFile);
		Profile profile = profUnserial.readProfile(false);
		info = (ToolResultInfo)profile.getPropertyValue(VAMPProperties.ToolResultInfoProp);

		if (!isLocked) {
		    unlockFile(serialFile);
		}

		profUnserial.close();
		profUnserial = null;
		profile = null;
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	}

	GraphElement getProfile(boolean full) throws Exception {
	    if (rs != null) {
		return null;
	    }
	    return ToolResultManager.getInstance().makeGraphElement(serialFile, full);
	}

	ResultString(File file) {
	    this(file, false);
	}

	ResultString(ResultString rs, int which) {
	    this.which = which;
	    this.rs = rs;
	}

	public String getTitle() {
	    if (rs == null) {
		return "";
	    }

	    if (which == OPERATION) {
		return "Operation";
	    }

	    if (which == DATE) {
		return "Date";
	    }

	    if (which == VIEWTYPE) {
		return "View Type";
	    }

	    if (which == PARAMS) {
		return "Params";

	    }
	    if (which == PROFILES) {
		String s = rs.info.graphElementIDs.size() == 1 ? "" : "s";
		return "Profile" + s;
	    }

	    return "";
	}

	public String getValue() {
	    if (which == OPERATION) {
		return rs.info.opname;
	    }

	    if (which == DATE) {
		return rs.info.timestamp.toString();
		//return dateFormat.format(rs.info.timestamp);
	    }

	    if (which == VIEWTYPE) {
		String viewType = rs.info.viewType;
		if (viewType.length() == 0) {
		    return "any type of view";
		}
		return viewType + " View";
	    }

	    if (which == PARAMS) {
		return rs.info.params.toString();
	    }

	    if (which == PROFILES) {
		return rs.info.graphElementIDs.toString();
	    }

	    return "";
	}

	public String toString() {
	    if (rs != null) {
		return getTitle() + ": " + getValue();
	    }

	    if (rootpath) {
		return "Tool Results (" + file.getAbsolutePath() + ")";
	    }
	    return info.opname + " [" + info.timestamp + "]";
	}
    }

    static void makeLocalTree(DefaultMutableTreeNode rootNode, File dir) {
	File files[] = dir.listFiles();
	for (int n = 0; n < files.length; n++) {
	    File file = files[n];
	    if (file.isDirectory() || file.getName().endsWith(EXT)) {
		Object userObj = new FileString(file);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObj);
		rootNode.add(node);
		if (file.isDirectory()) {
		    makeLocalTree(node, file);
		}
	    }
	}
    }

    static void makeResultTree(DefaultMutableTreeNode rootNode, File dir) {
	File files[] = dir.listFiles();
	for (int n = 0; n < files.length; n++) {
	    File file = files[n];
	    if (file.isDirectory() || file.getName().endsWith(EXT)) {
		ResultString rs = new ResultString(file);

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(rs);
		rootNode.add(node);
		if (file.isDirectory()) {
		    makeResultTree(node, file);
		}
		else {
		    DefaultMutableTreeNode childNode;
		    childNode = new DefaultMutableTreeNode(new ResultString(rs, ResultString.DATE));
		    node.add(childNode);
		    childNode = new DefaultMutableTreeNode(new ResultString(rs, ResultString.OPERATION));
		    node.add(childNode);
		    childNode = new DefaultMutableTreeNode(new ResultString(rs, ResultString.VIEWTYPE));
		    node.add(childNode);
		    childNode = new DefaultMutableTreeNode(new ResultString(rs, ResultString.PARAMS));
		    node.add(childNode);
		    childNode = new DefaultMutableTreeNode(new ResultString(rs, ResultString.PROFILES));
		    node.add(childNode);
		}
	    }
	}
    }

    static final int FONT_SIZE = 10;
    static Font titleFont = new Font("SansSerif", Font.BOLD, FONT_SIZE);
    static Font labelFont = new Font("SansSerif", Font.PLAIN, FONT_SIZE);

    class ResultRenderer extends DefaultTreeCellRenderer {

	public ResultRenderer() {
	}

	public Component getTreeCellRendererComponent(JTree tree,
						      Object value,
						      boolean sel,
						      boolean expanded,
						      boolean leaf,
						      int row,
						      boolean hasFocus) {
	    
	    super.getTreeCellRendererComponent(tree, value, sel,
					       expanded, leaf, row,
					       hasFocus);

	    DefaultMutableTreeNode node =(DefaultMutableTreeNode)value;
	    ResultString rs = (ResultString)node.getUserObject();

	    if (rs.rs != null) {
		setIcon(null);
		JPanel panel = new JPanel();
		JLabel l;

		l = new JLabel(rs.getTitle() + ": ");
		l.setFont(titleFont);
		panel.add(l);

		l = new JLabel(rs.getValue());
		l.setFont(labelFont);
		panel.add(l);

		panel.setBackground(getBackground());
		Dimension size = panel.getPreferredSize();
		panel.setPreferredSize(new Dimension(size.width, FONT_SIZE + 8));
		
		return panel;
	    }

	    return this;
	}
    }

    private JTree makeResultTree() {
	File resultDir = ToolResultManager.getInstance().getResultDir(globalContext);
	DefaultMutableTreeNode rootNode;
	if (resultDir == null) {
	    rootNode = new DefaultMutableTreeNode("<no tool result directory>");
	    return new JTree(rootNode);
	}

	rootNode = new DefaultMutableTreeNode(new ResultString(resultDir, true));

	makeResultTree(rootNode, resultDir);

	JTree tree = new JTree(rootNode);

	tree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    TreePath paths[] = resultTree.getSelectionPaths();
		    setSelected(getSelectedCount(paths));
		}
	    });
	
	tree.setCellRenderer(new ResultRenderer());
	return tree;
    }

    private JTree makeLocalTree() {
	File dataDir = ImportData.getDataDir(globalContext);

	DefaultMutableTreeNode rootNode;
	if (dataDir == null) {
	    rootNode = new DefaultMutableTreeNode("<no local data directory>");
	    return new JTree(rootNode);
	}

	rootNode = new DefaultMutableTreeNode(new FileString(dataDir, true));

	makeLocalTree(rootNode, dataDir);

	JTree tree = expandTree(new JTree(rootNode));

	tree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
		    TreePath paths[] = localTree.getSelectionPaths();
		    setSelected(getSelectedCount(paths));
		}
	    });
	
	return tree;
    }

    private static JTree expandTree(JTree tree) {
	for (int n = 0; n < tree.getRowCount(); n++) {
	    tree.expandRow(n);
	}
	return tree;
    }

    private void pop(View view, int n) {
	this.view = view;
	this.nn = n;

	if (needRefreshResults) {
	    resultTree = null;
	    needRefreshResults = false;
	}

	localTree = null;

	init();

	setVisible(true);
	toFront();
    }

    private Vector<String> getParents(DefaultMutableTreeNode path) {
	Vector<String> parents = new Vector<String>();
	for (;;) {
	    path = (DefaultMutableTreeNode)path.getParent();
	    if (path == null)
		break;
	    String s = (String)path.getUserObject();
	    parents.add(s);
	}
	return parents;
    }

    private void importPerform(boolean full) {

	if (view == null) {
	    //System.out.println("Warning: double import avoided !");
	    return;
	}

	JTree tree;
	if (mode == REMOTE_MODE) {
	    tree = remoteTree;
	}
	else if (mode == RESULT_MODE) {
	    tree = resultTree;
	}
	else {
	    return;
	}

	TreePath paths[] = tree.getSelectionPaths();
	if (paths == null)
	    return;

	if (USE_IMPORT_DATA_TASK) {
	    LinkedList<GraphElement> graphElements = new LinkedList();
	    LinkedList urlList = new LinkedList();
	    LinkedList typeList = new LinkedList();
	    LinkedList serialList = new LinkedList();
	    LinkedList parentList = new LinkedList();
	    LinkedList chrList = new LinkedList();
	    LinkedList pangenList = new LinkedList();

	    try {
		for (int n = 0; n < paths.length; n++) {
		    DefaultMutableTreeNode path =
			(DefaultMutableTreeNode)paths[n].getLastPathComponent();

		    Object uobj = path.getUserObject();

		    if (uobj instanceof ResultString) {
			ResultString rs = (ResultString)uobj;
			if (rs.which != 0) {
			    continue;
			}
			GraphPanel panel = view.getGraphPanelSet().getPanel(nn);
			GraphElementDisplayer graphDisplayer = null;
			AxisDisplayer axisDisplayer = null;

			if (rs.info.grphDispName.length() > 0) {
			    if ((rs.info.flags & ToolResultManager.PRIVATE_DISPLAYERS) != 0) {
				graphDisplayer = GraphElementDisplayer.get(rs.info.grphDispName);
				assert graphDisplayer != null;
			    }
			    else if (!panel.getDefaultGraphElementDisplayer().getName().equals(rs.info.grphDispName)) {
				if ((rs.info.flags & ToolResultManager.PRIVATE_DISPLAYERS_IF_DIFFERENT) != 0) {
				    graphDisplayer = GraphElementDisplayer.get(rs.info.grphDispName);
				}
				else if ((rs.info.flags & ToolResultManager.NEW_VIEW_IF_DIFFERENT_DISPLAYERS) != 0) {
				    InfoDialog.pop(view.getGlobalContext(), "Needs a " + rs.info.viewType + " View to see the tool result " + rs.info.opname);
				    return;
				}
			    }
			}

			if (rs.info.axisDispName.length() > 0) {
			    if ((rs.info.flags & ToolResultManager.PRIVATE_DISPLAYERS) != 0) {
				axisDisplayer = AxisDisplayer.get(rs.info.axisDispName);
				System.out.println("Axis Name [" + rs.info.axisDispName + "]");
				assert axisDisplayer != null;
			    }
			    else if (!panel.getDefaultGraphElementDisplayer().getName().equals(rs.info.axisDispName)) {
				if ((rs.info.flags & ToolResultManager.PRIVATE_DISPLAYERS_IF_DIFFERENT) != 0) {
				    axisDisplayer = AxisDisplayer.get(rs.info.axisDispName);
				}
				else if ((rs.info.flags & ToolResultManager.NEW_VIEW_IF_DIFFERENT_DISPLAYERS) != 0) {
				    InfoDialog.pop(view.getGlobalContext(), "Needs a " + rs.info.viewType + " View to see the tool result " + rs.info.opname);
				    return;
				}
			    }
			}

			GraphElement graphElement = rs.getProfile(full);
			if (graphElement != null) {
			    graphElement.setGraphElementDisplayer(graphDisplayer);
			    graphElement.setAxisDisplayer(axisDisplayer);
			    graphElements.add(graphElement);
			}
			continue;
		    }

		    if (!(uobj instanceof ImportDataItem)) {
			continue;
		    }

		    ImportDataItem item = (ImportDataItem)uobj;
		    urlList.add(item.getURL());
		    typeList.add(item.getType());
		    serialList.add(new Integer(item.getImportMode()));
		    chrList.add(item.getChrList());
		    pangenList.add(new Boolean(item.isPangen()));
		    parentList.add(getParents(path));
		}

		if (graphElements.size() > 0) {
		    // undo stack and so on: this code must be in ImportDataTask
		    GraphPanelSet panelSet = view.getGraphPanelSet();
		    GraphPanel panel = panelSet.getPanel(nn);
		    LinkedList wlist = new LinkedList();
		    wlist.addAll(panelSet.getGraphElements(nn));
		    wlist.addAll(graphElements);
		    panelSet.setGraphElements(wlist, nn);
		    panel.getCanvas().readaptSize();
		    view.repaint();
		}
		else if (urlList.size() > 0) {
		    ImportDataTask task = new ImportDataTask(globalContext, view,
							     nn, typeList, urlList,
							     serialList, parentList,
							     chrList, pangenList,
							     false, full);
		    task.start();
		}
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	}
	else {
	    Cursor cursor = Utils.setWaitCursor(this);
	    long ms0 = System.currentTimeMillis();

	    LinkedList list = new LinkedList();
	    for (int n = 0; n < paths.length; n++) {
		DefaultMutableTreeNode path =
		    (DefaultMutableTreeNode)paths[n].getLastPathComponent();
		Object uobj = path.getUserObject();
		if (!(uobj instanceof ImportDataItem))
		    continue;
		Vector<String> parents = getParents(path);

		ImportDataItem item = (ImportDataItem)uobj;
		String url = item.getURL();

		String type = item.getType();
		int import_mode = item.getImportMode();
		LinkedList ilist = ImportData.importData(globalContext, view,
							 view.getPanel(nn), type, url, import_mode, parents, null, true, false, true);
		if (ilist != null)
		    list.addAll(ilist);
	    }

	    if (list.size() != 0) {
		GraphPanelSet panelSet = view.getGraphPanelSet();
	    
		LinkedList wlist = new LinkedList();
		wlist.addAll(panelSet.getGraphElements(nn));
		wlist.addAll(list);
	    
		GraphPanel panel = panelSet.getPanel(nn);
		StandardVMStatement vmstat = new StandardVMStatement
		    (VMOP.getVMOP(VMOP.IMPORT), view.getPanel(nn));
		vmstat.beforeExecute();
		UndoVMStack.getInstance(panel).push(vmstat);

		panelSet.setGraphElements(wlist, nn);

		vmstat.afterExecute();

		panel.getCanvas().readaptSize();
	    }

	    setCursor(cursor);
	}

	view = null;
	setVisible(false);
    }

    static private HashMap<String, Boolean> lockMap = new HashMap();

    static String canonName(String filePath) {
	return filePath.replace('/', '\\');
    }

    static public void lockFile(String filePath) {
	filePath = canonName(filePath);
	//System.out.println("lockFile(" + filePath + ")");
	lockMap.put(filePath, true);
    }

    static public void unlockFile(String filePath) {
	filePath = canonName(filePath);
	//System.out.println("unlockFile(" + filePath + ")");
	lockMap.remove(filePath);
    }

    static private boolean isLocked(String filePath) {
	filePath = canonName(filePath);
	//System.out.println("isLocked(" + filePath + ") " + lockMap.get(filePath) + " " + lockMap.size());

	return lockMap.get(filePath) != null;
    }

    private void localPerform(boolean delete_all) {

	JTree tree;

	if (mode == LOCAL_MODE) {
	    tree = localTree;
	}
	else if (mode == RESULT_MODE) {
	    tree = resultTree;
	}
	else {
	    return;
	}

	if (delete_all) {
	    selectAll(tree);
	}

	TreePath paths[] = tree.getSelectionPaths();
	if (paths == null) {
	    return;
	}

	String err_msg = "";
	int file_cnt = 0;
	for (int n = 0; n < paths.length; n++) {
	    DefaultMutableTreeNode path =
		(DefaultMutableTreeNode)paths[n].getLastPathComponent();

	    File file = null;
	    if (mode == LOCAL_MODE) {
		FileString fs = (FileString)path.getUserObject();
		file = fs.file;
		if (file.isDirectory()) {
		    continue;
		}
	    }
	    else if (mode == RESULT_MODE) {
		ResultString rs = (ResultString)path.getUserObject();
		if (rs.which != 0) {
		    continue;
		}

		if (rs.rootpath) {
		    continue;
		}
		file = new File(rs.serialFile);
		if (file.isDirectory()) {
		    continue;
		}
		file = new File(rs.serialFile + ProfileSerialUtils.DISPLAY_SUFFIX);
	    }

	    file_cnt++;

	    String filePath = file.getAbsolutePath();
	    String canonPath = filePath.substring(0, filePath.length()-EXT_LEN);
	    if (isLocked(canonPath)) {
		if (err_msg.length() > 0) {
		    err_msg += "\n";
		}
		err_msg += "Cannot delete " + filePath + ": file is currently used by VAMP";
		continue;
	    }

	    //File dspFile = file;
	    File dspFile = new File(canonPath + ProfileSerialUtils.DISPLAY_SUFFIX);
	    File griFile = new File(canonPath + GraphicInfo.GRINFO_SUFFIX);
	    File prpFile = new File(canonPath + ProfileSerialUtils.PROP_SUFFIX);
	    boolean success = true;
	    if (dspFile.exists() && !dspFile.delete()) {
		System.out.println("cannot delete dspFile");
		success = false;
	    }
	    if (griFile.exists() && !griFile.delete()) {
		System.out.println("cannot delete griFile");
		success = false;
	    }
	    if (prpFile.exists() && !prpFile.delete()) {
		System.out.println("cannot delete prpFile");
		success = false;
	    }

	    File dspMD5File = new File(filePath.substring(0, filePath.length()-EXT_LEN) + ProfileSerialUtils.DISPLAY_SUFFIX + ImportData.MD5_EXT);

	    File griMD5File = new File(filePath.substring(0, filePath.length()-EXT_LEN) + GraphicInfo.GRINFO_SUFFIX + ImportData.MD5_EXT);

	    File prpMD5File = new File(filePath.substring(0, filePath.length()-EXT_LEN) + ProfileSerialUtils.PROP_SUFFIX + ImportData.MD5_EXT);

	    if (dspMD5File.exists() && !dspMD5File.delete()) {
		System.out.println("cannot delete dspMD5File");
		success = false;
	    }
	    if (griMD5File.exists() && !griMD5File.delete()) {
		System.out.println("cannot delete griMD5File");
		success = false;
	    }
	    if (prpMD5File.exists() && !prpMD5File.delete()) {
		System.out.println("cannot delete prpMD5FilepFile");
		success = false;
	    }

	    if (!success) {
		if (err_msg.length() > 0) {
		    err_msg += "\n";
		}
		err_msg += "Cannot delete " + filePath + ": an error occurs";
	    }
	    //((DefaultMutableTreeNode)path.getParent()).remove(path);
	    //path.removeFromParent();
	    //path.getParent().
	    //tree.removeSelectionPath(paths[n]);
	}

	// remove empty directories
	if (!delete_all) {
	    selectAll(tree);
	    paths = tree.getSelectionPaths();
	    if (paths == null) {
		return;
	    }
	}

	boolean error = false;
	int cnt = 0;
	final int MAX_ITERS = 100;

	do {
	    error = false;
	    for (int n = 0; n < paths.length; n++) {
		DefaultMutableTreeNode path =
		    (DefaultMutableTreeNode)paths[n].getLastPathComponent();
		    
		File file = null;

		if (mode == LOCAL_MODE) {
		    FileString fs = (FileString)path.getUserObject();
		    if (fs.rootpath) {
			continue;
		    }
		    file = fs.file;
		}
		else if (mode == RESULT_MODE) {
		    ResultString rs = (ResultString)path.getUserObject();
		    if (rs.which != 0) {
			continue;
		    }
		    if (rs.rootpath) {
			continue;
		    }
		    file = new File(rs.serialFile);
		}
		    
		if (file.isDirectory()) {
		    if (!file.delete()) {
			error = true;
		    }
		}
	    }
	    cnt++;
	}

	while (error && cnt < MAX_ITERS);

	if (file_cnt != 0) {
	    if (mode == LOCAL_MODE) {
		localTree = null;
	    }
	    else if (mode == RESULT_MODE) {
		resultTree = null;
	    }
	    init();
	}

		
	if (err_msg.length() > 0) {
	    InfoDialog.pop(view.getGlobalContext(), err_msg);
	}
    }

    static void selectAll(JTree tree) {
	int selRows[] = new int[tree.getRowCount()];
	for (int n = 0; n < selRows.length; n++) {
	    selRows[n] = n;
	}
	tree.setSelectionRows(selRows);
    }
}
