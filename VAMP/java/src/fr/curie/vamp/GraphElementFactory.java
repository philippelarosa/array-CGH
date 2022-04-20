
/*
 *
 * GraphElementFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.TreeMap;

abstract public class GraphElementFactory {

    protected GlobalContext globalContext;

    protected GraphElementFactory(GlobalContext globalContext) {
	this.globalContext = globalContext;
    }

    abstract public void init(String name, int data_cnt) throws Exception;

    abstract public void init(String name, int data_cnt, TreeMap properties) throws Exception;

    abstract public void init(String name, int data_cnt, TreeMap properties, boolean pangen) throws Exception;

    abstract public void add(RODataElementProxy data) throws Exception;

    abstract public void write(RODataElementProxy data) throws Exception;

    //abstract public void set(int n, RODataElementProxy data) throws Exception;

    abstract public void setGraphElementProperties(TreeMap properties) throws Exception;

    abstract public GraphElement epilogue() throws Exception;

    abstract public GraphElement getGraphElement() throws Exception;

    abstract public RWDataElementProxy makeRWDataElementProxy();

    abstract public void setProbeCount(int probe_cnt, RODataElementProxy probe);

    abstract public String serialFile();

    abstract public void deleteSerialFiles();
}
