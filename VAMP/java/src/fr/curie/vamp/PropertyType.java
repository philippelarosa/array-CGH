
/*
 *
 * PropertyType.java
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

abstract class PropertyType {

    private String name;

    protected PropertyType(String name) {
	this.name = name;
    }

    String getName() {return name;}

    abstract Component makeComponent(Object value);
    abstract boolean checkValue(Object value);
}
