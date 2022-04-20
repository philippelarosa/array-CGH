
/*
 *
 * VMOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;

class VMOP {

    public static final String CUT = "Cut";
    public static final String DRAG_AND_DROP = "Drag and drop";
    public static final String MOVE_MARK = "Move landmark";
    public static final String PASTE = "Paste";
    public static final String REMOVE_ALL = "Remove all";
    public static final String REMOVE_SELECTION = "Remove selection";
    public static final String REMOVE_PROFILES = "Remove profiles";
    public static final String REMOVE_MARKS = "Remove landmarks";
    public static final String REMOVE_REGIONS = "Remove regions";
    public static final String SET_SCALE_X = "Set scale X";
    public static final String SET_SCALE_Y = "Set scale Y";
    public static final String FIT_IN_PAGE = "Fit in page";
    public static final String SORT = "Sort";
    public static final String FILTER = "Filter";
    public static final String IMPORT = "Import";
    public static final String CREATE_MARK = "Put landmark";
    public static final String CREATE_REGION = "Create region";
    public static final String REMOVE_MARK = "Remove landmark";
    public static final String REMOVE_REGION = "Remove region";

    static HashMap vmop_map = new HashMap();

    private String name;

    static {
	new VMOP(CUT);
	new VMOP(DRAG_AND_DROP);
	new VMOP(MOVE_MARK);
	new VMOP(PASTE);
	new VMOP(REMOVE_ALL);
	new VMOP(REMOVE_SELECTION);
	new VMOP(REMOVE_MARKS);
	new VMOP(REMOVE_REGIONS);
	new VMOP(SET_SCALE_X);
	new VMOP(SET_SCALE_Y);
    }

    private VMOP(String name) {
	this.name = name;
	vmop_map.put(name, this);
    }

    static VMOP getVMOP(String name) {
	VMOP vmop = (VMOP)vmop_map.get(name);
	if (vmop == null) // kludge ??
	    vmop = new VMOP(name);
	return vmop;
    }

    public String getName() {
	return name;
    }
}
