
/*
 *
 * PropertyBooleanType.java
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

class PropertyBooleanType extends PropertyType {

    static PropertyBooleanType instance;

    static PropertyBooleanType getInstance() {
	if (instance == null)
	    instance = new PropertyBooleanType();
	return instance;
    }

    Component makeComponent(Object value) {
	JComboBox cb = new JComboBox(new String[]{"false", "true"});
	cb.setSelectedItem(value.toString().toLowerCase());
	return cb;
    }

    private PropertyBooleanType() {
	super("boolean");
    }

    boolean checkValue(Object value) {
	if (value instanceof Boolean) return true;

	if (!(value instanceof String)) return false;

	return ((String)value).equalsIgnoreCase("true") ||
	    ((String)value).equalsIgnoreCase("false");
    }
}
