
/*
 *
 * XMLTranscriptomeFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.net.*;
import java.io.*;
import java.awt.*;
import java.util.*;

class XMLTranscriptomeFactory extends TranscriptomeFactory {

    String urlTemplate;
    XMLArrayDataFactory data_factory;
    SystemConfig sysCfg;

    XMLTranscriptomeFactory(GlobalContext globalContext) {
	super(globalContext);
	data_factory = new XMLArrayDataFactory(globalContext, null,
					       VAMPConstants.TRANSCRIPTOME_TYPE);
    }

    void setFilter(PropertyElementFilter filter) {
	data_factory.setFilter(filter);
    }

    void setURLTemplate(String urlTemplate) {
	this.urlTemplate = urlTemplate;
    }

    static void postAction(GraphElement graphElement, DataElement d) {
	double pos_begin = PosBeginProp.toDouble(d);
	double pos_end = PosEndProp.toDouble(d);
	d.setPosX(graphElement, pos_begin);

	//d.setPosY(SignalProp.toDouble(d));
	String s = (String)d.getPropertyValue(SignalProp);
	if (s.equals(VAMPProperties.NA))
	    d.setPosY(graphElement, 0.);
	else
	    d.setPosY(graphElement, Utils.parseDouble(s));

	d.setPosSize(graphElement, pos_end - pos_begin);
	d.setPropertyValue(VAMPProperties.SizeProp,
			   Utils.toString((long)(pos_end - pos_begin)));

    }

    void buildDataSet(DataSet ref, DataSet graphElement) {
	DataElement data[] = graphElement.getData();

	// disconnected 13/04/05
	//VAMPUtils.setType(graphElement, VAMPConstants.TRANSCRIPTOME_TYPE);

	if (VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_CLUSTER_TYPE)) {
	    /*
	    System.out.println("CCNAME: " + graphElement.getID() + " : " +
			       graphElement.getPropertyValue(VAMPConstants.CCNameProp));
	    */
	    //graphElement.setPropertyValue(VAMPConstants.CCNameProp, VAMPConstants.CC_ABS_TRSCLS, false);
	}

	//graphElement.setPropertyValue(VAMPConstants.SignalScaleProp, VAMPConstants.SignalScale_M);
	if (graphElement.getPropertyValue(VAMPProperties.SignalScaleProp) == null &&
	    graphElement.getPropertyValue(VAMPProperties.RatioScaleProp) != null) {
	    graphElement.setPropertyValue(VAMPProperties.SignalScaleProp,
					  graphElement.getPropertyValue
					  (VAMPProperties.RatioScaleProp));
	    graphElement.removeProperty(VAMPProperties.RatioScaleProp);
	}

	graphElement.setPropertyValue(VAMPProperties.ThresholdsNameProp, VAMPConstants.THR_TRS);
	// 2/12/04: adding organism in transcriptome
	if (ref != null) {
	    graphElement.setPropertyValue(VAMPProperties.OrganismProp,
					  ref.getPropertyValue(VAMPProperties.OrganismProp));
	    // added 7/02/05
	    /*
	    System.out.println("factory : arrayRef of " +
			       graphElement.getID() + ":" +
			       VAMPUtils.getChr(graphElement) + " is " +
			       ref.getID() + ":" +
			       VAMPUtils.getChr(ref));
	    */
	    graphElement.setPropertyValue(VAMPProperties.ArrayRefProp, ref);
	}

	// NEW_YINFO
	graphElement.setAutoY(true);

	if (VAMPUtils.isMergeChr(graphElement))
	    VAMPUtils.setType(graphElement, VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE);

	for (int n = 0; n < data.length; n++) {
	    /*
	    DataElement d = data[n];
	    double pos_begin = PosBeginProp.toDouble(d);
	    double pos_end = PosEndProp.toDouble(d);
	    d.setPosX(pos_begin);

	    //d.setPosY(SignalProp.toDouble(d));
	    String s = (String)d.getPropertyValue(SignalProp);
	    if (s.equals(VAMPConstants.NA))
		d.setPosY(0.);
	    else
		d.setPosY(Utils.parseDouble(s));

	    d.setPosSize(pos_end - pos_begin);
	    d.setPropertyValue(VAMPConstants.SizeProp,
			       Utils.toString((long)(pos_end - pos_begin)));

	    */
	    postAction(graphElement, data[n]);
	}
    }

    LinkedList buildDataSets(Object conn, DataSet ref,
			     boolean mute_error, boolean full) {
	String url;
	if (conn == null)
	    url = ref.fromTemplate(urlTemplate);
	else
	    url = (String)conn;

	LinkedList dataSets = data_factory.getData(url, false, full, mute_error);
	if (dataSets == null) return null;
	int size = dataSets.size();

	for (int m = 0; m < size; m++)
	    buildDataSet(ref, (DataSet)dataSets.get(m));

	return dataSets;
    }
}
