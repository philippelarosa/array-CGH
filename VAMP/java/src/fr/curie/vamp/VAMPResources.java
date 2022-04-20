
/*
 *
 * VAMPResources.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;

public class VAMPResources {

    // Dialog Resources
    public static final String DIALOG_FONT = "DialogFont";
    public static final String DIALOG_BUTTON_FONT = "DialogButtonFont";

    public static final String DIALOG_BG = "DialogBG";
    public static final String DIALOG_BUTTON_BG = "DialogButtonBG";
    public static final String DIALOG_BUTTON_FG = "DialogButtonFG";
    // ...

    public static final String HORIZONTAL_INFO = "HorizontalInfo";

    public static final String SCALING_WHILE_ADJUSTING = "ScalingWhileAdjusting";
    public static final String SCROLLING_WHILE_ADJUSTING = "ScrollingWhileAdjusting";

    public static final String DEBUG_INFO = "DebugInfo";

    public static final String TITLE_PANEL_BG = "TitlePanelBG";
    public static final String INFO_PANEL_BG = "InfoPanelBG";
    public static final String INFO_PANEL_PINNED_UP_BG = "InfoPanelPinnedUpBG";
    public static final String INFO_TITLE_FG = "InfoTitleFG";
    public static final String INFO_PROPERTY_FG = "InfoPropertyFG";
    public static final String INFO_VALUE_FG = "InfoValueFG";
    public static final String INFO_TOP_TITLE_FG = "InfoTopTitleFG";
    public static final String INFO_BOTTOM_TITLE_FG = "InfoBottomTitleFG";
    public static final String TAB_BG = "TabBG";

    public static final String REGION_BG = "RegionDefaultBG";
    public static final String MARK_FG = "MarkFG";
    public static final String DATASET_SELECTED_FG = "DataSetSelectedFG";
    public static final String REGION_SELECTED_FG = "RegionSelectedFG";
    public static final String MARK_SELECTED_FG = "MarkSelectedFG";
    public static final String DATASET_PINNED_UP_FG = "DataSetPinnedUpFG";
    public static final String CGH_ARRAY_LIGHT_IMPORTED_FG = "CGHArrayLightImportedFG";
    public static final String TRANSCRIPTOME_LIGHT_IMPORTED_FG = "TranscriptomeLightImportedFG";

    public static final String CANVAS_BG = "CanvasBG";
    public static final String CANVAS_LOCKED_BG = "CanvasLockedBG";
    public static final String CANVAS_RUNNING_BG = "CanvasRunningBG";

    public static final String PRINT_PREVIEW_EDIT_BG = "PrintPreviewEditBG";

    public static final String ITEM_CENTERED_FG = "ItemCenteredFG";
    public static final String ITEM_CENTERED_SIZE = "ItemCenteredSize";
    public static final String ITEM_PINNED_UP_FG = "ItemPinnedUpFG";
    public static final String ITEM_PINNED_UP_SIZE = "ItemPinnedSize";
    public static final String ITEM_TAG_FG = "ItemTagFG";
    public static final String ITEM_TAG_SIZE = "ItemTagSize";
    public static final String MARK_CENTERED_FG = "MarkCenteredFG";

    public static final String AXIS_NORTH_SIZE = "AxisNorthSize";
    public static final String AXIS_SOUTH_SIZE = "AxisSouthSize";
    public static final String AXIS_WEST_SIZE = "AxisWestSize";
    public static final String AXIS_EAST_SIZE = "AxisEastSize";

    public static final String VIEW_DEFAULT_WIDTH = "ViewDefaultWidth";
    public static final String VIEW_DEFAULT_HEIGHT = "ViewDefaultHeight";

    public static final String MINIMAP_BG = "MinimapBG";
    public static final String MINIMAP_TITLE_FG = "MinimapTitleFG";
    public static final String MINIMAP_LABEL_FG = "MinimapLabelFG";
    public static final String MINIMAP_LOCATION_FG = "MinimapLocationFG";
    public static final String MINIMAP_TITLE_FONT = "MinimapTitleFont";
    public static final String MINIMAP_LABEL_FONT = "MinimapLabelFont";
    public static final String MINIMAP_LOCATION_WIDTH = "MinimapLocationWidth";
    public static final String MINIMAP_HIDE = "MinimapHide";

    public static final String TABBED_PANE_FONT = "TabbedPaneFont";

    public static final String VIEW_BG = "ViewBG";
    public static final String ZOOM_PANEL_BG = "ZoomPanelBG";
    public static final String SEARCH_PANEL_BG = "SeachPanelBG"; 
    public static final String COLOR_LEGEND_PANEL_BG = "ColorLegendPanelBG";

    public static final String COLOR_LEGEND_PANEL_LABEL_FONT = "ColorLegendPanelLabelFont";
    public static final String COLOR_LEGEND_PANEL_BUTTON_BG = "ColorLegendPanelButtonBG";
    public static final String THRESHOLD_PANEL_BG = "ThresholdsPanelBG";

    public static final String THRESHOLD_PANEL_LABEL_FONT = "ThresholdsPanelLabelFont";
    public static final String THRESHOLD_PANEL_BUTTON_BG = "ThresholdsPanelButtonBG";

    public static final String AXIS_BG = "AxisBG";
    public static final String AXIS_FG = "AxisFG";
    public static final String AXIS_LINE_FG = "AxisLineFG";
    public static final String AXIS_LABEL_FG = "AxisLabelFG";

    public static final String AXIS_TRANSCRIPTOME_FG = "AxisTranscriptomeFG";
    public static final String AXIS_TRANSCRIPTOME_LINE_FG = "AxisTranscriptomeLineFG";
    public static final String AXIS_TRANSCRIPTOME_LABEL_FG = "AxisTranscriptomeLabelFG";
    public static final String AXIS_TRANSCRIPTOME_REF_FG =
	"AxisTranscriptomeReferenceFG";
    public static final String AXIS_TRANSCRIPTOME_REF_LINE_FG =
	"AxisTranscriptomeReferenceLineFG";
    public static final String AXIS_TRANSCRIPTOME_REF_LABEL_FG =
	"AxisTranscriptomeReferenceLabelFG";

    public static final String AXIS_TRANSCRIPTOME_REL_FG =
	"AxisTranscriptomeRelativeFG";
    public static final String AXIS_TRANSCRIPTOME_REL_LINE_FG =
	"AxisTranscriptomeRelativeFG";
    public static final String AXIS_TRANSCRIPTOME_REL_LABEL_FG =
	"AxisTranscriptomeRelativeLabelFG";

    public static final String TRANSCRIPTOME_MERGE_COLOR_BASE =
	"TranscriptomeMergeColorBase";

    public static final String PROBE_SET_FG = "ProbeSetFG";

    public static final String AXIS_Y_PROPERTY_NAME_FG = "AxisYPropertyNameFG";
    public static final String	AXIS_Y_CANVAS_PROPERTY_NAME_FG = "AxisYCanvasPropertyNameFG";

    public static final String CHROMOSOME_SEPARATOR_FG = "ChromosomeSeparatorFG";
    public static final String CHROMOSOME_LINE_SEPARATOR_FG = "ChromosomeLineSeparatorFG";
    public static final String CHROMOSOME_SEPARATOR_DASH_WIDTH = "ChromosomeSeparatorDashWidth";
    public static final String CHROMOSOME_SEPARATOR_DASH_PADDING = "ChromosomeSeparatorDashPadding";

    public static final String CLONE_NA_FG = "CloneNAFG";
    public static final String CLONE_OUTLIER_FG = "CloneOutlierFG";

    public static final String DEFAULT_SLIDE_RATIO = "DefaultSlideRatio";
    public static final String DEFAULT_XSLIDE_MIN = "DefaultXSlideMin";
    public static final String DEFAULT_XSLIDE_MAX = "DefaultXSlideMax";
    public static final String DEFAULT_YSLIDE_MIN = "DefaultYSlideMin";
    public static final String DEFAULT_YSLIDE_MAX = "DefaultYSlideMax";

    public static final String DOTPLOT_XSLIDE_MIN = "DotPlotXSlideMin";
    public static final String DOTPLOT_XSLIDE_MAX = "DotPlotXSlideMax";
    public static final String DOTPLOT_YSLIDE_MIN = "DotPlotYSlideMin";
    public static final String DOTPLOT_YSLIDE_MAX = "DotPlotYSlideMax";

    public static final String KARYO_XSLIDE_MIN = "KaryoXSlideMin";
    public static final String KARYO_XSLIDE_MAX = "KaryoXSlideMax";
    public static final String KARYO_YSLIDE_MIN = "KaryoYSlideMin";
    public static final String KARYO_YSLIDE_MAX = "KaryoYSlideMax";

    public static final String POINT_NA_WIDTH = "PointNAWidth";
    public static final String POINT_WIDTH = "PointWidth";
    public static final String POINT_OUT_WIDTH = "PointOutlierWidth";
    public static final String LINE_WIDTH = "LineWidth";
    public static final String OVAL_WIDTH = "OvalWidth";
    public static final String POINT_KARYO_WIDTH = "PointKaryoWidth";

    public static final String SAMPLE_ANNOT_WIDTH = "SampleAnnotWidth";
    public static final String SAMPLE_ANNOT_MAX = "SampleAnnotMax";

    public static final String MIN_KARYO_CHROMOSOME_WIDTH = "MinKaryoChromosomeWidth";
    public static final String MAX_KARYO_CHROMOSOME_WIDTH = "MaxKaryoChromosomeWidth";

    public static final String COLOR_CODE_YMIN = "YMin";
    public static final String COLOR_CODE_YNORMAL_MIN = "YNormalMin";
    public static final String COLOR_CODE_YNORMAL_MAX = "YNormalMax";
    public static final String COLOR_CODE_LOH = "LOH";
    public static final String COLOR_CODE_YMAX = "YMax";
    public static final String COLOR_CODE_AMPLICON = "Amplicon";
    public static final String COLOR_CODE_MIN_FG = "MinFG";
    public static final String COLOR_CODE_NORMAL_FG = "NormalFG";
    public static final String COLOR_CODE_MAX_FG = "MaxFG";
    public static final String COLOR_CODE_AMPLICON_FG = "AmpliconFG";
    public static final String COLOR_CODE_LOH_FG = "LOHFG";
    public static final String COLOR_CODE_CONTINUOUS_MODE = "ContinuousMode";

    public static final String COLOR_CODE_COUNT = "ColorCodeCount";

    public static final String SEARCH_PANEL_BUTTON_BG = "SearchPanelButtonBG";

    public static final String BREAKPOINT_FG = "BreakpointFG";
    public static final String SMOOTHING_POINT_FG = "SmoothingPointFG";
    public static final String SMOOTHING_LINE_FG = "SmoothingLineFG";
    public static final String SMOOTHING_POINT_WIDTH = "SmoothingPointWidth";
    public static final String CENTROMERE_FG = "CentromereFG";
    public static final String CENTROMERE_BASE_WIDTH = "CentromereBaseWidth";
    public static final String BREAKPOINT_DASH_WIDTH = "BreakpointDashWidth";
    public static final String BREAKPOINT_DASH_PADDING = "BreakpointDashPadding";

    public static final String MENU_MAX_ITEMS = "MenuMaxItems";

    public static final String SEARCH_PANEL_BUTTON_FONT = "SearchPanelButtonFont";
    public static final String INFO_PANEL_TITLE_FONT = "InfoPanelTitleFont";
    public static final String INFO_PANEL_TEXT_FONT = "InfoPanelTextFont";
    public static final String INFO_PANEL_TYPE_FONT = "InfoPanelTypeFont";
    public static final String DATASET_DISPLAYER_FONT = "DataSetDisplayerFont";
    public static final String AXIS_X_DISPLAYER_FONT = "AxisXDisplayerFont";
    public static final String AXIS_Y_DISPLAYER_FONT = "AxisYDisplayerFont";
    public static final String AXIS_Y_NAME_DISPLAYER_FONT = "AxisYNameDisplayerFont";
    public static final String NAME_DATASET_DISPLAYER_FONT = "NameDataSetDisplayerFont";
    public static final String AXIS_KARYO_DISPLAYER_FONT = "AxisKaryoDisplayerFont";
    public static final String AXIS_KARYO_SMALL_DISPLAYER_FONT = "AxisKaryoSmallDisplayerFont";
    public static final String AXIS_KARYO_NAME_DISPLAYER_FONT = "AxisKaryoNameDisplayerFont";

    public static final String AXIS_MESSAGE_FONT = "AxisMessageFont";
    public static final String AXIS_MESSAGE_COLOR = "AxisMessageColor";

    public static final String ZOOM_PANEL_LABEL_FONT = "ZoomPanelLabelFont";
    public static final String ZOOM_PANEL_TEXTFIELD_FONT = "ZoomPanelTextFieldFont";

    public static final String THRESHOLD_MINY_FG = "ThresholdMinYFG";
    public static final String THRESHOLD_MAXY_FG = "ThresholdMaxYFG";

    public static final String THRESHOLD_MINY = "MinY";
    public static final String THRESHOLD_MAXY = "MaxY";

    public static final String THRESHOLD_LOG_MINY = "LogMinY";
    public static final String THRESHOLD_LOG_MAXY = "LogMaxY";

    public static final String THRESHOLD_CGH_ARRAY_MINY = "Threshold_CGH_" + THRESHOLD_MINY;
    public static final String THRESHOLD_CGH_ARRAY_MAXY = "Threshold_CGH_" + THRESHOLD_MAXY;
    public static final String THRESHOLD_CGH_ARRAY_LOG_MINY = "Threshold_CGH_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_CGH_ARRAY_LOG_MAXY = "Threshold_CGH_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_SNP_MINY = "Threshold_SNP_" + THRESHOLD_MINY;
    public static final String THRESHOLD_SNP_MAXY = "Threshold_SNP_" + THRESHOLD_MAXY;
    public static final String THRESHOLD_SNP_LOG_MINY = "Threshold_SNP_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_SNP_LOG_MAXY = "Threshold_SNP_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_LOH_MINY = "Threshold_LOH_" + THRESHOLD_MINY;
    public static final String THRESHOLD_LOH_MAXY = "Threshold_LOH_" + THRESHOLD_MAXY;
    public static final String THRESHOLD_LOH_LOG_MINY = "Threshold_LOH_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_LOH_LOG_MAXY = "Threshold_LOH_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_TRANSCRIPTOME_MINY = "Threshold_TRS_" + THRESHOLD_MINY;
    public static final String THRESHOLD_TRANSCRIPTOME_MAXY = "Threshold_TRS_" + THRESHOLD_MAXY;

    public static final String THRESHOLD_TRANSCRIPTOME_LOG_MINY = "Threshold_TRS_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_TRANSCRIPTOME_LOG_MAXY = "Threshold_TRS_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_TRANSCRIPTOME_REL_MINY = "Threshold_TRSREL_" + THRESHOLD_MINY;
    public static final String THRESHOLD_TRANSCRIPTOME_REL_MAXY = "Threshold_TRSREL_" + THRESHOLD_MAXY;

    public static final String THRESHOLD_TRANSCRIPTOME_REL_LOG_MINY = "Threshold_TRSREL_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_TRANSCRIPTOME_REL_LOG_MAXY = "Threshold_TRSREL_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_CHIP_CHIP_MINY = "Threshold_ChIP-chip_" + THRESHOLD_MINY;
    public static final String THRESHOLD_CHIP_CHIP_MAXY = "Threshold_ChIP-chip_" + THRESHOLD_MAXY;

    public static final String THRESHOLD_CHIP_CHIP_LOG_MINY = "Threshold_ChIP-chip_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_CHIP_CHIP_LOG_MAXY = "Threshold_ChIP-chip_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_BRK_FRQ_MINY = "Threshold_BRK_FRQ_" + THRESHOLD_MINY;
    public static final String THRESHOLD_BRK_FRQ_MAXY = "Threshold_BRK_FRQ_" + THRESHOLD_MAXY;

    public static final String THRESHOLD_BRK_FRQ_LOG_MINY = "Threshold_BRK_FRQ_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_BRK_FRQ_LOG_MAXY = "Threshold_BRK_FRQ_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_FRAGL_MINY = "Threshold_FrAGL_" + THRESHOLD_MINY;
    public static final String THRESHOLD_FRAGL_MAXY = "Threshold_FrAGL_" + THRESHOLD_MAXY;

    public static final String THRESHOLD_FRAGL_LOG_MINY = "Threshold_FrAGL_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_FRAGL_LOG_MAXY = "Threshold_FrAGL_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_KARYO_FRAGL_MINY = "Threshold_KaryoFrAGL_" + THRESHOLD_MINY;
    public static final String THRESHOLD_KARYO_FRAGL_MAXY = "Threshold_KaryoFrAGL_" + THRESHOLD_MAXY;

    public static final String THRESHOLD_KARYO_FRAGL_LOG_MINY = "Threshold_KaryoFrAGL_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_KARYO_FRAGL_LOG_MAXY = "Threshold_KaryoFrAGL_" + THRESHOLD_LOG_MAXY;

    public static final String THRESHOLD_DIFFANA_MINY = "Threshold_DiffAna_" + THRESHOLD_MINY;
    public static final String THRESHOLD_DIFFANA_MAXY = "Threshold_DiffAna_" + THRESHOLD_MAXY;

    public static final String THRESHOLD_DIFFANA_LOG_MINY = "Threshold_DiffAna_" + THRESHOLD_LOG_MINY;
    public static final String THRESHOLD_DIFFANA_LOG_MAXY = "Threshold_DiffAna_LogMaxY";

    public static final String GNL_LOST_FG = "GNLLostFG";
    public static final String GNL_NORMAL_FG = "GNLNormalFG";
    public static final String GNL_GAINED_FG = "GNLGainedFG";
    public static final String GNL_AMPLICON_FG = "GNLAmpliconFG";
    public static final String GNL_UNKNOWN_FG = "GNLUnknownFG";

    public static final String EXON_FG = "ExonFG";
    public static final String INTRON_FG = "IntronFG";
    public static final String UTR5_FG = "Utr5FG";
    public static final String UTR3_FG = "Utr3FG";

    public static final String EXON_HEIGHT = "ExonHeight";
    public static final String UTR_HEIGHT = "UtrHeight";
    public static final String INTRON_HEIGHT = "IntronHeight";

    public static final String MIN_HEIGHT_PAINT = "MinHeightPaint";

    static ColorResourceBuilder colorBuilder = new ColorResourceBuilder();
    static FontResourceBuilder fontBuilder = new FontResourceBuilder();
    static BooleanResourceBuilder boolBuilder = new BooleanResourceBuilder();
    static IntegerResourceBuilder intBuilder = new IntegerResourceBuilder();
    static DoubleResourceBuilder doubleBuilder = new DoubleResourceBuilder();

    static Resources resources;
    static ResourceItemList resItemList;

    static void init() {
	resItemList = new ResourceItemList();

	resItemList.add(DIALOG_BG, colorBuilder, new Color(0xccccff));
	resItemList.add(DIALOG_BUTTON_BG, colorBuilder, Color.WHITE);
	resItemList.add(DIALOG_BUTTON_FG, colorBuilder, Color.BLACK);
	resItemList.add(DIALOG_FONT, fontBuilder,
			new Font("MonoSpaced", Font.BOLD, 14));
	resItemList.add(DIALOG_BUTTON_FONT, fontBuilder,
			new Font("Serif", Font.BOLD, 14));

	resItemList.add(TABBED_PANE_FONT, fontBuilder,
			new Font("MonoSpaced", Font.BOLD, 10));

	resItemList.add(HORIZONTAL_INFO, boolBuilder,
			new Boolean(false));

	resItemList.add(SCALING_WHILE_ADJUSTING, boolBuilder,
			new Boolean(false));
	resItemList.add(SCROLLING_WHILE_ADJUSTING, boolBuilder,
			new Boolean(true));

	resItemList.add(TITLE_PANEL_BG, colorBuilder, new Color(0xffffd0));
	//resItemList.add(INFO_PANEL_BG, colorBuilder, new Color(0xffffaa));
	resItemList.add(INFO_PANEL_BG, colorBuilder, Color.WHITE);
	resItemList.add(INFO_TITLE_FG, colorBuilder, Color.BLACK);
	resItemList.add(INFO_PROPERTY_FG, colorBuilder, Color.BLACK);
	resItemList.add(INFO_VALUE_FG, colorBuilder, Color.GRAY);
	resItemList.add(INFO_TOP_TITLE_FG, colorBuilder, Color.BLACK);
	resItemList.add(INFO_BOTTOM_TITLE_FG, colorBuilder, Color.GRAY);

	resItemList.add(TAB_BG, colorBuilder, Color.WHITE);

	resItemList.add(REGION_BG, colorBuilder,
			new Color(0xffffd0));
	resItemList.add(MARK_FG, colorBuilder,
			new Color((47<<16)|(130<<8)|124));

	Color color = new Color(0xaaaaff);
	//Color color = new Color(0xff0000);
	resItemList.add(DATASET_SELECTED_FG, colorBuilder, color);
	resItemList.add(MARK_SELECTED_FG, colorBuilder, color);
	resItemList.add(REGION_SELECTED_FG, colorBuilder, color);

	resItemList.add(DEBUG_INFO, intBuilder, new Integer(0));

	resItemList.add(ITEM_CENTERED_FG, colorBuilder, Color.BLUE);
	resItemList.add(ITEM_CENTERED_SIZE, intBuilder, new Integer(4));
	color = new Color(0xdddddd);
	resItemList.add(INFO_PANEL_PINNED_UP_BG, colorBuilder, color);
	resItemList.add(DATASET_PINNED_UP_FG, colorBuilder, color);
	resItemList.add(ITEM_PINNED_UP_FG, colorBuilder, Color.BLACK);
	resItemList.add(ITEM_PINNED_UP_SIZE, intBuilder, new Integer(4));
	resItemList.add(ITEM_TAG_FG, colorBuilder, Color.BLACK);
	resItemList.add(ITEM_TAG_SIZE, intBuilder, new Integer(4));
	resItemList.add(MARK_CENTERED_FG, colorBuilder, Color.BLUE);

	resItemList.add(CGH_ARRAY_LIGHT_IMPORTED_FG, colorBuilder, new Color(0xffffaa));
	resItemList.add(TRANSCRIPTOME_LIGHT_IMPORTED_FG, colorBuilder, Color.ORANGE);

	resItemList.add(AXIS_NORTH_SIZE, intBuilder, new Integer(45));
	resItemList.add(AXIS_SOUTH_SIZE, intBuilder, new Integer(40));
	resItemList.add(AXIS_WEST_SIZE, intBuilder, new Integer(90));
	resItemList.add(AXIS_EAST_SIZE, intBuilder, new Integer(40));

	resItemList.add(VIEW_DEFAULT_WIDTH, intBuilder, new Integer(700));
	resItemList.add(VIEW_DEFAULT_HEIGHT, intBuilder, new Integer(600));

	resItemList.add(CANVAS_BG, colorBuilder, new Color(0xccccff));
	resItemList.add(CANVAS_LOCKED_BG, colorBuilder, new Color(0xeeeeff));
	//resItemList.add(CANVAS_RUNNING_BG, colorBuilder,  new Color(0xeecccc));
	resItemList.add(CANVAS_RUNNING_BG, colorBuilder,  new Color(0xdddddd));

	resItemList.add(PRINT_PREVIEW_EDIT_BG, colorBuilder,
			new Color(0xddddff));

	//	resItemList.add(MINIMAP_BG, colorBuilder, new Color(0xffffd0));
	resItemList.add(MINIMAP_BG, colorBuilder, new Color(0xffffff));
	resItemList.add(MINIMAP_TITLE_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 10));
	resItemList.add(MINIMAP_LABEL_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 9));
	resItemList.add(MINIMAP_TITLE_FG, colorBuilder, Color.BLACK);
	resItemList.add(MINIMAP_LABEL_FG, colorBuilder, Color.BLACK);
	resItemList.add(MINIMAP_LOCATION_FG, colorBuilder, Color.RED);
	resItemList.add(MINIMAP_LOCATION_WIDTH, intBuilder, new Integer(6));
	resItemList.add(MINIMAP_HIDE, boolBuilder, new Boolean(false));


	Color tabPanelBG = Color.ORANGE;
	resItemList.add(VIEW_BG, colorBuilder, Color.WHITE);
	resItemList.add(ZOOM_PANEL_BG, colorBuilder, tabPanelBG);
	resItemList.add(SEARCH_PANEL_BG, colorBuilder, tabPanelBG);
	resItemList.add(COLOR_LEGEND_PANEL_BG, colorBuilder, tabPanelBG);
	resItemList.add(COLOR_LEGEND_PANEL_BUTTON_BG, colorBuilder, Color.WHITE);
	resItemList.add(THRESHOLD_PANEL_BG, colorBuilder, tabPanelBG);
	resItemList.add(THRESHOLD_PANEL_BUTTON_BG, colorBuilder, Color.WHITE);
	resItemList.add(AXIS_BG, colorBuilder, new Color(0xffffd0));
	resItemList.add(AXIS_FG, colorBuilder, Color.BLACK);
	resItemList.add(AXIS_LINE_FG, colorBuilder, Color.LIGHT_GRAY);
	resItemList.add(AXIS_LABEL_FG, colorBuilder, Color.BLACK);

	resItemList.add(AXIS_TRANSCRIPTOME_FG, colorBuilder, Color.GRAY);
	resItemList.add(AXIS_TRANSCRIPTOME_LINE_FG, colorBuilder, Color.BLUE);
	resItemList.add(AXIS_TRANSCRIPTOME_LABEL_FG, colorBuilder, Color.GRAY);

	resItemList.add(AXIS_TRANSCRIPTOME_REF_FG, colorBuilder,
			new Color(0x770033));
	resItemList.add(AXIS_TRANSCRIPTOME_REF_LINE_FG, colorBuilder, Color.BLUE);
	resItemList.add(AXIS_TRANSCRIPTOME_REF_LABEL_FG, colorBuilder, new Color(0x770033));

	resItemList.add(AXIS_TRANSCRIPTOME_REL_FG, colorBuilder, Color.GRAY);
	resItemList.add(AXIS_TRANSCRIPTOME_REL_LINE_FG, colorBuilder, Color.BLUE);
	resItemList.add(AXIS_TRANSCRIPTOME_REL_LABEL_FG, colorBuilder, Color.GRAY);

	resItemList.add(TRANSCRIPTOME_MERGE_COLOR_BASE, colorBuilder, Color.WHITE);

	resItemList.add(PROBE_SET_FG, colorBuilder, Color.WHITE);

	resItemList.add(AXIS_Y_PROPERTY_NAME_FG, colorBuilder, Color.BLUE);
	resItemList.add(AXIS_Y_CANVAS_PROPERTY_NAME_FG, colorBuilder, Color.BLACK);

	resItemList.add(CHROMOSOME_SEPARATOR_FG, colorBuilder, Color.ORANGE);
	resItemList.add(CHROMOSOME_LINE_SEPARATOR_FG, colorBuilder,
			Color.GRAY);
	resItemList.add(CHROMOSOME_SEPARATOR_DASH_WIDTH, intBuilder, new Integer(10));
	resItemList.add(CHROMOSOME_SEPARATOR_DASH_PADDING, intBuilder, new Integer(4));

	resItemList.add(CLONE_NA_FG, colorBuilder, new Color(0xaaaaaa));
	resItemList.add(CLONE_OUTLIER_FG, colorBuilder, Color.BLACK);

	resItemList.add(DEFAULT_SLIDE_RATIO, intBuilder,
			new Integer(100));

	resItemList.add(DEFAULT_XSLIDE_MIN, intBuilder,
			new Integer(-2));
	resItemList.add(DEFAULT_XSLIDE_MAX, intBuilder,
			new Integer(20));
	resItemList.add(DEFAULT_YSLIDE_MIN, intBuilder,
			new Integer(-2));
	resItemList.add(DEFAULT_YSLIDE_MAX, intBuilder,
			new Integer(10));

	resItemList.add(DOTPLOT_XSLIDE_MIN, intBuilder,
			new Integer(-2));
	resItemList.add(DOTPLOT_XSLIDE_MAX, intBuilder,
			new Integer(8));
	resItemList.add(DOTPLOT_YSLIDE_MIN, intBuilder,
			new Integer(-6));
	resItemList.add(DOTPLOT_YSLIDE_MAX, intBuilder,
			new Integer(8));

	resItemList.add(KARYO_XSLIDE_MIN, intBuilder,
			new Integer(-2));
	resItemList.add(KARYO_XSLIDE_MAX, intBuilder,
			new Integer(6));
	resItemList.add(KARYO_YSLIDE_MIN, intBuilder,
			new Integer(0));
	resItemList.add(KARYO_YSLIDE_MAX, intBuilder,
			new Integer(5));

	resItemList.add(POINT_NA_WIDTH, intBuilder,
			new Integer(2));
	resItemList.add(POINT_WIDTH, intBuilder,
			new Integer(2));
	resItemList.add(POINT_OUT_WIDTH, intBuilder,
			new Integer(2));
	resItemList.add(LINE_WIDTH, intBuilder,
			new Integer(1));
	resItemList.add(OVAL_WIDTH, intBuilder,
			new Integer(4));

	resItemList.add(POINT_KARYO_WIDTH, intBuilder,
			new Integer(2));

	resItemList.add(SAMPLE_ANNOT_WIDTH, intBuilder,
			new Integer(5));

	resItemList.add(SAMPLE_ANNOT_MAX, intBuilder,
			new Integer(30));

	resItemList.add(MIN_KARYO_CHROMOSOME_WIDTH, intBuilder,
			new Integer(5)); // new Integer(3));

	resItemList.add(MAX_KARYO_CHROMOSOME_WIDTH, intBuilder,
			new Integer(20)); // new Integer(50));

	resItemList.add(SEARCH_PANEL_BUTTON_BG, colorBuilder, Color.WHITE);

	resItemList.add(BREAKPOINT_FG, colorBuilder, Color.RED);
	resItemList.add(SMOOTHING_POINT_FG, colorBuilder, Color.BLACK);
	resItemList.add(SMOOTHING_LINE_FG, colorBuilder, Color.BLACK);
	resItemList.add(SMOOTHING_POINT_WIDTH, intBuilder, new Integer(2));
	resItemList.add(CENTROMERE_FG, colorBuilder, Color.BLACK);
	resItemList.add(CENTROMERE_BASE_WIDTH, intBuilder, new Integer(6));
	resItemList.add(BREAKPOINT_DASH_WIDTH, intBuilder, new Integer(2));
	resItemList.add(BREAKPOINT_DASH_PADDING, intBuilder, new Integer(2));
	resItemList.add(MENU_MAX_ITEMS, intBuilder, new Integer(35));

	resItemList.add(INFO_PANEL_TITLE_FONT, fontBuilder,
			new Font("SansSerif", Font.BOLD, 10));

	resItemList.add(INFO_PANEL_TEXT_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 10));

	resItemList.add(INFO_PANEL_TYPE_FONT, fontBuilder,
			new Font("SansSerif", Font.BOLD, 12));

	resItemList.add(DATASET_DISPLAYER_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 8));

	resItemList.add(AXIS_X_DISPLAYER_FONT, fontBuilder,
			new Font("Serif", Font.PLAIN, 9));

	resItemList.add(AXIS_Y_DISPLAYER_FONT, fontBuilder,
			new Font("Serif", Font.PLAIN, 9));

	resItemList.add(AXIS_Y_NAME_DISPLAYER_FONT, fontBuilder,
			new Font("Serif", Font.PLAIN, 9));

	resItemList.add(AXIS_KARYO_DISPLAYER_FONT, fontBuilder,
			new Font("MonoSpaced", Font.PLAIN, 8));

	resItemList.add(AXIS_KARYO_SMALL_DISPLAYER_FONT, fontBuilder,
			new Font("MonoSpaced", Font.BOLD, 7));

	resItemList.add(AXIS_KARYO_NAME_DISPLAYER_FONT, fontBuilder,
			new Font("Serif", Font.BOLD, 9));

	resItemList.add(AXIS_MESSAGE_FONT, fontBuilder,
			new Font("MonoSpaced", Font.BOLD, 10));

	resItemList.add(AXIS_MESSAGE_COLOR, colorBuilder,
			Color.RED);

	resItemList.add(NAME_DATASET_DISPLAYER_FONT, fontBuilder,
			new Font("SansSerif", Font.BOLD, 10));

	resItemList.add(SEARCH_PANEL_BUTTON_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 9));

	resItemList.add(ZOOM_PANEL_LABEL_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 9));
	resItemList.add(ZOOM_PANEL_TEXTFIELD_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 10));

	resItemList.add(COLOR_LEGEND_PANEL_LABEL_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 9));

	resItemList.add(THRESHOLD_PANEL_LABEL_FONT, fontBuilder,
			new Font("SansSerif", Font.PLAIN, 9));

	resItemList.add(THRESHOLD_MINY_FG, colorBuilder, Color.GREEN);
	resItemList.add(THRESHOLD_MAXY_FG, colorBuilder, Color.RED);

	double t_maxY = 2.8;
	resItemList.add(THRESHOLD_CGH_ARRAY_MAXY, true, doubleBuilder,
			new Double(t_maxY));

	double t_minY = 0.;
	resItemList.add(THRESHOLD_CGH_ARRAY_MINY, true, doubleBuilder,
			new Double(t_minY));

	resItemList.add(THRESHOLD_CGH_ARRAY_LOG_MINY, true, doubleBuilder,
			new Double(-2.));

	resItemList.add(THRESHOLD_CGH_ARRAY_LOG_MAXY, true, doubleBuilder,
			new Double(2.));

	resItemList.add(THRESHOLD_SNP_MAXY, true, doubleBuilder,
			new Double(8.));

	resItemList.add(THRESHOLD_SNP_MINY, true, doubleBuilder,
			new Double(0.1));

	resItemList.add(THRESHOLD_SNP_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(8.)));

	resItemList.add(THRESHOLD_SNP_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(0.1)));

	resItemList.add(THRESHOLD_LOH_MAXY, true, doubleBuilder,
			new Double(10.));

	resItemList.add(THRESHOLD_LOH_MINY, true, doubleBuilder,
			new Double(-0.1));

	resItemList.add(THRESHOLD_LOH_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(10.)));

	resItemList.add(THRESHOLD_LOH_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(-0.1)));

	double trans_maxy = 5000;
	resItemList.add(THRESHOLD_TRANSCRIPTOME_MAXY, true, doubleBuilder,
			new Double(trans_maxy));

	resItemList.add(THRESHOLD_TRANSCRIPTOME_MINY, true, doubleBuilder,
			new Double(-10.));

	resItemList.add(THRESHOLD_TRANSCRIPTOME_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(trans_maxy)));

	resItemList.add(THRESHOLD_TRANSCRIPTOME_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(-10.)));

	resItemList.add(THRESHOLD_TRANSCRIPTOME_REL_MAXY, true, doubleBuilder,
			new Double(10));
	resItemList.add(THRESHOLD_TRANSCRIPTOME_REL_MINY, true, doubleBuilder,
			new Double(0.25));

	resItemList.add(THRESHOLD_TRANSCRIPTOME_REL_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(10)));
	resItemList.add(THRESHOLD_TRANSCRIPTOME_REL_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(0.25)));

	double chip_chip_maxy = 16;
	resItemList.add(THRESHOLD_CHIP_CHIP_MAXY, true, doubleBuilder,
			new Double(chip_chip_maxy));

	resItemList.add(THRESHOLD_CHIP_CHIP_MINY, true, doubleBuilder,
			new Double(0.01));

	resItemList.add(THRESHOLD_CHIP_CHIP_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(chip_chip_maxy)));

	resItemList.add(THRESHOLD_CHIP_CHIP_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(0.01)));

	resItemList.add(THRESHOLD_BRK_FRQ_MAXY, true, doubleBuilder,
			new Double(1.));

	resItemList.add(THRESHOLD_BRK_FRQ_MINY, true, doubleBuilder,
			new Double(0.00));

	resItemList.add(THRESHOLD_BRK_FRQ_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(1.)));

	resItemList.add(THRESHOLD_BRK_FRQ_LOG_MINY, true, doubleBuilder,
			new Double(-2));

	resItemList.add(THRESHOLD_FRAGL_MAXY, true, doubleBuilder,
			new Double(1.));

	resItemList.add(THRESHOLD_FRAGL_MINY, true, doubleBuilder,
			new Double(-1.));

	resItemList.add(THRESHOLD_FRAGL_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(1.)));

	resItemList.add(THRESHOLD_FRAGL_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(-1.)));

	resItemList.add(THRESHOLD_KARYO_FRAGL_MAXY, true, doubleBuilder,
			new Double(10.));

	resItemList.add(THRESHOLD_KARYO_FRAGL_MINY, true, doubleBuilder,
			new Double(-10.));

	resItemList.add(THRESHOLD_KARYO_FRAGL_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(10.)));

	resItemList.add(THRESHOLD_KARYO_FRAGL_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(-10.)));

	resItemList.add(THRESHOLD_DIFFANA_MAXY, true, doubleBuilder,
			new Double(1024));

	resItemList.add(THRESHOLD_DIFFANA_MINY, true, doubleBuilder,
			new Double(0.001));

	resItemList.add(THRESHOLD_DIFFANA_LOG_MAXY, true, doubleBuilder,
			new Double(Utils.log(1024)));

	resItemList.add(THRESHOLD_DIFFANA_LOG_MINY, true, doubleBuilder,
			new Double(Utils.log(0.001)));

	resItemList.add(GNL_LOST_FG, colorBuilder, Color.GREEN);
	resItemList.add(GNL_NORMAL_FG, colorBuilder, Color.YELLOW);
	resItemList.add(GNL_GAINED_FG, colorBuilder, Color.RED);
	resItemList.add(GNL_AMPLICON_FG, colorBuilder, Color.BLUE);
	resItemList.add(GNL_UNKNOWN_FG, colorBuilder, new Color(0x000088));

	resItemList.add(EXON_FG, colorBuilder, new Color(0xFF0000));
	resItemList.add(INTRON_FG, colorBuilder, Color.YELLOW);
	resItemList.add(UTR3_FG, colorBuilder, new Color(0x00EE00));
	resItemList.add(UTR5_FG, colorBuilder, new Color(0x006600));

	resItemList.add(UTR_HEIGHT, intBuilder, new Integer(2));
	resItemList.add(EXON_HEIGHT, intBuilder, new Integer(8));
	resItemList.add(INTRON_HEIGHT, intBuilder, new Integer(2));

	resItemList.add(MIN_HEIGHT_PAINT, doubleBuilder, new Double(1.));

	initColorCodes();

	reset(null, true);

	try {
	    String home = VAMPUtils.getHomeDir(null);
	    if (home != null) {
		File cfg = new File(home + "/" + ImportData.VAMPDIR + "/config.xml");
		if (cfg.canRead()) {
		    //System.out.println("Loading Resources File " + cfg.getAbsolutePath());
		    FileInputStream fis = new FileInputStream(cfg);
		    read(null, fis);
		}
	    }
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }

    static private String defConfigURL;

    static void reset(GlobalContext globalContext, boolean restore_def_config) {
	resources = new Resources(resItemList);

	if (globalContext != null && restore_def_config && defConfigURL != null) {
	    try {
		read(globalContext, defConfigURL);
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }
	}
    }

    static boolean hasDefaultConfig() {
	return defConfigURL != null;
    }

    static void setDefaultConfiguration(GlobalContext globalContext,
					String _defConfigURL)
	throws MalformedURLException, IOException {
	read(globalContext, _defConfigURL);
	defConfigURL = _defConfigURL;
    }

    static ResourceItem getResourceItem(String name) {
	return resItemList.get(name);
    }

    public static Object get(String name) {
	return resources.get(name);
    }

    public static Color getColor(String name) {
	return resources.getColor(name);
    }

    public static Font getFont(String name) {
	return resources.getFont(name);
    }

    public static int getInt(String name) {
	return resources.getInt(name);
    }

    public static double getDouble(String name) {
	return resources.getDouble(name);
    }

    public static boolean getBool(String name) {
	return resources.getBool(name);
    }

    static void read(GlobalContext globalContext, InputStream is) {
	resources.read(globalContext, is);
    }

    static void write(GlobalContext globalContext, OutputStream os) {
	resources.write(globalContext, os, Resources.HEADER|Resources.ALL);
    }

    static void writeParameters(GlobalContext globalContext, OutputStream os) {
	resources.write(globalContext, os, Resources.PARAMETERS_ONLY);
    }

    static void read(GlobalContext globalContext, String _defConfigURL)
	throws MalformedURLException, IOException {
	InputStream is = Utils.openStream(_defConfigURL);
	read(globalContext, is);
	is.close();
    }

    public static final Color LIGHTYELLOW = new Color(0xffffd0);
    public static final Color LIGHTPINK_1 = new Color(0xffbbbb);
    public static final Color LIGHTPINK_2 = new Color(0xffdddd);
    public static final Color LIGHTGREEN_1 = new Color(0xbbffbb);
    public static final Color LIGHTGREEN_2 = new Color(0xeeffee);

    public static final String getResPrefix(Thresholds thresholds) {
	return "Threshold_" + thresholds.getCanName() + "_";
    }

    public static final String getResPrefix(ColorCodes cc) {
	String cname = cc.getCodeName();
	if (!cname.equals(VAMPConstants.CC_LOH))
	    return getResPrefix(cname.substring(0,
						cname.length() - VAMPConstants.CC_SUFFIX_LENGTH));
	return getResPrefix(cname);
    }

    public static final String getResPrefix(String codeName) {
	return "ColorCode_" + codeName + "_";
    }

    private static final double CC_CGH_MIN = 0.6;
    private static final double CC_CGH_NORMAL_MIN = 0.9;
    private static final double CC_CGH_NORMAL_MAX = 1.1;
    private static final double CC_CGH_MAX = 1.4;
    private static final double CC_CGH_AMPLICON = 3.;
    private static final Color CC_CGH_MIN_FG = Color.GREEN;
    private static final Color CC_CGH_NORMAL_FG = Color.YELLOW;
    private static final Color CC_CGH_MAX_FG = Color.RED;
    private static final Color CC_CGH_AMPLICON_FG = Color.BLUE;

    private static final double CC_CHIP_CHIP_MIN = Utils.pow(-1.5);
    private static final double CC_CHIP_CHIP_NORMAL_MIN = Utils.pow(-0.5);
    private static final double CC_CHIP_CHIP_NORMAL_MAX = Utils.pow(0);
    private static final double CC_CHIP_CHIP_MAX = Utils.pow(1);
    private static final double CC_CHIP_CHIP_AMPLICON = Utils.pow(2);
    private static final Color CC_CHIP_CHIP_MIN_FG = Color.GREEN;
    private static final Color CC_CHIP_CHIP_NORMAL_FG = Color.YELLOW;
    private static final Color CC_CHIP_CHIP_MAX_FG = Color.RED;
    private static final Color CC_CHIP_CHIP_AMPLICON_FG = Color.BLUE;

    private static final double CC_TRSREL_MIN = 0.6;
    private static final double CC_TRSREL_NORMAL_MIN = 0.9;
    private static final double CC_TRSREL_NORMAL_MAX = 1.1;
    private static final double CC_TRSREL_MAX = 1.4;
    private static final double CC_TRSREL_AMPLICON = 1.4;
    private static final Color CC_TRSREL_MIN_FG = Color.BLUE;
    private static final Color CC_TRSREL_NORMAL_FG = Color.YELLOW;
    private static final Color CC_TRSREL_MAX_FG = Color.RED;
    private static final Color CC_TRSREL_AMPLICON_FG = Color.BLUE;

    private static final double CC_ABS_TRSCLS_MIN = 0.;
    private static final double CC_ABS_TRSCLS_NORMAL_MIN = 4;
    private static final double CC_ABS_TRSCLS_NORMAL_MAX = 5;
    private static final double CC_ABS_TRSCLS_MAX = 14;
    private static final double CC_ABS_TRSCLS_AMPLICON = 90;
    private static final Color CC_ABS_TRSCLS_MIN_FG = Color.GREEN;
    private static final Color CC_ABS_TRSCLS_NORMAL_FG = Color.YELLOW;
    private static final Color CC_ABS_TRSCLS_MAX_FG = Color.RED;
    private static final Color CC_ABS_TRSCLS_AMPLICON_FG = Color.BLUE;

    private static final double CC_REL_TRSCLS_MIN = -4.;
    private static final double CC_REL_TRSCLS_NORMAL_MIN = -2;
    private static final double CC_REL_TRSCLS_NORMAL_MAX = 0;
    private static final double CC_REL_TRSCLS_MAX = 2;
    private static final double CC_REL_TRSCLS_AMPLICON = 90;
    private static final Color CC_REL_TRSCLS_MIN_FG = Color.GREEN;
    private static final Color CC_REL_TRSCLS_NORMAL_FG = Color.YELLOW;
    private static final Color CC_REL_TRSCLS_MAX_FG = Color.RED;
    private static final Color CC_REL_TRSCLS_AMPLICON_FG = Color.BLUE;

    private static final double CC_LOH_MIN = 0.50;
    private static final double CC_LOH_LOH = 1.00;
    private static final double CC_LOH_MAX = 3.;

    private static final Color CC_LOH_MIN_FG = Color.GREEN;
    private static final Color CC_LOH_MAX_FG = Color.BLUE;
    private static final Color CC_LOH_LOH_FG = Color.YELLOW;

    private static final double CC_SNP_MIN = 1.2;
    private static final double CC_SNP_NORMAL_MIN = 2.;
    private static final double CC_SNP_NORMAL_MAX = 2.;
    private static final double CC_SNP_MAX = 4.;
    private static final double CC_SNP_AMPLICON = 5;
    private static final Color CC_SNP_MIN_FG = Color.GREEN;
    private static final Color CC_SNP_NORMAL_FG = Color.YELLOW;
    private static final Color CC_SNP_MAX_FG = Color.RED;
    private static final Color CC_SNP_AMPLICON_FG = Color.BLUE;

    private static final double CC_GTCA_MIN = 0.6598;
    private static final double CC_GTCA_NORMAL_MIN = 0.8706;
    private static final double CC_GTCA_NORMAL_MAX = 1.1487;
    private static final double CC_GTCA_MAX = 1.5157;
    private static final double CC_GTCA_AMPLICON = 4.0;
    private static final Color CC_GTCA_MIN_FG = Color.RED;
    private static final Color CC_GTCA_NORMAL_FG = Color.YELLOW;
    private static final Color CC_GTCA_MAX_FG = Color.RED;
    private static final Color CC_GTCA_AMPLICON_FG = Color.BLUE;

    static void initColorCodes() {
	String resPrefix;

	// CC_CGH
	resPrefix = getResPrefix(VAMPConstants.CC_CGH);

	resItemList.add(resPrefix + COLOR_CODE_YMIN, true, doubleBuilder,
			new Double(CC_CGH_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MIN, true, doubleBuilder,
			new Double(CC_CGH_NORMAL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MAX, true, doubleBuilder,
			new Double(CC_CGH_NORMAL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_CGH_MAX));
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON, true, doubleBuilder,
			new Double(CC_CGH_AMPLICON));
	
	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_CGH_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_NORMAL_FG, true, colorBuilder,
			CC_CGH_NORMAL_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_CGH_MAX_FG);
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON_FG, true, colorBuilder,
			CC_CGH_AMPLICON_FG);

	resItemList.add(resPrefix + COLOR_CODE_CONTINUOUS_MODE, true, boolBuilder,
			new Boolean(true));

	// CC_CHIP_CHIP
	resPrefix = getResPrefix(VAMPConstants.CC_CHIP_CHIP);

	resItemList.add(resPrefix + COLOR_CODE_YMIN,true,  doubleBuilder,
			new Double(CC_CHIP_CHIP_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MIN, true, doubleBuilder,
			new Double(CC_CHIP_CHIP_NORMAL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MAX, true, doubleBuilder,
			new Double(CC_CHIP_CHIP_NORMAL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_CHIP_CHIP_MAX));
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON, true, doubleBuilder,
			new Double(CC_CHIP_CHIP_AMPLICON));
	
	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_CHIP_CHIP_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_NORMAL_FG, true, colorBuilder,
			CC_CHIP_CHIP_NORMAL_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_CHIP_CHIP_MAX_FG);
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON_FG, true, colorBuilder,
			CC_CHIP_CHIP_AMPLICON_FG);

	resItemList.add(resPrefix + COLOR_CODE_CONTINUOUS_MODE, true, boolBuilder,
			new Boolean(true));

	// CC_SNP
	resPrefix = getResPrefix(VAMPConstants.CC_SNP);

	resItemList.add(resPrefix + COLOR_CODE_YMIN, true,  doubleBuilder,
			new Double(CC_SNP_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MIN, true, doubleBuilder,
			new Double(CC_SNP_NORMAL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MAX, true, doubleBuilder,
			new Double(CC_SNP_NORMAL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_SNP_MAX));
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON, true, doubleBuilder,
			new Double(CC_SNP_AMPLICON));
	
	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_SNP_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_NORMAL_FG, true, colorBuilder,
			CC_SNP_NORMAL_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_SNP_MAX_FG);
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON_FG, true, colorBuilder,
			CC_SNP_AMPLICON_FG);

	resItemList.add(resPrefix + COLOR_CODE_CONTINUOUS_MODE, true,
			boolBuilder, new Boolean(true));

	// CC_TRSREL
	resPrefix = getResPrefix(VAMPConstants.CC_TRSREL);

	resItemList.add(resPrefix + COLOR_CODE_YMIN, true, doubleBuilder,
			new Double(CC_TRSREL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MIN, true, doubleBuilder,
			new Double(CC_TRSREL_NORMAL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MAX, true, doubleBuilder,
			new Double(CC_TRSREL_NORMAL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_TRSREL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON, true, doubleBuilder,
			new Double(CC_TRSREL_AMPLICON));
	
	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_TRSREL_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_NORMAL_FG, true, colorBuilder,
			CC_TRSREL_NORMAL_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_TRSREL_MAX_FG);
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON_FG, true, colorBuilder,
			CC_TRSREL_AMPLICON_FG);

	resItemList.add(resPrefix + COLOR_CODE_CONTINUOUS_MODE, true, boolBuilder,
			new Boolean(true));

	// CC_ABS_TRSCLS
	resPrefix = getResPrefix(VAMPConstants.CC_ABS_TRSCLS);

	resItemList.add(resPrefix + COLOR_CODE_YMIN, true, doubleBuilder,
			new Double(CC_ABS_TRSCLS_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MIN, true, doubleBuilder,
			new Double(CC_ABS_TRSCLS_NORMAL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MAX, true, doubleBuilder,
			new Double(CC_ABS_TRSCLS_NORMAL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_ABS_TRSCLS_MAX));
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON, true, doubleBuilder,
			new Double(CC_ABS_TRSCLS_AMPLICON));
	
	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_ABS_TRSCLS_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_NORMAL_FG, true, colorBuilder,
			CC_ABS_TRSCLS_NORMAL_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_ABS_TRSCLS_MAX_FG);
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON_FG, true, colorBuilder,
			CC_ABS_TRSCLS_AMPLICON_FG);

	resItemList.add(resPrefix + COLOR_CODE_CONTINUOUS_MODE, true, boolBuilder,
			new Boolean(true));

	// CC_REL_TRSCLS
	resPrefix = getResPrefix(VAMPConstants.CC_REL_TRSCLS);

	resItemList.add(resPrefix + COLOR_CODE_YMIN, true, doubleBuilder,
			new Double(CC_REL_TRSCLS_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MIN, true, doubleBuilder,
			new Double(CC_REL_TRSCLS_NORMAL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MAX, true, doubleBuilder,
			new Double(CC_REL_TRSCLS_NORMAL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_REL_TRSCLS_MAX));
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON, true, doubleBuilder,
			new Double(CC_REL_TRSCLS_AMPLICON));
	
	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_REL_TRSCLS_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_NORMAL_FG, true, colorBuilder,
			CC_REL_TRSCLS_NORMAL_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_REL_TRSCLS_MAX_FG);
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON_FG, true, colorBuilder,
			CC_REL_TRSCLS_AMPLICON_FG);

	resItemList.add(resPrefix + COLOR_CODE_CONTINUOUS_MODE, true, boolBuilder,
			new Boolean(true));

	// CC_LOH
	resPrefix = getResPrefix(VAMPConstants.CC_LOH);

	resItemList.add(resPrefix + COLOR_CODE_YMIN, true, doubleBuilder,
			new Double(CC_LOH_MIN));

	resItemList.add(resPrefix + COLOR_CODE_LOH, true, doubleBuilder,
			new Double(CC_LOH_LOH));

	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_LOH_MAX));

	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_LOH_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_LOH_FG, true, colorBuilder,
			CC_LOH_LOH_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_LOH_MAX_FG);

	// CC_GTCA
	resPrefix = getResPrefix(VAMPConstants.CC_GTCA);

	resItemList.add(resPrefix + COLOR_CODE_YMIN, true, doubleBuilder,
			new Double(CC_GTCA_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MIN, true, doubleBuilder,
			new Double(CC_GTCA_NORMAL_MIN));
	resItemList.add(resPrefix + COLOR_CODE_YNORMAL_MAX, true, doubleBuilder,
			new Double(CC_GTCA_NORMAL_MAX));
	resItemList.add(resPrefix + COLOR_CODE_YMAX, true, doubleBuilder,
			new Double(CC_GTCA_MAX));
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON, true, doubleBuilder,
			new Double(CC_GTCA_AMPLICON));
	
	resItemList.add(resPrefix + COLOR_CODE_MIN_FG, true, colorBuilder,
			CC_GTCA_MIN_FG);
	resItemList.add(resPrefix + COLOR_CODE_NORMAL_FG, true, colorBuilder,
			CC_GTCA_NORMAL_FG);
	resItemList.add(resPrefix + COLOR_CODE_MAX_FG, true, colorBuilder,
			CC_GTCA_MAX_FG);
	resItemList.add(resPrefix + COLOR_CODE_AMPLICON_FG, true, colorBuilder,
			CC_GTCA_AMPLICON_FG);

	resItemList.add(resPrefix + COLOR_CODE_CONTINUOUS_MODE, true, boolBuilder,
			new Boolean(true));

	// CODE_COUNT
	resItemList.add(COLOR_CODE_COUNT, intBuilder, new Integer(60));
    }
}
