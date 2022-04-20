
/*
 *
 * ChangeListenerWrapper.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

abstract class ChangeListenerWrapper implements ChangeListener {

    private Object value;

    ChangeListenerWrapper(Object value) {
	this.value = value;
    }

    Object getValue() {return value;}
}

