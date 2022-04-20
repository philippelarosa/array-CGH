
/*
 *
 * AnnotLegendPanel.java
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

public class AnnotLegendPanel extends JPanel {

    private int location;
    static final int NORTH = 1;
    static final int SOUTH = 2;
    View view;

    Vector prop_v;

    AnnotLegendPanel(int location, View _view) {
	this.location = location;
	this.view = _view;
	reset();

	addMouseListener(new MouseAdapter() {

		public void mouseEntered(MouseEvent e) {
		    JPanel panel = view.getInfoDisplayer().displayAnnotLegendPanel
			(view.getInfoPanel(), view);
		    view.getInfoPanel().update(panel);
		}

		public void mouseExited(MouseEvent e) {
		}
	    });
    }

    void reset() {
	prop_v = new Vector();
    }

    private class PropLegend {
	int x;
	Property prop;

	PropLegend(int x, Property prop) {
	    this.x = x;
	    this.prop = prop;
	}
    }

    void setPropertyAnnot(int x, Property prop) {
	prop_v.add(new PropLegend(x, prop));
    }

    static private final double ANGLE = -Math.PI/2;

    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	Dimension dim = getSize();
	g2.setColor(getBackground());
	g2.fillRect(0, 0, dim.width, dim.height);

	g2.setColor(Color.BLACK);
	g2.setFont(new Font("Serif", Font.BOLD, 10));
	int sz = prop_v.size();

	int y0 = getSize().height - 5;
	for (int n = 0; n < sz; n++) {
	    PropLegend plegend = (PropLegend)prop_v.get(n);
	    String s = plegend.prop.getName();
	    Dimension d = Utils.getSize(g2, s);
	    int y;
	    if (location == SOUTH)
		y = d.width + 3;
	    else
		y = y0;

	    int x = plegend.x + 6;

	    g2.rotate(ANGLE, x, y);
	    g2.drawString(s, x, y);
	    g2.rotate(-ANGLE, x, y);
	}
    }
}
