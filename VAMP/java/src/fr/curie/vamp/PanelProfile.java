
/*
 *
 * PanelProfile.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

public class PanelProfile {

    public static final int SCROLL_NORTH = 0x1;
    public static final int SCROLL_SOUTH = 0x2;
    public static final int SCROLL_WEST = 0x4;
    public static final int SCROLL_EAST = 0x8;

    public static final int READONLY = 0x1;
    public static final int INTERNAL_READONLY = 0x2;
    public static final int DISABLED = 0x4;

    // not changeable
    private String name; // for instance Top, Bottom ...
    private int axisSizes[];
    private int scrollMask;
    private Margins margins;
    private boolean supportX;

    // changeable
    private GraphElementDisplayer defaultGraphElementDisplayer;
    private AxisDisplayer defaultAxisDisplayer;
    private GraphElementIDBuilder graphElementIDBuilder;
    private String bgImg;

    // changeable
    private boolean yaxisAutoAdapt;
    private GraphElementListOperation autoApplyDSLOP;

    // not changeable
    private ZoomTemplate zoomTemplate;
    private Scale defaultScale;

    // changeable
    private int flags;

    public PanelProfile(String name, int axisSizes[],
			int scrollMask,
			GraphElementDisplayer defaultGraphElementDisplayer,
			AxisDisplayer defaultAxisDisplayer,
			GraphElementIDBuilder graphElementIDBuilder,
			boolean yaxisAutoAdapt,
			GraphElementListOperation autoApplyDSLOP,
			ZoomTemplate zoomTemplate,
			Scale defaultScale,
			int flags,
			boolean supportX,
			Margins margins,
			String bgImg) {
	this.name = name;
	this.axisSizes = axisSizes;
	this.scrollMask = scrollMask;
	this.defaultGraphElementDisplayer = defaultGraphElementDisplayer;
	this.defaultAxisDisplayer = defaultAxisDisplayer;

	this.graphElementIDBuilder = graphElementIDBuilder;
	this.yaxisAutoAdapt = yaxisAutoAdapt;
	this.autoApplyDSLOP = autoApplyDSLOP;
	this.zoomTemplate = zoomTemplate;
	this.defaultScale = defaultScale;
	this.flags = flags;
	this.supportX = supportX;
	this.margins = margins;
	this.bgImg = bgImg;
    }

    public String getName() {return name;}
    public int[] getAxisSizes() {return axisSizes;}
    public int getScrollMask() {return scrollMask;}
    public String getBGImg() {return bgImg;}

    public GraphElementDisplayer getDefaultGraphElementDisplayer() {
	return defaultGraphElementDisplayer;
    }

    public AxisDisplayer getDefaultAxisDisplayer() {
	return defaultAxisDisplayer;
    }

    public GraphElementIDBuilder getGraphElementIDBuilder() {
	return graphElementIDBuilder;
    }

    public boolean isYAxisAutoAdapt() {return yaxisAutoAdapt;}
    public GraphElementListOperation getAutoApplyDSLOP() {return autoApplyDSLOP;}
    public ZoomTemplate getZoomTemplate() {return zoomTemplate;}
    public Scale getDefaultScale() {return defaultScale;}

    public boolean isReadOnly() {return (flags & READONLY) != 0;}
    public boolean isInternalReadOnly() {return (flags & INTERNAL_READONLY) != 0;}
    public boolean isDisabled() {return (flags & DISABLED) != 0;}

    public boolean supportX() {return supportX;}
    public Margins getMargins() {return margins;}
}

// all changeable attributes (+ name) are copied in Panel class
