
/*
 *
 * SearchDataElementCursor.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class SearchDataElementCursor extends SearchCursor {

    private int index_ds, index_de;

    SearchDataElementCursor() {
	index_ds = BEGIN_INDEX;
	index_de = BEGIN_INDEX;
    }

    int getIndexDS() {return index_ds;}
    int getIndexDE() {return index_de;}

    void setIndexDS(int index_ds) {
	this.index_ds = index_ds;
    }

    void setIndexDE(int index_de) {
	this.index_de = index_de;
    }

    void setIndex(int index_ds, int index_de) {
	this.index_ds = index_ds;
	this.index_de = index_de;
    }
}
