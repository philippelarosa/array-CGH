
/*
 *
 * AnnotAxis.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class AnnotAxis extends Axis {

    AnnotAxis(GraphCanvas canvas, Margins margins) {
	super("yaxis", canvas, margins);
	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {
		    manageInfo(e.getX(), e.getY());
		}
	    });
    }

    private AnnotDisplayer annotDisplayer;

    void setAnnotDisplayer(AnnotDisplayer annotDisplayer) {
	this.annotDisplayer = annotDisplayer;
    }

    void manageInfo(int x, int y) {
	if (annotDisplayer != null)
	    annotDisplayer.manageInfo(x, y);
    }

    private static final int _vSize = 3;

    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	Dimension dim = getSize();
	//g2.setColor(VAMPResources.getColor(VAMPResources.AXIS_BG));
	// should be in resources
	g2.setColor(Color.WHITE);
	g2.fillRect(0, 0, dim.width, dim.height);
	g2.setColor(VAMPResources.getColor(VAMPResources.AXIS_FG));
	g2.drawRect(0, 0, dim.width-1, dim.height-1);
	canvas.paintAnnotAxis(g2, this);
    }
}
