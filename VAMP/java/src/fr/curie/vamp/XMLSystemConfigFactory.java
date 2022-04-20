
/*
 *
 * XMLSystemConfigFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.net.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class XMLSystemConfigFactory {

    private GlobalContext globalContext;
    private static SAXParserFactory factory;
    private String url;
    private Handler handler;

    XMLSystemConfigFactory(GlobalContext globalContext) {
	this.globalContext = globalContext;
    }

    boolean build(String url, SystemConfig sysCfg) {
	try {
	    this.url = url;

	    InputStream is = Utils.openStream(url);
	    if (factory == null)
		factory = SAXParserFactory.newInstance();

	    SAXParser saxParser = factory.newSAXParser();
	    handler = new Handler(globalContext, sysCfg, false);
            saxParser.parse(is, handler);
	    return true;
	    
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, e.getMessage());
	    return false;
        }
    }

    class Handler extends DefaultHandler {

	SystemConfig sysCfg;
	boolean verbose;
	String transName = null;
	GlobalContext globalContext;
	Vector propList;
	String propType;
	Stack menu_stack = new Stack();
	XMLElementStack elem_stack = new XMLElementStack("XMLSystemConfigFactory");

	private PropertyElementMenuItemSeparator sep =
	    PropertyElementMenuItemSeparator.getInstance();

	Handler(GlobalContext globalContext, SystemConfig sysCfg,
		boolean verbose) {
	    this.globalContext = globalContext;
	    this.sysCfg = sysCfg;
	    this.verbose = verbose;
	}

	public void startElement(String namespaceURI,
				 String lName,
				 String qName,
				 Attributes attrs) throws SAXException	{
	    if (verbose)
		System.err.println("<" + qName + ">");

	    XMLElement elem = new XMLElement(qName, attrs);
	    if (qName.equals("Proxy")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"host", "port"});
		sysCfg.setProxy(values[0], values[1]);
	    }

	    else if (qName.equals("Parameter")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"key", "value"});
		sysCfg.addParameter(values[0], values[1]);
	    }
	    else if (qName.equals("GraphElementIcon")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type", "url"});
		sysCfg.addGraphElementIcon(values[0], values[1]);
	    }
	    else if (qName.equals("GeneSelectionProbeColumns")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type",
						"columns"});
		sysCfg.addGeneSelectionProbeColumns(values[0], values[1]);
	    }
	    else if (qName.equals("GeneSelectionNoArrayProbeColumns")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type",
						"columns"});
		sysCfg.addGeneSelectionNoArrayProbeColumns(values[0], values[1]);
	    }
	    else if (qName.equals("GeneSelectionGeneColumns")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type",
						"columns"});
		sysCfg.addGeneSelectionGeneColumns(values[0], values[1]);
	    }

	    else if (qName.equals("BreakpointFrequencyColumns")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type",
						"columns"});
		sysCfg.addBreakpointFrequencyColumns(values[0], values[1]);
	    }

	    else if (qName.equals("UserDocumentation")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"url"});
		sysCfg.setUserDoc(values[0]);
	    }

	    /*
	    else if (qName.equals("PropertyAnnotations")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"url"});
		PropertyAnnot.loadPropertyAnnotations(globalContext, null,
						      values[0]);
	    }
	    */

	    else if (qName.equals("DefaultConfiguration")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"url"});
		sysCfg.setDefaultConfiguration(values[0]);
	    }

	    else if (qName.equals("Cytoband")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"organism",
							     "url",
							     "resolutions",
							     "default_resolution?"});

		sysCfg.addCytoband(values[0], values[1], values[2],
				   values[3]);
	    }

	    else if (qName.equals("PrintPageTemplate")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"url"});
		sysCfg.addPrintPageTemplate(values[0]);
	    }

	    else if (qName.equals("PropertyElementMenu")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"object", "type?"});
		PropertyElementMenu menu = new PropertyElementMenu();
		sysCfg.addMenu(values[0], values[1], menu);
		emptyMenu();
		pushMenu(menu);
	    }

	    else if (qName.equals("TranscriptomeFactory")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type"});
		TranscriptomeFactory.setDefaultFactory(globalContext, values[0]);
	    }

	    else if (qName.equals("TranscriptomeSQLTemplate")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"name"});
		transName = values[0];
	    }

	    else if (qName.equals("Tool")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type",
							     "name",
							     "title",
							     "container",
							     "url",
							     "source?",
							     "target?"});
		sysCfg.addTool(values[0], values[1], values[2],
			       values[3], values[4], values[5], values[6]);
	    }

	    else if (qName.equals("PropertyList")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type"});
		propType = values[0];
		propList = new Vector();
	    }

	    else if (qName.equals("MenuItem")) {
		String values[] = getAttrValues(qName,
						attrs,
						new String[]{"type", "title?",
							     "url?", "target?"});
		String menuType = values[0];
		if (menuType.equalsIgnoreCase("url")) {
		    if (!values[2].equals("@data"))
			topMenu().add(new PropertyElementMenuItemURL
				      (values[1],
				       new BasicStringURLTemplate
				       (globalContext,
					values[2]), values[3]));
		    else
			topMenu().add(new PropertyElementMenuItemURL
				      (values[1],
				       new SyntaxTreeURLTemplate
				       (globalContext, elem), values[3]));
		}
		else if (menuType.equalsIgnoreCase("menu")) {
		    PropertyElementMenu menu = new PropertyElementMenu(values[1]);
		    topMenu().add(menu);
		    pushMenu(menu);
		}
		else if (menuType.equalsIgnoreCase("separator"))
		    topMenu().add(sep);
		else if (menuType.equalsIgnoreCase("array_list"))
		    topMenu().add(new PropertyElementMenuItemArrayList());
		else if (menuType.equalsIgnoreCase("reference"))
		    topMenu().add(new PropertyElementMenuItemReference());
	    }

	    elem_stack.push(elem);
	}

	private PropertyElementMenu topMenu() throws SAXException {
	    if (menu_stack.size() == 0)
		throw new SAXException("XMLSystemConfigFactory: missing top menu");
	    return (PropertyElementMenu)menu_stack.peek();
	}

	private PropertyElementMenu popMenu() throws SAXException {
	    if (menu_stack.size() == 0)
		throw new SAXException("XMLSystemConfigFactory: missing top menu");
	    return (PropertyElementMenu)menu_stack.pop();
	}

	private void pushMenu(PropertyElementMenu menu) {
	    menu_stack.push(menu);
	}

	private void emptyMenu() {
	    menu_stack.empty();
	}

	public void characters(char buf[], int offset, int len)
	    throws SAXException
	{
	    String s = new String(buf, offset, len);
	    elem_stack.peek().pushData(s);
	}

	public void endElement(String namespaceURI,
			       String sName,
			       String qName)
	    throws SAXException
	{
	    XMLElement elem = elem_stack.pop();
	    String curData = elem.getData();
	    curData = curData.trim();
	    if (verbose) {
		System.err.print("{{" + curData + "}}");
		System.err.println("</" + qName + ">\n");
	    }

	    if (qName.equals("PropertyElementMenu"))
		popMenu();
	    else if (qName.equals("MenuItem")) {
		String menuType = elem.getAttrValue("type");
		if (menuType.equalsIgnoreCase("menu"))
		    popMenu();
	    }
	    /*
	    else if (qName.equals("TranscriptomeSQLTemplate")) {
		TranscriptomeFactory.getDBFactory(globalContext).addSQLTemplate
		    (curData, transName);
		transName = null;
	    }
	    */
	    else if (qName.equals("TranscriptomeURLTemplate")) {
		TranscriptomeFactory.getXMLFactory(globalContext).setURLTemplate
		    (curData);
	    }

	    else if (qName.equals("TranscriptomeChrMergeURLTemplate")) {
		sysCfg.setTranscriptomeChrMergeURLTemplate(curData);
	    }

	    else if (qName.equals("LOHURLTemplate")) {
		XMLLOHFactory.getFactory(globalContext).setURLTemplate
		    (curData);
	    }

	    else if (qName.equals("Property")) {
		propList.add(curData);
	    }

	    else if (qName.equals("PropertyList")) {
		PropertyManager.getInstance().setPropertyList(propType,
							      propList);
	    }

	    curData = "";
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

	private String[] getAttrValues(String tag,
				       Attributes attrs,
				       String attrNames[])
	    throws SAXException {
	    return XMLUtils.getAttrValues("XMLSystemConfigFactory", tag,
					  attrs, attrNames);
	}
    }
}

