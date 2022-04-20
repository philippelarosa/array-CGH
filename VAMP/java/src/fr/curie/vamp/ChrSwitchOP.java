
/*
 *
 * ChrSwitchOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import fr.curie.vamp.data.*;

import java.util.*;
import java.io.*;

class ChrSwitchOP extends GraphElementListOperation {
   
    static final String ALL_NAME = "Chromosome Switch All";
    static final String SELECTED_NAME = "Chromosome Switch Selected";
    static final String CHR_PARAM = "Chr";
    static final String VIEW_PARAM = "View";
    static final String CURRENT_VIEW_PARAM = "Current";
    static final String NEW_VIEW_PARAM = "New";
    static final String CHR_PREFIX = "/chr";

    private XMLArrayDataFactory arrayFactory;

    public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
			    VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.GENOME_ANNOT_TYPE,
			    VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.CHIP_CHIP_TYPE,
			    VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.TRANSCRIPTOME_TYPE,
			    VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.LOH_TYPE,
			    VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.SNP_TYPE,
			    VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE};
    }

    public String getReturnedType() {
	return null;
    }

    ChrSwitchOP(boolean all) {
	super(all ? ALL_NAME : SELECTED_NAME,
	      SHOW_MENU | (all ? ON_ALL_AUTO : 0));
    }

    public boolean mayApplyP(View view,  GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	int size = graphElements.size();
	String lastOs = null;
	String lastChr = null;
	for (int m = 0; m < size; m++) {
	    /*
	    DataSet ds = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (ds == null)
		return false;

	    */
	    GraphElement ds = (GraphElement)graphElements.get(m);
	    String os = VAMPUtils.getOS(ds);
	    if (os == null)
		return false;
	    String chr = VAMPUtils.getChr(ds);

	    if (chr == null)
		return false;

	    if (lastOs != null &&
		(!os.equals(lastOs) || !chr.equals(lastChr)))
		return false;

	    lastOs = os;
	    lastChr = chr;
	}

	//	return true;
	return getMergeChrOP(graphElements) != null;
    }

    //static final HashMap view_map = new HashMap();

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = ChrSwitchDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	//view_map.put(view, params);
	return params;
    }

    public TreeMap getDefaultParams(View view, Vector graphElements) {
	TreeMap params = new TreeMap();
	//	params.put(CHR_PARAM, getChr(graphElements));
	params.put(CHR_PARAM, "");
	params.put(VIEW_PARAM, CURRENT_VIEW_PARAM);
	return params;
    }

    private void moveRegions(LinkedList regions, long incpos) {
	int size = regions.size();

	for (int n = 0; n < size; n++) {
	    Region r = (Region)regions.get(n);
	    Mark begin = r.getBegin();
	    Mark end = r.getEnd();
	    begin.setLocation(begin.getPosX() + incpos);
	    end.setLocation(end.getPosX() + incpos);
	}
    }

    private void manageRegions(GlobalContext globalContext, GraphPanel panel, String organism, String origChr, String destChr) {
	LinkedList regions = panel.getRegions();
	if (regions == null || regions.size() == 0) {
	    return;
	}

	long chrPos[] = VAMPUtils.getChrPos(globalContext, organism);

	if (!VAMPUtils.isMergeChr(origChr)) {
	    origChr = VAMPUtils.norm2Chr(origChr);
	    int origChr_i = Integer.parseInt(origChr)-1;

	    if (destChr.equals(ChrSwitchDialog.ALL)) {
		long incpos = chrPos[origChr_i];
		moveRegions(regions, incpos);
		return;
	    }
	    destChr = VAMPUtils.norm2Chr(destChr);
	    int destChr_i = Integer.parseInt(destChr)-1;

	    long incpos = chrPos[origChr_i] - chrPos[destChr_i];
	    moveRegions(regions, incpos);
	    return;
	}

	assert !destChr.equals(ChrSwitchDialog.ALL);

	destChr = VAMPUtils.norm2Chr(destChr);
	int destChr_i = Integer.parseInt(destChr)-1;

	long incpos = -chrPos[destChr_i];
	moveRegions(regions, incpos);
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	try {
	    if (arrayFactory == null)
		arrayFactory = new XMLArrayDataFactory(view.getGlobalContext(),
						       null);

	    String chr = (String)params.get(CHR_PARAM);
	    if (chr.trim().length() == 0) {
		return graphElements;
	    }

	    chr = VAMPUtils.normChr(chr);
	    Vector rGraphElements = new Vector();
	    int size = graphElements.size();
	    String oldChrStr = CHR_PREFIX + "[^/]+/";

	    String chrs[];

	    if (chr.equals(ChrSwitchDialog.ALL)) {
		chrs = getChrList(view.getGlobalContext(), graphElements);
	    }
	    else {
		chrs = new String[]{chr};
	    }

	    String origChr = null;

	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = (GraphElement)graphElements.get(m);
		origChr = VAMPUtils.getChr(graphElement);
		DataSet ds = graphElement.asDataSet();
		if (ds == null) {
		    Profile profile = graphElement.asProfile();

		    Profile newProfile = null;
		    String chr_s, type;
		    if (chr.equals(ChrSwitchDialog.ALL)) {
			newProfile = profile.merge();
			// depends on the original type !
			type = VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE;
			chr_s = "";
			for (int n = 0; n < chrs.length; n++) {
			    if (n > 0) chr_s += ",";
			    chr_s += chrs[n];
			}
		    }		    
		    else {
			String s = VAMPUtils.norm2Chr(chr);
			int c = Integer.parseInt(s)-1;
			newProfile = (!profile.isWholePanGen() ? profile.merge() : profile).split(c);
			// depends on the original type !
			type = VAMPConstants.CGH_ARRAY_TYPE;
			chr_s = (new Integer(c+1)).toString();
		    }

		    VAMPUtils.setType(newProfile, type);
		    newProfile.setPropertyValue(VAMPProperties.ChromosomeProp, chr_s);
		    rGraphElements.add(newProfile);
		    continue;
		}

		if (VAMPUtils.isMergeChr(graphElement)) {
		    manageChrMerge(view, panel, graphElement, chr, chrs, rGraphElements);
		    continue;
		}

		for (int n = 0; n < chrs.length; n++) {
		    String s = chrs[n];
		    for (;;) {
			String newChrStr = CHR_PREFIX + VAMPUtils.normChr(s) + "/";
			String uri = ds.getURL().replaceFirst(oldChrStr, newChrStr);
			String url = XMLUtils.makeURL(view.getGlobalContext(), uri);
			//System.out.println("url : " + url);
			try {
			    InputStream is = Utils.openStream(url);
			} catch (FileNotFoundException e) {
			    // hack for chr X/23 and Y/24
			    String ns = VAMPUtils.norm2Chr(s);
			    if (!ns.equals(s)) {
				s = ns;
				continue;
			    }
			    InfoDialog.pop(view.getGlobalContext(), "File not found: " + url);
			    break;
			}

			LinkedList v = arrayFactory.getData(uri, false, ds.isFullImported());
			if (v != null && v.size() == 1) {
			    ((GraphElement)v.get(0)).setSourceType(ds.getSourceType());
			    rGraphElements.add(v.get(0));
			}
			break;
		    }
		}
	    }
	    
	    if (chr.equals(ChrSwitchDialog.ALL)) {
		GraphElementListOperation mergeChrOP = getMergeChrOP(graphElements);
		if (mergeChrOP == null) {
		    return null;
		}

		rGraphElements = mergeChrOP.apply(view, panel, rGraphElements, null);
	    }

	    manageRegions(view.getGlobalContext(), panel, (String)((GraphElement)graphElements.get(0)).getPropertyValue(VAMPProperties.OrganismProp), origChr, chr);

	    if (params.get(VIEW_PARAM).equals(CURRENT_VIEW_PARAM)) {
		return undoManage(panel, rGraphElements);
	    }

	    GraphPanelSet panelSet = view.getGraphPanelSet();
	    ViewFrame vf = new ViewFrame(view.getGlobalContext(),
					 view.getName(),
					 panelSet.getPanelProfiles(),
					 panelSet.getPanelLayout(),
					 panelSet.getPanelLinks(),
					 null, null,
					 new LinkedList(),
					 Config.defaultDim,
					 null);
	    LinkedList list = Utils.vectorToList(rGraphElements);
	    vf.getView().getGraphPanelSet().getPanel(panel.getWhich()).setGraphElements(list);
	    vf.setVisible(true);
	    vf.getView().syncGraphElements();
	    vf.getView().getGraphPanelSet().setDefaultGraphElementDisplayer(panel.getDefaultGraphElementDisplayer());


	    return null;
	}
	catch(Exception e) {
	    System.err.println(e);
	    return null;
	}
    }

    static String[] getChrList(GlobalContext globalContext,
			       Vector graphElements) {
	
	GraphElement ds = (GraphElement)graphElements.get(0);
	String os = VAMPUtils.getOS(ds);
	if (os == null)
	    return null;

	Cytoband cytog = MiniMapDataFactory.getCytoband(globalContext, os);
	if (cytog == null)
	    return null;

	Vector chrV = cytog.getChrV();
	int sz = chrV.size();

	String s[] = new String[sz];
	for (int n = 0; n < sz; n++) {
	    s[n] = ((Chromosome)chrV.get(n)).getName();
	}

	return s;
    }

    String getChr(Vector graphElements) {
	
	int size = graphElements.size();
	String lastOs = null;
	for (int m = 0; m < size; m++) {
	    DataSet ds = (DataSet)graphElements.get(m);
	    if (ds == null)
		return null;
	    //if (VAMPUtils.getType(ds).equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
	    if (VAMPUtils.isMergeChr(ds))
		return ChrSwitchDialog.ALL;
	    return VAMPUtils.getChr(ds);
	}
	return "";
    }

    private void manageChrMerge(View view, GraphPanel panel,
				GraphElement ds, String chr, String chrs[],
				Vector rGraphElements) {
	if (chr.equals(ChrSwitchDialog.ALL)) {
	    rGraphElements.add(ds);
	    return;
	}

	GraphElementListOperation splitChrOP = getSplitChrOP(ds);

	AxisDisplayer axis_dsp = panel.getDefaultAxisDisplayer();
	for (int n = 0; n < chrs.length; n++) {
	    Vector v = new Vector();
	    v.add(ds);
	    Vector rv = splitChrOP.apply(view, panel, v, null);
	    int sz = rv.size();
	    for (int n2 = 0; n2 < sz; n2++) {
		if (VAMPUtils.norm2Chr(VAMPUtils.getChr((GraphElement)rv.get(n2))).equals
		    (VAMPUtils.norm2Chr(chrs[n])))
		    rGraphElements.add(rv.get(n2));
	    }
	}
	panel.setDefaultAxisDisplayer(axis_dsp);
    }

    static GraphElementListOperation getMergeChrOP(Vector graphElements) {
	int size = graphElements.size();
	GraphElementListOperation op = null;

	for (int n = 0; n < size; n++) {
	    /*
	    DataSet ds = ((GraphElement)graphElements.get(n)).asDataSet();
	    if (ds == null)
		return null;
	    String type = VAMPUtils.getType(ds);
	    */
	    String type = VAMPUtils.getType((GraphElement)graphElements.get(n));
	    GraphElementListOperation xop;
	    if (type.equals(VAMPConstants.CGH_ARRAY_TYPE) ||
		type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE)) {
		xop = GraphElementListOperation.get(MergeChrOP.CGH_NAME);
		if (op != null && op != xop)
		    return null;
		op = xop;
	    }
	    else if (type.equals(VAMPConstants.GENOME_ANNOT_TYPE) ||
		     type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE)) {
		xop = GraphElementListOperation.get(MergeChrOP.GENOME_ANNOT_NAME);
		if (op != null && op != xop)
		    return null;
		op = xop;
	    }
	    else if (type.equals(VAMPConstants.CHIP_CHIP_TYPE) ||
		     type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE)) {
		xop = GraphElementListOperation.get(MergeChrOP.CHIP_CHIP_NAME);
		if (op != null && op != xop)
		    return null;
		op = xop;
	    }
	    else if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
		     type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE)) {
		xop = GraphElementListOperation.get(MergeChrOP.TRANSCRIPTOME_NAME);
		if (op != null && op != xop)
		    return null;
		op = xop;
	    }
	    else if (type.equals(VAMPConstants.SNP_TYPE) ||
		     type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE)) {
		xop = GraphElementListOperation.get(MergeChrOP.SNP_NAME);
		if (op != null && op != xop)
		    return null;
		op = xop;
	    }
	    else if (type.equals(VAMPConstants.LOH_TYPE) ||
		     type.equals(VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE)) {
		xop = GraphElementListOperation.get(MergeChrOP.LOH_NAME);
		if (op != null && op != xop)
		    return null;
		op = xop;
	    }
	    else
		return null;
	}

	return op;
    }

    GraphElementListOperation getSplitChrOP(GraphElement ds) {
	String type = VAMPUtils.getType(ds);
	if (type.equals(VAMPConstants.CGH_ARRAY_TYPE) ||
	    type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
	    return GraphElementListOperation.get(SplitChrOP.CGH_NAME);

	if (type.equals(VAMPConstants.GENOME_ANNOT_TYPE) ||
	    type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE))
	    return GraphElementListOperation.get(SplitChrOP.GENOME_ANNOT_NAME);

	if (type.equals(VAMPConstants.CHIP_CHIP_TYPE) ||
	    type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE))
	    return GraphElementListOperation.get(SplitChrOP.CHIP_CHIP_NAME);

	if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
	    type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE))
	    return GraphElementListOperation.get(SplitChrOP.TRANSCRIPTOME_NAME);
	if (type.equals(VAMPConstants.SNP_TYPE) ||
	    type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE))
	    return GraphElementListOperation.get(SplitChrOP.SNP_NAME);

	if (type.equals(VAMPConstants.LOH_TYPE) ||
	    type.equals(VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE))
	    return GraphElementListOperation.get(SplitChrOP.LOH_NAME);

	return null;
    }

    public boolean useThread() {
	return true;
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean supportProfiles() {return true;}
}
