
/*
 *
 * Task.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.awt.Cursor;
import javax.swing.SwingUtilities;

public class Task extends Thread {

    public static interface Operation {
	View getView();

	GraphPanel getPanel();
	void perform1();
	void perform2();
	String getMessage();
    }

    abstract static class OperationWrapper implements Task.Operation {
	Object value;
	
	public OperationWrapper(Object value) {
	    this.value = value;
	}
    
	abstract public View getView();
	abstract public GraphPanel getPanel();

	abstract public void perform1();
	abstract public void perform2();

	abstract public String getMessage();
    }

    private Operation op;

    Task() {
	this(null);
    }

    Task(Operation op) {
	this.op = op;
    }

    void setOperation(Operation op) {
	this.op = op;
    }

    private View view;
    private String o_msg;
    private Cursor cursor;
    private boolean o_running_mode;

    private boolean post_perform1_done = false;
    private boolean post_perform2_done = false;
    private boolean isReadOnly = false;

    private GraphPanel panel;

    public void run() {
	view = op.getView();
	panel = op.getPanel();

	isReadOnly = panel.isReadOnly();
	panel.setReadOnly(true);

	o_running_mode = view.setRunningMode(true);
	o_msg = view.setMessage(op.getMessage());
	cursor = Utils.setWaitCursor(view);
	view.repaint();

	op.perform1();
	postPerform1();

	op.perform2();
	postPerform2();
    }

    public void performBeforeOPFrameVisible() {
	postPerform1();
	postPerform2();
    }

    public void postPerform1() {
	if (post_perform1_done)
	    return;
	panel.setReadOnly(isReadOnly);
	post_perform1_done = true;
    }

    public void postPerform2() {
	if (post_perform2_done)
	    return;

	view.setMessage(o_msg);

	view.setRunningMode(o_running_mode);
	Utils.setCursor(view, cursor);
	view.repaint();
	post_perform2_done = true;
    }
}
