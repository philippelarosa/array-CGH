
/*
 *
 * GeneSelectionOP.java
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

class GeneSelectionOP extends GraphElementListOperation {
   
    static final String NAME = "Genome Selection";

    private static int rgen_annot_cnt = 1;

    static final String SCOPE_PARAM = "Scope";
    static final String OVERLAP_PARAM = "Overlap";
    //static final String ALT_TYPE_PARAM = "AltType";
    static final String RESULT_PARAM = "Result";

    static final String ALL = "All";
    static final String REGIONS = "Regions";
    static final String SEL_REGIONS = "SelRegions";

    static final String BYPASS = "Bypass";
    static final String OVERLAP = "Overlap";
    static final String NOOVERLAP = "NoOverlap";

    static final String ANY_ALT = "Any";
    static final String ALTERED_ALT = "Altered";
    static final String GAINED_ALT = "Gained";
    static final String LOST_ALT = "Lost";

    static final int HTML_REPORT = 0x1;
    static final int CSV_REPORT = 0x2; // ??
    static final int PROFILE_DISPLAY = 0x4;

    static final Property RelBeginProp = Property.getHiddenProperty("RelBegin");
    static final Property RelEndProp = Property.getHiddenProperty("RelEnd");

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return VAMPConstants.GENOME_ANNOT_TYPE;
    }

    GeneSelectionOP() {
	super(NAME, SHOW_MENU);
    }

    public boolean mayApplyP(View view,  GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	int size = graphElements.size();
	if (size == 1) {
	    GraphElement graphElem = (GraphElement)graphElements.get(0);
	    String type = VAMPUtils.getType(graphElem);
	    if (!type.equals(VAMPConstants.GENOME_ANNOT_TYPE) &&
		!type.equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE))
		return false;
	    graphElements = Utils.listToVector(panel.getGraphElements());
	    size = graphElements.size();
	}
	else if (size != 2) {
	    return false;
	}

	boolean geno_chr_annot = false;
	boolean geno_merge_chr_annot = false;
	int merge_chr_cnt = 0;
	int chr_cnt = 0;
	String chr = null;

	for (int m = 0; m < size; m++) {
	    DataSet ds = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (ds == null)
		return false;

	    if (VAMPUtils.getType(ds).equals(VAMPConstants.GENOME_ANNOT_TYPE)) {
		if (geno_chr_annot || geno_merge_chr_annot)
		    return false;
		if (chr != null && !VAMPUtils.getNormChr(ds).equals(chr))
		    return false;
		chr = VAMPUtils.getNormChr(ds);
		geno_chr_annot = true;
	    }
	    else if (VAMPUtils.getType(ds).equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE)) {
		if (geno_chr_annot || geno_merge_chr_annot)
		    return false;
		geno_merge_chr_annot = true;
	    }
	    else if (VAMPUtils.isMergeChr(ds)) {
		if (chr_cnt > 0)
		    return false;
		merge_chr_cnt++;
	    }
	    else {
		if (merge_chr_cnt > 0)
		    return false;
		if (chr != null && !VAMPUtils.getNormChr(ds).equals(chr))
		    return false;
		chr = VAMPUtils.getNormChr(ds);
		chr_cnt++;
	    }
	}

	/*
	  return (geno_chr_annot && chr_cnt > 0) ||
	  (geno_merge_chr_annot && merge_chr_cnt > 0);
	*/
	return geno_chr_annot || geno_merge_chr_annot;
    }

    static final HashMap view_map = new HashMap();

    public TreeMap getParams(View view, Vector graphElements) {
	TreeMap params = GeneSelectionDialog.getParams
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
	params.put(OVERLAP_PARAM, BYPASS);
	params.put(SCOPE_PARAM, REGIONS);
	//params.put(ALT_TYPE_PARAM, ANY_ALT);
	params.put(RESULT_PARAM, new Integer(PROFILE_DISPLAY));

	view_map.put(view, params);
	return params;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector oriGraphElements, TreeMap params,
			boolean autoApply) {
	if (!mayApplyP(view, panel, oriGraphElements, autoApply))
	    return null;

	try {
	    boolean single;
	    int size = oriGraphElements.size();
	    Vector graphElements;
	    if (size == 1) {
		graphElements = Utils.listToVector(panel.getGraphElements());
		single = true;
	    }
	    else {
		single = false;
		graphElements = oriGraphElements;
	    }

	    DataSet genAnnot = findGenomeAnnot(graphElements);
	    DataSet refDataSet = findNotGenomeAnnot(graphElements);
	    graphElements = new Vector();
	    graphElements.add(refDataSet);
	    graphElements.add(genAnnot);
	    size = graphElements.size();

	    boolean overlap = params.get(OVERLAP_PARAM).equals(OVERLAP);
	    boolean bypass = params.get(OVERLAP_PARAM).equals(BYPASS);
	    boolean sel_region_scope = params.get(SCOPE_PARAM).equals(SEL_REGIONS);
	    boolean region_scope = sel_region_scope || params.get(SCOPE_PARAM).equals(REGIONS);
	    int result = ((Integer)params.get(RESULT_PARAM)).intValue();
	    boolean dspProfile = (result & PROFILE_DISPLAY) != 0;


	    String desc = "Consider: ";
	    if (bypass)
		desc += ": All Genes\n";
	    else if (overlap)
		desc += ": Overlap\n";
	    else
		desc += ": No Overlap\n";

	    desc += "From: ";
	    Vector out_reg_v = null;
	    if (region_scope) {
		// recompute new regions:
		// - add property: chr
		// - add relative positions
		Cytoband cytoband = MiniMapDataFactory.getCytoband
		    (view.getGlobalContext(), VAMPUtils.getOS
		     ((GraphElement)graphElements.get(0)));

		out_reg_v = splitRegions(cytoband, panel, sel_region_scope);

		/*
		int sz = panel.getRegions().size();
		*/
		int sz = out_reg_v.size();
		desc += "Regions (" + sz + ")\n";
		for (int n = 0; n < sz; n++) {
		    //Region region = (Region)panel.getRegions().get(n);
		    /*
		    Region region = (Region)out_reg_v.get(n);
		    desc += "  " + (long)region.getBegin().getPosX() +
			":" + (long)region.getEnd().getPosX() + "\n";
		    */
		    desc += "   " + getRegionTitle(out_reg_v, n) + "\n";
		}
	    }
	    else
		desc += "All\n";

	    if (single) {
		desc += "No Array Selected";
	    }
	    else {
		desc += "Array:\n";

		for (int m = 0; m < size; m++) {
		    DataSet ds = (DataSet)graphElements.get(m);
		    if (ds == genAnnot)
			continue;
		    
		    desc += ds.getID() + "\n";
		}
	    }

	    Vector data_v = new Vector();

	    //DataSet refDataSet = (DataSet)graphElements.get(0);
	    DataElement genAnnotData[] = genAnnot.getData();

	    DataSet rgenAnnot = new DataSet();
	    for (int j = 0; j < genAnnotData.length; j++) {
		if (isEligible(out_reg_v, genAnnotData[j], genAnnot, region_scope) < 0)
		    continue;

		if (bypass) {
		    DataElement gn = (DataElement)genAnnotData[j].clone();
		    gn.copyPos(rgenAnnot, genAnnotData[j], genAnnot);
		    data_v.add(gn);
		    continue;
		}

		boolean found = false;

		double gabeginx = genAnnotData[j].getPosX(genAnnot);
		double gaendx = gabeginx + genAnnotData[j].getPosSize(genAnnot);

		TreeSet probe_v = (overlap ? new TreeSet(new IDComparator()) : null);

		for (int m = 0; m < size; m++) {
		    DataSet ds = (DataSet)graphElements.get(m);
		    if (ds == genAnnot)
			continue;

		    DataElement data[] = ds.getData();

		    for (int i = 0; i < data.length; i++) {
			if (isEligible(out_reg_v, data[i], refDataSet, region_scope) < 0)
			    continue;

			double beginx = data[i].getPosX(ds);
			double endx = beginx + data[i].getPosSize(ds);

			if ((beginx >= gabeginx && beginx <= gaendx) ||
			    (endx >= gabeginx && endx <= gaendx) ||
			    (gabeginx >= beginx && gabeginx <= endx) ||
			    (gaendx >= beginx && gaendx <= endx)) {
			    found = true;
			    if (overlap)
				probe_v.add(data[i]);
			    else
				break;
			}

			if (beginx > gaendx)
			    break;
		    }

		    if (found) {
			if (overlap || bypass) {
			    DataElement gn = (DataElement)genAnnotData[j].clone();
			    gn.copyPos(rgenAnnot, genAnnotData[j], genAnnot);
			    if (overlap)
				gn.setPropertyValue(Property.getProperty
						    ("Probes"),
						    makeProbeList(probe_v));
			    data_v.add(gn);
			}
			break;
		    }
		}
		    
		if (!overlap && !found) {
		    //data_v.add(genAnnotData[j]);
		    DataElement gn = (DataElement)genAnnotData[j].clone();
		    gn.copyPos(rgenAnnot, genAnnotData[j], genAnnot);
		    data_v.add(gn);
		}
	    }		

	    int data_sz = data_v.size();
	    DataElement gData[] = new DataElement[data_sz];
	    for (int n = 0; n < data_sz; n++) {
		gData[n] = (DataElement)data_v.get(n);
	    }

	    rgenAnnot.setData(gData);

	    rgenAnnot.cloneProperties(genAnnot);
	    rgenAnnot.setPropertyValue(VAMPProperties.CloneCountProp,
				       new Integer(gData.length));
	    rgenAnnot.setPropertyValue(Property.getProperty("Gene Selection"),
				       desc);
	    rgenAnnot.setPropertyValue
		(VAMPProperties.NameProp,
		 rgenAnnot.getPropertyValue(VAMPProperties.NameProp) + " [" +
		 rgen_annot_cnt++ + "]");

	    rgenAnnot.setGraphElementDisplayer
		(new GenomeAnnotDataSetDisplayer());

	    if (dspProfile) {
		Vector rGraphElements = new Vector();
		rGraphElements.addAll(oriGraphElements);
		rGraphElements.add(rgenAnnot);
		return undoManage(panel, rGraphElements);
	    }

	    buildReport(view, panel, out_reg_v, region_scope, rgenAnnot,
			graphElements, result, single);

	    return oriGraphElements;
	}
	catch(Exception e) {
	    System.err.println(e);
	    e.printStackTrace();
	    return null;
	}
    }

    Vector buildReport(View view, GraphPanel panel, Vector out_reg_v,
		       boolean region_scope,
		       DataSet rgenAnnot, Vector graphElements, int result,
		       boolean single)
	throws Exception {
	File file = DialogUtils.openFileChooser(new Frame(), "Save",
						DialogUtils.HTML_FILE_FILTER, true);
	if (file == null)
	    return graphElements;

	boolean isHTML = (result == HTML_REPORT);
	String ext = (isHTML ? ".html" : ".csv");
	
	if (!Utils.hasExtension(file.getName(), ext))
	    file = new File(file.getAbsolutePath() + ext);

	FileOutputStream os = new FileOutputStream(file);
	PrintStream ps = new PrintStream(os);

	int size = graphElements.size();
	DataSet genAnnot = findGenomeAnnot(graphElements);
	DataSet refDS = null;
	for (int n = 0; n < size; n++) {
	    if (graphElements.get(n) != genAnnot) {
		refDS = (DataSet)graphElements.get(n);
		break;
	    }
	}


	ReportBuilder builder;
	if (isHTML)
	    builder = new HTMLReportBuilder(ps, "Gene Selection");
	else
	    builder = new CSVReportBuilder(ps, "Gene Selection");

	builder.startDocument();

	String s = ((String)rgenAnnot.getPropertyValue(Property.getProperty("Gene Selection"))).replaceAll("\n", "<br>").replaceAll(" ", "&nbsp;");
	builder.addTitle2(s);
	builder.addVPad(2);
	builder.startTable();

	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().get(SystemConfig.SYSTEM_CONFIG);

	if (refDS != null) {

	    DataElement data[] = refDS.getData();
	    DataElement genAnnotData[] = rgenAnnot.getData();

	    String probeType = (data.length > 0 ?
				VAMPUtils.getType(data[0]) : "Clone");

	    String probeColumns[];

	    if (single) {
		probeColumns =
		    sysCfg.getGeneSelectionNoArrayProbeColumns(probeType);

		if (probeColumns == null) {
		    InfoDialog.pop(view.getGlobalContext(),
				   "GeneSelectionNoArrayProbeColumns is not set " +
				   "for type " + probeType +
				   " in configuration file");
		    return null;
		    
		}
	    }
	    else {
		probeColumns = 
		    sysCfg.getGeneSelectionProbeColumns(probeType);

		if (probeColumns == null) {
		    InfoDialog.pop(view.getGlobalContext(),
				   "GeneSelectionProbeColumns is not set " +
				   "for type " + probeType +
				   " in configuration file");
		    return null;
		    
		}
	    }


	    String genAnnotType = (genAnnotData.length > 0 ?
				   VAMPUtils.getType(genAnnotData[0]) : "Gene");

	    String geneColumns[] =
		sysCfg.getGeneSelectionGeneColumns(genAnnotType);

	    if (geneColumns == null) {
		InfoDialog.pop(view.getGlobalContext(),
			       "GeneSelectionGeneColumns is not set " +
			       "for type " + genAnnotType +
			       " in configuration file");
		return null;

	    }

	    Vector pair_v = new Vector();
	    HashSet gset = new HashSet();

	    for (int i = 0; i < data.length; i++) {
		int regNum = isEligible(out_reg_v, data[i], refDS, region_scope);
		if (regNum < 0)
		    continue;

		double beginx = data[i].getPosX(refDS);
		double endx = beginx + data[i].getPosSize(refDS);

		boolean found = false;
		for (int j = 0; j < genAnnotData.length; j++) {
		    double gabeginx = genAnnotData[j].getPosX(rgenAnnot);
		    double gaendx = gabeginx + genAnnotData[j].getPosSize(rgenAnnot);
		    if ((beginx >= gabeginx && beginx <= gaendx) ||
			(endx >= gabeginx && endx <= gaendx) ||
			(gabeginx >= beginx && gabeginx <= endx) ||
			(gaendx >= beginx && gaendx <= endx)) {
			pair_v.add(new Pair(refDS, data[i], rgenAnnot, genAnnotData[j], regNum));
			gset.add(genAnnotData[j]);
			found = true;
		    }

		    if (gabeginx > endx)
			break;
		}

		if (!found) {
		    pair_v.add(new Pair(refDS, data[i], null, null, regNum));
		}
	    }


	    for (int j = 0; j < genAnnotData.length; j++) {
		if (!gset.contains(genAnnotData[j])) {
		    pair_v.add(new Pair(null, null, rgenAnnot, genAnnotData[j],
					isEligible(out_reg_v, 
						   genAnnotData[j], rgenAnnot,
						   region_scope)));
		}
	    }


	    TreeSet tset = new TreeSet();
	    tset.addAll(pair_v);
	    Iterator it = tset.iterator();

	    int regNum = -1;
	    boolean start = false;
	    while (it.hasNext()) {
		Pair p = (Pair)it.next();

		if (p.regNum != regNum) {
		    regNum = p.regNum;
		    if (region_scope) {
			String rgb = Integer.toHexString(((Region)out_reg_v.get(p.regNum)).getColor().getRGB() & 0xffffff);
			String region_title = getRegionTitle(out_reg_v, regNum);

			builder.startRow();
			builder.addCell(region_title,
					probeColumns.length +
					geneColumns.length,
					" style='font-weight: bold;' align=center bgcolor=#" + rgb);
					
			builder.endRow();
		    }

		    display_title(ps, builder, probeType, genAnnotType,
				  probeColumns, geneColumns);
		}
		else if (start) {
		    display_title(ps, builder, probeType, genAnnotType,
				  probeColumns, geneColumns);
		    start = false;
		}

		builder.startRow();
		display(ps, builder, p.data, probeColumns, refDS);
		display(ps, builder, p.gene, geneColumns, refDS);
		builder.endRow();
	    }

	    builder.endTable();
	    builder.endDocument();

	    ps.close();
	    return graphElements;
	}


	builder.endTable();
	builder.endDocument();
	ps.close();
	return graphElements;
    }

    private void display_title(PrintStream ps, ReportBuilder builder,
			       String probeType, String genAnnotType,
			       String probeColumns[], String geneColumns[]) {
	builder.startRow();
	builder.addCell(probeType, probeColumns.length,
			"align='center' style='font-weight: bold;'");
	builder.addCell(genAnnotType, geneColumns.length,
			"align='center' style='font-weight: bold;'");
	builder.endRow();

	
	builder.startRow();
	for (int n = 0; n < probeColumns.length; n++)
	    builder.addCell(getCol(probeColumns[n]), "style='font-weight: bold;'");

	for (int n = 0; n < geneColumns.length; n++)
	    builder.addCell(getCol(geneColumns[n]), "style='font-weight: bold;'");
	
	builder.endRow();
    }

    static DataSet findGenomeAnnot(Vector graphElements) {
	int size = graphElements.size();
	for (int m = 0; m < size; m++) {
	    DataSet ds = (DataSet)graphElements.get(m);
	    if (VAMPUtils.getType(ds).equals(VAMPConstants.GENOME_ANNOT_TYPE))
		return ds;

	    if (VAMPUtils.getType(ds).equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE))
		return ds;
	}

	return null;
    }

    static DataSet findNotGenomeAnnot(Vector graphElements) {
	int size = graphElements.size();
	for (int m = 0; m < size; m++) {
	    DataSet ds = (DataSet)graphElements.get(m);
	    if (!VAMPUtils.getType(ds).equals(VAMPConstants.GENOME_ANNOT_TYPE) &&
		!VAMPUtils.getType(ds).equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE))
		return ds;
	}

	return null;
    }

    private int isEligible(Vector out_reg_v, DataElement data,
			   GraphElement graphElement,
			   boolean region_scope) {
	if (!region_scope)
	    return 0;

	int size = out_reg_v.size();
	boolean isin = false;
	for (int n = 0; n < size; n++) {
	    Region region = (Region)out_reg_v.get(n);
	    if (data.crossRegion(region, graphElement))
		return n;
	}
	    
	return -1;
    }

    static String makeProbeList(TreeSet probe_v) {
	int sz = probe_v.size();
	String str = "";
	Iterator it = probe_v.iterator();
	for (int n = 0; it.hasNext(); n++) {
	    DataElement probe = (DataElement)it.next();
	    if (n != 0)
		str += "\n";
	    
	    str += probe.getID();
	}

	return str;
    }

    class IDComparator implements Comparator {

	public int compare(Object o1, Object o2) {
	    DataElement d1 = (DataElement)o1;
	    DataElement d2 = (DataElement)o2;
	    return ((String)d1.getID()).compareTo((String)d2.getID());
	}
    }

    static class Pair implements Comparable {
	DataSet dataSet;
	DataElement data;
	DataSet geneSet;
	DataElement gene;
	int regNum;

	Pair(DataSet dataSet, DataElement data,
	     DataSet geneSet, DataElement gene, int regNum) {
	    this.dataSet = dataSet;
	    this.data = data;
	    this.geneSet = geneSet;
	    this.gene = gene;
	    this.regNum = regNum;
	}

	public int compareTo(Object o) {
	    Pair p1 = this;
	    Pair p2 = (Pair)o;

	    double d_beg1, d_beg2, g_beg1, g_beg2;
	    String d_id1, d_id2, g_id1, g_id2;

	    boolean d_set1 = false;

	    if (p1.data != null) {
		d_beg1 = p1.data.getPosX(p1.dataSet);
		d_id1 = (String)p1.data.getPropertyValue(VAMPProperties.NmcProp);
		if (d_id1 == null)
		    d_id1 = (String)p1.data.getID();
		d_set1 = true;
	    }
	    else {
		d_beg1 = 0;
		d_id1 = null;
	    }

	    if (p1.gene != null) {
		g_beg1 = p1.gene.getPosX(p1.geneSet);
		g_id1 = (String)p1.gene.getID();
	    }
	    else {
		g_beg1 = 0;
		g_id1 = null;
	    }

	    boolean d_set2 = false;

	    if (p2.data != null) {
		d_beg2 = p2.data.getPosX(p2.dataSet);
		d_id2 = (String)p2.data.getPropertyValue(VAMPProperties.NmcProp);
		if (d_id2 == null)
		    d_id2 = (String)p2.data.getID();
		d_set2 = true;
	    }
	    else {
		d_beg2 = 0;
		d_id2 = null;
	    }

	    if (p2.gene != null) {
		g_beg2 = p2.gene.getPosX(p2.geneSet);
		g_id2 = (String)p2.gene.getID();
	    }
	    else {
		g_beg2 = 0;
		g_id2 = null;
	    }

	    double beg1, beg2;
	    if (d_set1)
		beg1 = d_beg1;
	    else
		beg1 = g_beg1;

	    if (d_set2)
		beg2 = d_beg2;
	    else
		beg2 = g_beg2;

	    if (beg1 != beg2)
		return (beg1 - beg2) > 0 ? 1 : -1;

	    if (d_beg1 != 0 && d_beg2 != 0 && d_beg1 != d_beg2)
		return (d_beg1 - d_beg2) > 0 ? 1 : -1;

	    if (g_beg1 != 0 && g_beg2 != 0 && g_beg1 != g_beg2)
		return (g_beg1 - g_beg2) > 0 ? 1 : -1;

	    if (d_id1 != null && d_id2 != null && !d_id1.equals(d_id2))
		return d_id1.compareTo(d_id2);

	    if (g_id1 != null && g_id2 != null && !g_id1.equals(g_id2))
		return g_id1.compareTo(g_id2);

	    /*
	      System.out.println("OUPS: " + d_id1 + " : " + d_id2 + " : " +
	      g_id1 + " : " + g_id2);
	      if (p1.data != null)
	      System.out.println("NMC1: " + p1.data.getPropertyValue(VAMPProperties.NmcProp));
	      if (p2.data != null)
	      System.out.println("NMC2: " + p2.data.getPropertyValue(VAMPProperties.NmcProp));
	    */

	    return 0;
	}
    }

    void display(PrintStream ps, ReportBuilder builder,
		 DataElement data, String columns[], DataSet refDS) {

	if (data == null) {
	    for (int n = 0; n < columns.length; n++)
		builder.addEmptyCell();
	    return;
	}

	for (int n = 0; n < columns.length; n++) {
	    String href[] = columns[n].split("::");

	    Object value = data.getPropertyValue(Property.getProperty(href[0]));
	    String rgb = "";

	    if (href[0].equals("Ratio")) {
		if (value == null)
		    value = "NA";
		else {
		    double ratio = Double.parseDouble((String)value);
		    ColorCodes cc = VAMPUtils.getColorCodes(refDS);
		    Color color = cc.getColor(ratio);
		    rgb = "bgcolor=#" + Integer.toHexString(color.getRGB() & 0xffffff);
		    value = Utils.performRound(ratio, 4);
		}
	    }


	    if (href.length == 1 || !(builder instanceof HTMLReportBuilder))
		builder.addCell((String)value, rgb);
	    else
		builder.addCell("<a href='" + data.fromTemplate(href[1])
				+ "'>" + value + "</a>", rgb);

	}
    }

    static String getCol(String column) {
	String href[] = column.split("::");
	return href[0];
    }

    private String getRegionTitle(Vector out_reg_v, int regNum) {
	Region region = (Region)out_reg_v.get(regNum);
	return "Region Chr " +
	    region.getPropertyValue(VAMPProperties.ChromosomeProp) + " [" + region.getPropertyValue(RelBeginProp) +
	    ":" + region.getPropertyValue(RelEndProp) + "]";
    }


    static class DisplayHints {
	String start, sep, end, blank;
	String s_b, e_b;
	boolean isHTML;

	DisplayHints(boolean isHTML) {
	    this.isHTML = isHTML;
	    if (isHTML) {
		start = "<tr><td>";
		sep = "</td><td>";
		end = "</td></tr>\n";
		s_b = "<b>";
		e_b = "</b>";
		blank = "&nbsp";
		return;
	    }
	    else {
		start = "";
		sep = ",";
		end = "\n";
		s_b = "";
		e_b = "";
		blank = " ";
	    }
	}

	String getSep(String attrs) {
	    if (!isHTML)
		return sep;
	    return "</td><td " + attrs + ">";
	}
    }


    Vector splitRegions(Cytoband cytoband, GraphPanel panel, boolean sel_region_scope) {
	Vector out_reg_v = new Vector();

	int size = panel.getRegions().size();
	Vector chrV = cytoband.getChrV();
	int chrv_size = chrV.size();

	for (int n = 0; n < size; n++) {
	    Region region = (Region)panel.getRegions().get(n);
	    if (sel_region_scope && !region.isSelected())
		continue;
	    long begin = (long)region.getBegin().getPosX();
	    long end = (long)region.getEnd().getPosX();

	    Chromosome beg_chr = cytoband.getChromosome(begin);
	    Chromosome end_chr = cytoband.getChromosome(end);

	    boolean state = false;

	    for (int m = 0; m < chrv_size; m++) {
		Chromosome chr = (Chromosome)chrV.get(m);
		long chr_begin = chr.getBegin();
		long chr_end = chr.getEnd();
		long chr_off = chr.getOffsetPos();

		if (chr == end_chr) {
		    chr_end = end - chr_off;
		}

		if (chr == beg_chr) {
		    chr_begin = begin - chr_off;
		    state = true;
		}
		else if (!state) {
		    continue;
		}

		Region reg = new Region(new Mark(chr_begin + chr_off), new Mark(chr_end + chr_off));
		reg.setColor(region.getColor());
		reg.setPropertyValue(VAMPProperties.ChromosomeProp, chr.getName());
		reg.setPropertyValue(RelBeginProp, new Long(chr_begin));
		reg.setPropertyValue(RelEndProp, new Long(chr_end));

		reg.setSelected(region.isSelected(), null);
		out_reg_v.add(reg);
		if (chr == end_chr)
		    break;
	    }
	}

	/*
	for (int n = 0; n < out_reg_v.size(); n++) {
	    Region region = (Region)out_reg_v.get(n);
	    System.out.println("REGION " + (long)region.getBegin().getPosX() + 
			       " " + (long)region.getEnd().getPosX() + 
			       " chr: " + region.getPropertyValue(VAMPProperties.ChromosomeProp) +
			       " relbegin: " + region.getPropertyValue(RelBeginProp) +
			       " relend: " + region.getPropertyValue(RelEndProp));
	}
	*/

	return out_reg_v;
    }

    public boolean useThread() {
	return true;
    }
}
