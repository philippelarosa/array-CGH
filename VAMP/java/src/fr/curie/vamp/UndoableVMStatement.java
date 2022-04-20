
/*
 *
 * UndoableVMStatement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

abstract class UndoableVMStatement extends VMStatement {

    UndoableVMStatement(VMOP op, GraphPanel graphPanel) {
	super(op, graphPanel);
    }

    boolean isUndoable() {
	return true;
    }

    abstract void undo();
    abstract void redo();
}
