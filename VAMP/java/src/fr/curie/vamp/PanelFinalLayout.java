
/*
 *
 * PanelFinalLayout.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;

class PanelFinalLayout extends PanelLayout {
    
    private int ind;

    PanelFinalLayout(int ind) {
	this.ind = ind;
    }

    int getInd() {return ind;}

    Component makeComponent(GraphPanel panels[]) {
	return panels[ind];
    }

    void trace(String indent) {
	System.out.println(indent + "FinalLayout [" + ind + "]");
    }
}

