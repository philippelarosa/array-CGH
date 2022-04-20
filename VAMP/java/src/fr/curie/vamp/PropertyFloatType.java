
/*
 *
 * PropertyFloatType.java
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

class PropertyFloatType extends PropertyType {

    static PropertyFloatType instance;

    static PropertyFloatType getInstance() {
	if (instance == null)
	    instance = new PropertyFloatType();
	return instance;
    }

    Component makeComponent(Object value) {
	return new JTextField(Utils.performRound(value.toString(), Utils.DEFAULT_PRECISION));
    }

    private PropertyFloatType() {
	super("float");
    }

    boolean checkValue(Object value) {
	if (value instanceof Float || value instanceof Double)
	    return true;

	if (!(value instanceof String)) return false;

	try {
	    Float.parseFloat((String)value);
	}
	catch(Exception e) {
	    return false;
	}

	return true;
    }
}
