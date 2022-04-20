
/*
 *
 * AnnotDisplayer.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;

abstract class AnnotDisplayer implements Cloneable {

    abstract public void displayAnnots(GraphCanvas canvas, AnnotAxis annotAxis,
				       Graphics2D g, GraphElement graphElement,
				       int m,
				       PrintContext pctx);

    abstract public void manageInfo(int x, int y);
    abstract void init();
}

