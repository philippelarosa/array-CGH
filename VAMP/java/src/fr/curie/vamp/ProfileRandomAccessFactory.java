
/*
 *
 * ProfileRandomAccessFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.TreeMap;

class ProfileRandomAccessFactory extends ProfileFactory {

    public ProfileRandomAccessFactory(GlobalContext globalContext, String serialFile, int hints, GraphElement graphElementBase) {
	super(globalContext, serialFile, hints, graphElementBase);
    }

    /*
    public void init(String name, int data_cnt) throws Exception {
    }

    public void init(String name, int data_cnt, TreeMap properties) throws Exception {
    }
    */

    public void add(RODataElementProxy data) throws Exception {
    }

    public void write(RODataElementProxy data) throws Exception {
    }

    /*
    public void set(int n, RODataElementProxy data) throws Exception {
    }
    */

    public GraphElement epilogue() throws Exception {
	return profile;
    }
}
