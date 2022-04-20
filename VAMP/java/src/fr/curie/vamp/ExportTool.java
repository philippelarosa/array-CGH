
/*
 *
 * ExportTool.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

abstract class ExportTool {

    public final int ADD_SEPARATOR = 0x1;
    protected String name;
    protected int flags;

    static private Vector tools = new Vector();

    ExportTool(String name, int flags) {
	this.name = name;
	this.flags = flags;
	tools.add(this);
    }

    boolean addSeparator() {return (flags & ADD_SEPARATOR) != 0;}

    static Vector getTools() {return tools;}
    String getName() {return name;}

    abstract void perform(View view);

    abstract boolean isEnabled(View view);
}
