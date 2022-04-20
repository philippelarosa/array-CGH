
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

abstract class ViewMenuItem extends MenuItem {

    protected String defaultName;
    protected int supportFlags;
    protected boolean addSeparator;

    public static final int SUPPORT_X = 0x1;
    public static final int SUPPORT_NEW_VIEW = 0x2;
    public static final int SUPPORT_CURRENT_VIEW = 0x4;
    public static final int SUPPORT_SIMPLE_VIEW = 0x8;
    public static final int SUPPORT_DOUBLE_VIEW = 0x10;
    public static final int SUPPORT_PROFILE = 0x20;
    public static final int SUPPORT_ALL = SUPPORT_X |
	SUPPORT_NEW_VIEW | SUPPORT_CURRENT_VIEW |
	SUPPORT_SIMPLE_VIEW | SUPPORT_DOUBLE_VIEW | SUPPORT_PROFILE;
    public static final int ALWAYS_DISABLE = 0x40;

    private static Vector viewMenuItems = new Vector();

    ViewMenuItem(String defaultName, int supportFlags, boolean addSeparator) {
	super(defaultName, addSeparator);

	this.supportFlags = supportFlags;
	this.addSeparator = addSeparator;
	this.defaultName = defaultName;

	viewMenuItems.addElement(this);
    }

    boolean hasActionListener() {return false;}
    boolean hasMenuListener() {return true;}
    abstract void menuSelected(View view, GraphPanel panel,
			       JMenuItem menuItem);
    abstract void actionPerformed(View view, GraphPanel panel);

    String getDefaultName() {return defaultName;}
    static Vector getViewMenuItems() {return viewMenuItems;}
    boolean addSeparator() {return addSeparator;}

    /*
    int getSupportFlags() {return supportFlags;}
    boolean supportX() {return (supportFlags & SUPPORT_X) != 0;}
    boolean supportNewView() {return (supportFlags & SUPPORT_NEW_VIEW) != 0;}
    boolean supportSimpleView() {return (supportFlags & SUPPORT_SIMPLE_VIEW) != 0;}
    boolean supportDoubleView() {return (supportFlags & SUPPORT_DOUBLE_VIEW) != 0;}
    */

    boolean supportCurrentView() {return (supportFlags & SUPPORT_CURRENT_VIEW) != 0;}
    boolean alwaysDisable() {return (supportFlags & ALWAYS_DISABLE) != 0;}
}
