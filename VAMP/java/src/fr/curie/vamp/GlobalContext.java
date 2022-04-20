
/*
 *
 * GlobalContext.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.awt.*;

public class GlobalContext {

    private TreeMap globalMap;
    private java.applet.AppletContext appletContext;
    private String codeBase;
    private static GlobalContext lastInstance;

    public GlobalContext()
    {
	globalMap = new TreeMap();

	MiniMapDataFactory.init(this);

	lastInstance = this;
    }


    public GlobalContext(java.applet.AppletContext appletContext, String codeBase) {
	this.appletContext = appletContext;
	this.codeBase = codeBase;
	globalMap = new TreeMap();

	View.init(this);
	ViewFrame.init(this);
	ConfirmDialog.init(this);
	InfoDialog.init(this);
	MiniMapDataFactory.init(this);
	ChrSwitchDialog.init(this);
	InfoFrame.init(this);
	GenomeAlterationDialog.init(this);
	GTCorrelationAnalysisDialog.init(this);
	GTCorrelationAnalysisRedisplayDialog.init(this);
// 	GTCorrelationAnalysisReportDialog.init(this);
	KaryoAnalysisDialog.init(this);
	TCMDialog.init(this);
	GeneSelectionDialog.init(this);
	BreakpointFrequencyDialog.init(this);
	CytogenRegionDialog.init(this);
	ClusterDialog.init(this);
	DifferentialAnalysisDialog.init(this);
	DifferentialAnalysisRedisplayDialog.init(this);
	DifferentialAnalysisReportDialog.init(this);
	FrAGLDialog.init(this);
	PrintPreviewer.init(this);
	PrintExportDialog.init(this);
	ColorCodes.init(this);
	Thresholds.init(this);
	TranscriptomeFactory.init(this);
	XMLLOHFactory.init(this);
	ImportDataDialog.init(this);
	PropertyEditDialog.init(this);
	SortDialog.init(this);
	FilterDialog.init(this);
	AnnotDisplayDialog.init(this);
	AboutDialog.init(this);

	lastInstance = this;
    }

    public void put(String name, Object value) {
	globalMap.put(name, value);
    }

    public Object get(String name) {
	return globalMap.get(name);
    }

    java.applet.AppletContext getAppletContext() {
	return appletContext;
    }

    String getCodeBase() {
	return codeBase;
    }

    static GlobalContext getLastInstance() {
	return lastInstance;
    }
}
