
/*
 *
 * DataSetIDChrBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class DataSetIDChrBuilder extends GraphElementIDBuilder {

    static DataSetIDChrBuilder instance = new DataSetIDChrBuilder();

    static DataSetIDChrBuilder getInstance() {
	return instance;
    }

    private DataSetIDChrBuilder() {
	super("Chromosome", true);
    }

    String buildID(GraphElement graphElement) {
	return "Chr " + VAMPUtils.getChr(graphElement);
    }
}
