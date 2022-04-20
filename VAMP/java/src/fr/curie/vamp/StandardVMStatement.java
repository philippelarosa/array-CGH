
/*
 *
 * StandardVMStatement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;

class StandardVMStatement extends UndoableVMStatement {

    //protected GraphPanel graphPanel;
    protected GraphCanvas.State undo_state;
    protected GraphCanvas.State redo_state;
    protected int graphElement_cnt, region_cnt, mark_cnt;

    protected StandardVMStatement(VMOP op, GraphPanel graphPanel) {
	super(op, graphPanel);
	//this.graphPanel = graphPanel;
    }

    void beforeExecute() {
	undo_state = new GraphCanvas.State(graphPanel.getCanvas(), true);
    }

    void afterExecute() {
	GraphCanvas.State newState = new GraphCanvas.State(graphPanel.getCanvas(), false);
	graphElement_cnt = undo_state.graphElements.size() - newState.graphElements.size();
	if (graphElement_cnt < 0)
	    graphElement_cnt = -graphElement_cnt;

	region_cnt = undo_state.regions.size() - newState.regions.size();
	if (region_cnt < 0)
	    region_cnt = -region_cnt;

	LinkedList marks;
	if (newState.marks.size() > undo_state.marks.size()) {
	    marks = (LinkedList)newState.marks.clone();
	    marks.removeAll(undo_state.marks);
	}
	else {
	    marks = (LinkedList)undo_state.marks.clone();
	    marks.removeAll(newState.marks);
	}

	int sz = marks.size();
	mark_cnt = 0;
	for (int n = 0; n < sz; n++)
	    if (((Mark)marks.get(n)).getRegion() == null)
		mark_cnt++;
    }

    void undo() {
	redo_state = new GraphCanvas.State(graphPanel.getCanvas(), true);
	undo_state.restore(graphPanel.getCanvas());
	graphPanel.syncGraphElements(true);
    }

    void redo() {
	if (redo_state == null)
	    return;
	redo_state.restore(graphPanel.getCanvas());
	graphPanel.syncGraphElements(true);
	redo_state = null;
    }

    public String toString() {
	return op.getName() + " " + graphPanel.getPanelName();
    }

    public String shortDesc() {
	String desc = op.getName();
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
