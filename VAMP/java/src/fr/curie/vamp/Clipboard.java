
/*
 *
 * Clipboard.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class Clipboard {

    private LinkedList contents = new LinkedList();

    private static Clipboard instance;
    private Object dragSource, dropTarget;
    private boolean dragMoving = false;

    private Clipboard() {
    }

    static Clipboard getInstance() {
	if (instance == null)
	    instance = new Clipboard();
	return instance;
    }

    void clear() {
	contents.clear();
    }

    void add(Pasteable pasteable) {
	contents.add(pasteable);
    }

    LinkedList getContents() {return contents;}

    void setDragInfo(Object dragSource, boolean dragMoving) {
	this.dragSource = dragSource;
	this.dragMoving = dragMoving;
    }

    Object getDragSource() {
	return dragSource;
    }

    void setDropTarget(Object dropTarget) {
	this.dropTarget = dropTarget;
    }

    Object getDropTarget() {
	return dropTarget;
    }

    boolean isDragMoving() {
	return dragMoving;
    }
}
