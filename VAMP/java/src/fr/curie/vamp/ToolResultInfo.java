
/*
 *
 * ToolResultInfo.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.TreeMap;
import java.util.Vector;
import java.util.Date;

public class ToolResultInfo implements java.io.Serializable {

    Date timestamp;
    String opname;
    TreeMap params;
    Vector<String> graphElementIDs;
    int flags;
    String grphDispName;
    String axisDispName;
    String viewType;

    ToolResultInfo(Date timestamp, String opname, TreeMap params, Vector<String> graphElementIDs) {
	this(timestamp, opname, params, graphElementIDs, ToolResultManager.NEW_VIEW_IF_DIFFERENT_DISPLAYERS, "", "", "");
    }

    ToolResultInfo(Date timestamp, String opname, TreeMap params, Vector<String> graphElementIDs, int flags, String grphDispName, String axisDispName, String viewType) {
	this.timestamp = timestamp;
	this.opname = opname;
	if (params == null) {
	    params = new TreeMap();
	}
	this.params = params;
	this.graphElementIDs = graphElementIDs;
	this.flags = flags;
	this.grphDispName = grphDispName;
	this.axisDispName = axisDispName;
	this.viewType = viewType;
    }

    public String toString() {
	String str =
	    "Date: " + timestamp + "\n" +
	    "Operation: " + opname + "\n" +
	    "Params: " + params + "\n" +
	    "Profiles: " + graphElementIDs + "\n" +
	    "Graph Displayer: " + grphDispName + "\n" +
	    "Axis Displayer: " + axisDispName + "\n" +
	    "View Type: " + viewType + "\n";
	return str;
    }
}
