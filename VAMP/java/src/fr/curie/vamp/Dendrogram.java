
/*
 *
 * Dendrogram.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class Dendrogram {

    static final Property DendrogramProp =
	Property.getHiddenProperty("Dendrogram");

    static final Property DendrogramLeafProp =
	Property.getHiddenProperty("DendrogramLeaf");

    static final Property DendrogramOrderProp =
	Property.getHiddenProperty("DendrogramOrder");

    static final int X_TYPE = 1;
    static final int Y_TYPE = 2;

    private DendrogramNode root;
    private Vector leaves; // redundant information
    private double maxheight;
    private int type;

    public Dendrogram(DendrogramNode root, int type) {
	this.root = root;
	this.type = type;
	compile();
    }

    public DendrogramNode getRoot() {return root;}
    public Vector getLeaves() {return leaves;}

    public String toString() {
	return root.toString();
    }

    public double getMaxHeight() {return maxheight;}

    public int getMinOrder() {
	int size = leaves.size();
	int min_order = Integer.MAX_VALUE;
	for (int n = 0; n < size; n++) {
	    DendrogramNode node = (DendrogramNode)leaves.get(n);
	    if (node instanceof DendrogramLeaf) {
		int order = ((DendrogramLeaf)node).getOrder();
		if (order < min_order)
		    min_order = order;
	    }
	}
	return min_order;
    }

    private void compile() {
	leaves = new Vector();
	root.makeLeaves(leaves);
	root.setDendrogram(this);
	root.makePos();
	maxheight = root.getMaxHeight(0);
	root.setMinOrder(getMinOrder());
	
    }

    DendrogramNode find(String id) {
	return root.find(id);
    }

    int getType() {
	return type;
    }
}
