
/*
 *
 * MenuListenerWrapper.java
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

abstract class MenuListenerWrapper implements MenuListener {

    private Object value;

    MenuListenerWrapper(Object value) {
	this.value = value;
    }

    Object getValue() {return value;}
}

