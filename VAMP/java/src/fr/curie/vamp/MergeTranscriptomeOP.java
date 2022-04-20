
/*
 *
 * MergeTranscriptomeOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class MergeTranscriptomeOP extends MergeOP {
   
    static final String NAME = "Merge Transcriptomes";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.TRANSCRIPTOME_TYPE,
			    VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE,
			    VAMPConstants.TRANSCRIPTOME_MERGE_TYPE};
    }

public String getReturnedType() {
	return VAMPConstants.TRANSCRIPTOME_MERGE_TYPE;
    }

    static TreeSet make(Vector graphElements) {
	TreeSet treeSet = new TreeSet();
	int size = graphElements.size();
	for (int n = 0; n < size; n++)
	    treeSet.add(new DataSetArrayComparator((GraphElement)graphElements.get(n)));

	return treeSet;
    }

    MergeTranscriptomeOP() {
	super(NAME, 0, SHOW_MENU);
    }

public boolean mayApply(GraphElementListOperation op) {
	if (op == null) return true;
	return !op.equals(this) &&
	    !op.equals(GraphElementListOperation.get
		       (SplitTranscriptomeOP.NAME));
    }

    static boolean check(TreeSet treeSet) {
	String lastID = null;
	Object graphElements[] = treeSet.toArray();
	for (int n = 0; n < graphElements.length; n++) {
	    GraphElement graphElement = ((DataSetArrayComparator)graphElements[n]).graphElement;
	    if (graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.TRANSCRIPTOME_MERGE_TYPE)) continue;

	    String id = (String)graphElement.getID();
	    if (lastID == null && id.equals(lastID)) return false;
	    lastID = id;
	}
	return true;
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {
	return check(make(graphElements)) && !areAllMerged(graphElements);
    }

public Vector apply(View view, GraphPanel panel,
		 Vector _graphElements, TreeMap params,
		 boolean autoApply) {

	if (areAllMerged(_graphElements))
	    return _graphElements;

	_graphElements = applyPrologue(view, panel, _graphElements, autoApply);

	TreeSet treeSet = make(_graphElements);

	try {
	    Object graphElements[] = treeSet.toArray();

	    int length = 0;
	    for (int m = 0; m < graphElements.length; m++) {
		DataSet dataSet = ((DataSetArrayComparator)graphElements[m]).graphElement.asDataSet();
		if (dataSet == null) return null;
		length += dataSet.getData().length;
	    }

	    DataElement rData[] = new DataElement[length];

	    DataSet rDataSet = new DataSet(rData);
	    Vector curDataSets = new Vector();

	    Vector names = new Vector();
	    int l = 0;
	    for (int m = 0; m < graphElements.length; m++) {
		DataSet dataSet = ((DataSetArrayComparator)graphElements[m]).graphElement.asDataSet();
		if (dataSet == null)
		    return null;

		if (m == 0) {
		    rDataSet.cloneProperties(dataSet);
		    rDataSet.setAutoY(dataSet.isAutoY());
		    rDataSet.setAutoY2(dataSet.isAutoY2());
		}

		curDataSets.add(dataSet);
		names.add(dataSet.getPropertyValue(VAMPProperties.NameProp));

		DataElement data[] = dataSet.getData();
		for (int n = 0; n < data.length; n++) {
		    rData[l] = (DataElement)data[n].clone();
		    rData[l].copyPos(rDataSet, data[n], dataSet);
		    l++;
		}
	    }

	    VAMPUtils.setType(rDataSet, VAMPConstants.TRANSCRIPTOME_MERGE_TYPE);
	    rDataSet.setPropertyValue(VAMPProperties.TNamesProp, names.toArray());
	    rDataSet.setAxisDisplayer(Config.defaultTranscriptomeAxisDisplayer);
	    rDataSet.setPropertyValue(VAMPProperties.VectorArrayProp, curDataSets);

	    Vector rDataSets = new Vector();
	    rDataSets.add(rDataSet);
	    return undoManage(panel, rDataSets);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static boolean areAllMerged(Vector graphElements) {
	int size = graphElements.size();
	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    if (!graphElement.getPropertyValue(VAMPProperties.TypeProp).equals
		(VAMPConstants.TRANSCRIPTOME_MERGE_TYPE)) return false;
	}

	return true;
    }

public boolean mustInitScale() {return true;}
}
