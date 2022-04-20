
/*
 *
 * TranscriptomeClusterExportTool.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import javax.swing.*;
import java.io.*;

class TranscriptomeClusterExportTool {

    static TranscriptomeClusterExportTool instance;

    private TranscriptomeClusterExportTool() {
    }

    static TranscriptomeClusterExportTool getInstance() {
	if (instance == null)
	    instance = new TranscriptomeClusterExportTool();
	return instance;
    }

    void perform(GraphCanvas canvas, DendrogramGraphElement dendroGE) {
	JFileChooser fc = DialogUtils.getFileChooser("Export", 0, null);
	int returnVal = fc.showSaveDialog(new JFrame());

	if (returnVal != JFileChooser.APPROVE_OPTION)
	    return;

	File file = fc.getSelectedFile();
	try {
	    FileOutputStream os =  new FileOutputStream(file);
	    PrintStream ps = new PrintStream(os);
	    
	    DendrogramBinNode node =
		(DendrogramBinNode)dendroGE.getDendrogramNode();
	    Vector leaves = new Vector();
	    node.makeLeaves(leaves);
	    int sz = leaves.size();
	    boolean x_type = (node.getDendrogram().getType() == Dendrogram.X_TYPE);
	    GraphCanvas master_canvas;
	    if (x_type)
		master_canvas = XDendrogramGraphElementDisplayer.getMasterCanvas(canvas);
	    else
		master_canvas = YDendrogramGraphElementDisplayer.getMasterCanvas(canvas);
	    
	    LinkedList graphElements = master_canvas.getGraphElements();
	    
	    for (int n = 0; n < sz; n++) {
		DendrogramLeaf leaf = (DendrogramLeaf)leaves.get(n);
		String id = leaf.getDataSetID();
		if (x_type) {
		    DataElement data = XDendrogramGraphElementDisplayer.find(((DataSet)graphElements.get(0)).getData(), id);
		    ps.println(data.getID() + "\t" +
			       data.getPropertyValue(VAMPProperties.GeneSymbolProp) + "\t" +
			       data.getPropertyValue(Property.getProperty("Source")) +
			       ":" +
			       data.getPropertyValue(Property.getProperty("SourceID")) +
			       "\t" +
			       data.getPropertyValue(VAMPProperties.ChromosomeProp));
		}
		else {
		    GraphElement ge = YDendrogramGraphElementDisplayer.find(graphElements, id);
		}
	    }
	}
	catch(IOException exc) {
	    GlobalContext globalContext = canvas.getGlobalContext();
	    InfoDialog.pop(globalContext, exc.getMessage());
	}
    }
}
