
/*
 *
 * VAMPApplib.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class VAMPApplib {

    static void loadPlugins(SystemConfig sysCfg, GlobalContext globalContext) {
	String plugins = sysCfg.getParameter("plugins");
	if (plugins == null)
	    return;

	    String pl[] = plugins.split(":");
	    for (int n = 0; n < pl.length; n++) {
		try {
		    System.out.println("Loading Plugin " + pl[n]);
		    Class cls = Class.forName(pl[n]);
		    Object o = cls.newInstance();
		}
		catch(Exception e) {
		    e.printStackTrace();
		    InfoDialog.pop(globalContext, "VAMP: cannot load plugin " + pl[n] + "\nPlease, check the syscfg.xml file and your CLASSPATH");
		}
	    }
    }

    static LinkedList makeGraphElements(GlobalContext globalContext,
					View view, GraphPanel panel,
					String type, String args[],
					int offset) {
	return makeGraphElements(globalContext, view, panel, type, args, offset, args.length);
    }

    static LinkedList makeGraphElements(GlobalContext globalContext,
					View view, GraphPanel panel,
					String type, String args[],
					int offset, int max) {

	LinkedList graphElements = new LinkedList();
	for (int n = offset; n < max; n++) {
	    LinkedList _graphElements = new LinkedList();
	    String url = args[n];
	    if (url.trim().length() == 0)
		continue;

	    LinkedList list = ImportData.importData(globalContext,
						    view, panel, type, url, ImportData.XML_IMPORT, null, null, true, false, true);
	    if (list != null)
		graphElements.addAll(list);

	}

	return graphElements;
    }

    // BWC means backward compatibility
    static GraphElementIDBuilder getIDBuilder(String type) {

	if (type.equals("CGH_CHR"))
	    return Config.dataSetIDChrBuilder;

	if (type.equals("CGH_CHRMERGE"))
	    return Config.dataSetIDArrayBuilder;

	if (type.equals("SNP"))
	    return Config.dataSetIDChrBuilder;

	if (type.equals("SNP_CHRMERGE"))
	    return Config.dataSetIDArrayBuilder;

	if (type.equals("TRS_CHR") || type.equals("TRS_CHRMERGE"))
	    return DataSetIDTranscriptomeBuilder.getInstance();

	// 28/01/05: should add support for chip and loh

	// backward compatibility
	if (type.equals("CHR"))
	    return Config.dataSetIDChrBuilder;

	if (type.equals("ARRAY") || type.equals("CHR_ARRAY"))
	    return Config.dataSetIDArrayBuilder;

	return null;
    }

    static String getSupportedTypes() {
	return "CGH_CHR|CGH_CHRMERGE|TRS_CHR|TRS_CHRMERGE";
    }
}

