
/*
 *
 * GraphMenuListener.java
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

abstract class GraphMenuListener implements ActionListener {

    Object value;
    GraphMenuListener() {
	this.value = null;
    }

    void setValue(Object value) {
	this.value = value;
    }
}

