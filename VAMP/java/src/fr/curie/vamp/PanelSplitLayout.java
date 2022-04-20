
/*
 *
 * PanelSplitLayout.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class PanelSplitLayout extends PanelLayout {
    
    static final int VERTICAL = 1;
    static final int HORIZONTAL = 2;

    private int orientation;
    private PanelLayout first, second;
    private double divider;

    PanelSplitLayout(int orientation, PanelLayout first, PanelLayout second) {
	this(orientation, first, second, .5);
    }

    PanelSplitLayout(int orientation, PanelLayout first, PanelLayout second,
		     double divider) {
	this.orientation = orientation;
	this.first = first;
	this.second = second;
	this.divider = divider;
    }

    int getOrientation() {return orientation;}
    PanelLayout getFirst() {return first;}
    PanelLayout getSecond() {return second;}

    Component makeComponent(GraphPanel panels[]) {
	Component first_c = first.makeComponent(panels);
	Component second_c = second.makeComponent(panels);

	JSplitPane jpane = new JSplitPane(orientation == VERTICAL ?
					  JSplitPane.HORIZONTAL_SPLIT :
					  JSplitPane.VERTICAL_SPLIT,
					  first_c, second_c);

	jpane.setOneTouchExpandable(true);
	jpane.setDividerLocation(divider);
	jpane.setDividerSize(2);

	jpane.setResizeWeight(0.5);

	makeSynchro(first_c, jpane);
	makeSynchro(second_c, jpane);

	return jpane;
    }

    private void makeSynchro(Component comp, JSplitPane jpane) {
	if (!(comp instanceof GraphPanel))
	    return;

	GraphPanel panel = (GraphPanel)comp;
	panel.setRootSplitPane(jpane);
	panel.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent e) {
		    ((GraphPanel)e.getSource()).syncSizes();
		}
	    });
    }

    void trace(String indent) {
	System.out.println(indent + "SplitLayout [" + orientation + "] {");
	first.trace(indent + "\t");
	second.trace(indent + "\t");
	System.out.println(indent + "}");
    }
}
