
/*
 *
 * VMStatement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

abstract class VMStatement {

    protected VMOP op;
    protected GraphPanel graphPanel;

    VMStatement(VMOP op, GraphPanel graphPanel) {
	this.op = op;
	this.graphPanel = graphPanel;
    }

    VMOP getVMOP() {
	return op;
    }

    abstract void beforeExecute();
    abstract void afterExecute();

    boolean isUndoable() {
	return false;
    }

    abstract public String toString();
    abstract public String shortDesc();

    GraphPanel getGraphPanel() {
	return graphPanel;
    }
}
