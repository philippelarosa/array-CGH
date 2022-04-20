
/*
 *
 * TranscriptomeFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.sql.*;

abstract class TranscriptomeFactory {

    private Hashtable sql_ht = new Hashtable();

    static private final String DB_TRANSCRIPTOME_FACTORY =
	"DBTranscriptomeFactory";
    static private final String XML_TRANSCRIPTOME_FACTORY =
	"XMLTranscriptomeFactory";
    static private final String DEFAULT_TRANSCRIPTOME_FACTORY =
	"DefaultTranscriptomeFactory";
    protected GlobalContext globalContext;
    private Hashtable trs_ht = new Hashtable();

    static boolean init(GlobalContext globalContext) {
	/*
	globalContext.put(DB_TRANSCRIPTOME_FACTORY,
			  new DBTranscriptomeFactory(globalContext));
	*/
	globalContext.put(XML_TRANSCRIPTOME_FACTORY,
			  new XMLTranscriptomeFactory(globalContext));
	//setDefaultFactory(globalContext, getDBFactory(globalContext));
	return true;
    }

    static void setDefaultFactory(GlobalContext globalContext,
				  TranscriptomeFactory factory) {
	globalContext.put(DEFAULT_TRANSCRIPTOME_FACTORY, factory);
    }

    static void setDefaultFactory(GlobalContext globalContext,
				  String type) {
	/*
	if (type.equalsIgnoreCase("DB"))
	    setDefaultFactory(globalContext, getDBFactory(globalContext));
	    else */
	    setDefaultFactory(globalContext, getXMLFactory(globalContext));
    }

    static TranscriptomeFactory getDefaultFactory(GlobalContext globalContext) {
	return (TranscriptomeFactory)globalContext.get(DEFAULT_TRANSCRIPTOME_FACTORY);
    }

    /*
    static DBTranscriptomeFactory getDBFactory(GlobalContext globalContext) {
	return (DBTranscriptomeFactory)globalContext.get(DB_TRANSCRIPTOME_FACTORY);
    }
    */

    static XMLTranscriptomeFactory getXMLFactory(GlobalContext globalContext) {
	return (XMLTranscriptomeFactory)globalContext.get(XML_TRANSCRIPTOME_FACTORY);
    }

    TranscriptomeFactory(GlobalContext globalContext) {
	this.globalContext = globalContext;
    }
    
    static private final String SEP = "##";

    static Property ObjectIdProp = Property.getProperty("ObjectId", true);
    static Property SignalProp = VAMPProperties.SignalProp;
    static Property DetectionProp = Property.getProperty("Detection");
    static Property PValueProp = Property.getProperty("PValue");
    static Property PosBeginProp = Property.getProperty("PosBegin");
    static Property PosEndProp = Property.getProperty("PosEnd");
    static Property SourceProp = Property.getProperty("Source");
    static Property SourceIDProp = Property.getProperty("SourceID");

    private String get_key(DataSet ref) {
	return
	    ref.getID() + SEP +
	    (String)ref.getPropertyValue(VAMPProperties.NumHistoProp) + SEP +
	    (String)ref.getPropertyValue(VAMPProperties.ProjectProp) + SEP +
	    (String)ref.getPropertyValue(VAMPProperties.ChromosomeProp);
    }

    abstract LinkedList buildDataSets(Object conn, DataSet ref,
				      boolean mute_error, boolean full);

    DataSet get(DataSet ref) {
	return (DataSet)trs_ht.get(get_key(ref));
    }

    void put(DataSet ref, DataSet dset) {
	trs_ht.put(get_key(ref), dset);
    }

    abstract void setFilter(PropertyElementFilter filter);
}
