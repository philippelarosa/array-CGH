
/*
 *
 * PropertyTextType.java
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

class PropertyTextType extends PropertyType {

    static PropertyTextType instance;

    static PropertyTextType getInstance() {
	if (instance == null)
	    instance = new PropertyTextType();
	return instance;
    }

    Component makeComponent(Object value) {
	return new JTextArea((String)value, 5, 15);
    }

    private PropertyTextType() {
	super("text");
    }

    boolean checkValue(Object value) {
	return value instanceof String;
    }
}
