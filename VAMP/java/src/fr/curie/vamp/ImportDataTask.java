
/*
 *
 * ImportDataTask.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.util.LinkedList;
import java.util.Vector;

class ImportDataTask extends Thread {

    GlobalContext globalContext;
    View view;
    GraphPanel panel;
    String type;
    int panel_num;
    LinkedList typeList;
    LinkedList urlList;
    LinkedList serialList;
    LinkedList parentList;
    LinkedList chrList;
    LinkedList pangenList;
    boolean update;
    boolean full;

    ImportDataTask(GlobalContext globalContext,
		   View view, int panel_num,
		   LinkedList typeList, LinkedList urlList,
		   LinkedList serialList,
		   LinkedList parentList,
		   LinkedList chrList,
		   LinkedList pangenList,
		   boolean update,
		   boolean full) {

	this.globalContext = globalContext;
	this.view = view;
	this.panel_num = panel_num;
	this.panel = view.getPanel(panel_num);
	this.typeList = typeList;
	this.urlList = urlList;
	this.serialList = serialList;
	this.parentList = parentList;
	this.chrList = chrList;
	this.pangenList = pangenList;
	this.update = update;
	this.full = full;
    }

    public void run() {

	LinkedList list = new LinkedList();

	boolean isReadOnly = panel.isReadOnly();
	panel.setReadOnly(true);

	String action = (update ? "Updating" : "Importing");
	boolean o_running_mode = view.setRunningMode(true);
	String o_msg = view.setMessage(action + " profiles ...");
	java.awt.Cursor cursor = Utils.setWaitCursor(view);
	view.repaint();

	long ms0 = System.currentTimeMillis();

	//System.out.println("trace #1");
	int size = urlList.size();
	for (int n = 0; n < size; n++) {
	    LinkedList ilist = ImportData.importData(globalContext, view, panel,
						     (String)typeList.get(n),
						     (String)urlList.get(n),
						     ((Integer)serialList.get(n)).intValue(),
						     (Vector<String>)parentList.get(n),
						     (String[])chrList.get(n),
						     ((Boolean)pangenList.get(n)).booleanValue(),
						     update, full);
	    if (ilist != null && ilist.size() > 0) {
		list.addAll(ilist);
		view.setMessage(action + " profiles: " +
				((GraphElement)ilist.get(0)).getID() + "  [" +
				(n+1) + "/" + size + "]");
		view.repaint();
	    }
	}

	//long ms1 = System.currentTimeMillis();
	//System.out.println(((ms1-ms0)/1000.) + " seconds");

	view.setMessage("All profiles " + (update ? "updated" : "imported"));
	panel.setReadOnly(isReadOnly);
	view.setRunningMode(o_running_mode);
	view.repaint();

	if (list.size() != 0) {
	    GraphPanelSet panelSet = view.getGraphPanelSet();
	    
	    LinkedList wlist = new LinkedList();
	    if (update) {
		updatePerform(wlist, list);
	    }
	    else {
		wlist.addAll(panelSet.getGraphElements(panel_num));
		wlist.addAll(list);
	    }
	    
	    GraphPanel panel = panelSet.getPanel(panel_num);
	    StandardVMStatement vmstat = new StandardVMStatement
		(VMOP.getVMOP(VMOP.IMPORT), view.getPanel(panel_num));
	    vmstat.beforeExecute();
	    UndoVMStack.getInstance(panel).push(vmstat);
	    
	    panelSet.setGraphElements(wlist, panel_num);
	    
	    vmstat.afterExecute();
	    
	    panel.getCanvas().readaptSize();
	}

	//System.out.println("trace #3");
	view.setMessage(o_msg);

	//view.setCursor(cursor);
	Utils.setCursor(view, cursor);
	view.repaint();
    }

    private void updatePerform(LinkedList wlist, LinkedList list) {
	GraphPanelSet panelSet = view.getGraphPanelSet();
	LinkedList owlist = panelSet.getGraphElements(panel_num);
	int owlist_size = owlist.size();
	int list_size = list.size();

	for (int i = 0; i < owlist_size; i++) {
	    GraphElement ographElement = (GraphElement)owlist.get(i);
	    boolean found = false;

	    for (int j = 0; j < list_size; j++) {
		GraphElement ngraphElement = (GraphElement)list.get(j);

		if (compare(ographElement, ngraphElement) &&
		    ographElement.isSelected()) { // this second test means is for duplicate array (same name + same chr)
		    if (!wlist.contains(ngraphElement)) {
			wlist.add(ngraphElement);
			ngraphElement.setSelected(true, panelSet.getPanel(panel_num).getCanvas());
			found = true;
			break;
		    }
		}
	    }

	    if (!found)
		wlist.add(ographElement);
	}

	String update_msg = "";
	for (int j = 0; j < list_size; j++) {
	    GraphElement ngraphElement = (GraphElement)list.get(j);
	    boolean found = false;

	    for (int i = 0; i < owlist_size; i++) {
		GraphElement ographElement = (GraphElement)owlist.get(i);

		if (compare(ographElement, ngraphElement)) {
		    found = true;
		    break;
		}
	    }

	    update_msg += VAMPUtils.getMessageName(ngraphElement);
	    if (!found) {
		update_msg +=
		    " CANNOT be updated (reimport the profile and update it)\n";
	    }
	    else {
		update_msg += " has been updated\n";
	    }
	}

	if (update_msg.length() > 0)
	    InfoDialog.pop(view.getGlobalContext(), update_msg);
    }

    private boolean compare(GraphElement ographElement, GraphElement ngraphElement) {
	String nChr = VAMPUtils.getChr(ngraphElement);
	String nURL = ngraphElement.getURL();
	if (nURL == null)
	    nURL = ngraphElement.getSourceURL();

	String oChr = VAMPUtils.getChr(ographElement);
	String oURL = ographElement.getURL();
	if (oURL == null)
	    oURL = ographElement.getSourceURL();

	return oURL.equals(nURL) && oChr.equals(nChr);
    }
}
