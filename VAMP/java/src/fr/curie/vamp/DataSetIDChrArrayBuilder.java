
/*
 *
 * DataSetIDChrArrayBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class DataSetIDChrArrayBuilder extends GraphElementIDBuilder {

    static DataSetIDChrArrayBuilder instance = new DataSetIDChrArrayBuilder();

    static DataSetIDChrArrayBuilder getInstance() {
	return instance;
    }

    private DataSetIDChrArrayBuilder() {
	super("Chromosome/Array", true);
    }

    String buildID(GraphElement graphElement) {
	return "Chr " + VAMPUtils.getChr(graphElement) +
	    " / " + (String)graphElement.getID();
    }
}
