
/*
 *
 * XMLLoadDataFactory.java
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

class XMLLoadDataFactory {

    private static SAXParserFactory factory = SAXParserFactory.newInstance();
    private Handler handler;
    private SAXParser saxParser;
    private GlobalContext globalContext;
    static final String DENDRO_PREFIX = "#CLUSTER#";
    static final String DENDRO_CHILD = "Child";
    private boolean normalize;
    private boolean verbose;

    XMLTranscriptomeFactory trsFactory;

    XMLLoadDataFactory(GlobalContext globalContext, boolean verbose,
		       boolean normalize) {
	this.globalContext = globalContext;
	this.normalize = normalize;
	this.verbose = verbose;
	trsFactory = new XMLTranscriptomeFactory(globalContext);
	try {
	    saxParser = factory.newSAXParser();
	    // changed 8/11/06
	    //handler = new Handler(this, verbose);
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadDataFactory: ", e);
        }
    }

    /*
    boolean setData(File file, View view, boolean replace) {
	try {
	    FileInputStream is = new FileInputStream(file);
	    return setData(is, file, view, replace, null);
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadDataFactory: ", e);
	    return false;
        }
    }
    */

    boolean setData(InputStream is, String url, View view, boolean replace,
		    RemoteOP op) {
	try {
	    handler = parse(is);

	    if (handler.getError() != null) {
		InfoDialog.pop(globalContext, "Error reported: " +
			       handler.getError());
		return false;
	    }

	    if (handler.setData(view, replace, op)) {
		updateViewTitle(view, url);
		return true;
	    }
	    return false;
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadDataFactory: ", e);
	    return false;
        }
    }

    LinkedList[] getGraphElements(InputStream is) {
	try {
	    handler = parse(is);

	    if (handler.getError() != null) {
		InfoDialog.pop(globalContext, "Error reported: " +
			       handler.getError());
		return null;
	    }

	    return handler.getGraphElements();
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadDataFactory: ", e);
	    return null;
        }
    }

    ViewFrame makeViewFrame(GlobalContext globalContext, InputStream is,
			    String url) {
	return makeViewFrame(globalContext, is, url, null);
    }

    ViewFrame makeViewFrame(GlobalContext globalContext, InputStream is,
			    String url, Task task) {

	try {
	    handler = parse(is);

	    if (handler.getError() != null) {
		InfoDialog.pop(globalContext, "Error reported: " +
			       handler.getError());
		return null;
	    }

	    ViewFrame frame = handler.makeViewFrame(globalContext, task);
	    if (frame != null)
		updateViewTitle(frame.getView(), url);
	    return frame;
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadDataFactory: ", e);
	    return null;
        }
    }

    private Handler parse(InputStream is) throws Exception {
	boolean show_ind = DataSet.SHOW_IND;
	boolean show_id = DataSet.SHOW_ID;
	
	handler = new Handler(this, verbose);

	handler.init();
	saxParser.parse(is, handler);
	
	DataSet.SHOW_IND = show_ind;
	DataSet.SHOW_ID = show_id;

	return handler;
    }

    String getStringVersion() {
	if (handler != null)
	    return handler.getStringVersion();
	return "";
    }

    void postAction(View view) {
	handler.postAction(view);
    }

    class Handler extends DefaultHandler {
	
	String viewName;
	GraphElementDisplayer graphElementDisplayer;
	AxisDisplayer axisDisplayer;
	GraphElementIDBuilder graphElementIDBuilder;
	GraphElementListOperation autoApplyDSLOP = null;

	boolean yaxisAutoAdapt = false;
	String panel_name;
	int orientation = PanelSplitLayout.HORIZONTAL;
	int scrollMask;
	
	boolean hasX;

	boolean isLocked[];
	boolean isDisabled[];

	int XSlide_min, XSlide_max;
	int YSlide_min, YSlide_max;
	int marginNorth, marginEast, marginWest, marginSouth;

	int width, height;
	int axisNorth = 0, axisSouth = 0, axisWest = 0, axisEast = 0;
	boolean axisMask = false;
	PanelProfile panelProfiles[];

	boolean fitInPage;

	double scaleX[], scaleY[];
	int dividers[];
	LinkedList panelGraphElements[];

	LinkedList marks;
	LinkedList regions;
	XMLLoadDataFactory load_factory;	
	XMLArrayDataFactory array_factory;
	Dendrogram dendro;
	String dendro_type;
	HashMap dendro_map;
	HashMap dendro_prop_map;
	DendrogramNode dendro_root;

	boolean verbose;
	String version_str;

	//
	// contextual information
	// 

	String curData;
	//boolean isBottom;
	String error;
	int panel_num;

	Stack opGraphElementStack;
	Stack extraPropertyStack;
	Stack attrsStack;
	Stack layoutStack;
	Stack layoutOrientationStack;

	Vector panelLinks;
	String linksName;
	int linksSynchro;

	LinkedList lastGraphElementList;
	PropertyElement lastElem;
	boolean showSize = true;
	boolean configState, hasConfig;
	LinkedList dendroGraphElementList;
	String bgImg;

	Resources.AnnotContext annot_ctx;

	Handler(XMLLoadDataFactory load_factory, boolean verbose) {
	    this.load_factory = load_factory;
	    this.verbose = verbose;
	    this.verbose = false;
	    this.array_factory =
		new XMLArrayDataFactory(globalContext, null);
	}

	void init() {
	    opGraphElementStack = new Stack();
	    extraPropertyStack = new Stack();
	    attrsStack = new Stack();
	    layoutStack = new Stack();
	    layoutOrientationStack = new Stack();
	    panelLinks = new Vector();
	    panelGraphElements = null;
	    marks = new LinkedList();
	    regions = new LinkedList();
	    scrollMask = PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_EAST;
	    panel_name = "";
	    //isBottom = false;
	    error = null;
	    lastGraphElementList = null;
	    lastElem = null;
	    //topScaleX = topScaleY = bottomScaleX = bottomScaleY = 1.;
	    scaleX = scaleY = null;
	    dividers = null;

	    configState = false;
	    hasConfig = false;
	    dendro = null;
	    dendro_type = null;
	    dendro_map = null;
	    dendro_prop_map = null;
	    dendro_root = null;
	    panelProfiles = null;
	    panel_num = 0;
	    dendroGraphElementList = new LinkedList();
	    makePanels(1);
	    marginNorth = Config.defaultMargins.getMarginNorth();
	    marginWest = Config.defaultMargins.getMarginWest();
	    marginEast = Config.defaultMargins.getMarginEast();
	    marginSouth = Config.defaultMargins.getMarginSouth();
	    //annot_ctx = new Resources.AnnotContext();
	    annot_ctx = null;
	    hasX = true;
	    bgImg = null;
	}

	public void startElement(String namespaceURI,
				 String lName,
				 String qName,
				 Attributes attrs) throws SAXException	{
	    if (error != null)
		return;

	    if (verbose) {
		System.out.print("<" + qName + " ");
		XMLUtils.displayAttrs(attrs);
		System.out.println(">");
	    }

	    if (qName.equals("CGHConfig")) {
		configState = true;
		hasConfig = true;
		annot_ctx = new Resources.AnnotContext();
		return;
	    }

	    if (configState) {
		Resources.startAnnots(null, qName, attrs, annot_ctx);
		curData = "";
		return;
	    }

	    // TBD: local annotations
	    /*
	    if (Resources.startAnnots(view, qName, attrs, annot_ctx))
		return;
	    */

	    if (qName.equals("DataElement")) {
		if (lastGraphElementList != null && lastGraphElementList.size() > 0) {
		    String values[] =
			XMLUtils.getAttrValues("XMLLoadDataFactory",
					       qName, attrs,
					       new String[]{"num", "ID"});
		    GraphElement graphElement = (GraphElement)lastGraphElementList.get(0);
		    int num = getInt(values[0]);
		    lastElem = ((DataSet)graphElement).getData()[num];
		}
	    }
	    else if (qName.equals("Property")) {
		String values[] =
		    XMLUtils.getAttrValues("XMLLoadDataFactory",
					   qName, attrs,
					   new String[]{"key", "value"});
		Property prop = Property.getProperty(values[0]);
		String value = values[1].replaceAll
		    (XMLSaveDataProducer.NEWLINE, "\n");
		if (lastElem != null) {

		    if (lastElem instanceof Region) {
			if (prop.getName().equals("Size"))
			    prop = VAMPProperties.RegionSizeProp;
			else if (prop.getName().equals("Gnl"))
			    prop = VAMPProperties.GNLProp;;
		    }

		    lastElem.setPropertyValue(prop, value);
		}
		else if (lastGraphElementList != null &&
			 lastGraphElementList.size() > 0) {
		    int sz = lastGraphElementList.size();
		    for (int n = 0; n < sz; n++) {
			GraphElement graphElement = (GraphElement)lastGraphElementList.get(n);
			graphElement.setPropertyValue(prop, value);
		    }
		}
	    }
	    else if (qName.equals("ChromosomeMerge") ||
		     qName.equals("TranscriptomeChromosomeMerge") ||
		     qName.equals("SNPChromosomeMerge") ||
		     qName.equals("FrAGL") ||
		     qName.equals("GTCA") ||
		     qName.equals("BreakpointFrequency") ||
		     qName.equals("SyntenySwitch") ||
		     qName.equals("Synteny") ||
		     qName.equals("RegionSynteny") ||
		     qName.equals("TranscriptomeAverage") ||
		     qName.equals("Average") ||
		     qName.equals("ArrayMerge") ||
		     qName.equals("ChipChipChromosomeMerge") ||
		     qName.equals("GenomeAnnotChromosomeMerge") ||
		     qName.equals("ChipChipAverage") ||
		     qName.equals("ChipChipArrayMerge") ||
		     qName.equals("TranscriptomeMerge") ||
		     qName.equals("TranscriptomeRelative")) {
		opGraphElementStack.push(new LinkedList());
		extraPropertyStack.push(getExtraProperties(qName, attrs));
		attrsStack.push(makeMap(attrs));
	    }
	    else if (qName.equals("Array") ||
		     qName.equals("ChipChip") ||
		     qName.equals("Transcriptome") ||
		     qName.equals("LOH")) {
		if (attrs != null) {
		    extraPropertyStack.push(getExtraProperties(qName, attrs));
		    attrsStack.push(makeMap(attrs));
		    String url = attrs.getValue("URL");

		    if (qName.equals("Transcriptome"))
			loadTrans(url, attrs);
		    else if (qName.equals("LOH"))
			loadLOH(url, attrs);
		    else
			loadGraphElements(url, attrs);
		    extraPropertyStack.pop();
		    attrsStack.pop();
		}
	    }
	    else if (qName.equals("Landmark") ||
		     qName.equals("Mark")) {
		String position = attrs.getValue("position");
		String color = attrs.getValue("color");

		if (position != null) {
		    Mark mark =
			makeMark(position, attrs.getValue("xid"),
				 (GraphElement)panelGraphElements[0].get(0));
		    if (color != null)
			mark.setColor(new Color(getInt(color)));
		    marks.add(mark);
		    lastElem = mark;
		}
	    }
	    else if (qName.equals("Region")) {
		String begin = attrs.getValue("begin");
		String end = attrs.getValue("end");
		String color = attrs.getValue("color");

		if (begin != null && end != null) {
		    Mark mBegin = getMark(marks, begin, attrs.getValue("begin_xid"));
		    Mark mEnd = getMark(marks, end, attrs.getValue("end_xid"));
		    Region region = new Region(mBegin, mEnd);
		    if (color != null)
			region.setColor(new Color(getInt(color)));
		    regions.add(region);
		    lastElem = region;
		}
	    }
	    else if (qName.equals("Dendrogram")) {
		dendro_type = attrs.getValue("type");
		if (dendro_type == null)
		    dendro_type = "Y";
		dendro_map = new HashMap();
		dendro_prop_map = new HashMap();
	    }
	    else if (qName.equals("DendroNodePropertyList")) {
		String values[] =
		    XMLUtils.getAttrValues("XMLLoadDataFactory",
					   qName, attrs,
					   new String[]{"ID", "color?"});

		lastElem = (PropertyElement)dendro_prop_map.get(values[0]);
		if (values[1] != null)
		    ((DendrogramGraphElement)lastElem).setColor
			(new Color(Integer.parseInt(values[1])),
			 DendrogramGraphElement.LOCAL_MODE);
	    }
	    else if (qName.equals("DendroNode")) {
		String values[] =
		    XMLUtils.getAttrValues("XMLLoadDataFactory",
					   qName, attrs,
					   new String[]{"ID", "height",
							"left", "right",
							"left_order?",
							"right_order?"});
		String id = values[0];
		double height = Utils.parseDouble(values[1]);
		String left = values[2];
		String right = values[3];
		String left_order = values[4];
		String right_order = values[5];

		DendrogramNode left_node, right_node;
		if (left.startsWith(DENDRO_PREFIX)) {
		    left_node = (DendrogramNode)dendro_map.get(left);
		    if (left_node == null) {
			error = "Dendrogram left " + left + " not found";
			return;
		    }
		}
		else {
		    if ((left_node = (DendrogramNode)dendro_map.get
			 (DENDRO_PREFIX + left)) != null)
			System.err.println("WARNING: dendrogram left inconsistency : " +
					   left + " is both a leave and a node");
		    else
			left_node = new DendrogramLeaf(left, left_order);
		}

		if (right.startsWith(DENDRO_PREFIX)) {
		    right_node = (DendrogramNode)dendro_map.get(right);
		    if (right_node == null) {
			error = "Dendrogram right " + right + " not found";
			return;
		    }

		}
		else {
		    if ((right_node = (DendrogramNode)dendro_map.get
			 (DENDRO_PREFIX + right)) != null)
			System.err.println("WARNING: dendrogram right inconsistency : " +
					   right + " is both a leave and a node");
		    else
			right_node = new DendrogramLeaf(right, right_order);
		}

		String sid = id;
		if (id.startsWith(DENDRO_PREFIX))
		    sid = id.substring(DENDRO_PREFIX.length(),
				       id.length());

		dendro_root = new DendrogramBinNode(sid, height,
						    left_node,
						    right_node);

		DendrogramGraphElement dendroGE_left =
		    new DendrogramGraphElement(left_node);

		dendroGE_left.setChild((DendrogramGraphElement)left_node.getUserData(DENDRO_CHILD));

		DendrogramGraphElement dendroGE_right =
		    new DendrogramGraphElement(right_node);
		dendroGE_right.setChild((DendrogramGraphElement)right_node.getUserData(DENDRO_CHILD));

		DendrogramGraphElement dendroGE_bridge =
		    new DendrogramGraphElement(dendro_root,
					       dendroGE_left,
					       dendroGE_right);

		dendro_root.setUserData(DENDRO_CHILD, dendroGE_bridge);

		LinkedList l = new LinkedList();
		l.add(dendroGE_left);
		l.add(dendroGE_right);
		l.add(dendroGE_bridge);

		dendroGraphElementList.addAll(l);

		addGraphElements(l);
		dendro_map.put(id, dendro_root);

		putDendroPropMap(dendroGE_left);
		putDendroPropMap(dendroGE_right);
		putDendroPropMap(dendroGE_bridge);
	    }
	    curData = "";
	}

	private void putDendroPropMap(DendrogramGraphElement dendroGE) {
	    DendrogramNode node = dendroGE.getDendrogramNode();
	    String id = node.getID();
	    if (dendroGE.isBridge())
		id += "::bridge";
	    dendro_prop_map.put(id, dendroGE);
	}

	private GraphElement getDendroGraphElement(String id) {
	    for (int n = 0; n < panel_num; n++) {
		GraphElement graphElement = getDendroGraphElement(panelGraphElements[n], id);
		if (graphElement != null) return graphElement;
	    }
	    return null;
	}

	private GraphElement getDendroGraphElement(LinkedList list, String id) {
	    int size = list.size();
	    for (int n = 0; n < size; n++) {
		GraphElement graphElement = (GraphElement)list.get(n);
		if (graphElement.getID().equals(id)) {
		    graphElement.setPropertyValue(Dendrogram.DendrogramOrderProp,
					     new Integer(n));
		    return graphElement;
		}
	    }

	    return null;
	}

	public void characters(char buf[], int offset, int len)
	    throws SAXException
	{
	    if (error != null)
		return;
	    String s = new String(buf, offset, len);
	    curData += s;
	}

	public void endElement(String namespaceURI,
			       String sName,
			       String qName
			       )
	    throws SAXException
	{
	    if (error != null)
		return;
	    if (verbose)
		System.out.println("</" + qName + ">");

	    if (qName.equals("CGHConfig")) {
		configState = false;
		annot_ctx = new Resources.AnnotContext();
		return;
	    }

	    if (qName.equals("Error")) {
		error = new String(curData);
		return;
	    }

	    if (configState) {
		if (!Resources.endAnnots(null, qName, annot_ctx))
		    VAMPResources.resources.add(qName, curData);
		return;
	    }

	    // TBD: local annotations
	    /*
	    if (Resources.endAnnots(view, qName, annot_ctx))
		return;
	    */

	    curData = curData.trim();
	    if (verbose)
		System.out.print(curData);
	    
	    if (qName.equals("DataElement")) {
		lastElem = null;
	    }
	    else if (qName.equals("ChromosomeMerge")) {
		chrMergeGraphElements();
	    }
	    else if (qName.equals("TranscriptomeChromosomeMerge")) {
		trsChrMergeGraphElements();
	    }
	    else if (qName.equals("FrAGL")) {
		frAGLMergeGraphElements();
	    }
	    else if (qName.equals("GTCA")) {
		gtcaMergeGraphElements();
	    }
	    else if (qName.equals("BreakpointFrequency")) {
		bkpFreqMergeGraphElements();
	    }
	    else if (qName.equals("SyntenySwitch")) {
		syntenySwitchGraphElements();
	    }
	    else if (qName.equals("Synteny")) {
		syntenyGraphElements();
	    }
	    else if (qName.equals("RegionSynteny")) {
		regionSyntenyGraphElements();
	    }
	    else if (qName.equals("TranscriptomeAverage")) {
		transAverageGraphElements();
	    }
	    else if (qName.equals("Average")) {
		averageGraphElements();
	    }
	    else if (qName.equals("ArrayMerge")) {
		arrayMergeGraphElements();
	    }
	    else if (qName.equals("TranscriptomeMerge")) {
		transMergeGraphElements();
	    }
	    else if (qName.equals("SNPChromosomeMerge")) {
		snpChrMergeGraphElements();
	    }
	    else if (qName.equals("TranscriptomeRelative")) {
		transRelGraphElements();
	    }
	    else if (qName.equals("ChipChipChromosomeMerge")) {
		chipChipChrMergeGraphElements();
	    }
	    else if (qName.equals("ChipChipAverage")) {
		chipChipAverageGraphElements();
	    }
	    else if (qName.equals("ChipChipArrayMerge")) {
		chipChipArrayMergeGraphElements();
	    }
	    else if (qName.equals("GenomeAnnotChromosomeMerge")) {
		genomeAnnotChrMergeGraphElements();
	    }
	    else if (qName.equals("ViewName"))
		viewName = curData;
	    else if (qName.equals("GraphElementDisplayer") ||
		     qName.equals("DataSetDisplayer")) {
		try {
		    graphElementDisplayer = (GraphElementDisplayer)
			GraphElementDisplayer.get(curData).clone();
		}
		catch(CloneNotSupportedException e) {
		}
	    }
	    else if (qName.equals("ShowBreakpoints")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.showBreakpoints(getBool(curData));
	    }
	    else if (qName.equals("ShowSmoothingLines")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.showSmoothingLines(getBool(curData));
	    }
	    else if (qName.equals("ShowSmoothingPoints")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.showSmoothingPoints(getBool(curData));
	    }
	    else if (qName.equals("ShowCentromere")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.showCentromere(getBool(curData));
	    }
	    else if (qName.equals("ShowOut")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.showOut(getBool(curData));
	    }
	    else if (qName.equals("ShowTag")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.showTag(getBool(curData));
	    }
	    else if (qName.equals("ShowTagString")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.showTagString(getBool(curData));
	    }
	    else if (qName.equals("ShowSize")) {
		showSize = getBool(curData);
	    }
	    else if (qName.equals("ShowInd")) {
		DataSet.SHOW_IND = getBool(curData);
	    }
	    else if (qName.equals("ShowId")) {
		DataSet.SHOW_ID = getBool(curData);
	    }
	    else if (qName.equals("GNLColorCodes")) {
		StandardDataSetDisplayer sds = getSDS();
		if (sds != null)
		    sds.setGNLColorCodes(getBool(curData));
	    }
	    else if (qName.equals("AxisDisplayer"))
		axisDisplayer = AxisDisplayer.get(curData);
	    else if (qName.equals("GraphElementIDBuilder") ||
		     qName.equals("DataSetIDBuilder"))
		graphElementIDBuilder = GraphElementIDBuilder.get(curData);
	    else if (qName.equals("AutoApplyDSLOP"))
		autoApplyDSLOP = GraphElementListOperation.get(curData);
	    else if (qName.equals("BiPanels")) {
		// version before 1.2.30
		boolean isBiPanels = getBool(curData);
		makePanels(isBiPanels ? 2 : 1);
	    }
	    else if (qName.equals("BackgroundImage"))
		bgImg = curData;
	    else if (qName.equals("PanelCount"))
		makePanels(getInt(curData));
	    else if (qName.equals("HasX"))
		hasX = getBool(curData);
	    else if (qName.equals("Locked"))
		isLocked[panel_num] = getBool(curData);
	    else if (qName.equals("Disabled"))
		isDisabled[panel_num] = getBool(curData);
	    else if (qName.equals("Xmin"))
		XSlide_min = getInt(curData);
	    else if (qName.equals("Xmax"))
		XSlide_max = getInt(curData);
	    else if (qName.equals("Ymin"))
		YSlide_min = getInt(curData);
	    else if (qName.equals("Ymax"))
		YSlide_max = getInt(curData);
	    else if (qName.equals("PanelName"))
		panel_name = curData;
	    else if (qName.equals("MarginNorth"))
		marginNorth = getInt(curData);
	    else if (qName.equals("MarginEast"))
		marginEast = getInt(curData);
	    else if (qName.equals("MarginWest"))
		marginWest = getInt(curData);
	    else if (qName.equals("MarginSouth"))
		marginSouth = getInt(curData);
	    else if (qName.equals("Width"))
		width = getInt(curData);
	    else if (qName.equals("Height"))
		height = getInt(curData);
	    else if (qName.equals("ScrollMask"))
		scrollMask = getInt(curData);
	    else if (qName.equals("AxisMask"))
		axisMask = true;
	    else if (qName.equals("AxisNorth"))
		axisNorth = getInt(curData);
	    else if (qName.equals("AxisSouth"))
		axisSouth = getInt(curData);
	    else if (qName.equals("AxisWest"))
		axisWest = getInt(curData);
	    else if (qName.equals("AxisEast"))
		axisEast = getInt(curData);

	    else if (qName.equals("Version"))
		version_str = curData;
	    else if (qName.equals("FitInPage") ||
		     qName.equals("FitInPage"))
		fitInPage = getBool(curData);
	    else if (qName.equals("ScaleX"))
		scaleX[panel_num] = getDouble(curData);
	    else if (qName.equals("ScaleY"))
		scaleY[panel_num] = getDouble(curData);
	    else if (qName.equals("DividerLocation"))
		dividers[panel_num] = getInt(curData);
	    else if (qName.equals("PanelFinalLayout"))
		layoutStack.push(new PanelFinalLayout
			    (Integer.parseInt(curData)));
	    else if (qName.equals("PanelSplitLayout")) {
		PanelLayout second = (PanelLayout)layoutStack.pop();
		PanelLayout first = (PanelLayout)layoutStack.pop();
		layoutStack.push(new PanelSplitLayout
				 (((Integer)layoutOrientationStack.pop()).
				  intValue(),
				  first, second));
	    }
	    else if (qName.equals("PanelSplitLayoutOrientation")) {
		layoutOrientationStack.push(new Integer(Integer.parseInt(curData)));
	    }
	    else if (qName.equals("PanelLinksName"))
		linksName = curData;
	    else if (qName.equals("PanelLinksSynchro"))
		linksSynchro = Integer.parseInt(curData);
	    else if (qName.equals("PanelLinksInd")) {
		panelLinks.add(new PanelLinks(linksName,
					      linksSynchro,
					      makeLinksInd(curData)));
	    }
	    else if (qName.equals("Panel")) {
		panelProfiles[panel_num] =
		    new PanelProfile(panel_name,
				     new int[]{axisNorth,
					       axisSouth,
					       axisWest,
					       axisEast},
				     scrollMask,
				     graphElementDisplayer,
				     axisDisplayer,
				     graphElementIDBuilder,
				     yaxisAutoAdapt,
				     autoApplyDSLOP,
				     new ZoomTemplate(XSlide_min, XSlide_max, 
						      YSlide_min, YSlide_max),
				     null,
				     (isDisabled[panel_num] ? PanelProfile.DISABLED : 0),
				     hasX,
				     new Margins(marginNorth,
						 marginWest,
						 marginSouth,
						 marginEast),
				     bgImg);
		bgImg = null;
		panel_num++;
	    }
	    else if (qName.equals("Dendrogram")) {
		dendro = new Dendrogram(dendro_root,
					(dendro_type.equals("X") ?
					 Dendrogram.X_TYPE :
					 Dendrogram.Y_TYPE));

		int sz = dendroGraphElementList.size();
		for (int n = 0; n < sz; n++)
		    ((DendrogramGraphElement)dendroGraphElementList.get(n)).
			compile();
		//System.out.println(dendro);
	    }
	}

	private void loadTrans(String url, Attributes attrs) {
	    TranscriptomeOP op =
		(TranscriptomeOP)GraphElementListOperation.get
		(isLightImported(attrs) ? TranscriptomeOP.LIGHT_NAME : TranscriptomeOP.NAME);

	    TreeMap params = new TreeMap();
	    params.put("URL", url);
	    LinkedList l0 = getArrayData(attrs.getValue("ARRAY_URL"));
	    if (l0 == null) {
		l0 = trsFactory.buildDataSets(url, null, false,
					      !isLightImported(attrs));
		addGraphElements_r(l0);
		return;
	    }

	    Vector v = op.apply(globalContext,
				Utils.listToVector(l0),
				params, false);
	    if (v == null) return;
	    op.setFilter(null);
	    LinkedList l = new LinkedList();
	    // #0 is the reference, #1 is the transcriptome
	    GraphElement trans = (GraphElement)v.get(1);
	    l.add(trans);
	    addGraphElements_r(l);
	}

	private void loadLOH(String url, Attributes attrs) {
	    LOHLoadOP op =
		(LOHLoadOP)GraphElementListOperation.get(LOHLoadOP.NAME);

	    TreeMap params = new TreeMap();
	    params.put("URL", url);
	    LinkedList l0 = getArrayData(attrs.getValue("ARRAY_URL"));
	    if (l0 == null) {/*error = "Error #2"; */ return;}
	    Vector v = op.apply(globalContext,
				Utils.listToVector(l0),
				params, false);
	    if (v == null) return;
	    LinkedList l = new LinkedList();
	    // #0 is the reference, #1 is the LOH
	    GraphElement loh = (GraphElement)v.get(1);
	    l.add(loh);
	    addGraphElements_r(l);
	}

	private HashMap extraProperties() {
	    return extraPropertyStack.size() > 0 ?
		(HashMap)extraPropertyStack.peek() : null;
	}

	private void addGraphElements(LinkedList graphElements) {
	    panelGraphElements[panel_num].addAll(graphElements);
	}

	private LinkedList opGraphElements() {
	    return (LinkedList)opGraphElementStack.peek();
	}

	private void addGraphElements_r(LinkedList graphElements) {
	    HashMap extraProperties = extraProperties();
	    if (extraProperties != null) {
		int size = graphElements.size();
		for (int n = 0; n < size; n++)
		    setExtraProperties((GraphElement)graphElements.get(n),
				       extraProperties);
	    }

	    setColorCodes(graphElements, (HashMap)attrsStack.peek());
	    setThresholds(graphElements, (HashMap)attrsStack.peek());

	    if (opGraphElementStack.size() > 0)
		opGraphElements().addAll(graphElements);
	    else
		addGraphElements(graphElements);

	    lastGraphElementList = graphElements;
	}

	private boolean isLightImported(Attributes attrs) {
	    String value = attrs.getValue("LightImported");
	    if (value != null && value.equalsIgnoreCase("true"))
		return true;
	    return false;
	}

	private int getImportMode(Attributes attrs) {
	    String value = attrs.getValue("mode");
	    if (value == null) {
		return ImportData.XML_IMPORT;
	    }
	    if (value.equalsIgnoreCase("serial+optim")) {
		return ImportData.SERIAL_IMPORT|ImportData.GRAPHIC_OPTIM_IMPORT;
	    }
	    return ImportData.SERIAL_IMPORT;
	}

	private boolean isPangen(Attributes attrs) {
	    String value = attrs.getValue("chr");
	    if (value != null) {
		return ImportDataItem.isPangen(value);
	    }
	    return true;
	}

	private String [] getChrList(Attributes attrs) {
	    String value = attrs.getValue("chr");
	    if (value == null) {
		return null;
	    }

	    return ImportDataItem.getChrList(isPangen(attrs), value);
	}

	private Vector<String> getParents(Attributes attrs) {
	    String value = attrs.getValue("parent_dir");
	    if (value == null) {
		return new Vector<String>();
	    }
	    String parent_arr[] = value.split("/");
	    Vector<String> parents = new Vector();
	    for (int n = parent_arr.length-1; n >= 0; n--) {
		parents.add(parent_arr[n]);
	    }
	    return parents;
	}

	private void loadGraphElements(String url, Attributes attrs) {
	    LinkedList graphElements = getArrayData(url, isLightImported(attrs), getImportMode(attrs), getParents(attrs), getChrList(attrs), isPangen(attrs));
	    if (graphElements == null) {error = "Error #3"; return;}
	    String srcUrl = attrs.getValue("SrcURL");
	    if (srcUrl != null && srcUrl.length() > 0) {
		//System.out.println("srcUrl: " + srcUrl);
		int sz = graphElements.size();
		for (int n = 0; n < sz; n++)
		    ((GraphElement)graphElements.get(n)).setSourceURL(srcUrl);
	    }

	    addGraphElements_r(graphElements);
	}

	LinkedList getArrayData(String url) {
	    return getArrayData(url, false, ImportData.XML_IMPORT, new Vector<String>(), null, true);
	}

	LinkedList getArrayData(String url, boolean light_import, int import_mode, Vector<String> parents, String chrList[], boolean pangen) {
	    if (url == null) {
		return null;
	    }

	    if ((import_mode & ImportData.SERIAL_IMPORT) != 0) {
		return ImportData.importSerial(globalContext, null, null, import_mode, "", url, parents, chrList, pangen, false, !light_import);
	    }

	    return array_factory.getData(url, false, !light_import);
	}

	LinkedList[] getGraphElements() {
	    return panelGraphElements;
	}

	private void setColorCodes(LinkedList graphElements, HashMap map) {
	    if (map.get("cc_min") == null) return;

	    // backward compatiblity:
	    if (map.get("cc_amplicon") == null)
		map.put("cc_amplicon", map.get("cc_max"));
	    if (map.get("cc_ampliconRGB") == null)
		map.put("cc_ampliconRGB", map.get("cc_maxRGB"));
	    // end of backward compatibility

	    Color min_fg = new Color(getRGB((String)map.get("cc_minRGB")));
	    Color normal_fg = new Color(getRGB((String)map.get("cc_normalRGB")));
	    Color max_fg = new Color(getRGB((String)map.get("cc_maxRGB")));
	    Color amplicon_fg = new Color(getRGB((String)map.get("cc_ampliconRGB")));

	    StandardColorCodes ccLog =
		new StandardColorCodes
		(globalContext,
		 true,
		 "",
		 "",
		 Utils.log(getDouble((String)map.get("cc_min"))),
		 Utils.log(getDouble((String)map.get("cc_normal_min"))),
		 Utils.log(getDouble((String)map.get("cc_normal_max"))),
		 Utils.log(getDouble((String)map.get("cc_max"))),
		 Utils.log(getDouble((String)map.get("cc_amplicon"))),
		 VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		 min_fg, normal_fg, max_fg, amplicon_fg,
		 getBool((String)map.get("cc_continuous")));

	    StandardColorCodes ccLin =
		new StandardColorCodes
		(globalContext,
		 false,
		 "",
		 "",
		 getDouble((String)map.get("cc_min")),
		 getDouble((String)map.get("cc_normal_min")),
		 getDouble((String)map.get("cc_normal_max")),
		 getDouble((String)map.get("cc_max")),
		 getDouble((String)map.get("cc_amplicon")),
		 VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		 min_fg, normal_fg, max_fg, amplicon_fg,
		 getBool((String)map.get("cc_continuous")));

	    int size = graphElements.size();
	    for (int n = 0; n < size; n++)
		VAMPUtils.setLocalColorCodes((GraphElement)graphElements.get(n),
				       ccLog, ccLin);
	}

	private void setThresholds(LinkedList graphElements, HashMap map) {
	    if (map.get("thr_min") == null) return;

	    double min = getDouble((String)map.get("thr_min"));
	    double max = getDouble((String)map.get("thr_max"));
	    Thresholds thrLin = new Thresholds(false, "", false,
					       min, max);

	    Thresholds thrLog = new Thresholds(false, "", true,
					       Utils.log(min), Utils.log(max));

	    int size = graphElements.size();
	    for (int n = 0; n < size; n++)
		VAMPUtils.setLocalThresholds((GraphElement)graphElements.get(n),
				       thrLog, thrLin);
	}

	private void opGraphElements(GraphElementListOperation op) {
	    opGraphElements(op, null);
	}

	private void opGraphElements(GraphElementListOperation op,
				     TreeMap params) {
	    //System.out.println(op.getName() + ": " + opGraphElements().size());
	    if (params == null)
		params = new TreeMap();
	    params.put("GlobalContext", globalContext);
	    //Utils.display(params);
	    Vector v = op.apply(null, null,
				Utils.listToVector(opGraphElements()),
				params, false);
	    if (v == null) return;
	    opGraphElementStack.pop();
	    addGraphElements_r(Utils.vectorToList(v));
	    extraPropertyStack.pop();
	    attrsStack.pop();
	}

	private void chrMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
			    (MergeChrOP.CGH_NAME));
	}

	private void trsChrMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
			    (MergeChrOP.TRANSCRIPTOME_NAME));
	    /*
	    opGraphElements(GraphElementListOperation.get
			    (TranscriptomeOP.NAME));
	    */
	}

	private void frAGLMergeGraphElements() {
	    GraphElementListOperation op = GraphElementListOperation.get
		(FrAGLOP.NAME);
	    TreeMap params = op.makeParams((HashMap)attrsStack.peek());
	    opGraphElements(op, params);
	}

	private void gtcaMergeGraphElements() {
	    GraphElementListOperation op = GraphElementListOperation.get
		(GTCorrelationAnalysisOP.NAME);
	    TreeMap params = op.makeParams((HashMap)attrsStack.peek());
	    opGraphElements(op, params);
	}

	private void bkpFreqMergeGraphElements() {
	    GraphElementListOperation op = GraphElementListOperation.get
		(BreakpointFrequencyOP.NAME);
	    TreeMap params = op.makeParams((HashMap)attrsStack.peek());
	    opGraphElements(op, params);
	}

	private void syntenySwitchGraphElements() {
	    opGraphElements(GraphElementListOperation.get
			    (SyntenyOP.SWITCH_NAME));
	}

	private void syntenyGraphElements() {
	    TreeMap params = Utils.hashToTreeMap((HashMap)attrsStack.peek());
	    opGraphElements(GraphElementListOperation.get
			    (SyntenyOP.NAME), params);
	}

	private void regionSyntenyGraphElements() {
	    TreeMap params = Utils.hashToTreeMap((HashMap)attrsStack.peek());
	    opGraphElements(GraphElementListOperation.get
			    (SyntenyOP.REGION_NAME), params);
	}

	private void chipChipChrMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (MergeChrOP.CHIP_CHIP_NAME));
	}

	private void snpChrMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (MergeChrOP.SNP_NAME));
	}

	private void genomeAnnotChrMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (MergeChrOP.GENOME_ANNOT_NAME));
	}

	private void transAverageGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (TranscriptomeAverageOP.NAME));
	}

	private void averageGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (AverageOP.CGH_NAME));
	}

	private void chipChipAverageGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (AverageOP.CHIP_CHIP_NAME));
	}

	private void arrayMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (MergeArrayOP.CGH_NAME));
	}

	private void chipChipArrayMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (MergeArrayOP.CHIP_CHIP_NAME));
	}

	private void transMergeGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (MergeTranscriptomeOP.NAME));
	}

	private void transRelGraphElements() {
	    opGraphElements(GraphElementListOperation.get
		       (TranscriptomeRelOP.NO_ADD_REF_NAME));
	}

	public void error(SAXParseException e)
	    throws SAXException {
	    System.err.println("XMLLoadDataFactory: SAX Error at line #" + e.getLineNumber() + " " + e);
	}

	public void warning(SAXParseException e) {
	    System.err.println("XMLLoadDataFactory: SAX Warning at line #" + e.getLineNumber() + " " + e);
	}

	public void ignorableWhitespace(char[] ch, int start, int length) {
	}

	public void fatalError(SAXParseException e)
	    throws SAXException {
	    System.err.println("SAX Fatal Error at line #" + e.getLineNumber() + " " + e);
	}

	String getStringVersion() {
	    return version_str;
	}

	View makeView(GlobalContext globalContext) {
	    // orientation !
	    View view = new View(globalContext, viewName,
				 panelProfiles,
				 getPanelLayout(),
				 getPanelLinks(),
				 null, null,
				 new LinkedList(),
				 null,
				 new Dimension(width, height));

	    setData_r(view, false, null);

	    postAction(view);
	    return view;
	}

	void postAction(View view) {
	    GraphPanelSet panelSet = view.getGraphPanelSet();
	    showSize(view);
	    int panel_max = view.getPanelProfiles().length;

	    for (int n = 0; n < panel_max; n++) {
		if (n < panel_num && isLocked[n])
		    view.setReadOnly(n, true);
	    }

	    for (int n = 0; n < panel_max; n++) {
		int m = n < panel_num ? n : panel_num-1;
		update(view, panelSet.getPanel(n),
		       new Scale(scaleX[m], scaleY[m]));
		if (dividers[m] != 0) {
		    if (panelSet.getPanel(n).getSplitPane() != null) {
			panelSet.getPanel(n).getSplitPane().setDividerLocation(dividers[m]);
		    }
		}
	    }

	    if (hasConfig) {
		Thresholds.init(view.getGlobalContext());
		ColorCodes.init(view.getGlobalContext());
		View.syncAllGraphElements(view.getGlobalContext(), true);
		View.syncAll(view.getGlobalContext(), true);
	    }
	}

	ViewFrame makeViewFrame(GlobalContext globalContext, Task task) {

	    ViewFrame vf = new ViewFrame(globalContext,
					 viewName,
					 panelProfiles,
					 getPanelLayout(),
					 getPanelLinks(),
					 null, null,
					 new LinkedList(),
					 new Dimension(width, height),
					 null);

	    setData_r(vf.getView(), false, null);
	    if (task != null)
		task.performBeforeOPFrameVisible();
	    vf.setVisible(true); // very important !!
	    postAction(vf.getView());
	    return vf;
	}

	private void setData_r(View view, boolean replace, RemoteOP op) {
	    GraphPanelSet panelSet = view.getGraphPanelSet();
	    int panel_max = view.getPanelProfiles().length;

	    // hack for BW compatibility
	    if (panel_num == 0)
		panel_num = 1;

	    /*
	    if (op != null)
		op.postAction();
	    */

	    for (int n = 0; n < panel_num; n++) {
		LinkedList l = new LinkedList();
		if (!replace)
		    l.addAll(panelSet.getGraphElements(n));
		l.addAll(panelGraphElements[n]);
		int m = n < panel_max ? n : panel_max-1;
		panelSet.setGraphElements(l, m);

		if (normalize) {
		    l = panelSet.getGraphElements(m);
		    Vector v = NormalizeOP.normalize(view.getGlobalContext(), Utils.listToVector(l));
		    panelSet.setGraphElements(Utils.vectorToList(v), m);
		}
	    }

	    // TBD
	    panelSet.setRegions(0, regions, !replace);
	    panelSet.setMarks(0, marks, !replace);
	    /*
	    int sz = marks.size();
	    for (int n = 0; n < sz; n++) {
		Mark mark = (Mark)marks.get(n);
		System.out.print("Landmark " + (long)mark.getPosX());
		if (mark.getRegion() == null)
		    System.out.print(" is");
		else
		    System.out.print(" is *not*");
		System.out.println(" orphan");
	    }
	    */

	    if (op != null)
		op.postAction();
	}

	boolean setData(View view, boolean replace, RemoteOP op) {
	    setData_r(view, replace, op);

	    postAction(view);
	    return true;
	}

	void showSize(View view) {
	    StandardDataSetDisplayer sds = getSDS();
	    if (sds == null) return;

	    sds.showSize(showSize);
	    if (showSize)
		view.applyOnGraphElements
		    (DataSetSizePerformer.getSetSizePerformer());
	    else
		view.applyOnGraphElements
		    (DataSetSizePerformer.getUnsetSizePerformer());
	}

	void update(View view, GraphPanel panel, Scale scale) {
	    boolean o_fitInPage = panel.isFitInPage();
	    panel.setFitInPage(true);
	    if (!fitInPage && !o_fitInPage) {
		panel.setFitInPage(false);
		view.getZoomPanel().setScale(scale);
	    }
	    panel.getCanvas().readaptSize();
	}

	void display() {
	    System.out.println(
			       "viewName: " + viewName + ", " +
			       "graphElementDisplayer: " + graphElementDisplayer + ", " +
			       "axisDisplayer: " + axisDisplayer + ", " +
			       //"isBiPanels: " + isBiPanels + ", " +
			       "hasX: " + hasX + ", " +
			       "XSlide_min: " + XSlide_min + ", " +
			       "XSlide_max: " + XSlide_max + ", " +
			       "YSlide_min: " + YSlide_min + ", " +
			       "YSlide_max: " + YSlide_max + ", " +
			       "width: " + width + ", " +
			       "height: " + height);
	    /*
	    display("Top", topGraphElements);
	    if (isBiPanels)
		display("Bottom", bottomGraphElements);
	    */
	    display("Landmarks", marks);
	    display("Regions", regions);
	}

	void display(String name, LinkedList list) {
	    int size = list.size();
	    System.out.println("List " + name + ": " + size + " elements");
	    for (int i = 0; i < size; i++)
		System.out.println(list.get(i));
	}

	boolean hasErrors() {return error != null;}
	String getError() {return error;}

	StandardDataSetDisplayer getSDS() {
	    if (graphElementDisplayer != null &&
		graphElementDisplayer instanceof StandardDataSetDisplayer)
		return (StandardDataSetDisplayer)graphElementDisplayer;
	    return null;
	}
	
	void makePanels(int count) {
	    panelProfiles = new PanelProfile[count];
	    scaleX = new double[count];
	    scaleY = new double[count];
	    dividers = new int[count];
	    isLocked = new boolean[count];
	    isDisabled = new boolean[count];
	    panelGraphElements = new LinkedList[count];
	    for (int n = 0; n < count; n++) {
		panelGraphElements[n] = new LinkedList();
		isLocked[n] = false;
	    }
	}

	PanelLinks[] getPanelLinks() {
	    if (panelLinks.size() == 0)
		return null;
	    PanelLinks pLinks[] = new PanelLinks[panelLinks.size()];
	    Object arr[] = panelLinks.toArray();
	    for (int n = 0; n < arr.length; n++)
		pLinks[n] = (PanelLinks)arr[n];

	    return pLinks;
	}

	PanelLayout getPanelLayout() {
	    if (layoutStack.size() == 1)
		return (PanelLayout)layoutStack.pop();
	    return null;
	}

	int[] makeLinksInd(String s) {
	    String arr[] = s.split(" ");
	    int ind[] = new int[arr.length];
	    for (int n = 0; n < ind.length; n++)
		ind[n] = Integer.parseInt(arr[n]);
	    return ind;
	}
    }

    static boolean getBool(String s) {
	return s.equalsIgnoreCase("true") ? true : false;
    }

    static int getInt(String s) {
	return Utils.parseInt(s);
    }

    static int getRGB(String s) {
	return Utils.parseInt(s, 16);
    }

    static long getLong(String s) {
	return Long.parseLong(s);
    }

    static double getDouble(String s) {
	return Utils.parseDouble(s);
    }

    static Mark getMark(LinkedList marks, String position, String xid_s) {
	int xid = 0;
	if (xid_s != null)
	    xid = Integer.parseInt(xid_s);

	int size = marks.size();
	for (int i = 0; i < size; i++) {
	    Mark mark = (Mark)marks.get(i);
	    if (xid != 0) {
		if (mark.getXID() == xid)
		    return mark;
	    }
	    else if (XMLSaveDataProducer.toString(mark).equals(position))
		return mark;
	}
	return null;
    }

    static Mark makeMark(String position, String sid,
			 GraphElement graphElement) {
	int idx = position.indexOf(':');
	if (idx >= 0) {
	    // backward compatibility
	    String positions[] = position.split(":");
	    DataElementRange range =
		new DataElementRange(graphElement,
				     getInt(positions[0]),
				     getInt(positions[1]),
				     getDouble(positions[2]));
	     return new Mark(range.computePosX());
	}

	Mark mark = new Mark(getDouble(position));
	if (sid != null)
	    mark.setXID(Integer.parseInt(sid));
	return mark;
    }

    static private void updateViewTitle(View view, String url) {
	/*
	if (url != null)
	    view.setOrigName(view.getViewName() +
			     " [" + url + "]");
	*/
	view.getGraphPanelSet().setTopTitle();
    }

    static final Property properties[] = {
	VAMPProperties.ReferenceProp
    };

    HashMap getExtraProperties(String qName, Attributes attrs)
	throws SAXException {
	if (attrs == null)
	    return null;

	int len = attrs.getLength();
	HashMap map = null;
	for (int i = 0; i < len; i++) {
	    String aName = attrs.getQName(i);
	    for (int n = 0; n < properties.length; n++) {
		if (aName.equals(properties[n].getName())) {
		    if (map == null)
			map = new HashMap();
		    map.put(properties[n].getName(), attrs.getValue(i));
		    break;
		}		
	    }
	}

	return map;
    }

    void setExtraProperties(GraphElement graphElement, HashMap extraProperties) {
	if (extraProperties == null)
	    return;
	/*
	System.out.println("should set properties " + extraProperties.size()
			   + " to " + graphElement.getID());
	*/

	Iterator it = extraProperties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    String prop = (String)entry.getKey();
	    //System.out.println("setting " + prop + " to " + entry.getValue());
	    graphElement.setPropertyValue(Property.getProperty(prop), 
				     entry.getValue());
	}

    }

    private HashMap makeMap(Attributes attrs) {
	HashMap map = new HashMap();
	int len = (attrs == null ? 0 : attrs.getLength());
	for (int n = 0; n < len; n++) {
	    String qName = attrs.getQName(n);
	    String value = attrs.getValue(n);
	    map.put(qName, value);
	}

	return map;
    }

    // used by VAMPApplet (type==LOAD)
    /*
    View makeView(GlobalContext globalContext, File file) {
	try {
	    FileInputStream is = new FileInputStream(file);
	    parse(is);

	    if (handler.getError() != null) {
		InfoDialog.pop(globalContext, "Error reported: " +
			       handler.getError());
		return null;
	    }

	    View view = handler.makeView(globalContext);
	    updateViewTitle(view, file);
	    return view;
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, "XMLLoadDataFactory: ", e);
	    return null;
        }
    }
    */
}
