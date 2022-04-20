
/*
 *
 * Mark.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;
import java.awt.geom.*;

class Mark extends PropertyElement implements Pasteable {

    private MarkTrigger trigger;
    private boolean centered;
    private Color color = VAMPResources.getColor(VAMPResources.MARK_FG);
    private boolean selected = false;
    private Region region = null;
    private GraphPanel graphPanel;
    private boolean endingRegion = false;
    private int xid = 0;
    double posx = 0.;

    Mark(double posx) {
	this.posx = posx;
	setPropertyValue(VAMPProperties.TypeProp, "Landmark");
	setPropertyValue(VAMPProperties.CommentProp, "");
	setPropertyValue(VAMPProperties.TagProp, "");
	setPositionProperty();
	setRegionProperties();
    }

    double getVX(GraphCanvas canvas) {
	return getVX_c(canvas) + canvas.getVW(getRDelta(canvas));
    }
	     
    // xid is used for XML Save and Load convenience
    int getXID() {return xid;}
    void setXID(int xid) {this.xid = xid;}

    double getRX(GraphCanvas canvas) {
	return canvas.getRX(getVX_c(canvas)) + getRDelta(canvas);
    }
	     
    double getPosX() {
	return posx;
    }

    private double getRDelta(GraphCanvas canvas) {
	if (!endingRegion)
	    return 0;

	if (!canvas.getGraphPanel().getDefaultGraphElementDisplayer().needDeltaEndRegion())
	    return 0;

	DataSet templDS = canvas.getTemplateDS();

	if (templDS == null) return 0;
	DataElement data = templDS.dataAtPosX(posx);
	if (data == null) return 0;

	Rectangle2D.Double rbounds = data.getRBounds(templDS);
	
	if (data.getRX(templDS) > rbounds.x) return 0;

	// 14/09/04: suppressed -1
	//return rbounds.width-1;
	return rbounds.width;
    }

    private double getVX_c(GraphCanvas canvas) {
	if (!canvas.getGraphPanel().getDefaultGraphElementDisplayer().isVXRelocated())
	    return posx;

	DataSet templDS = canvas.getTemplateDS();
	return templDS.posxToVX(posx);
    }

    void setLocation(double posx) {
	if (this.posx == posx)
	    return;

	if (trigger != null) {
	    LinkedList v = graphPanel.getGraphElements();
	    boolean mergeChr = v.size() > 0 ?
		VAMPUtils.isMergeChr((GraphElement)v.get(0)) : false;
	    posx = trigger.setLocation(this, mergeChr, posx);
	    if (posx < 0) return;
	}

	this.posx = posx;
	setPositionProperty();

	if (region != null)
	    region.orderMarks();

	if (centered)
	    graphPanel.centerOnMark(this);
    }

    public void setTrigger(MarkTrigger trigger) {
	this.trigger = trigger;
    }

    boolean equalsTo(Mark mark) {
	return posx == mark.posx;
    }

    boolean lessThan(Mark mark) {
	return posx < mark.posx;
    }

    boolean isCentered() {return centered;}
    void setCentered(boolean centered) {this.centered = centered;}

    public void setSelected(boolean selected, Object container) {
	this.selected = selected;
    }

    public boolean isSelected() {
	return selected;
    }

    Color getColor() {return color;}
    void setColor(Color color) {this.color = color;}

    public Object clone() /* throws CloneNotSupportedException */ {
	Mark mark = new Mark(posx);
	mark.color = color;
	mark.region = region;
	mark.cloneProperties(this);
	mark.setTrigger(trigger);
	return mark;
    }

    public Object clone_light() {
	return clone();
    }

    Region getRegion() {return region;}

    void setRegion(Region region) {
	this.region = region;
	endingRegion = false;
	setRegionProperties();
    }

    public void postClone() {
	if (region != null) {
	    region.setCloneMark(this);
	    region = null;
	}
    }

    public void prePaste() {
    }

    public boolean isPasteable(int action) {
	if (!isSelected()) return false;
	if (action != CUT || region == null) return true;
	return region.isPasteable(action);
	/*
	if (action != CUT) return true;
	return region == null;
	*/
    }

    void setGraphPanel(GraphPanel graphPanel) {
	this.graphPanel = graphPanel;
    }

    void setEndingRegion(boolean endingRegion) {
	this.endingRegion = endingRegion;
	setRegionProperties();
    }

    boolean isEndingRegion() {return endingRegion;}

    private void setPositionProperty() {
	setPropertyValue(VAMPProperties.PositionProp, Utils.toString((long)posx));
    }

    private void setRegionProperties() {
	if (region == null) {
	    removeProperty(VAMPProperties.RegionProp);
	    return;
	}

	setPropertyValue
	    (VAMPProperties.RegionProp,
	     Utils.toString((long)region.getBegin().getPosX()) + ":" +
	     Utils.toString((long)region.getEnd().getPosX()));
    }
}
