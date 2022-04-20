
/*
 *
 * XMLLOHFactory.java
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

class XMLLOHFactory {

    GlobalContext globalContext;
    String urlTemplate;
    XMLArrayDataFactory data_factory;
    static final String XML_LOH_FACTORY = "XMLLOHFactory";
    static Property PosBeginProp = Property.getProperty("PosBegin");
    static Property PosEndProp = Property.getProperty("PosEnd");

    static void init(GlobalContext globalContext) {
	globalContext.put(XML_LOH_FACTORY,new XMLLOHFactory(globalContext));
    }

    private XMLLOHFactory(GlobalContext globalContext) {
	this.globalContext = globalContext;
	data_factory = new XMLArrayDataFactory(globalContext, null,
					       VAMPConstants.LOH_TYPE);
    }

    static XMLLOHFactory getFactory(GlobalContext globalContext) {
	/*
	if (instance == null)
	    instance = new XMLLOHFactory(globalContext);
	return instance;
	*/
	return (XMLLOHFactory)globalContext.get(XML_LOH_FACTORY);
    }

    /*
    void setFilter(PropertyElementFilter filter) {
	data_factory.setFilter(filter);
    }
    */

    void setURLTemplate(String urlTemplate) {
	this.urlTemplate = urlTemplate;
    }

    static void postAction(GraphElement graphElement, DataElement d) {
	double pos_begin = PosBeginProp.toDouble(d);
	double pos_end = PosEndProp.toDouble(d);
	d.setPosX(graphElement, pos_begin);
	d.setPosSize(graphElement, pos_end - pos_begin);
	d.setPropertyValue(VAMPProperties.SizeProp,
			   Utils.toString((long)(pos_end - pos_begin)));
    }

    boolean buildDataSet(DataSet ref, DataSet dataSet) {
	DataElement data[] = dataSet.getData();
	if (data == null || data.length == 0)
	    return false;

	VAMPUtils.setType(dataSet, VAMPConstants.LOH_TYPE);
	dataSet.setPropertyValue(VAMPProperties.CCNameProp, VAMPConstants.CC_LOH);
	dataSet.setPropertyValue(VAMPProperties.ThresholdsNameProp, VAMPConstants.THR_LOH);
	dataSet.setPropertyValue(VAMPProperties.RatioScaleProp, VAMPConstants.RatioScale_M);

	for (int n = 0; n < data.length; n++) {
	    postAction(dataSet, data[n]);
	    /*
	    DataElement d = data[n];
	    double pos_begin = PosBeginProp.toDouble(d);
	    double pos_end = PosEndProp.toDouble(d);
	    d.setPosX(dataSet, pos_begin);
	    d.setPosSize(dataSet, pos_end - pos_begin);
	    d.setPropertyValue(VAMPConstants.SizeProp,
			       Utils.toString((long)(pos_end - pos_begin)));

	    */
	}
	return true;
    }

    LinkedList buildDataSets(Object conn, DataSet ref, boolean mute_error) {
	String url;
	if (conn == null)
	    url = ref.fromTemplate(urlTemplate);
	else
	    url = (String)conn;

 	LinkedList dataSets = data_factory.getData(url, mute_error);
	if (dataSets == null) return null;
	int size = dataSets.size();

	for (int m = 0; m < size; m++) {
	    if (!buildDataSet(ref, (DataSet)dataSets.get(m)))
		return dataSets;
	}

	return dataSets;
    }
}
