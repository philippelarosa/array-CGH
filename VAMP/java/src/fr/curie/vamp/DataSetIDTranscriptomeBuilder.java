
/*
 *
 * DataSetIDTranscriptomeBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class DataSetIDTranscriptomeBuilder extends GraphElementIDBuilder {

    static DataSetIDTranscriptomeBuilder instance = new DataSetIDTranscriptomeBuilder();

    static DataSetIDTranscriptomeBuilder getInstance() {
	return instance;
    }

    private DataSetIDTranscriptomeBuilder() {
	super("Transcriptome", false);
    }

    String buildID(GraphElement graphElement) {
	if (VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_MERGE_TYPE))
	    return "T" +
		VAMPUtils.getChr(graphElement) +
		" " +
		//graphElement.getPropertyValue(VAMPConstants.ArrayRefNameProp);
		graphElement.getPropertyValue(VAMPProperties.NameProp);
	
	// 15/02/05
	//if (VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE))
	if (VAMPUtils.isMergeChr(graphElement))
	    return "T " +
		graphElement.getPropertyValue(VAMPProperties.NameProp);

	if (VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_REL_TYPE))
	    return "TRel " +
		VAMPUtils.getChr(graphElement) +
		" " +
		//graphElement.getPropertyValue(VAMPConstants.ArrayRefNameProp);
		graphElement.getPropertyValue(VAMPProperties.NameProp);

	if (VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE)) {
	    String prefix;
	    if (graphElement.getPropertyValue(VAMPProperties.ReferenceProp) != null)
		prefix = "TRef";
	    else
		prefix = "TAv";
	    return prefix + " " +
		VAMPUtils.getChr(graphElement) +
		" [" + graphElement.getPropertyValue(VAMPProperties.ArrayCountProp) + "]";
	}

	return "??";
    }
}
