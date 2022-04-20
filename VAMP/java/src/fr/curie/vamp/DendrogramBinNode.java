
/*
 *
 * DendrogramBinNode.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class DendrogramBinNode extends DendrogramNode {
    private double height;

    private DendrogramNode left;
    private DendrogramNode right;
    private double middle_pos, left_pos, right_pos;

    public DendrogramBinNode(String id, double height,
			     DendrogramNode left,
			     DendrogramNode right) {
	super(id);
	this.height = height;
	this.left = left;
	this.right = right;

	this.left.setParent(this);
	this.right.setParent(this);
    }

    public double getHeight() {return height;}
    public DendrogramNode getLeft() {return left;}
    public DendrogramNode getRight() {return right;}

    public String toString() {
	return "DendrogramNode[height=" + height + ",ID=" + getID() + "]";
    }

    public double getMaxHeight(double maxheight) {
	if (height > maxheight)
	    maxheight = height;

	maxheight = left.getMaxHeight(maxheight);
	maxheight = right.getMaxHeight(maxheight);

	return maxheight;
    }

    public void setDendrogram(Dendrogram dendrogram) {
	super.setDendrogram(dendrogram);

	left.setDendrogram(dendrogram);
	right.setDendrogram(dendrogram);
    }

    public void makeLeaves(Vector leaves) {
	left.makeLeaves(leaves);
	right.makeLeaves(leaves);
    }

    public double makePos() {
	left_pos = left.makePos();
	right_pos = right.makePos();

	if (left_pos > right_pos)
	    middle_pos = right_pos + (left_pos - right_pos) / 2;
	else
	    middle_pos = left_pos + (right_pos - left_pos) / 2;

	return middle_pos;
    }

    public double getLeftPos() {return left_pos;}
    public double getRightPos() {return right_pos;}
    public double getMiddlePos() {return middle_pos;}

    DendrogramNode copy() {
	DendrogramNode cleft = left.copy();
	DendrogramNode cright = right.copy();
	DendrogramBinNode node = new DendrogramBinNode
	    (getID(), height, cleft, cright);
	return node;
    }

    DendrogramNode find(String id) {
	if (getID().equals(id))
	    return this;
	DendrogramNode node = left.find(id);
	if (node != null)
	    return node;
	return right.find(id);
    }

    void setMinOrder(int order) {
	left.setMinOrder(order);
	right.setMinOrder(order);
    }

    double getExtLeftPos() {
	return left.getExtLeftPos();
    }

    double getExtRightPos() {
	return right.getExtRightPos();
    }
}
