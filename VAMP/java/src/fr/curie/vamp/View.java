
/*
 *
 * View.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.applet.*;

public class View extends JPanel {

    private InfoPanel infoPanel;
    private ZoomPanel zoomPanel;
    private ColorLegendPanel colorLegendPanel;
    private SearchGraphElementPanel searchGraphElementPanel;
    private SearchDataElementPanel searchDataElementPanel;
    private ThresholdsPanel thresholdPanel;
    private GraphPanelSet graphPanelSet;

    private InfoDisplayer infoDisplayer;
    private boolean running_mode;
    private String name;
    private String orig_name;

    private GlobalContext globalContext;
    private ViewFrame viewFrame;
    private PrintPreviewer previewer = null;
    private static AxisDisplayer stdAxisDisplayer =
	Config.defaultGenomicPositionAxisDisplayer;
    private String message;

    static final int ALL = ~0;

    private PanelProfile panelProfiles[];
    private PanelLinks panelLinks[];
    private Menu _newViewMenu, _currentViewMenu;

    View(GlobalContext globalContext,
	 String name,
	 PanelProfile panelProfiles[],
	 PanelLayout panelLayout,
	 PanelLinks panelLinks[],
	 Menu _newViewMenu,
	 Menu _currentViewMenu,
	 LinkedList graphElements, // which panel ??
	 InfoDisplayer infoDisplayer,
	 Dimension size) {

	setBackground(VAMPResources.getColor(VAMPResources.VIEW_BG));
	this.panelProfiles = panelProfiles;
	this.globalContext = globalContext;
	this.name = name;

	if (_newViewMenu == null)
	    _newViewMenu = Config.newViewMenu;
	this._newViewMenu = _newViewMenu;

	if (_currentViewMenu == null)
	    _currentViewMenu = Config.currentViewMenu_std;
	this._currentViewMenu = _currentViewMenu;

	if (infoDisplayer == null)
	    infoDisplayer = new StandardInfoDisplayer();

	this.infoDisplayer = infoDisplayer;

	infoPanel = new InfoPanel(this);

	searchGraphElementPanel = new SearchGraphElementPanel(panelProfiles);
	searchDataElementPanel = new SearchDataElementPanel(panelProfiles);

	graphPanelSet = new GraphPanelSet(this, panelProfiles,
					  panelLayout, panelLinks);
	graphPanelSet.setGraphElements(graphElements);

	zoomPanel = new ZoomPanel(graphPanelSet, panelProfiles,
				  panelLinks, size, graphElements);

	colorLegendPanel = new ColorLegendPanel(this);
	thresholdPanel = new ThresholdsPanel(this);

	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
	tabbedPane.addTab("Zooming", zoomPanel);
	//tabbedPane.addTab("Search", searchPanel);

	tabbedPane.addTab("Search Arrays", searchGraphElementPanel);
	//tabbedPane.addTab("Search Probes", searchDataElementPanel);
	tabbedPane.addTab("Search Elements", searchDataElementPanel);

	tabbedPane.addTab("Color Legend", colorLegendPanel);
	tabbedPane.addTab("Y Range", thresholdPanel);

	tabbedPane.setBackground(VAMPResources.getColor
				 (VAMPResources.TAB_BG));
	tabbedPane.setForeground(Color.BLACK);
	tabbedPane.setFont(VAMPResources.getFont(VAMPResources.TABBED_PANE_FONT));
	JSplitPane jpaneSouthWest = infoPanel;

	//int tbHeight = computeTBHeight(zoomPanel);
	//tabbedPane.setPreferredSize(new Dimension(100, tbHeight));

	/*
	JPanel jpaneNorthWest = new JPanel(new BorderLayout());
	jpaneNorthWest.add(tabbedPane, BorderLayout.NORTH);	
	jpaneNorthWest.add(jpaneSouthWest, BorderLayout.CENTER);
	*/
	JSplitPane jpaneNorthWest = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
						   tabbedPane, jpaneSouthWest);
        jpaneNorthWest.setDividerSize(2);
	jpaneNorthWest.setBackground(VAMPResources.getColor
				     (VAMPResources.INFO_PANEL_BG));

	JSplitPane jpaneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					      jpaneNorthWest, graphPanelSet);
        jpaneMain.setOneTouchExpandable(true);
        jpaneMain.setDividerSize(2);
	jpaneMain.setResizeWeight(0.);
	//        jpaneMain.setDividerLocation(220);
        jpaneMain.setDividerLocation(260);

	setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	add(jpaneMain);
	
	jpaneMain.setBackground(Color.WHITE);
	setBackground(Color.RED);
	getViewList().add(this);
    }

    void setViewFrame(ViewFrame viewFrame) {
	this.viewFrame = viewFrame;
	graphPanelSet.setTopTitle();
    }

    void setTitle(String title) {
	if (viewFrame != null)
	    viewFrame.setTitle(title);
    }

    String getTitle() {
	if (viewFrame != null)
	    return viewFrame.getTitle();
	return "";
    }

    JMenuBar makeMenuBar(JFrame frame) {
        JMenuBar menuBar;

        menuBar = new JMenuBar();
	makeFileMenu(frame, menuBar);
	makeViewsMenu(frame, menuBar);
	makeEditMenu(frame, menuBar);
	makeToolsMenu(frame, menuBar);
	makeHelpMenu(frame, menuBar);

	return menuBar;
    }

    void makeSaveSubmenu(JFrame frame, JMenu fileMenu) {
        JMenuItem menuItem;
	JMenu saveMenu = new JMenu("Save View");
	fileMenu.add(saveMenu);
	menuItem = new JMenuItem("Current View");
	menuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    Frame frame = (Frame)getValue();
		    savePerform(frame, false);
		}
	    });

	saveMenu.add(menuItem);

	/*
	if (globalContext.getAppletContext() != null) {
	    menuItem = new JMenuItem("All");
	    menuItem.addActionListener(new ActionListenerWrapper(frame) {
		    public void actionPerformed(ActionEvent e) {
			Frame frame = (Frame)getValue();
			savePerform(frame, true);
		    }
		});
	    
	    saveMenu.add(menuItem);
	    // WARNING : 19/12/03 disabling all Save (asked by PLR because of
	    // security reason on data)
	    menuItem.setEnabled(false);
	}
	*/
    }

    void makeLoadSubmenu(JFrame frame, JMenu fileMenu) {
	JMenu loadMenu = new JMenu("Load View");
	fileMenu.add(loadMenu);

	JMenuItem menuItem;

	menuItem = new JMenuItem("in New View");
	menuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    Frame frame = (Frame)getValue();
		    File file = DialogUtils.openFileChooser
			(frame, "Load View", DialogUtils.XML_FILE_FILTER, false);
		    if (file == null)
			return;

		    if (!Utils.checkRead(globalContext, file))
			return;

		    Cursor cursor = Utils.setWaitCursor(getThis());
		    XMLLoadDataFactory ldf =
			new XMLLoadDataFactory(globalContext, true, false);
		    try {
			FileInputStream is = new FileInputStream(file);
			Object valarr[] = new Object[4];
			Task task = new Task();
			valarr[0] = file;
			valarr[1] = ldf;
			valarr[2] = is;
			valarr[3] = task;
			
			LoadOperation op = new LoadOperation(valarr, "Loading profiles in new view...") {

				ViewFrame vf;
				public void perform1() {
				}

				public void perform2() {
				    Object o[] = (Object[])value;
				    File file = (File)o[0];
				    XMLLoadDataFactory ldf = (XMLLoadDataFactory)o[1];
				    InputStream is = (InputStream)o[2];
				    Task task = (Task)o[3];

				    vf = ldf.makeViewFrame(globalContext, is,
							   file.getName(), task);
				}

			    };

			task.setOperation(op);
			task.start();
		    }
		    catch (Exception ex) {
			ex.printStackTrace();
			InfoDialog.pop(globalContext, "loading: ", ex);
		    }
		    Utils.setCursor(getThis(), cursor);
		}
	    });

	loadMenu.add(menuItem);

	menuItem = new JMenuItem("replace in Current View");
	menuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    load((Frame)getValue(), true);
		}
	    });

	loadMenu.add(menuItem);

	menuItem = new JMenuItem("add in Current View");
	menuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    load((Frame)getValue(), false);
		}
	    });

	loadMenu.add(menuItem);
    }

    void makeConfigurationSubmenu(JFrame frame, JMenu fileMenu) {
	JMenu confMenu = new JMenu("Configuration");
	fileMenu.add(confMenu);

	JMenuItem menuItem = new JMenuItem("Load");
	menuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    Frame frame = (Frame)getValue();
		    File file = DialogUtils.openFileChooser
			(frame, "Load", DialogUtils.XML_FILE_FILTER, false);

		    if (file == null)
			return;

		    if (!Utils.checkRead(globalContext, file))
			return;

		    try {
			FileInputStream is =  new FileInputStream(file);
			VAMPResources.read(globalContext, is);
			is.close();
			ColorCodes.init(globalContext);
			Thresholds.init(globalContext);
			//repaintAllViews();
			syncAll(globalContext, false);
		    }
		    catch(Exception exc) {
			InfoDialog.pop(getGlobalContext(), exc.getMessage());
		    }
		}
	    });

	confMenu.add(menuItem);

	menuItem = new JMenuItem("Save");
	menuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    Frame frame = (Frame)getValue();
		    File file = DialogUtils.openFileChooser
			(frame, "Save", DialogUtils.XML_FILE_FILTER, true);
		    if (file == null) return;
		    if (!Utils.isXMLFile(file.getName()))
			file = new File(file.getAbsolutePath() + ".xml");

		    try {
			FileOutputStream os =  new FileOutputStream(file);
			VAMPResources.write(globalContext, os);
			os.close();
		    }
		    catch(IOException exc) {
			System.out.println(exc);
			//InfoDialog.pop(getGlobalContext(), exc.getMessage());
		    }
		}
	    });

	confMenu.add(menuItem);

	if (VAMPResources.hasDefaultConfig()) {
	    menuItem = new JMenuItem("Restore Default");
	    menuItem.addActionListener(new ActionListenerWrapper(frame) {
		    public void actionPerformed(ActionEvent e) {
			VAMPResources.reset(globalContext, true);
			ColorCodes.init(globalContext);
			syncAll(globalContext, false);
		    }
		});
	    confMenu.add(menuItem);
	}

	menuItem = new JMenuItem("Restore Factory Default");
	menuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    VAMPResources.reset(globalContext, false);
		    ColorCodes.init(globalContext);
		    syncAll(globalContext, false);
		}
	    });

	confMenu.add(menuItem);
    }

    void makeImportSubmenu(JFrame frame, JMenu fileMenu) {
	if (multiplePanels()) {
	    JMenu importMenu = new JMenu("Import");
	    
	    for (int n = 0; n < panelProfiles.length; n++) {
		JMenuItem item = new JMenuItem("in " +
					       panelProfiles[n].getName() +
					       " Panel");
		item.addActionListener(new ActionListenerWrapper(new int[]{n}) {
			public void actionPerformed(ActionEvent e) {
			    int which[] = (int[])getValue();
			    ImportDataDialog.pop(globalContext, getThis(),
						 which[0]);
			}
		    });
		importMenu.add(item);

		Vector v = new Vector();
		v.add(item);
		v.add(new Integer(n));
		importMenu.addMenuListener(new MenuListenerWrapper(v) {
			public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
			    Vector v = (Vector)getValue();
			    JMenuItem item = (JMenuItem)v.get(0);
			    int which = ((Integer)v.get(1)).intValue();
			    item.setEnabled(!isReadOnly(which));
			}
		});
	    }

	    fileMenu.add(importMenu);
	    return;
	}

	JMenuItem importMenuItem = new JMenuItem("Import");
	importMenuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    ImportDataDialog.pop(globalContext, getThis(), 0);
		}
	    });

	fileMenu.add(importMenuItem);

	fileMenu.addMenuListener(new MenuListenerWrapper(importMenuItem) {
			public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
			    JMenuItem item = (JMenuItem)getValue();
			    item.setEnabled(!isReadOnly(0));
			}
		});

	/*
	// to disapear
	JMenuItem importTestMenuItem = new JMenuItem("Import Test");
	importTestMenuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    ImportTestDialog.pop(getThis(), 0);
		}
	    });

	fileMenu.add(importTestMenuItem);
	*/
    }

    void makeUpdateSubmenu(JFrame frame, JMenu fileMenu) {

	JMenuItem importMenuItem = new JMenuItem("Update");
	importMenuItem.addActionListener(new ActionListenerWrapper(frame) {
		public void actionPerformed(ActionEvent e) {
		    String msg = "";
		    for (int m = 0; m < panelProfiles.length; m++) {
			Vector v = getSelectedGraphElements(m);
			int size = v.size();
			LinkedList urlList = new LinkedList();
			LinkedList typeList = new LinkedList();
			LinkedList serialList = new LinkedList();
			for (int n = 0; n < size; n++) {
			    GraphElement graphElement = (GraphElement)v.get(n);
			    String URL = graphElement.getURL();
			    if (URL == null) {
				String srcURL = graphElement.getSourceURL();
				if (srcURL == null || srcURL.length() == 0) {
				    msg += VAMPUtils.getMessageName(graphElement) + " CANNOT be updated (reimport the profile and update it)\n";
				    continue;
				}
				URL = srcURL;
			    }

			    String srcType = graphElement.getSourceType();
			    if (srcType == null) {
				msg += VAMPUtils.getMessageName(graphElement) + " CANNOT be updated (reimport the profile and update it)\n";
				continue;
			    }

			    if (!VAMPUtils.isPanGenomicURL(URL))
				srcType = "";

			    urlList.add(URL);
			    typeList.add(srcType);
			    serialList.add(new Boolean(graphElement.asProfile() != null));
			    
			}
		    
			if (msg.length() > 0) {
			    InfoDialog.pop(globalContext, msg);
			}

			if (urlList.size() > 0) {
			    ImportDataTask task = new ImportDataTask(globalContext, getThis(), m, typeList, urlList, serialList, null, null, null, true, true);
			    
			    task.start();
			}
		    }
		}
	    });

	fileMenu.add(importMenuItem);

	fileMenu.addMenuListener(new MenuListenerWrapper(importMenuItem) {
		public void menuCanceled(MenuEvent e) { }
		public void menuDeselected(MenuEvent e) { }
		public void menuSelected(MenuEvent e) {
		    JMenuItem item = (JMenuItem)getValue();
		    item.setEnabled(!isReadOnly(0) && getSelectedGraphElements(View.ALL).size() > 0);
		}
	    });
    }

    void makeExportSubmenu(JFrame frame, JMenu fileMenu) {
	Vector exportTools = ExportTool.getTools();
	int tool_cnt = exportTools.size();
	if (tool_cnt > 0) {
	    JMenu exportMenu = new JMenu("Export");
	    fileMenu.add(exportMenu);
	    for (int n = 0; n < tool_cnt; n++) {
		ExportTool tool = (ExportTool)exportTools.get(n);
		JMenuItem menuItem = new JMenuItem(tool.getName());
		Vector v = new Vector();
		v.add(tool);
		v.add(this);
		v.add(menuItem);
	    
		menuItem.addActionListener(new ActionListenerWrapper(v) {
			public void actionPerformed(ActionEvent e) {
			    Vector v = (Vector)getValue();
			    ExportTool tool = (ExportTool)v.get(0);
			    View view = (View)v.get(1);
			    tool.perform(view);
		    }
		});

		exportMenu.addMenuListener(new MenuListenerWrapper(v) {
		    public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
			    Vector v = (Vector)getValue();
			    ExportTool tool = (ExportTool)v.get(0);
			    View view = (View)v.get(1);
			    JMenuItem item = (JMenuItem)v.get(2);
			    item.setEnabled(tool.isEnabled(view));
			}
		    });
		
		exportMenu.add(menuItem);
	    }
	}
    }

    void makePrintSubmenu(JFrame frame, JMenu fileMenu) {
	JMenu printMenu = new JMenu("Print");
	/*
	JMenuItem menuItem = new JMenuItem("Print Dialog");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (PrintDialog.isVisible(globalContext))
			getToolkit().beep();
		    else
			PrintDialog.pop(globalContext, getThis());
		}
	    });

	printMenu.add(menuItem);
	*/

	JMenuItem menuItem = new JMenuItem("Print Preview");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (PrintPreviewer.isVisible(globalContext))
			getToolkit().beep();
		    else
			PrintPreviewer.preview(globalContext, getThis(), null);
		}
	    });

	printMenu.add(menuItem);

	makePrintTemplates(printMenu);

	fileMenu.add(printMenu);
    }

    void makePrintTemplates(JMenu printMenu) {
	PrintPageTemplate.init(globalContext);
	Vector pageTemplate_v = PrintPageTemplate.getPageTemplates();
	int sz = pageTemplate_v.size();
	for (int n = 0; n < sz; n++) {
	    PrintPageTemplate pageTemplate =
		(PrintPageTemplate)pageTemplate_v.get(n);
	    if (!pageTemplate.isFileMenu())
		continue;
	    JMenuItem menuItem =
		new JMenuItem(pageTemplate.getName() + " Template");
	    menuItem.addActionListener(new ActionListenerWrapper(pageTemplate) {
		public void actionPerformed(ActionEvent e) {
			PrintPreviewer.preview(globalContext, getThis(),
					       (PrintPageTemplate)getValue());
		}
	    });

	    printMenu.add(menuItem);
	}
    }

    void makeFileMenu(JFrame frame, JMenuBar menuBar) {
        JMenu fileMenu = new JMenu("File");
	JMenuItem menuItem;
        menuBar.add(fileMenu);

	makeSaveSubmenu(frame, fileMenu);
	makeLoadSubmenu(frame, fileMenu);
	makeExportSubmenu(frame, fileMenu);
	makeImportSubmenu(frame, fileMenu);
	makeUpdateSubmenu(frame, fileMenu);
	makeConfigurationSubmenu(frame, fileMenu);

	makePrintSubmenu(frame, fileMenu);

	if (frame != null) {
	    menuItem = new JMenuItem("Close");
	    menuItem.addActionListener(new ActionListenerWrapper(frame) {
		    public void actionPerformed(ActionEvent e) {
			//((Frame)getValue()).setVisible(false);
			((Frame)getValue()).dispose();
		}
	    });
	    fileMenu.add(menuItem);

	    if (globalContext.getAppletContext() == null) {
		menuItem = new JMenuItem("Exit");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    System.exit(0);
			}
		    });
		fileMenu.add(menuItem);
	    }
	} else if (globalContext.getAppletContext() != null) {
	    menuItem = new JMenuItem("Close all Frames");
	    menuItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			closeAllFrames();
		    }
		});
	    fileMenu.add(menuItem);
	}
    }

    void makeViewsMenu(JFrame frame, JMenuBar menuBar) {
        JMenu viewsMenu = new JMenu("Views");
        menuBar.add(viewsMenu);
	JMenu newViewMenu = new JMenu("New View");
	viewsMenu.add(newViewMenu);

	_newViewMenu.buildMenu(newViewMenu);
	makeNewViewMenu(frame);

	JMenu currentViewMenu = new JMenu("Current View");
	viewsMenu.add(currentViewMenu);
	makeCurrentViewMenu(frame, currentViewMenu);

	//currentViewMenu.addSeparator();

	/*
	Vector builders = GraphElementIDBuilder.getIDBuilders();
	int size = builders.size();
	for (int n = 0; n < size; n++) {
	    GraphElementIDBuilder builder = (GraphElementIDBuilder)builders.get(n);
	    JMenuItem menuItem = new JMenuItem("Show " + builder.getName() +
					       " Name");
	    menuItem.addActionListener(new ActionListenerWrapper(builder) {
		    public void actionPerformed(ActionEvent e) {
			// TBD
			getPanel(0).setGraphElementIDBuilder((GraphElementIDBuilder)getValue());
			syncGraphElements();
		    }
		});

	    currentViewMenu.add(menuItem);
	}
	*/
    }

    void makeNewViewMenu(JFrame frame) {
	LinkedList viewMenuItems = new LinkedList();
	_newViewMenu.makeMenuItems(viewMenuItems);

	int cnt = viewMenuItems.size();

	for (int n = 0; n < cnt; n++) {
	    ViewMenuItem vmi = (ViewMenuItem)viewMenuItems.get(n);

	    if (!(vmi instanceof NewViewMenuItem))
		continue;

	    JMenuItem menuItem = vmi.getMenuItem();
	    menuItem.addActionListener(new ActionListenerWrapper(vmi) {
		    public void actionPerformed(ActionEvent e) {
			NewViewMenuItem vmi = (NewViewMenuItem)getValue();

			ViewFrame view = new ViewFrame
			    (globalContext, 
			     vmi.getViewName(),
			     vmi.getPanelProfiles(),
			     vmi.getPanelLayout(),
			     vmi.getPanelLinks(),
			     vmi.getNewViewMenu(),
			     vmi.getCurrentViewMenu(),
			     new LinkedList(),
			     vmi.getDefaultDim(),
			     vmi.getInfoDisplayer());
			view.setVisible(true);
		}
	    });
	}
    }

    void makeCurrentViewMenu(JFrame frame, JMenu currentViewMenu) {
	if (multiplePanels()) {
	    for (int n = 0; n < panelProfiles.length; n++) {
		JMenu menu = new JMenu
		    (panelProfiles[n].getName() + " Panel");
		currentViewMenu.add(menu);
		makeCurrentViewMenu(frame, getPanel(n), menu);
	    }
	    return;
	}

	makeCurrentViewMenu(frame, getPanel(0), currentViewMenu);

    }

    void makeCurrentViewMenu(JFrame frame, GraphPanel panel,
			     JMenu currentViewMenu) {
	_currentViewMenu.buildMenu(currentViewMenu);
	LinkedList viewMenuItems = new LinkedList();
	_currentViewMenu.makeMenuItems(viewMenuItems);
	/*
	Vector viewMenuItems = ViewMenuItem.getViewMenuItems();
	*/
	int cnt = viewMenuItems.size();

	for (int n = 0; n < cnt; n++) {
	    ViewMenuItem vmi = (ViewMenuItem)viewMenuItems.get(n);

	    //System.out.println("vmi: " + vmi.getDefaultName());
	    if (vmi instanceof NewViewMenuItem)
		continue;

	    /*
	    if (!(vmi instanceof CurrentViewMenuItem))
		continue;
	    */

	    if (!vmi.supportCurrentView())
		continue;

	    /*
	    if (vmi.addSeparator())
		currentViewMenu.addSeparator();
	    */

	    JMenuItem menuItem = vmi.getMenuItem();
	    //JMenuItem menuItem = new JMenuItem(vmi.getDefaultName());
	    Vector v = new Vector();
	    v.add(vmi);
	    v.add(menuItem);
	    v.add(panel);

	    menuItem.addActionListener(new ActionListenerWrapper(v) {
		    public void actionPerformed(ActionEvent e) {
			Vector v = (Vector)getValue();
			ViewMenuItem vmi = (ViewMenuItem)v.get(0);
			JMenuItem menuItem = (JMenuItem)v.get(1);
			GraphPanel panel = (GraphPanel)v.get(2);
			if (vmi.hasActionListener())
			    vmi.actionPerformed(getThis(), panel);
			else if (vmi instanceof CurrentViewMenuItem) {
			    CurrentViewMenuItem cvmi =
				(CurrentViewMenuItem)vmi;
			    AxisDisplayer axisDisplayer =
				panel.getDefaultAxisDisplayer();

			    panel.setAutoApplyDSLOP(cvmi.getAutoApplyDSLOP());
			    if (cvmi.getDefaultGraphElementDisplayer() != null)
				panel.setDefaultGraphElementDisplayer
				    (cvmi.getDefaultGraphElementDisplayer());

			    graphPanelSet.syncGraphElements(true);

			    boolean applyOP;
			    if (cvmi.getDefaultAxisDisplayer() != null &&
				cvmi.getDefaultGraphElementDisplayer() == null) {
				panel.setDefaultAxisDisplayer
				    (cvmi.getDefaultAxisDisplayer());
				applyOP = false;
			    }
			    else if (cvmi.getDefaultAxisDisplayer() != null &&
				     !panel.getDefaultGraphElementDisplayer().
				     isCompatible(axisDisplayer)) {
				panel.setDefaultAxisDisplayer
				    (cvmi.getDefaultAxisDisplayer());
				applyOP = true;
			    }
			    else {
				panel.setDefaultAxisDisplayer(axisDisplayer);
				applyOP = false;
			    }

			    // added 09/06/05
			    graphPanelSet.syncGraphElements(true, applyOP, false);
			}
		    }
		});

	    if (vmi.hasMenuListener()) {
		currentViewMenu.addMenuListener(new MenuListenerWrapper(v) {
			public void menuCanceled(MenuEvent e) { }
			public void menuDeselected(MenuEvent e) { }
			public void menuSelected(MenuEvent e) {
			    Vector v = (Vector)getValue();
			    ViewMenuItem vmi = (ViewMenuItem)v.get(0);
			    JMenuItem menuItem = (JMenuItem)v.get(1);
			    GraphPanel panel = (GraphPanel)v.get(2);
			    vmi.menuSelected(getThis(), panel,
					    menuItem);
			}
		    });
	    }

	    //currentViewMenu.add(menuItem);
	}

    //currentViewMenu.addSeparator();

	Vector builders = GraphElementIDBuilder.getIDBuilders();
	int size = builders.size();
	JMenu ordMenu = new JMenu("Ordinate");
	for (int n = 0; n < size; n++) {
	    GraphElementIDBuilder builder = (GraphElementIDBuilder)builders.get(n);
	    if (!builder.isMenuable()) continue;
	    Object args[] = new Object[]{builder, panel};
	    JMenuItem menuItem = new JMenuItem(builder.getName() +
					       " Name");
	    menuItem.addActionListener(new ActionListenerWrapper(args) {
		    public void actionPerformed(ActionEvent e) {
			Object args[] = (Object[])getValue();
			GraphElementIDBuilder idBuilder = (GraphElementIDBuilder)args[0];
			GraphPanel panel = (GraphPanel)args[1];
			panel.setGraphElementIDBuilder(idBuilder);
			syncGraphElements();
		    }
		});

	    //currentViewMenu.add(menuItem);
	    ordMenu.add(menuItem);
	}
	currentViewMenu.add(ordMenu);
    }

    void makeEditMenu(JFrame frame, JMenuBar menuBar) {
        JMenuItem menuItem;
	JMenu editMenu = new JMenu("Edit");

	menuBar.add(editMenu);

	if (multiplePanels()) {
	    for (int n = 0; n < panelProfiles.length; n++) {
		JMenu panelMenu = new JMenu(panelProfiles[n].getName() +
					    " Panel");
		editMenu.add(panelMenu);
		setStandardEditMenu(panelMenu, n);
	    }
	}
	else
	    setStandardEditMenu(editMenu, 0);
    }

    void makeToolsMenu(JFrame frame, JMenuBar menuBar) {

        JMenuItem menuItem;
	JMenu toolsMenu = new JMenu("Tools");

	menuBar.add(toolsMenu);

	if (multiplePanels()) {
	    for (int n = 0; n < panelProfiles.length; n++) {
		JMenu panelMenu = new JMenu(panelProfiles[n].getName() +
					    " Panel");
		toolsMenu.add(panelMenu);
		setStandardToolsMenu(panelMenu, n);
	    }
	}
 	else {
	    setStandardToolsMenu(toolsMenu, 0);
	}
    }

    void makeHelpMenu(JFrame frame, JMenuBar menuBar) {
	JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
	if (globalContext.getAppletContext() != null) {
	    SystemConfig systemConfig = (SystemConfig)globalContext.
		get(SystemConfig.SYSTEM_CONFIG);
	    String helpURL = systemConfig.getParameter("help:URL");
	    if (helpURL != null) {
		try {
		    URL url = new URL(helpURL);
		    JMenuItem menuItem = new JMenuItem("Online help");
		    menuItem.addActionListener(new ActionListenerWrapper(url) {
			    public void actionPerformed(ActionEvent e) {
				URL url = (URL)getValue();
				globalContext.getAppletContext().showDocument(url,
									      "VAMP Help");
			    }
			});
		    helpMenu.add(menuItem);
		    //helpMenu.addSeparator();
		} catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	JMenuItem menuItem = new JMenuItem("About VAMP");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    AboutDialog.pop(globalContext);
		}
	    });
	helpMenu.add(menuItem);
    }
	
    static final int EDIT_IMASK = 0;
    static final int EDIT_MENU = 1;
    static final int EDIT_UNDO = 2;
    static final int EDIT_REDO = 3;
    static final int EDIT_UNDO_STACK = 4;
    static final int EDIT_COPY = 5;
    static final int EDIT_CUT = 6;
    static final int EDIT_PASTE = 7;

    static final int EDIT_SELECT_ALL = 8;
    static final int EDIT_SELECT_ALL_PROFILES = 9;
    static final int EDIT_SELECT_ALL_REGIONS = 10;
    static final int EDIT_SELECT_ALL_LANDMARKS = 11;

    static final int EDIT_SELECT_COPY_ALL = 12;
    static final int EDIT_SELECT_COPY_ALL_PROFILES = 13;
    static final int EDIT_SELECT_COPY_ALL_REGIONS = 14;
    static final int EDIT_SELECT_COPY_ALL_LANDMARKS = 15;

    static final int EDIT_UNSELECT_ALL = 16;
    static final int EDIT_UNSELECT_ALL_PROFILES = 17;
    static final int EDIT_UNSELECT_ALL_REGIONS = 18;
    static final int EDIT_UNSELECT_ALL_LANDMARKS = 19;

    static final int EDIT_REMOVE_SELECTED = 20;
    static final int EDIT_REMOVE_ALL = 21;
    static final int EDIT_REMOVE_PROFILES = 22;
    static final int EDIT_REMOVE_REGIONS = 23;
    static final int EDIT_REMOVE_MARKS = 24;

    static final int EDIT_LOCK = 25;
    static final int EDIT_RECENTER = 26;
    static final int EDIT_UNCENTER = 27;
    static final int EDIT_UNPINUP_INFO = 28;

    private void setStandardEditMenu(JMenu menu, int n) {
	JMenuItem menuItem;

	int imask[] = new int[]{n};
	Vector editMenuV = new Vector();
	editMenuV.add(EDIT_IMASK, imask);
	editMenuV.add(EDIT_MENU, menu);

	menuItem = new JMenuItem("Undo"); // , KeyEvent.VK_U);
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    undo(mask[0]);
		}
	    });

	menu.add(menuItem);
	editMenuV.add(EDIT_UNDO, menuItem);

	if (UndoVMStack.REDO_MANAGE) {
	    menuItem = new JMenuItem("Redo");
	    menuItem.addActionListener(new ActionListenerWrapper(imask) {
		    public void actionPerformed(ActionEvent e) {
			int mask[] = (int[])getValue();
			redo(mask[0]);
		    }
		});
	    
	    menu.add(menuItem);
	    editMenuV.add(EDIT_REDO, menuItem);
	}
	else
	    editMenuV.add(EDIT_REDO, null);

	JMenu undoStackMenu = new JMenu("Go before...");
	undoStackMenu.addMenuListener(new MenuListenerWrapper(editMenuV) {
		public void menuCanceled(MenuEvent e) { }
		public void menuDeselected(MenuEvent e) { }
		public void menuSelected(MenuEvent e) {
		    Vector v = (Vector)getValue();
		    JMenu undoStackMenu = (JMenu)e.getSource();
		    undoStackMenu.removeAll();
		    int mask[] = (int[])v.get(EDIT_IMASK);
		    Vector undoStack =
			UndoVMStack.getInstance(getPanel(mask[0])).getStack();
		    //int sz = undoStack.size();
		    int sz = UndoVMStack.getInstance(getPanel(mask[0])).getCurrent() + 1;
		    for (int n = sz-1; n >= 0; n--) {
			UndoableVMStatement vmstat = (UndoableVMStatement)undoStack.get(n);
			//if (vmstat.getGraphPanel() != getPanel(mask[0]))
			//break;

			JMenuItem menuItem = new JMenuItem(vmstat.shortDesc());
			/*if (n == sz-1)
			    menuItem.setEnabled(false);
			    else*/
			    menuItem.addActionListener(new ActionListenerWrapper(vmstat) {
				    public void actionPerformed(ActionEvent e) {
					UndoableVMStatement vmstat = (UndoableVMStatement)getValue();
					UndoVMStack.getInstance(vmstat.getGraphPanel()).undo(vmstat);
					
				    }
				});
			
			undoStackMenu.add(menuItem);
		    }
		}
	    });
	
	menu.add(undoStackMenu);
	editMenuV.add(EDIT_UNDO_STACK, undoStackMenu);

	menu.addSeparator();

	menuItem = new JMenuItem("Copy", KeyEvent.VK_C);
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    copySelection(mask[0]);
		}
	    });
	menu.add(menuItem);
	editMenuV.add(EDIT_COPY, menuItem);

	menuItem = new JMenuItem("Cut");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    cutSelection(mask[0]);
		}
	    });
	menu.add(menuItem);
	editMenuV.add(EDIT_CUT, menuItem);

	menuItem = new JMenuItem("Paste");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    pasteSelection(mask[0]);
		}
	    });
	menu.add(menuItem);
	editMenuV.add(EDIT_PASTE, menuItem);

	menu.addSeparator();

	JMenu subMenu;
	subMenu = new JMenu("Select");
	menu.add(subMenu);

	// BEGIN
	menuItem = new JMenuItem("all", KeyEvent.VK_A);
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAll(mask[0], true, true);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_ALL, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all profiles");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAllGraphElements(mask[0], true, true);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_ALL_PROFILES, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all regions");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAllRegions(mask[0], true);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_ALL_REGIONS, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all landmarks");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAllMarks(mask[0], true);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_ALL_LANDMARKS, menuItem);
	// END

	subMenu = new JMenu("Select and copy");
	menu.add(subMenu);

	// BEGIN
	menuItem = new JMenuItem("all");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAndCopyAll(mask[0]);
		}
	    });

	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_COPY_ALL, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all profiles");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAndCopyAllGraphElements(mask[0]);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_COPY_ALL_PROFILES, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all regions");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAndCopyAllRegions(mask[0]);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_COPY_ALL_REGIONS, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all landmarks");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAndCopyAllMarks(mask[0]);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_SELECT_COPY_ALL_LANDMARKS, menuItem);
	// END

	subMenu = new JMenu("Unselect");
	menu.add(subMenu);

	// BEGIN
	menuItem = new JMenuItem("all");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAll(mask[0], false, true);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_UNSELECT_ALL, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all profiles");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAllGraphElements(mask[0], false, true);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_UNSELECT_ALL_PROFILES, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all regions");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAllRegions(mask[0], false);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_UNSELECT_ALL_REGIONS, menuItem);
	// END

	// BEGIN
	menuItem = new JMenuItem("all landmarks");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    selectAllMarks(mask[0], false);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_UNSELECT_ALL_LANDMARKS, menuItem);
	// END


	//menu.addSeparator();

	subMenu = new JMenu("Remove");
	menu.add(subMenu);

	menuItem = new JMenuItem("selected");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    removeSelection(mask[0]);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_REMOVE_SELECTED, menuItem);

	menuItem = new JMenuItem("all");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    removeAllS(mask[0]);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_REMOVE_ALL, menuItem);

	menuItem = new JMenuItem("all profiles");

	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    removeGraphElements(mask[0]);
		}
	    });

	subMenu.add(menuItem);
	editMenuV.add(EDIT_REMOVE_PROFILES, menuItem);

	menuItem = new JMenuItem("all regions");

	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    removeRegions(mask[0], true);
		}
	    });

	subMenu.add(menuItem);
	editMenuV.add(EDIT_REMOVE_REGIONS, menuItem);

	menuItem = new JMenuItem("all landmarks");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    removeMarks(mask[0]);
		}
	    });
	subMenu.add(menuItem);
	editMenuV.add(EDIT_REMOVE_MARKS, menuItem);

	menu.addSeparator();

	menuItem = new JMenuItem(getLockItemString(n));
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    setReadOnly(mask[0],
				!isReadOnly(mask[0]));
		}
	    });
	menu.add(menuItem);
	editMenuV.add(EDIT_LOCK, menuItem);

	/*
	menu.addMenuListener(new MenuListenerWrapper(editMenuV) {

		public void menuCanceled(MenuEvent e) { }

		public void menuDeselected(MenuEvent e) { }

		public void menuSelected(MenuEvent e) {
		    Vector v = (Vector)getValue();
		    JMenu menu = (JMenu)v.get(EDIT_MENU);
		    JMenuItem menuItem;
		    int mask[] = (int[])v.get(EDIT_IMASK);
		    menuItem = (JMenuItem)v.get(EDIT_UNDO);
		    UndoableVMStatement vmstat = getUndoable(mask[0]);
		    if (vmstat == null) {
			menuItem.setText("Undo");
			menuItem.setEnabled(false);
		    }
		    else {
			menuItem.setText("Undo " + vmstat.shortDesc());
			menuItem.setEnabled(true);
		    }

		    if (UndoVMStack.REDO_MANAGE) {
			menuItem = (JMenuItem)v.get(EDIT_REDO);
			vmstat = getRedoable(mask[0]);
			if (vmstat == null) {
			    menuItem.setText("Redo");
			    menuItem.setEnabled(false);
			}
			else {
			    menuItem.setText("Redo " + vmstat.shortDesc());
			    menuItem.setEnabled(true);
			}
		    }

		    Vector undoStack =
			UndoVMStack.getInstance(getPanel(mask[0])).getStack();
		    menuItem = (JMenuItem)v.get(EDIT_UNDO_STACK);
		    menuItem.setEnabled(undoStack.size() > 0);

		    menuItem = (JMenuItem)v.get(EDIT_PASTE);
		    menuItem.setEnabled(Clipboard.getInstance().getContents().size() > 0);
		    boolean hasSelection = hasSelection(mask[0]);
		    menuItem = (JMenuItem)v.get(EDIT_CUT);
		    menuItem.setEnabled(hasSelection);
		    menuItem = (JMenuItem)v.get(EDIT_COPY);
		    menuItem.setEnabled(hasSelection);
		    menuItem = (JMenuItem)v.get(EDIT_REMOVE_SELECTED);
		    menuItem.setEnabled(hasSelection);
		    menuItem = (JMenuItem)v.get(EDIT_UNSELECT_ALL);
		    menuItem.setEnabled(hasSelection);

		    menuItem = (JMenuItem)v.get(EDIT_LOCK);
		    menuItem.setText((isReadOnly(mask[0]) ? "Unlock" :
				      "Lock") + " panel");
		    if (isInternalReadOnly(mask[0]))
			menuItem.setEnabled(false);
		}
	    });
	*/

	menuItem = new JMenuItem("");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    graphPanelSet.recenter(mask[0]);
		    graphPanelSet.sync(false);
		}
	    });
	menu.add(menuItem);
	editMenuV.add(EDIT_RECENTER, menuItem);

	menuItem = new JMenuItem("");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    graphPanelSet.clearCenter(mask[0]);
		    graphPanelSet.sync(false);
		}
	    });
	menu.add(menuItem);
	editMenuV.add(EDIT_UNCENTER, menuItem);

	menuItem = new JMenuItem("");
	menuItem.addActionListener(new ActionListenerWrapper(imask) {
		public void actionPerformed(ActionEvent e) {
		    int mask[] = (int[])getValue();
		    graphPanelSet.clearPinnedUp(mask[0]);
		    graphPanelSet.sync(false);
		}
	    });
	menu.add(menuItem);
	editMenuV.add(EDIT_UNPINUP_INFO, menuItem);

	/*
	menu.addMenuListener(new MenuListenerWrapper(editMenuV) {
		public void menuCanceled(MenuEvent e) { }
		public void menuDeselected(MenuEvent e) { }
		public void menuSelected(MenuEvent e) {
		    Vector v = (Vector)getValue();
		    int mask[] = (int[])v.get(EDIT_IMASK);
		    JMenu menu = (JMenu)v.get(EDIT_MENU);
		    JMenuItem recenterItem = (JMenuItem)v.get(EDIT_RECENTER);
		    JMenuItem uncenterItem = (JMenuItem)v.get(EDIT_UNCENTER);
		    String centerType = graphPanelSet.getCenterType(mask[0]);
		    if (centerType == null) {
			recenterItem.setEnabled(false);
			recenterItem.setText("Recenter on element");
			uncenterItem.setEnabled(false);
			uncenterItem.setText("Uncenter element");
		    }
		    else {
			recenterItem.setEnabled(true);
			recenterItem.setText("Recenter on " + centerType);
			uncenterItem.setEnabled(true);
			uncenterItem.setText("Uncenter " + centerType);
		    }

		    JMenuItem unpinupInfoItem = (JMenuItem)v.get(EDIT_UNPINUP_INFO);
		    unpinupInfoItem.setText("Take info down");
		    if (graphPanelSet.hasPinnedUp(mask[0]))
			unpinupInfoItem.setEnabled(true);
		    else
			unpinupInfoItem.setEnabled(false);
		}
	    });
	*/

	setMenuListener(menu, editMenuV);
    }

    private void setMenuListener(JMenu menu, Vector editMenuV) {

	menu.addMenuListener(new MenuListenerWrapper(editMenuV) {
		
		public void menuCanceled(MenuEvent e) { }

		public void menuDeselected(MenuEvent e) { }

		public void menuSelected(MenuEvent e) {
		    Vector v = (Vector)getValue();
		    JMenu menu = (JMenu)v.get(EDIT_MENU);
		    JMenuItem menuItem;
		    int mask[] = (int[])v.get(EDIT_IMASK);
		    menuItem = (JMenuItem)v.get(EDIT_UNDO);
		    UndoableVMStatement vmstat = getUndoable(mask[0]);
		    if (vmstat == null) {
			menuItem.setText("Undo");
			menuItem.setEnabled(false);
		    }
		    else {
			menuItem.setText("Undo " + vmstat.shortDesc());
			menuItem.setEnabled(true);
		    }

		    if (UndoVMStack.REDO_MANAGE) {
			menuItem = (JMenuItem)v.get(EDIT_REDO);
			vmstat = getRedoable(mask[0]);
			if (vmstat == null) {
			    menuItem.setText("Redo");
			    menuItem.setEnabled(false);
			}
			else {
			    menuItem.setText("Redo " + vmstat.shortDesc());
			    menuItem.setEnabled(true);
			}
		    }

		    Vector undoStack =
			UndoVMStack.getInstance(getPanel(mask[0])).getStack();
		    menuItem = (JMenuItem)v.get(EDIT_UNDO_STACK);
		    menuItem.setEnabled(undoStack.size() > 0);

		    menuItem = (JMenuItem)v.get(EDIT_PASTE);
		    menuItem.setEnabled(Clipboard.getInstance().getContents().size() > 0);
		    boolean hasSelection = hasSelection(mask[0]);
		    menuItem = (JMenuItem)v.get(EDIT_CUT);
		    menuItem.setEnabled(hasSelection);

		    menuItem = (JMenuItem)v.get(EDIT_COPY);
		    menuItem.setEnabled(hasSelection);

		    menuItem = (JMenuItem)v.get(EDIT_REMOVE_SELECTED);
		    menuItem.setEnabled(hasSelection);

		    menuItem = (JMenuItem)v.get(EDIT_UNSELECT_ALL);
		    menuItem.setEnabled(hasSelection);

		    boolean hasGraphElems = hasGraphElements(mask[0]);
		    menuItem = (JMenuItem)v.get(EDIT_REMOVE_PROFILES);
		    menuItem.setEnabled(hasGraphElems);

		    boolean hasRegions = hasRegions();
		    menuItem = (JMenuItem)v.get(EDIT_REMOVE_REGIONS);
		    menuItem.setEnabled(hasRegions);

		    boolean hasMarks = hasMarks();
		    menuItem = (JMenuItem)v.get(EDIT_REMOVE_MARKS);
		    menuItem.setEnabled(hasMarks);

		    boolean hasGraphElemSelection = hasGraphElementSelection(mask[0]);
		    menuItem = (JMenuItem)v.get(EDIT_UNSELECT_ALL_PROFILES);
		    menuItem.setEnabled(hasGraphElemSelection);

		    boolean hasRegionSelection = hasRegionSelection(mask[0]);
		    menuItem = (JMenuItem)v.get(EDIT_UNSELECT_ALL_REGIONS);
		    menuItem.setEnabled(hasRegionSelection);
		    
		    boolean hasMarkSelection = hasMarkSelection(mask[0]);
		    menuItem = (JMenuItem)v.get(EDIT_UNSELECT_ALL_LANDMARKS);
		    menuItem.setEnabled(hasMarkSelection);

		    menuItem = (JMenuItem)v.get(EDIT_SELECT_ALL);
		    menuItem.setEnabled(hasGraphElems || hasRegions || hasMarks);
		    menuItem = (JMenuItem)v.get(EDIT_SELECT_COPY_ALL);
		    menuItem.setEnabled(hasGraphElems || hasRegions || hasMarks);

		    menuItem = (JMenuItem)v.get(EDIT_SELECT_ALL_PROFILES);
		    menuItem.setEnabled(hasGraphElems);

		    menuItem = (JMenuItem)v.get(EDIT_SELECT_COPY_ALL_PROFILES);
		    menuItem.setEnabled(hasGraphElems);

		    menuItem = (JMenuItem)v.get(EDIT_SELECT_ALL_REGIONS);
		    menuItem.setEnabled(hasRegions);

		    menuItem = (JMenuItem)v.get(EDIT_SELECT_COPY_ALL_REGIONS);
		    menuItem.setEnabled(hasRegions);

		    menuItem = (JMenuItem)v.get(EDIT_SELECT_ALL_LANDMARKS);
		    menuItem.setEnabled(hasMarks);

		    menuItem = (JMenuItem)v.get(EDIT_SELECT_COPY_ALL_LANDMARKS);
		    menuItem.setEnabled(hasMarks);

		    menuItem = (JMenuItem)v.get(EDIT_LOCK);
		    menuItem.setText((isReadOnly(mask[0]) ? "Unlock" :
				      "Lock") + " panel");
		    if (isInternalReadOnly(mask[0]))
			menuItem.setEnabled(false);

		    //Vector v = (Vector)getValue();
		    //int mask[] = (int[])v.get(EDIT_IMASK);
		    //JMenu menu = (JMenu)v.get(EDIT_MENU);

		    JMenuItem recenterItem = (JMenuItem)v.get(EDIT_RECENTER);
		    JMenuItem uncenterItem = (JMenuItem)v.get(EDIT_UNCENTER);
		    String centerType = graphPanelSet.getCenterType(mask[0]);
		    if (centerType == null) {
			recenterItem.setEnabled(false);
			recenterItem.setText("Recenter on element");
			uncenterItem.setEnabled(false);
			uncenterItem.setText("Uncenter element");
		    }
		    else {
			recenterItem.setEnabled(true);
			recenterItem.setText("Recenter on " + centerType);
			uncenterItem.setEnabled(true);
			uncenterItem.setText("Uncenter " + centerType);
		    }

		    JMenuItem unpinupInfoItem = (JMenuItem)v.get(EDIT_UNPINUP_INFO);
		    unpinupInfoItem.setText("Take info down");
		    if (graphPanelSet.hasPinnedUp(mask[0]))
			unpinupInfoItem.setEnabled(true);
		    else
			unpinupInfoItem.setEnabled(false);
		}
	    });
    }

    static final int TOOLS_IMASK = 0;
    static final int TOOLS_MENU = 1;
    static final int TOOLS_FIRST_OP = 2;

    static final int TOOLS_OP_IMASK = 0;
    static final int TOOLS_OP_OP = 1;

    private void setStandardToolsMenu(JMenu menu, int n) {
	OPMenu.toolMenu.buildMenu(menu);

	LinkedList menuItems = new LinkedList();
	OPMenu.toolMenu.makeMenuItems(menuItems);

	JMenuItem menuItem;

	int imask[] = new int[]{n};
	Vector toolsMenuV = new Vector();
	toolsMenuV.add(TOOLS_IMASK, imask);
	toolsMenuV.add(TOOLS_MENU, menu);

	int size = menuItems.size();
	for (int i = 0; i < size; i++) {
	    ToolMenuItem item = (ToolMenuItem)menuItems.get(i);
	    GraphElementListOperation op = item.getOP();
	    if (!op.showMenu())
		continue;

	    menuItem = item.getMenuItem();
	    toolsMenuV.add(menuItem);
	    toolsMenuV.add(item.getMenu());
	    toolsMenuV.add(op);
	    Vector v = new Vector();
	    v.add(TOOLS_OP_IMASK, imask);
	    v.add(TOOLS_OP_OP, op);
	    menuItem.addActionListener(new ActionListenerWrapper(v) {
		    public void actionPerformed(ActionEvent e) {
			Cursor cursor = null;

			Vector v = (Vector)getValue();
			GraphElementListOperation op = (GraphElementListOperation)v.get(TOOLS_OP_OP);
			int mask[] = (int[])v.get(TOOLS_OP_IMASK);
			Vector graphElements = getSelectedGraphElements(mask[0]);
			if (!op.useThread())
			    cursor = Utils.setWaitCursor(getThis());

			graphElements = op.getGraphElements(getPanel(mask[0]), graphElements,
						  false);
			if (op.mayApply(getThis(), getPanel(mask[0]),
					graphElements)) {
			    TreeMap params = op.getParams(getThis(), graphElements);
			    if (params != null) {
				Vector rGraphElements =
				    op.apply_thread(getThis(), 
					     getPanel(mask[0]),
					     graphElements, params);
				if (rGraphElements != null)
				    replaceGraphElements(mask[0],
						    graphElements, rGraphElements,
						    op.mustInitScale());
			    }
			}

			if (cursor != null)
			    Utils.setCursor(getThis(), cursor);
		    }

		});
	}

	menu.addMenuListener(new MenuListenerWrapper(toolsMenuV) {
		public void menuCanceled(MenuEvent e) { }
		public void menuDeselected(MenuEvent e) { }
		public void menuSelected(MenuEvent e) {
		    Vector v = (Vector)getValue();
		    //JMenu menu = (JMenu)v.get(TOOLS_MENU);
		    int mask[] = (int[])v.get(TOOLS_IMASK);
		    Vector graphElements = getSelectedGraphElements(mask[0]);
		    int size = v.size();
		    GraphPanel panel = getPanel(mask[0]);
		    boolean one_enabled = false;
		    for (int i = TOOLS_FIRST_OP; i < size; i += 3) {
			JMenu menu = (JMenu)v.get(i+1);
			menu.setEnabled(false);
		    }

		    for (int i = TOOLS_FIRST_OP; i < size; i += 3) {
			JMenuItem menuItem = (JMenuItem)v.get(i);
			GraphElementListOperation op = (GraphElementListOperation)v.get(i+2);

			if (panel.getAutoApplyDSLOP() == null ||
			    panel.getAutoApplyDSLOP().mayApply(op)) {
			    boolean enabled = op.mayApply(getThis(), panel,
							  graphElements);
			    menuItem.setEnabled(enabled);
			    if (enabled) {
				JMenu menu = (JMenu)v.get(i+1);
				menu.setEnabled(true);
			    }
			}
			else
			    menuItem.setEnabled(false);
		    }
		}
	    });

    }

    void selectAll(int n, boolean select) {
	graphPanelSet.selectAll(n, select, false);
    }

    void selectAll(int n, boolean select, boolean immediate) {
	graphPanelSet.selectAll(n, select, immediate);
    }

    void selectAllGraphElements(int n, boolean select) {
	graphPanelSet.selectAllGraphElements(n, select, false);
    }

    void selectAllGraphElements(int n, boolean select, boolean immediate) {
	graphPanelSet.selectAllGraphElements(n, select, immediate);
    }

    void selectAllRegions(int n, boolean select) {
	graphPanelSet.selectAllRegions(n, select);
    }

    void selectAllMarks(int n, boolean select) {
	graphPanelSet.selectAllMarks(n, select);
    }

    void selectAndCopyAll(int n) {
	graphPanelSet.selectAndCopyAll(n);
    }

    void selectAndCopyAllGraphElements(int n) {
	graphPanelSet.selectAndCopyAllGraphElements(n);
    }

    void selectAndCopyAllRegions(int n) {
	graphPanelSet.selectAndCopyAllRegions(n);
    }

    void selectAndCopyAllMarks(int n) {
	graphPanelSet.selectAndCopyAllMarks(n);
    }

    String getLockItemString(int n) {
	//return isReadOnly(n) ? "Unlock view" : "Lock view";
	/*
	return "Lock/Unlock " + graphPanelSet.getPanel(n).getPanelName() +
	    " Panel";
	*/
	return "Lock/Unlock";
    }

    boolean isReadOnly(int n) {
	return graphPanelSet.isReadOnly(n);
    }

    void setReadOnly(int n, boolean isReadOnly) {
	graphPanelSet.setReadOnly(n, isReadOnly);
	repaint();
    }

    void setReadOnly(boolean isReadOnly) {
	int sz = graphPanelSet.getPanelCount();
	for (int n = 0; n < sz; n++)
	    graphPanelSet.setReadOnly(n, isReadOnly);
	repaint();
    }

    boolean isInternalReadOnly(int n) {
	return graphPanelSet.isInternalReadOnly(n);
    }

    void setInternalReadOnly(int n, boolean internalReadOnly) {
	graphPanelSet.setInternalReadOnly(n, internalReadOnly);
    }

    void setInternalReadOnly(boolean internalReadOnly) {
	int sz = graphPanelSet.getPanelCount();
	for (int n = 0; n < sz; n++)
	    graphPanelSet.setInternalReadOnly(n, internalReadOnly);
	repaint();
    }

    UndoableVMStatement getUndoable(int n) {
	UndoableVMStatement vmstat =
	    UndoVMStack.getInstance(getPanel(n)).getLastUndoableVMStatement();
	return vmstat;
    }

    UndoableVMStatement getRedoable(int n) {
	UndoableVMStatement vmstat =
	    UndoVMStack.getInstance(getPanel(n)).getLastRedoableVMStatement();
	return vmstat;
    }

    void undo(int n) {
	if (getUndoable(n) != null)
	    UndoVMStack.getInstance(getPanel(n)).undoLast();
    }

    void redo(int n) {
	if (getRedoable(n) != null)
	    UndoVMStack.getInstance(getPanel(n)).redoLast();
    }

    void copySelection(int n) {
	graphPanelSet.copySelection(n);
    }

    void cutSelection(int n) {
	graphPanelSet.cutSelection(n);
    }

    void pasteSelection(int n) {
	graphPanelSet.pasteSelection(n);
    }

    void removeSelection(int n) {
	graphPanelSet.removeSelection(n);
    }

    void removeAllS(int n) {
	graphPanelSet.removeAllS(n);
    }

    void removeGraphElements(int n) {
	graphPanelSet.removeGraphElements(n);
    }

    void removeRegions(int n, boolean removeRegionMarks) {
	graphPanelSet.removeRegions(n, removeRegionMarks);
    }

    void removeMarks(int n) {
	graphPanelSet.removeMarks(n);
    }

    boolean hasSelection(int n) {
	return graphPanelSet.hasSelection(n);
    }

    boolean hasGraphElementSelection(int n) {
	return graphPanelSet.hasGraphElementSelection(n);
    }

    boolean hasRegionSelection(int n) {
	return graphPanelSet.hasRegionSelection(n);
    }

    boolean hasMarkSelection(int n) {
	return graphPanelSet.hasMarkSelection(n);
    }

    Vector getSelectedGraphElements(int n) {
	return graphPanelSet.getSelectedGraphElements(n);
    }

    LinkedList getGraphElements(int n) {
	return graphPanelSet.getGraphElements(n);
    }

    LinkedList getMarks() {
	return graphPanelSet.getMarks();
    }

    LinkedList getRegions() {
	return graphPanelSet.getRegions();
    }

    boolean hasGraphElements(int n) {
	return getGraphElements(n).size() > 0;
    }

    boolean hasRegions() {
	return getRegions().size() > 0;
    }

    boolean hasMarks() {
	return getMarks().size() > 0;
    }

    void replaceGraphElements(int n, Vector from, Vector to,
			 boolean mustInitScale) {
	if (mustInitScale)
	    reinitScale();
	graphPanelSet.replaceGraphElements(n, from, to);
    }

    void reinitScale() {
	zoomPanel.setScale(new Scale(1, 1));
    }

    public GraphPanelSet getGraphPanelSet() {return graphPanelSet;}

    View getThis() {return this;} // for anonymous listener

    String getViewName() {return name;}
    public void setOrigName(String orig_name) {this.orig_name = orig_name;}
    public String getOrigName() {return orig_name;}
    public String getName() {
	return getOrigName() != null ? getOrigName() : getViewName();
    }

    static final String HTML = ".html";
    static final String XML = ".xml";

    void savePerform(Frame frame, boolean all) {
	File file = DialogUtils.openFileChooser(frame, "Save",
						(all ? DialogUtils.HTML_FILE_FILTER :
						 DialogUtils.XML_FILE_FILTER), true);
	if (file == null) return;
	String ext = (all ? HTML : XML);

	if (!Utils.hasExtension(file.getName(), ext))
	    file = new File(file.getAbsolutePath() + ext);

	Vector v = new Vector();
	v.add(file);

	if (file.exists()) {
	    ConfirmDialog.pop
		(globalContext, "Warning: file " + file.getAbsolutePath() +
		 " already exists.\n" +
		 "Do you want to override it ?",
		 new Action() {
		     public void perform(Object arg) {
			 Vector v = (Vector)arg;
			 File file = (File)v.get(0);
			 v.add(new Boolean(true));
		     }
		 }, v, "Yes", "No");
	}
	else
	    v.add(new Boolean(true));

	if (v.size() == 2 && ((Boolean)v.get(1)).booleanValue()) {
	    XMLSaveDataProducer sdp = new XMLSaveDataProducer();
	    sdp.save(globalContext, file, (all ? null : this));
	}
    }

    void suppress() {
	ImportDataDialog.hide(globalContext, this);
	getViewList().remove(this);
    }

    public GlobalContext getGlobalContext() {return globalContext;}

    static final String VIEW_LIST = "ViewList";

    static void init(GlobalContext globalContext) {
	globalContext.put(VIEW_LIST, new LinkedList());
    }

    LinkedList getViewList() {
	return getViewList(globalContext);
    }

    static LinkedList getViewList(GlobalContext globalContext) {
	return (LinkedList)globalContext.get(VIEW_LIST);
    }

    void closeAllFrames() {
	LinkedList viewFrameList = ViewFrame.getViewFrameList(globalContext);
	int size = viewFrameList.size();
	for (int n = 0; n < size; n++) {
	    ((ViewFrame)viewFrameList.get(n)).dispose();
	}
    }

    void repaintAllViews() {
	LinkedList viewList = View.getViewList(globalContext);
	int size = viewList.size();
	for (int n = 0; n < size; n++) {
	    ((View)viewList.get(n)).repaint();
	}
    }

    public void syncGraphElements() {
	syncGraphElements(true);
    }

    public void syncGraphElements(boolean readaptSize) {
	if (graphPanelSet != null)
	    graphPanelSet.syncGraphElements(readaptSize);
    }

    static void syncAllGraphElements(GlobalContext globalContext,
				boolean readaptSize) {
	LinkedList list = getViewList(globalContext);
	int size = list.size();
	for (int n = 0; n < size; n++)
	    ((View)list.get(n)).syncGraphElements(readaptSize);
    }

    void sync(boolean invalidate) {
	if (colorLegendPanel != null)
	    colorLegendPanel.sync();
	if (thresholdPanel != null)
	    thresholdPanel.sync();
	getGraphPanelSet().sync(invalidate);
    }

    static void syncAll(GlobalContext globalContext,
			boolean invalidate) {
	LinkedList list = getViewList(globalContext);
	int size = list.size();
	for (int n = 0; n < size; n++)
	    ((View)list.get(n)).sync(invalidate);
    }

    InfoPanel getInfoPanel() {return infoPanel;}
    ZoomPanel getZoomPanel() {return zoomPanel;}
    ColorLegendPanel getColorLegendPanel() {return colorLegendPanel;}
    ThresholdsPanel getThresholdsPanel() {return thresholdPanel;}
    SearchGraphElementPanel getSearchGraphElementPanel() {return searchGraphElementPanel;}
    SearchDataElementPanel getSearchDataElementPanel() {return searchDataElementPanel;}
    InfoDisplayer getInfoDisplayer() {return infoDisplayer;}

    void applyOnGraphElements(DataSetPerformer ds_perform) {
	getGraphPanelSet().applyOnGraphElements(ds_perform);
    }

    LinkedList autoApplyDSLOP(GraphPanel panel, LinkedList graphElements) {
	GraphElementListOperation autoApplyDSLOP = panel.getAutoApplyDSLOP();
	if (autoApplyDSLOP == null)
	    return graphElements;

	Vector v = Utils.listToVector(graphElements);

	if (!autoApplyDSLOP.mayApply(this, panel, v, true))
	    return null;

	v = autoApplyDSLOP.apply(this, panel, v,
				 autoApplyDSLOP.getDefaultParams(this, v),
				 true);
	return Utils.vectorToList(v);
    }

    static boolean checkDSLOP(View view, GraphPanel panel,
			      GraphElementListOperation dslop,
			      LinkedList graphElements, boolean autoApply) {
	if (dslop != null)
	    graphElements = dslop.getGraphElements(panel, graphElements, autoApply);

	if (dslop == null)
	    return VAMPUtils.checkGraphElementType(graphElements);

	Vector v = Utils.listToVector(graphElements);
	if (!dslop.mayApply(view, panel, v, autoApply)) {
	    System.out.println("checkDSPLOP: may not apply " + dslop.getName());
	    return false;
	}

	return VAMPUtils.checkGraphElementType(graphElements,
					 dslop.getSupportedInputTypes());
    }

    boolean checkAutoApplyDSLOP(GraphPanel panel, LinkedList graphElements) {
	return checkDSLOP(this, panel, panel.getAutoApplyDSLOP(),
			  graphElements, true);
    }

    static int[] getDefaultAxisSizes() {
	int sizes[] = new int[4];

	sizes[GraphPanel.NORTH_X] =
	    VAMPResources.getInt(VAMPResources.AXIS_NORTH_SIZE);
	sizes[GraphPanel.SOUTH_X] =
	    VAMPResources.getInt(VAMPResources.AXIS_SOUTH_SIZE);
	sizes[GraphPanel.WEST_Y] =
	    VAMPResources.getInt(VAMPResources.AXIS_WEST_SIZE);
	sizes[GraphPanel.EAST_Y] =
	    VAMPResources.getInt(VAMPResources.AXIS_EAST_SIZE);
	return sizes;
    }

    static int[] getAxisSizes(int north, int south, int west, int east) {
	if (north == 0 && south == 0 && west == 0 && east == 0)
	    return null;
	int sizes[] = new int[4];
	sizes[GraphPanel.NORTH_X] = north;
	sizes[GraphPanel.SOUTH_X] = south;
	sizes[GraphPanel.WEST_Y] = west;
	sizes[GraphPanel.EAST_Y] = east;
	return sizes;
    }

    void setPrintPreviewer(PrintPreviewer previewer) {
	this.previewer = previewer;
    }

    void repaintPrintPreviewer(boolean force) {
	if (previewer != null)
	    previewer.redisplay(force);
    }

    void printPreviewerClosed(PrintPreviewer p) {
	previewer = null;
    }

    void load(Frame frame, boolean replace) {
	File file = DialogUtils.openFileChooser(frame, "Load View", DialogUtils.XML_FILE_FILTER, false);
	if (file == null || !Utils.checkRead(globalContext, file))
	    return;

	Cursor cursor = Utils.setWaitCursor(getThis());
	XMLLoadDataFactory ldf =
	    new XMLLoadDataFactory(globalContext, true, false);

	try {
	    FileInputStream is = new FileInputStream(file);
	    //ldf.setData(is, file.getName(), this, replace, null);

	    Object valarr[] = new Object[4];
	    valarr[0] = file;
	    valarr[1] = new Boolean(replace);
	    valarr[2] = is;
	    valarr[3] = ldf;
			
	    LoadOperation op = new LoadOperation(valarr, (replace ? "Replacing" : "Adding") + " profiles in current view...") {
		    
		    public void perform1() {
		    }

		    public void perform2() {
			Object o[] = (Object[])value;
			File file = (File)o[0];
			boolean replace = ((Boolean)o[1]).booleanValue();
			FileInputStream is = (FileInputStream)o[2];
			XMLLoadDataFactory ldf = (XMLLoadDataFactory)o[3];

			ldf.setData(is, file.getName(), getThis(), replace, null);
		    }
		};

	    Task task = new Task(op);
	    task.start();

	}
	catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "loading: ", e);
	}
	setCursor(cursor);
    }

    boolean isEmpty() {
	return getGraphElements(View.ALL).size() == 0;
    }

    String getMessage() {return message;}

    String setMessage(String message) {
	String old_message = this.message;
	this.message = message;
	return old_message;
    }

    boolean setRunningMode(boolean running_mode) {
	boolean o_running_mode = this.running_mode;
	this.running_mode = running_mode;
	return o_running_mode;
    }

    boolean isRunningMode() {return running_mode;}

    boolean multiplePanels() {
	return panelProfiles.length > 1;
    }

    public GraphPanel getPanel(int n) {
	return graphPanelSet.getPanel(n);
    }

    /*
    static int computeTBHeight(PanelProfile panelProfiles[]) {
	return 100 + (panelProfiles.length+1) * 60;
    }
    */

    /*
    static int computeTBHeight(ZoomPanel zoomPanel) {
    return 100 + zoomPanel.getSliderCount() * 60;
    }
    */

    PanelProfile[] getPanelProfiles() {return panelProfiles;}

    Property annotDisplayFilterProp;

    void setAnnotDisplayFilterProp(Property annotDisplayFilterProp) {
	this.annotDisplayFilterProp = annotDisplayFilterProp;
    }

    Property getAnnotDisplayFilterProp() {
	return annotDisplayFilterProp;
    }

    boolean annot_global = true;

    void setAnnotGlobal(boolean annot_global) {
	this.annot_global = annot_global;
    }

    boolean isAnnotGlobal() {
	return annot_global;
    }

    boolean isEnabled(View view) {
	return view.getSelectedGraphElements(View.ALL).size() > 0;
    }

    abstract class LoadOperation extends Task.OperationWrapper {

	String msg;

	LoadOperation(Object value, String msg) {
	    super(value);
	    this.msg = msg;
	}

	public View getView() {
	    return getThis();
	}

	public GraphPanel getPanel() {
	    return getThis().getPanel(0);
	}

	abstract public void perform1();
	
	abstract public void perform2();
	
	public String getMessage() {
	    return msg;
	}
    }
}
