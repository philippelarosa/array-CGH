
/*
 *
 * Config.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.print.*;
import java.awt.geom.*;

public class Config {

    public static final boolean NEW_CURRENT_VIEW_MENU = true;
    public static final boolean DEFAULT_MODAL = true;
    public static final String SIGNAL_HISTOGRAM_AXIS = "Signal Histogram";

    public static DataSetIDChrBuilder dataSetIDChrBuilder =
	DataSetIDChrBuilder.getInstance();
    public static DataSetIDChrArrayBuilder dataSetIDChrArrayBuilder =
	DataSetIDChrArrayBuilder.getInstance();
    public static DataSetIDArrayBuilder dataSetIDArrayBuilder =
	DataSetIDArrayBuilder.getInstance();

    public static AxisDisplayer defaultGenomicPositionAxisDisplayer;
    public static AxisDisplayer defaultChromosomeNameAxisDisplayer;
    public static AxisDisplayer defaultTranscriptomeAxisDisplayer;
    public static AxisDisplayer defaultTranscriptomeChrMergeAxisDisplayer;
    public static AxisDisplayer defaultTranscriptomeReferenceAxisDisplayer;
    public static AxisDisplayer defaultTranscriptomeRelAxisDisplayer;

    public static Menu newViewMenu;
    public static Menu currentViewMenu_std;
    public static Menu currentViewMenu_std_double;
    public static Menu currentViewMenu_dotplot_double;
    public static Menu currentViewMenu_all_disable;

    public static Margins defaultMargins;

    public static int defaultAxisSizes[];
    public static Dimension defaultDim;
    public static ZoomTemplate defaultZoomTemplate;
    public static ZoomTemplate listZoomTemplate;
    public static ZoomTemplate dotPlotZoomTemplate;
    public static ZoomTemplate karyoZoomTemplate;

    public static PanelProfile karyoPanelProfiles[];
    public static Dimension karyoDim;
    public static int karyoAxisSizes[];

    public static Paper paperA4, paperLetter;

    static {
	defaultGenomicPositionAxisDisplayer =
	    new GenomicPositionAxisDisplayer
	    ("Array",
	     1000., 0.1, false,
	     DataSetIDArrayBuilder.getInstance());

	defaultChromosomeNameAxisDisplayer =
	    new ChromosomeNameAxisDisplayer
	    ("Array", 1., 0.1, false,
	     DataSetIDArrayBuilder.getInstance());

	GraphElementIDBuilder transIDBuilder =
	    DataSetIDTranscriptomeBuilder.getInstance();

	defaultTranscriptomeAxisDisplayer =
	    new GenomicPositionAxisDisplayer
	    ("Transcriptome", 1000., 0.01, false,
	     transIDBuilder,
	     GenomicPositionAxisDisplayer.IS_TRANS);

	defaultTranscriptomeChrMergeAxisDisplayer =
	    new ChromosomeNameAxisDisplayer
	    ("Transcriptome",
	     1., 0.1, false,
	     transIDBuilder);

	defaultTranscriptomeReferenceAxisDisplayer =
	    new GenomicPositionAxisDisplayer
	    ("Transcriptome Reference",
	     1000., 0.01, false,
	     transIDBuilder,
	     GenomicPositionAxisDisplayer.IS_TRANS_REF);

	defaultTranscriptomeRelAxisDisplayer =
	    new GenomicPositionAxisDisplayer
	    ("Transcriptome Relative",
	     1000., 0.01, false,
	     transIDBuilder,
	     GenomicPositionAxisDisplayer.IS_TRANS_REL);

	new GenomicPositionAxisDisplayer(SIGNAL_HISTOGRAM_AXIS, 0.01, 0.001, false, DataSetIDArrayBuilder.getInstance());
	new SignalHistogramDataSetDisplayer();
							    
	//defaultDim = new Dimension(700, 600);
	int viewWidth = VAMPResources.getInt(VAMPResources.VIEW_DEFAULT_WIDTH);
	int viewHeight = VAMPResources.getInt(VAMPResources.VIEW_DEFAULT_HEIGHT);
	if (viewWidth == 0) {
	    viewWidth = Utils.screenSize.width;
	}

	if (viewHeight == 0) {
	    viewHeight = Utils.screenSize.height;
	}

	defaultDim = new Dimension(viewWidth, viewHeight);

	defaultAxisSizes = View.getDefaultAxisSizes();
	defaultMargins = new Margins(30, 40, 30, 40);

	makeViewMenu();
	makePrintPageTemplates();
    }

    //
    // View Templates
    //

    static private void makeViewMenu() {
	defaultZoomTemplate =
	    new ZoomTemplate(VAMPResources.getInt
			     (VAMPResources.DEFAULT_XSLIDE_MIN),
			     VAMPResources.getInt
			     (VAMPResources.DEFAULT_XSLIDE_MAX),
			     VAMPResources.getInt
			     (VAMPResources.DEFAULT_YSLIDE_MIN),
			     VAMPResources.getInt
			     (VAMPResources.DEFAULT_YSLIDE_MAX));

	listZoomTemplate = new ZoomTemplate(0, 0, 0, 8);
	dotPlotZoomTemplate =
	    new ZoomTemplate(VAMPResources.getInt
			     (VAMPResources.DOTPLOT_XSLIDE_MIN),
			     VAMPResources.getInt
			     (VAMPResources.DOTPLOT_XSLIDE_MAX),
			     VAMPResources.getInt
			     (VAMPResources.DOTPLOT_YSLIDE_MIN),
			     VAMPResources.getInt
			     (VAMPResources.DOTPLOT_YSLIDE_MAX));

	//karyoZoomTemplate = new ZoomTemplate(-2, 6, 0, 5);
	karyoZoomTemplate = 
	    new ZoomTemplate(VAMPResources.getInt
			     (VAMPResources.KARYO_XSLIDE_MIN),
			     VAMPResources.getInt
			     (VAMPResources.KARYO_XSLIDE_MAX),
			     VAMPResources.getInt
			     (VAMPResources.KARYO_YSLIDE_MIN),
			     VAMPResources.getInt
			     (VAMPResources.KARYO_YSLIDE_MAX));

	new ChrAxisOP();
	new MergeArrayOP(GraphElementListOperation.CHIP_CHIP_TYPE);

	java.util.TreeMap kparams = new java.util.TreeMap();
	kparams.put(KaryoAnalysisOP.SKIP_OUTLIERS_PARAM, new Boolean(true));
	kparams.put(KaryoAnalysisOP.SKIP_EMPTY_PROFILES_PARAM, new Boolean(true));
	kparams.put(KaryoAnalysisOP.ALT_MASK_PARAM, new Integer(KaryoAnalysisOP.LOSS_MASK|KaryoAnalysisOP.GAIN_AMPLICON_MASK|KaryoAnalysisOP.GAIN_MASK));
	kparams.put(KaryoAnalysisOP.SORT_ALGO_PARAM, KaryoAnalysisOP.STD_SORT_ALGO);
	GraphElementListOperation op = new MergeArrayOP(GraphElementListOperation.CGH_TYPE, kparams);
	String mergeArrayOP_CGH_NAME = op.getName();
	new MergeArrayOP(GraphElementListOperation.CGH_TYPE);
	new MergeArrayOP(GraphElementListOperation.FRAGL_TYPE);

	new MergeChrOP(MergeOP.CGH_TYPE);
	new MergeChrOP(MergeOP.CGH_TYPE, true);
	new MergeChrOP(MergeOP.GTCA_TYPE);

	//new MinimalRegionOP(GraphElementListOperation.CGH_TYPE);
	//new MinimalRegionOP(GraphElementListOperation.SNP_TYPE);
	//new RecurrentRegionOP(GraphElementListOperation.CGH_TYPE);
	//new RecurrentRegionOP(GraphElementListOperation.SNP_TYPE);

	// new NormalizeOP();

	newViewMenu = new Menu("");
	currentViewMenu_std = new Menu("");
	currentViewMenu_std_double = new Menu("");
	currentViewMenu_dotplot_double = new Menu("");
	currentViewMenu_all_disable = new Menu("");

	Menu simpleMenu = new Menu("Simple View");
	Menu doubleMenu = new Menu("Double View");
	Menu speMenu = new Menu("Specialized View");

	newViewMenu.add(simpleMenu);
	newViewMenu.add(doubleMenu);
	newViewMenu.add(speMenu);

	PanelProfile panelProfile;
	PanelProfile panelProfiles[];

	// Simple Point View
	panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("Point View",
				 "Point View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 currentViewMenu_std,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 false));

	// Simple Barplot View
	panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new BarplotDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
						     
	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("Barplot View",
				 "Barplot View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 currentViewMenu_std,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 false));

	// Simple Centered Barplot View
	panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new BarplotDataSetDisplayer(true),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
						     
	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("Centered Barplot View",
				 "Centered Barplot View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 currentViewMenu_std,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 false));

	// Simple Curve View
	panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new CurveDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("Curve View",
				 "Curve View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 currentViewMenu_std,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~(ViewMenuItem.SUPPORT_DOUBLE_VIEW |
				   ViewMenuItem.SUPPORT_PROFILE),
				 false));

	// Simple List View
	panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new NameDataSetDisplayer(dataSetIDArrayBuilder, false),
	     NullAxisDisplayer.getInstance(),
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("List View",
				 "List View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 currentViewMenu_std,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 true));

	// Simple Dotplot View
	panelProfile = new PanelProfile
	    ("",
	     Config.defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new DotPlotDataSetDisplayer(),
	     new DotPlotAxisDisplayer(),
	     null,
	     false,
	     null,
	     dotPlotZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("DotPlot View",
				 "DotPlot View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 currentViewMenu_std,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~(ViewMenuItem.SUPPORT_DOUBLE_VIEW |
				   ViewMenuItem.SUPPORT_PROFILE),
				 true));

	// Simple Karyo View

	karyoAxisSizes = new int[4];
	karyoAxisSizes[GraphPanel.NORTH_X] = 90;
	karyoAxisSizes[GraphPanel.SOUTH_X] = 40;

	karyoAxisSizes[GraphPanel.WEST_Y] = 90;
	karyoAxisSizes[GraphPanel.EAST_Y] = 0;

	panelProfile = new PanelProfile
	    ("Karyo View",
	     karyoAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryoDataSetDisplayer.PROFILE_TYPE,
				       KaryoDataSetDisplayer.POINT_TYPE),
	     defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(mergeArrayOP_CGH_NAME),
	     karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	
	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("Karyotype Classic View",
				 "Karyotype Classic View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 //currentViewMenu_std,
				 // 09/06/05
				 currentViewMenu_all_disable,
				 new Dimension(900, 600),
				 null,
				 (ViewMenuItem.SUPPORT_X |
				  ViewMenuItem.SUPPORT_NEW_VIEW |
				  ViewMenuItem.SUPPORT_SIMPLE_VIEW),// &
				 //~ViewMenuItem.SUPPORT_PROFILE,
				 true));

	panelProfile = new PanelProfile
	    ("Karyo FrAGL View",
	     karyoAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryoDataSetDisplayer.FRAGL_TYPE,
				       KaryoDataSetDisplayer.BARPLOT_TYPE),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeArrayOP.FRAGL_NAME),
	     Config.karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	
	panelProfiles = new PanelProfile[]{panelProfile};

	simpleMenu.add
	    (new NewViewMenuItem("Karyotype FrAGL View",
				 "Karyotype FrAGL View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 //currentViewMenu_std,
				 // 09/06/05
				 currentViewMenu_all_disable,
				 new Dimension(900, 600),
				 null,
				 (ViewMenuItem.SUPPORT_X |
				  ViewMenuItem.SUPPORT_NEW_VIEW |
				  ViewMenuItem.SUPPORT_SIMPLE_VIEW),// &
				 //~ViewMenuItem.SUPPORT_PROFILE,
				 false));
	/*
	// TEST KARYO DOUBLE VIEW
	int karyoAxisSizesTop[] = new int[4];
	karyoAxisSizesTop[GraphPanel.NORTH_X] = 90;
	karyoAxisSizesTop[GraphPanel.SOUTH_X] = 0;

	karyoAxisSizesTop[GraphPanel.WEST_Y] = 90;
	karyoAxisSizesTop[GraphPanel.EAST_Y] = 0;

	panelProfiles = new PanelProfile[5];

	panelProfiles[0] = new PanelProfile
	    ("Karyo Top",
	     karyoAxisSizesTop,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryoDataSetDisplayer.POINT),
	     defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeArrayOP.CGH_NAME),
	     karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	
	int karyoAxisSizesMiddle[] = new int[4];
	karyoAxisSizesMiddle[GraphPanel.NORTH_X] = 0;
	karyoAxisSizesMiddle[GraphPanel.SOUTH_X] = 0;

	karyoAxisSizesMiddle[GraphPanel.WEST_Y] = 90;
	karyoAxisSizesMiddle[GraphPanel.EAST_Y] = 0;

	panelProfiles[1] = new PanelProfile
	    ("Karyo Top Middle",
	     karyoAxisSizesMiddle,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryDataSetDisplayer.POINT),
	     defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeArrayOP.CGH_NAME),
	     karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	
	panelProfiles[2] = new PanelProfile
	    ("Karyo Middle",
	     karyoAxisSizesMiddle,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryoDataSetDisplayer.POINT),
	     defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeArrayOP.CGH_NAME),
	     karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	
	panelProfiles[3] = new PanelProfile
	    ("Karyo Bottom Middle",
	     karyoAxisSizesMiddle,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryoDataSetDisplayer.POINT),
	     defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeArrayOP.CGH_NAME),
	     karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	
	int karyoAxisSizesBottom[] = new int[4];
	karyoAxisSizesBottom[GraphPanel.NORTH_X] = 0;
	karyoAxisSizesBottom[GraphPanel.SOUTH_X] = 40;

	karyoAxisSizesBottom[GraphPanel.WEST_Y] = 90;
	karyoAxisSizesBottom[GraphPanel.EAST_Y] = 0;

	panelProfiles[4] = new PanelProfile
	    ("Karyo Bottom",
	     karyoAxisSizesBottom,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new KaryoDataSetDisplayer(KaryoDataSetDisplayer.POINT),
	     defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeArrayOP.CGH_NAME),
	     karyoZoomTemplate,
	     new Scale(Utils.pow(0.30), Utils.pow(1.80)),
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	*/
	
	/*
	simpleMenu.add
	    (new NewViewMenuItem
	     ("Karyotype 5 View",
	      "Karyotype 5 View",
	      panelProfiles,
	      new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
				   new PanelFinalLayout(0),
				   new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
							new PanelFinalLayout(1),
							new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
									     new PanelFinalLayout(2),
									     new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
												  new PanelFinalLayout(3),
												  new PanelFinalLayout(4))))),
	      
	      //null,
	      new PanelLinks[]{
		  new PanelLinks("All",
				 GraphPanel.SYNCHRO_Y,
				 new int[]{0, 1, 2, 3, 4})},
	      newViewMenu,
	      //currentViewMenu_std,
	      // 09/06/05
	      currentViewMenu_all_disable,
	      new Dimension(900, 600),
	      null,
	      ViewMenuItem.SUPPORT_X |
	      ViewMenuItem.SUPPORT_NEW_VIEW |
	      ViewMenuItem.SUPPORT_SIMPLE_VIEW,
	      true));
	*/
	// END OF TEST KARYO DOUBLE VIEW

	// ----
	int defaultTopAxisSizes[] = new int[]{50, 0, 90, 40};
	int defaultBottomAxisSizes[] = new int[]{0, 50, 90, 40};

	// Double Point View
	panelProfiles = new PanelProfile[2];
	
	panelProfiles[0] = new PanelProfile
	    ("Top",
	     defaultTopAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_NORTH,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles[1] = new PanelProfile
	    ("Bottom",
	     defaultBottomAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	doubleMenu.add
	    (new NewViewMenuItem("Point View",
				 "Point View",
				 panelProfiles,
				 new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
						      new PanelFinalLayout(0),
						      new PanelFinalLayout(1)),
				 new PanelLinks[]{
				     new PanelLinks("Both",
						    GraphPanel.SYNCHRO_X,
						    new int[]{0, 1})},
				 newViewMenu,
				 currentViewMenu_std_double,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 false));

	// Double Barplot View
	panelProfiles = new PanelProfile[2];
	
	panelProfiles[0] = new PanelProfile
	    ("Top",
	     defaultTopAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_NORTH,
	     new BarplotDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles[1] = new PanelProfile
	    ("Bottom",
	     defaultBottomAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new BarplotDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	doubleMenu.add
	    (new NewViewMenuItem("Barplot View",
				 "Barplot View",
				 panelProfiles,
				 new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
						      new PanelFinalLayout(0),
						      new PanelFinalLayout(1)),
				 new PanelLinks[]{
				     new PanelLinks("Both",
						    GraphPanel.SYNCHRO_X,
						    new int[]{0, 1})},
				 newViewMenu,
				 currentViewMenu_std_double,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 false));

	// Double Curve View
	panelProfiles = new PanelProfile[2];
	
	panelProfiles[0] = new PanelProfile
	    ("Top",
	     defaultTopAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_NORTH,
	     new CurveDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles[1] = new PanelProfile
	    ("Bottom",
	     defaultBottomAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new CurveDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	doubleMenu.add
	    (new NewViewMenuItem("Curve View",
				 "Curve View",
				 panelProfiles,
				 new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
						      new PanelFinalLayout(0),
						      new PanelFinalLayout(1)),
				 new PanelLinks[]{
				     new PanelLinks("Both",
						    GraphPanel.SYNCHRO_X,
						    new int[]{0, 1})},
				 newViewMenu,
				 currentViewMenu_std_double,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~(ViewMenuItem.SUPPORT_DOUBLE_VIEW |
				   ViewMenuItem.SUPPORT_PROFILE),
				 false));

	// Double List View
	panelProfiles = new PanelProfile[2];
	
	panelProfiles[0] = new PanelProfile
	    ("Top",
	     defaultTopAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_NORTH,
	     new NameDataSetDisplayer(dataSetIDArrayBuilder, false),
	     NullAxisDisplayer.getInstance(),
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles[1] = new PanelProfile
	    ("Bottom",
	     defaultBottomAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new NameDataSetDisplayer(dataSetIDArrayBuilder, false),
	     NullAxisDisplayer.getInstance(),
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	doubleMenu.add
	    (new NewViewMenuItem("List View",
				 "List View",
				 panelProfiles,
				 new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
						      new PanelFinalLayout(0),
						      new PanelFinalLayout(1)),
				 new PanelLinks[]{
				     new PanelLinks("Both",
						    GraphPanel.SYNCHRO_X,
						    new int[]{0, 1})},
				 newViewMenu,
				 currentViewMenu_std_double,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 true));

	// Double DotPlot View
	panelProfiles = new PanelProfile[2];
	
	panelProfiles[0] = new PanelProfile
	    ("Top",
	     defaultTopAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_NORTH,
	     new DotPlotDataSetDisplayer(),
	     new DotPlotAxisDisplayer(),
	     null,
	     false,
	     null,
	     dotPlotZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	panelProfiles[1] = new PanelProfile
	    ("Bottom",
	     defaultBottomAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new DotPlotDataSetDisplayer(),
	     new DotPlotAxisDisplayer(),
	     null,
	     false,
	     null,
	     dotPlotZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);

	doubleMenu.add
	    (new NewViewMenuItem("DotPlot View",
				 "DotPlot View",
				 panelProfiles,
				 new PanelSplitLayout(PanelSplitLayout.HORIZONTAL,
						      new PanelFinalLayout(0),
						      new PanelFinalLayout(1)),
				 new PanelLinks[]{
				     new PanelLinks("Both",
						    GraphPanel.SYNCHRO_X,
						    new int[]{0, 1})},
				 newViewMenu,
				 currentViewMenu_dotplot_double,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~(ViewMenuItem.SUPPORT_DOUBLE_VIEW |
				   ViewMenuItem.SUPPORT_PROFILE),
				 true));

	// Merge Chromosome Point View
	panelProfile = new PanelProfile
	    ("Merge Chromosome Point View",
	     defaultAxisSizes,
	     PanelProfile.SCROLL_WEST|PanelProfile.SCROLL_SOUTH,
	     new PointDataSetDisplayer("Merge Chromosome Point", false),
	     defaultChromosomeNameAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(MergeChrOP.CGH_NAME),
	     defaultZoomTemplate,
	     null,
	     0,
	     true,
	     Config.defaultMargins,
	     null);
	
	panelProfiles = new PanelProfile[]{panelProfile};

	speMenu.add
	    (new NewViewMenuItem("Merge Chromosome View",
				 "Merge Chromosome View",
				 panelProfiles,
				 null,
				 null,
				 newViewMenu,
				 currentViewMenu_std,
				 defaultDim,
				 null,
				 ViewMenuItem.SUPPORT_ALL &
				 ~ViewMenuItem.SUPPORT_DOUBLE_VIEW,
				 false));


	new YDendrogramGraphElementDisplayer();
	new YDendrogramAxisDisplayer();
	new XDendrogramGraphElementDisplayer();
	new XDendrogramAxisDisplayer();

	new TranscriptomeClusterAxisDisplayer();

	panelProfiles = new PanelProfile[4];

	panelProfiles[0] = new PanelProfile
	    ("ProbeSet Dendrogram",
	     new int[]{50, 0, 90, 0},
	     PanelProfile.SCROLL_NORTH|PanelProfile.SCROLL_WEST,
	     new PointDataSetDisplayer(false),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     new Margins(30, 20, 30, 0),
	     null);

	panelProfiles[1] = new PanelProfile
	    ("Logo",
	     new int[]{50, 0, 0, 0},
	     PanelProfile.SCROLL_NORTH|PanelProfile.SCROLL_EAST,
	     new CurveDataSetDisplayer(),
	     Config.defaultGenomicPositionAxisDisplayer,
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     PanelProfile.DISABLED,
	     true,
	     new Margins(30, 0, 30, 30),
	     null);

	panelProfiles[2] = new PanelProfile
	    ("Transcriptome",
	     new int[]{0, 50, 90, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_WEST,
	     new DotPlotDataSetDisplayer(),
	     new DotPlotAxisDisplayer(),
	     null,
	     false,
	     GraphElementListOperation.get(ChrAxisOP.NAME),
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     new Margins(30, 20, 30, 0),
	     null);

	panelProfiles[3] = new PanelProfile
	    ("Dendrogram",
	     new int[]{0, 50, 0, 0},
	     PanelProfile.SCROLL_SOUTH|PanelProfile.SCROLL_EAST,
	     new XDendrogramGraphElementDisplayer(),
	     new XDendrogramAxisDisplayer(),
	     null,
	     false,
	     null,
	     Config.defaultZoomTemplate,
	     null,
	     0,
	     true,
	     new Margins(30, 0, 30, 30),
	     null);

	if (NEW_CURRENT_VIEW_MENU) {
	    MenuItem pointViewMenu =
		new CurrentViewMenuItem("Point View",
					new PointDataSetDisplayer(false),
					defaultGenomicPositionAxisDisplayer,
					//null,
					GraphElementListOperation.get
					(ChrAxisOP.NAME),
					ViewMenuItem.SUPPORT_ALL,
					false);
	
	    currentViewMenu_std.add(pointViewMenu);
	    currentViewMenu_std_double.add(pointViewMenu);

	    MenuItem pointViewMenu_disable = 
		new CurrentViewMenuItem("Point View",
					null,
					null,
					null,
					ViewMenuItem.SUPPORT_ALL|
					ViewMenuItem.ALWAYS_DISABLE,
					false);

	    currentViewMenu_dotplot_double.add(pointViewMenu_disable);
	    currentViewMenu_all_disable.add(pointViewMenu_disable);
	}
	else {
	    Menu pointViewMenu = new Menu("Point View");
	    currentViewMenu_std.add(pointViewMenu);
	    currentViewMenu_std_double.add(pointViewMenu);

	    pointViewMenu.add
		(new CurrentViewMenuItem("Genomic Position",
					 new PointDataSetDisplayer(false),
					 defaultGenomicPositionAxisDisplayer,
					 GraphElementListOperation.get
					 (ChrAxisOP.NAME),
					 ViewMenuItem.SUPPORT_ALL,
					 false));

	    pointViewMenu.add
		(new CurrentViewMenuItem("Chromosome Name",
					 new PointDataSetDisplayer(false),
					 defaultChromosomeNameAxisDisplayer,
					 GraphElementListOperation.get
					 (ChrAxisOP.NAME),
					 ViewMenuItem.SUPPORT_ALL,
					 false));

	    Menu pointViewMenu_disable = new Menu("Point View");
	    currentViewMenu_dotplot_double.add(pointViewMenu_disable);
	    currentViewMenu_all_disable.add(pointViewMenu_disable);

	    pointViewMenu_disable.add
		(new CurrentViewMenuItem("Genomic Position",
					 null,
					 null,
					 null,
					 ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE,
					 false));

	    pointViewMenu_disable.add
		(new CurrentViewMenuItem("Chromosome Name",
					 null,
					 null,
					 null,
					 ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE,
					 false));

	}

	if (NEW_CURRENT_VIEW_MENU) {
	    MenuItem barplotViewMenu =
		new CurrentViewMenuItem("Barplot View",
					new BarplotDataSetDisplayer(),
					defaultGenomicPositionAxisDisplayer,
					//null,
					GraphElementListOperation.get
					(ChrAxisOP.NAME),
					ViewMenuItem.SUPPORT_ALL,
					//~ViewMenuItem.SUPPORT_PROFILE,
					false);
			 
	    currentViewMenu_std.add(barplotViewMenu);
	    currentViewMenu_std_double.add(barplotViewMenu);

	    MenuItem barplotViewMenu_disable =
		new CurrentViewMenuItem("Barplot View",
					null,
					null,
					null,
					(ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE),
					//~ViewMenuItem.SUPPORT_PROFILE,
					false);

	    currentViewMenu_dotplot_double.add(barplotViewMenu_disable);
	    currentViewMenu_all_disable.add(barplotViewMenu_disable);
	}
	else {
	    Menu barplotViewMenu = new Menu("Barplot View");
	    currentViewMenu_std.add(barplotViewMenu);
	    currentViewMenu_std_double.add(barplotViewMenu);

	    barplotViewMenu.add
		(new CurrentViewMenuItem("Genomic Position",
					 new BarplotDataSetDisplayer(),
					 defaultGenomicPositionAxisDisplayer,
					 GraphElementListOperation.get
					 (ChrAxisOP.NAME),
					 ViewMenuItem.SUPPORT_ALL,
					 //~ViewMenuItem.SUPPORT_PROFILE,
					 false));
			 
	    barplotViewMenu.add
		(new CurrentViewMenuItem("Chromosome Name",
					 new BarplotDataSetDisplayer(),
					 defaultChromosomeNameAxisDisplayer,
					 GraphElementListOperation.get
					 (ChrAxisOP.NAME),
					 ViewMenuItem.SUPPORT_ALL,
					 false));
			 
	    Menu barplotViewMenu_disable = new Menu("Barplot View");
	    currentViewMenu_dotplot_double.add(barplotViewMenu_disable);
	    currentViewMenu_all_disable.add(barplotViewMenu_disable);

	    barplotViewMenu_disable.add
		(new CurrentViewMenuItem("Genomic Position",
					 null,
					 null,
					 null,
					 ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE,
					 false));
			 
	    barplotViewMenu_disable.add
		(new CurrentViewMenuItem("Chromosome Name",
					 null,
					 null,
					 null,
					 ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE,
					 false));
			 
	}

	if (NEW_CURRENT_VIEW_MENU) {
	    MenuItem curveViewMenu =
		new CurrentViewMenuItem("Curve View",
					new CurveDataSetDisplayer(),
					defaultGenomicPositionAxisDisplayer,
					//null,
					GraphElementListOperation.get
					(ChrAxisOP.NAME),
					ViewMenuItem.SUPPORT_ALL &
					~ViewMenuItem.SUPPORT_PROFILE,
					false);
			 
	    currentViewMenu_std.add(curveViewMenu);
	    currentViewMenu_std_double.add(curveViewMenu);

	    MenuItem curveViewMenu_disable =
		new CurrentViewMenuItem("Curve View",
					null,
					null,
					null,
					(ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE) &
					~ViewMenuItem.SUPPORT_PROFILE,
					false);
	    currentViewMenu_dotplot_double.add(curveViewMenu_disable);
	    currentViewMenu_all_disable.add(curveViewMenu_disable);
	}
	else {
	    Menu curveViewMenu = new Menu("Curve View");
	    currentViewMenu_std.add(curveViewMenu);
	    currentViewMenu_std_double.add(curveViewMenu);

	    curveViewMenu.add
		(new CurrentViewMenuItem("Genomic Position",
					 new CurveDataSetDisplayer(),
					 defaultGenomicPositionAxisDisplayer,
					 GraphElementListOperation.get
					 (ChrAxisOP.NAME),
					 ViewMenuItem.SUPPORT_ALL,
					 false));
			 
	    curveViewMenu.add
		(new CurrentViewMenuItem("Chromosome Name",
					 new CurveDataSetDisplayer(),
					 defaultChromosomeNameAxisDisplayer,
					 GraphElementListOperation.get
					 (ChrAxisOP.NAME),
					 ViewMenuItem.SUPPORT_ALL,
					 false));
			 
	    Menu curveViewMenu_disable = new Menu("Curve View");
	    curveViewMenu_disable.add
		(new CurrentViewMenuItem("Genomic Position",
					 null,
					 null,
					 null,
					 ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE,
					 false));
			 
	    curveViewMenu_disable.add
		(new CurrentViewMenuItem("Chromosome Name",
					 null,
					 null,
					 null,
					 ViewMenuItem.SUPPORT_ALL|
					 ViewMenuItem.ALWAYS_DISABLE,
					 false));
			 
	    currentViewMenu_dotplot_double.add(curveViewMenu_disable);
	    currentViewMenu_all_disable.add(curveViewMenu_disable);
	}

	// ----

	MenuItem listViewMenuItem =
	    new CurrentViewMenuItem("List View",
				    new NameDataSetDisplayer(dataSetIDArrayBuilder, false),
				    NullAxisDisplayer.getInstance(),
				    null,
				    ViewMenuItem.SUPPORT_ALL,
				    false);

	currentViewMenu_std.add(listViewMenuItem);
	currentViewMenu_std_double.add(listViewMenuItem);

	MenuItem listViewMenuItem_disable =
	    new CurrentViewMenuItem("List View",
				    null,
				    null,
				    null,
				    ViewMenuItem.SUPPORT_ALL|
				    ViewMenuItem.ALWAYS_DISABLE,
				    false);

	currentViewMenu_dotplot_double.add(listViewMenuItem_disable);
	currentViewMenu_all_disable.add(listViewMenuItem_disable);

	MenuItem dotplotViewMenuItem = 
	    new CurrentViewMenuItem("DotPlot View",
				    new DotPlotDataSetDisplayer(),
				    new DotPlotAxisDisplayer(),
				    null,
				    ViewMenuItem.SUPPORT_ALL &
				    ~ViewMenuItem.SUPPORT_PROFILE,
				    false);

	MenuItem dotplotViewMenuItem_disable = 
	    new CurrentViewMenuItem("DotPlot View",
				    new DotPlotDataSetDisplayer(),
				    new DotPlotAxisDisplayer(),
				    null,
				    (ViewMenuItem.SUPPORT_ALL|
				     ViewMenuItem.ALWAYS_DISABLE) &
				    ~ViewMenuItem.SUPPORT_PROFILE,
				    false);
	
	currentViewMenu_std.add(dotplotViewMenuItem);
	currentViewMenu_std_double.add(dotplotViewMenuItem_disable);
	currentViewMenu_dotplot_double.add(dotplotViewMenuItem);
	currentViewMenu_all_disable.add(dotplotViewMenuItem_disable);

	addAllCurrentViewMenus
	    (new CurrentViewColorCodesMenuItem(true));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Normal Probes",
						 CurrentViewExtraDisplayMenuItem.SHOW_NORMAL,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Breakpoints",
						 CurrentViewExtraDisplayMenuItem.SHOW_BRK,
						 true));

	/*
	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Smoothing Points",
						 CurrentViewExtraDisplayMenuItem.SHOW_SMT_PTS,
						 false));
	*/

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Smoothing Lines",
						 CurrentViewExtraDisplayMenuItem.SHOW_SMT_LNS,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Centromeres",
						 CurrentViewExtraDisplayMenuItem.SHOW_CTM,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Highlight/Unhighlight Outliers",
						 CurrentViewExtraDisplayMenuItem.SHOW_OUT,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Probe Sizes",
						 CurrentViewExtraDisplayMenuItem.SHOW_SIZE,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show All",
						 CurrentViewExtraDisplayMenuItem.SHOW_ALL,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Hide All",
						 CurrentViewExtraDisplayMenuItem.HIDE_ALL,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewHighlightMinimalRegionsMenuItem(true));

	addAllCurrentViewMenus
	    (new CurrentViewHighlightRecurrentRegionsMenuItem(false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Annotations",
						 CurrentViewExtraDisplayMenuItem.SHOW_ANNOT,
						 true));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Synteny Links",
						 CurrentViewExtraDisplayMenuItem.SHOW_SYNTENY_LINKS,
						 true));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Tags",
						 CurrentViewExtraDisplayMenuItem.SHOW_TAG,
						 true));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Tag Strings",
						 CurrentViewExtraDisplayMenuItem.SHOW_TAG_STRING,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Info",
						 CurrentViewExtraDisplayMenuItem.SHOW_INFO,
						 true));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Out of bounds Info",
						 CurrentViewExtraDisplayMenuItem.SHOW_OUT_OF_BOUNDS_INFO,
						 false));

	addAllCurrentViewMenus
	    (new CurrentViewExtraDisplayMenuItem("Show/Hide Chromosome Separators",
						 CurrentViewExtraDisplayMenuItem.SHOW_CHR_SEP,
						 true));

	addAllCurrentViewMenus
	    (new CurrentViewBarplotMenuItem(true));

	addAllCurrentViewMenus
	    (new CurrentViewSyncMenuItem(true));
	
	Menu absMenu = new Menu("Abscissa", true);

	absMenu.add
	    (new CurrentViewMenuItem("Genomic Position",
				     null,
				     defaultGenomicPositionAxisDisplayer,
				     null,
				     ViewMenuItem.SUPPORT_ALL &
				     ~ViewMenuItem.SUPPORT_NEW_VIEW,
				     false));

	absMenu.add
	    (new CurrentViewMenuItem("Chromosome Name",
				     null,
				     defaultChromosomeNameAxisDisplayer,
				     null,
				     ViewMenuItem.SUPPORT_ALL &
				     ~ViewMenuItem.SUPPORT_NEW_VIEW,
				     false));
	
	addAllCurrentViewMenus(absMenu);

    }

    static private void makePrintPageTemplates() {
	PrintPageTemplate p;

	paperA4 = new Paper();
	paperA4.setSize(597, 844);
	paperA4.setImageableArea(72, 72, 452, 699);

	// TBD: wrong dimension
	paperLetter = new Paper();
	paperLetter.setSize(597, 844);
	paperLetter.setImageableArea(72, 72, 452, 699);

	PageFormat portrait = new PageFormat();
	portrait.setPaper(paperA4);
	portrait.setOrientation(PageFormat.PORTRAIT);

	PageFormat landscape = new PageFormat();
	landscape.setPaper(paperA4);
	landscape.setOrientation(PageFormat.LANDSCAPE);

	// -------------------------------------------------------------
	// NOTICE: the following will disapear when template persistance
	// will be done
	// -------------------------------------------------------------

	int minimapW = 120;
	int titleH = 45;
	int pageH = 20;
	int pageW = 120;

	p = new PrintPageTemplate("GraphElement Only", portrait);

	String titleTemplate = "@#Project# [#Team#]\n@#Title#";
	String pagenumTemplate = "#PrintDate#      #PageNum#/#PageCount#";
	String mmTemplate = "@Chr #MMChr# / #MMOrganism# / #MMResolution#";

	p.addArea(new PrintTextArea
		  ("Title",
		   titleTemplate,
		   new Rectangle2D.Double(portrait.getImageableX(),
					  portrait.getImageableY(),
					  portrait.getImageableWidth(),
					  titleH),
		   Color.WHITE,
		   new Font("MonoSpaced", Font.BOLD, 14)));

	p.addArea(new PrintArea
		  (PrintArea.GRAPHELEMENTS,
		   new Rectangle2D.Double(portrait.getImageableX(),
					  portrait.getImageableY()+titleH,
					  portrait.getImageableWidth(), 
					  portrait.getImageableHeight()-titleH-pageH)));

	p.addArea(new PrintTextArea
		  ("Pagenum",
		   pagenumTemplate,
		   new Rectangle2D.Double(portrait.getImageableX() +
					  portrait.getImageableWidth() - pageW,
					  portrait.getImageableY() +
					  portrait.getImageableHeight() - pageH,
					  pageW,
					  pageH)));

	p = new PrintPageTemplate("GraphElement Only Landscape", landscape);

	p.addArea(new PrintTextArea
		  ("Title",
		   titleTemplate,
		   new Rectangle2D.Double(landscape.getImageableX(),
					  landscape.getImageableY(),
					  landscape.getImageableWidth(),
					  titleH),
		   Color.WHITE,
		   new Font("MonoSpaced", Font.BOLD, 14)));

	p.addArea(new PrintArea
		  (PrintArea.GRAPHELEMENTS,
		   new Rectangle2D.Double(landscape.getImageableX(),
					  landscape.getImageableY()+titleH,
					  landscape.getImageableWidth(), 
					  landscape.getImageableHeight()-titleH-pageH)));

	p.addArea(new PrintTextArea
		  ("Pagenum",
		   pagenumTemplate,
		   new Rectangle2D.Double(landscape.getImageableX() +
					  landscape.getImageableWidth() - pageW,
					  landscape.getImageableY() +
					  landscape.getImageableHeight() - pageH,
					  pageW,
					  pageH)));

	p = new PrintPageTemplate("Standard", portrait);


	p.addArea(new PrintTextArea
		  ("Title",
		   titleTemplate,
		   new Rectangle2D.Double(portrait.getImageableX(),
					  portrait.getImageableY(),
					  portrait.getImageableWidth(),
					  titleH),
		   Color.WHITE,
		   new Font("MonoSpaced", Font.BOLD, 14)));
		  
	int mmTitleH = 20;
	p.addArea(new PrintTextArea
		  ("MMTitle",
		   mmTemplate,
		   new Rectangle2D.Double(portrait.getImageableX(),
					  portrait.getImageableY() + titleH,
					  minimapW,
					  mmTitleH),
		   Color.WHITE));

	p.addArea(new PrintArea
		  (PrintArea.MINIMAP,
		   new Rectangle2D.Double(portrait.getImageableX(),
					  portrait.getImageableY() + titleH +
					  mmTitleH,
					  minimapW,
					  portrait.getImageableHeight() - titleH - mmTitleH),
		   Color.WHITE));

	int YscaleW = 0;
	int XscaleH = 0;

	p.addArea(new PrintArea
		  (PrintArea.GRAPHELEMENTS,
		   new Rectangle2D.Double(portrait.getImageableX() + minimapW +
					  YscaleW,
					  portrait.getImageableY() + titleH,
					  portrait.getImageableWidth() - minimapW - YscaleW, 
					  portrait.getImageableHeight() - pageH - XscaleH - titleH)));


	p.addArea(new PrintTextArea
		  ("Pagenum",
		   pagenumTemplate,
		   new Rectangle2D.Double(portrait.getImageableX() +
					  portrait.getImageableWidth() - pageW,
					  portrait.getImageableY() +
					  portrait.getImageableHeight() - pageH,
					  pageW,
					  pageH)));

	p = new PrintPageTemplate("Standard Landscape", landscape);

	p.addArea(new PrintTextArea
		  ("Title",
		   titleTemplate,
		   new Rectangle2D.Double(landscape.getImageableX(),
					  landscape.getImageableY(),
					  landscape.getImageableWidth(),
					  titleH),
		   Color.WHITE,
		   new Font("MonoSpaced", Font.BOLD, 14)));
		  
	p.addArea(new PrintTextArea
		  ("MMTitle",
		   mmTemplate,
		   new Rectangle2D.Double(landscape.getImageableX(),
					  landscape.getImageableY() +
					  titleH,
					  minimapW,
					  mmTitleH),
		   Color.WHITE));

	p.addArea(new PrintArea
		  (PrintArea.MINIMAP,
		   new Rectangle2D.Double(landscape.getImageableX(),
					  landscape.getImageableY() +
					  titleH + mmTitleH,
					  minimapW,
					  landscape.getImageableHeight() - titleH - mmTitleH),
		   Color.WHITE));

	p.addArea(new PrintArea
		  (PrintArea.GRAPHELEMENTS,
		   new Rectangle2D.Double(landscape.getImageableX() + minimapW +
					  YscaleW,
					  landscape.getImageableY() + titleH,
					  landscape.getImageableWidth() - minimapW - YscaleW, 
					  landscape.getImageableHeight() - pageH - XscaleH - titleH)));


	p.addArea(new PrintTextArea
		  ("Pagenum",
		   pagenumTemplate,
		   new Rectangle2D.Double(landscape.getImageableX() +
					  landscape.getImageableWidth() - pageW,
					  landscape.getImageableY() +
					  landscape.getImageableHeight() - pageH,
					  pageW,
					  pageH)));

    }

    static void addAllCurrentViewMenus(MenuItem menu) {
	currentViewMenu_std.add(menu);
	currentViewMenu_std_double.add(menu);
	currentViewMenu_dotplot_double.add(menu);
	currentViewMenu_all_disable.add(menu);
    }
}
