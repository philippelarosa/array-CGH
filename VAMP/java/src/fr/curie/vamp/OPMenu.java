
/*
 *
 * OPMenu.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.util.*;

public class OPMenu {

    private static boolean init = false;
    public static Menu toolMenu;

    private static void addTools(HashMap menuMap, SystemConfig sysCfg) {
	Vector tools = sysCfg.getTools();
	int sz = tools.size();

	for (int n = 0; n < sz; n++) {
	    SystemConfig.Tool tool = (SystemConfig.Tool)tools.get(n);
	    Menu menu = (Menu)menuMap.get(tool.container);
	    if (menu != null)
		continue;

	    menu = new Menu(tool.container);
	    toolMenu.add(menu);
	    menuMap.put(menu.getName(), menu);
	    addTools(menu, sysCfg);
	}
    }

    private static void addTools(Menu menu, SystemConfig sysCfg) {
	Vector tools = sysCfg.getTools(menu.getName());
	int sz = tools.size();
	for (int n = 0; n < sz; n++) {
	    SystemConfig.Tool tool = (SystemConfig.Tool)tools.get(n);
	    menu.add(new ToolMenuItem(new URLOP(tool.type,
						tool.name,
						tool.url,
						tool.target,
						tool.isOnAll()),
				      tool.title));
	}
    }

    static void init(GlobalContext globalContext) {

	if (init)
	    return;

	init = true;

	SystemConfig sysCfg = (SystemConfig)globalContext.
	    get(SystemConfig.SYSTEM_CONFIG);
	
	toolMenu = new Menu("");
	Menu cghMenu = new Menu("CGH");
	Menu snpMenu = new Menu("Affy-SNPs");
	Menu trsMenu = new Menu("Transcriptome");
	Menu lohMenu = new Menu("LOH");
	Menu chipMenu = new Menu("ChIP-chip");

	Menu clustMenu = new Menu("Clustering", true);
	Menu bkpFreqMenu = new Menu("Breakpoint Frequency");
	Menu cytogRegionMenu = new Menu("Cytogenetic Banding");
	Menu diffAnaMenu = new Menu("Differential Analysis");
	Menu fraglMenu = new Menu("FrAGL");
	Menu genAltMenu = new Menu("Genome Alteration");
        Menu GTCAMenu = new Menu("Correlation Analysis");
	Menu karyoMenu = new Menu("Karyotype Analysis");
	Menu sgnDensMenu = new Menu("Signal Histogram");
	
	Menu genAnnotMenu = new Menu("Genome Annotation", true);
	Menu sampleAnnotMenu = new Menu("Sample Annotations");

	Menu chrSwitchMenu = new Menu("Chromosome Switch", true);
	Menu filterMenu = new Menu("Filter");
	Menu sortMenu = new Menu("Sort by");
	//Menu designMenu = new Menu("Array Designs");
	Menu complImportMenu = new Menu("Complete Import");
	Menu miscMenu = new Menu("Misc");

	toolMenu.add(cghMenu);
	toolMenu.add(snpMenu);
	toolMenu.add(trsMenu);
	toolMenu.add(lohMenu);
	toolMenu.add(chipMenu);

	toolMenu.add(clustMenu);
	toolMenu.add(bkpFreqMenu);
	toolMenu.add(cytogRegionMenu);
	// DISCONNECT THE FOLLOWING LINE TO SUPPRESS DIFF_ANA
	toolMenu.add(diffAnaMenu);
	toolMenu.add(fraglMenu);
	toolMenu.add(genAltMenu);
	toolMenu.add(GTCAMenu);
	toolMenu.add(karyoMenu);
	toolMenu.add(sgnDensMenu);

	toolMenu.add(genAnnotMenu);
	toolMenu.add(sampleAnnotMenu);

	toolMenu.add(chrSwitchMenu);
	toolMenu.add(filterMenu);
	toolMenu.add(sortMenu);
	//toolMenu.add(designMenu);
	toolMenu.add(complImportMenu);
	toolMenu.add(miscMenu);

	cghMenu.add(new ToolMenuItem(new AverageOP(GraphElementListOperation.CGH_TYPE), "Average"));
	cghMenu.add(new ToolMenuItem(new UnaverageOP(GraphElementListOperation.CGH_TYPE), "Unaverage"));

	cghMenu.add(new ToolMenuItem(new ChangeRatioOP(GraphElementListOperation.CGH_TYPE, false)));
	cghMenu.add(new ToolMenuItem(new ChangeRatioOP(GraphElementListOperation.CGH_TYPE, true)));

	cghMenu.add(new ToolMenuItem(new SplitChrOP(MergeSplitOP.CGH_TYPE),
				     "Split Chromosomes"));
	cghMenu.add(new ToolMenuItem(new MergeChrOP(MergeSplitOP.CGH_TYPE),
				     "Merge Chromosomes"));
	Menu syntenyMenu = new Menu("Synteny");
	syntenyMenu.add(new ToolMenuItem(new SyntenyOP(SyntenyOP.SWITCH),
					 "Synteny Switch"));

	syntenyMenu.add(new ToolMenuItem(new SyntenyOP(0),
					 "Add Synteny"));

	syntenyMenu.add(new ToolMenuItem(new SyntenyOP(SyntenyOP.REGION),
					 "Add Region Synteny"));

	//syntenyMenu.add(new ToolMenuItem(new UnsyntenyOP(), "Unsynteny"));

	cghMenu.add(syntenyMenu);


	trsMenu.add(new ToolMenuItem(new TranscriptomeOP(true), "Load"));
	trsMenu.add(new ToolMenuItem(new TranscriptomeOP(false), "Light Load"));
	trsMenu.add(new ToolMenuItem(new RelatedArrayOP(true)));
	trsMenu.add(new ToolMenuItem(new TranscriptomeAverageOP(), "Average"));
	trsMenu.add(new ToolMenuItem(new TranscriptomeUnaverageOP(), "Unaverage"));
	trsMenu.add(new ToolMenuItem(new TranscriptomeRelOP(0), "Relative"));


	trsMenu.add(new ToolMenuItem(new RelatedTranscriptomeOP(RelatedTranscriptomeOP.RELATED_TRANSCRIPTOME_ABS_OP), "Absolute"));
	trsMenu.add(new ToolMenuItem(new RelatedTranscriptomeOP(RelatedTranscriptomeOP.RELATED_TRANSCRIPTOME_REF_OP), "Reference Array"));

	trsMenu.add(new ToolMenuItem(new SplitChrOP(GraphElementListOperation.TRANSCRIPTOME_TYPE),
				     "Split Chromosomes"));
	trsMenu.add(new ToolMenuItem(new MergeChrOP(GraphElementListOperation.TRANSCRIPTOME_TYPE),
				     "Merge Chromosomes"));
	trsMenu.add(new ToolMenuItem(new SplitTranscriptomeOP(), "Split"));
	trsMenu.add(new ToolMenuItem(new MergeTranscriptomeOP(), "Merge"));
// 	trsMenu.add(new ToolMenuItem(new GTCorrelationAnalysisOP(), "Correlation Analysis"));
	trsMenu.add(new ToolMenuItem(new TCMOP(), "TCM"));

	trsMenu.add(new ToolMenuItem(new ChangeSignalOP(false)));
	trsMenu.add(new ToolMenuItem(new ChangeSignalOP(true)));

	lohMenu.add(new ToolMenuItem(new LOHLoadOP(), "Load"));
	lohMenu.add(new ToolMenuItem(new RelatedArrayOP(false)));

	lohMenu.add(new ToolMenuItem(new SplitChrOP(GraphElementListOperation.LOH_TYPE),
				     "Split Chromosomes"));
	lohMenu.add(new ToolMenuItem(new MergeChrOP(GraphElementListOperation.LOH_TYPE),
				     "Merge Chromosomes"));
	chipMenu.add(new ToolMenuItem(new AverageOP(GraphElementListOperation.CHIP_CHIP_TYPE), "Average"));
	chipMenu.add(new ToolMenuItem(new UnaverageOP(GraphElementListOperation.CHIP_CHIP_TYPE), "Unaverage"));
	chipMenu.add(new ToolMenuItem(new ChangeRatioOP(GraphElementListOperation.CHIP_CHIP_TYPE, false)));
	chipMenu.add(new ToolMenuItem(new ChangeRatioOP(GraphElementListOperation.CHIP_CHIP_TYPE, true)));
	chipMenu.add(new ToolMenuItem(new SplitChrOP(GraphElementListOperation.CHIP_CHIP_TYPE),
				      "Split Chromosomes"));
	chipMenu.add(new ToolMenuItem(new MergeChrOP(GraphElementListOperation.CHIP_CHIP_TYPE),
				      "Merge Chromosomes"));
	

	snpMenu.add(new ToolMenuItem(new AverageOP(GraphElementListOperation.SNP_TYPE), "Average"));
	snpMenu.add(new ToolMenuItem(new UnaverageOP(GraphElementListOperation.SNP_TYPE), "Unaverage"));
	snpMenu.add(new ToolMenuItem(new ChangeRatioOP(GraphElementListOperation.SNP_TYPE, false)));
	snpMenu.add(new ToolMenuItem(new ChangeRatioOP(GraphElementListOperation.SNP_TYPE, true)));

	snpMenu.add(new ToolMenuItem(new SplitChrOP(GraphElementListOperation.SNP_TYPE),
				     "Split Chromosomes"));
	snpMenu.add(new ToolMenuItem(new MergeChrOP(GraphElementListOperation.SNP_TYPE),
				     "Merge Chromosomes"));
	

	genAnnotMenu.add(new ToolMenuItem(new GeneSelectionOP(),
					  "Gene Selection")); // temporary name

	genAnnotMenu.add(new ToolMenuItem(new SplitChrOP(GraphElementListOperation.GENOME_ANNOT_TYPE),
					  "Split Chromosomes"));
	genAnnotMenu.add(new ToolMenuItem(new MergeChrOP(GraphElementListOperation.GENOME_ANNOT_TYPE),
				     "Merge Chromosomes"));

	fraglMenu.add(new ToolMenuItem(new FrAGLOP(), "Compute"));
	fraglMenu.add(new ToolMenuItem(new RelatedArraysOP(false), "Related Arrays"));

	genAltMenu.add(new ToolMenuItem(new GenomeAlterationOP(), "Compute"));

	// for testing
	genAltMenu.add(new ToolMenuItem(new GenomeAlterationOP(true), "Test"));

        // Genome Transcriptome Analysis
        GTCAMenu.add(new ToolMenuItem(new GTCorrelationAnalysisOP(), "Compute"));
	GTCAMenu.add(new ToolMenuItem(new GTCorrelationAnalysisRedisplayOP(), "Redisplay"));
	GTCAMenu.add(new ToolMenuItem(new GTCorrelationAnalysisReportOP(), "Reports"));

        karyoMenu.add(new ToolMenuItem(new KaryoAnalysisOP(), "Compute"));

	clustMenu.add(new ToolMenuItem(new ClusterOP(),
				       "Compute"));

	bkpFreqMenu.add(new ToolMenuItem(new BreakpointFrequencyOP(),
					 "Compute"));

	bkpFreqMenu.add(new ToolMenuItem(new RelatedArraysOP(true),
					 "Related Arrays"));

	sgnDensMenu.add(new ToolMenuItem(new SignalHistogramOP(),
					 "Compute"));

	sgnDensMenu.add(new ToolMenuItem(new RelatedArraysOP(false),
					 "Related Arrays"));

	diffAnaMenu.add(new ToolMenuItem(new DifferentialAnalysisOP(),
					 "Compute"));

	diffAnaMenu.add(new ToolMenuItem(new DifferentialAnalysisRedisplayOP(),
					 "Redisplay"));

	diffAnaMenu.add(new ToolMenuItem(new DifferentialAnalysisReportOP(),
					 "Reports"));

	diffAnaMenu.add(new ToolMenuItem(new MergeChrOP(GraphElementListOperation.DIFFANA_TYPE),
					 "Merge Chromosomes"));

	diffAnaMenu.add(new ToolMenuItem(new SplitChrOP(GraphElementListOperation.DIFFANA_TYPE),
					 "Split Chromosomes"));


	chrSwitchMenu.add(new ToolMenuItem(new ChrSwitchOP(true),
					   "All"));
	chrSwitchMenu.add(new ToolMenuItem(new ChrSwitchOP(false),
					   "Selected"));

	complImportMenu.add(new ToolMenuItem(new CompleteImportOP(),
					     "Import"));

	cytogRegionMenu.add(new ToolMenuItem(new CytogenRegionOP(),
					     "Create"));

	sortMenu.add(new ToolMenuItem(new OrderByNameOP(), "Name"));
	sortMenu.add(new ToolMenuItem(new OrderByNameChrOP(), "Name / Chromosome"));
	sortMenu.add(new ToolMenuItem(new OrderByChrOP(), "Chromosome / Name"));
	sortMenu.add(new ToolMenuItem(new OrderByTypeOP(), "Type / Name"));
	sortMenu.add(new ToolMenuItem(new SortOP
				      ("Clinical Property Sort",
				       VAMPProperties.ClinicalDataProp),
				      "Clinical property"));

	sortMenu.add(new ToolMenuItem(new SortOP
				       ("All Property Sort", null),
				       "Any property"));

	filterMenu.add(new ToolMenuItem(new FilterOP
					("Clinical Property Filter",
					 VAMPProperties.ClinicalDataProp),
					"Clinical property"));

	filterMenu.add(new ToolMenuItem(new FilterOP
					("All Property Filter", null),
					"Any property"));

	sampleAnnotMenu.add(new ToolMenuItem(new AnnotDisplayOP
					      ("Clinical Property Sample Annotations", VAMPProperties.ClinicalDataProp),
					      "Clinical property"));

	sampleAnnotMenu.add(new ToolMenuItem(new AnnotDisplayOP
					      ("All Property Sample Annotations", null),
					      "Any property"));

	new NormalizeOP();
	//designMenu.add(new ToolMenuItem(new NormalizeOP(), "Normalize Array Designs"));
	miscMenu.add(new ToolMenuItem(new CheckConsistencyOP(), "Check Profile Consistency"));

	addTools(cghMenu, sysCfg);
	addTools(trsMenu, sysCfg);
	addTools(lohMenu, sysCfg);
	addTools(chipMenu, sysCfg);
	addTools(snpMenu, sysCfg);
	addTools(sortMenu, sysCfg);
	addTools(filterMenu, sysCfg);
	addTools(sampleAnnotMenu, sysCfg);
	addTools(miscMenu, sysCfg);

	HashMap menuMap = new HashMap();

	menuMap.put(cghMenu.getName(), cghMenu);
	menuMap.put(trsMenu.getName(), trsMenu);
	menuMap.put(lohMenu.getName(), lohMenu);
	menuMap.put(chipMenu.getName(), chipMenu);
	menuMap.put(snpMenu.getName(), snpMenu);
	menuMap.put(sortMenu.getName(), sortMenu);
	menuMap.put(filterMenu.getName(), filterMenu);
	menuMap.put(sampleAnnotMenu.getName(), sampleAnnotMenu);
	menuMap.put(miscMenu.getName(), miscMenu);

	addTools(menuMap, sysCfg);
	
	new ChrAxisOP();

	new SplitArrayOP(GraphElementListOperation.CHIP_CHIP_TYPE);
	new SplitArrayOP(GraphElementListOperation.CGH_TYPE);
	new SplitArrayOP(GraphElementListOperation.FRAGL_TYPE);

	new MergeArrayOP(GraphElementListOperation.CHIP_CHIP_TYPE);
	new MergeArrayOP(GraphElementListOperation.CGH_TYPE);
	new MergeArrayOP(GraphElementListOperation.FRAGL_TYPE);

	new SplitChrOP(GraphElementListOperation.FRAGL_TYPE);

	new TranscriptomeRelOP(TranscriptomeRelOP.NO_ADD_REF);
	new RelatedTranscriptomeOP(RelatedTranscriptomeOP.RELATED_TRANSCRIPTOME_INFO_OP);
    }

    public static Menu createMenu(String name) {
	return createMenu(name, false);
    }

    public static Menu createMenu(String name, boolean sep) {
	Menu menu = new Menu(name, sep);
	toolMenu.add(menu);
	return menu;
    }

    public static Menu createMenu(String name, Menu parentMenu) {
	Menu menu = new Menu(name, false);
	parentMenu.add(menu);
	return menu;
    }

    public static void addToMenu(Menu menu, GraphElementListOperation op, String label) {
	menu.add(new ToolMenuItem(op, label));
    }

    public static void addToMenu(Menu menu, GraphElementListOperation op) {
	menu.add(new ToolMenuItem(op, op.getName()));
    }
}
