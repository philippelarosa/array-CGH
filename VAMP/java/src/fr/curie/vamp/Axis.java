
/*
 *
 * Axis.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class Axis extends MPanel {

    protected GraphCanvas canvas;

    Axis(String name, GraphCanvas canvas, Margins margins) {
	super(name, true, false, false, margins);
	this.canvas = canvas;
    }
}
