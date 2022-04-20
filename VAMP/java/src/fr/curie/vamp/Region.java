
/*
 *
 * Region.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

class Region extends PropertyElement implements Pasteable {

    private boolean centered;
    private Mark begin, end;
    private Mark begin_c, end_c;
    private Color color;
    private boolean selected = false;
    private GraphPanel graphPanel;
    private boolean pinned_up;

    Region(Mark begin, Mark end) {
	this(begin, end, VAMPResources.getColor(VAMPResources.REGION_BG));
    }

    Region(Mark begin, Mark end, Color color) {
	this.begin = begin;
	this.end = end;
	this.begin.setRegion(this);
	this.end.setRegion(this);
	orderMarks(); // must be called after begin & end.setRegion(this)
	centered = false;
	setColor(color);
	setPropertyValue(VAMPProperties.TypeProp, "Region");
	setPropertyValue(VAMPProperties.CommentProp, "");
	setPropertyValue(VAMPProperties.TagProp, "");
	setPositionProperties();
    }

    void dispose() {
	this.begin.setRegion(null);
	this.end.setRegion(null);
    }

    Color getColor() {return color;}
    void setColor(Color color) {
	this.color = color;
	begin.setColor(color);
	end.setColor(color);
    }

    Mark getBegin() {return begin;}
    Mark getEnd() {return end;}

    public void setSelected(boolean selected, Object container) {
	begin.setSelected(selected, container);
	end.setSelected(selected, container);
	this.selected = selected;
    }

    public boolean isSelected() {return selected;}

    public Object clone() /*throws CloneNotSupportedException */{
	if (begin_c == null || end_c == null) {
	    System.out.println("OUH LA LA: " + begin_c + " | " + end_c);
	}

	Region region = new Region(begin_c, end_c, color);
	region.cloneProperties(this);
	return region;
    }

    public Object clone_light() {
	return clone();
    }

    public Region clone_r() {
	Region region = new Region((Mark)begin.clone(),
				   (Mark)end.clone(), color);

	region.cloneProperties(this);
	return region;
    }

    void setCloneMark(Mark mark) {
	if (mark.equalsTo(begin))
	    begin_c = mark;
	else if (mark.equalsTo(end))
	    end_c = mark;
	else
	    System.err.println("INTERNAL ERROR: " + this + ", setCloneMark: " +
			       mark);
    }

    public void postClone() {
    }

    public void prePaste() {
    }

    public boolean isPasteable(int action) {
	return isSelected();
    }

    void orderMarks() {
	if (end.lessThan(begin)) {
	    Mark mark = begin;
	    begin = end;
	    end = mark;
	}

	setPositionProperties();

	begin.setEndingRegion(false);
	end.setEndingRegion(true);

	if (centered)
	    graphPanel.centerOnRegion(this);
    }

    void setGraphPanel(GraphPanel graphPanel) {
	this.graphPanel = graphPanel;
    }

    boolean isCentered() {return centered;}
    void setCentered(boolean centered) {this.centered = centered;}

    private void setPositionProperties() {
	setPropertyValue(VAMPProperties.PositionBeginProp,
			 Utils.toString((long)begin.getPosX()));
	setPropertyValue(VAMPProperties.PositionEndProp,
			 Utils.toString((long)end.getPosX()));
	setPropertyValue(VAMPProperties.RegionSizeProp,
			 Utils.toString((long)(end.getPosX()-begin.getPosX())));
    }

    public boolean isPinnedUp() {return pinned_up;}
    public void setPinnedUp(boolean pinned_up) {
	this.pinned_up = pinned_up;
    }
}
