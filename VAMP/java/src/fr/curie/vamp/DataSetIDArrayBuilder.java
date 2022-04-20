
/*
 *
 * DataSetIDArrayBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class DataSetIDArrayBuilder extends GraphElementIDBuilder {

    static DataSetIDArrayBuilder instance = new DataSetIDArrayBuilder();

    static DataSetIDArrayBuilder getInstance() {
	return instance;
    }

    private DataSetIDArrayBuilder() {
	super("Array", true);
    }

    String buildID(GraphElement graphElement) {
	return (String)graphElement.getPropertyValue(VAMPProperties.NameProp);
    }
}
