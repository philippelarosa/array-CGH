
/*
 *
 * PropertyElementMenuItemURL.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.awt.*;
import java.applet.*;

class PropertyElementMenuItemURL extends PropertyElementMenuItem {

    URLTemplate url_template;
    String target;
    GlobalContext globalContext;

    PropertyElementMenuItemURL(String name, URLTemplate url_template,
			       String target) {
	super(name);
	this.url_template = url_template;
	this.target = target;
    }

    PropertyElementMenuItemURL(String name, URLTemplate url_template) {
	this(name, url_template, null);
    }

    void buildJPopupMenu(View view,
			 JPopupMenu menu, JMenu subMenu,
			 PropertyElement elem) {
	JMenuItem item = new JMenuItem(elem.fromTemplate(name));
	addItem(menu, subMenu, item);
	this.globalContext = view.getGlobalContext();
	AppletContext appletContext = globalContext.getAppletContext();
	/*
	if (appletContext == null) {
	    item.setEnabled(false);
	    return;
	}
	*/

	item.addActionListener(new ActionListenerWrapper(elem) {
		public void actionPerformed(ActionEvent e) {
		    PropertyElement elem = (PropertyElement)getValue();
		    AppletContext appletContext = getGlobalContext().getAppletContext();
		    try {
			System.out.println("URL " + url_template.eval(elem));
			URL url = new URL(url_template.eval(elem));
			if (appletContext != null) {
			    if (target != null)
				appletContext.showDocument(url, target);
			    else
				appletContext.showDocument(url);
			}
		    }
		    catch(Exception exc) {
			System.err.println(exc);
		    }
		}
	    });
    }

    GlobalContext getGlobalContext() {return globalContext;}
}
