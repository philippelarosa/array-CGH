
/*
 *
 * Resources.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.awt.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class Resources extends DefaultHandler {
    TreeMap map;
    ResourceItemList resItemList;
    String curData;
    static final String CGHConfigTag = "CGHConfig";
    GlobalContext globalContext;

    public static class AnnotContext {
	Property prop;
	Vector annot_v;
    }

    AnnotContext annot_ctx = new AnnotContext();

    Resources(ResourceItemList resItemList) {
	this.resItemList = resItemList;
	reset();
    }

    void reset() {
	map = new TreeMap();
	HashMap resMap = resItemList.getMap();
	Iterator it = resMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    ResourceItem resItem = (ResourceItem)entry.getValue();
	    add(resItem.name, resItem.defval);
	}
    }

    static SAXParserFactory factory = SAXParserFactory.newInstance();

    void read(GlobalContext globalContext, InputStream is) {
	this.globalContext = globalContext;
	annot_ctx.prop = null;

	try {
	    SAXParser saxParser = factory.newSAXParser();
	    saxParser.parse(is, this);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    static final int HEADER = 0x1;
    static final int ALL = 0x2;
    static final int PARAMETERS_ONLY = 0x4;

    void write(GlobalContext globalContext, OutputStream os, int flags) {
	PrintStream ps = new PrintStream(os);
	if ((flags & HEADER) != 0)
	    XMLUtils.printHeader(ps);

	XMLUtils.printOpenTag(ps, CGHConfigTag);
	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    ResourceItem resItem = resItemList.get((String)entry.getKey());

	    if ((flags & PARAMETERS_ONLY) != 0) {
		if (resItem == null || !resItem.isParameter())
		    continue;
	    }

	    XMLUtils.printTag(ps, ((String)entry.getKey()).replaceAll(" ", "_"),
			      (resItem != null ?
			       resItem.builder.toString(entry.getValue()) :
			       (String)entry.getValue()));
	}

	savePropertyAnnotations(globalContext, null, ps);
	XMLUtils.printCloseTag(ps, CGHConfigTag);
    }

    void add(String name, Object value) {
	map.put(name, value);
    }

    void add(String name, String value) {
	ResourceItem resItem = resItemList.get(name);
	if (resItem != null)
	    add(resItem.name, resItem.builder.fromString (value));
	else
	    add(name, (Object)value);
    }

    Object get(String name) {
	return map.get(name);
    }

    void display() {
	Iterator it = map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    System.out.println(entry.getKey() + ": " + entry.getValue());
	}
    }

    Color getColor(String name) {
	return (Color)get(name);
    }

    Font getFont(String name) {
	return (Font)get(name);
    }

    int getInt(String name) {
	Integer i = (Integer)get(name);
	if (i != null)
	    return i.intValue();
	return 0;
    }

    double getDouble(String name) {
	Double d = (Double)get(name);
	if (d != null)
	    return d.doubleValue();
	return 0;
    }

    boolean getBool(String name) {
	Boolean b = (Boolean)get(name);
	if (b != null)
	    return b.booleanValue();
	return false;
    }

    public void characters(char buf[], int offset, int len)
	throws SAXException
    {
	String s = new String(buf, offset, len);
	curData += s;
    }

    public static boolean startAnnots(View view, String qName,
				      Attributes attrs,
				      AnnotContext annot_ctx)
	throws SAXException {
	if (qName.equals("PropertyName")) {
	    String values[] = XMLUtils.getAttrValues("Resources", qName, attrs,
						     new String[]{"name"});
	    annot_ctx.prop = Property.getProperty(values[0]);
	    annot_ctx.annot_v = new Vector();
	    return true;
	}

	if (qName.equals("Annotation")) {
	    String values[] = XMLUtils.getAttrValues("Resources", qName, attrs,
						     new String[]{"op", "value", "color"});
	    int op = PropertyElement.getOP(values[0]);
	    String value = values[1];
	    Color color = new Color(Integer.parseInt(values[2], 16));
	    annot_ctx.annot_v.add(new PropertyAnnot(annot_ctx.prop, op, value, color));
	    return true;
	}

	return false;
    }

    public static boolean endAnnots(View view, String qName,
				    AnnotContext annot_ctx)
	throws SAXException {
	if (qName.equals("PropertyAnnotations") ||
	    qName.equals("Annotation")) 
	    return true;

	if (qName.equals("PropertyName")) {
	    int sz = annot_ctx.annot_v.size();
	    if (sz > 0) {
		PropertyAnnot pannots[] = new PropertyAnnot[sz];
		for (int n = 0; n < sz; n++)
		    pannots[n] = (PropertyAnnot)annot_ctx.annot_v.get(n);
		annot_ctx.prop.addAnnotations(view, pannots);
	    }
	    annot_ctx.annot_v = null;
	    return true;
	}

	return false;
    }

    public void startElement(String namespaceURI,
			     String lName,
			     String qName,
			     Attributes attrs) throws SAXException	{
	startAnnots(null, qName, attrs, annot_ctx);
	/*
	if (qName.equals("PropertyName")) {
	    String values[] = XMLUtils.getAttrValues("Resources", qName, attrs,
						     new String[]{"name"});
	    annot_ctx.prop = Property.getProperty(values[0]);
	    annot_ctx.annot_v = new Vector();
	}
	else if (qName.equals("Annotation")) {
	    String values[] = XMLUtils.getAttrValues("Resources", qName, attrs,
						     new String[]{"op", "value", "color"});
	    int op = PropertyElement.getOP(values[0]);
	    String value = values[1];
	    Color color = new Color(Integer.parseInt(values[2], 16));
	    annot_ctx.annot_v.add(new PropertyAnnot(annot_ctx.prop, op, value, color));
	}
	*/

	curData = "";
    }

    public void endElement(String namespaceURI,
                           String sName, // simple name
                           String qName  // qualified name
                          )
    throws SAXException
    {
	if (endAnnots(null, qName, annot_ctx))
	    ; // skip
	/*
	if (qName.equals("PropertyAnnotations") ||
	    qName.equals("Annotation")) 
	    ; // skip
	else if (qName.equals("PropertyName")) {
	    int sz = annot_ctx.annot_v.size();
	    if (sz > 0) {
		PropertyAnnot pannots[] = new PropertyAnnot[sz];
		for (int n = 0; n < sz; n++)
		    pannots[n] = (PropertyAnnot)annot_ctx.annot_v.get(n);
		annot_ctx.prop.addAnnotations(null, pannots);
	    }
	    annot_ctx.annot_v = null;
	}
	*/
	else if (!qName.equals(CGHConfigTag))
	    add(qName, curData);
    }

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
		    ps.println("</PropertyName>");
		ps.println("<PropertyName name=\"" + propName + "\">");
		lastPropName = propName;
	    }
	    ps.println(" <Annotation op=\"" +
		       PropertyElement.getStringOP(annot.getOP()) +
		       "\" value=\"" + annot.getValue() +
		       "\" color=\"" + XMLUtils.RGBtoString(annot.getColor().getRGB()) + "\"/>");
	}

	if (lastPropName != null)
	    ps.println("</PropertyName>");
	XMLUtils.printCloseTag(ps, "PropertyAnnotations");
    }
}

