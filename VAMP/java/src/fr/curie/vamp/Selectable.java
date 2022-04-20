
/*
 *
 * Selectable.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

interface Selectable extends Cloneable {

    public boolean isSelected();
    public void setSelected(boolean selected, Object container);
    Object clone() throws CloneNotSupportedException;
    Object clone_light() throws CloneNotSupportedException;
}
