
/*
 *
 * URLOP.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;

class URLOP extends GraphElementListOperation {

    String type;
    String name;
    String urlTemplate;
    String target;
    private static File tempfile;

    public final static Property URLProp =
	Property.getProperty("URL");

    public final static Property SourceURLProp =
	Property.getProperty("SourceURL");

public String[] getSupportedInputTypes() {
	return null;
    }

public String getReturnedType() {
	return null;
    }

    URLOP(String type, String name, String urlTemplate, String target,
	  boolean on_all) {
	super(name, SHOW_MENU | (on_all ? ON_ALL_AUTO : 0));

	this.type = type;
	this.name = name;
	this.urlTemplate = urlTemplate;
	this.target = target;
    }

public boolean mayApplyP(View view, GraphPanel panel,
		      Vector graphElements, boolean autoApply) {
	//return view.getGlobalContext().getAppletContext() != null;
	return true;
    }

    static final String R_VALUE_SEP = "\\|";
    static final String VALUE_SEP = "|";

    static String append(String value, GraphElement graphElem,
			 String s) {

	if (s == null || s.length() == 0)
	    return value;

	if (value.length() == 0)
	    return s;

	String svalues[] = value.split(R_VALUE_SEP);
	for (int n = 0; n < svalues.length; n++) {
	    if (svalues[n].equals(s))
		return value;
	}
	return value + VALUE_SEP + s;
    }

    static String append(String value, GraphElement graphElem,
			 Property prop) {
	return append(value, graphElem, 
		      (String)graphElem.getPropertyValue(prop));
    }

public Vector apply(View view, GraphPanel panel,
		 Vector graphElements, TreeMap params,
		 boolean autoApply) {

	graphElements = getGraphElements(panel, graphElements, autoApply);

	if (graphElements.size() == 0)
	    return null;

	int size = graphElements.size();
	String project = "";
	String projectid = "";
	String team = "";
	String organism = "";
	String name = "";
	String url_v = "";
	String url_s = "";

	for (int n = 0; n < size; n++) {
	    GraphElement graphElem = (GraphElement)graphElements.get(n);
	    project = append(project, graphElem, VAMPProperties.ProjectProp);
	    projectid = append(projectid, graphElem, VAMPProperties.ProjectIdProp);
	    team = append(team, graphElem, VAMPProperties.TeamProp);
	    organism = append(organism, graphElem, VAMPProperties.OrganismProp);
	    name = append(name, graphElem, VAMPProperties.NameProp);
	    url_v = append(url_v, graphElem, graphElem.getURL());
	    url_s = append(url_s, graphElem, graphElem.getSourceURL());
	}

	PropertyElement elem = new PropertyElement();
	elem.setPropertyValue(VAMPProperties.ProjectProp, project);
	elem.setPropertyValue(VAMPProperties.ProjectIdProp, projectid);
	elem.setPropertyValue(VAMPProperties.TeamProp, team);
	elem.setPropertyValue(VAMPProperties.OrganismProp, organism);
	elem.setPropertyValue(VAMPProperties.NameProp, name);
	elem.setPropertyValue(URLProp, url_v);
	elem.setPropertyValue(SourceURLProp, url_s);

	SystemConfig sysCfg = (SystemConfig)view.getGlobalContext().
	    get(SystemConfig.SYSTEM_CONFIG);

	try {
	    AppletContext appletContext = view.getGlobalContext().
		getAppletContext();

	    URL url = makeURL(view.getGlobalContext(), elem);
	    if (url == null)
		return null;

	    if (appletContext != null) {
		if (target == null)
		    appletContext.showDocument(url);
		else
		    appletContext.showDocument(url, target);
	    }
        } catch (Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(view.getGlobalContext(), e.getMessage());
	    return null;
        }
	return null;
    }

    static final String TEMPLATE_PROTOCOL = "template://";

    private URL makeURL(GlobalContext globalContext, PropertyElement elem)
	throws Exception {

	if (type.equalsIgnoreCase("url"))
	    return new URL(elem.fromTemplate(urlTemplate));

	if (!type.equalsIgnoreCase("template")) {
	    InfoDialog.pop(globalContext, "Unknown tool type: " + type);
	    return null;
	}

	InputStream is = Utils.openStream(elem.fromTemplate(urlTemplate));

	/*
	int idx = urlTemplate.indexOf('?');

	String query;
	if (idx > 0)
	    query = urlTemplate.substring(idx+1, urlTemplate.length());
	else {
	    idx = urlTemplate.length();
	    query = null;
	}

	String file_path = urlTemplate.substring(TEMPLATE_PROTOCOL.length(), idx);

	//System.out.println("file=" + file_path);

	File file = new File(file_path);
	if (!Utils.checkRead(globalContext, file))
	    return null;

	FileInputStream is = new FileInputStream(file);
	*/

	byte b[] = new byte[4096];
	String contents = "";
	int n;
	while ((n = is.read(b)) >= 0)
	    contents += new String(b, 0, n);

	/*
	if (query != null) {
	    String key_values[] = query.split("\\&");
	    System.out.println("key_values => " + key_values.length);
	    for (n = 0; n < key_values.length; n++) {
		String key_value[] = key_values[n].split("=");
		contents =
		    contents.replaceAll("#" + key_value[0] + "#", key_value[1]);
	    }
	}
	*/
	TreeMap properties = elem.getProperties();
	Iterator it = properties.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    Property prop = (Property)entry.getKey();
	    contents = contents.replaceAll
		("#" + prop.getName() + "#",
		 (String)entry.getValue());

	}

	tempfile = getTempFile();

	FileOutputStream os = new FileOutputStream(tempfile);
	PrintStream ps = new PrintStream(os);
	ps.print(contents);
	ps.close();

	System.out.println("file: " + tempfile.getAbsolutePath());
	return new URL("file://" + tempfile.getAbsolutePath());
    }

    static void garbage() {
	if (tempfile != null) {
	    System.out.println("deleting tempfile '" +
			       tempfile.getAbsolutePath());
	    tempfile.delete();
	}
    }

    static File getTempFile() {
	try {
	    if (tempfile == null) {
		tempfile = File.createTempFile("vamp_", ".html");
	    }
	}
	catch(IOException e) {
	    e.printStackTrace();
	}

	return tempfile;
    }
}
