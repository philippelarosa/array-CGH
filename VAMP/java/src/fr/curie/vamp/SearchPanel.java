
/*
 *
 * SearchPanel.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import javax.swing.*;
import java.awt.*;
import java.util.*;

abstract class SearchPanel extends JPanel {
    static final String HELP_MSG = "Use * as wildcard";
    static final String CHOOSE_PROP = "Choose a property";
    static Color bgColor = VAMPResources.getColor(VAMPResources.SEARCH_PANEL_BG);

    static final int PADW = 0;
    static final int PADH = 1;

    static int VOID_INDEX = 0;

    protected static String defProps[] = {"", "etc."};
    protected GraphCanvas canvas[];

    protected static boolean isIn(Vector v, String name) {
	int size = v.size();
	for (int n = 0; n < size; n++)
	    if (v.get(n).equals(name)) {
		return true;
	    }
	return false;
    }

    protected void setPanels(GraphPanel panels[]) {
	for (int n = 0; n < panels.length; n++)
	    canvas[n] = panels[n].getCanvas();
    }

    protected int addHelp(int x, int y) {
	MLLabel helpL = new MLLabel();
	helpL.setFont(new Font("SansSerif", Font.PLAIN, 10));
	helpL.setText(HELP_MSG);
	GridBagConstraints c = Utils.makeGBC(x, y);
	helpL.setBackground(bgColor);
	add(helpL, c);
	y++;
	Utils.addPadPanel(this, 0, y, PADW, PADH, bgColor);
	return 2;
    }

    protected static void setFGB(Component comp, Color bg) {
	Font font = VAMPResources.getFont(VAMPResources.SEARCH_PANEL_BUTTON_FONT);
	comp.setFont(font);
	comp.setBackground(bg);
    }

    protected static void setFGB(Component comp) {
	setFGB(comp, bgColor);
    }

    protected void updateCB(JComboBox cb, Vector v) {
	cb.removeAllItems();
	int size = v.size();
	cb.addItem(CHOOSE_PROP);
	for (int n = 0; n < size; n++)
	    cb.addItem(v.get(n));
    }

    protected String concat(TreeSet set) {
	String s = "";
	Iterator it = set.iterator();
	while (it.hasNext()) {
	    String entry = (String)it.next();
	    if (s.length() == 0)
		s = entry;
	    else
		//s += " / " + entry;
		s += "\n" + entry;
	}
	return s;
    }

    public void update() {
	if (canvas == null)
	    return;
	for (int n = 0; n < canvas.length; n++) {
	    update(n);
	}
    }

    abstract public void update(int n);
}
