
/*
 *
 * DragAndDropVMStatement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;

class DragAndDropVMStatement extends UndoableVMStatement {

    private GraphPanel graphPanel_src, graphPanel_dst;
    private GraphCanvas.State redo_state_src, redo_state_dst;
    private GraphCanvas.State state_src, state_dst, delta_state_dst;
    protected int graphElement_cnt, region_cnt, mark_cnt;
    private boolean isMoving;

    DragAndDropVMStatement(boolean isMoving, GraphPanel graphPanel_src,
			   GraphPanel graphPanel_dst,
			   LinkedList elem_list) {
	super(VMOP.getVMOP(VMOP.DRAG_AND_DROP), graphPanel_src);
	this.graphPanel_src = graphPanel_src;
	this.graphPanel_dst = graphPanel_dst;
	this.isMoving = isMoving;
	makeCounts(elem_list);
    }

    void beforeExecute() {

	state_src = new GraphCanvas.State(graphPanel_src.getCanvas(),
						true);
	if (graphPanel_dst == graphPanel_src)
	    return;

	state_dst = new GraphCanvas.State(graphPanel_dst.getCanvas(), true);
    }

    void afterExecute() {
	if (graphPanel_dst == graphPanel_src)
	    return;

	delta_state_dst = new GraphCanvas.State
	    (graphPanel_dst.getCanvas(), true);
	
	delta_state_dst.graphElements.removeAll(state_dst.graphElements);
	delta_state_dst.marks.removeAll(state_dst.marks);
	delta_state_dst.regions.removeAll(state_dst.regions);
    }

    void makeCounts(LinkedList elem_list) {
	graphElement_cnt = region_cnt = mark_cnt = 0;
	int sz = elem_list.size();
	for (int n = 0; n < sz; n++) {
	    Object o = elem_list.get(n);
	    if (o instanceof GraphElement)
		graphElement_cnt++;
	    else if (o instanceof Region)
		region_cnt++;
	    else if (o instanceof Mark && ((Mark)o).getRegion() == null)
		mark_cnt++;
	    }
    }

    void undo() {
	redo_state_src = new GraphCanvas.State(graphPanel_src.getCanvas(),
					       true);

	if (graphPanel_dst != graphPanel_src)
	    redo_state_dst = new GraphCanvas.State(graphPanel_dst.getCanvas(),
						   true);

	state_dst = new GraphCanvas.State(graphPanel_dst.getCanvas(), true);
	state_src.restore(graphPanel_src.getCanvas());
	if (graphPanel_dst != graphPanel_src) {
	    GraphCanvas.State new_state_dst = new GraphCanvas.State(graphPanel_dst.getCanvas(),
								    true);
	    new_state_dst.graphElements.removeAll(delta_state_dst.graphElements);
	    new_state_dst.marks.removeAll(delta_state_dst.marks);
	    new_state_dst.regions.removeAll(delta_state_dst.regions);
	    new_state_dst.restore(graphPanel_dst.getCanvas());
	}
    }

    void redo() {
	if (redo_state_src != null)
	    redo_state_src.restore(graphPanel_src.getCanvas());
	if (redo_state_dst != null)
	    redo_state_dst.restore(graphPanel_dst.getCanvas());
	redo_state_src = redo_state_dst = null;
    }

    public String toString() {
	if (graphPanel_dst != graphPanel_src)
	    return op.getName() + " " + graphPanel_src.getPanelName() + " -> "
		+ graphPanel_dst.getPanelName();
	return op.getName() + " " + graphPanel_src.getPanelName();
    }

    public String shortDesc() {
	String desc = (graphPanel_dst == graphPanel_src ? "Self d" : "D");
	desc += "rag and drop";
	if (!isMoving)
	    desc += " (Copy)";

	if (graphElement_cnt + region_cnt + mark_cnt != 0) {
	    boolean has = false;
	    desc += " [";
	    if (graphElement_cnt != 0) {
		desc += Utils.getCount(graphElement_cnt, "Profile");
		has = true;
	    }

	    if (region_cnt != 0) {
		if (has) desc += ", ";
		else has = true;
		desc += Utils.getCount(region_cnt, "Region");
	    }

	    if (mark_cnt != 0) {
		if (has) desc += ", ";
		else has = true;
		desc += Utils.getCount(mark_cnt, "Landmark");
	    }

	    desc += "]";
	}

	return desc;
    }
}
