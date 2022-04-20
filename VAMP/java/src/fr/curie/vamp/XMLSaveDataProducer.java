
/*
 *
 * XMLSaveDataProducer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.awt.*;
import java.util.*;

import fr.curie.vamp.data.Profile;

class XMLSaveDataProducer {

    static final String NEWLINE = "_NEWLINE_";

    private static final boolean NEW_CHR_MERGE_IMPL = true;

    private String getAppletTemplateBegin(GlobalContext globalContext) {
	return "<html><head><title>Interface CGH</title></head>\n" +
	    "  <body>\n" +
	    "    <h1>Interface CGH</h1>\n" + 
	    "    <hr>\n" +
	    "    <applet codebase=\"" + globalContext.getCodeBase() + "\" code=\"VAMPApplet\" archive=\"VAMPApplet.jar\" width=\"400\" height=\"700\">\n" +
	    "      alt=\"Your browser understands the &lt;APPLET&gt; tag but isnt running the applet, for some reason.\"\n" +
	    "      Your browser is completely ignoring the &lt;APPLET&gt; tag!\n" +
	    "       <param name=\"vtype\" value=\"LOAD\">\n";
    }

    String getAppletTemplateEnd(GlobalContext globalContext) {
	return "    </applet>\n" +
	    "  </body></html>\n";
    }

    boolean save(GlobalContext globalContext, File file, View view) {
	try {
	    if (view != null)
		return process(globalContext, file, view);

	    String canName = Utils.suppressExtension(file.getAbsolutePath());
	    LinkedList list = View.getViewList(globalContext);
	    int size = list.size();
	    FileOutputStream os = new FileOutputStream(file);
	    PrintStream ps = new PrintStream(os);
	    XMLUtils.printComment(ps, file);
	    ps.print(getAppletTemplateBegin(globalContext));
	    ps.print("     <param name=\"viewCount\" value=\"" + XMLUtils.toString(list.size()) + "\">\n");
	    for (int n = 0; n < size; n++) {
		view = (View)list.get(n);
		File vfile = new File(canName + "_" + XMLUtils.toString(n+1) + ".xml");
		if (!process(globalContext, vfile, view))
		    return false;

		ps.print("     <param name=\"view_" + XMLUtils.toString(n) +
			 "\" value=\"" + vfile.getAbsolutePath() + "\">\n");
	    }
	    
	    ps.print(getAppletTemplateEnd(globalContext));
	    ps.close();
	    return true;
	}
	catch(FileNotFoundException e) {
	    InfoDialog.pop(globalContext, "Error: file " + e.getMessage());
	}
	return false;
    }

    boolean process(GlobalContext globalContext, File file, View view) throws
	FileNotFoundException {
	//System.out.println("Saving " + view.getViewName());
	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);
	GraphPanelSet gPanel = view.getGraphPanelSet();

	XMLUtils.printHeader(ps);
	XMLUtils.printComment(ps, file);
	XMLUtils.printOpenTag(ps, "View");

	// added 14/09/04: should be controled by a parameter ?
	VAMPResources.writeParameters(globalContext, ps);

	XMLUtils.printTag(ps, "Version", VersionManager.getStringVersion());
	//	XMLUtils.printTag(ps, "ViewName", view.getViewName());
	XMLUtils.printTag(ps, "ViewName", view.getName());

	Dimension size = view.getSize();
	XMLUtils.printTag(ps, "Width", XMLUtils.toString(size.width));
	XMLUtils.printTag(ps, "Height", XMLUtils.toString(size.height));
	XMLUtils.printTag(ps, "FitInPage", XMLUtils.toString(view.getPanel(0).isFitInPage()));
	int panelCount = gPanel.getPanelCount();
	XMLUtils.printTag(ps, "PanelCount", XMLUtils.toString(panelCount));
	
	printPanelLayout(ps, gPanel);
	printPanelLinks(ps, gPanel);

	for (int n = 0; n < panelCount; n++) {
	    GraphPanel panel = gPanel.getPanel(n);
	    XMLUtils.printOpenTag(ps, "Panel");
	    /*
	      Dimension panelSize = panel.getSize();
	      XMLUtils.printTag(ps, "PanelWidth", XMLUtils.toString(panelSize.width));
	      XMLUtils.printTag(ps, "PanelHeight", XMLUtils.toString(panelSize.height));
	    */
	    if (panel.getSplitPane() != null)
		XMLUtils.printTag(ps, "DividerLocation", XMLUtils.toString(panel.getSplitPane().getDividerLocation()));

	    printGraphElementDisplayer(ps, panel);

	    if (panel.getDefaultAxisDisplayer() != null)
		XMLUtils.printTag(ps, "AxisDisplayer",
				  panel.getDefaultAxisDisplayer().getName());
	    else {
		LinkedList list = panel.getCanvas().getGraphElements();
		if (list.size() > 0 ) {
		    GraphElement graphElement = (GraphElement)list.get(0);
		    if (graphElement.getAxisDisplayer() != null) {
			XMLUtils.printTag(ps, "AxisDisplayer",
					  graphElement.getAxisDisplayer().getName());
		    }
		}
	    }

	    if (panel.getAutoApplyDSLOP() != null)
		XMLUtils.printTag(ps, "AutoApplyDSLOP",
				  panel.getAutoApplyDSLOP().getName());

	    
	    if (panel.getGraphElementIDBuilder() != null)
		XMLUtils.printTag(ps, "GraphElementIDBuilder",
				  panel.getGraphElementIDBuilder().getName());
	
	    XMLUtils.printTag(ps, "HasX", XMLUtils.toString(panel.supportX()));

	    XMLUtils.printTag(ps, "Locked", XMLUtils.toString(panel.isReadOnly()));
	    String bgImg = panel.getPanelProfile().getBGImg();
	    if (bgImg != null)
		XMLUtils.printTag(ps, "BackgroundImage", bgImg);

	    if (panel.getPanelProfile().isDisabled())
		XMLUtils.printTag(ps, "Disabled", XMLUtils.toString(true));

	    int axisSizes[] = panel.getAxisSizes();
	    if (axisSizes != null) {
		XMLUtils.printTag(ps, "AxisNorth",
				  XMLUtils.toString(axisSizes[GraphPanel.NORTH_X]));
		XMLUtils.printTag(ps, "AxisSouth",
				  XMLUtils.toString(axisSizes[GraphPanel.SOUTH_X]));
		XMLUtils.printTag(ps, "AxisWest",
				  XMLUtils.toString(axisSizes[GraphPanel.WEST_Y]));
		XMLUtils.printTag(ps, "AxisEast",
				  XMLUtils.toString(axisSizes[GraphPanel.EAST_Y]));
	    }
		
	    XMLUtils.printOpenTag(ps, "Zoom", true);
	    ZoomTemplate zoomTemplate = panel.getPanelProfile().getZoomTemplate();
	    XMLUtils.printTag(ps, "Xmin", XMLUtils.toString(zoomTemplate.getXSlideMin()));
	    XMLUtils.printTag(ps, "Xmax", XMLUtils.toString(zoomTemplate.getXSlideMax()));
	    XMLUtils.printTag(ps, "Ymin", XMLUtils.toString(zoomTemplate.getYSlideMin()));
	    XMLUtils.printTag(ps, "Ymax", XMLUtils.toString(zoomTemplate.getYSlideMax()));
	    XMLUtils.printCloseTag(ps, "Zoom", true);

	    XMLUtils.printTag(ps, "ScrollMask",
			      Utils.toString(panel.getPanelProfile().getScrollMask()));

	    XMLUtils.printOpenTag(ps, "Margins", true);
	    Margins margins = panel.getPanelProfile().getMargins();
	    XMLUtils.printTag(ps, "MarginNorth",
			      XMLUtils.toString(margins.getMarginNorth()));
	    XMLUtils.printTag(ps, "MarginWest",
			      XMLUtils.toString(margins.getMarginWest()));
	    XMLUtils.printTag(ps, "MarginEast",
			      XMLUtils.toString(margins.getMarginEast()));
	    XMLUtils.printTag(ps, "MarginSouth",
			      XMLUtils.toString(margins.getMarginSouth()));
	    XMLUtils.printCloseTag(ps, "Margins", true);

	    if (!printPanel(globalContext, ps, panel,
			    mustPrintMarksAndRegions(gPanel, n))) {
		ps.close();
		file.delete();
		return false;
	    }

	    XMLUtils.printCloseTag(ps, "Panel");
	}

	if (!view.isAnnotGlobal())
	    Resources.savePropertyAnnotations(globalContext, view, ps);

	XMLUtils.printCloseTag(ps, "View");
	ps.close();
	return true;
    }

    void printGraphElementDisplayer(PrintStream ps, GraphPanel panel) {
	GraphElementDisplayer graphElementDisplayer =
	    panel.getDefaultGraphElementDisplayer();
	if (graphElementDisplayer == null) return;
	XMLUtils.printTag(ps, "GraphElementDisplayer",
			  graphElementDisplayer.getName());
	if (!(graphElementDisplayer instanceof CommonDataSetDisplayer)) return;

	CommonDataSetDisplayer cds =
	    (CommonDataSetDisplayer)graphElementDisplayer;

	XMLUtils.printTag(ps, "GNLColorCodes",
			  XMLUtils.toString(cds.isGNLColorCodes()));

	if (!(graphElementDisplayer instanceof StandardDataSetDisplayer)) return;

	StandardDataSetDisplayer sds =
	    (StandardDataSetDisplayer)graphElementDisplayer;

	XMLUtils.printTag(ps, "ShowBreakpoints",
			  XMLUtils.toString(sds.showBreakpoints()));
	XMLUtils.printTag(ps, "ShowSmoothingLines",
			  XMLUtils.toString(sds.showSmoothingLines()));
	XMLUtils.printTag(ps, "ShowSmoothingPoints",
			  XMLUtils.toString(sds.showSmoothingPoints()));
	XMLUtils.printTag(ps, "ShowCentromere",
			  XMLUtils.toString(sds.showCentromere()));
	XMLUtils.printTag(ps, "ShowOut",
			  XMLUtils.toString(sds.showOut()));
	XMLUtils.printTag(ps, "ShowSize",
			  XMLUtils.toString(sds.showSize()));
	XMLUtils.printTag(ps, "ShowTag",
			  XMLUtils.toString(sds.showTag()));
	XMLUtils.printTag(ps, "ShowTagString",
			  XMLUtils.toString(sds.showTagString()));
    }

    String getGraphElementColorCodes(GlobalContext globalContext, GraphElement graphElement) {
	StandardColorCodes scc = (StandardColorCodes)
	    graphElement.getPropertyValue(VAMPProperties.CCLinProp);
	if (scc == null) return "";

	return
	    " cc_min=\"" + XMLUtils.toString(scc.getMin()) + "\"" +
	    " cc_normal_min=\"" + XMLUtils.toString(scc.getNormalMin()) +
	    "\"" +
	    " cc_normal_max=\"" + XMLUtils.toString(scc.getNormalMax()) +
	    "\"" +
	    " cc_max=\"" + XMLUtils.toString(scc.getMax()) + "\"" +
	    " cc_amplicon=\"" + XMLUtils.toString(scc.getAmplicon()) + "\"" +
	    " cc_minRGB=\"" + XMLUtils.RGBtoString(scc.getMinRGB()) + "\"" +
	    " cc_normalRGB=\"" + XMLUtils.RGBtoString(scc.getNormalRGB()) +
	    "\"" +
	    " cc_maxRGB=\"" + XMLUtils.RGBtoString(scc.getMaxRGB()) + "\"" +
	    " cc_ampliconRGB=\"" + XMLUtils.RGBtoString(scc.getAmpliconRGB()) + "\"" +
	    " cc_continuous=\"" + XMLUtils.toString(scc.isContinuous()) +
	    "\"";
    }

    String getGraphElementThresholds(GlobalContext globalContext, GraphElement graphElement) {
	Thresholds thresholds = (Thresholds)
	    graphElement.getPropertyValue(VAMPProperties.ThresholdsLinProp);
	if (thresholds == null) return "";

	return
	    " thr_min=\"" + XMLUtils.toString(thresholds.getMin()) + "\"" +
	    " thr_max=\"" + XMLUtils.toString(thresholds.getMax()) + "\"";
    }

    boolean printGraphElement(GlobalContext globalContext, PrintStream ps,
			      GraphPanel panel, GraphElement graphElement,
			      String tag, String extra, String outer_tag) {
	if (graphElement.getURL() == null) {
	    InfoDialog.pop(globalContext,
			   "Error: cannot save the current data: " +
			   "URL is missing for data set " + graphElement.getID());
	    return false;
	}

	HashMap url_map = (HashMap)graphElement.getPropertyValue(VAMPProperties.URLMapProp);

	if (outer_tag != null)
	    ps.println("<" + outer_tag + ">");

	ps.print("<" + tag + (extra != null ? extra : "") +
		 " ID=\"" + graphElement.getID() + "\" URL=\"" +
		 graphElement.getURL() + "\" SrcURL=\"" +
		 graphElement.getSourceURL() + "\"");

	if (graphElement.asProfile() != null) {
	    Profile profile = graphElement.asProfile();
	    if (profile.getGraphicProfile().getGraphicInfo() != null) {
		ps.print(" mode=\"serial+optim\"");
	    }
	    else {
		ps.print(" mode=\"serial\"");
	    }

	    
	    printChromosomes(ps, profile);

	    ps.print(" parent_dir=\"" + graphElement.getPropertyValue(ImportData.parentDirProp) + "\"");
	}

	if (!graphElement.isFullImported()) {
	    ps.print(" LightImported=\"true\"");
	}

	ps.println(">");
	printModifiedProperties(globalContext, ps, graphElement);
	ps.println("</" + tag + ">");

	if (outer_tag != null)
	    ps.println("</" + outer_tag + ">");

	return true;
    }

    void printChromosomes(PrintStream ps, Profile profile) {
	String chr_s;

	if (profile.isWholePanGen()) {
	    chr_s = ImportDataItem.PANGEN_STR;
	}
	else if (profile.isChr()) {
	    chr_s = (profile.getChrNum() + 1) + "";
	}
	else {
	    int chr_cnt = profile.getChrCount();
	    chr_s = ImportDataItem.PANGEN_STR + ":";
	    boolean chrNums[] = profile.getChrNums();
	    int chr_n = 0;
	    for (int n = 0; n < chrNums.length; n++) {
		if (chrNums[n]) {
		    if (chr_n > 0) {
			chr_s += ",";
		    }
		    chr_s += (n+1) + "";
		    chr_n++;
		}
	    }
	}
	
	ps.print(" chr=\"" + chr_s + "\"");
    }

    static boolean hasModifiedProperties(GraphElement graphElement) {
	HashSet set = graphElement.getModifiedProperties();
	return set != null && set.size() > 0;
    }

    void printModifiedProperties(GlobalContext globalContext,
				 PrintStream ps, GraphElement graphElement) {
	HashSet set = graphElement.getModifiedProperties();
	
	if (set != null) {
	    int sz = set.size();
	    if (sz > 0)
		printProperties(ps, graphElement, set);
	}

	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) {
	    return;
	}
	DataElement data[] = dataSet.getData();
	for (int n = 0; n < data.length; n++) {
	    set = data[n].getModifiedProperties();
	    if (set != null) {
		int sz = set.size();
		if (sz > 0) {
		    ps.println("<DataElement num=\"" + n + "\" ID=\"" +
			       data[n].getID() + "\">");
		    printProperties(ps, data[n], set);
		    ps.println("</DataElement>");
		}
	    }
	}
    }

    void printProperty(PrintStream ps, Property prop, Object _value) {
	if (isPrintable(prop, _value)) {
	    String value = _value.toString();
	    value = value.replaceAll("\n", NEWLINE);
	    ps.println("<Property key=\"" +
		       prop.getName() + "\" value=\"" +
		       value + "\"/>");
	}
    }

    boolean isPrintable(Property prop, Object value) {
	return /*prop.isInfoable() && */
	    (value.getClass().getName().equals("java.lang.String") ||
	     value.getClass().getName().equals("java.lang.Boolean") ||
	     value.getClass().getName().equals("java.lang.Integer") ||
	     value.getClass().getName().equals("java.lang.Long") ||
	     value.getClass().getName().equals("java.lang.Float") ||
	     value.getClass().getName().equals("java.lang.Double"));
    }

    void printProperties(PrintStream ps, PropertyElement elem,
			 HashSet set) {
	Iterator it = set.iterator();
	while (it.hasNext()) {
	    Property prop = (Property)it.next();
	    Object value = elem.getPropertyValue(prop);
	    printProperty(ps, prop, value);
	}
    }

    void printProperties(PrintStream ps, PropertyElement elem) {
	TreeMap properties = elem.getProperties();

	if (properties.size() == 0)
	    return;

	//XMLUtils.printOpenTag(ps, "PropertySet");
	Iterator it = properties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    printProperty(ps, prop, entry.getValue());
	}
	//XMLUtils.printCloseTag(ps, "PropertySet");
    }

    boolean isSyntenyTarget(GraphElement graphElement) {
	return graphElement.getPropertyValue(VAMPProperties.SyntenyOPProp) != null;
    }

			    
    boolean hasSyntenyTarget(GraphPanel panel, GraphElement graphElement) {
	GraphElement ref = (GraphElement)graphElement.getPropertyValue
	    (VAMPProperties.LinkedDataSetProp);
	return ref != null && panel.getGraphElements().contains(ref);
    }

    static boolean isDendrogram(LinkedList graphElements) {
	return graphElements.size() > 0 &&
	    graphElements.get(0) instanceof DendrogramGraphElement;
    }

    boolean printDendrogram(GlobalContext globalContext,
			    PrintStream ps,
			    GraphPanel panel,
			    LinkedList graphElements,
			    String tag, String set_extra,
			    int depth) {
	int size = graphElements.size();
	if (size == 0)
	    return true;

	XMLUtils.printOpenTag(ps, tag, set_extra);
	for (int n = 0; n < size; n++) {
	    DendrogramGraphElement dendroGE =
		(DendrogramGraphElement)graphElements.get(n);
	    if (!dendroGE.isBridge()) {
		/*
		  if (hasModifiedProperties(dendroGE)) {
		  ps.println("<DendroElement ID=\"" +
		  (dendroGE.isLeaf() ? "" :
		  XMLLoadDataFactory.DENDRO_PREFIX) +
		  dendroGE.getID() + "\">");
		  printModifiedProperties(globalContext, ps, dendroGE);
		  ps.println("</DendroElement>");
		  }
		*/
		continue;
	    }

	    DendrogramBinNode dendroNode =
		(DendrogramBinNode)dendroGE.getDendrogramNode();
	    DendrogramNode left = dendroNode.getLeft();
	    DendrogramNode right = dendroNode.getRight();
	    int left_order, right_order;
	    String left_id, right_id;

	    String attr_str = "";
	    if (left instanceof DendrogramLeaf) {
		attr_str += " left_order=\"" +
		    (((DendrogramLeaf)left).getOrder()+1) + "\"";
		attr_str += " left=\"" + left.getID() + "\"";
	    }
	    else
		attr_str += " left=\"" +
		    XMLLoadDataFactory.DENDRO_PREFIX + left.getID() +
		    "\"";

	    if (right instanceof DendrogramLeaf) {
		attr_str += " right_order=\"" +
		    (((DendrogramLeaf)right).getOrder()+1) + "\"";
		attr_str += " right=\"" + right.getID() + "\"";
	    }
	    else
		attr_str += " right=\"" +
		    XMLLoadDataFactory.DENDRO_PREFIX + right.getID() +
		    "\"";

	    ps.println("<DendroNode ID=\"" +
		       XMLLoadDataFactory.DENDRO_PREFIX +
		       dendroGE.getID() + "\" height=\"" +
		       dendroNode.getHeight() + "\"" +
		       attr_str + ">");

	    //printModifiedProperties(globalContext, ps, dendroGE);
	    ps.println("</DendroNode>");
	}

	for (int n = 0; n < size; n++) {
	    DendrogramGraphElement dendroGE =
		(DendrogramGraphElement)graphElements.get(n);
	    /*
	      if (!hasModifiedProperties(dendroGE))
	      continue;
	    */
	    ps.println("<DendroNodePropertyList ID=\"" +
		       dendroGE.getID() +
		       (dendroGE.isBridge() ? "::bridge" : "") +
		       "\" color=\"" +
		       dendroGE.getColor().getRGB() + "\">");
	    printModifiedProperties(globalContext, ps, dendroGE);
	    ps.println("</DendroNodePropertyList>");
	}

	XMLUtils.printCloseTag(ps, tag);
	return true;
    }

    boolean printGraphElements(GlobalContext globalContext,
			       PrintStream ps,
			       GraphPanel panel,
			       LinkedList graphElements,
			       String tag, String set_extra,
			       int depth) {
	int size = graphElements.size();
	if (size > 0) {
	    XMLUtils.printOpenTag(ps, tag, set_extra);
	    for (int n = 0; n < size; n++) {
		GraphElement graphElement = (GraphElement)graphElements.get(n);
		GraphElementListOperation op = null;
		String type = VAMPUtils.getType(graphElement);
		String optag = null;
		String outer_tag = null;
		if (type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE)) {
		    if (isSyntenyTarget(graphElement)) {
			//op = GraphElementListOperation.get(UnsyntenyOP.NAME);
			// 27/05/05: changed because of merge of synteny and unsynteny
			op = GraphElementListOperation.get(SyntenyOP.SWITCH_NAME);
			optag = (String)graphElement.getPropertyValue
			    (VAMPProperties.SyntenyOPProp);
		    }
		    else if (depth == 0 &&
			     hasSyntenyTarget(panel, graphElement))
			continue;
		    else {
			// 17/09/07 replaced :
			if (NEW_CHR_MERGE_IMPL && (graphElement.asProfile() != null || VAMPUtils.isFullMergeChr(graphElement))) {
			    op = null;
			    optag = "Array";
			    outer_tag = "ChromosomeMerge";
			}
			else {
			    op = GraphElementListOperation.get
				(SplitChrOP.CGH_NAME);
			    optag = "ChromosomeMerge";
			}
		    }
		}
		else if (type.equals(VAMPConstants.CGH_ARRAY_MERGE_TYPE)) {
		    op = GraphElementListOperation.get
			(SplitArrayOP.CGH_NAME);
		    optag = "ArrayMerge";
		}
		else if (type.equals(VAMPConstants.CGH_AVERAGE_TYPE)) {
		    op = GraphElementListOperation.get
			(UnaverageOP.CGH_NAME);
		    optag = "Average";
		}
		else if (type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE)) {
		    op = GraphElementListOperation.get
			(SplitChrOP.CHIP_CHIP_NAME);
		    optag = "ChipChipChromosomeMerge";
		}
		else if (type.equals(VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE)) {
		    op = GraphElementListOperation.get
			(SplitArrayOP.CHIP_CHIP_NAME);
		    optag = "ChipChipArrayMerge";
		}
		else if (type.equals(VAMPConstants.CHIP_CHIP_AVERAGE_TYPE)) {
		    op = GraphElementListOperation.get
			(UnaverageOP.CHIP_CHIP_NAME);
		    optag = "ChipChipAverage";
		}
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE)) {
		    op = GraphElementListOperation.get
			(TranscriptomeUnaverageOP.NAME);
		    optag = "TranscriptomeAverage";
		}
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_MERGE_TYPE)) {
		    op = GraphElementListOperation.get
			(SplitTranscriptomeOP.NAME);
		    optag = "TranscriptomeMerge";
		}
		else if (type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE)) {
		    op = GraphElementListOperation.get
			(SplitChrOP.GENOME_ANNOT_NAME);
		    optag = "GenomeAnnotChromosomeMerge";
		}
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_REL_TYPE)) {
		    op = GraphElementListOperation.get
			(RelatedTranscriptomeOP.RELATED_TRANSCRIPTOME_INFO_NAME);
		    optag = "TranscriptomeRelative";
		}
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE)) {
		    op = GraphElementListOperation.get(RelatedArrayOP.NAME);
		    op = null;
		    optag = "Transcriptome";
		}
		else if (type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE)) {
		    op = GraphElementListOperation.get
			(SplitChrOP.TRANSCRIPTOME_NAME);
		    optag = "TranscriptomeChromosomeMerge";
		}
		else if (type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE)) {
		    op = GraphElementListOperation.get
			(SplitChrOP.SNP_NAME);
		    optag = "SNPChromosomeMerge";
		}
		else if (type.equals(VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE) ||
			 type.equals(VAMPConstants.FRAGL_TYPE)) {
		    op = GraphElementListOperation.get
			(RelatedArraysOP.NAME);
		    optag = "FrAGL";
		}
		else if (type.equals(VAMPConstants.FRAGL_ARRAY_MERGE_TYPE)) {
		    InfoDialog.pop(globalContext,
				   "Cannot save the Karyotype FrAGL view. " +
				   "Save the initial FrAGL profile instead");
		    return false;
		}
		else if (type.equals(VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE) ||
			 type.equals(VAMPConstants.GTCA_TYPE)) {
		    op = GraphElementListOperation.get
			(RelatedArraysOP.NAME);
		    optag = "GTCA";
		}
		else if (type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE) ||
			 type.equals(VAMPConstants.BREAKPOINT_FREQUENCY_TYPE)) {
		    op = GraphElementListOperation.get
			(RelatedArraysOP.NAME);
		    optag = "BreakpointFrequency";
		}
		else if (type.equals(VAMPConstants.LOH_TYPE)) {
		    op = GraphElementListOperation.get(RelatedArrayOP.NAME);
		    op = null;
		    optag = "LOH";
		}
		else if (type.equals(VAMPConstants.CGH_ARRAY_TYPE)) {
		    op = null;
		    optag = "Array";
		}
		else if (type.equals(VAMPConstants.CHIP_CHIP_TYPE)) {
		    op = null;
		    optag = "ChipChip";
		}
		else {
		    op = null;
		    optag = "Array";
		}

		String extra = getExtraProperties(graphElement) +
		    getGraphElementColorCodes(globalContext, graphElement) +
		    getGraphElementThresholds(globalContext, graphElement);

		if (op != null) {
		    Vector v = new Vector();
		    v.add(graphElement);
		    v = op.apply(null, null, v, null, false);
		    if (!printGraphElements(globalContext, ps, panel,
					    Utils.vectorToList(v), optag,
					    extra, depth+1))
			return false;
		} else {
		    if (!printGraphElement(globalContext, ps, panel,
					   graphElement, optag, extra, outer_tag))
			return false;
		}

	    }

	    XMLUtils.printCloseTag(ps, tag);
	}

	return true;
    }

    boolean printPanel(GlobalContext globalContext, PrintStream ps,
		       GraphPanel panel, boolean printMarksAndRegions) {
	GraphCanvas canvas = panel.getCanvas();
	Scale scale = canvas.getCanonScale();
	XMLUtils.printTag(ps, "PanelName", panel.getPanelName());
	XMLUtils.printTag(ps, "ScaleX", XMLUtils.toString(scale.getScaleX()));
	XMLUtils.printTag(ps, "ScaleY", XMLUtils.toString(scale.getScaleY()));

	XMLUtils.printOpenTag(ps, "Contents");

	LinkedList graphElements = canvas.getGraphElements();
	if (isDendrogram(graphElements)) {
	    if (!printDendrogram(globalContext, ps, panel,
				 graphElements, "Dendrogram",
				 null, 0))
		return false;
	}
	else if (!printGraphElements(globalContext, ps, panel,
				     graphElements, "ArraySet",
				     null, 0))
	    return false;
	if (printMarksAndRegions) {
	    LinkedList marks = canvas.getMarks();
	    int size = marks.size();
	    if (size > 0) {
		XMLUtils.printOpenTag(ps, "LandmarkSet");
		for (int n = 0; n < size; n++) {
		    Mark mark = (Mark)marks.get(n);
		    mark.setXID(n+1);
		    ps.println("<Mark xid=\"" + Utils.toString(mark.getXID()) +
			       "\" position=\"" + toString(mark) +
			       "\" color=\"" +
			       mark.getColor().getRGB() + "\">");
		    printProperties(ps, mark);
		    ps.println("</Mark>");
		}
		XMLUtils.printCloseTag(ps, "LandmarkSet");
	    }

	    LinkedList regions = canvas.getRegions();
	    size = regions.size();
	    if (size > 0) {
		XMLUtils.printOpenTag(ps, "RegionSet");
		for (int n = 0; n < size; n++) {
		    Region region = (Region)regions.get(n);
		    ps.println("<Region begin=\"" +
			       toString(region.getBegin()) +
			       "\" end=\"" +
			       toString(region.getEnd()) +
			       "\" begin_xid=\"" + region.getBegin().getXID() +
			       "\" end_xid=\"" + region.getEnd().getXID() +
			       "\" color=\"" +
			       region.getColor().getRGB() + "\">");
		    printProperties(ps, region);
		    ps.println("</Region>");
		}
		XMLUtils.printCloseTag(ps, "RegionSet");
	    }

	}

	XMLUtils.printCloseTag(ps, "Contents");

	return true;
    }

    String getExtraProperties(GraphElement graphElement) {
	String extra = "";
	String type = VAMPUtils.getType(graphElement);
	  
	extra += GraphElementListOperation.codeParams(graphElement);

	if (type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE) &&
	    graphElement.getPropertyValue(VAMPProperties.SyntenyOrigProp) != null) {
	    GraphElement ref = (GraphElement)graphElement.getPropertyValue
		(VAMPProperties.SyntenyReferenceProp);
	    if (ref != null)
		extra += " ARRAY_ID=\"" + ref.getID() + "\"";
	    String region_str = (String)graphElement.getPropertyValue
		(VAMPProperties.SyntenyRegionProp);
	    if (region_str != null)
		extra += " " + SyntenyOP.REGION_LIST_STR_PARAM +
		    "=\"" + region_str + "\"";
	}

	if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
	    /*type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) ||*/
	    type.equals(VAMPConstants.TRANSCRIPTOME_REL_TYPE) ||
	    type.equals(VAMPConstants.LOH_TYPE)) {
	    GraphElement rel = (GraphElement)graphElement.getPropertyValue(VAMPProperties.ArrayRefProp);
	    /*
	      System.out.println("rel of " + graphElement.getID() +
	      VAMPUtils.getChr(graphElement) + " is " + (rel == null ? "null" : rel.getID()));
	    */
	    if (rel != null)
		extra += " ARRAY_ID=\"" + rel.getID() +
		    "\" ARRAY_URL=\"" +
		    rel.getURL() + "\"";
	}

	Object ref = graphElement.getPropertyValue(VAMPProperties.ReferenceProp);
	if (ref != null)
	    extra += " " + VAMPProperties.ReferenceProp.getName() + "=\"" + ref + "\"";
	return extra;
    }

    static String toString(Mark mark) {
	//return mark.getRange().toString();
	return Utils.toString(mark.getPosX());
    }

    static void	printPanelLinkInd(PrintStream ps, int ind[]) {
	XMLUtils.printOpenTag(ps, "PanelLinksInd", false);
	for (int n = 0; n < ind.length; n++)
	    ps.print((n > 0 ? " " : "") + ind[n]);
	XMLUtils.printCloseTag(ps, "PanelLinksInd");
    }

    static void	printPanelLinks(PrintStream ps, GraphPanelSet gPanel) {
	PanelLinks panelLinks[] = gPanel.getPanelLinks();
	if (panelLinks == null) return;
	XMLUtils.printOpenTag(ps, "PanelLinksSet");
	for (int n = 0; n < panelLinks.length; n++) {
	    XMLUtils.printOpenTag(ps, "PanelLinks");
	    PanelLinks panelLink = panelLinks[n];
	    XMLUtils.printTag(ps, "PanelLinksName", panelLink.getName());
	    XMLUtils.printTag(ps, "PanelLinksSynchro",
			      Utils.toString(panelLink.getSyncMode()));
	    printPanelLinkInd(ps, panelLink.getInd());
	    XMLUtils.printCloseTag(ps, "PanelLinks");
	}
	XMLUtils.printCloseTag(ps, "PanelLinksSet");
    }

    static void printPanelLayout(PrintStream ps, PanelLayout panelLayout) {
	if (panelLayout instanceof PanelFinalLayout) {
	    XMLUtils.printTag(ps, "PanelFinalLayout",
			      XMLUtils.toString(((PanelFinalLayout)panelLayout).getInd()));
	}
	else {
	    XMLUtils.printOpenTag(ps, "PanelSplitLayout");
	    PanelSplitLayout panelSplitLayout =
		(PanelSplitLayout)panelLayout;
	    XMLUtils.printTag(ps, "PanelSplitLayoutOrientation",
			      XMLUtils.toString(panelSplitLayout.getOrientation()));
	    /*
	      XMLUtils.printTag(ps, "PanelSplitLayoutDivider",
	      XMLUtils.toString(panelSplitLayout.getDivider()));
	    */

	    printPanelLayout(ps, panelSplitLayout.getFirst());
	    printPanelLayout(ps, panelSplitLayout.getSecond());

	    XMLUtils.printCloseTag(ps, "PanelSplitLayout");
	}
    }

    static void	printPanelLayout(PrintStream ps, GraphPanelSet gPanel) {
	PanelLayout panelLayout = gPanel.getPanelLayout();
	if (panelLayout == null) return;

	XMLUtils.printOpenTag(ps, "PanelLayout");
	printPanelLayout(ps, panelLayout);
	XMLUtils.printCloseTag(ps, "PanelLayout");
    }

    boolean mustPrintMarksAndRegions(GraphPanelSet gPanel, int n) {
	if (n == 0)
	    return true;

	LinkedList linkedPaneX = gPanel.getPanel(n).getLinkedPaneX();
	int cnt = linkedPaneX.size();

	for (int i = 0; i < cnt; i++) {
	    GraphPanel lnk = (GraphPanel)linkedPaneX.get(i);
	    for (int j = 0; j < n; j++) {
		if (lnk == gPanel.getPanel(j))
		    return false;
	    }
	}

	return true;
    }
}

