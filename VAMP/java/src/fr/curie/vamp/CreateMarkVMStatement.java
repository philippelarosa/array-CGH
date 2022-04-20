
/*
 *
 * CreateMarkVMStatement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;

class CreateMarkVMStatement extends StandardVMStatement {

    private Mark mark;

    CreateMarkVMStatement(GraphPanel graphPanel, Mark mark) {
	super(VMOP.getVMOP(VMOP.CREATE_MARK), graphPanel);
	this.mark = mark;
    }

    public boolean equals(Object o) {
	if (!(o instanceof CreateMarkVMStatement))
	    return false;
	CreateMarkVMStatement vmstat = (CreateMarkVMStatement)o;
	return vmstat.getGraphPanel() == getGraphPanel() &&
	    vmstat.mark == mark;
    }
}
