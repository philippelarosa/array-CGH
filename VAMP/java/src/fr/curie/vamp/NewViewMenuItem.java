
/*
 *
 * NewViewMenuItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.awt.*;
import java.util.*;

class NewViewMenuItem extends ViewMenuItem {

    private Dimension defaultDim;
    private InfoDisplayer infoDisplayer;
    private int supportFlags;
    private PanelProfile panelProfiles[];
    private PanelLayout panelLayout;
    private PanelLinks panelLinks[];
    private String viewName;
    private Menu newViewMenu, currentViewMenu;

    public static final int SUPPORT_X = 0x1;
    public static final int SUPPORT_NEW_VIEW = 0x2;
    public static final int SUPPORT_CURRENT_VIEW = 0x4;
    public static final int SUPPORT_SIMPLE_VIEW = 0x8;
    public static final int SUPPORT_DOUBLE_VIEW = 0x10;
    public static final int SUPPORT_ALL =
	SUPPORT_X |
	SUPPORT_NEW_VIEW | SUPPORT_CURRENT_VIEW |
	SUPPORT_SIMPLE_VIEW | SUPPORT_DOUBLE_VIEW;
    boolean addSeparator;

    private static Vector viewTemplates = new Vector();

    NewViewMenuItem(String defaultName,
		    String viewName,
		    PanelProfile panelProfiles[],
		    PanelLayout panelLayout,
		    PanelLinks panelLinks[],
		    Menu newViewMenu,
		    Menu currentViewMenu,
		    Dimension defaultDim,
		    InfoDisplayer infoDisplayer,
		    int supportFlags,
		    boolean addSeparator) {
	super(defaultName, supportFlags|SUPPORT_NEW_VIEW, addSeparator);
	this.viewName = viewName;
	this.newViewMenu = newViewMenu;
	this.currentViewMenu = currentViewMenu;
	this.panelProfiles = panelProfiles;
	this.panelLayout = panelLayout;
	this.panelLinks = panelLinks;
	this.defaultDim = defaultDim;
	this.infoDisplayer = infoDisplayer;
    }

    boolean hasActionListener() {return false;}
    boolean hasMenuListener() {return true;}

    void actionPerformed(View view, GraphPanel panel) {
    }

    void menuSelected(View view, GraphPanel panel, JMenuItem menuItem) {
	menuItem.setEnabled(true);
    }

    PanelProfile[] getPanelProfiles() {return panelProfiles;}
    PanelLayout getPanelLayout() {return panelLayout;}
    PanelLinks[] getPanelLinks() {return panelLinks;}
    Menu getNewViewMenu() {return newViewMenu;}
    Menu getCurrentViewMenu() {return currentViewMenu;}

    Dimension getDefaultDim() {return defaultDim;}
    InfoDisplayer getInfoDisplayer() {return infoDisplayer;}
    String getViewName() {return viewName;}
}
