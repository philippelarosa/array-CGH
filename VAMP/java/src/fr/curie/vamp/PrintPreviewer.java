
/*
 *
 * PrintPreviewer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;

class PrintPreviewer extends JFrame implements ActionListener, MenuListener {

    static final String PREVIOUS = "Previous";
    static final String NEXT = "Next";
    static final String EXPORT = "Export";
    static final String PRINT = "Print";
    static final String EDIT = "Edit";
    static final String PREVIEW = "Preview";
    static final String NEW_AREA = "New Area";
    static final String HIDE_AREAS = "Hide Areas";
    static final String SUPPRESSED = "Remove Selected";
    static final String TEXT_AREA = "Text";
    static final String HTML_AREA = "HTML";
    static final String IMAGE_AREA = "Image";
    static private int areaNum = 1;

    static int NEW_COUNT = 0;
    static final Color bgColor = Color.ORANGE;
    static final int MAX_WIDTH = 900;
    static final int MIN_WIDTH = 600;
    static final int MAX_HEIGHT = 700;
    PrintableSet printableSet;
    PrintPreviewerPanel previewerPanel;
    JScrollPane scrollPane;
    int curPageNum = 0;
    JButton edit, print, export, close, previous, next, newArea;
    String pageCountStr;
    JPanel namePanel;
    MLLabel nameLabel;
    JTextField nameText;
    JLabel pageLabel;
    JComboBox newAreaCB;
    JMenu contentsMenu;
    JMenuItem renameItem, copyItem, removeItem;
    JMenuItem activeGridItem, showGridItem, alignGridItem;
    JMenu templateMenu, perPageMenu, gridWidthMenu;
    XMLSavePrintTemplate savePrint;
    static final String NAME = "PrintPreviewer";
    GlobalContext globalContext;
    View view;
    static PrintPageTemplate defaultTemplate = 
	PrintPageTemplate.get("Standard");
    boolean ok;
    PrintPageTemplate currentTemplate = defaultTemplate;
    int currentFlags = PrintableSet.PRINT_ALL|PrintableSet.WHOLE_X|PrintableSet.WHOLE_Y;
    int currentGraphElemCountPerPage = 4;

    public static void init(GlobalContext globalContext) {
	globalContext.put(NAME, new PrintPreviewer(globalContext));
    }

    public static void preview(GlobalContext globalContext, View view,
			       PrintPageTemplate template0) {
	PrintPreviewer printPreviewer = (PrintPreviewer) globalContext.get(NAME);
	printPreviewer.preview(view, template0);
    }

    public static boolean isVisible(GlobalContext globalContext) {
	PrintPreviewer printPreviewer = (PrintPreviewer) globalContext.get(NAME);
	return printPreviewer.isVisible();
    }

    public static PrintPreviewer getInstance(GlobalContext globalContext) {
	return (PrintPreviewer)globalContext.get(NAME);
    }

    class Item {
	String name;
	int panel_num;
	String panel_name;
	Item(String name, int panel_num, String panel_name) {
	    this.name = name;
	    this.panel_num = panel_num;
	    this.panel_name = panel_name;
	}

	Item(String name) {
	    this(name, 0, null);
	}

	public String toString() {
	    return (panel_name != null && panel_name.length() > 0 ?
		    panel_name + " " : "") + name;
	}
    }

    private JButton makeButton(String name) {
	JButton b = new JButton(name);
	b.setActionCommand(name);
	//b.setFont(VAMPResources.getFont(VAMPResources.DIALOG_FONT));
	b.addActionListener(this);
	makeComponent(b);
	return b;
    }

    private void makeComponent(JComponent comp) {
	comp.setFont(VAMPResources.getFont(VAMPResources.INFO_PANEL_TEXT_FONT));
	comp.setBackground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_BG));
	comp.setForeground(VAMPResources.getColor(VAMPResources.DIALOG_BUTTON_FG));
    }

    private void setPageLabel() {
	pageLabel.setText("Page #" + (new Integer(curPageNum+1)).toString() +
			  pageCountStr + "   ");
    }

    private JMenuBar makeMenuBar() {
        JMenuBar menuBar;
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

	JMenuItem saveFileItem = new JMenuItem("Save template in file");
	saveFileItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    savePageTemplate(getTemplName(), printableSet.getPageTemplate());
		}
	    });

	saveFileItem.setEnabled(true);
	fileMenu.add(saveFileItem);

	JMenuItem loadFileItem = new JMenuItem("Load template from file");
	loadFileItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    File file = DialogUtils.openFileChooser
			(new Frame(), "Load", DialogUtils.XML_FILE_FILTER, false);
		    if (file == null) return;
		    XMLLoadPrintTemplate loadPrint =
			new XMLLoadPrintTemplate(globalContext, false);
		    PrintPageTemplate template =
			loadPrint.getTemplate(file);

		    if (template != null) {
			updateTemplates();
			init(template, 0, 0);
			repaint();
			InfoDialog.pop(globalContext, "Template " +
				       template.getName() + " has been " +
				       "succesfully loaded");
		    }
		}
	    });

	loadFileItem.setEnabled(true);
	fileMenu.add(loadFileItem);

	JMenuItem closeItem = new JMenuItem("Close");
	closeItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    close();
		}
	    });

	fileMenu.add(closeItem);

        menuBar.add(fileMenu);

	JMenu editMenu = new JMenu("Edit");

	copyItem = new JMenuItem("Copy to New Template");
	copyItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(currentTemplate.copy(makeNewName()), 0, 0);
		    updateTemplates();
		    repaint();
		}
	    });
	editMenu.add(copyItem);

	renameItem = new JMenuItem("Rename Template");
	renameItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    showRenameArea();
		}
	    });
	editMenu.add(renameItem);

	removeItem = new JMenuItem("Remove Template");
	removeItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ok = false;
		    ConfirmDialog.pop
			(globalContext, "Do you really want to remove " +
			 getTemplName() + " Template ?",
			 new Action() {
			     public void perform(Object arg) {
				 ok = true;
			     }
			 }, null, "Yes", "No");

		    if (ok) {
			currentTemplate.remove();
			updateTemplates();
			init(defaultTemplate, 0, 0);
			repaint();
		    }
		}
	    });
	editMenu.add(removeItem);

	menuBar.add(editMenu);

	contentsMenu = new JMenu("Contents");
	JMenuItem menuItem = new JMenuItem(getFlagString(PrintableSet.PRINT_ALL|PrintableSet.WHOLE_X|PrintableSet.WHOLE_Y));
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(null, PrintableSet.PRINT_ALL|PrintableSet.WHOLE_X|PrintableSet.WHOLE_Y, 0);
		    repaint();
		}
	    });
	contentsMenu.add(menuItem);

	menuItem = new JMenuItem(getFlagString(PrintableSet.PRINT_SELECTED|PrintableSet.WHOLE_X|PrintableSet.WHOLE_Y));
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(null, PrintableSet.PRINT_SELECTED|PrintableSet.WHOLE_X|PrintableSet.WHOLE_Y, 0);
		    repaint();
		}
	    });
	contentsMenu.add(menuItem);

	menuItem = new JMenuItem(getFlagString(PrintableSet.PRINT_ALL));
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(null, PrintableSet.PRINT_ALL, 0);
		    repaint();
		}
	    });
	contentsMenu.add(menuItem);

	// for debug only
	menuItem = new JMenuItem(getFlagString(PrintableSet.PRINT_ALL|PrintableSet.THRU));
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(null, PrintableSet.PRINT_ALL|PrintableSet.THRU, 0);
		    repaint();
		}
	    });
	contentsMenu.add(menuItem);

	perPageMenu = new JMenu("Profiles per Page");

	menuItem = new JMenuItem("All");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(null, 0, Integer.MAX_VALUE);
		    repaint();
		}
	    });

	perPageMenu.add(menuItem);

	contentsMenu.addMenuListener(this);

	for (int n = 1; n <= 12; n++) {
	    menuItem = new JMenuItem(Utils.toString(n));
	    menuItem.addActionListener(new ActionListenerWrapper(new Integer(n)) {
		    public void actionPerformed(ActionEvent e) {
			int perPage = ((Integer)getValue()).intValue();
			init(null, 0, perPage);
			repaint();
		    }
		});
	    perPageMenu.add(menuItem);
	}

	contentsMenu.add(perPageMenu);
	menuBar.add(contentsMenu);

	templateMenu = new JMenu("Templates");
	menuBar.add(templateMenu);

	JMenu gridMenu = new JMenu("Grid");

	activeGridItem = new JMenuItem("");
	activeGridItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    previewerPanel.activeGrid(!previewerPanel.activeGrid());
		    repaint();
		}
	    });

	gridMenu.add(activeGridItem);

	showGridItem = new JMenuItem("");
	showGridItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    previewerPanel.showGrid(!previewerPanel.showGrid());
		    repaint();
		}
	    });
	gridMenu.add(showGridItem);

	alignGridItem = new JMenuItem("Align on Grid");
	alignGridItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    previewerPanel.alignAreas();
		    repaint();
		}
	    });

	gridMenu.add(alignGridItem);

	gridWidthMenu = new JMenu("Grid Width");
	int widths[] = new int[]{3, 5, 8, 10, 12, 15, 20};
	for (int n = 0; n < widths.length; n++) {
	    menuItem = new JMenuItem(Utils.toString(widths[n]));
	    menuItem.addActionListener(new ActionListenerWrapper(new Integer(widths[n])) {
		    public void actionPerformed(ActionEvent e) {
			int grid = ((Integer)getValue()).intValue();
			previewerPanel.setGridWidth(grid);
			repaint();
		    }
		});
	    gridWidthMenu.add(menuItem);
	}

	gridMenu.add(gridWidthMenu);

	gridMenu.addMenuListener(new MenuListener() {
		public void menuCanceled(MenuEvent e) { }
		public void menuDeselected(MenuEvent e) { }
		public void menuSelected(MenuEvent e) {
		    if (previewerPanel.activeGrid()) {
			showGridItem.setEnabled(true);
			alignGridItem.setEnabled(true);
			gridWidthMenu.setEnabled(true);
			if (previewerPanel.showGrid())
			    showGridItem.setText("Hide Grid");
			else
			    showGridItem.setText("Show Grid");
		    }
		    else {
			showGridItem.setEnabled(false);
			alignGridItem.setEnabled(false);
			gridWidthMenu.setEnabled(false);
		    }

		    if (previewerPanel.activeGrid())
			activeGridItem.setText("Inactive Grid");
		    else
			activeGridItem.setText("Active Grid");
		}
	    });

	menuBar.add(gridMenu);

	return menuBar;
    }

    PrintPreviewer(GlobalContext globalContext) {
	super(VAMPUtils.getTitle() + ": Print Previewer");
	this.globalContext = globalContext;
	savePrint = new XMLSavePrintTemplate();
	setJMenuBar(makeMenuBar());

	JPanel controlPanel = new JPanel(new BorderLayout());
	JPanel buttonPanel = new JPanel(new BorderLayout());

	JPanel buttonPreviewPanel = new JPanel(new FlowLayout());
	buttonPreviewPanel.setBackground(bgColor);
	buttonPanel.setBackground(bgColor);
	controlPanel.setBackground(bgColor);

	nameLabel = new MLLabel();
	nameLabel.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    showRenameArea();
		}
	    });

	nameLabel.setBackground(Color.ORANGE);

	close = makeButton("Close");
	buttonPreviewPanel.add(close);
	previous = makeButton(PREVIOUS);
	buttonPreviewPanel.add(previous);
	next = makeButton(NEXT);
	buttonPreviewPanel.add(next);

	export = makeButton(EXPORT);
	buttonPreviewPanel.add(export);

	print = makeButton(PRINT);
	buttonPreviewPanel.add(print);

	edit = makeButton(EDIT);
	buttonPreviewPanel.add(edit);
	pageLabel = new JLabel();

	JPanel buttonEditPanel = new JPanel(new FlowLayout());
	buttonEditPanel.setBackground(Color.ORANGE);

	newAreaCB = new JComboBox();
	makeComponent(newAreaCB);
	buttonEditPanel.add(newAreaCB);

	newArea = makeButton(NEW_AREA);
	buttonEditPanel.add(newArea);

	// depends on format
	/*
	PageFormat format = printableSet.format;
	previewerPanel = new PrintPreviewerPanel(printableSet);
	previewerPanel.setPreferredSize(new Dimension((int)format.getWidth(), (int)format.getHeight()));
	*/
	previewerPanel = new PrintPreviewerPanel();
	previewerPanel.setPreferredSize(new Dimension(600, 800));

	scrollPane = new JScrollPane
	    (previewerPanel,
	     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	getContentPane().setLayout(new BorderLayout());
	buttonPanel.add(buttonPreviewPanel, BorderLayout.NORTH);
	buttonPanel.add(buttonEditPanel, BorderLayout.SOUTH);

	namePanel = new JPanel(new BorderLayout());
	namePanel.setBackground(bgColor);
	namePanel.add(nameLabel);
	controlPanel.add(namePanel, BorderLayout.WEST);
	nameText = new JTextField("coucou");
	nameText.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    currentTemplate.setName(((JTextField)e.getSource()).getText());
		    hideRenameArea();
		}
	    });

	controlPanel.add(buttonPanel, BorderLayout.CENTER);
	JPanel p = new JPanel();
	p.setBackground(bgColor);
	p.add(pageLabel);
	controlPanel.add(p, BorderLayout.EAST);

	getContentPane().add(controlPanel, BorderLayout.NORTH);
	getContentPane().add(scrollPane, BorderLayout.CENTER);

	scrollPane.getViewport().setViewPosition(new Point(0, 0));
	int width = 850;
	int height = 950;

	if (width > MAX_WIDTH) width = MAX_WIDTH;
	if (width < MIN_WIDTH) width = MIN_WIDTH;
	if (height > MAX_HEIGHT) height = MAX_HEIGHT;

	setSize(new Dimension(width, height));
	setVisible(false);
    }

    void preview(View view, PrintPageTemplate template) {
	preview(view, template, true);
    }

    void preview(View view, PrintPageTemplate template, boolean visible) {

	if (this.view != view) {
	    updateTemplates();
	    this.view = view;
	    newAreaCB.removeAllItems();
	    newAreaCB.addItem(new Item(TEXT_AREA));
	    newAreaCB.addItem(new Item(HTML_AREA));
	    newAreaCB.addItem(new Item(IMAGE_AREA));
	    newAreaCB.addItem(new Item(PrintArea.MINIMAP));
	    int panel_count = view.getGraphPanelSet().getPanelCount();
	    for (int m = 0; m < panel_count; m++) {
		GraphPanel panel = view.getGraphPanelSet().getPanel(m);
		String name = panel.getPanelName();
		if (name.length() > 0)
		    name += " ";
		newAreaCB.addItem(new Item(PrintArea.XSCALE, m, name));
		newAreaCB.addItem(new Item(PrintArea.YSCALE, m, name));
		newAreaCB.addItem(new Item(PrintArea.YANNOT, m, name));
		newAreaCB.addItem(new Item(PrintArea.GRAPHELEMENTS, m, name));
	    }
	}

	view.setPrintPreviewer(this);
	init(template, 0, 0);
	if (visible) {
	    setVisible(true);
	}
    }

    void init(PrintPageTemplate template, int flags,
	      int graphElementCountPerPage) {

	if (template != null)
	    currentTemplate = template;

	if (currentTemplate.getPerPage() != 0)
	    currentGraphElemCountPerPage = currentTemplate.getPerPage();
	else if (graphElementCountPerPage != 0)
	    currentGraphElemCountPerPage = graphElementCountPerPage;
	    
	currentTemplate.setPrintPreviewer(this);

	if (flags != 0)
	    currentFlags = flags;

	printableSet = new PrintableSet(view, currentFlags,
					0,
					currentGraphElemCountPerPage, currentTemplate);
	currentFlags = printableSet.getPrintFlags();

	previewerPanel.setPrintableSet(printableSet);
	curPageNum = 0;
	previewerPanel.setPageNum(curPageNum);
	updateNameLabel();
	pageCountStr = " / " +
	    (new Integer(printableSet.getNumberOfPages()).toString());
	setPageLabel();
	enableButtons();
    }

    void display() {
	repaint();
    }

    void redisplay(boolean force) {
	if (force || printableSet.needRedisplay())
	    repaint();
    }

    public void actionPerformed(ActionEvent e) {
	JButton b = (JButton)e.getSource();
	if (b.getActionCommand().equals("Close")) {
	    close();
	}
	else if (b.getActionCommand().equals(PREVIOUS)) {
	    if (curPageNum > 0) {
		previewerPanel.setPageNum(--curPageNum);
		setPageLabel();
		repaint();
	    }
	}
	else if (b.getActionCommand().equals(NEXT)) {
	    if (curPageNum < printableSet.getNumberOfPages()-1) {
		previewerPanel.setPageNum(++curPageNum);
		setPageLabel();
		repaint();
	    }
	}
	else if (b.getActionCommand().equals(EXPORT)) {
	    PrintExportDialog.pop(printableSet.getView().getGlobalContext(),
				  this);
	}
	else if (b.getActionCommand().equals(PRINT)) {
	    try {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPageable(printableSet);
		if (job.printDialog()) {
		    boolean showAreaBounds =
			printableSet.showAreaBounds(false);
		    boolean showAreaNames =
			printableSet.showAreaNames(false);

		    job.print();
		    printableSet.showAreaBounds(showAreaBounds);
		    printableSet.showAreaNames(showAreaNames);
		}
	    }
	    catch(PrinterException ex) {
		ex.printStackTrace();
	    }
	}
	else if (b.getActionCommand().equals(EDIT)) {
	    printableSet.setEditMode(true);
	    printableSet.showAreaBounds(true);
	    b.setText(PREVIEW);
	    b.setActionCommand(PREVIEW);
	    repaint();
	}
	else if (b.getActionCommand().equals(PREVIEW)) {
	    printableSet.setEditMode(false);
	    printableSet.showAreaBounds(false);
	    b.setText(EDIT);
	    b.setActionCommand(EDIT);
	    repaint();
	}
	else if (b.getActionCommand().equals(NEW_AREA)) {
	    if (printableSet.isEditMode()) {
		Item item = (Item)newAreaCB.getSelectedItem();
		String name = item.name;
		int panel_num = item.panel_num;
		int x = previewerPanel.roundX(140);
		int y = previewerPanel.roundY(140);
		Rectangle2D.Double area = new Rectangle2D.Double(x, y,
								 80, 80);
		if (name.equals(TEXT_AREA)) {
		    String s = "Text " + Utils.toString(areaNum++);
		    printableSet.addTextArea(previewerPanel, s, "@" + s,
					     area);
		}
		else if (name.equals(HTML_AREA)) {
		    String s = "HTML " + Utils.toString(areaNum++);
		    printableSet.addHTMLArea(previewerPanel, s,
					     "<h3>" + s + "</h3>",
					     area);
		}
		else if (name.equals(IMAGE_AREA)) {
		    String s = "Image " + Utils.toString(areaNum++);
		    printableSet.addImageArea(previewerPanel, s,
					     area);
		}
		else
		    printableSet.addArea(previewerPanel, name, panel_num,
					 area);
		
		repaint();
	    }
	}

	enableButtons();
    }

    private void enableButtons() {
	if (printableSet == null) {
	    previous.setEnabled(false);
	    next.setEnabled(false);
	    print.setEnabled(false);
	    newArea.setVisible(false);
	    newAreaCB.setVisible(false);
	    edit.setText(EDIT);
	    edit.setActionCommand(EDIT);
	    return;
	}

	previous.setEnabled(curPageNum != 0);
	next.setEnabled(curPageNum < printableSet.getNumberOfPages()-1);

	print.setEnabled(!printableSet.isEditMode());
	newArea.setVisible(printableSet.isEditMode());
	if (printableSet.isEditMode()) {
	    edit.setText(PREVIEW);
	    edit.setActionCommand(PREVIEW);
	}
	else {
	    edit.setText(EDIT);
	    edit.setActionCommand(EDIT);
	}

	newAreaCB.setVisible(printableSet.isEditMode());
	/*
	if (!printableSet.isEditMode())
	    hideRenameArea();
	*/
    }

    private String getTemplName() {
	return currentTemplate.getName();
    }

    PrintPreviewerPanel getPrintPreviewerPanel() {
	return previewerPanel;
    }

    int getCurrentPage() {return curPageNum;}

    private void savePageTemplate(String name, PrintPageTemplate template) {
	File file = DialogUtils.openFileChooser
	    (this, "Save", DialogUtils.XML_FILE_FILTER, true);
	if (file == null) return;
	savePrint.save(printableSet.getView().getGlobalContext(), file, name,
		       template);
    }

    private PrintPageTemplate makeNewTemplate(String name,
					      int orientation) {
	PageFormat format = new PageFormat();
	format.setPaper(Config.paperA4);
	format.setOrientation(orientation);
	PrintPageTemplate template = new PrintPageTemplate(name, format);
	updateTemplates();
	return template;
    }

    void updateTemplates() {
	templateMenu.removeAll();
	Vector v = PrintPageTemplate.getPageTemplates();
	JMenuItem menuItem;

	int sz = v.size();
	for (int n = 0; n < sz; n++) {
	    PrintPageTemplate templ = (PrintPageTemplate)v.get(n);
	    menuItem = new JMenuItem(templ.getName());
	    menuItem.addActionListener(new ActionListenerWrapper(templ) {
		public void actionPerformed(ActionEvent e) {
		    PrintPageTemplate templ = (PrintPageTemplate)getValue();
		    init(templ, 0, 0);
		    repaint();
		}
	    });
	    templateMenu.add(menuItem);
	}

	JMenu newMenu = new JMenu("New");
	menuItem = new JMenuItem("Portrait Template");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(makeNewTemplate(makeNewName(),
					 PageFormat.PORTRAIT), 0, 0);
		    repaint();
		}
	    });

	newMenu.add(menuItem);
	menuItem = new JMenuItem("Landscape Template");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    init(makeNewTemplate(makeNewName(),
					 PageFormat.LANDSCAPE), 0, 0);
		    repaint();
		}
	    });

	newMenu.add(menuItem);
	templateMenu.add(newMenu);


    }

    static boolean isVisibleElements(int flags) {
	return (flags == PrintableSet.PRINT_ALL);
    }

    static boolean isThroughMode(int flags) {
	return (flags & PrintableSet.THRU) != 0;
    }

    static String getFlagString(int flags) {
	if (flags == (PrintableSet.PRINT_ALL|PrintableSet.WHOLE_X|PrintableSet.WHOLE_Y))
	    return "All Profiles";
	if (flags == (PrintableSet.PRINT_SELECTED|PrintableSet.WHOLE_X|PrintableSet.WHOLE_Y))
	    return "Selected Profiles";
	if (flags == PrintableSet.PRINT_ALL)
	    return "Visible Area";
	if (flags == (PrintableSet.PRINT_ALL|PrintableSet.THRU))
	    return "Through mode";

	return "??";
    }

    void updateNameLabel() {
	String s = currentTemplate.getName() + " Template";
	if (currentTemplate.isModified())
	    s += " (*)";

	s += "\n" + getFlagString(currentFlags);

	if (!isVisibleElements(currentFlags)) {
	    if (currentGraphElemCountPerPage == Integer.MAX_VALUE)
		s += "\nAll in one page";
	    else
		s += "\n" + Utils.toString(currentGraphElemCountPerPage) +
		    " per page";
	}

	if (!s.equals(nameLabel.getText())) {
	    nameLabel.setText(s);
	    validate();
	}
    }

    void alertTemplateStateChanged() {
	updateNameLabel();
    }

    static String makeNewName() {
	return "New #" + (++NEW_COUNT);
    }

    void close() {
	setVisible(false);
	printableSet.close();
	printableSet.getView().printPreviewerClosed(this);
	//dispose();
    }

    private void showRenameArea() {
	namePanel.removeAll();
	String name = getTemplName();
	int cols = name.length();
	if (cols > 20)
	    cols = 20;
	nameText.setColumns(cols);
	nameText.setText(name);
	namePanel.add(nameText, BorderLayout.NORTH);
	JLabel l = new JLabel("Press enter to rename template");
	l.setFont(new Font("Serif", Font.ITALIC, 10));
	namePanel.add(l, BorderLayout.CENTER);
	validate();
    }

    private void hideRenameArea() {
	namePanel.removeAll();
	namePanel.add(nameLabel);
	updateTemplates();
	updateNameLabel();
	validate();
    }

    public void menuCanceled(MenuEvent e) { }

    public void menuDeselected(MenuEvent e) { }

    public void menuSelected(MenuEvent e) {
	int cnt = contentsMenu.getItemCount();
	if (currentTemplate.getPrintFlags() > 0) {
	    String str = getFlagString(currentTemplate.getPrintFlags());
	    for (int n = 0; n < cnt; n++)
		contentsMenu.getItem(n).setEnabled
		    (contentsMenu.getItem(n).getText().equals(str));
	}
	else
	    for (int n = 0; n < cnt; n++)
		contentsMenu.getItem(n).setEnabled(true);

	if (isVisibleElements(currentFlags) || isThroughMode(currentFlags) ||
	    currentTemplate.getPerPage() > 0)
	    perPageMenu.setEnabled(false);
	else
	    perPageMenu.setEnabled(true);
    }
}
