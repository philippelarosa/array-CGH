
/*
 *
 * SplitChrOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import fr.curie.vamp.data.Profile;
import java.util.*;

class SplitChrOP extends MergeSplitOP {
   
    static final String CGH_NAME = "Split CGH Chromosomes";
    static final String CHIP_CHIP_NAME = "Split ChIP-chip Chromosomes";
    static final String TRANSCRIPTOME_NAME = "Split Transcriptome Chromosomes";
    static final String SNP_NAME = "Split SNP Chromosomes";
    static final String LOH_NAME = "Split LOH Chromosomes";
    static final String GENOME_ANNOT_NAME = "Split Genome Annotation Chromosomes";
    static final String FRAGL_NAME = "Split FrAGL Chromosomes";
    static final String DIFFANA_NAME = "Split Differential Analysis Chromosomes";
    static final String GTCA_NAME = "Split GTCA Chromosomes";

    public String[] getSupportedInputTypes() {
	if (isChipChip())
	    return new String[]{VAMPConstants.CHIP_CHIP_TYPE,
				VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE};
	if (isCGH())
	    return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
				VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
	if (isTranscriptome())
	    return new String[]{VAMPConstants.TRANSCRIPTOME_TYPE,
				VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE};

	if (isDifferentialAnalysis())
	    return new String[]{VAMPConstants.DIFFANA_TYPE,
				VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE};

	if (isGTCAAnalysis())
	    return new String[]{VAMPConstants.GTCA_TYPE,
				VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE};

	if (isSNP())
	    return new String[]{VAMPConstants.SNP_TYPE,
				VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE};

	if (isLOH())
	    return new String[]{VAMPConstants.LOH_TYPE,
				VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE};

	if (isGenomeAnnot())
	    return new String[]{VAMPConstants.GENOME_ANNOT_TYPE,
				VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE};

	if (isFrAGL())
	    return new String[]{VAMPConstants.FRAGL_TYPE,
				VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE};

	return null;
    }

    public String getReturnedType() {
	return null;
    }

    static String getName(int type) {
	if (type == CHIP_CHIP_TYPE)
	    return CHIP_CHIP_NAME;
	if (type == CGH_TYPE)
	    return CGH_NAME;
	if (type == TRANSCRIPTOME_TYPE)
	    return TRANSCRIPTOME_NAME;
	if (type == DIFFANA_TYPE)
	    return DIFFANA_NAME;
	if (type == GTCA_TYPE)
	    return GTCA_NAME;
	if (type == SNP_TYPE)
	    return SNP_NAME;
	if (type == LOH_TYPE)
	    return LOH_NAME;
	if (type == GENOME_ANNOT_TYPE)
	    return GENOME_ANNOT_NAME;
	if (type == FRAGL_TYPE)
	    return FRAGL_NAME;
	return "<unknown>";
    }

    SplitChrOP(int type) {
	// suppress ON_ALL 4/02/05
	super(getName(type), type, /*ON_ALL|*/SHOW_MENU);
    }

    public boolean mayApply(GraphElementListOperation op) {
	if (op == null)
	    return true;

	if (op.equals(this))
	    return false;

	if (isChipChip())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.CHIP_CHIP_NAME));

	if (isCGH())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.CGH_NAME));
	if (isTranscriptome())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.TRANSCRIPTOME_NAME));

	if (isDifferentialAnalysis())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.DIFFANA_NAME));

	if (isGTCAAnalysis())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.GTCA_NAME));

	if (isSNP())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.SNP_NAME));
	if (isLOH())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.LOH_NAME));
	if (isGenomeAnnot())
	    return !op.equals(GraphElementListOperation.get(MergeChrOP.GENOME_ANNOT_NAME));
	if (isFrAGL())
	    //return !op.equals(GraphElementListOperation.get(MergeChrOP.FRAGL_NAME));
	    return true; // TBD

	return false;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {
	if (noOneMerged(graphElements)) {
	    return graphElements;
	}

	/*
	  if (view != null &&
	  (view.getMarks().size() > 0 ||
	  (view.getRegions().size() > 0))) {
	  InfoDialog.pop(view.getGlobalContext(),
	  "Cannot split a region with marks and/or regions:" +
	  " must remove them from view before");
	  return graphElements;
	  }
	*/

	try {
	    Vector rGraphElements = new Vector();
	    int size = graphElements.size();
	    HashMap splitMap = new HashMap();

	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = (GraphElement)graphElements.get(m);
		if (!isMerged(graphElement)) {
		    rGraphElements.add(graphElement);
		    continue;
		}

		DataSet dataSet = graphElement.asDataSet();
		Profile profile = graphElement.asProfile();
		if (dataSet != null) {
		    manageDataset(view, panel, params, autoApply, splitMap, rGraphElements, dataSet);
		}
		else if (profile != null) {
		    manageProfile(view, panel, params, autoApply, splitMap, rGraphElements, profile);
		}
		else {
		    continue;
		}
	    }

	    if (panel != null) {
		panel.setDefaultDisplayers(panel.getDefaultGraphElementDisplayer(),
					   Config.defaultGenomicPositionAxisDisplayer);
		// the IDBuilder should be taken in a hidden property of one dataset
		panel.setGraphElementIDBuilder(Config.dataSetIDChrArrayBuilder);
	    }

	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    System.err.println(e);
	    return null;
	}
    }

    void manageProfile(View view, GraphPanel panel, TreeMap params, boolean autoApply, HashMap splitMap, Vector rGraphElements, Profile profile) throws Exception {
	/*
	// just to test
	if (true) {
	    boolean chrNums[] = new boolean[24];
	    chrNums[0] = true;
	    chrNums[8] = true;
	    chrNums[17] = true;
	    profile.setChrNums(chrNums);
	    rGraphElements.addElement(profile);
	    return;
	}
	*/

	long chrPosMap[] = profile.getChrPosMap();
	for (int n = 0; n < profile.getChrCount(); n++) {
	    Profile prof = profile.split(n);
	    if (prof == null) {
		continue;
	    }
	    rGraphElements.addElement(prof);
	    setType(prof, (new Integer(n+1)).toString());
	    prof.setPropertyValue(VAMPProperties.CloneCountProp, new Integer(prof.getProbeCount()));

	    // ??
	    prof.removeProperty(ChromosomeNameAxisDisplayer.ChrCacheProp);
	    prof.removeProperty(ChromosomeNameAxisDisplayer.ChrCache2Prop);
	}
	profile.release();
    }

    void manageDataset(View view, GraphPanel panel, TreeMap params, boolean autoApply, HashMap splitMap, Vector rGraphElements, DataSet dataSet) throws Exception {
	LinkedList curDataList = new LinkedList();
	DataSet curDataSet = null;
	HashMap url_map = (HashMap)dataSet.getPropertyValue(VAMPProperties.URLMapProp);
	String lastChr = "";

	Iterator it;
	int max;
	if (dataSet.isFullImported()) {
	    it = null;
	    max = dataSet.getData().length;
	}
	else {
	    it = url_map.entrySet().iterator();
	    max = url_map.size();
	}

	for (int n = 0, chr_n = 0; n < max; n++) {
	    DataElement odata;

	    Object v;
	    if (dataSet.isFullImported()) {
		odata = dataSet.getData()[n];
		v = VAMPUtils.getChr(odata);
	    }
	    else {
		Map.Entry entry = (Map.Entry)it.next();
		v = entry.getKey();
		odata = null;
	    }

	    if (!lastChr.equals(v)) {
		complete(curDataSet, curDataList);
		curDataSet = new DataSet(dataSet.isFullImported());

		curDataSet.setMinMaxX(dataSet.getLMaxX());
		curDataSet.setMinMaxX(dataSet.getLMinX());
		curDataSet.setMinMaxY(dataSet.getLMaxY());
		curDataSet.setMinMaxY(dataSet.getLMinY());

		curDataSet.setSourceURL(dataSet.getSourceURL());
		curDataSet.setSourceType(dataSet.getSourceType());
		//curDataSet.setProperties((TreeMap)dataSet.getProperties().clone());
		curDataSet.cloneProperties(dataSet);
		String url = (url_map != null ? (String)url_map.get(v) :
			      null);
		//System.out.println("url = " + url + " -> " + v);
		manageArrayRef(view, panel, params,
			       autoApply, curDataSet, splitMap,
			       (String)v);

		curDataSet.setURL((String)url);
					   
		setType(curDataSet, v);

		rGraphElements.addElement(curDataSet);
		curDataList = new LinkedList();
		lastChr = (String)v;
		chr_n++;
	    }

	    if (odata != null) {
		DataElement data = (DataElement)odata.clone();
		data.copyPos(curDataSet, odata, dataSet);
		data.cloneProperties(odata);
			
		if (isTranscriptome())
		    XMLTranscriptomeFactory.postAction(curDataSet, data);
		else if (isLOH())
		    XMLLOHFactory.postAction(curDataSet, data);
		else
		    data.setPosX(curDataSet, Utils.parseDouble((String)
							       data.getPropertyValue(VAMPProperties.PositionProp)));
		curDataList.add(data);
	    }
	}

	complete(curDataSet, curDataList);
    }

    private void complete(DataSet curDataSet, LinkedList curDataList) {
	if (curDataSet == null) {
	    return;
	}
	curDataSet.removeProperty(ChromosomeNameAxisDisplayer.ChrCacheProp);
	curDataSet.removeProperty(ChromosomeNameAxisDisplayer.ChrCache2Prop);
	curDataSet.setData(DataElement.makeData(curDataList));
	curDataSet.setPropertyValue(VAMPProperties.CloneCountProp,
				    new Integer(curDataList.size()));
    }

    private void setType(GraphElement graphElem, Object chr) {
	if (isChipChip())
	    VAMPUtils.setType(graphElem, VAMPConstants.CHIP_CHIP_TYPE);
	else if (isCGH())
	    VAMPUtils.setType(graphElem, VAMPConstants.CGH_ARRAY_TYPE);
	else if (isTranscriptome()) {
	    graphElem.setAutoY(true); // 4/02/05
	    VAMPUtils.setType(graphElem, VAMPConstants.TRANSCRIPTOME_TYPE);
	}
	else if (isSNP())
	    VAMPUtils.setType(graphElem, VAMPConstants.SNP_TYPE);
	else if (isLOH())
	    VAMPUtils.setType(graphElem, VAMPConstants.LOH_TYPE);
	else if (isDifferentialAnalysis())
	    VAMPUtils.setType(graphElem, VAMPConstants.DIFFANA_TYPE);
	else if (isGTCAAnalysis())
	    VAMPUtils.setType(graphElem, VAMPConstants.GTCA_TYPE);
	
	else if (isGenomeAnnot()) {
	    VAMPUtils.setType(graphElem, VAMPConstants.GENOME_ANNOT_TYPE);
	    graphElem.setGraphElementDisplayer
		(new GenomeAnnotDataSetDisplayer());
	}
	else if (isFrAGL())
	    VAMPUtils.setType(graphElem, VAMPConstants.FRAGL_TYPE);

	graphElem.setPropertyValue(VAMPProperties.ChromosomeProp, chr);
	graphElem.setPropertyValue(VAMPProperties.SplitChrProp, "true");
    }

    /*
    public boolean isMerged(GraphElement graphElem) {

	String type = VAMPUtils.getType(graphElem);

	if (isChipChip()) {
	    return type.equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE);
	}

	if (isCGH()) {
	    return type.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE);
	}

	if (isTranscriptome()) {
	    return type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE);
	}

	if (isDifferentialAnalysis()) {
	    return type.equals(VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE);
	}

	if (isGTCAAnalysis()) {
	    return type.equals(VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE);
	}

	if (isSNP()) {
	    return type.equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE);
	}

	if (isLOH()) {
	    return type.equals(VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE);
	}

	if (isGenomeAnnot()) {
	    return type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE);
	}

	if (isFrAGL()) {
	    return type.equals(VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE);
	}

	return false;
    }

    private boolean noOneMerged(Vector graphElements) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    if (isMerged((GraphElement)graphElements.get(n))) {
		return false;
	    }
	}

	return true;
    }
    */

    private DataSet getChr(Vector split_v, String chr) {
	int size = split_v.size();
	if (chr.equals("X")) chr = "23";
	else if (chr.equals("Y")) chr = "24";

	for (int n = 0; n < size; n++) {
	    DataSet dset = (DataSet)split_v.get(n);
	    if (VAMPUtils.getChr(dset).equals(chr))
		return dset;
	}

	//System.out.println("returning null for " + chr);
	return null;
    }

    private void manageArrayRef(View view, GraphPanel panel,
				TreeMap params, boolean autoApply,
				DataSet curDataSet,
				HashMap splitMap,
				String chr) {
	DataSet array_ref = (DataSet)curDataSet.getPropertyValue(VAMPProperties.ArrayRefProp);
	if (array_ref == null)
	    return;

	/*
	  System.out.println("splitChrOP: arrayRef of " + curDataSet.getID() + ":" + VAMPUtils.getChr(curDataSet) + "[" + chr + "] is " + array_ref.getID() + " " + VAMPUtils.getChr(array_ref));
	*/

	if (!VAMPUtils.isMergeChr(array_ref))
	    return;

	GraphElementListOperation op =
	    GraphElementListOperation.get(SplitChrOP.CGH_NAME);
	Vector split_v = (Vector)splitMap.get(array_ref.getID());
	if (split_v == null) {
	    Vector v = new Vector();
	    v.add(array_ref);
	    split_v = op.apply(view, panel, v, params, autoApply);
	    splitMap.put(array_ref.getID(), split_v);
	}

	curDataSet.setPropertyValue(VAMPProperties.ArrayRefProp,
				    getChr(split_v, chr));
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}

    public boolean mustInitScale() {return true;}

    public boolean supportProfiles() {return true;}
}
