
/*
 *
 * PropertyChoiceType.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class PropertyChoiceType extends PropertyType {

    private String choices[];

    Component makeComponent(Object value) {
	JComboBox cb = new JComboBox(choices);
	cb.setSelectedItem(value.toString().toLowerCase());
	return cb;
    }

    PropertyChoiceType(String name, String choices[]) {
	super(name);
	this.choices = choices;
    }

    boolean checkValue(Object value) {
	if (!(value instanceof String)) return false;

	String svalue = (String)value;
	for (int n = 0; n < choices.length; n++)
	    if (svalue.equalsIgnoreCase(choices[n]))
		return true;
	return false;
    }

    String[] getChoices() {return choices;}
}
