
/*
 *
 * MiniMapDataFactory.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

public abstract class MiniMapDataFactory {

    static final String MINI_MAP_DATA_FACTORY = "MiniMapDataFactory";

    abstract String [] getSupportedResolutions();
    abstract String getDefaultResolution();
    abstract Cytoband getData(String resolution);

    MiniMapDataFactory(GlobalContext globalContext, String organism) {
	getDataFactMap(globalContext).put(organism, this);
    }

    static void init(GlobalContext globalContext) {
	globalContext.put(MINI_MAP_DATA_FACTORY, new HashMap());
    }

    static HashMap getDataFactMap(GlobalContext globalContext) {
	return (HashMap)globalContext.get(MINI_MAP_DATA_FACTORY);
    }

    static MiniMapDataFactory getDataFact(GlobalContext globalContext,
					  String organism) {
	MiniMapDataFactory datafact =
	    (MiniMapDataFactory)getDataFactMap(globalContext).get(organism);
	if (datafact == null) {
	    SystemConfig syscfg =
		(SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);
	    if (syscfg == null)
		return null;
	    datafact = new XMLMiniMapDataFactory
		(globalContext,
		 organism,
		 syscfg.getCytobandURL(organism),
		 syscfg.getCytobandResolutions(organism),
		 syscfg.getCytobandDefaultResolution(organism));
	}
	return datafact;
    }

    public static Cytoband getCytoband(GlobalContext globalContext,
				       String organism) {
	if (organism == null) {
	    InfoDialog.pop(globalContext,
			   "Cannot get cytoband for an empty organism");
	    return null;
	}
	MiniMapDataFactory datafact = getDataFact(globalContext, organism);
	if (datafact == null) {
	    return null;
	}
	return datafact.getData(datafact.getDefaultResolution());
    }
}
