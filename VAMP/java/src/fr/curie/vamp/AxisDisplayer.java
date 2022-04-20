
/*
 *
 * AxisDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;

public abstract class AxisDisplayer implements Cloneable {

    String name;
    String canonicalName;
    static private HashMap displayers = new HashMap();
    protected GraphElementIDBuilder graphElementIDBuilder;

    public AxisDisplayer(String name, String canonicalName,
			 GraphElementIDBuilder graphElementIDBuilder) {
	this.name = name;
	this.canonicalName = canonicalName;

	setGraphElementIDBuilder(graphElementIDBuilder);
	if (get(getName()) == null)
	    displayers.put(getName(), this);
    }

    void setGraphElementIDBuilder(GraphElementIDBuilder graphElementIDBuilder) {
	this.graphElementIDBuilder = graphElementIDBuilder;
    }

    GraphElementIDBuilder getGraphElementIDBuilder() {
	return graphElementIDBuilder;
    }

    static AxisDisplayer get(String name) {
	// BWC
	if (name.equals("Chromosome"))
	    name = "Array / Chromosome Name";
	else if (name.equals("Standard"))
	    name = "Array / Genomic Position";
	else if (name.equals("Genomic Position"))
	    name = "Array / Genomic Position";

	return (AxisDisplayer)displayers.get(name);
    }

    /*
    static AxisDisplayer get(String name) {
	int size = displayers.size();
	for (int n = 0; n < size; n++) {
	    AxisDisplayer displayer =
		(AxisDisplayer)displayers.get(n);
	    if (displayer.getName().equals(name))
		return displayer;
	}

	return null;
    }
    */

    String getName() {
	return getName(name, getCanonicalName());
    }

    public static String getName(String name, String canName) {
	return name + " / " + canName;
    }

    String getCanonicalName() {
	return canonicalName;
    }

    abstract public void displayXAxis(GraphCanvas canvas, Axis xaxis,
				      Graphics2D g, GraphElement graphElement, int m,
				      PrintContext pctx);
    abstract public void displayYAxis(GraphCanvas canvas, Axis yaxis,
				      Graphics2D g, GraphElement graphElement, int m,
				      PrintContext pctx);

    public void display(GraphCanvas canvas, Graphics2D g,
			GraphElement graphElement, int m, PrintContext pctx) {
	displayXAxis(canvas, null, g, graphElement, m, pctx);
	displayYAxis(canvas, null, g, graphElement, m, pctx);
    }

    public double getMaxX(GraphElement graphElement) {return Double.MIN_VALUE;}
    public double getMaxY(GraphElement graphElement) {return Double.MIN_VALUE;}
    public double getMinX(GraphElement graphElement) {return Double.MAX_VALUE;}
    public double getMinY(GraphElement graphElement) {return Double.MAX_VALUE;}

    protected Object clone() throws CloneNotSupportedException {
	return super.clone();
    }

    boolean checkGraphElements(LinkedList graphElements) {
	return true;
    }

    boolean isCompatible(GraphElementDisplayer graphElementDisplayer) {
	return graphElementDisplayer.isCompatible(this);
    }

    void warnGraphElements(GlobalContext globalContext,
			   LinkedList graphElements) {
    }

    void displayInfo(GraphCanvas canvas, Axis xaxis, Graphics2D g) {
    }
}

