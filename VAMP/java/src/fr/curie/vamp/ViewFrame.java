
/*
 *
 * ViewFrame.java
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

public class ViewFrame extends JFrame {

    private View view;
    private GlobalContext globalContext;

    public ViewFrame(GlobalContext globalContext, String name,
		     PanelProfile panelProfiles[],
		     PanelLayout panelLayout,
		     PanelLinks panelLinks[],
		     Menu newViewMenu,
		     Menu currentViewMenu,
		     LinkedList graphElements, Dimension size,
		     InfoDisplayer infoDisplayer) {
	super(VAMPUtils.getTitle() + " Interface: " + name);

	this.globalContext = globalContext;
	view = new View(globalContext, name,
			panelProfiles,
			panelLayout,
			panelLinks,
			newViewMenu,
			currentViewMenu,
			graphElements,
			infoDisplayer,
			size);
	view.setViewFrame(this);
	setJMenuBar(view.makeMenuBar(this));
	setContentPane(view);
	setSize(size.width, size.height);
	addWindowListener(new WindowAdapter() {
		public void windowClosed(WindowEvent e) {
		    suppress();
		}
	    });
	getViewFrameList().add(this);
	addWindowListener(new WindowAdapter() {
		public void windowActivated(WindowEvent e) {
		    // disconnected because of a "psycadelic" problem on Windows
		    /*
		    System.out.println("Activated");
		    view.getGraphPanelSet().getPanel(View.TOP_PANEL).getCanvas().requestFocus();
		    */
		}

		/*
		public void windowDeactivated(WindowEvent e) {
		}
		*/
	    });
    }

    private void suppress() {
	view.suppress();
	getViewFrameList().remove(this);
	Utils.gc();
    }

    static final String VIEW_FRAME_LIST = "ViewFrameList";

    static void init(GlobalContext globalContext) {
	globalContext.put(VIEW_FRAME_LIST, new LinkedList());
    }

    LinkedList getViewFrameList() {
	return getViewFrameList(globalContext);
    }

    static LinkedList getViewFrameList(GlobalContext globalContext) {
	return (LinkedList)globalContext.get(VIEW_FRAME_LIST);
    }

    public View getView() {return view;}
}
