
/*
 *
 * TranscriptomeAverageOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class TranscriptomeAverageOP
    extends TranscriptomeCommonOP {
   
    static final String NAME = "Transcriptome Average";

public String[] getSupportedInputTypes() {
	return new String[]{VAMPConstants.TRANSCRIPTOME_TYPE,
			    VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE};
    }

public String getReturnedType() {
	return VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE;
    }

    TranscriptomeAverageOP() {
	super(NAME, SHOW_MENU);
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {
	return VAMPUtils.isMonoChr(graphElements);
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {
	try {
	    int size = graphElements.size();
	    HashMap maps[] = new HashMap[size];
	    for (int m = 0; m < size; m++) {
		GraphElement graphElement = (GraphElement)graphElements.get(m);
		maps[m] = makeMap(graphElement);
	    }

	    HashSet wholeSet = makeSet(graphElements);

	    DataElement data[] = new DataElement[wholeSet.size()];
	    Iterator it = wholeSet.iterator();

	    DataSet dataSet = new DataSet();
	    for (int n = 0; it.hasNext(); n++) {
		String id = (String)it.next();
		data[n] = null;
		double y = 0.;
		double signal = 0.;
		double pvalue = 0.;

		int cnt = 0;
		for (int m = 0; m < size; m++) {
		    GraphElement graphElement = (GraphElement)graphElements.get(m);
		    DataElement d = getData(id, maps[m]);
		    if (d == null)
			continue;

		    if (data[n] == null) {
			data[n] = (DataElement)d.clone();
			data[n].copyPos(dataSet, d, graphElement.asDataSet());
		    }

		    cnt++;
		    y += d.getVY(graphElement);

		    signal += VAMPProperties.SignalProp.toDouble(d);
		    /*
		    if (!d.getPropertyValue(TranscriptomeFactory.PValueProp).equals(VAMPConstants.NA)) // 4/02/05
			pvalue += TranscriptomeFactory.PValueProp.toDouble(d);
		    */
		}

		data[n].setPosY(dataSet, y / cnt);
		data[n].setPropertyValue(TranscriptomeFactory.SignalProp,
					 Utils.toString(signal / size));
		/*
		data[n].setPropertyValue(TranscriptomeFactory.PValueProp,
					 Utils.toString(pvalue / size));
		*/
	    }

	    String arr_cnt = (new Integer(size)).toString();
	    dataSet.setData(data);

	    GraphElement ref = (GraphElement)graphElements.get(0);
	    String type = VAMPUtils.getType(ref);
	    //graphElement.setProperties((TreeMap)ref.getProperties().clone());

	    // 20/01/05
	    //dataSet.cloneProperties(ref.getProperties());
	    dataSet.cloneProperties(ref);
	    VAMPUtils.setType(dataSet, VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE);
	    dataSet.removeProperty(VAMPProperties.CloneCountProp);
	    dataSet.removeProperty(VAMPProperties.RatioScaleProp);
	    dataSet.setPropertyValue(VAMPProperties.ProbeSetCountProp,
				     new Integer(wholeSet.size()));
	    dataSet.setPropertyValue(VAMPProperties.ArrayCountProp, arr_cnt);
	    if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE))
		dataSet.setPropertyValue(VAMPProperties.NameProp, "T. Ref. " +
					 VAMPUtils.getChr(ref) + " " +
					 (String)ref.getPropertyValue(VAMPProperties.ArrayRefNameProp));
	    else
		dataSet.setPropertyValue(VAMPProperties.NameProp, "T. Ref. " +
					 (String)ref.getPropertyValue(VAMPProperties.ArrayRefNameProp));

	    dataSet.setPropertyValue(VAMPProperties.VectorArrayProp, graphElements);

	    // 15/02/05
	    if (type.equals(VAMPConstants.TRANSCRIPTOME_TYPE))
		dataSet.setAxisDisplayer(Config.defaultTranscriptomeAxisDisplayer);
	    else
		dataSet.setAxisDisplayer(Config.defaultTranscriptomeChrMergeAxisDisplayer);
	    //dataSet.setAxisDisplayer(Config.defaultTranscriptomeAxisDisplayer);

	    // NEW_YINFO
	    dataSet.setYInfo(0, 0);
	    dataSet.setAutoY(true);

	    Vector rGraphElements = new Vector();
	    rGraphElements.add(dataSet);
	    return undoManage(panel, rGraphElements);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
