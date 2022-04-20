
/*
 *
 * SystemConfig.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.util.*;

public class SystemConfig {

    public static String SYSTEM_CONFIG = "SYSTEM_CONFIG";
    private GlobalContext globalContext;
    private String userDoc;
    private HashMap menuMap = new HashMap();
    private HashMap paramMap = new HashMap();
    private HashMap cytoMap = new HashMap();
    private HashMap geneSelProbeColMap = new HashMap();
    private HashMap geneSelNoArrayProbeColMap = new HashMap();
    private HashMap geneSelGeneColMap = new HashMap();
    private HashMap bkpFreqColMap = new HashMap();
    private HashMap graphElementIconMap = new HashMap();
    private Vector printPageTemplateV = new Vector();
    private Vector toolV = new Vector();
    private String transcriptomeChrMergeURLTemplate;

    private final static String SEP = "##";

    private SystemConfig(GlobalContext globalContext) {
	this.globalContext = globalContext;
    }

    public static SystemConfig build(GlobalContext globalContext, String url) {
	SystemConfig sysCfg = new SystemConfig(globalContext);
	XMLSystemConfigFactory factory = new XMLSystemConfigFactory(globalContext);
	if (factory.build(url, sysCfg)) {
	    globalContext.put(SYSTEM_CONFIG, sysCfg);
	    return sysCfg;
	}
	return null;
    }

    void setProxy(String proxyHost, String proxyPort) {
	Utils.setProxyConfiguration(proxyHost, proxyPort);
    }

    void setUserDoc(String userDoc) {
	this.userDoc = userDoc;
    }

    void setDefaultConfiguration(String defConfig) {
	try {
	    VAMPResources.setDefaultConfiguration(globalContext, defConfig);
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, e.getMessage());
        }
    }

    private static String getKey(String object, String type) {
	if (type != null && type.length() > 0)
	    return object + SEP + type;
	return object;
    }

    void addMenu(String object, String type, PropertyElementMenu menu) {
	menuMap.put(getKey(object, type), menu);
    }

    void addParameter(String key, String value) {
	paramMap.put(key, value);
    }

    void addGeneSelectionProbeColumns(String type, String columns) {
	geneSelProbeColMap.put(type, columns.split("\\|"));
    }

    void addGeneSelectionNoArrayProbeColumns(String type, String columns) {
	geneSelNoArrayProbeColMap.put(type, columns.split("\\|"));
    }

    void addGeneSelectionGeneColumns(String type, String columns) {
	geneSelGeneColMap.put(type, columns.split("\\|"));
    }

    void addBreakpointFrequencyColumns(String type, String columns) {
	bkpFreqColMap.put(type, columns.split("\\|"));
    }

    public String [] getGeneSelectionProbeColumns(String type) {
	return (String [])geneSelProbeColMap.get(type);
    }

    public String [] getGeneSelectionNoArrayProbeColumns(String type) {
	return (String [])geneSelNoArrayProbeColMap.get(type);
    }

    public String [] getGeneSelectionGeneColumns(String type) {
	return (String [])geneSelGeneColMap.get(type);
    }

    public String [] getBreakpointFrequencyColumns(String type) {
	return (String [])bkpFreqColMap.get(type);
    }

    public String getParameter(String key) {
	return (String)paramMap.get(key);
    }

    public String getUserDoc() {
	return userDoc;
    }

    public PropertyElementMenu getMenu(String object) {
	return getMenu(object, null);
    }

    public PropertyElementMenu getMenu(String object, String type) {
	PropertyElementMenu menu = (PropertyElementMenu)menuMap.get(getKey(object, type));
	if (menu != null)
	    return menu;
	if (type != null)
	    menu = getMenu(object, null);
	return menu;
    }

    public void addGraphElementIcon(String type, String url) {
	graphElementIconMap.put(type, url);
    }
    
    public String getGraphElementIcon(String type) {
	//System.out.println("type: " + type + " -> " + graphElementIconMap.get(type));
	return (String)graphElementIconMap.get(type);
    }

    public void addCytoband(String organism, String url, String resolutions,
			    String default_resolution) {
	cytoMap.put(organism,
		    new String[]{url, resolutions, default_resolution});
    }

    public void addPrintPageTemplate(String url) {
	printPageTemplateV.add(url);
    }

    public String getCytobandURL(String organism) {
	String map[] = (String[])cytoMap.get(organism);
	if (map == null) return null;
	return map[0];
    }

    public String getCytobandResolutions(String organism) {
	String map[] = (String[])cytoMap.get(organism);
	if (map == null) return null;
	return map[1];
    }

    public String getCytobandDefaultResolution(String organism) {
	String map[] = (String[])cytoMap.get(organism);
	if (map == null) return null;
	return map[2];
    }

    public Vector getPrintPageTemplates() {
	return printPageTemplateV;
    }

    class Tool {
	String type;
	String name;
	String title;
	String container;
	String url;
	String source;
	String target;

	Tool(String type, String name, String title, String container,
	     String url, String source, String target) {
	    this.type = type;
	    this.name = name;
	    this.title = title;
	    this.container = container;
	    this.url = url;
	    this.source = source;
	    this.target = target;
	}

	boolean isOnAll() {
	    return source != null && source.equalsIgnoreCase("all");
	}
    }

    public void addTool(String type, String name, String title,
			String container, String url, String source,
			String target) {
	toolV.add(new Tool(type, name, title, container, url, source, target));
    }

    public Vector getTools() {
	return toolV;
    }

    public Vector getTools(String container) {
	Vector v = new Vector();
	int sz = toolV.size();
	for (int n = 0; n < sz; n++) {
	    Tool tool = (Tool)toolV.get(n);
	    if (tool.container.equals(container))
		v.add(tool);
	}

	return v;
    }

    public void setTranscriptomeChrMergeURLTemplate(String transcriptomeChrMergeURLTemplate) {
	this.transcriptomeChrMergeURLTemplate = transcriptomeChrMergeURLTemplate;
    }

    public String getTranscriptomeChrMergeURLTemplate() {
	return transcriptomeChrMergeURLTemplate;
    }
}



