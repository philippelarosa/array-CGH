
/*
 *
 * DendrogramNode.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

abstract class DendrogramNode {

    private Dendrogram dendrogram;
    private DendrogramNode parent;
    private String id;
    private HashMap userData;

    public DendrogramNode(String id) {
	this(id, null);
    }

    public DendrogramNode(String id, DendrogramNode parent) {
	this.id = id;
	this.parent = parent;
	userData = new HashMap();
    }

    public DendrogramNode getParent() {return parent;}

    public void setParent(DendrogramNode parent) {
	this.parent = parent;
    }

    abstract public double getHeight();

    public double getMaxHeight(double maxheight) {
	return maxheight;
    }

    abstract public void makeLeaves(Vector leaves);

    public Dendrogram getDendrogram() {return dendrogram;}

    public void setDendrogram(Dendrogram dendrogram) {
	this.dendrogram = dendrogram;
    }

    abstract public double makePos();

    public String getID() {
	return id;
    }

    void setUserData(Object key, Object value) {
	userData.put(key, value);
    }

    Object getUserData(Object key) {
	return userData.get(key);
    }

    void unsetUserData(Object key) {
	userData.remove(key);
    }

    abstract DendrogramNode copy();
    abstract DendrogramNode find(String id);

    abstract void setMinOrder(int order);

    abstract double getExtLeftPos();
    abstract double getExtRightPos();
}
