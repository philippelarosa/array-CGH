
/*
 *
 * UndoVMStack.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;

class UndoVMStack {

    private static final int MAX_DEPTH = 16;
    private boolean active;
    private GraphPanel graphPanel;
    static final boolean REDO_MANAGE = true;
    private int cur = -1;

    private static HashMap instance_map = new HashMap();
    Vector stack = new Vector();

    private UndoVMStack(GraphPanel graphPanel) {
	active = true;
	this.graphPanel = graphPanel;
    }

    public static UndoVMStack getInstance(GraphPanel graphPanel) {
 	UndoVMStack instance = (UndoVMStack)instance_map.get(graphPanel);
	if (instance == null) {
	    instance = new UndoVMStack(graphPanel);
	    instance_map.put(graphPanel, instance);
	}

	return instance;
    }

    void push(UndoableVMStatement vmstat) {
	if (!active)
	    return;

	if (vmstat.getGraphPanel() != graphPanel) {
	    System.err.println("ERROR 1: graphPanels does not match");
	    (new Exception()).printStackTrace();
	}

	if (REDO_MANAGE) {
	    while (stack.size() - 1 > cur)
		stack.remove(stack.size()-1);
	}

	if (stack.size() >= MAX_DEPTH)
	    stack.remove(0);

	//System.out.println("PUSH: " + vmstat.getVMOP().getName());
	//(new Exception()).printStackTrace();

	stack.add(vmstat);

	if (REDO_MANAGE)
	    cur = stack.size()-1;
    }

    public UndoableVMStatement getLastRedoableVMStatement() {
	if (cur == stack.size() - 1)
	    return null;
	return (UndoableVMStatement)stack.get(cur+1);
    }

    public UndoableVMStatement getLastUndoableVMStatement() {
	if (REDO_MANAGE)
	    return cur >= 0 ? (UndoableVMStatement)stack.get(cur) : null;
	int sz = stack.size();
	return sz > 0 ? (UndoableVMStatement)stack.get(sz-1) : null;
    }

    public void undo(UndoableVMStatement target_vmstat) {
	if (target_vmstat.getGraphPanel() != graphPanel) {
	    System.err.println("ERROR2: graphPanels does not match");
	    (new Exception()).printStackTrace();
	}

	for (;;) {
	    UndoableVMStatement vmstat = getLastUndoableVMStatement();
	    undoPerform(vmstat);
	    if (vmstat == target_vmstat)
		break;
	}
    }

    private void undoPerform(UndoableVMStatement vmstat) {
	vmstat.undo();
	if (REDO_MANAGE) {
	    cur--;
	}
	else {
	    stack.remove(stack.size()-1);
	}
    }

    public void undoLast() {
	undoPerform(getLastUndoableVMStatement());
    }

    public void redoLast() {
	UndoableVMStatement vmstat = getLastRedoableVMStatement();
	if (vmstat != null) {
	    vmstat.redo();
	    cur++;
	}
    }

    public Vector getStack() {
	return stack;
    }

    boolean isActive() {
	return active;
    }

    boolean setActive(boolean active) {
	boolean active_o = this.active;
	this.active = active;
	return active_o;
    }

    UndoableVMStatement getVMStatement(UndoableVMStatement vmstat) {
	int sz = stack.size();
	for (int n = sz-1; n >= 0; n--) {
	    if (stack.get(n).equals(vmstat)) {
		return (UndoableVMStatement)stack.get(n);
	    }
	}

	return null;
    }

    int remove(UndoableVMStatement vmstat) {
	int sz = stack.size();
	for (int n = sz-1; n >= 0; n--) {
	    if (stack.get(n).equals(vmstat)) {
		stack.remove(n);
		return n;
	    }
	}

	return -1;
    }

    int getCurrent() {
	return cur;
    }
}
