
/*
 *
 * PanelLayout.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;

abstract class PanelLayout {

    abstract Component makeComponent(GraphPanel panels[]);

    void trace() {
	trace("");
    }

    abstract void trace(String indent);
}

