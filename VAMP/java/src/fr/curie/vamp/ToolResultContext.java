
/*
 *
 * ToolResultContext.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.Vector;
import java.util.Date;
import java.util.TreeMap;

public class ToolResultContext {

    private GlobalContext globalContext;
    private ToolResultInfo info;
    private Vector<GraphElement> graphElements;
    private GraphElement graphElementBase;
    private GraphElement graphElementDiscrim;

    private GraphElementFactory factory;
    private GraphElement graphElementResult;

    ToolResultContext(GlobalContext globalContext, String opname, TreeMap params, Vector<GraphElement> graphElements, Vector<String> graphElementIDs, GraphElement graphElementBase, GraphElement graphElementDiscrim, String chr) {

	this.globalContext = globalContext;
	Date date = new Date();
	this.info = new ToolResultInfo(date, opname, params, graphElementIDs);
	this.graphElements = graphElements;
	this.graphElementBase = graphElementBase;
	this.graphElementDiscrim = graphElementDiscrim;

	this.factory = null;
	this.graphElementResult = null;
    }

    ToolResultContext(GlobalContext globalContext, String opname, TreeMap params, Vector<String> graphElementIDs, GraphElement graphElementBase, GraphElement graphElementDiscrim, String chr) {
	this(globalContext, opname, params, null, graphElementIDs, graphElementBase, graphElementDiscrim, "");
    }

    ToolResultContext(GlobalContext globalContext, String opname, TreeMap params, Vector<GraphElement> graphElements, GraphElement graphElementBase, GraphElement graphElementDiscrim) {
	this(globalContext, opname, params, graphElements, GraphElementListOperation.makeVectorID(graphElements), graphElementBase, graphElementDiscrim, "");
    }

    public Vector<GraphElement> getGraphElements() {
	return graphElements;
    }

    public GraphElement getGraphElementBase() {
	return graphElementBase;
    }

    public GraphElement getGraphElementDiscrim() {
	return graphElementDiscrim;
    }

    public GraphElementFactory getFactory() {
	return factory;
    }

    public GraphElement getGraphElementResult() {
	return graphElementResult;
    }

    void setFactory(GraphElementFactory factory) {
	this.factory = factory;
    }

    void setGraphElementResult(GraphElement graphElementResult) {
	this.graphElementResult = graphElementResult;
    }

    ToolResultInfo getInfo() {
	return info;
    }
}
