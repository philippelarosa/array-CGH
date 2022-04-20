
/*
 *
 * XMLMiniMapDataFactory.java
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
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class XMLMiniMapDataFactory extends MiniMapDataFactory {

    private String url_base;
    private String resolutions[];
    private String default_resolution;
    private GlobalContext globalContext;
    private static SAXParserFactory factory = SAXParserFactory.newInstance();
    private Hashtable dataHT;

    XMLMiniMapDataFactory(GlobalContext globalContext, String resolution,
			  String url_base,
			  String resolution_str,
			  String default_resolution) {
	super(globalContext, resolution);
	this.globalContext = globalContext;
	if (url_base.charAt(url_base.length()-1) != '/')
	    url_base += "/";
	this.url_base = url_base;

	resolutions = resolution_str.split(":");
	this.default_resolution = (default_resolution != null ?
				   default_resolution : resolutions[0]);
	dataHT = new Hashtable();
    }

    String [] getSupportedResolutions() {
	return resolutions;
    }

    String getDefaultResolution() {
	return default_resolution;
    }

    Cytoband getData(String resolution) {
	Cytoband cytoband = (Cytoband)dataHT.get(resolution);
	if (cytoband != null)
	    return cytoband;

	String url = url_base + "cytoband_" + resolution + ".xml";
	try {
	    InputStream is = Utils.openStream(url);
	    if (factory == null)
		factory = SAXParserFactory.newInstance();

	    SAXParser saxParser = factory.newSAXParser();
	    Handler handler = new Handler();
            saxParser.parse(is, handler);
	    cytoband = handler.getData();
	    if (cytoband != null)
		dataHT.put(resolution, cytoband);
	    return cytoband;
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
	
	String curData;
	Cytoband cytoband;
	Chromosome chromo;
	String cytoName, cytoOG, cytoRSL;
	Vector bands;

	Handler() {
	    cytoband = null;
	}

	public void startElement(String namespaceURI,
				 String lName,
				 String qName,
				 Attributes attrs) throws SAXException	{
	    if (qName.equals("Band")) {
		if (attrs != null) {
		    String name=null, arm=null, giestain = null;
		    int begin = -1, end = -1, code = -1, colorCode = -1;
		    int len = attrs.getLength();
		    for (int i = 0; i < len; i++) {
			String aName = attrs.getQName(i);
			String value = attrs.getValue(i);
			if (aName.equals("name"))
			    name = value;
			else if (aName.equals("arm"))
			    arm = value;
			else if (aName.equals("start"))
			    begin = Utils.parseInt(value);
			else if (aName.equals("end"))
			    end = Utils.parseInt(value);
			else if (aName.equals("code"))
			    code = Utils.parseInt(value);
			else if (aName.equals("colour"))
			   colorCode = Utils.parseInt(value.substring(1, value.length()), 16);
			else if (aName.equals("giestain"))
			    giestain = value;
		    }
		    bands.add(new Band(chromo, arm, name, begin, end, code,
				       colorCode, giestain));
		}
	    }
	    curData = "";
	}

	public void characters(char buf[], int offset, int len)
	    throws SAXException
	{
	    String s = new String(buf, offset, len);
	    curData += s;
	}

	public void endElement(String namespaceURI,
			       String sName,
			       String qName
			       )
	    throws SAXException
	{
	    if (qName.equals("SetName"))
		cytoName = curData;
	    else if (qName.equals("Organism"))
		cytoOG = curData;
	    else if (qName.equals("Resolution"))
		cytoRSL = curData;
	    else if (qName.equals("Name")) {
		if (cytoband == null)
		    cytoband = new Cytoband(cytoName, cytoOG, cytoRSL);
		/*
		else
		    cytoband.addChromosome(chromo);
		if (chromo != null)
		    chromo.setBands(bands.toArray());
		*/
		bands = new Vector();
		chromo = new Chromosome(curData);
	    }
	    else if (qName.equals("Chromosome")) {
		chromo.setBands(bands.toArray());
		cytoband.addChromosome(chromo);
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

 	Cytoband getData() {
	    return cytoband;
	}
    }
}
