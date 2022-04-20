
/*
 *
 * MoveMarkVMOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.io.*;

class MoveMarkVMOP extends VMOP {

    static MoveMarkVMOP instance;

    private MoveMarkVMOP() {
	super("Move landmark", "mvlandmark");
    }

    public static MoveMarkVMOP getInstance() {
	if (instance == null)
	    instance = new MoveMarkVMOP();
	return instance;
    }

    public void writeStatement(PrintStream ps, VMStatement statement) {
    }

    public VMStatement readStatement(InputStream is) {
	return null;
    }
}
