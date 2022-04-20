
/*
 *
 * PrintExportDialog.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;

class PrintExportDialog extends JDialog implements ChangeListener {

    private JRadioButton exportCurrent, exportAll, exportList;
    private PrintPreviewer printPreviewer;
    private PrintableSet printableSet;
    private Color bgColor;
    private JTextField pageListTF;
    private JComboBox formatCB, scaleCB;
    private GlobalContext globalContext;
    private int pageCount;

    static final int GRID_WIDTH = 2;
    static final int GRID_HEIGHT = 9;

    static final String NAME = "PrintExportDialog";

    public static void init(GlobalContext globalContext) {
	globalContext.put(NAME, new PrintExportDialog(globalContext));
    }

    public static void pop(GlobalContext globalContext,
			   PrintPreviewer printPreviewer) {
	PrintExportDialog printExportDialog =
	    (PrintExportDialog) globalContext.get(NAME);
	printExportDialog.pop(printPreviewer);
    }

    public static PrintExportDialog getInstance(GlobalContext globalContext) {
	return (PrintExportDialog)globalContext.get(NAME);
    }

    PrintExportDialog(GlobalContext globalContext) { 
	super(new Frame(), VAMPUtils.getTitle() + ": Print Export Dialog");

	this.globalContext = globalContext;
	this.printPreviewer = null;
	getContentPane().setLayout(new BorderLayout());

	JPanel mainPanel = new JPanel(new GridLayout(GRID_HEIGHT, GRID_WIDTH));
	JPanel buttonPanel = new JPanel(new FlowLayout());
	bgColor = VAMPResources.getColor(VAMPResources.DIALOG_BG);
	mainPanel.setBackground(bgColor);
	buttonPanel.setBackground(bgColor);
	getContentPane().setBackground(bgColor);

	ButtonGroup toExportGroup = new ButtonGroup();
	exportCurrent = DialogUtils.makeRadio(mainPanel, "Current page",
					      toExportGroup, this);
	mainPanel.add(exportCurrent);
	DialogUtils.makePad(mainPanel);

	exportAll = DialogUtils.makeRadio(mainPanel, "All pages",
					 toExportGroup, this);
	mainPanel.add(exportAll);
	DialogUtils.makePad(mainPanel);

	exportList = DialogUtils.makeRadio(mainPanel, "Page list",
					      toExportGroup, this);
	pageListTF = new JTextField(10);

	mainPanel.add(exportList);
	mainPanel.add(pageListTF);

	DialogUtils.makePad(mainPanel);
	JLabel help = new JLabel("page list separated by semi-columns");
	help.setFont(new Font("SansSerif", Font.PLAIN, 9));
	mainPanel.add(help);

	DialogUtils.jumpLine(mainPanel, GRID_WIDTH);

	JLabel label = new JLabel("Format");
	//formatCB = new JComboBox(new String[]{"PNG", "JPG"});
	formatCB = new JComboBox(new String[]{"PNG"});
	//formatCB = new JComboBox(ImageIO.getWriterFormatNames());

	mainPanel.add(label);
	mainPanel.add(formatCB);

	DialogUtils.jumpLine(mainPanel, GRID_WIDTH);

	label = new JLabel("Scale");
	scaleCB = new JComboBox(new String[]{"25%", "40%", "50%", "60%", "75%",
					     "80%", "90%", "100%"});

	scaleCB.setSelectedItem("100%");

	mainPanel.add(label);
	mainPanel.add(scaleCB);

	DialogUtils.jumpLine(mainPanel, GRID_WIDTH);

	JButton ok = new JButton("OK");
	buttonPanel.add(ok);
	ok.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    int pages[] = getPageList();
		    if (pages == null) return;

		    File file = DialogUtils.openFileChooser(new Frame(),
							    "Export", 0, true);
		    if (file == null)
			return;
			
		    export(file, pages);
		    setVisible(false);
		}
	    });

	JButton cancel = new JButton("Cancel");
	cancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });

	buttonPanel.add(cancel);

	JPanel globalPanel = new JPanel(new BorderLayout());
	globalPanel.setBackground(bgColor);

	DialogUtils.pad(globalPanel, BorderLayout.WEST);
	DialogUtils.pad(globalPanel, BorderLayout.EAST);
	DialogUtils.pad(globalPanel, BorderLayout.NORTH);
	DialogUtils.pad(globalPanel, BorderLayout.SOUTH);
	globalPanel.add(mainPanel, BorderLayout.CENTER);

	getContentPane().add(globalPanel, BorderLayout.CENTER);
	getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	exportCurrent.setSelected(true);

	pack();
	Utils.centerOnScreen(this, false);
    }

    void pop(PrintPreviewer printPreviewer) {
	if (isVisible()) {
	    getToolkit().beep();
	    return;
	}

	this.printPreviewer = printPreviewer;
	printableSet = printPreviewer.getPrintPreviewerPanel().
	    getPrintableSet();
	pageCount = printableSet.getNumberOfPages();

	exportAll.setText("All pages (" + pageCount + ")");

	enableCB();
	setVisible(true);
    }

    void exportPNG(PrintPreviewer printPreviewer, String filename) {
	this.printPreviewer = printPreviewer;
	printableSet = printPreviewer.getPrintPreviewerPanel().
	    getPrintableSet();
	pageCount = printableSet.getNumberOfPages();
	int pages[] = getPageList();
	File file = new File(filename);
	export(file, pages);
    }

    private void enableCB() {
	pageListTF.setEnabled(exportList.isSelected());
    }

    public void stateChanged(ChangeEvent e) {
	enableCB();
    }

    private int[] getPageList() {
	if (exportList.isSelected()) {
	    String p[] = pageListTF.getText().split(";");
	    int pages[] = new int[p.length];
	    for (int n = 0; n < pages.length; n++) {
		try {
		    pages[n] = Utils.parseInt(p[n].trim())-1;
		}
		catch(Exception e) {
		    InfoDialog.pop(globalContext,
				   "Invalid number: " + p[n]);
		    return null;
		}

		if (pages[n] < 0) {
		    InfoDialog.pop(globalContext,
				   "Invalid negative number: " + p[n]);
		    return null;
		}

		if (pages[n] >= pageCount) {
		    InfoDialog.pop(globalContext,
				   "Page number too large: " + p[n]);
		    return null;
		}
	    }
	    return pages;
	}

	if (exportCurrent.isSelected())
	    return new int[]{printPreviewer.getCurrentPage()};

	int pages[] = new int[pageCount];
	for (int n = 0; n < pageCount; n++)
	    pages[n] = n;

	return pages;
    }

    private void export(File file, int pages[]) {
	String basename = Utils.suppressExtension(file.getAbsolutePath());
	PageFormat format = printableSet.getPageFormat(0);
	String scale_s = (String)scaleCB.getSelectedItem();
	scale_s = scale_s.substring(0, scale_s.length()-1);
	double scale = (double)Utils.parseInt(scale_s)/100.;

	String ext = "." + ((String)formatCB.getSelectedItem()).toLowerCase();

	for (int n = 0; n < pages.length; n++) {
	    BufferedImage img =
		new BufferedImage((int)(format.getImageableWidth()*scale),
				  (int)(format.getImageableHeight()*scale),
				  BufferedImage.TYPE_INT_ARGB);
	    
	    Graphics2D g = (Graphics2D)img.getGraphics();
	    g.translate(-(int)(format.getImageableX()*scale),
			-(int)(format.getImageableY()*scale));
	    g.scale(scale, scale);
	    printableSet.print(g, null, pages[n]);
	    
	    try {
		if (!exportCurrent.isSelected())
		    file = new File(basename + "_" + (pages[n]+1) + ext);
		else
		    file = new File(basename + ext);

		System.out.println("Exporting image to " + file.getAbsolutePath());
		boolean b = ImageIO.write(img,
					  (String)formatCB.getSelectedItem(),
					  file);
		if (!b)
		    InfoDialog.pop(globalContext,
				   "Unknown error occured while saving image " +
				   "in " + formatCB.getSelectedItem() + " format");
	    }
	    catch(Exception e) {
		InfoDialog.pop(globalContext, e.getMessage());
	    }
	}
    }
}

