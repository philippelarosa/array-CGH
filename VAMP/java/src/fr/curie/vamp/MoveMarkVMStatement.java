
/*
 *
 * MoveMarkVMStatement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;

class MoveMarkVMStatement extends UndoableVMStatement {

    protected GraphCanvas.State redo_state;
    Mark mark;
    double oposx, posx;

    MoveMarkVMStatement(GraphPanel graphPanel, Mark mark,
			double posx) {
	super(VMOP.getVMOP(VMOP.MOVE_MARK), graphPanel);
	this.mark = mark;
	this.posx = posx;
	this.oposx = mark.getPosX();
    }

    void undo() {
	redo_state = new GraphCanvas.State(graphPanel.getCanvas(), true);
	graphPanel.getView().getGraphPanelSet().moveMark(mark, oposx);
	graphPanel.repaint();
    }

    void redo() {
	if (redo_state == null)
	    return;
	redo_state.restore(graphPanel.getCanvas());
	graphPanel.repaint();
	redo_state = null;
    }

    public String toString() {
	return shortDesc() + " " + graphPanel.getPanelName();
    }

    public String shortDesc() {
	if (mark.getRegion() == null)
	    return "Move landmark";
	return "Resize region";
    }

    void setPosX(double posx) {
	this.posx = posx;
    }

    void beforeExecute() {
    }

    void afterExecute() {
    }
}
