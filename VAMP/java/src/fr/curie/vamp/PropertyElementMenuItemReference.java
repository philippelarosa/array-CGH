
/*
 *
 * PropertyElementMenuItemReference.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;

class PropertyElementMenuItemReference extends PropertyElementMenuItem {

    static private final int VIEW_IND = 0;
    static private final int ELEM_IND = 1;

    PropertyElementMenuItemReference() {
	super("");
    }

    void buildJPopupMenu(View view,
			 JPopupMenu menu, JMenu subMenu,
			 PropertyElement elem) {
	if (!VAMPUtils.getType(elem).equals(VAMPConstants.TRANSCRIPTOME_TYPE) &&
	    !VAMPUtils.getType(elem).equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) &&
	    !VAMPUtils.getType(elem).equals(VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE))
	    return;
	boolean isRef = elem.getPropertyValue(VAMPProperties.ReferenceProp) != null;
	JMenuItem item = new JMenuItem(isRef ? "Unreference Array" : "Define as Reference Array");
	addItem(menu, subMenu, item);
	Object args[] = new Object[2];
	args[VIEW_IND] = view;
	args[ELEM_IND] = elem;
	item.addActionListener(new ActionListenerWrapper(args) {
		public void actionPerformed(ActionEvent e) {
		    Object args[] = (Object [])getValue();
		    View view = (View)args[VIEW_IND];
		    GraphElement graphElement = (GraphElement)args[ELEM_IND];
		    boolean isRef = graphElement.getPropertyValue(VAMPProperties.ReferenceProp)
			!= null;
		    if (isRef) {
			graphElement.removeProperty(VAMPProperties.ReferenceProp);
			//graphElement.setAxisDisplayer(Config.defaultTranscriptomeAxisDisplayer);
		    }
		    else {
			graphElement.setPropertyValue(VAMPProperties.ReferenceProp, "true");
			//graphElement.setAxisDisplayer(Config.defaultTranscriptomeReferenceAxisDisplayer);
		    }
		    view.repaint();
		}
	    });
    }
}
