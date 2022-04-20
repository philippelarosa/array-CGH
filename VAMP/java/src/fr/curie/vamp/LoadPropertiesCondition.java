
/*
 *
 * LoadPropertiesCondition.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2008
 *
 */

package fr.curie.vamp;

import fr.curie.vamp.data.Probe;

public abstract class LoadPropertiesCondition {

    private GraphElement graphElement;

    public LoadPropertiesCondition(GraphElement graphElement) {
	this.graphElement = graphElement;
    }
    
    public GraphElement getGraphElement() {
	return graphElement;
    }

    abstract public boolean loadProperties(Probe probe);
}

