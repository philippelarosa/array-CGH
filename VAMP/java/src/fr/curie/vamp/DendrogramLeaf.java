
/*
 *
 * DendrogramLeaf.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class DendrogramLeaf extends DendrogramNode {

    private int order;
    private double pos;
    private String dataSetID;

    public DendrogramLeaf(String dataSetID, String order) {
	this(dataSetID, Utils.parseInt(order)-1);
    }

    private DendrogramLeaf(String dataSetID, int order) {
	super(dataSetID);
	this.dataSetID = dataSetID;
	this.order = order;
	this.pos = this.order + 0.5;
    }

    public String getDataSetID() {return dataSetID;}

    public String toString() {
	return "DendrogramLeaf[#" + order + ": " + dataSetID + "]";
    }

    public void makeLeaves(Vector leaves) {
	leaves.add(this);
    }

    public double makePos() {
	//return order;
	return pos;
    }

    public void setPos(double pos) {
	this.pos = pos;
    }

    public double getHeight() {return 0;}

    public int getOrder() {
	return order;
    }

    DendrogramNode copy() {
	DendrogramLeaf leaf = new DendrogramLeaf(dataSetID, order);
	return leaf;
    }

    DendrogramNode find(String id) {
	if (id.equals(dataSetID))
	    return this;
	return null;
    }

    void setMinOrder(int order) {
	this.order -= order;
	this.pos -= order;
    }

    double getExtLeftPos() {
	return pos;
    }

    double getExtRightPos() {
	return pos;
    }
}
