
/*
 *
 * DendrogramIDBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class DendrogramIDBuilder extends GraphElementIDBuilder {

    static DendrogramIDBuilder instance = new DendrogramIDBuilder();

    static DendrogramIDBuilder getInstance() {
	return instance;
    }

    private DendrogramIDBuilder() {
	super("Dendrogram", false);
    }

    String buildID(GraphElement graphElement) {
	DendrogramGraphElement dendroGE =
	    graphElement.asDendrogramGraphElement();

	if (dendroGE == null || !dendroGE.isLeaf())
	    return null;

	return (String)graphElement.
	    getPropertyValue(DendrogramGraphElement.DendroIDProp);
    }
}
