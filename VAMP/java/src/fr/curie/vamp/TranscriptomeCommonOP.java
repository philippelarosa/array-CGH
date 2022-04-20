
/*
 *
 * TranscriptomeCommonOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

abstract class TranscriptomeCommonOP extends GraphElementListOperation {
   
    TranscriptomeCommonOP(String name, int flags) {
	super(name, flags);
    }

    String getID(DataElement data) {
	return data.getPropertyValue(TranscriptomeFactory.ObjectIdProp) + ":" +
	    data.getPropertyValue(TranscriptomeFactory.PosBeginProp) + ":" +
	    data.getPropertyValue(TranscriptomeFactory.PosEndProp);

    }

    HashSet makeSet(Vector graphElements) {
	HashSet set = new HashSet();
	int size = graphElements.size();

	for (int m = 0; m < size; m++) {
	    DataSet dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (dataSet == null) return null;
	    DataElement data[] = dataSet.getData();
	    for (int n = 0; n < data.length; n++)
		set.add(getID(data[n]));
	}

	return set;
    }

    HashMap makeMap(GraphElement graphElement) {
	DataSet dataSet = graphElement.asDataSet();
	if (dataSet == null) return null;
	HashMap map = new HashMap();
	DataElement data[] = dataSet.getData();
	for (int n = 0; n < data.length; n++)
	    map.put(getID(data[n]), data[n]);
	return map;
    }

    DataElement getData(String s, HashMap map) {
	return (DataElement)map.get(s);
    }
}
