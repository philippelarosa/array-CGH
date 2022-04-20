
/*
 *
 * VAMP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

public class VAMP {

    private static boolean init = false;
    private static boolean init_gbl = false;

    public static void init() {
	if (init)
	    return;

	VAMPProperties.init();
	SystemConfigOP.init();
	VAMPResources.init();

	new ExcelExportTool(ExcelExportTool.EXCEL|ExcelExportTool.STD);
	new ExcelExportTool(ExcelExportTool.EXCEL|ExcelExportTool.AVG);
	new ExcelExportTool(ExcelExportTool.CSV|ExcelExportTool.STD);

	init = true;
    }

    static void init(GlobalContext globalContext) {

	if (init_gbl)
	    return;

	OPMenu.init(globalContext);

	init_gbl = true;
    }
}
