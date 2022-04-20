
/*
 *
 * MergeChrOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import fr.curie.vamp.data.Profile;

class MergeChrOP extends MergeOP {
   
    static final String LIGHT_SUFFIX = "_LIGHT";

    static final String CGH_NAME = "Merge CGH Chromosomes";
    static final String CGH_NAME_LIGHT = "Merge CGH Chromosomes" + LIGHT_SUFFIX;
    static final String TRANSCRIPTOME_NAME = "Merge Transcriptome Chromosomes";
    static final String CHIP_CHIP_NAME = "Merge Chip Chip Chromosomes";
    static final String SNP_NAME = "Merge SNP Chromosomes";
    static final String LOH_NAME = "Merge LOH Chromosomes";
    static final String GENOME_ANNOT_NAME = "Merge Genome Annotation Chromosomes";
    static final String DIFFANA_NAME = "Merge Differential Analysis Chromosomes";
    static final String GTCA_NAME = "Merge GTCA Chromosomes";

    static final int OFFSET_CHR = 0;
    boolean light;

    public String[] getSupportedInputTypes() {
	if (isChipChip())
	    return new String[]{VAMPConstants.CHIP_CHIP_TYPE,
				VAMPConstants.CHIP_CHIP_ARRAY_MERGE_TYPE,
				VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE};
	if (isCGH())
	    return new String[]{VAMPConstants.CGH_ARRAY_TYPE,
				VAMPConstants.CGH_ARRAY_MERGE_TYPE,
				VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE};
	if (isTranscriptome())
	    return new String[]{VAMPConstants.TRANSCRIPTOME_TYPE,
				VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE};

	if (isSNP())
	    return new String[]{VAMPConstants.SNP_TYPE,
				VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE};

	if (isLOH())
	    return new String[]{VAMPConstants.LOH_TYPE,
				VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE};

	if (isGenomeAnnot())
	    return new String[]{VAMPConstants.GENOME_ANNOT_TYPE,
				VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE};

	if (isDifferentialAnalysis())
	    return new String[]{VAMPConstants.DIFFANA_TYPE,
				VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE};

	if (isGTCAAnalysis())
	    return new String[]{VAMPConstants.GTCA_TYPE,
				VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE};

	return null;
    }

    public String getReturnedType() {
	if (isChipChip())
	    return VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE;

	if (isCGH())
	    return VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE;

	if (isTranscriptome())
	    return VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE;

	if (isDifferentialAnalysis())
	    return VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE;

	if (isGTCAAnalysis())
	    return VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE;

	if (isSNP())
	    return VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE;
	    
	if (isLOH())
	    return VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE;
	    
	if (isGenomeAnnot())
	    return VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE;
	    
	return null;
    }

    static TreeSet make(Vector graphElements) {
	TreeSet treeSet = new TreeSet();
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    treeSet.add(new DataSetChrComparator((GraphElement)graphElements.get(n)));
	}
	return treeSet;
    }

    boolean check(TreeSet treeSet) {
	String lastChr = "";
	Object graphElements[] = treeSet.toArray();
	for (int n = 0; n < graphElements.length; n++) {
	    GraphElement graphElement = ((DataSetChrComparator)graphElements[n]).graphElement;
	    if (isChipChip()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }
	    else if (isCGH()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }
	    else if (isTranscriptome()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }
	    else if (isDifferentialAnalysis()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }
	    else if (isGTCAAnalysis()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }
	    else if (isSNP()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }
	    else if (isLOH()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }
	    else if (isGenomeAnnot()) {
		if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		    (VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE))
		    continue;
	    }

	    String chr = VAMPUtils.getChr(graphElement);
	    /*
	      if (chr == null || chr.equals(lastChr)) {
	      System.out.println("chr=" + chr + ", lastChr=" + lastChr);
	      return false;
	      }
	    */

	    if (chr == null)
		return false;

	    lastChr = chr;
	}
	return true;
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
	return "<unknown>";
    }

    static String getName(int type, boolean light) {
	return getName(type) + (light ? LIGHT_SUFFIX : "");
    }

    MergeChrOP(int type) {
	super(getName(type, false), type, SHOW_MENU);
	this.light = false;
    }

    MergeChrOP(int type, boolean light) {
	super(getName(type, light), type, (light ? 0 : SHOW_MENU));
	this.light = light;
    }

    public boolean mayApply(GraphElementListOperation op) {
	if (op == null)
	    return true;

	if (op.equals(this))
	    return false;

	if (isChipChip())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.CHIP_CHIP_NAME));

	if (isCGH())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.CGH_NAME));
	if (isTranscriptome())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.TRANSCRIPTOME_NAME));

	if (isDifferentialAnalysis())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.DIFFANA_NAME));

	if (isGTCAAnalysis())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.GTCA_NAME));

	if (isSNP())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.SNP_NAME));
	if (isLOH())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.LOH_NAME));
	if (isGenomeAnnot())
	    return !op.equals(GraphElementListOperation.get(SplitChrOP.GENOME_ANNOT_NAME));
	return false;
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {
	return check(make(graphElements));
    }

    private class ChrProfile {
	Profile profile;
	boolean chrNums[];

	ChrProfile(Profile profile) {
	    this.profile = profile;
	    chrNums = new boolean[Profile.MAX_CHR_CNT];
	    for (int n = 0; n < chrNums.length; n++) {
		chrNums[n] = false;
	    }
	}

	void add(Profile profile) {
	    chrNums[profile.getChrNum()] = true;
	}
    }

    public Vector apply(View view, GraphPanel panel,
			Vector _graphElements, TreeMap params,
			boolean autoApply) {

	if (areAllMerged(_graphElements)) {
	    return _graphElements;
	}

	_graphElements = applyPrologue(view, panel, _graphElements, autoApply);
	TreeSet treeSet = make(_graphElements);

	try {
	    Vector rDataSets = new Vector();
	    Object graphElements[] = treeSet.toArray();
	    double lastPos = 0.;
	    String lastChr = "";

	    String chrList = null;
	    boolean ended = false;
	    String lastID = "";
	    DataSet rDataSet = null;
	    LinkedList curDataList = null;

	    HashMap url_map = null;


	    HashMap<String, ChrProfile> profileMap = new HashMap();

	    GlobalContext globalContext = (view != null ?
					   view.getGlobalContext() :
					   GlobalContext.getLastInstance());

	    LinkedList curDataSetList = null;
	    int count_obj = 0;
	    for (int m = 0; m < graphElements.length; m++) {
		GraphElement graphElement = ((DataSetChrComparator)graphElements[m]).graphElement;
		if (isMerged(graphElement)) {
		    rDataSets.add(graphElement);
		    continue;
		}

		Profile profile = graphElement.asProfile();
		if (profile != null) {
		    String id = (String)profile.getID();
		    ChrProfile chrProfile = profileMap.get(id);
		    if (chrProfile == null) {
			chrProfile = new ChrProfile(profile);
			profileMap.put(id, chrProfile);
		    }
		    
		    chrProfile.add(profile);
		    continue;
		}

		DataSet dataSet = graphElement.asDataSet();
		if (dataSet == null) {
		    continue;
		}

		String chr = VAMPUtils.getChr(dataSet);
		Cytoband cytoband;
		String organism = VAMPUtils.getOS(dataSet);
		cytoband = MiniMapDataFactory.getCytoband
		    (globalContext, organism);

		String id = (String)dataSet.getID();
		if (!id.equals(lastID)) {
		    complete(globalContext, rDataSet, curDataList, chrList, curDataSetList, url_map, count_obj);

		    url_map = new HashMap();
		    curDataSetList = new LinkedList();
		    lastID = id;
		    lastChr = "";
		    lastPos = 0;
		    ended = false;
		    chrList = "";
		    rDataSet = new DataSet(dataSet.isFullImported());
		    rDataSet.setURL(VAMPUtils.makePanGenomicURL(dataSet)); // added 17/09/07


		    rDataSet.setSourceURL(VAMPUtils.makePanGenomicURL(dataSet));
		    rDataSet.setSourceType(dataSet.getSourceType());
		    rDataSets.add(rDataSet);
		    curDataList = new LinkedList();
		    count_obj = 0;
		    //rDataSet.setProperties((TreeMap)dataSet.getProperties().clone());
		    rDataSet.cloneProperties(dataSet);
		}

		if (!dataSet.isFullImported()) {
		    Integer count = (Integer)dataSet.getPropertyValue(VAMPProperties.CloneCountProp);
		    if (count != null) {
			count_obj += count.intValue();
		    }
		}

		curDataSetList.add(dataSet);

		if (!lastChr.equals(chr) && cytoband != null) {
		    Chromosome xchr = cytoband.getChromosome(chr);
		    if (xchr == null) {
			InfoDialog.pop(globalContext, "Warning: chromosome " +
				       chr + " not found in cytoband " +
				       organism);
			continue;
		    }
		    lastPos = xchr.getBegin_o();
		}

		url_map.put(chr, dataSet.getURL());
		int length = dataSet.getData() != null ?
		    dataSet.getData().length : 0;
		//if (length == 0) {
		if (false) {
		    DataElement data = new DataElement();
		    // make a dummy element !
		    // EV 2/10/06  : why ?
		    data.setPropertyValue(VAMPProperties.ChromosomeProp, chr);
		    data.setPropertyValue(VAMPProperties.PositionProp, "0");
		    data.setPropertyValue(VAMPProperties.IsNAProp, "True");
		    data.setPropertyValue(VAMPProperties.RatioProp, "0");
		    data.setPropertyValue(VAMPProperties.ArrayProp, 
					  dataSet.getPropertyValue(VAMPProperties.NameProp));
		    curDataList.add(data);
		}
		else {
		    for (int n = 0; n < length; n++) {
			DataElement odata = dataSet.getData()[n];
			DataElement data = (DataElement)odata.clone();
			data.copyPos(rDataSet, odata, dataSet);
			//data.setProperties((TreeMap)odata.getProperties().clone());
			// 30/04/06:
			// not useful because properties are already cloned
			//data.cloneProperties(odata);


			// suppressed 7/02/05
			/*
			// added 4/02/05
			data.setPropertyValue(VAMPConstants.ChromosomeProp, chr);
			*/
			data.setPosX(rDataSet, data.getPosX(rDataSet) + lastPos);
			data.setPropertyValue(VAMPProperties.MergeOffsetProp,
					      new Double(lastPos));
			curDataList.add(data);
		    }
		}
		
		if (!chr.equals(lastChr)) {
		    if (length > 0) {
			/*
			  lastPos += dataSet.getData()[length-1].getVX() +
			  dataSet.getData()[length-1].getVSize() +
			  OFFSET_CHR;
			*/
			if (cytoband != null) {
			    /*
			      lastPos = cytoband.getChromosome(chr).getEnd_o();
			      System.out.println("lastPos : " + chr + ":" +
			      lastPos);
			    */
			}
			else {
			    System.out.println("Error cytoband == null");
			    lastPos += dataSet.getData()[length-1].getVX(dataSet) +
				dataSet.getData()[length-1].getVSize(dataSet) +
				OFFSET_CHR;
			}
		    }

		    if (chrList.length() > 0) chrList += ",";
		    chrList += chr;
		}
		lastChr = chr;
	    }

	    //chrList = makeChrList(chrList);

	    Iterator it = profileMap.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		ChrProfile chrProfile = (ChrProfile)entry.getValue();
		Profile profile = chrProfile.profile.merge(chrProfile.chrNums);
		setType(globalContext, profile);
		rDataSets.add(profile);
	    }

	    complete(globalContext, rDataSet, curDataList, chrList,
		     curDataSetList, url_map, count_obj);

	    if (panel != null) {
		if (panel.getDefaultAxisDisplayer() instanceof
		    YDendrogramAxisDisplayer)
		    panel.setDefaultDisplayers(panel.getDefaultGraphElementDisplayer(),
					       panel.getDefaultAxisDisplayer());
		else if (!(panel.getDefaultAxisDisplayer() instanceof
			   ChromosomeNameAxisDisplayer)) {
		    panel.setDefaultDisplayers
			(panel.getDefaultGraphElementDisplayer(),
			 new ChromosomeNameAxisDisplayer
			 (VAMPUtils.getAxisName((GraphElement)_graphElements.get(0)), 1., 0.1, false));
		}
		panel.setGraphElementIDBuilder(Config.dataSetIDArrayBuilder);
	    }

	    return undoManage(panel, rDataSets);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private void complete(GlobalContext globalContext,
			  DataSet rDataSet, LinkedList curDataList,
			  String chrList, LinkedList curDataSetList,
			  HashMap url_map, int count_obj) {
	if (rDataSet == null) return;
	rDataSet.setData(DataElement.makeData(curDataList));
	if (curDataList.size() == 0)
	    rDataSet.setPropertyValue(VAMPProperties.CloneCountProp,
				      count_obj);
	else
	    rDataSet.setPropertyValue(VAMPProperties.CloneCountProp,
				      new Integer(curDataList.size()));
	rDataSet.removeProperty(ChromosomeNameAxisDisplayer.ChrCacheProp);
	rDataSet.removeProperty(ChromosomeNameAxisDisplayer.ChrCache2Prop);
	rDataSet.setPropertyValue(VAMPProperties.ChromosomeProp, chrList);
	// EV 21/08/06 kludge
	/*
	  if (rDataSet.getSourceURL().indexOf(VAMPConstants.ARRAY_DIR) >= 0)
	  rDataSet.cache();
	*/
	if (VAMPUtils.isPanGenomicURL(rDataSet))
	    rDataSet.cache();

	// added 8/02/05 !!
	manageArrayRef(rDataSet, curDataSetList);

	if (!rDataSet.isFullImported())
	    manageMaxMin(rDataSet, curDataSetList);

	setType(globalContext, rDataSet);

	/*
	if (isChipChip())
	    VAMPUtils.setType(rDataSet, VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE);
	else if (isCGH())
	    VAMPUtils.setType(rDataSet, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE);
	else if (isTranscriptome()) {
	    rDataSet.setAutoY(true); // 4/02/05
	    VAMPUtils.setType(rDataSet, VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE);
	    SystemConfig sysCfg = (SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);
	    if (VAMPUtils.isMergeChr(rDataSet)) {
		String url = rDataSet.fromTemplate(sysCfg.getTranscriptomeChrMergeURLTemplate());
		rDataSet.setURL(url);
	    }
	}
	else if (isSNP())
	    VAMPUtils.setType(rDataSet, VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE);
	else if (isLOH())
	    VAMPUtils.setType(rDataSet, VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE);
	else if (isDifferentialAnalysis())
	    VAMPUtils.setType(rDataSet, VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE);
	else if (isGTCAAnalysis())
	    VAMPUtils.setType(rDataSet, VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE);
	else if (isGenomeAnnot()) {
	    VAMPUtils.setType(rDataSet, VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE);
	    rDataSet.setGraphElementDisplayer
		(new GenomeAnnotDataSetDisplayer());
	}
	*/

	rDataSet.setPropertyValue(VAMPProperties.URLMapProp, url_map);
    }

    private void setType(GlobalContext globalContext, GraphElement graphElement) {
	if (isChipChip())
	    VAMPUtils.setType(graphElement, VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE);
	else if (isCGH())
	    VAMPUtils.setType(graphElement, VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE);
	else if (isTranscriptome()) {
	    graphElement.setAutoY(true); // 4/02/05
	    VAMPUtils.setType(graphElement, VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE);
	    SystemConfig sysCfg = (SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);
	    if (VAMPUtils.isMergeChr(graphElement) && sysCfg.getTranscriptomeChrMergeURLTemplate() != null) {
		String url = graphElement.fromTemplate(sysCfg.getTranscriptomeChrMergeURLTemplate());
		graphElement.setURL(url);
	    }
	}
	else if (isSNP())
	    VAMPUtils.setType(graphElement, VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE);
	else if (isLOH())
	    VAMPUtils.setType(graphElement, VAMPConstants.LOH_CHROMOSOME_MERGE_TYPE);
	else if (isDifferentialAnalysis())
	    VAMPUtils.setType(graphElement, VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE);
	else if (isGTCAAnalysis())
	    VAMPUtils.setType(graphElement, VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE);
	else if (isGenomeAnnot()) {
	    VAMPUtils.setType(graphElement, VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE);
	    graphElement.setGraphElementDisplayer
		(new GenomeAnnotDataSetDisplayer());
	}
    }

    private void manageMaxMin(DataSet rDataSet,
			      LinkedList curDataSetList) {
	int sz = curDataSetList.size();
	for (int n = 0; n < sz; n++) {
	    DataSet dset = (DataSet)curDataSetList.get(n);
	    rDataSet.setMinMaxX(dset.getLMinX());
	    rDataSet.setMinMaxX(dset.getLMaxX());
	    rDataSet.setMinMaxY(dset.getLMinY());
	    rDataSet.setMinMaxY(dset.getLMaxY());
	}	

	// test
	/*
	rDataSet.setPropertyValue(Property.getProperty("XXX"),
				  Utils.listToVector(curDataSetList));
	*/
    }

    private void manageArrayRef(DataSet rDataSet,
				LinkedList curDataSetList) {
	int sz = curDataSetList.size();
	if (sz == 0)
	    return;
	Vector v = new Vector();
	for (int n = 0; n < sz; n++) {
	    DataSet dset = (DataSet)curDataSetList.get(n);
	    DataSet array_ref = (DataSet)dset.getPropertyValue(VAMPProperties.ArrayRefProp);
	    if (array_ref == null)
		continue;
	    v.add(array_ref);
	}
	
	if (v.size() == 0)
	    return;

	GraphElementListOperation op =
	    GraphElementListOperation.get(MergeChrOP.CGH_NAME);
	
	Vector rv = op.apply(null, null, v, null, false);
	DataSet mdset = rv.size() == 1 ? (DataSet)rv.get(0) : null;

	//System.out.println("mdset = " + mdset);
	if (mdset == null)
	    rDataSet.removeProperty(VAMPProperties.ArrayRefProp);
	else
	    rDataSet.setPropertyValue(VAMPProperties.ArrayRefProp, mdset);
    }

    static boolean areAllMerged(Vector graphElements) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    /*
	    DataSet dataSet = ((GraphElement)graphElements.get(n)).asDataSet();
	    if (dataSet == null)
		return false;
	    if (!VAMPUtils.isMergeChr(dataSet))
		return false;
	    */
	    if (!VAMPUtils.isMergeChr((GraphElement)graphElements.get(n))) {
		return false;
	    }
	    /*
	      if (!dataSet.getPropertyValue(VAMPConstants.TypeProp).equals
	      (VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE) &&
	      !dataSet.getPropertyValue(VAMPConstants.TypeProp).equals
	      (VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
	      return false;
	    */
	}

	return true;
    }

    public boolean mustInitScale() {return true;}

    static boolean isInteger(String s) {
	return s.matches("[0-9]+");
    }

    // Attempt to introduces ranges into the chrList to avoid having
    // a long list of chromosomes.
    // does not work !
    static String makeChrList(String chrList) {
	String chrs[] = chrList.split(",");
	if (chrs.length == 1) return chrs[0];

	int startChr = isInteger(chrs[0]) ? Utils.parseInt(chrs[0]) : 0;

	String lastChr = chrs[0];
	chrList = lastChr;

	for (int n = 1; n < chrs.length; n++) {
	    if (startChr > 0 && isInteger(chrs[n]) && isInteger(lastChr)) {
		int lchr = Utils.parseInt(lastChr);
		int chr = Utils.parseInt(chrs[n]);
		if (chr - lchr != 1 || n == chrs.length-1) {
		    chrList += "-" + chrs[n-1];
		    startChr = chr;
		}
	    }
	    else {
		if (startChr > 0) {
		    if (n > 1)
			chrList += "-" + chrs[n-1];
		}

		chrList += "," + chrs[n];
		startChr = 0;
	    }

	    lastChr = chrs[n];
	}

	return chrList;
    }

    //public boolean mayApplyOnLightImportedProfiles() {return light;}
    public boolean mayApplyOnLightImportedProfiles() {return true;}

    /*
      private String makeSourceURL(DataSet dataSet) {
      String srcUrl = dataSet.getSourceURL();
      if (srcUrl.indexOf(VAMPConstants.ARRAY_DIR) >= 0)
      return srcUrl;

      if (srcUrl.indexOf(VAMPConstants.ALL_DIR) >= 0) {
      System.err.println("***** WARNING ***** this is just a test for the Pic Sein project");
      srcUrl = srcUrl.replaceAll(VAMPConstants.ALL_DIR + "chr../", "/SEIN" + VAMPConstants.ARRAY_DIR);
      return srcUrl;
      }

      return null;
      }
    */

    public boolean supportProfiles() {return true;}
}

