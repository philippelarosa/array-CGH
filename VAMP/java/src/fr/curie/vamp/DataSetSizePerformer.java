
/*
 *
 * DataSetSizePerformer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class DataSetSizePerformer implements DataSetPerformer {

    private static DataSetSizePerformer set_size, unset_size;
    private final boolean set;

    public static DataSetSizePerformer getSetSizePerformer() {
	if (set_size == null)
	    set_size = new DataSetSizePerformer(true);
	return set_size;
    }

    public static DataSetSizePerformer getUnsetSizePerformer() {
	if (unset_size == null)
	    unset_size = new DataSetSizePerformer(false);
	return unset_size;
    }

    private DataSetSizePerformer(boolean set) {
	this.set = set;
    }

    public void apply(java.util.LinkedList graphElements) {
	int size = graphElements.size();
	for (int m = 0; m < size; m++) {
	    DataSet dataSet = ((GraphElement)graphElements.get(m)).asDataSet();
	    if (dataSet == null) {
		continue;
	    }

	    if (equals(dataSet.getLastDataSetPerformer())) {
		continue;
	    }

	    dataSet.setLastDataSetPerformer(this);
	    
	    int length = dataSet.getProbeCount();
	    DataElement data[] = dataSet.getData();
	    for (int n = 0; n < length; n++) {
		DataElement item = data[n];
		if (set /*&& item.getPropertyValue(VAMPConstants.IsNAProp) == null*/) {
		    String v = (String)item.getPropertyValue(VAMPProperties.SizeProp);
		    if (v != null && !v.equalsIgnoreCase("NA"))
			item.setPosSize(dataSet, Utils.parseDouble(v));
		    else
			item.setPosSize(dataSet, 0);
		}
		else
		    item.setPosSize(dataSet, 0);
	    }
	}
    }
}
