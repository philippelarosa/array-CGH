
/*
 *
 * PropertyAnnot.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;

class PropertyAnnot {

    private Property prop;
    private int op; // ignored for now => equals
    private String value;
    private Pattern pattern;
    private Color color;
    private int ind;

    PropertyAnnot(Property prop, int op, String value, Color color) {
	this.prop = prop;
	this.op = op;
	this.ind = -1;
	this.value = value;
	this.color = color;
	pattern = (op == PropertyElement.PATTERN_EQUAL_OP ||
		   op == PropertyElement.PATTERN_DIFF_OP) ?
	    new Pattern(value, true) : null;
    }

    boolean matches(PropertyElement elem) {
	return elem.matches(prop, op,
			    pattern != null ? (Object)pattern : (Object)value);
    }

    Color getColor(PropertyElement elem) {
	return matches(elem) ? color : null;
    }

    Property getProperty() {return prop;}
    Color getColor() {return color;}
    int getOP() {return op;}
    String getValue() {return value;}
    int getInd() {return ind;}
    void setInd(int ind) {this.ind = ind;}

    public String toString() {
	return "PropertyAnnotation:\n" +
	    prop.getName() + PropertyElement.getStringOP(op) + value + " " +
	    ind;
    }

    /*
    static void savePropertyAnnotations(GlobalContext globalContext,
					View view, OutputStream os) {
	PrintStream ps = new PrintStream(os);
	XMLUtils.printHeader(ps);
	savePropertyAnnotations(globalContext, view, ps);
    }

    static void savePropertyAnnotations(GlobalContext globalContext,
					View view, PrintStream ps) {
	XMLUtils.printOpenTag(ps, "PropertyAnnotations");
	PropertyAnnot annots[] = Property.getAllAnnotations(globalContext,
							    view);
	String lastPropName = null;
	for (int n = 0; n < annots.length; n++) {
	    PropertyAnnot annot = annots[n];
	    String propName = annot.getProperty().getName();
	    if (!propName.equals(lastPropName)) {
		if (lastPropName != null)
		    ps.println("</Property>");
		ps.println("<Property name=\"" + propName + "\">");
		lastPropName = propName;
	    }
	    ps.println(" <Annotation op=\"" +
		       PropertyElement.getStringOP(annot.getOP()) +
		       "\" value=\"" + annot.getValue() +
		       "\" color=\"" + XMLUtils.RGBtoString(annot.getColor().getRGB()) + "\"/>");
	}

	if (lastPropName != null)
	    ps.println("</Property>");
	XMLUtils.printCloseTag(ps, "PropertyAnnotations");
    }
    */

    /*
    static void loadPropertyAnnotations(GlobalContext globalContext,
					View view, String url) {
	Document doc = XMLDOM.getInstance().parse(globalContext, url, true);
	if (doc == null)
	    return;
	loadPropertyAnnotations_p(globalContext, view,
				  doc.getFirstChild(), null);
    }

    static void loadPropertyAnnotations(GlobalContext globalContext,
					View view, InputStream is) {
	Document doc = XMLDOM.getInstance().parse(globalContext, is);
	if (doc == null)
	    return;
	loadPropertyAnnotations_p(globalContext, view,
				  doc.getFirstChild(), null);
    }

    private static void loadPropertyAnnotations_p(GlobalContext globalContext,
						  View view, Node node,
						  Property prop) {
	int ind = 0;
	Vector annot_v = new Vector();
	for (Node child = node.getFirstChild();
	     child != null;
	     child = child.getNextSibling()) {
	    if (child.getNodeType() == Node.ELEMENT_NODE) {
		if (child.getNodeName().equals("Property")) {
		    NamedNodeMap atts = child.getAttributes();
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String name = att.getNodeName();
			String v = att.getNodeValue().toString();
			if (name.equals("name"))
			    prop = Property.getProperty(v);
		    }

		    loadPropertyAnnotations_p(globalContext, view,
					      child, prop);
		}
		else if (child.getNodeName().equals("Annotation")) {
		    NamedNodeMap atts = child.getAttributes();
		    int op = 0;
		    String value = null;
		    Color color = null;
		    for (int i = 0; i < atts.getLength(); i++) {
			Node att = atts.item(i);
			String name = att.getNodeName();
			String v = att.getNodeValue().toString();
			if (name.equals("op"))
			    op = PropertyElement.getOP(v);
			else if (name.equals("value"))
			    value = v;
			else if (name.equals("color"))
			    color = new Color(Integer.parseInt(v, 16));
			else if (name.equals("ind"))
			    ind = Integer.parseInt(v);
		    }
		    
		    annot_v.add(new PropertyAnnot(prop, ind++, op,
						  value, color));
		}
	    }
	}

	if (annot_v.size() > 0) {
	    int sz = annot_v.size();
	    PropertyAnnot pannots[] = new PropertyAnnot[sz];
	    for (int n = 0; n < sz; n++)
		pannots[n] = (PropertyAnnot)annot_v.get(n);
	    prop.setAnnotations(view, pannots);
	}
    }
    */
}
