
/*
 *
 * PropertyIntegerType.java
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

class PropertyIntegerType extends PropertyType {

    static PropertyIntegerType instance;

    static PropertyIntegerType getInstance() {
	if (instance == null)
	    instance = new PropertyIntegerType();
	return instance;
    }

    Component makeComponent(Object value) {
	return new JTextField(value.toString());
    }

    private PropertyIntegerType() {
	super("integer");
    }

    boolean checkValue(Object value) {
	if (value instanceof Integer || value instanceof Long)
	    return true;

	if (!(value instanceof String)) return false;

	try {
	    Long.parseLong((String)value);
	}
	catch(Exception e) {
	    return false;
	}

	return true;
    }
}
