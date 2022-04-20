
/*
 *
 * XMLArrayDataFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.net.*;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class XMLArrayDataFactory { //extends DataFactory {

    private static SAXParserFactory factory;
    private PropertyElementFilter filter;
    private String type;
    private double maxy = Double.MAX_VALUE;
    private GlobalContext globalContext;
    private String url;
    private Handler handler;

    XMLArrayDataFactory(GlobalContext globalContext,
			PropertyElementFilter filter, String type) {
	this.globalContext = globalContext;

	this.filter = filter;
	this.type = type;
    }

    XMLArrayDataFactory(GlobalContext globalContext,
			PropertyElementFilter filter) {
	this(globalContext, filter, VAMPConstants.CGH_ARRAY_TYPE);
    }

    void setFilter(PropertyElementFilter filter) {
	this.filter = filter;
    }

    java.util.LinkedList getData(String url) {
	return getData(url, false);
    }

    java.util.LinkedList getData(String url, boolean update) {
	return getDataPerform(url, update, true, false);
    }

    java.util.LinkedList getData(String url, boolean update, boolean full) {
	return getDataPerform(url, update, full, false);
    }

    java.util.LinkedList getData(String url, boolean update, boolean full, boolean mute_error) {
	return getDataPerform(url, update, full, mute_error);
    }

    static int NN = 1;

    private java.util.LinkedList getDataPerform(String uri, boolean update, boolean full, boolean mute_error) {
	try {
	    String url = XMLUtils.makeURL(globalContext, uri);

	    GraphElement graphElem;
	    if (!update && full && (graphElem = GraphElementCache.getInstance().get(uri)) != null) {
		LinkedList graphElements = new LinkedList();
		//System.out.println("already in cache");
		graphElements.add(graphElem.asDataSet().clone_realize(false));
		return graphElements;
	    }

	    this.url = url;

	    if (GraphCanvas.VERBOSE) {
		System.out.println("Loading XML profile " + url);
	    }

	    InputStream is = Utils.openStream(url);
	    //is = Utils.tee(is, "/tmp/VAMP-" + NN++);
	    java.util.LinkedList list = getDataPerform(is, uri, update, full, mute_error);
	    is = null;
	    return list;

        } catch (FileNotFoundException e) {
	    if (!mute_error)
		InfoDialog.pop(globalContext, "File not found: " + url);
	    return null;
        } catch (Exception e) {
	    if (!mute_error) {
		e.printStackTrace();
		InfoDialog.pop(globalContext, e.getMessage());
	    }
	    return null;
        }
    }

    java.util.LinkedList getData(InputStream is) {
	return getDataPerform(is, null, false, true, false);
    }

    private java.util.LinkedList getDataPerform(InputStream is, String uri, boolean update, boolean full, boolean mute_error) {
	try {
	    if (factory == null)
		factory = SAXParserFactory.newInstance();

	    SAXParser saxParser = factory.newSAXParser();
	    handler = new Handler(filter, uri, update, full, false);
	    //handler = new Handler(filter, uri, update, full, true);
            saxParser.parse(is, handler);

	    if (handler.getError() != null) {
		if (!mute_error)
		    InfoDialog.pop(globalContext, "Error reported: " +
				   handler.getError());
		is.close();
		return null;
	    }

	    LinkedList graphElements = handler.getData();
	    is.close();
	    return graphElements;
	    
        } catch (FileNotFoundException e) {
	    InfoDialog.pop(globalContext, "File not found: " + url);
	    return null;
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, e.getMessage());
	    return null;
        }
    }

    String getName() {
	if (handler == null) return null;
	return handler.getSetName();
    }

    class Handler extends DefaultHandler {
	
	LinkedList graphElements;
	LinkedList curDataElementV;
	String curURL;
	GraphElement curGraphElement;
	String curType;
	DataElement curDataElement;
	String curXMLElement;
	int elemCnt;
	boolean verbose;
	String curData;
	PropertyElementFilter filter;
	String curArrayName;
	String setName;
	int dataelem_cnt = 0;
	String error;
	String srcURL;
	HashMap prop_map;
	boolean skip_props;
	Property objKeyProp;
	HashMap addPropMap;
	boolean cached;

	// gene management
	long curPosGeneEnd[];
	long curNegGeneEnd[];
	int curPosGeneInd;
	int curNegGeneInd;
	boolean curGeneStrand;
	boolean update;
	boolean full;
	boolean objState;
	int count_obj;

	static final int GENE_MAX_IND = 100;
	static final double BASE_GENE_OFF = 0.02;
	static final double INCR_GENE_OFF = 0.015;

	Handler(PropertyElementFilter filter, String srcURL, boolean update, boolean full, boolean verbose) {
	    this.filter = filter;
	    this.verbose = verbose;
	    this.update = update;
	    this.full = full;
	    this.error = null;
	    this.srcURL = srcURL;
	    graphElements = new LinkedList();
	    curGraphElement = null;
	    curType = null;
	    curGeneStrand = false;
	    curPosGeneEnd = new long[GENE_MAX_IND];
	    curNegGeneEnd = new long[GENE_MAX_IND];
	    initGeneEnd();
	    curXMLElement = "";
	    elemCnt = 0;
	    curData = null;
	    addPropMap = new HashMap();
	    cached = false;
	    objState = false;
	    count_obj = 0;
	}

	void initGeneEnd() {
	    for (int n = 0; n < GENE_MAX_IND; n++)
		curPosGeneEnd[n] = 0;
	    for (int n = 0; n < GENE_MAX_IND; n++)
		curNegGeneEnd[n] = 0;
	    curPosGeneInd = 0;
	    curNegGeneInd = 0;
	}

	String getSetName() {
	    return setName;
	}

	public void startElement(String namespaceURI,
				 String lName,
				 String qName,
				 Attributes attrs) throws SAXException	{
	    if (error != null)
		return;

	    if (cached) {
		curXMLElement = qName;
		curData = null;
		return;
	    }

	    if (verbose)
		System.err.println("<" + qName + ">");

	    if (qName.equals("ArraySet")) {
		graphElements = new LinkedList();
	    }
	    else if (qName.equals("Array")) {
		curGraphElement = new DataSet(full);
		initGeneEnd();
		curGraphElement.setSourceURL(srcURL);
		curDataElementV = new LinkedList();
		//objKeyProp = VAMPConstants.NameProp;
		objKeyProp = VAMPProperties.NmcProp;
	    }
	    else if (qName.equals("SampleAdditionalData")) {
		String values[] =
		    XMLUtils.getAttrValues("XMLArrayDataFactory",
					   qName, attrs,
					   new String[]{"URL"});
		setAdditionalProperties(values[0], curGraphElement);
	    }
	    else if (qName.equals("Obj")) {
		if (full) {
		    curDataElement = new DataElement();
		    curDataElement.declare(curGraphElement);
		    prop_map = new HashMap();
		    skip_props = false;
		}
		else if (!objState) {
		    count_obj = 0;
		    objState = true;
		}
		
	    }
	    curXMLElement = qName;
	    curData = null;
	}

	public void characters(char buf[], int offset, int len)
	    throws SAXException
	{
	    String s = new String(buf, offset, len);
	    if (curData == null)
		curData = s;
	    else
		curData += s;
	}

	public void endElement(String namespaceURI,
			       String sName,
			       String qName)
	    throws SAXException
	{
	    if (error != null)
		return;

	    if (cached) {
		if (!qName.equals("Array")) {
		    curXMLElement = "";
		    curData = "";
		    return;
		}
		//cached = false;
	    }

	    if (qName.equals("Ctm"))
		return;

	    if (curData == null)
		curData = "";
	    else
		curData = curData.trim();
	    if (verbose) {
		System.err.print(curData);
		System.err.println("</" + qName + ">\n");
	    }

	    if (!full) {
		if (objState && !qName.equals("Array")) {
		    if (qName.equals("Obj")) {
			count_obj++;
		    }
		    else if (qName.equals("X")) {
			long x = Utils.parseLong(curData);
			curGraphElement.setMinMaxX(x);
		    }
		    else if (qName.equals("Y")) {
			if (!curData.equals(VAMPProperties.NA)) {
			    double y = Utils.parseDouble(curData);
			    curGraphElement.setMinMaxY(y);
			}
		    }
		    curXMLElement = "";
		    curData = "";
		    return;
		}
	    }

	    if (curXMLElement.equals("Error")) {
		error = new String(curData);
	    }

	    if (curXMLElement.equals("NbObj")) {
		/*
		  int cnt = Utils.parseInt(s);
		  curGraphElement.data = new DataElement[cnt];
		*/
	    }
	    else if (curXMLElement.equalsIgnoreCase("ObjKey")) {
		// WARNING: must be reconnected ! 7/02/05
		objKeyProp = Property.getProperty(curData);
		addProperty(curGraphElement, curXMLElement, curData);
	    }
	    else if (curXMLElement.equalsIgnoreCase("URL")) {
		curURL = curData;
		GraphElement graphElem;
		if (!update && full && (graphElem = GraphElementCache.getInstance().get(curURL))
		    != null) {
		    curGraphElement = graphElem.asDataSet().clone_realize
			(false);
		    cached = true;
		    //System.out.println("isCached: " + graphElem + " -> " + curGraphElement);
		    curXMLElement = "";
		    curData = "";
		    return;
		}

		// moved 4/10/06
		// GraphElementCache.getInstance().put(curURL, curGraphElement);
	    }
	    else if (curXMLElement.equals("X")) {
		curDataElement.setPosX(curGraphElement, Utils.parseLong(curData));
	    }
	    else if (curXMLElement.equals("CopyNb")) {
		double y;
		if (curData.equals(VAMPProperties.NA)) {
		    addProperty(curDataElement, VAMPProperties.NA, "True");
		    y = 0;
		}
		else
		    y = Utils.parseDouble(curData);
		addProperty(curDataElement, curXMLElement, Utils.toString(y));
		curDataElement.setPosY(curGraphElement, y);
	    }
	    else if (curXMLElement.equals("Strand")) {
		curGeneStrand = curData.equals("+");
		addProperty(curDataElement, curXMLElement, curData);
	    }
	    else if (curXMLElement.equals("Y"))
		//  || (curXMLElement.equals("CorrelCoef") && // GTCA !!!
		// 	    curType.equals(VAMPConstants.GTCA_TYPE)))
		{
		    if (curType.equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
			/*
			  curY = Utils.parseDouble(curData);
			  double y = Utils.parseDouble(curData);
			  geneOff += INCR_GENE_OFF;

			  if (geneOff == MAX_GENE_OFF)
			  geneOff = BASE_GENE_OFF;

			  if (y < 0)
			  y = -geneOff;
			  else
			  y = geneOff;

			  curDataElement.setPosY(y);
			*/
		    }
		    else if (curData.equals(VAMPProperties.NA)) {
			curDataElement.setPosY(curGraphElement, 0.);
			addProperty(curDataElement, VAMPProperties.NA, "True");
		    }
		    else {
			// added 31/01/05
			addProperty(curDataElement, VAMPProperties.NA, "False");
			double y = Utils.parseDouble(curData);
			addProperty(curDataElement, "Ratio", Utils.toString(y));
			if (y > maxy) {
			    y = maxy;
			    curDataElement.setThresholded(true);
			}
			curDataElement.setPosY(curGraphElement, y);
		    }
		}
	    else if (curXMLElement.equals("Size")) {
		double size = curData.equalsIgnoreCase("NA") ? 0 : 
		    Utils.parseDouble(curData);
		addProperty(curDataElement, "Size", curData);
		// Does not put size on NA...
		// EV 19/07/04: disconnected test
		//if (curDataElement.getPropertyValue(VAMPConstants.IsNAProp) == null)
		curDataElement.setPosSize(curGraphElement, size);
	    }
	    else if (!curXMLElement.equals("ArraySet") &&
		     !curXMLElement.equals("Array") &&
		     !curXMLElement.equals("Obj") &&
		     !curXMLElement.equals("SampleAdditionalData")) {
		if (type.equals(VAMPConstants.LOH_TYPE) &&
		    curXMLElement.equals("Ratio")) {
		    if (curData.equals(VAMPProperties.NA))
			curDataElement.setPosY(curGraphElement, 0.);
		    else {
			double y = Utils.parseDouble(curData);
			curDataElement.setPosY(curGraphElement, y);
			//curDataElement.setPosY(curGraphElement, VAMPConstants.LOH_POS_Y);
		    }
		}
		else if (curXMLElement.equals("Ratio") ||
			 curXMLElement.equals("RatioScale"))
		    curXMLElement = VAMPProperties.RatioScaleProp.getName();

		if (curDataElement != null)
		    addProperty(curDataElement, curXMLElement, curData);
		else if (curGraphElement != null)
		    addProperty(curGraphElement, curXMLElement, curData);
	    }

	    if (qName.equals("SetName"))
		setName = curData;

	    if (qName.equals("Name") && curDataElement == null)
		curArrayName = curData;

	    if (qName.equals("Obj")) {
		if (curDataElement != null) {
		    addProperty(curDataElement, "Position",
				(new Long((long)curDataElement.getVX(curGraphElement))).toString());
		    if (filter == null || filter.accept(curDataElement)) {
			addProperty(curDataElement, "Array", curArrayName);
			if (full) {
			    curDataElementV.add(curDataElement);
			}
		    }
		    propEpilogue(curDataElement);
		}
	    }
	    else if (qName.equals("Array")) {
		objState = false;
		if (filter == null || filter.accept(curGraphElement)) {
		    if (!cached) {
			((DataSet)curGraphElement).setData(DataElement.makeData(curDataElementV));
		    }
		    Integer count;
		    if (full)
			count = new Integer(((DataSet)curGraphElement).getData().length);
		    else
			count = new Integer(count_obj);


		    Property prop;

		    if (curType.equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
			curGraphElement.setGraphElementDisplayer
			    (new GenomeAnnotDataSetDisplayer());
		    }

		    if (type.equals(VAMPConstants.LOH_TYPE))
			prop = VAMPProperties.MicrosatCountProp;
		    else if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE))
			prop = VAMPProperties.ProbeSetCountProp;
		    else
			prop = VAMPProperties.CloneCountProp;

		    curGraphElement.setPropertyValue(prop, count, false);

		    if (curURL == null)
			curURL = url;
		    curGraphElement.setURL(curURL);

		    graphElements.add(curGraphElement);
		    cached = false;
		}

		// moved 4/10/06

		GraphElementCache.getInstance().put(curURL, curGraphElement);

		curGraphElement = null;
		curDataElement = null;
		elemCnt = 0;
	    }
	    else if (qName.equals("Obj")) {
		curDataElement = null;
		elemCnt++;
	    }


	    // 25/05/05
	    if (curType != null && curXMLElement != null &&
		curType.equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
		if (curXMLElement.equals("GeneBegin")) {
		    long geneBegin = Utils.parseLong(curData);
		    curDataElement.setPosX(curGraphElement, geneBegin);
		    double y = 0;
		    if (curGeneStrand) {
			//y = BASE_GENE_OFF;
			for (curPosGeneInd = 0; curPosGeneInd < GENE_MAX_IND;
			     curPosGeneInd++) {
			    if (geneBegin > curPosGeneEnd[curPosGeneInd] ||
				curPosGeneInd == GENE_MAX_IND - 1) {
				if (curPosGeneInd == GENE_MAX_IND - 1)
				    System.out.println("Oups max overlap");

				y = BASE_GENE_OFF +
				    curPosGeneInd * INCR_GENE_OFF;
				break;
			    }
			}
		    }
		    else {
			//y = -BASE_GENE_OFF;
			for (curNegGeneInd = 0; curNegGeneInd < GENE_MAX_IND;
			     curNegGeneInd++) {
			    if (geneBegin > curNegGeneEnd[curNegGeneInd] ||
				curNegGeneInd == GENE_MAX_IND - 1) {
				if (curNegGeneInd == GENE_MAX_IND - 1)
				    System.out.println("Oups max overlap");
				y = -(BASE_GENE_OFF + curNegGeneInd * INCR_GENE_OFF);
				break;
			    }
			}
		    }
		    
		    curDataElement.setPosY(curGraphElement, y);
		}
		else if (curXMLElement.equals("GeneEnd")) {
		    long geneEnd = Utils.parseLong(curData);
		    if (curGeneStrand)
			curPosGeneEnd[curPosGeneInd] = geneEnd;
		    else
			curNegGeneEnd[curNegGeneInd] = geneEnd;
		}
	    }

	    curXMLElement = "";
	    curData = "";
	}

	public void error(SAXParseException e)
	    throws SAXException {
	    System.err.println("XMLArrayDataFactory: SAX Error at line #" + e.getLineNumber());
	}

	public void warning(SAXParseException e) {
	    System.err.println("XMLArrayDataFactory: SAX Warning at line #" + e.getLineNumber());
	}

	public void ignorableWhitespace(char[] ch, int start, int length) {
	}

	public void fatalError(SAXParseException e)
	    throws SAXException {
	    System.err.println("XMLArrayDataFactory: SAX Fatal Error at line #" + e.getLineNumber());
	}

	private void setAdditionalProperties(String url,
					     GraphElement graphElem) {
	    if (url.trim().length() == 0)
		return;

	    Document doc = (Document)addPropMap.get(url);
	    if (doc == null) {
		doc = XMLDOM.getInstance().parse(globalContext, url, false);
		if (doc == null)
		    return;
		addPropMap.put(url, doc);
	    }

	    Stack propV = new Stack();
	    setAdditionalProperties(graphElem, doc, propV);
	}

	private void setAdditionalProperties(GraphElement graphElem, Node node,
					     Stack propV) {
	    int propVsz = propV.size();
	    for (Node child = node.getFirstChild();
		 child != null;
		 child = child.getNextSibling()) {

		if (child.getNodeType() == Node.ELEMENT_NODE) {
		    Property prop = Property.getProperty(child.getNodeName());
		    propV.add(prop);
		    int sz = propV.size();
		    for (int n = 0; n < propV.size(); n++)
			prop.setPropertyValue((Property)propV.get(n), "true");

		    setAdditionalProperties(graphElem, child, propV);


		    // 29/03/05: disconnected
		    /*
		      if (graphElem.getPropertyValue(prop) == null)
		      graphElem.setPropertyValue(prop, "true");
		    */

		    propV.pop();
		} else if (child.getNodeType() == Node.TEXT_NODE) {
		    Property prop = (Property)propV.get(propV.size()-1);
		    String s = (String)graphElem.getPropertyValue(prop);
		    if (s == null)
			s = "";
		    s += child.getNodeValue();
		    s = s.trim();
		    if (s.length() > 0)
			graphElem.setPropertyValue(prop, s);
		}

	    }
	}

	private Property getProperty(String propName) {
	    if (propName.length() == 0) return null;
	    /*
	      if (propName.equals("NbP"))
	      return VAMPConstants.PointCountProp;
	    */
	    return Property.getProperty(propName);
	}

	private void setSharedProp(PropertyElement shared) {
	    Iterator it = prop_map.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		if (prop.isShared()) {
		    shared.setPropertyValue((Property)entry.getKey(),
					    entry.getValue(), false);
		}
	    }

	    shared.setCompleted();
	}

	private void propEpilogue(DataElement elem) {
	    Iterator it = prop_map.entrySet().iterator();
	    PropertyElement shared = null;
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		if (prop == objKeyProp) {
		    shared = prop.getSharedElem(entry.getValue(), false);
		    if (shared == null) {
			shared = prop.getSharedElem(entry.getValue(), true);
			setSharedProp(shared);
		    }
		    break;
		}
	    }

	    elem.setSharedElem(shared);

	    it = prop_map.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		Property prop = (Property)entry.getKey();
		if (!prop.isShared() || shared == null)
		    elem.setPropertyValue((Property)entry.getKey(),
					  entry.getValue(), false);
	    }
	}

	static final boolean PROP_OPTIM = true;

	private void addProperty(DataElement elem, String propName, String value) {
	    if (propName.equals("Ctm"))
		return;

	    Property prop = getProperty(propName);
	    if (prop == null)
		return;

	    if (PROP_OPTIM) {
		if (!skip_props || !prop.isShared()) {
		    if (prop == objKeyProp) {
			PropertyElement shared = prop.getSharedElem(value, false);
			if (shared != null && shared.isCompleted())
			    skip_props = true;
		    }

		    prop_map.put(prop, value);
		}
	    }
	    else {
		if (verbose)
		    System.err.println("addDataElementProperty " + elem + ", " + propName + ", " + value);
		elem.setPropertyValue(prop, value, false);
	    }
	}

	private void addProperty(GraphElement set, String propName, String value) {
	    Property prop = getProperty(propName);
	    if (prop == null) return;
	    if (verbose)
		System.err.println("addSetProperty -> " + propName + ", " + value);
	    if (propName.equals("Type")) {
		if (value.equals("CGH"))
		    value = VAMPConstants.CGH_ARRAY_TYPE;
		else if (value.equals("AFFY-SNP"))
		    value = VAMPConstants.SNP_TYPE;
		else if (value.equals("TRS"))
		    value = VAMPConstants.TRANSCRIPTOME_TYPE;
		else if (value.equals("TRS_CLS"))
		    value = VAMPConstants.TRANSCRIPTOME_CLUSTER_TYPE;
		else if (value.equals("DiffAna"))
		    value = VAMPConstants.DIFFANA_TYPE;
		else if (value.equals("GTCA"))
		    value = VAMPConstants.GTCA_TYPE;

	    }

	    set.setPropertyValue(prop, value, false);

	    if (propName.equals(VAMPProperties.RatioScaleProp.getName())) {
		String type = VAMPUtils.getType(set);
		boolean is_log = value.equals(VAMPConstants.RatioScale_L);
		if (type.equals(VAMPConstants.CHIP_CHIP_TYPE)) {
		    set.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_CHIP_CHIP, false);
		}
		else if (type.equals(VAMPConstants.GTCA_TYPE)) {
		    set.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_GTCA, false);
		}
		else if (type.equals(VAMPConstants.SNP_TYPE)) {
		    set.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_SNP, false);
		}
		else if (type.equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
		    //set.setPropertyValue(VAMPConstants.CCNameProp, null, false);
		}
		else {
		    // ?? if (VAMPUtils.getType(set).equals(VAMPConstants.CGH_ARRAY_TYPE)) {
		    set.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_CGH, false);
		}
	    }

	    if (propName.equals("Type")) {
		String type = VAMPUtils.getType(set);
		curType = type;

		if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
		    type.equals(VAMPConstants.TRANSCRIPTOME_MERGE_TYPE) ||
		    type.equals(VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE)) {
		    set.setPropertyValue(VAMPProperties.ThresholdsNameProp,
					 VAMPConstants.THR_TRS, false);
		}
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_REL_TYPE)) {
		    set.setPropertyValue(VAMPProperties.ThresholdsNameProp,
					 VAMPConstants.THR_TRSREL, false);
		}
		else if (type.equals(VAMPConstants.SNP_TYPE)) {
		    set.setPropertyValue(VAMPProperties.ThresholdsNameProp,
					 VAMPConstants.THR_SNP, false);
		}
		else if (type.equals(VAMPConstants.DIFFANA_TYPE) ||
			 type.equals(VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE)) {
		    set.setPropertyValue(VAMPProperties.ThresholdsNameProp,
					 VAMPConstants.THR_DIFFANA, false);
		}
		else if (type.equals(VAMPConstants.GTCA_TYPE) ||
			 type.equals(VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE)) {
		    set.setPropertyValue(VAMPProperties.ThresholdsNameProp,
					 VAMPConstants.THR_GTCA, false);
		}
		else if (type.equals(VAMPConstants.CHIP_CHIP_TYPE) ||
			 type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE) ||
			 type.equals(VAMPConstants.CHIP_CHIP_AVERAGE_TYPE) ||
			 type.equals(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE)) {
		    set.setPropertyValue(VAMPProperties.ThresholdsNameProp,
					 VAMPConstants.THR_CHIP_CHIP, false);
		}
		else
		    set.setPropertyValue(VAMPProperties.ThresholdsNameProp,
					 VAMPConstants.THR_CGH, false);
	    }
	}

	/*
	  private void copyProperty(GraphElement from, GraphElement to) {
	  TreeMap props = from.getProperties();
	  Iterator it = props.entrySet().iterator();
	  while (it.hasNext()) {
	  Map.Entry entry = (Map.Entry)it.next();
	  to.setPropertyValue((Property)entry.getKey(),
	  entry.getValue());
	  }
	  }
	*/

	void display() {
	    for (int i = 0; i < graphElements.size(); i++) {
		GraphElement graphElement = ((GraphElement)graphElements.get(i));
		graphElement.display();
		DataElement data[] = ((DataSet)graphElement).getData();
		System.out.println("count: " + data.length);
		for (int j = 0; j < data.length; j++) {
		    System.out.println("X: " + data[j].getVX(curGraphElement));
		    System.out.println("Y: " + data[j].getVY(curGraphElement));
		    data[j].display();
		}
	    }
	}

	LinkedList getData() {
	    for (int i = 0; i < graphElements.size(); i++) {
		GraphElement graphElement = ((GraphElement)graphElements.get(i));
		if (graphElement.getPropertyValue(VAMPProperties.CenterPosProp) != null) {
		    DataElement data[] = ((DataSet)graphElement).getData();
		    for (int j = 0; j < data.length; j++) {
			DataElement d = data[j];
			d.setPosX(graphElement, d.getPosX(graphElement) -
				  d.getPosSize(graphElement)/2);
		    }
		}
	    }

	    if (VAMPConstants._DISPLAY_)
		display();
	    curDataElementV = null;
	    return graphElements;
	}

	String getError() {return error;}
    }


    protected void finalize() throws Throwable {
	//System.out.println("XMLArrayDataFactory finalize");
	filter = null;
	type = null;
	globalContext = null;
	url = null;
	handler = null;
    }
}
