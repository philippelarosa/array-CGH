
/*
 *
 * SignalHistogramOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.awt.*;

class SignalHistogramOP extends GraphElementListOperation {
   
    static Property countProp = Property.getProperty("Count");
    static Property arrayProp = Property.getProperty("Arrays");
    static Property freqProp = Property.getProperty("Frequency");
    static final double COEF = 100000;

    static final boolean USE_FACTORY = true;

    static final String NAME = "Signal Histogram";

    public String[] getSupportedInputTypes() {
	return null;
    }

    public String getReturnedType() {
	return null;
    }

    SignalHistogramOP() {
	super(NAME, SHOW_MENU);
    }

    public boolean mayApplyP(View view, GraphPanel panel,
			     Vector graphElements, boolean autoApply) {

	int size = graphElements.size();
	if (size == 0)
	    return false;

	String otype = null;
	//int len = 0;
	for (int m = 0; m < size; m++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(m);

	    String type = VAMPUtils.getType(graphElement);

	    if (otype == null)
		otype = type;
	    else if (!otype.equals(type))
		return false;

	    /*
	      if (len == 0)
	      len = dataSet.getData().length;
	      else if (len != dataSet.getData().length)
	      return false;
	    */
	}

	return true;
    }

    public Vector apply(View view, GraphPanel panel,
			Vector graphElements, TreeMap params,
			boolean autoApply) {

	try {	
	    // 09/02/06
	    // 30/06/08 disconnected normalization
	    //graphElements = NormalizeOP.normalize(view.getGlobalContext(), graphElements);
	    int size = graphElements.size();

	    long ms0 = System.currentTimeMillis();
	    GraphElement rDataSet;
	    rDataSet = compute2(view.getGlobalContext(), graphElements, params);

	    /*
	    if (((GraphElement)graphElements.get(0)).asDataSet() != null) {
		rDataSet = compute2(view.getGlobalContext(), graphElements, params);
	    }
	    else {
		rDataSet = compute2(view.getGlobalContext(), graphElements, params);
	    }
	    */

	    long ms1 = System.currentTimeMillis();
	    System.out.println("Signal Histogram duration: " + ((ms1-ms0)/1000.) + " seconds");

	    // for testing
	    //rDataSet.cloneProperties(((DataSet)graphElements.get(0)));
	    // end of testing

	    // Notes:
	    // Color Codes (CCProp) a mettre dans le profil de retour

	    GraphPanelSet panelSet = view.getGraphPanelSet();
	    ViewFrame vf = new ViewFrame(view.getGlobalContext(),
					 view.getName(),
					 panelSet.getPanelProfiles(),
					 panelSet.getPanelLayout(),
					 panelSet.getPanelLinks(),
					 null, null,
					 new LinkedList(),
					 Config.defaultDim,
					 null);
	    LinkedList list = new LinkedList();
	    list.add(rDataSet);
	    vf.getView().getGraphPanelSet().getPanel(panel.getWhich()).setGraphElements(list);
	    if (task != null)
		task.performBeforeOPFrameVisible();
	    vf.setVisible(true);
	    vf.getView().syncGraphElements();
	    return graphElements;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static class CompPos implements Comparator {


	static class Item {
	    GraphElement graphElement;
	    DataElement d;
	    Item(GraphElement graphElement, DataElement d) {
		this.graphElement = graphElement;
		this.d = d;
	    }
	}

	public int compare(Object o1, Object o2) {
	    Item i1 = (Item)o1;
	    Item i2 = (Item)o2;

	    if (i1.d == i2.d)
		return 0;

	    if (i1.d.getPosY(i1.graphElement) < i2.d.getPosY(i2.graphElement))
		return -1;

	    if (i2.d.getPosY(i2.graphElement) < i1.d.getPosY(i1.graphElement))
		return 1;

	    if (i1.d.getInd() < i2.d.getInd())
		return -1;

	    if (i2.d.getInd() < i1.d.getInd())
		return 1;

	    int r = ((String)(i1.d.getPropertyValue(VAMPProperties.ArrayProp))).
		compareTo((String)i2.d.getPropertyValue(VAMPProperties.ArrayProp));

	    if (r < 0 || r > 0)
		return r;

	    System.out.println("OUPS " + i1.d.getID() + ", " +
			       i2.d.getID() + " : " +
			       i1.d + " vs. " + i2.d + " " +
			       i1.d.getPropertyValue(VAMPProperties.ArrayProp) + " " +
			       i2.d.getPropertyValue(VAMPProperties.ArrayProp));
	    return 0;
	}
    }

    GraphElement compute(Vector graphElements, TreeMap params) {

	TreeSet pos_set = new TreeSet(new CompPos());

	double min_value = Double.MAX_VALUE;
	double max_value = - Double.MAX_VALUE;
	
	int sz = 0;
	int size = graphElements.size();
	for (int m = 0; m < size; m++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(m);
	    DataSet dataSet = graphElement.asDataSet();
	    DataElement data[] = dataSet.getData();
	    for (int n = 0; n < data.length; n++) {
		DataElement d = data[n];

		if (VAMPUtils.isNA(d)) {
		    continue;
		}

		pos_set.add(new CompPos.Item(graphElement, d));
		if (d.getPosY(graphElement) < min_value)
		    min_value = d.getPosY(graphElement);
		if (d.getPosY(graphElement) > max_value)
		    max_value = d.getPosY(graphElement);
		sz++;
	    }
	}

	int total_size = pos_set.size();

	Object pos[] = pos_set.toArray();
	CompPos.Item c10 = (CompPos.Item)pos[pos_set.size()/10];
	CompPos.Item c90 = (CompPos.Item)pos[(9*pos_set.size())/10];
	double Y10 = c10.d.getPosY(c10.graphElement);
	double Y90 = c90.d.getPosY(c90.graphElement);
	final double step = ((Y90 - Y10) / ((DataSet)graphElements.get(0)).getData().length) * 40;

	//System.out.println("step: " + step);
	Vector v = new Vector();
	double lastY = min_value;

	int step_cnt = 0;

	int total_cnt = 0;
	DataSet rDataSet = new DataSet();
	for (int n = 0; n < pos.length; n++) {
	    CompPos.Item item = (CompPos.Item)pos[n];
	    DataElement d = item.d;
	    GraphElement graphElement = item.graphElement;
	    if (d.getPosY(item.graphElement) - lastY > step || n == pos.length-1) {

		if (n == pos.length-1 && d.getPosY(graphElement) - lastY <= step)
		    step_cnt++;

		total_cnt += step_cnt;
		v.add(set(rDataSet, lastY + step/2, step_cnt, step, min_value, total_size));
		/*
		  System.out.println("lastY: " + (lastY));
		  System.out.println("lastY + step: " + (lastY + step));
		  System.out.println("d.getPosY(): " + d.getPosY());
		*/

		for (double y = lastY + step; y < d.getPosY(graphElement) - step;
		     y += step)
		    v.add(set(rDataSet, y, 0, step, min_value, total_size));

		if (n == pos.length-1 && d.getPosY(graphElement) - lastY > step) {
		    total_cnt++;
		    v.add(set(rDataSet, d.getPosY(graphElement), 1, step, min_value, total_size));
		}

		if (n == pos.length-1)
		    break;

		lastY = d.getPosY(graphElement);
		step_cnt = 0;
	    }

	    step_cnt++;
	}

	//System.out.println("total_cnt: " + total_cnt);
	//System.out.println("total_size: " + total_size);
	DataElement rData[] = new DataElement[v.size()];

	for (int n = 0; n < rData.length; n++)
	    rData[n] = (DataElement)v.get(n);

	/*
	  for (int n = 0; n < rData.length; n++)
	  System.out.println(((rData[n].getPosX()/COEF)+min_value) + " -> " + rData[n].getPosY());
	*/

	rDataSet.setData(rData);
	
	rDataSet.setGraphElementDisplayer(new SignalHistogramDataSetDisplayer());
	rDataSet.setAxisDisplayer(new GenomicPositionAxisDisplayer
				  ("", 0.01, 0.001, false, DataSetIDArrayBuilder.getInstance()));
							    
	rDataSet.setPropertyValue(arrayProp, makeNames(graphElements));
	rDataSet.setPropertyValue(VAMPProperties.ArraysRefProp, graphElements);
	rDataSet.setPropertyValue(VAMPProperties.CCNameProp, ((DataSet)graphElements.get(0)).getPropertyValue(VAMPProperties.CCNameProp));
	Object o = ((DataSet)graphElements.get(0)).getPropertyValue(VAMPProperties.RatioScaleProp);
	if (o != null)
	    rDataSet.setPropertyValue(VAMPProperties.RatioScaleProp, o);
	else {
	    o = ((DataSet)graphElements.get(0)).getPropertyValue(VAMPProperties.SignalScaleProp);
	    if (o != null)
		rDataSet.setPropertyValue(VAMPProperties.SignalScaleProp, o);
	}

	rDataSet.setPropertyValue(VAMPProperties.AffineTransformProp,
				  new double[]{-min_value, COEF});
	VAMPUtils.setType(rDataSet, VAMPConstants.SIGNAL_DENSITY_TYPE);

	return rDataSet;
    }

    static final String sep = "------------\n";

    static String makeNames(Vector v) {
	int size = v.size();
	
	String names = sep;
	for (int n = 0; n < size; n++) {
	    if (n != 0)
		names += "\n";
	    names += ((GraphElement)v.get(n)).getID();
	}

	return names + "\n" + sep;
    }

    DataElement set(GraphElement graphElement, double y, int step_cnt, double step, double min_value, int total_size) {
	DataElement d = new DataElement();
	d.declare(graphElement);
	VAMPUtils.setType(d, VAMPConstants.SIGNAL_DENSITY_ITEM_TYPE);
	d.setPosX(graphElement, (y - min_value) * COEF);
	d.setPropertyValue(VAMPProperties.RatioProp,
			   Utils.toString(y));
	d.setPosY(graphElement, (double)step_cnt/total_size);
	d.setPropertyValue(Property.getProperty("Signal "),
			   Utils.toString(d.getPosY(graphElement)));
	d.setPropertyValue(freqProp, step_cnt + "/" + total_size);

	//System.out.println("setting " + ((d.getPosX()/COEF)+min_value) + " -> " + d.getPosY());
	return d;
    }

    RWDataElementProxy set(GraphElementFactory factory, GraphElement graphElement, double y, int step_cnt, double step, double min_value, int total_size) {
	//DataElement d = new DataElement();
	RWDataElementProxy d = factory.makeRWDataElementProxy();
	d.declare(graphElement);
	d.setPropertyValue(VAMPProperties.TypeProp, VAMPConstants.SIGNAL_DENSITY_ITEM_TYPE);
	d.setPosX(graphElement, (y - min_value) * COEF);
	d.setPropertyValue(VAMPProperties.RatioProp,
			   Utils.toString(y));
	d.setPosY(graphElement, (double)step_cnt/total_size);
	d.setPropertyValue(Property.getProperty("Signal "),
			   Utils.toString(d.getPosY(graphElement)));
	d.setPropertyValue(freqProp, step_cnt + "/" + total_size);

	//System.out.println("setting " + ((d.getPosX()/COEF)+min_value) + " -> " + d.getPosY());
	return d;
    }

    static class CompPos2 implements Comparator {


	static class Item {
	    GraphElement graphElement;
	    RODataElementProxy d;
	    int ind;
	    Item(GraphElement graphElement, RODataElementProxy d, int ind) {
		this.graphElement = graphElement;
		this.d = d;
		this.ind = ind;
	    }
	}

	public int compare(Object o1, Object o2) {
	    Item i1 = (Item)o1;
	    Item i2 = (Item)o2;

	    if (i1.d == i2.d) {
		return 0;
	    }

	    if (i1.d.getPosY(i1.graphElement) < i2.d.getPosY(i2.graphElement))
		return -1;

	    if (i2.d.getPosY(i2.graphElement) < i1.d.getPosY(i1.graphElement))
		return 1;

	    if (i1.ind < i2.ind)
		return -1;

	    if (i2.ind < i1.ind)
		return 1;

	    int r = ((String)i1.graphElement.getID()).compareTo((String)i2.graphElement.getID());
	    /*
	    int r = ((String)(i1.d.getPropertyValue(VAMPProperties.ArrayProp))).
		compareTo((String)i2.d.getPropertyValue(VAMPProperties.ArrayProp));
	    */
	    
	    if (r < 0 || r > 0) {
		return r;
	    }

	    //System.out.println("OUPS");
	    return 0;
	}
    }

    GraphElement compute2(GlobalContext globalContext, Vector graphElements, TreeMap params) throws Exception {

	ToolResultContext toolResultContext = ToolResultManager.getInstance().prologue(globalContext, NAME, params, graphElements, null);

	GraphElement rGraphElement = toolResultContext.getGraphElementResult();
	if (rGraphElement != null) {
	    //System.out.println("Signal Histo: ALREADY DONE!");
	    rGraphElement.setGraphElementDisplayer(GraphElementDisplayer.get("Signal Histogram"));
	    rGraphElement.setAxisDisplayer(AxisDisplayer.get(AxisDisplayer.getName(Config.SIGNAL_HISTOGRAM_AXIS, GenomicPositionAxisDisplayer.CANONICAL_NAME)));
	    //rGraphElement.setAxisDisplayer(AxisDisplayer.get("Signal Histogram Axis"));
	    return rGraphElement;
	}

	TreeSet pos_set = new TreeSet(new CompPos2());

	double min_value = Double.MAX_VALUE;
	double max_value = - Double.MAX_VALUE;
	
	int sz = 0;
	int size = graphElements.size();
	for (int m = 0; m < size; m++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(m);
	    int length = graphElement.getProbeCount();
	    for (int n = 0; n < length; n++) {
		RODataElementProxy d = graphElement.getDataProxy(n);

		if (VAMPUtils.isNA(d)) {
		    continue;
		}

		pos_set.add(new CompPos2.Item(graphElement, d, n));
		if (d.getPosY(graphElement) < min_value)
		    min_value = d.getPosY(graphElement);
		if (d.getPosY(graphElement) > max_value)
		    max_value = d.getPosY(graphElement);
		sz++;
	    }
	}

	int total_size = pos_set.size();

	Object pos[] = pos_set.toArray();
	CompPos2.Item c10 = (CompPos2.Item)pos[pos_set.size()/10];
	CompPos2.Item c90 = (CompPos2.Item)pos[(9*pos_set.size())/10];
	double Y10 = c10.d.getPosY(c10.graphElement);
	double Y90 = c90.d.getPosY(c90.graphElement);
	final double step = ((Y90 - Y10) / ((GraphElement)graphElements.get(0)).getProbeCount()) * 40;

	//System.out.println("step: " + step);
	Vector v = new Vector();
	double lastY = min_value;

	int step_cnt = 0;

	int total_cnt = 0;
	GraphElementFactory factory = toolResultContext.getFactory();
	TreeMap properties = new TreeMap();
	properties.put(VAMPProperties.OrganismProp, ((GraphElement)(graphElements.get(0))).getPropertyValue(VAMPProperties.OrganismProp));
	factory.init("", 0, properties, false);
	rGraphElement = factory.getGraphElement();

	for (int n = 0; n < pos.length; n++) {
	    CompPos2.Item item = (CompPos2.Item)pos[n];
	    RODataElementProxy d = item.d;
	    GraphElement graphElement = item.graphElement;
	    if (d.getPosY(item.graphElement) - lastY > step || n == pos.length-1) {

		if (n == pos.length-1 && d.getPosY(graphElement) - lastY <= step)
		    step_cnt++;

		total_cnt += step_cnt;
		v.add(set(factory, rGraphElement, lastY + step/2, step_cnt, step, min_value, total_size));
		/*
		  System.out.println("lastY: " + (lastY));
		  System.out.println("lastY + step: " + (lastY + step));
		  System.out.println("d.getPosY(): " + d.getPosY());
		*/

		for (double y = lastY + step; y < d.getPosY(graphElement) - step;
		     y += step)
		    v.add(set(factory, rGraphElement, y, 0, step, min_value, total_size));

		if ( n == pos.length-1 && d.getPosY(graphElement) - lastY > step) {
		    total_cnt++;
		    v.add(set(factory, rGraphElement, d.getPosY(graphElement), 1, step, min_value, total_size));
		}

		if (n == pos.length-1)
		    break;

		lastY = d.getPosY(graphElement);
		step_cnt = 0;
	    }

	    step_cnt++;
	}

	//System.out.println("total_cnt: " + total_cnt);
	//System.out.println("total_size: " + total_size);
	factory.setProbeCount(v.size(), null);
	for (int n = 0; n < v.size(); n++) {
	    factory.write((RWDataElementProxy)v.get(n));
	}

	rGraphElement.setGraphElementDisplayer(new SignalHistogramDataSetDisplayer());
	rGraphElement.setAxisDisplayer(new GenomicPositionAxisDisplayer
				  ("", 0.01, 0.001, false, DataSetIDArrayBuilder.getInstance()));
							    
	rGraphElement.setPropertyValue(arrayProp, makeNames(graphElements));
	//rGraphElement.setPropertyValue(VAMPProperties.ArraysRefProp, graphElements);
	rGraphElement.setPropertyValue(VAMPProperties.CCNameProp, ((GraphElement)graphElements.get(0)).getPropertyValue(VAMPProperties.CCNameProp));
	Object o = ((GraphElement)graphElements.get(0)).getPropertyValue(VAMPProperties.RatioScaleProp);
	if (o != null)
	    rGraphElement.setPropertyValue(VAMPProperties.RatioScaleProp, o);
	else {
	    o = ((GraphElement)graphElements.get(0)).getPropertyValue(VAMPProperties.SignalScaleProp);
	    if (o != null)
		rGraphElement.setPropertyValue(VAMPProperties.SignalScaleProp, o);
	}

	rGraphElement.setPropertyValue(VAMPProperties.AffineTransformProp,
				  new double[]{-min_value, COEF});
	VAMPUtils.setType(rGraphElement, VAMPConstants.SIGNAL_DENSITY_TYPE);

	toolResultContext.getInfo().flags = ToolResultManager.PRIVATE_DISPLAYERS;
	toolResultContext.getInfo().grphDispName = "Signal Histogram";
	toolResultContext.getInfo().axisDispName = "Signal Histogram / Genomic Position";
	toolResultContext.getInfo().viewType = "Signal Histogram";

	rGraphElement = ToolResultManager.getInstance().epilogue(toolResultContext);
	rGraphElement.setGraphElementDisplayer(GraphElementDisplayer.get("Signal Histogram"));
	rGraphElement.setAxisDisplayer(AxisDisplayer.get(AxisDisplayer.getName(Config.SIGNAL_HISTOGRAM_AXIS, GenomicPositionAxisDisplayer.CANONICAL_NAME)));
	return rGraphElement;
    }

    public boolean supportProfiles() {
	return true;
    }

    public boolean useThread() {
	return true;
    }
}

