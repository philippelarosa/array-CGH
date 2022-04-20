
/*
 *
 * DataSetFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.util.TreeMap;
import java.util.Vector;

class DataSetFactory extends GraphElementFactory {

    private DataSet dataSet;
    private Vector<DataElement> data_v = null;
    private int data_cnt;
    private int data_n;

    public DataSetFactory(GlobalContext globalContext, DataSet dataSet) {
	super(globalContext);

	this.dataSet = dataSet;
	data_n = 0;
    }

    public DataSetFactory(GlobalContext globalContext) {
	this(globalContext, null);
    }

    public void init(String name, int data_cnt) throws Exception {
	init(name, data_cnt, null);
    }

    public void init(String name, int data_cnt, TreeMap properties) throws Exception {
	init(name, data_cnt, properties, true);
    }

    public void init(String name, int data_cnt, TreeMap properties, boolean pangen) throws Exception {
	if (dataSet == null) {
	    dataSet = new DataSet();
	}

	if (properties != null) {
	    dataSet.setProperties(properties);
	}

	this.data_cnt = data_cnt;

	if (data_cnt == 0) {
	    data_v = new Vector();
	}
	else {
	    DataElement data[] = new DataElement[data_cnt];
	    dataSet.setData(data);
	}
	// what about name ?
    }

    public void add(RODataElementProxy data) throws Exception {
	if (data_v != null) {
	    data_v.add((DataElement)data);
	}
	else {
	    dataSet.getData()[data_n++] = (DataElement)data;
	}
    }

    public void write(RODataElementProxy data) throws Exception {
	if (data_v != null) {
	    data_v.add((DataElement)data);
	}
	else {
	    dataSet.getData()[data_n++] = (DataElement)data;
	}
    }

    /*
    public void set(int n, RODataElementProxy data) throws Exception {
	dataSet.getData()[n] = (DataElement)data;
    }
    */

    public void setGraphElementProperties(TreeMap properties) throws Exception {
	dataSet.setProperties(properties);
    }

    public GraphElement epilogue() throws Exception {
	if (data_v != null) {
	    int length = data_v.size();
	    assert length == data_cnt;
	    DataElement data[] = new DataElement[data_cnt];
	    for (int n = 0; n < length; n++) {
		data[n] = data_v.get(n);
	    }
	    dataSet.setData(data);
	    data_v = null;
	}

	return dataSet;
    }

    public void setProbeCount(int data_cnt, RODataElementProxy probe) {
	this.data_cnt = data_cnt;
    }

    public GraphElement getGraphElement() throws Exception {
	return dataSet;
    }

    public RWDataElementProxy makeRWDataElementProxy() {
	return new DataElement();
    }

    public String serialFile() {
	return null;
    }

    public void deleteSerialFiles() {
    }
}
