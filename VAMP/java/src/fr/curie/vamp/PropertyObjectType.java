
/*
 *
 * PropertyObjectType.java
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

class PropertyObjectType extends PropertyType {

    static PropertyObjectType instance;

    static PropertyObjectType getInstance() {
	if (instance == null)
	    instance = new PropertyObjectType();
	return instance;
    }

    Component makeComponent(Object value) {
	return new JTextField(value.toString());
    }

    private PropertyObjectType() {
	super("object");
    }

    boolean checkValue(Object value) {
	return true;
    }
}
