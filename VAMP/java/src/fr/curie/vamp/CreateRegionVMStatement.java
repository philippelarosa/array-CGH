
/*
 *
 * CreateRegionVMStatement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;

class CreateRegionVMStatement extends StandardVMStatement {

    Mark begin, end;
    CreateMarkVMStatement crmark_vmstat;
    
    CreateRegionVMStatement(GraphPanel graphPanel,
			    Mark begin, Mark end) {
	super(VMOP.getVMOP(VMOP.CREATE_REGION), graphPanel);
	this.begin = begin;
	this.end = end;
    }

    void beforeExecute() {
	super.beforeExecute();

	if (begin == null)
	    return;

	UndoVMStack undo_stack = UndoVMStack.getInstance(graphPanel);
	CreateMarkVMStatement crmark_stat = (CreateMarkVMStatement)
	    undo_stack.getVMStatement
	    (new CreateMarkVMStatement(graphPanel, end));
	if (crmark_stat == null)
	    return;

	undo_stack.remove(crmark_stat);
	crmark_stat = (CreateMarkVMStatement)undo_stack.getVMStatement
	    (new CreateMarkVMStatement(graphPanel, begin));
	if (crmark_stat == null)
	    return;

	undo_stack.remove(crmark_stat);

	undo_state.marks.remove(begin);
	undo_state.marks.remove(end);
    }
}
