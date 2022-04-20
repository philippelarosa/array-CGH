
/*
 *
 * XMLSavePrintTemplate.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.awt.*;
import java.awt.print.*;
import java.util.*;
import java.awt.geom.*;

class XMLSavePrintTemplate {

    XMLSavePrintTemplate() {
    }

    void save(GlobalContext globalContext, File file,
	      String name, PrintPageTemplate template ) {
	try {
	    FileOutputStream os = new FileOutputStream(file);
	    PrintStream ps = new PrintStream(os);
	    save(globalContext, ps, name, template);
	    ps.close();
	}
	catch(FileNotFoundException e) {
	    InfoDialog.pop(globalContext, "Error: file " + e.getMessage());
	}
    }

    private void save(GlobalContext globalContext, PrintStream ps,
		      String name, PrintPageTemplate template) {
	XMLUtils.printHeader(ps);

	XMLUtils.printOpenTag(ps, "PrintPageTemplate", true);

	XMLUtils.printOpenTag(ps, "TemplateName", false);
	ps.print(name);
	XMLUtils.printCloseTag(ps, "TemplateName");

	if (template.getPerPage() > 0) {
	    XMLUtils.printOpenTag(ps, "TemplatePerPage", false);
	    ps.print(template.getPerPage());
	    XMLUtils.printCloseTag(ps, "TemplatePerPage");
	}

	if (template.getPrintFlags() > 0) {
	    XMLUtils.printOpenTag(ps, "TemplatePrintFlags", false);
	    ps.print(template.getPrintFlags());
	    XMLUtils.printCloseTag(ps, "TemplatePrintFlags");
	}

	save(globalContext, ps, template.getPageFormat());

	Vector area_v = template.getAreas();
	int size = area_v.size();
	for (int n = 0; n < size; n++)
	    save(globalContext, ps, (PrintArea)area_v.get(n));

	if (template.getCSS() != null) {
	    XMLUtils.printOpenTag(ps, "TemplateCSS", false);
	    ps.print(template.getCSS());
	    XMLUtils.printCloseTag(ps, "TemplateCSS");
	}

	XMLUtils.printCloseTag(ps, "PrintPageTemplate");
    }

    private void save(GlobalContext globalContext, PrintStream ps,
		      PageFormat format) {
	XMLUtils.printOpenTag(ps, "TemplateFormat", true);
	XMLUtils.printOpenTag(ps, "PageOrientation", false);
	ps.print(format.getOrientation());
	XMLUtils.printCloseTag(ps, "PageOrientation");

	ps.println("<PageImageable " +
		 "width=\"" + format.getWidth() + "\" " +
		 "height=\"" + format.getHeight() + "\" " +
		 "img_x=\"" + format.getImageableX() + "\" " +
		 "img_y=\"" + format.getImageableY() + "\" " +
		 "img_width=\"" + format.getImageableWidth() + "\" " +
		 "img_height=\"" + format.getImageableHeight() + "\"/>");

	XMLUtils.printCloseTag(ps, "TemplateFormat");
    }

    private void save(GlobalContext globalContext, PrintStream ps,
		      PrintArea area) {
	String area_type = area.getClass().getName();
	int idx = area_type.lastIndexOf(".");
	if (idx >= 0) {
	    area_type = area_type.substring(idx+1);
	}

	XMLUtils.printOpenTag(ps, area_type, true);
	XMLUtils.printOpenTag(ps, "AreaName", false);
	ps.print(area.getName());
	XMLUtils.printCloseTag(ps, "AreaName");

	XMLUtils.printOpenTag(ps, "AreaPanelNum", false);
	ps.print(area.getPanelNum());
	XMLUtils.printCloseTag(ps, "AreaPanelNum");

	Rectangle2D.Double rect_area = area.getArea();
	ps.println("<AreaRect " +
		 "x=\"" + (int)rect_area.x + "\" " +
		 "y=\"" + (int)rect_area.y + "\" " +
		 "width=\"" + (int)rect_area.width + "\" " +
		 "height=\"" + (int)rect_area.height + "\"/>");

	XMLUtils.printOpenTag(ps, "AreaBGColor", false);
	ps.print(area.getBGColor().getRGB());
	XMLUtils.printCloseTag(ps, "AreaBGColor");

	XMLUtils.printOpenTag(ps, "AreaBDColor", false);
	ps.print(area.getBDColor().getRGB());
	XMLUtils.printCloseTag(ps, "AreaBDColor");

	XMLUtils.printOpenTag(ps, "AreaHasBorder", false);
	ps.print(XMLUtils.toString(area.hasBorder()));
	XMLUtils.printCloseTag(ps, "AreaHasBorder");

	if (area instanceof PrintTextArea) {
	    PrintTextArea tarea = (PrintTextArea)area;
	    XMLUtils.printOpenTag(ps, "AreaFGColor", false);
	    ps.print(tarea.getFGColor().getRGB());
	    XMLUtils.printCloseTag(ps, "AreaFGColor");
	    XMLUtils.printOpenTag(ps, "AreaTextTemplate", true);
	    String buffer = tarea.getTemplate().replaceAll("&", "&amp;");
	    buffer = buffer.replaceAll("<", "&lt;");
	    ps.print(buffer);
	    XMLUtils.printCloseTag(ps, "AreaTextTemplate");

	    if (tarea.getFont() != null) {
		XMLUtils.printOpenTag(ps, "AreaFont", false);
		ps.print(FontResourceBuilder._toString(tarea.getFont()));
		XMLUtils.printCloseTag(ps, "AreaFont");
	    }
	}
	else if (area instanceof PrintHTMLArea) {
	    PrintHTMLArea tarea = (PrintHTMLArea)area;
	    XMLUtils.printOpenTag(ps, "AreaHTMLTemplate", true);
	    String buffer = tarea.getTemplate().replaceAll("&", "&amp;");
	    buffer = buffer.replaceAll("<", "&lt;");
	    ps.print(buffer);
	    XMLUtils.printCloseTag(ps, "AreaHTMLTemplate");
	}
	else if (area instanceof PrintImageArea) {
	    PrintImageArea iarea = (PrintImageArea)area;
	    XMLUtils.printOpenTag(ps, "AreaImageURL", false);
	    ps.print(iarea.getImageURL());
	    ps.print("<AreaImageSize " +
		     "width=\"" + iarea.getImageSize().width + "\" " +
		     "height=\"" + iarea.getImageSize().height + "\"/>");
	}

	XMLUtils.printCloseTag(ps, area_type);
    }
}
