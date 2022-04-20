
/*
 *
 * XMLLoadPrintTemplate.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class XMLLoadPrintTemplate {

    private static SAXParserFactory factory = SAXParserFactory.newInstance();
    private Handler handler;
    private SAXParser saxParser;
    private GlobalContext globalContext;

    XMLLoadPrintTemplate(GlobalContext globalContext, boolean verbose) {
	this.globalContext = globalContext;
	try {
	    saxParser = factory.newSAXParser();
	    handler = new Handler(verbose);
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadPrintTemplate: ", e);
        }
    }

    PrintPageTemplate getTemplate(File file) {
	try {
	    FileInputStream is = new FileInputStream(file);
	    return getTemplate(is);
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadPrintTemplate: ", e);
	    return null;
        }
    }

    PrintPageTemplate getTemplate(InputStream is) {
	try {
	    handler.init();
            saxParser.parse(is, handler);

	    if (handler.getError() != null) {
		InfoDialog.pop(globalContext, "Error reported: " +
			       handler.getError());
		return null;
	    }

	    return handler.getTemplate();
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadPrintTemplate: ", e);
	    return null;
        }
    }
    class Handler extends DefaultHandler {
	
	String curData;
	String error;
	boolean verbose;
	String templateName;
	boolean fileMenu;
	int printFlags;
	int pageOrientation;
	double width, height, img_x, img_y, img_width, img_height;
	PrintPageTemplate pageTemplate;
	String areaName;
	int panelNum;
	Rectangle2D.Double area_rect;
	Color bgColor, fgColor, bdColor;
	boolean hasBorder;
	Font font;
	String textTemplate;
	String imgURL;
	int perPage;

	Handler(boolean verbose) {
	    this.verbose = verbose;
	}

	void init() {
	    templateName = null;
	    textTemplate = null;
	    pageTemplate = null;
	    bgColor = fgColor = bdColor = null;
	    font = null;
	    fileMenu = false;
	    perPage = 0;
	    printFlags = 0;
	}

	public void startElement(String namespaceURI,
				 String lName,
				 String qName,
				 Attributes attrs) throws SAXException	{
	    if (error != null) return;

	    if (verbose)
		System.out.println("<" + qName + ">");

	    if (qName.equals("PrintArea") ||
		qName.equals("PrintTextArea") ||
		qName.equals("PrintHTMLArea") ||
		qName.equals("PrintImageArea")) {
		areaName = null;
		panelNum = -1;
		bgColor = fgColor = bdColor = null;
		font = null;
	    }
	    else if (qName.equals("PageImageable")) {
		String values[] =
		    XMLUtils.getAttrValues
		    ("XMLLoadPrintTemplate",
		     qName, attrs,
		     new String[]{"width", "height", "img_x", "img_y",
		     "img_width", "img_height"});
		double width = Double.parseDouble(values[0]);
		double height = Double.parseDouble(values[1]);

		double img_x = Double.parseDouble(values[2]);
		double img_y = Double.parseDouble(values[3]);
		double img_width = Double.parseDouble(values[4]);
		double img_height = Double.parseDouble(values[5]);

		PageFormat pageFormat = new PageFormat();
		/*
		Paper paper = new Paper();
		paper.setSize(width, height);
		paper.setImageableArea(img_x, img_y, img_width, img_height);

		pageFormat.setPaper(paper);
		*/

		pageFormat.setPaper(Config.paperA4);
		pageFormat.setOrientation(pageOrientation);

		pageTemplate = new PrintPageTemplate(templateName, pageFormat);

		pageTemplate.setPerPage(perPage);
		pageTemplate.setFileMenu(fileMenu);
		pageTemplate.setPrintFlags(printFlags);
	    }
	    else if (qName.equals("AreaRect")) {
		String values[] =
		    XMLUtils.getAttrValues
		    ("XMLLoadPrintTemplate",
		     qName, attrs,
		     new String[]{"x", "y", "width", "height"});
		double x = Double.parseDouble(values[0]);
		double y = Double.parseDouble(values[1]);
		double width = Double.parseDouble(values[2]);
		double height = Double.parseDouble(values[3]);
		area_rect = new Rectangle2D.Double(x, y, width, height);
	    }
	    else if (qName.equals("AreaTextTemplate") ||
		      qName.equals("AreaHTMLTemplate")) {
		String values[] =
		    XMLUtils.getAttrValues
		    ("XMLLoadPrintTemplate",
		     qName, attrs, new String[]{"url?"});
		try {
		    textTemplate = read(values[0]);
		}
		catch(Exception e) {
		    error = e.getMessage();
		    return;
		}
	    }
	    curData = "";
	 }

	 public void characters(char buf[], int offset, int len)
	     throws SAXException
	 {
	     if (error != null) return;
	     String s = new String(buf, offset, len);
	     curData += s;
	 }

	 public void endElement(String namespaceURI,
				String sName,
				String qName
				)
	     throws SAXException
	 {
	     if (error != null) return;

	     if (verbose)
		 System.out.println("</" + qName + ">");

	     if (qName.equals("TemplateName"))
		 templateName = curData;
	     else if (qName.equals("TemplateFileMenu"))
		 fileMenu = true;
	     else if (qName.equals("TemplatePrintFlags"))
		 printFlags = Integer.parseInt(curData);
	     else if (qName.equals("PageOrientation"))
		 pageOrientation = Integer.parseInt(curData);
	     else if (qName.equals("AreaName"))
		 areaName = curData;
	     else if (qName.equals("AreaPanelNum"))
		 panelNum = Integer.parseInt(curData);
	     else if (qName.equals("AreaBGColor"))
		 bgColor = new Color(Integer.parseInt(curData));
	     else if (qName.equals("AreaFGColor"))
		 fgColor = new Color(Integer.parseInt(curData));
	     else if (qName.equals("AreaBDColor"))
		 bdColor = new Color(Integer.parseInt(curData));
	     else if (qName.equals("AreaHasBorder"))
		 hasBorder = curData.equalsIgnoreCase("true") ? true : false;
	     else if (qName.equals("AreaFont"))
		 font = FontResourceBuilder._fromString(curData);
	     else if (qName.equals("AreaTextTemplate") ||
		      qName.equals("AreaHTMLTemplate")) {
		 if (textTemplate == null)
		     textTemplate = curData;
	    }
	    else if (qName.equals("AreaImageURL"))
		imgURL = curData;
	    else if (qName.equals("TemplatePerPage"))
		perPage = Integer.parseInt(curData);
	    else if (qName.equals("TemplateCSS")) {
		try {
		    pageTemplate.setCSS(curData);
		} catch(Exception e) {
		    e.printStackTrace();
		}
	    }
	    else if (qName.equals("PrintArea") ||
		qName.equals("PrintTextArea") ||
		qName.equals("PrintHTMLArea") ||
		qName.equals("PrintImageArea")) {
		PrintArea area = null;
		if (qName.equals("PrintArea"))
		    area = new PrintArea(areaName, panelNum, area_rect,
					 bgColor);
		else if (qName.equals("PrintTextArea"))
		    area = new PrintTextArea(areaName, textTemplate,
					     area_rect, bgColor,
					     font);
		else if (qName.equals("PrintHTMLArea"))
		    area = new PrintHTMLArea(areaName, textTemplate,
					     area_rect, bgColor);
		else if (qName.equals("PrintImageArea"))
		    area = new PrintImageArea(areaName, area_rect, imgURL);
		area.setBDColor(bdColor);
		area.hasBorder(hasBorder);
		pageTemplate.addArea(area);
	    }
	}

	public void error(SAXParseException e)
	    throws SAXException {
	    System.err.println("SAX Error at line #" + e.getLineNumber());
	}

	public void warning(SAXParseException e) {
	    System.err.println("SAX Warning at line #" + e.getLineNumber());
	}

	public void ignorableWhitespace(char[] ch, int start, int length) {
	}

	public void fatalError(SAXParseException e)
	    throws SAXException {
	    System.err.println("SAX Fatal Error at line #" + e.getLineNumber());
	}

	boolean hasErrors() {return error != null;}
	String getError() {return error;}

	PrintPageTemplate getTemplate() {return pageTemplate;}
    }

    static String read(String url) throws java.io.IOException, java.net.MalformedURLException {
	if (url == null)
	    return null;

	InputStream is = Utils.openStream(url);
	byte b[] = new byte[2048];
	int n;
	String textTemplate = "";
	while ((n = is.read(b)) >= 0)
	    textTemplate += new String(b, 0, n);

	return textTemplate;
    }
}
