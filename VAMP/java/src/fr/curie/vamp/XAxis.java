
/*
 *
 * XAxis.java
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

public class XAxis extends Axis {

    XAxis(GraphCanvas canvas, Margins margins) {
	super("xaxis", canvas, margins);
    }

    static final int _vSize = 3;

    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	Dimension dim = getSize();
	g2.setColor(VAMPResources.getColor(VAMPResources.AXIS_BG));
	g2.fillRect(0, 0, dim.width, dim.height);
	g2.setColor(VAMPResources.getColor(VAMPResources.AXIS_FG));
	g2.drawRect(0, 0, dim.width-1, dim.height-1);
	canvas.paintXAxis(g2, this);
    }
}
