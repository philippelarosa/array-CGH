
/*
 *
 * TCMOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.awt.*;

class TCMOP extends GraphElementListOperation {
   
    static final String NAME = "TCM";

    static final String NEIGHBORHOOD_PARAM = "Neighborhood";
    static final String SIGNIFICANCE_PARAM = "Significance";
    static final String RESULT_PARAM = "Result";

    static final int HTML_REPORT = 0x1;
    static final int CSV_REPORT = 0x2;
    static final int PROFILE_DISPLAY = 0x4;
    static final String ALL = "@";
    static final double NO_SCORE = -10;
    static final Property TypePuceProp = Property.getProperty("TypePuce");
    static final Property S0Prop = Property.getProperty("S0");
    static final Property S1Prop = Property.getProperty("S1");
    static final Property PValueProp = Property.getProperty("P-Value");
    static final Property NeighborhoodProp = Property.getProperty("Neighborhood");

    static final Property ProbeSetCountProp = Property.getHiddenProperty("__probeset_count");
    static final Property HScoreProp = Property.getHiddenProperty("__score");
    static final Property HGeneProp = Property.getHiddenProperty("__gene");

public String[] getSupportedInputTypes() {
	return null;
    }

public String getReturnedType() {
	return null;
    }

    TCMOP() {
	super(NAME, SHOW_MENU | ON_ALL_AUTO);
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {

	int size = graphElements.size();
	if (size < 2)
	    return false;

	String curChr = null;
	String genAnnotChr = null;
	String typePuce = null;
	
	for (int m = 0; m < size; m++) {
	    GraphElement graphElem = (GraphElement)graphElements.get(m);
	    String type = VAMPUtils.getType(graphElem);

	    if (type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE)) {
		if (genAnnotChr != null)
		    return false;
		genAnnotChr = ALL;
		continue;
	    }
	    else if (type.equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
		if (genAnnotChr != null)
		    return false;
		genAnnotChr = VAMPUtils.getChr(graphElem);
		continue;
	    }

	    if (type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE)) {
		if (curChr == null)
		    curChr = ALL;
		else if (!curChr.equals(ALL))
		    return false;
	    }
	    else if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE)) {
		if (curChr == null)
		    curChr = VAMPUtils.getChr(graphElem);
		else if (!curChr.equals(VAMPUtils.getChr(graphElem)))
		    return false;
	    }

	    if (typePuce == null)
		typePuce = (String)graphElem.getPropertyValue(TypePuceProp);
	    else if (!typePuce.equals(graphElem.getPropertyValue
				      (TypePuceProp)))
		return false;
	}	    
	
	if (genAnnotChr == null || curChr == null)
	    return false;

	return genAnnotChr.equals(curChr);
    }

    static final HashMap view_map = new HashMap();

public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = TCMDialog.getParams
	    (view, graphElements, getDefaultParams(view, graphElements));
	if (params != null)
	    view_map.put(view, params);
	return params;
    }

public TreeMap getDefaultParams(View view, Vector graphElements) {
	TreeMap params = (TreeMap)view_map.get(view);

	if (params != null)
	    return params;

	params = new TreeMap();

	params.put(NEIGHBORHOOD_PARAM, new Integer(10));
	params.put(SIGNIFICANCE_PARAM, new Double(0.002));

	view_map.put(view, params);
	return params;
    }

    static DataSet makeGeneDataSet(DataSet genAnnot) {
	DataSet geneDS = (DataSet)genAnnot.clone();
	VAMPUtils.setType(geneDS, VAMPUtils.isMergeChr(geneDS) ?
		    VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE :
		    VAMPConstants.TRANSCRIPTOME_TYPE);
	DataElement geneData[] = geneDS.getData();
	for (int n = 0; n < geneData.length; n++) {
	    geneData[n].setPosY(geneDS, 0);
	    geneData[n].setPropertyValue(VAMPProperties.SignalProp, "0");
	    geneData[n].setPropertyValue(ProbeSetCountProp, new Integer(0));
	}
	return geneDS;
    }

    static DataElement getGene(DataSet ds, DataElement data, DataSet geneDS,
			       DataElement geneData[]) {
	int ind = getGeneInd2(ds, data, geneDS, geneData);
	if (ind >= 0)
	    return geneData[ind];
	return null;
    }

    /*
    static void displayGeneData(DataElement geneData[]) {
	for (int n = 0; n < geneData.length; n++) {
	    double gbegin = geneData[n].getPosX();
	    double gend = gbegin + geneData[n].getPosSize();
	    System.out.println(n + " [" + gbegin + ":" + gend + "] " +
			       geneData[n].getID());
	}
    }
    */

    static int getGeneInd(DataSet ds, DataElement data, DataSet geneDS,
			  DataElement geneData[]) {
	double begin = data.getPosX(ds);
	double end = data.getPosX(ds) + data.getPosSize(ds);

	for (int n = 0; n < geneData.length; n++) {
	    if (isIn(ds, begin, end, n, geneDS, geneData)) 
		return n;
	    /*
	    double gbegin = geneData[n].getPosX();
	    double gend = gbegin + geneData[n].getPosSize();
	    
	    if ((begin >= gbegin && begin <= gend) ||
		(end >= gbegin && end <= gend))
		return n;
	    if (gbegin > end)
		return -1;
	    */
	}

	return -1;
    }

    static boolean isIn(DataSet ds, double begin, double end, int n,
			DataSet geneDS, DataElement geneData[]) {
	if (n < 0 || n >= geneData.length)
	    return false;

	double gbegin = geneData[n].getPosX(geneDS);
	double gend = gbegin + geneData[n].getPosSize(geneDS);
	    
	return (begin >= gbegin && begin <= gend) ||
	    (end >= gbegin && end <= gend) ||
	    (gbegin >= begin && gbegin <= end) ||
	    (gend >= begin && gend <= end);
    }

    static int isIn2(DataSet ds, double begin, double end, double d,
		     DataSet geneDS, DataElement geneData[]) {
	double floor = Math.floor(d);
	if (isIn(ds, begin, end, (int)floor, geneDS, geneData))
	    return (int)floor;

	if (floor == d)
	    return -1;

	if (isIn(ds, begin, end, (int)floor+1, geneDS, geneData))
	    return (int)floor+1;
	return -1;
    }

    static int getGeneInd2(DataSet ds, DataElement data,
			   DataSet geneDS, DataElement geneData[]) {
	double begin = data.getPosX(ds);
	double end = data.getPosX(ds) + data.getPosSize(ds);

	int min = 0, max = geneData.length-1;
	double r2 = (double)max/2.;

	for (;;) {
	    int ind = isIn2(ds, begin, end, r2, geneDS, geneData);
	    if (ind >= 0)
		return ind;

	    if (max == min) {
		return getGeneInd(ds, data, geneDS, geneData);
		//return -1;
	    }

	    double floor = Math.floor(r2);
	    double gbegin = geneData[(int)floor].getPosX(geneDS);

	    if (gbegin > end) {
		max = (int)floor;
		r2 = min + (floor - min)/2.;
	    } else {
		if (floor != r2)
		    floor++;
		min = (int)floor;
		r2 = min + (max - floor)/2.;
	    }
	}
    }

    static double[] getXi(int ind, Vector graphElements) {
	int size = graphElements.size();
	double Xi[] = new double[size];
	for (int m = 0; m < size; m++) {
	    DataSet dataSet = (DataSet)graphElements.get(m);
	    DataElement data = dataSet.getData()[ind];
	    Xi[m] = data.getPosY(dataSet);
	}
	return Xi;
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
    if (!mayApplyP(view, panel, graphElements, autoApply))
	    return null;

	int result_mask = ((Integer)params.get(RESULT_PARAM)).intValue();
	if (result_mask == 0)
	    return null;

	boolean isProfileDisplay = ((result_mask & PROFILE_DISPLAY) != 0);
	boolean isHTML = (result_mask & HTML_REPORT) != 0;
	boolean isCSV = (result_mask & CSV_REPORT) != 0;

	try {
	    long ms0 = (new Date()).getTime();
	    Vector nGraphElements = pass1(graphElements, params);
	    DataSet geneDS = (DataSet)nGraphElements.get(0);
	    DataElement geneData[] = geneDS.getData();

	    // parameter
	    int neighborhood = ((Integer)params.get(NEIGHBORHOOD_PARAM)).intValue();
	    double pvalue = ((Double)params.get(SIGNIFICANCE_PARAM)).doubleValue();

	    // should be computed from nb genes
	    //int perm_cnt = (int)Math.max(10, (100 / (geneData.length * pvalue)) + 1);
	    int perm_cnt = 10;
	    
	    System.out.println("Permutation Number: " + perm_cnt);

	    double refScore[] = new double[geneData.length];
	    Random rand = new Random(System.currentTimeMillis());
	    double S[];
	    TreeSet refscore_set = new TreeSet();
	    for (int n = 0; n < geneData.length; n++) {
		double score = computeScore(geneData, n, nGraphElements, neighborhood);
		geneData[n].setPropertyValue(HScoreProp, new Double(score));
		refScore[n] = score;
		refscore_set.add(new Double(score));
	    }

	    /*
	    Iterator it = refscore_set.iterator();
	    for (int i = 0; it.hasNext(); i++) {
		Double d = (Double)it.next();
		System.out.println("refscore: " + d);
	    }
	    */

	    S = computeS(rand, geneData, perm_cnt, geneData.length,
			 neighborhood, nGraphElements, refScore, pvalue);

	    DataSet tcm = (DataSet)((DataSet)graphElements.get(0)).clone();
	    VAMPUtils.setType(tcm,
			VAMPUtils.isMergeChr(tcm) ? VAMPConstants.TCM_CHROMOSOME_MERGE_TYPE :
			VAMPConstants.TCM_TYPE);
	    DataElement tcmData[] = tcm.getData();
	    for (int n = 0; n < tcmData.length; n++) {
		DataElement gene = getGene(tcm, tcmData[n], geneDS, geneData);
		// does not work because geneInd has changed : geneData is
		// not the original, empty genes have been skipped !
		/*
		int geneInd = ((Integer)tcmData[n].getPropertyValue(HGeneProp)).intValue();
		DataElement gene = (geneInd >= 0 ? geneData[geneInd] : null);
		*/
		Double score_d = (gene != null ? (Double)gene.getPropertyValue(HScoreProp) : null);
		double score = (score_d != null ? score_d.doubleValue() : 0);
		tcmData[n].setPosY(tcm, score);
		tcmData[n].setPropertyValue(VAMPProperties.SignalProp, 
					    Utils.performRound(score));
		if (gene != null)
		    tcmData[n].setPropertyValue(VAMPProperties.GeneSymbolProp,
						gene.getPropertyValue(VAMPProperties.GeneSymbolProp));
		
	    }

	    tcm.setAutoY(true);
	    tcm.setPropertyValue(S0Prop, new Double(S[0]));
	    tcm.setPropertyValue(S1Prop, new Double(S[1]));
	    tcm.setPropertyValue(NeighborhoodProp, new Integer(neighborhood));
	    tcm.setPropertyValue(PValueProp, new Double(pvalue));
	    ms0 = (new Date()).getTime() - ms0;
	    System.out.println((ms0/1000) + " seconds");

	    if (isProfileDisplay)
		frameManage(view, panel, tcm);
	    else
		reportManage(graphElements, isHTML, tcm, S[0], S[1]);

	    return graphElements;

	    //return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    System.err.println(e);
	    e.printStackTrace();
	    return null;
	}
    }

    void reportManage(Vector graphElements, boolean isHTML, DataSet tcm, double S0, double S1) throws java.io.FileNotFoundException 
    {
	File file = DialogUtils.openFileChooser(new Frame(), "Save", 0, true);
	
	if (file == null)
	    return;

	String ext = isHTML ? ".html" : ".csv";

	if (!Utils.hasExtension(file.getName(), ext))
	    file = new File(file.getAbsolutePath() + ext);

	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);

	ReportBuilder builder;
	if (isHTML)
	    builder = new HTMLReportBuilder(ps, "TCM");
	else
	    builder = new CSVReportBuilder(ps, "TCM");

	builder.startDocument();
	int size = graphElements.size();
	builder.addTitle3("Transcriptomes (" + (size-1) + ")");
	DataSet genAnnot = GeneSelectionOP.findGenomeAnnot(graphElements);
	for (int m = 0; m < size; m++) {
	    GraphElement graphElem = (GraphElement)graphElements.get(m);
	    if (graphElem != genAnnot) {
		builder.addText(graphElem.getID().toString());
		builder.addVPad();
	    }
	}
	
	builder.addTitle3("Parameters");
	builder.addText("Neighborhood", HTMLReportBuilder.BOLD_STYLE);
	builder.addText(": " + tcm.getPropertyValue(NeighborhoodProp));
	builder.addVPad();
	builder.addText("P-Value", HTMLReportBuilder.BOLD_STYLE);
	builder.addText(": " + tcm.getPropertyValue(PValueProp));
	builder.addVPad();

	Color minColor = Color.GREEN;
	Color maxColor = Color.RED;
	Color ampliconColor = Color.BLUE;

	String minC = "#" + ColorResourceBuilder.RGBString(minColor.getRGB());
	String maxC = "#" + ColorResourceBuilder.RGBString(maxColor.getRGB());
	String ampliconC = "#" + ColorResourceBuilder.RGBString(ampliconColor.getRGB());
	builder.addTitle3("Scores");
	builder.addText("S0", HTMLReportBuilder.BOLD_STYLE);
	builder.addText(":");
	builder.addText("score", "style='color: " + minC + ";'");
	builder.addText(" < " + S0);
	builder.addVPad();
	builder.addText("S1", HTMLReportBuilder.BOLD_STYLE);
	builder.addText(":");
	builder.addText("score", "style='color: " + maxC + ";'");
	builder.addText(" > " + S1);
	builder.addVPad(2);
	
	builder.startTable(new String[]{"ProbeSet", "Gene", "Score"});
	DataElement data[] = tcm.getData();
	
	for (int n = 0; n < data.length; n++) {
	    String color;
	    if (data[n].getPosY(tcm) == 0.0)
		color = ampliconC;
	    else if (data[n].getPosY(tcm) < S0)
		color = minC;
	    else if (data[n].getPosY(tcm) > S1)
		color = maxC;
	    else
		color = null;
	    
	    String attrs = color == null ? "" : ("style='color: " + color +
						 ";'");
	    
	    builder.startRow(attrs);
	    builder.addCell(data[n].getID().toString());
	    String geneSymbol = (String)data[n].getPropertyValue(VAMPProperties.GeneSymbolProp);
	    if (geneSymbol != null)
		builder.addCell(geneSymbol);
	    else
		builder.addEmptyCell();
	    
	    builder.addCell(Utils.performRound(data[n].getPosY(tcm)));
	    builder.endRow();
	}
	
	builder.endTable();
	builder.endDocument();
    }
    
    void frameManage(View view, GraphPanel panel, DataSet tcm) {
	Vector rGraphElements = new Vector();
	rGraphElements.add(tcm);

	PanelProfile panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer(false),
	     (VAMPUtils.isMergeChr(tcm) ? Config.defaultChromosomeNameAxisDisplayer :
	      Config.defaultGenomicPositionAxisDisplayer),
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	    
	PanelProfile panelProfiles[] = new PanelProfile[]{panelProfile};
	    
	GraphPanelSet panelSet = view.getGraphPanelSet();
	ViewFrame vf = new ViewFrame(view.getGlobalContext(),
				     view.getName(),
				     panelProfiles,
				     null,
				     null,
				     null, null,
				     new LinkedList(),
				     Config.defaultDim,
				     null);
	LinkedList list = Utils.vectorToList(rGraphElements);
	vf.getView().getGraphPanelSet().getPanel(panel.getWhich()).setGraphElements(list);
	vf.setVisible(true);
	vf.getView().syncGraphElements();
    }

    static double[] computeS(Random rand, DataElement refData[], int perm_cnt,
			     int size, int neighborhood,
			     Vector nGraphElements, double refScore[],
			     double pvalue) {
	DataElement nRefData[] = new DataElement[size];
	TreeSet score_set = new TreeSet(new ScoreComparator());
	TreeSet diff_score_set = new TreeSet();

	for (int i = 0; i < perm_cnt; i++) {
	    permute(rand, size, nGraphElements);
	    nRefData = ((DataSet)nGraphElements.get(0)).getData();
	    /*
	    for (int n = 0; n < size; n++) {
		System.out.println("G" + refData[n].getID() + " " +
				   refData[n].getPosY() + " :: G" +
				   nRefData[n].getID() + " " + nRefData[n].getPosY());
	    }
	    */
	    int precis = 4;
	    for (int n = 0; n < size; n++) {
		double score = computeScore(nRefData, n,
					    nGraphElements, neighborhood);
		if (score != NO_SCORE) {
		    score_set.add(new Score(score, (String)nRefData[n].getID(), i));
		    diff_score_set.add(new Double(score));
		}
	    }
	}

	Iterator it = score_set.iterator();
	boolean found = false;
	Object scores[] = score_set.toArray();
	int rank = (int)(score_set.size() * (1 - pvalue));
	double S[] = new double[2];

	/*
	for (int i = 0; it.hasNext(); i++) {
	    Object s = (Object)it.next();
	    System.out.println("score: " + s);
	}
	*/

	Score s1 = (Score)scores[scores.length - rank];
	S[0] = s1.score;

	Score s2 = (Score)scores[rank];
	S[1] = s2.score;

	//System.out.println("S0: " + S[0]);
	//System.out.println("S1: " + S[1]);

	return S;
    }

    static double computeScore(DataElement refData[], int n,
			       Vector nGraphElements, int neighborhood) {
	double Xi[] = getXi(n, nGraphElements);
	String refChr = VAMPUtils.getChr(refData[n]);
	double score = 0.;
	int m = 0;
	for (int k = n - neighborhood; k <= n + neighborhood; k++) {
	    if (k == n)
		continue;
	    if (k < 0 || k >= refData.length)
		continue;

	    /*
	    if (!refChr.equals(VAMPUtils.getChr(refData[k])))
		continue;
	    */

	    double Yi[] = getXi(k, nGraphElements);
	    CorrelationInfo correl_info = new CorrelationInfo();
	    for (int i = 0; i < Xi.length; i++) {
		if (Xi[i] != 0 && Yi[i] != 0)
		    correl_info.add(Xi[i], Yi[i]);
	    }

	    if (correl_info.getCorrelSize() > 1) {
		double sp = correl_info.computeSpearman();
		//score += Math.abs(sp);
		//System.out.println("sp: " + sp + (sp < 0 ? " neg" : " pos"));
		score += sp;
		m++;
	    }
	}

	if (m != 0) {
	    score = score/m;
	    //System.out.println("SCORE " + score + " " + m);
	    return score;
	}

	return NO_SCORE;
    }

    static Vector pass1(Vector graphElements, TreeMap params) {
	Vector nGraphElements = new Vector();
	DataSet genAnnot = GeneSelectionOP.findGenomeAnnot(graphElements);
	int size = graphElements.size();

	DataSet ds0 = null;
	for (int m = 0; m < size; m++) {
	    DataSet ds = (DataSet)graphElements.get(m);
	    if (ds == genAnnot)
		continue;
	    if (ds0 == null)
		ds0 = ds;
	    DataSet geneDS = makeGeneDataSet(genAnnot);
	    DataElement geneData[] = geneDS.getData();
	    /*
	    if (ds0 == ds)
		displayGeneData(geneData);
	    */

	    DataElement data[] = ds.getData();
	    for (int n = 0; n < data.length; n++) {
		int geneInd;
		if (ds == ds0) {
		    geneInd = getGeneInd2(ds, data[n], geneDS, geneData);
		    data[n].setPropertyValue(HGeneProp, new Integer(geneInd));
		}
		else
		    geneInd = ((Integer)ds0.getData()[n].getPropertyValue(HGeneProp)).intValue();
		DataElement gene = (geneInd >= 0 ? geneData[geneInd] : null);
		//System.out.println("gene: " + geneInd + " " + geneData);

		if (gene == null)
		    continue;
		gene.setPosY(geneDS, gene.getPosY(geneDS) + data[n].getPosY(ds));
		Integer i = (Integer)gene.getPropertyValue(ProbeSetCountProp);
		Integer ni = new Integer(i.intValue() + 1);
		gene.setPropertyValue(ProbeSetCountProp, ni);
	    }

	    Vector gene_v = new Vector();
	    for (int j = 0; j < geneData.length; j++) {
		Integer i = (Integer)geneData[j].getPropertyValue(ProbeSetCountProp);
		if (i.intValue() != 0) {
		    geneData[j].setPosY(geneDS, geneData[j].getPosY(geneDS)/i.intValue());
		    geneData[j].setPropertyValue(VAMPProperties.SignalProp,
					       Utils.toString(geneData[j].getPosY(geneDS)));
		    gene_v.add(geneData[j]);
		    /*
		    System.out.println("GENE " + geneData[j].getID() + " has " +
				       i + " probesets");
		    */
		}
		else {
		    //System.out.println("GENE " + geneData[j].getID() + " has no probesets");
		    if (geneData[j].getPosY(geneDS) != 0)
			System.err.println("ERROR");
		}
	    }

	    geneData = new DataElement[gene_v.size()];
	    for (int n = 0; n < geneData.length; n++)
		geneData[n] = (DataElement)gene_v.get(n);

	    geneDS.setData(geneData);
	    nGraphElements.add(geneDS);
			
	}
	return nGraphElements;
    }

    static void permute(Random rand, int size, Vector nGraphElements) {
	int perm[] = new int[size];
	for (int i = 0; i < size; i++)
	    perm[i] = -1;
	
	for (int i = 0; i < size; i++) {
	    for (;;) {
		int n_ind = rand.nextInt(size);
		if (perm[n_ind] < 0) {
		    perm[n_ind] = i;
		    break;
		}
	    }		
	}

	for (int i = 0; i < size; i++) {
	    if (perm[i] < 0)
		System.err.println("ERROR: ind " + i);
	}

	int nbP = nGraphElements.size();

	for (int m = 0; m < nbP; m++) {
	    DataSet ds = (DataSet)nGraphElements.get(m);
	    DataElement data[] = ds.getData();
	    DataElement ndata[] = new DataElement[data.length];
	    for (int n = 0; n < data.length; n++) {
		ndata[n] = data[perm[n]];
	    }

	    ds.setData(ndata);
	}
    }

    static class Score {
	double score;
	String id;
	int i;
	Score(double score, String id, int i) {
	    this.score = score;
	    this.id = id;
	    this.i = i;
	}

	public String toString() {
	    return Utils.toString(score);
	}
    }

    static class ScoreComparator implements Comparator {

	public int compare(Object o1, Object o2) {
	    Score s1 = (Score)o1;
	    Score s2 = (Score)o2;

	    if (s1.score < s2.score)
		return -1;

	    if (s1.score > s2.score)
		return 1;

	    int r = s1.id.compareTo(s2.id);
	    if (r != 0)
		return r;

	    if (s1.i < s2.i)
		return -1;

	    if (s1.i > s2.i)
		return 1;

	    System.out.println("OUPS !");
	    return 0;
	}
    }
}
