
/*
 *
 * SearchGraphElementCursor.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class SearchGraphElementCursor extends SearchCursor {

    private int index;

    SearchGraphElementCursor() {
	index = BEGIN_INDEX;
    }

    int getIndex() {return index;}
    void setIndex(int index) {this.index = index;}
}
