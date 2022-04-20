
/*
 *
 * DataSet.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

import fr.curie.vamp.data.*;
import fr.curie.vamp.data.serial.*;
import fr.curie.vamp.gui.optim.*;

public class DataSet extends GraphElement {

    private DataElement data[];
    private DataSetPerformer last_dsperformer = null;

    /*
    private double ycoef = 1, yoffset = 0;
    private boolean autoY = false;
    private boolean autoY2 = false;
    */
    private boolean full_imported;

    static boolean SHOW_IND = false; // used for debug
    static boolean SHOW_ID = false; // used for debug

    DataSet(boolean full_imported) {
	this.data = null;
	this.full_imported = full_imported;
    }

    DataSet() {
	this(true);
    }

    DataSet(DataElement data[]) {
	full_imported = true;
	setData(data);
    }

    public boolean isFullImported() {return full_imported;}

    /*
    void setYInfo(double ycoef, double yoffset) {
	this.ycoef = ycoef;
	this.yoffset = yoffset;
    }

    double getYCoef() {
	return ycoef;
    }

    double getYOffset() {
	return yoffset;
    }

    double yTransform(double y) {
	if (ycoef == 0) return y;
	return y / ycoef + yoffset;
    }

    double yTransform_1(double y) {
	if (ycoef == 0) return y;
	return (y - yoffset) * ycoef;
    }
    */

    double vxToPosX(double vx) {
	if (data.length > 0 && vx < data[0].getVX(this))
	    return data[0].getPosX(this);

	for (int n = 0; n < data.length; n++) {
	    if (n < data.length - 1) {
		if (vx >= data[n].getVX(this) && vx < data[n+1].getVX(this))
		    return data[n].getPosX(this);
	    }
	}

	return data[data.length-1].getPosX(this);
    }

    double posxToVX(double posx) {
	return dataAtPosX(posx).getVX(this);
    }

    DataElement dataAtPosX_sz(DataElement data[], double posx) {
	for (int n = 0; n < data.length; n++) {
	    double dposx = data[n].getPosX(this);
	    if (dposx+data[n].getPosSize(this) >= posx)
		return data[n];
	}

	return data[data.length-1];
    }

    DataElement getElementByID(String id) {
	for (int n = 0; n < data.length; n++)
	    if (data[n].getID().equals(id))
		return data[n];
	return null;
    }

    DataElement dataAtPosX(DataElement data[], double posx) {
	for (int n = 0; n < data.length; n++) {
	    if (data[n].getPosX(this) >= posx)
		return data[n];
	}

	return data[data.length-1];
    }

    DataElement dataAtPosX(double posx) {
	return dataAtPosX(data, posx);
    }


    public GraphElement clone_realize(boolean full) {
	try {
	    DataSet dataSet = (DataSet)super.clone();
	    if (data == null)
		System.out.println("oups for " + getID());
	    if (full)
		dataSet.setData(new DataElement[data.length]);
	    dataSet.last_dsperformer = null;
	    for (int n = 0; n < data.length; n++) {
		if (full) {
		    dataSet.data[n] = (DataElement)data[n].clone();
		    // 27/04/06 16:45
		    dataSet.data[n].copyPos(dataSet, data[n], this);
		    //dataSet.data[n].declare(dataSet);
		}
		else
		    dataSet.data[n].copyPos(dataSet, dataSet.data[n], this);
	    }

	    if (getAxisDisplayer() != null)
		dataSet.setAxisDisplayer((AxisDisplayer)getAxisDisplayer().clone());

	    /*
	    dataSet.ycoef = ycoef;
	    dataSet.yoffset = yoffset;
	    dataSet.autoY = autoY;
	    dataSet.autoY2 = autoY2;
	    */
	    dataSet.full_imported = full_imported;

	    return dataSet;
	}
	catch(CloneNotSupportedException e) {
	    return null;
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public Object clone() {
	return clone_realize(true);
    }

    public Object clone_light() {
	return clone_realize(false);
    }

    /*
    public String toString() {
	return "DataSet[data=" + (data != null ? data.length : 0) +
	    ", vBounds=" + getVBounds() +
	    ", axisDisplayer=" + getAxisDisplayer() +
	    ", graphElementDisplayer=" + getGraphElementDisplayer() + "]";
    }
    */

    public void prePaste() {
	for (int n = 0; n < data.length; n++)
	    data[n].setCentered(false);
    }

    public SmoothingLineEngine.SmoothingInfo getSmoothingInfo() {
	return new SmoothingLineEngine.SmoothingInfo(this);
    }

    public DataElement[] getData() {return data;}

    public void setData(DataElement data[]) {
	this.data = data;
	if (data != null) {
	    for (int n = 0; n < data.length; n++) {
		if (data[n] != null) {
		    data[n].declare(this);
		    data[n].setInd(n);
		    if (SHOW_IND)
			data[n].setPropertyValue
			    (VAMPProperties.TagProp, Utils.toString(n));
		    else if (SHOW_ID)
			data[n].setPropertyValue
			    (VAMPProperties.TagProp, data[n].getID());
		}
	    }
	}
    }

    DataSetPerformer getLastDataSetPerformer() {return last_dsperformer;}

    void setLastDataSetPerformer(DataSetPerformer last_dsperformer) {
	this.last_dsperformer = last_dsperformer;
    }

    public DataSet asDataSet() {return this;}

    public int getProbeCount() {return data != null ? data.length : 0;}

    public RODataElementProxy getDataProxy(int n) throws Exception {return data[n];}

    public RODataElementProxy getDataProxy(int n, boolean load_props) throws Exception {return data[n];}

}
