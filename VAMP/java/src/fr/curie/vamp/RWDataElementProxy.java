
/*
 *
 * RWDataElementProxy.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.TreeMap;

public interface RWDataElementProxy extends RODataElementProxy {

    public void setPropertyValue(Property prop, Object value);

    public boolean removeProperty(Property prop);

    public void removeAllProperties();

    public void maskProperty(Property prop);

    public void setPosX(GraphElement graphElem, double pos_x);

    public void setPosY(GraphElement graphElement, double posy);

    public void setPosSize(GraphElement graphElem, double pos_sz);

    public void setIsNA(GraphElement graphElement);

    public void setIsMissing();

    public void syncProperties(int flags, GraphElement graphElement);

    public void setUserVal(int offset, int size, int value);

    // other modifiers coming...
    //public void setChr(int chr); // ??

    //public void setGnl(int gnl); // ??

    // etc.
}

