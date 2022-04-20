
/*
 *
 * PrintPageTemplate.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;
import java.awt.print.*;
import javax.swing.text.html.*;
import java.net.*;

class PrintPageTemplate {

    private String name;
    private boolean fileMenu = false;
    private int printFlags = 0;
    private PageFormat format;
    private Vector area_v;
    private String css_url;
    private int per_page = 0;

    static boolean init = false;
    static private TreeMap templ_map = new TreeMap();
    boolean modified = false;
    PrintPreviewer printPreviewer;

    static boolean init(GlobalContext globalContext) {
	if (init)
	    return false;

	init = true;

	SystemConfig sysCfg = (SystemConfig)globalContext.
	    get(SystemConfig.SYSTEM_CONFIG);

	Vector v = sysCfg.getPrintPageTemplates();
	int size = v.size();
	XMLLoadPrintTemplate loadPrint =
	    new XMLLoadPrintTemplate(globalContext, false);

	try {
	    for (int n = 0; n < size; n++) {
		String url = (String)v.get(n);
		loadPrint.getTemplate(Utils.openStream(url));
	    }
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return false;
	}

	return true;
    }

    PrintPageTemplate copy(String name) {
	PrintPageTemplate t = new PrintPageTemplate(name, format);
	int sz = area_v.size();
	for (int n = 0; n < sz; n++)
	    t.addArea((PrintArea)((PrintArea)area_v.get(n)).clone());
	return t;
    }

    PrintPageTemplate(String name, PageFormat format) {
	this.name = name;
	this.format = format;
	area_v = new Vector();
	if (this.name.length() > 0)
	    templ_map.put(this.name, this);
    }

    void suppressArea(PrintArea area) {
	area.setPageTemplate(null);
	area_v.remove(area);
    }

    void addArea(PrintArea area) {
	area.setPageTemplate(this);
	area_v.add(area);
    }

    void setName(String name) {
	if (!name.equals(this.name)) {
	    if (this.name.length() > 0) 
		templ_map.remove(this.name);
	    this.name = name;
	    templ_map.put(this.name, this);
	}
    }

    String getName() {return name;}

    PageFormat getPageFormat() {return format;}
    void setPageFormat(PageFormat format) {this.format = format;}

    Vector getAreas() {
	return area_v;
    }

    static PrintPageTemplate get(String name) {
	return (PrintPageTemplate)templ_map.get(name);
   }

    void remove() {
	templ_map.remove(name);
    }

    static Vector getPageTemplates() {
	return Utils.keyVector(templ_map);
    }

    String getCSS() {return css_url;}

    void setCSSFile(String css_file, PrintableSet printableSet)
	throws Exception {
	this.css_url = "file://" + css_file;
	int sz = area_v.size();

	for (int n = 0; n < sz; n++) {
	    if (area_v.get(n) instanceof PrintHTMLArea) {
		PrintHTMLArea harea = (PrintHTMLArea)area_v.get(n);
		HTMLEditorKit editorKit = 
		    (HTMLEditorKit)harea.getPreviewArea().getEditorKit();
		editorKit.getStyleSheet().importStyleSheet
		    (new URL(css_url));
		break;
	    }
	}

	printableSet.syncHTMLAreas();
    }

    void setCSS(String css_url)
	throws Exception {
	this.css_url = css_url;
	HTMLEditorKit editorKit = new HTMLEditorKit();
	editorKit.getStyleSheet().importStyleSheet
	    (new URL(css_url));
    }

    /*
    void setCSSFile(String css_file) {
	setCSS("file://" + css_file);
    }
    */

    void setPerPage(int per_page) {
	this.per_page = per_page;
    }

    int getPerPage() {return per_page;}

    void setFileMenu(boolean fileMenu) {
	this.fileMenu = fileMenu;
    }

    boolean isFileMenu() {return fileMenu;}

    void setPrintFlags(int printFlags) {
	this.printFlags = printFlags;
    }

    int getPrintFlags() {return printFlags;}

    void setModified() {
	setModified(true);
    }

    void setModified(boolean modified) {
	this.modified = modified;
	if (printPreviewer != null)
	    printPreviewer.alertTemplateStateChanged();
    }

    boolean isModified() {return modified;}

    void setPrintPreviewer(PrintPreviewer printPreviewer) {
	this.printPreviewer = printPreviewer;
    }
}
