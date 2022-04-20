
/*
 *
 * MarkTrigger.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

abstract class MarkTrigger {

    abstract public double setLocation(Mark mark, boolean mergeChr,
				       double posx);
}
