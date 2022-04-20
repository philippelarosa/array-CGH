
/*
 *
 * ToolMenuItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.awt.*;
import java.util.*;

class ToolMenuItem extends MenuItem {

    GraphElementListOperation op;

    protected ToolMenuItem(GraphElementListOperation op, String name, boolean sep) {
	super(name, sep);
	this.op = op;
    }

    protected ToolMenuItem(GraphElementListOperation op, String name) {
	this(op, name, false);
    }

    protected ToolMenuItem(GraphElementListOperation op) {
	this(op, op.getMenuName(), false);
    }

    GraphElementListOperation getOP() {return op;}
}
