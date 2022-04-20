
/*
 *
 * PanelLinks.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;

class PanelLinks {
    
    String name;
    int sync_mode;
    private int ind[];

    PanelLinks(String name, int sync_mode, int ind[]) {
	this.name = name;
	this.sync_mode = sync_mode;
	this.ind = ind;
    }

    int getSyncMode() {return sync_mode;}
    int[] getInd() {return ind;}

    void setLinkedPanes(GraphPanel panels[]) {
	for (int n = 1; n < ind.length; n++)
	    panels[ind[n]].addLinkedPane(panels[ind[n-1]], sync_mode);

	if (ind.length > 1)
	    panels[ind[0]].addLinkedPane(panels[ind[ind.length-1]], sync_mode);
    }

    String getName() {return name;}
}

