
/*
 *
 * InfoDisplayer.java
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

abstract class InfoDisplayer implements Cloneable {

    abstract public JPanel display(JComponent parent, GraphElement set,
				   PropertyElement elem, Region region, Mark mark, boolean pinnedUp);

    abstract public JPanel display(JComponent parent, GraphElement set,
				   String propName, String sop, String value,
				   Color color);

    abstract public JPanel displayAnnotLegendPanel(JComponent parent, View view);

    protected Object clone() throws CloneNotSupportedException {
	return super.clone();
    }
}
