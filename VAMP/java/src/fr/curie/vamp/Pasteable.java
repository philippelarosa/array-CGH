
/*
 *
 * Pasteable.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

interface Pasteable extends Selectable {

    static final int COPY = 1;
    static final int CUT = 2;
    static final int DRAG = 3;
    static final int REMOVE = 4;

    void postClone();
    void prePaste();
    boolean isPasteable(int action);
}
