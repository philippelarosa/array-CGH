
/*
 *
 * GraphPanelSet.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

public class GraphPanelSet extends JPanel {

    private View view;
    private Mark markBegin;

    GraphPanel graphPanels[];
    PanelProfile panelProfiles[];
    PanelLinks panelLinks[];
    PanelLayout panelLayout;

    GraphPanelSet(View view, PanelProfile panelProfiles[],
		  PanelLayout panelLayout, PanelLinks panelLinks[]) {
	super(new BorderLayout());
	this.view = view;
	this.panelProfiles = panelProfiles;
	this.panelLayout = panelLayout;
	this.panelLinks = panelLinks;
	graphPanels = new GraphPanel[panelProfiles.length];

	for (int n = 0; n < panelProfiles.length; n++) {
	    // TBD
	    //boolean supportX = panelGroupProfile.supportX();
	    boolean supportX = true;
	    graphPanels[n] = new GraphPanel(view, n, supportX,
					    panelProfiles[n]);
	    graphPanels[n].setVirtualSize(2000, 1000);
	}

	if (panelLayout != null)
	    add(panelLayout.makeComponent(graphPanels));
	else
	    add(graphPanels[0]);

	view.getSearchGraphElementPanel().setPanels(graphPanels);
	view.getSearchDataElementPanel().setPanels(graphPanels);

	if (panelLinks != null) {
	    for (int n = 0; n < panelLinks.length; n++)
		panelLinks[n].setLinkedPanes(graphPanels);
	}

	setTopTitle();
    }

    void setTopTitle() {
	String title = view.getName();

	view.getInfoPanel().setTopTitle(title, "");
	view.setTitle(VAMPUtils.getTitle() + " V" + VersionManager.getStringVersion() + ": " + title);
    }

    void changeGraphElements() {
	view.sync(false);
	updateBottomTitle();
    }

    void updateBottomTitle() {
	int total_cnt = 0;
	String cnt_s = "";
	for (int n = 0; n < graphPanels.length; n++) {
	    //int cnt = getPanel(n).getGraphElements().size();
	    GraphPanel panel = getPanel(n);
	    int cnt = panel.getDefaultGraphElementDisplayer().
		getHSizeSets(panel.getGraphElements());
	    if (cnt_s.length() > 0) cnt_s += " / ";
	    cnt_s += (new Integer(cnt)).toString();
	    total_cnt += cnt;
	}
	
	view.getInfoPanel().setBottomTitle(cnt_s + " element" +
					   (total_cnt != 1 ? "s" : ""));
    }

    LinkedList getGraphElements(int n) {
	if (n == View.ALL) {
	    LinkedList l = new LinkedList();
	    for (n = 0; n < graphPanels.length; n++)
		l.addAll(getPanel(n).getGraphElements());
	    
	    return l;
	}

	return getPanel(n).getGraphElements();
    }

    void setFitInPage(boolean value) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).setFitInPage(value);
    }

    void setScaleX(int n, double value) {
	getPanel(n).setScaleX(value);
    }
 
    void setScaleY(int n, double value) {
	getPanel(n).setScaleY(value);
    }

    boolean setGraphElements(LinkedList graphElements) {
	// ???
	return getPanel(0).setGraphElements(graphElements);
    }

    void setGraphElements(LinkedList graphElements, int n) {
	getPanel(n).setGraphElements(graphElements);
    }

    void syncGraphElements(boolean readaptSize) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).syncGraphElements(readaptSize);
    }

    void syncGraphElements(boolean readaptSize, boolean applyOP,
			   boolean warn) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).syncGraphElements(readaptSize, applyOP, warn);
    }

    void selectAll(int n, boolean select, boolean immediate) {
	if (n == View.ALL) {
	    LinkedList l = new LinkedList();
	    for (n = 0; n < graphPanels.length; n++)
		getPanel(n).selectAll(select, immediate);
	    return;
	}

	getPanel(n).selectAll(select);
    }

    void selectAllGraphElements(int n, boolean select, boolean immediate) {
	if (n == View.ALL) {
	    LinkedList l = new LinkedList();
	    for (n = 0; n < graphPanels.length; n++)
		getPanel(n).selectAllGraphElements(select, immediate);
	    return;
	}

	getPanel(n).selectAllGraphElements(select);
    }

    void selectAllRegions(int n, boolean select) {
	if (n == View.ALL) {
	    LinkedList l = new LinkedList();
	    for (n = 0; n < graphPanels.length; n++)
		getPanel(n).selectAllRegions(select);
	    return;
	}

	getPanel(n).selectAllRegions(select);
    }

    void selectAllMarks(int n, boolean select) {
	if (n == View.ALL) {
	    LinkedList l = new LinkedList();
	    for (n = 0; n < graphPanels.length; n++)
		getPanel(n).selectAllMarks(select);
	    return;
	}

	getPanel(n).selectAllMarks(select);
    }

    void selectAndCopyAll(int n) {
	getPanel(n).selectAndCopyAll();
    }

    void selectAndCopyAllGraphElements(int n) {
	getPanel(n).selectAndCopyAllGraphElements();
    }

    void selectAndCopyAllRegions(int n) {
	getPanel(n).selectAndCopyAllRegions();
    }

    void selectAndCopyAllMarks(int n) {
	getPanel(n).selectAndCopyAllMarks();
    }

    void removeAllS(int n) {
	getPanel(n).removeAllS();
    }

    void removeGraphElements(int n) {
	getPanel(n).removeGraphElements();
    }

    void cutSelection(int n) {
	getPanel(n).cutSelection();
    }

    void copySelection(int n) {
	getPanel(n).copySelection();
    }

    void pasteSelection(int n) {
	getPanel(n).pasteSelection();
    }

    void removeSelection(int n) {
	getPanel(n).removeSelection();
    }

    void setReadOnly(int n, boolean isReadOnly) {
	getPanel(n).setReadOnly(isReadOnly);
    }

    boolean isReadOnly(int n) {
	return getPanel(n).isReadOnly();
    }

    void setInternalReadOnly(int n, boolean internalReadOnly) {
	getPanel(n).setInternalReadOnly(internalReadOnly);
    }

    boolean isInternalReadOnly(int n) {
	return getPanel(n).isInternalReadOnly();
    }

    void removeMarks(int which) {
	GraphPanel panel = getPanel(which);
	panel.removeMarks();
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    ((GraphPanel)l.get(n)).removeMarks();
    }

    void removeRegions(int which, boolean removeRegionMarks) {
	GraphPanel panel = getPanel(which);
	panel.removeRegions(removeRegionMarks);
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    ((GraphPanel)l.get(n)).removeRegions(removeRegionMarks);
    }

    boolean hasSelection(int n) {
	if (n == View.ALL) {
	    for (n = 0; n < graphPanels.length; n++)
		if (getPanel(n).hasSelection())
		    return true;
	    return false;
	}

	return getPanel(n).hasSelection();
    }

    boolean hasGraphElementSelection(int n) {
	if (n == View.ALL) {
	    for (n = 0; n < graphPanels.length; n++)
		if (getPanel(n).hasGraphElementSelection())
		    return true;
	    return false;
	}

	return getPanel(n).hasGraphElementSelection();
    }

    boolean hasRegionSelection(int n) {
	if (n == View.ALL) {
	    for (n = 0; n < graphPanels.length; n++)
		if (getPanel(n).hasRegionSelection())
		    return true;
	    return false;
	}

	return getPanel(n).hasRegionSelection();
    }

    boolean hasMarkSelection(int n) {
	if (n == View.ALL) {
	    for (n = 0; n < graphPanels.length; n++)
		if (getPanel(n).hasMarkSelection())
		    return true;
	    return false;
	}

	return getPanel(n).hasMarkSelection();
    }

    Vector getSelectedGraphElements(int n) {
	if (n == View.ALL) {
	    Vector v = new Vector();
	    for (n = 0; n < graphPanels.length; n++)
		v.addAll(getPanel(n).getSelectedGraphElements());

	    return v;
	}

	return getPanel(n).getSelectedGraphElements();
    }

    void replaceGraphElements(int n, Vector from, Vector to) {
	getPanel(n).replaceGraphElements(from, to);
    }

    Mark addMark(int which, Mark mark) {
	GraphPanel panel = getPanel(which);
	panel.addMark(mark);
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    ((GraphPanel)l.get(n)).addMark(mark);

	return mark;
    }

    Mark addMark(int which, double posx) {
	return addMark(which, new Mark(posx));
    }

    Mark addMarkBegin(int which, double posx) {
	markBegin = addMark(which, posx);
	return markBegin;
    }

    boolean removeMark(int which, Mark mark) {
	GraphPanel panel = getPanel(which);
	panel.removeMark(mark);
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    ((GraphPanel)l.get(n)).removeMark(mark);

	return true;
    }

    boolean cutMark(int which, Mark mark) {
	GraphPanel panel = getPanel(which);
	if (!panel.cutMark(mark)) return false;
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    if (!((GraphPanel)l.get(n)).cutMark(mark))
		return false;

	return true;
    }

    Region addRegion(int which, Region region) {
	return addRegion(which, region, null, null);
    }

    Region addRegion(int which, Region region, Mark begin, Mark end) {
	GraphPanel panel = getPanel(which);
	panel.addRegion(region, begin, end);
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    ((GraphPanel)l.get(n)).addRegion(region, begin, end);

	return region;
    }

    Region addRegion(int which, Mark end) {
	return addRegion(which, new Region(markBegin, end), markBegin, end);
    }

    void removeRegion(int which, Region region, boolean removeMarks) {
	GraphPanel panel = getPanel(which);
	panel.removeRegion(region, removeMarks);
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    ((GraphPanel)l.get(n)).removeRegion(region, removeMarks);
    }

    boolean cutRegion(int which, Region region) {

	GraphPanel panel = getPanel(which);
	if (!panel.cutRegion(region)) return false;
	LinkedList l = panel.getLinkedPaneX();
	int cnt = l.size();
	for (int n = 0; n < cnt; n++)
	    if (!((GraphPanel)l.get(n)).cutRegion(region))
		return false;

	return true;
    }

    void sync(boolean invalidate) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).sync(invalidate);
    }

    void selectionSync() {
	if (view.getColorLegendPanel() != null)
	    view.getColorLegendPanel().sync();
	if (view.getThresholdsPanel() != null)
	    view.getThresholdsPanel().sync();
    }

    void setRegionColor(Region region, Color color) {
	region.setColor(color);
	sync(false);
    }

    void setMarkColor(Mark mark, Color color) {
	mark.setColor(color);
	sync(false);
    }

    void moveMark(Mark mark, double posx) {
	mark.setLocation(posx);
	sync(false);
    }

    Mark getMarkBegin() {return markBegin;}
    void setMarkBegin(Mark mark) {markBegin = mark;}

    void clearPinnedUp(int which) {
	getPanel(which).clearPinnedUp();
    }

    boolean hasPinnedUp(int which) {
	return getPanel(which).hasPinnedUp();
    }

    void clearCenter(int which) {
	getPanel(which).clearCenter();
    }

    void recenter(int which) {
	getPanel(which).recenter();
    }

    String getCenterType(int which) {
	return getPanel(which).getCenterType();
    }

    public GraphPanel getPanel(int n) {
	return graphPanels[n];
    }

    int setPaintingRegionMode(int which, int sync_mode) {
	return getPanel(which).setPaintingRegionMode(sync_mode);
    }

    void setRegions(int which, LinkedList regions, boolean add) {
	if (!add)
	    removeRegions(which, true);

	int size = regions.size();
	for (int i = 0; i < size; i++) {
	    Region region = (Region)regions.get(i);
	    addRegion(which, region);
	}
    }

    void setMarks(int which, LinkedList marks, boolean add) {
	if (!add)
	    removeMarks(which);
	int size = marks.size();
	for (int i = 0; i < size; i++) {
	    Mark mark = (Mark)marks.get(i);
	    addMark(which, mark);
	}
    }

    View getView() {return view;}

    void applyOnGraphElements(DataSetPerformer ds_perform, GraphPanel sub) {
	if (sub == null) return;
	ds_perform.apply(sub.getGraphElements());
    }

    void applyOnGraphElements(DataSetPerformer ds_perform) {
	for (int n = 0; n < graphPanels.length; n++)
	    applyOnGraphElements(ds_perform, getPanel(n));
    }

    void setWestYSize(int sz, boolean force) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).setWestYSize(sz, force);
    }

    void reinitWestYSize() {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).reinitWestYSize();
    }

    void incrWestYSize(int incr) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).incrWestYSize(incr);
    }

    void incrWestMargin(int incr) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).incrWestMargin(incr);
    }

    LinkedList getMarks() {
	// ??
	return getPanel(0).getMarks();
    }

    LinkedList getRegions() {
	// ??
	return getPanel(0).getRegions();
    }

    void setDefaultDisplayers(int n,
			      GraphElementDisplayer defaultGraphElementDisplayer,
			      AxisDisplayer defaultAxisDisplayer) {
	getPanel(n).setDefaultDisplayers(defaultGraphElementDisplayer,
					    defaultAxisDisplayer);
    }

    void setDefaultGraphElementDisplayer(GraphElementDisplayer defaultGraphElementDisplayer) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).setDefaultGraphElementDisplayer(defaultGraphElementDisplayer);
    }

    void setDefaultAxisDisplayer(AxisDisplayer defaultAxisDisplayer) {
	for (int n = 0; n < graphPanels.length; n++)
	    getPanel(n).setDefaultAxisDisplayer(defaultAxisDisplayer);
    }

    GraphElementDisplayer getDefaultGraphElementDisplayer(int n) {
	return getPanel(n).getDefaultGraphElementDisplayer();
    }

    AxisDisplayer getDefaultAxisDisplayer(int n) {
	return getPanel(n).getDefaultAxisDisplayer();
    }

    GraphPanel[] getPanels() {return graphPanels;}
    int getPanelCount() {return graphPanels.length;}

    PanelProfile[] getPanelProfiles() {return panelProfiles;}
    PanelLinks[] getPanelLinks() {return panelLinks;}
    PanelLayout getPanelLayout() {return panelLayout;}
}
