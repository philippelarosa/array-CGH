
/*
 *
 * RODataElementProxy.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.TreeMap;

public interface RODataElementProxy {

    public static final int TYPE_PROP = 0x1;
    public static final int CHR_PROP = 0x2;
    public static final int POS_PROP = 0x4;
    public static final int SIZE_PROP = 0x8;
    public static final int GNL_PROP = 0x10;

    public double getRX(GraphCanvas canvas, GraphElement graphElement);

    public double getRX(GraphCanvas canvas, GraphElement graphElement, boolean pangen);

    public double getRY(GraphCanvas canvas, GraphElement graphElement);

    public double getRY(GraphCanvas canvas, GraphElement graphElement, boolean pangen);

    public double getRSize(GraphCanvas canvas, GraphElement graphElement);

    public double getPosX(GraphElement graphElement);

    public double getPanGenPosX(GraphElement graphElement);

    public double getPosSize(GraphElement graphElem);

    public double getPosY(GraphElement graphElement);

    public double getVY(GraphElement graphElem);

    public double getVSize(GraphElement graphElem);

    public String getSChr();

    public Object getPropertyValue(Property prop);

    public TreeMap getProperties();

    public Object getID();

    public String getSType(GraphElement graphElement);
    
    // clone methods
    public RWDataElementProxy cloneToRWProxy(boolean load_props) throws Exception;

    public RWDataElementProxy cloneToRWProxy(LoadPropertiesCondition cond) throws Exception;

    // used in MergeArrayOP : this method is useful... but I think we need to avoid to use it
    public DataElement cloneToDataElement(GraphElement profile) throws Exception;

    public void copyToPos(GraphElement graphElement, RODataElementProxy dataElement, GraphElement ographElement);

    // end of cloning methods

    public boolean isCompletable();

    public void declare(GraphElement graphElement);

    public void complete(GraphElement graphElement) throws Exception;

    public void release();

    public void release(GraphElement graphElement, int n);

    public int getUserVal(int offset, int size);

    // currently used in KaryoDataSetDisplayer: means that this RODataElementProxy (which could be a Probe) has the special state rbounds => memory is consumed
    public void setRBounds(GraphElement graphElement, double rx, double ry, double width, double height); // MUST BE NOT MODIFY THE STATE !!

    // currently used in KaryoDataSetDisplayer: actually, DOES NOT SEAMS TO MODIFY THE STATE of the RODataElementProxy (see RatioProp.setGraphics etc.) => acceptable
    public void setGraphics(Graphics2D g, GraphElement graphElement);

    public void setTempPropertyValue(Property prop, Object value) throws Exception;
    // assert prop.isTemporary() or throw Exception !
}
