
/*
 *
 * PropertyFloatNAType.java
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

class PropertyFloatNAType extends PropertyType {

    static PropertyFloatNAType instance;

    static PropertyFloatNAType getInstance() {
	if (instance == null)
	    instance = new PropertyFloatNAType();
	return instance;
    }

    Component makeComponent(Object value) {
	return new JTextField(Utils.performRound(value.toString(), Utils.DEFAULT_PRECISION));
    }

    private PropertyFloatNAType() {
	super("float_or_NA");
    }

    boolean checkValue(Object value) {
	if (value instanceof Float || value instanceof Double)
	    return true;

	if (!(value instanceof String)) return false;

	if (value.equals(VAMPProperties.NA)) return true;

	try {
	    Float.parseFloat((String)value);
	}
	catch(Exception e) {
	    return false;
	}

	return true;
    }
}
