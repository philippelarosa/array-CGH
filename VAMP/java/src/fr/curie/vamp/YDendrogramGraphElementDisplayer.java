
/*
 *
 * YDendrogramGraphElementDisplayer.java
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

class YDendrogramGraphElementDisplayer extends GraphElementDisplayer {

    YDendrogramGraphElementDisplayer() {
	super("Dendrogram", null);
    }

    public void computeVBounds(GraphCanvas canvas, Graphics2D g,
			       GraphElement graphElement, int m) {
	graphElement.resetPaintVBounds();

	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();

	if (dendroGE == null)
	    return;

	GraphCanvas master_canvas = getMasterCanvas(canvas);

	if (dendroGE.isLeaf() && master_canvas != null) {
	    String dendroID = (String)dendroGE.getPropertyValue(DendrogramGraphElement.DendroIDProp);
	    LinkedList l = master_canvas.getGraphElements();
	
	    GraphElement ge = find(master_canvas.getGraphElements(),
				   dendroID);
	    if (ge != null) {
		double pos = ge.getVBounds().y - ge.getVBounds().height/2;
		dendroGE.setPos(pos);
	    }
	}

	dendroGE.compile_pos();

	double espx1 = canvas.getVW(1.);
	double espy1 = canvas.getVH(1.);
	double espx2 = canvas.getVW(2.);
	double espy2 = canvas.getVH(2.);

	double vx1 = dendroGE.getVX1();
	double vy1 = dendroGE.getVY1();
	double vx2 = dendroGE.getVX2();
	double vy2 = dendroGE.getVY2();
	double width = vx2-vx1;
	double height = vy2-vy1;

	if (width == 0)
	    width += espx2;

	width += espx1;

	if (height == 0)
	    height += espy2;

	height += espy1;
	vy2 += espy1;

	graphElement.setVBounds(vx1, vy2, width, height);
    }

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement, int m,
			PrintContext pctx) {
	if (g == null)
	    return;

	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();

	if (dendroGE == null)
	    return;

	DendrogramNode dendro_node = dendroGE.getDendrogramNode();

	Color color = dendroGE.getColor();
	if (color == null)
	    color = Color.GRAY;

	g.setColor(color);

	double rx1 = canvas.getRX(dendroGE.getVX1());
	double rx2 = canvas.getRX(dendroGE.getVX2());
	double ry1 = canvas.getRY(dendroGE.getVY1());
	double ry2 = canvas.getRY(dendroGE.getVY2());

	if (pctx != null) {
	    rx1 = pctx.getRX(rx1);
	    rx2 = pctx.getRX(rx2);
	    ry1 = pctx.getRY(ry1);
	    ry2 = pctx.getRY(ry2);
	}

	g.drawLine((int)rx1, (int)ry1, (int)rx2, (int)ry2);

	if (showTagString()) {
	    String tag = VAMPUtils.getTag(dendroGE);
	    if (tag != null && tag.length() > 0) {
		Font tagFont =
		    VAMPResources.getFont(VAMPResources.AXIS_Y_NAME_DISPLAYER_FONT);
		g.setFont(tagFont);
		Dimension sz = Utils.getSize(g, tag);
		g.setColor(Color.BLACK);
		if (rx1 == rx2)
		    g.drawString(tag,
				 (int)(rx1 - sz.width - 2),
				 (int)(ry1 + (ry2-ry1)/2 + 2));
		else if (ry1 == ry2)
		    g.drawString(tag,
				 (int)(rx1 + (rx2 - rx1 - sz.width) / 2),
				 (int)(ry1 - 3));
	    }
	}
    }

    double getRPadY() {return 0.;}

    int getHSizeSets(LinkedList graphElements) {
	if (graphElements.size() == 0)
	    return 0;

	DendrogramGraphElement dendroGE =
	    ((GraphElement)graphElements.get(0)).asDendrogramGraphElement();
	if (dendroGE == null)
	    return 0;

	/*
	System.out.println("node: " + dendroGE.getDendrogramNode());
	System.out.println("dendro: " + dendroGE.getDendrogramNode().getDendrogram());
	System.out.println("leaves: " + dendroGE.getDendrogramNode().getDendrogram().getLeaves());
	*/
	return dendroGE.getDendrogramNode().getDendrogram().getLeaves().size();
    }

    static GraphElement find(LinkedList geList, String dendroID) {
	int sz = geList.size();
	for (int n = 0; n < sz; n++) {
	    if (((GraphElement)geList.get(n)).getID().equals(dendroID))
		return (GraphElement)geList.get(n);
	}
	return null;
    }

    void computeGraphElements(GraphCanvas canvas) {
	GraphCanvas master_canvas = getMasterCanvas(canvas);
	//System.out.println("computeGraphElements : " + master_canvas);
	if (master_canvas == null) {
	    super.computeGraphElements(canvas);
	    return;
	}

	computeGraphElementVBounds(canvas,
				   canvas.getCanonScale().getScaleY());
    }

    void computeGraphElementVBounds(GraphCanvas canvas, double scaleY) {
	GraphCanvas master_canvas = getMasterCanvas(canvas);
	//System.out.println("computeGraphElementsVBounds : " + master_canvas);
	if (master_canvas == null) {
	    super.computeGraphElementVBounds(canvas, scaleY);
	    return;
	}

	Dimension2DDouble master_vSize = master_canvas.getVirtualSize();
	double maxX = getMaxX(canvas);
	canvas.getGraphPanel().setVirtualSize(maxX,
					      master_vSize.height);
	canvas.setMinX(0);
	canvas.setMaxX(maxX);
	canvas.setMinY(master_canvas.getMinY());
	canvas.setMaxY(master_canvas.getMaxY());
    }

    double getMaxX(GraphCanvas canvas) {
	LinkedList graphElements = canvas.getGraphElements();
	int size = graphElements.size();
	double maxY = 0.;
	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    DendrogramGraphElement dendroGE =
		graphElement.asDendrogramGraphElement();
	    if (dendroGE == null) continue;
	    double height = dendroGE.getDendrogramNode().getDendrogram().getMaxHeight();
	    if (height > maxY)
		maxY = height;
	}
	return maxY;
    }

    void computeRCoords(GraphCanvas canvas) {
	// not needed
    }

    static GraphCanvas getMasterCanvas(GraphCanvas canvas) {
	// method based on panel links to find master canvas
	// fragile !

	GraphPanelSet panelSet = canvas.getView().getGraphPanelSet();

	int which = canvas.getGraphPanel().getWhich();

	PanelLinks links[] = panelSet.getPanelLinks();
	if (links == null)
	    return null;
	for (int n = 0; n < links.length; n++) {
	    if (links[n].getSyncMode() == GraphPanel.SYNCHRO_Y) {
		int ind[] = links[n].getInd();
		if (ind.length == 2) {
		    for (int j = 0; j < ind.length; j++) {
			if (ind[j] == which) {
			    if (j == 0)
				return panelSet.getPanel(ind[1]).getCanvas();
			    return panelSet.getPanel(ind[0]).getCanvas();
			}
		    }
		}
	    }
	}

	return null;
    }

    boolean show_tag_string = true;

    boolean showTagString() {return show_tag_string;}

    void showTagString(boolean show_tag_string) {
	this.show_tag_string = show_tag_string;
    }

    boolean useOptSelection() {return false;}

    boolean useHardThresholds() {
	return false;
    }
}
