
/*
 *
 * PropertyStringType.java
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

class PropertyStringType extends PropertyType {

    static PropertyStringType instance;

    static PropertyStringType getInstance() {
	if (instance == null)
	    instance = new PropertyStringType();
	return instance;
    }

    Component makeComponent(Object value) {
	return new JTextField((String)value);
    }

    private PropertyStringType() {
	super("string");
    }

    boolean checkValue(Object value) {
	return value instanceof String;
    }
}
