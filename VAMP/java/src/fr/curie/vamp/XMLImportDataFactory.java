
/*
 *
 * XMLImportDataFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import javax.swing.tree.*;

import java.net.*;
import java.io.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class XMLImportDataFactory {

    private static SAXParserFactory factory;
    private GlobalContext globalContext;
    private String url;
    private Handler handler;

    XMLImportDataFactory(GlobalContext globalContext) {
	this.globalContext = globalContext;
    }

    JTree makeTree(String url) {
	try {
	    this.url = url;

	    InputStream is = Utils.openStream(url);
	    if (factory == null)
		factory = SAXParserFactory.newInstance();

	    SAXParser saxParser = factory.newSAXParser();
	    handler = new Handler();
            saxParser.parse(is, handler);
	    return handler.makeTree();
	    
        } catch (FileNotFoundException e) {
	    InfoDialog.pop(globalContext, "File not found: " + url);
	    return null;
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, e.getMessage());
	    return null;
        }
    }

    class Handler extends DefaultHandler {
	
	JTree tree;
	DefaultMutableTreeNode rootNode;
	String folderType = null;
	Stack folderStack = new Stack();

	Handler() {
	    rootNode = new DefaultMutableTreeNode("VAMP Data");
	    folderStack.push(rootNode);
	}

	private String[] getAttrValues(String tag,
				       Attributes attrs,
				       String attrNames[])
	    throws SAXException {
	    return XMLUtils.getAttrValues("XMLImportDataFactory", tag,
					  attrs, attrNames);
	}

	public void startElement(String namespaceURI,
				 String lName,
				 String qName,
				 Attributes attrs) throws SAXException	{
	    if (qName.equals("Folder")) {
		String values[] =
		    getAttrValues(qName, attrs, new String[]{"label",
							     "type?"});

		String label = values[0];
		folderType = values[1];

		DefaultMutableTreeNode newNode =
		    new DefaultMutableTreeNode(label);
		
		DefaultMutableTreeNode folderNode =
		    (DefaultMutableTreeNode)folderStack.peek();
		folderNode.add(newNode);
		
		folderStack.push(newNode);
	    }
	    else if (qName.equals("Item")) {
		String values[] =
		    getAttrValues(qName, attrs, new String[]{"label",
							     "url",
							     "type?",
							     "mode?",
							     "chr?"});
		String label = values[0];
		String url = values[1];
		String type = values[2];
		String import_mode_str = values[3];
		String chr = values[4];

		if (type == null) {
		    type = folderType;
		}

		int import_mode = ImportData.XML_IMPORT;

		if (import_mode_str != null) {
		    if (import_mode_str.equalsIgnoreCase("serial+optim")) {
			import_mode = ImportData.SERIAL_IMPORT|ImportData.GRAPHIC_OPTIM_IMPORT;
		    }
		    else if (import_mode_str.equalsIgnoreCase("serial")) {
			import_mode = ImportData.SERIAL_IMPORT;
		    }
		    else {
			throw new SAXException("Invalid mode " + import_mode_str);
		    }
		}

		if (chr == null) {
		    chr = ImportDataItem.PANGEN_STR;
		}

		DefaultMutableTreeNode folderNode =
		    (DefaultMutableTreeNode)folderStack.peek();
		DefaultMutableTreeNode newNode =
		    new DefaultMutableTreeNode
		    (new ImportDataItem(label, url, type, import_mode, chr));
			 
		folderNode.add(newNode);
	    }
	}

	public void characters(char buf[], int offset, int len)
	    throws SAXException
	{
	}

	public void endElement(String namespaceURI,
			       String sName,
			       String qName)
	    throws SAXException
	{
	    if (qName.equals("Folder")) {
		folderType = null;
		folderStack.pop();
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

	JTree makeTree() {
	    if (tree == null) {
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode
		    (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	    }

	    return tree;
	}
    }
}

