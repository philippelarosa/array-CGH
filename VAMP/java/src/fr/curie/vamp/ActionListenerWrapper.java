
/*
 *
 * ActionListenerWrapper.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;

abstract class ActionListenerWrapper implements ActionListener {

    private Object value;

    ActionListenerWrapper(Object value) {
	this.value = value;
    }

    Object getValue() {return value;}
}

