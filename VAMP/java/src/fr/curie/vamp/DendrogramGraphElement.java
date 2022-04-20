
/*
 *
 * DendrogramGraphElement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

class DendrogramGraphElement extends GraphElement {

    DendrogramNode dendro_node;
    boolean bridge;
    double vx1, vy1, vx2, vy2;
    DendrogramGraphElement left, right, child;

    DendrogramGraphElement(DendrogramNode dendro_node) {
	this.dendro_node = dendro_node;
	this.bridge = false;
	this.left = null;
	this.right = null;
    }

    DendrogramGraphElement(DendrogramNode dendro_node, 
			   DendrogramGraphElement left,
			   DendrogramGraphElement right) {
	this.dendro_node = dendro_node;
	this.bridge = true;
	this.left = left;
	this.right = right;
    }

    void setChild(DendrogramGraphElement child) {
	this.child = child;
    }

    DendrogramNode getDendrogramNode() {return dendro_node;}
    boolean isBridge() {return bridge;}
    boolean isLeaf() {return dendro_node instanceof DendrogramLeaf;}

    private DendrogramGraphElement copy() {
	DendrogramGraphElement dendroGE = (DendrogramGraphElement)super.clone();
	this.color = color;
	this.bridge = bridge;

	dendroGE.dendro_node = dendro_node;
	// 14/12/04: this does not work !!!
	if (isBridge()) {
	    dendroGE.left = left.copy();
	    dendroGE.right = right.copy();
	}
	else if (child != null)
	    dendroGE.child = child.copy();

	return dendroGE;
    }

    public String toString() {
	return "DendrogramGraphElement[bridge=" + bridge + ",node=" +
	    dendro_node + "]";
    }

    DendrogramGraphElement asDendrogramGraphElement() {return this;}

    double getVX1() {return vx1;}
    double getVY1() {return vy1;}
    double getVX2() {return vx2;}
    double getVY2() {return vy2;}

    void setVX1(double vx1) {this.vx1 = vx1;}
    void setVX2(double vx2) {this.vx2 = vx2;}
    void setVY1(double vy1) {this.vy1 = vy1;}
    void setVY2(double vy2) {this.vy2 = vy2;}

    public final static Property BranchTypeProp =
	Property.getProperty("Branch Type ");
    public final static Property DendroIDProp =
	Property.getProperty("Dendrogram ID", true);
    public final static Property LeftIDProp =
	Property.getProperty("Left ID");
    public final static Property RightIDProp =
	Property.getProperty("Right ID");
    public final static Property HeightProp =
	Property.getProperty("Height");

    void compile() {

	compile_pos();

	VAMPUtils.setType(this, VAMPConstants.DENDROGRAM_BRANCH_TYPE);

	//System.out.println("compiling " + dendro_node.getID());
	DendrogramBinNode dendro_parent =
	    (DendrogramBinNode)dendro_node.getParent();

	setPropertyValue(DendroIDProp, dendro_node.getID());

	if (isLeaf()) {
	    DendrogramLeaf dendro_leaf = (DendrogramLeaf)dendro_node;
	    double height = dendro_parent.getHeight();
	    int order = dendro_leaf.getOrder();

	    setPropertyValue(BranchTypeProp, "leaf");
	    setPropertyValue(HeightProp, Utils.toString(height));
	    setPropertyValue(VAMPProperties.OrderProp, Utils.toString(order));
	    return;
	}

	DendrogramBinNode dendro_bnode = (DendrogramBinNode)dendro_node;
	double height = dendro_bnode.getHeight();
	setPropertyValue(LeftIDProp, dendro_bnode.getLeft().getID());
	setPropertyValue(RightIDProp, dendro_bnode.getRight().getID());

	if (isBridge()) {
	    setPropertyValue(BranchTypeProp, "bridge");
	    setPropertyValue(HeightProp, Utils.toString(height));
	    return;
	}

	setPropertyValue(BranchTypeProp, "branch");
	setPropertyValue(HeightProp, Utils.toString(dendro_parent.getHeight()));
    }

    void compile_pos() {

	dendro_node.makePos();

	DendrogramBinNode dendro_parent =
	    (DendrogramBinNode)dendro_node.getParent();

	if (isLeaf()) {
	    DendrogramLeaf dendro_leaf = (DendrogramLeaf)dendro_node;
	    double height = dendro_parent.getHeight();
	    double pos = dendro_leaf.makePos();

	    vx1 = 0;
	    vy1 = pos;
	    vx2 = height;
	    vy2 = vy1;

	    adapt_pos();
	    return;
	}

	DendrogramBinNode dendro_bnode = (DendrogramBinNode)dendro_node;
	double height = dendro_bnode.getHeight();

	if (isBridge()) {
	    vx1 = height;
	    vy1 = dendro_bnode.getLeftPos();
	    vx2 = height;
	    vy2 = dendro_bnode.getRightPos();
	    adapt_pos();
	    return;
	}

	vx1 = height;
	vy1 = dendro_bnode.getMiddlePos();
	vx2 = dendro_parent.getHeight();
	vy2 = vy1;

	adapt_pos();
    }

    private void adapt_pos() {
	if (dendro_node.getDendrogram().getType() != Dendrogram.X_TYPE)
	    return;

	double v;
	v = vx1;
	vx1 = vy1;
	vy1 = v;

	v = vx2;
	vx2 = vy2;
	vy2 = v;
    }

    public void setPos(double pos) {
	if (isLeaf()) {
	    DendrogramLeaf dendro_leaf = (DendrogramLeaf)dendro_node;
	    dendro_leaf.setPos(pos);
	}
	else
	    System.err.println("INTERNAL ERROR #1");
    }

    static final int LOCAL_MODE = 1;
    static final int BRIDGE_MODE = 2;
    static final int DEPTH_MODE = 3;

    Color color = Color.GRAY;

    public void setColor(Color color, int mode) {
	this.color = color;
	if (mode == LOCAL_MODE)
	    return;

	if (mode == BRIDGE_MODE)
	    mode = LOCAL_MODE;

	if (child != null)
	    child.setColor(color, mode);
	else if (isBridge()) {
	    left.setColor(color, mode);
	    right.setColor(color, mode);
	}
    }

    public Color getColor() {
	return color;
    }

    public void setSelected(boolean selected, int mode) {
	setSelected(selected, null);

	if (mode == LOCAL_MODE)
	    return;

	if (mode == BRIDGE_MODE)
	    mode = LOCAL_MODE;

	if (child != null)
	    child.setSelected(selected, mode);
	else if (isBridge()) {
	    left.setSelected(selected, mode);
	    right.setSelected(selected, mode);
	}
    }

    void make(LinkedList list, Dendrogram dendro) {
	list.add(this);
	String id = dendro_node.getID();
	dendro_node = dendro.find(id);

	if (isBridge()) {
	    left.make(list, dendro);
	    right.make(list, dendro);
	}
	else if (child != null)
	    child.make(list, dendro);
    }

    LinkedList copySubTree() {
	Dendrogram dendro = new Dendrogram
	    (dendro_node.copy(),
	     dendro_node.getDendrogram().getType());
	DendrogramGraphElement dendroGE = copy();

	LinkedList list = new LinkedList();
	dendroGE.make(list, dendro);
	return list;
    }

    public boolean isPasteable(int action) {
	return false;
    }
}
