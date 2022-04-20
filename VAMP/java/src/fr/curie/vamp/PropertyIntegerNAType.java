
/*
 *
 * PropertyIntegerNAType.java
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

class PropertyIntegerNAType extends PropertyType {

    static PropertyIntegerNAType instance;

    static PropertyIntegerNAType getInstance() {
	if (instance == null)
	    instance = new PropertyIntegerNAType();
	return instance;
    }

    Component makeComponent(Object value) {
	return new JTextField(value.toString());
    }

    private PropertyIntegerNAType() {
	super("integer_or_NA");
    }

    boolean checkValue(Object value) {
	if (value instanceof Integer || value instanceof Long)
	    return true;

	if (!(value instanceof String)) return false;

	if (value.equals(VAMPProperties.NA)) return true;

	try {
	    Long.parseLong((String)value);
	}
	catch(Exception e) {
	    return false;
	}

	return true;
    }
}
