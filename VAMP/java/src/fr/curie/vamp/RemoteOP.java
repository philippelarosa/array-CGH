
/*
 *
 * RemoteOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.awt.*;

public class RemoteOP extends Thread {

    ViewFrame viewFrame;
    View view, data_view;
    GraphPanel panel;
    InputStream is;
    XMLLoadDataFactory load_factory;
    XMLArrayDataFactory array_factory;
    LinkedList regions = null;
    String running_msg, done_msg, old_msg;
    boolean isReadOnly;
    boolean setReadOnly;
    boolean result = false;
    public static final int LOAD_FACTORY = 1;
    public static final int ARRAY_FACTORY = 2;
    GraphElementListOperation postOP = null;
    GraphElementListOperation post2OP = null;

    Cursor view_cursor;

    public RemoteOP(ViewFrame viewFrame, View view, GraphPanel panel,
	     boolean setReadOnly, boolean addRegions, long offset,
	     boolean normalize, InputStream is,
	     String running_msg, String done_msg, int type) {
	this.viewFrame = viewFrame;
	this.view = view;
	this.data_view = (viewFrame == null ? view : viewFrame.getView());
	this.panel = panel;
	this.setReadOnly = setReadOnly;
	this.is = is;
	this.running_msg = running_msg;
	this.done_msg = done_msg;
	if (type == LOAD_FACTORY)
	    load_factory = new XMLLoadDataFactory(view.getGlobalContext(), false, normalize);
	else if (type == ARRAY_FACTORY)
	    array_factory = new XMLArrayDataFactory(view.getGlobalContext(), null);

	if (!addRegions)
	    return;

	regions = new LinkedList();
	for (int n = 0; n < view.getRegions().size(); n++) {
	    Region r = (Region)((Region)view.getRegions().get(n));
	    double begin = r.getBegin().getPosX();
	    double end = r.getEnd().getPosX();
	    regions.add(new Region(new Mark(begin + offset),
				   new Mark(end + offset),
				   r.getColor()));
	}
    }

    public void run() {
	view_cursor = Utils.setWaitCursor(view);
	view.setRunningMode(true);
	if (running_msg != null) {
	    old_msg = view.setMessage(running_msg);
	    view.repaint();
	}
	else
	    old_msg = null;

	if (panel != null) {
	    isReadOnly = panel.isReadOnly();
	    panel.setReadOnly(true);
	}

	if (load_factory != null)
	    result = load_factory.setData(is, null, data_view, true, this);
	else {
	    LinkedList list = array_factory.getData(is);
	    if (list == null)
		list = new LinkedList();
	    result = list.size() > 0;
	    data_view.getGraphElements(0).addAll(list);
	    postAction();
	}

	if (!result) {
	    if (panel != null)
		panel.setReadOnly(isReadOnly);

	    view.setRunningMode(false);
	    view.setMessage(old_msg);
	    Utils.setCursor(view, view_cursor);
	}
	else if (regions != null) {
	    GraphPanelSet panelSet = data_view.getGraphPanelSet();
	    int sz = regions.size();
	    int which_panel = 0; // really panel 0 ?
	    for (int n = 0; n < sz; n++) {
		Region r = (Region)regions.get(n);
		panelSet.addMark(which_panel, r.getBegin());
		panelSet.addMark(which_panel, r.getEnd());
		panelSet.addRegion(which_panel, r);
	    }
	}

	if (viewFrame != null && result) {
	    viewFrame.setVisible(true);
	    data_view.syncGraphElements();
	}

	data_view.repaint();
	if (data_view != view)
	    view.repaint();
    }

    static final int BEEP_COUNT = 1;

    private void advert() {
	for (int n = 0; n < BEEP_COUNT; n++) {
	    view.getToolkit().beep();
	    try {
		Thread.sleep(100);
	    }
	    catch(java.lang.InterruptedException e) {
	    }
	}

	if (done_msg != null && done_msg.length() > 0)
	    InfoDialog.pop(view.getGlobalContext(), done_msg);
    }

    void postAction() {
	advert();

	if (panel != null)
	    panel.setReadOnly(isReadOnly);

	GraphPanel xpanel = data_view.getPanel(0);
	if (postOP != null) {
	    Vector elems = Utils.listToVector(xpanel.getGraphElements());
	    Vector v = postOP.apply(data_view, xpanel,
				    elems,
				    postOP.getDefaultParams(data_view, elems),
				    true);
	    xpanel.setGraphElements(Utils.vectorToList(v));
	}

	if (post2OP != null)
	    post2OP.postPerform(xpanel.getGraphElements());

	view.setRunningMode(false);

	view.setMessage(old_msg);
	Utils.setCursor(view, view_cursor);
	if (setReadOnly)
	    data_view.setReadOnly(true);
    }

    public void setPostOP(GraphElementListOperation postOP) {
	this.postOP = postOP;
    }

    public void setPost2OP(GraphElementListOperation post2OP) {
	this.post2OP = post2OP;
    }

    public boolean mayApplyOnLightImportedProfiles() {return true;}
}
