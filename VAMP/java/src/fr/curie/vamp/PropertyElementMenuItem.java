
/*
 *
 * PropertyElementMenuItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.awt.*;

abstract class PropertyElementMenuItem {

    Color fg, bg;
    Font font;

    protected String name;

    protected PropertyElementMenuItem(String name) {
	this.name = name;
    }

    protected PropertyElementMenuItem() {
	this("");
    }

    abstract void buildJPopupMenu(View view,
				  JPopupMenu popup, JMenu subMenu,
				  PropertyElement elem);

    public void setGraphics(Color fg, Color bg, Font font) {
	this.fg = fg;
	this.bg = bg;
	this.font = font;
    }

    private void updateGraphics(JMenuItem item) {
	if (fg != null)
	    item.setBackground(fg);
	if (bg != null)
	    item.setForeground(bg);
	if (font != null)
	    item.setFont(font);
    }

    protected void addItem(JPopupMenu popup, JMenu subMenu, JMenuItem item) {
	if (subMenu != null)
	    subMenu.add(item);
	else
	    popup.add(item);
	updateGraphics(item);
    }
}
