
/*
 *
 * XMLUtils.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.awt.*;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

class XMLUtils {

    static String toString(int n) {
	//return (new Integer(n)).toString();
	return Integer.toString(n);
    }

    static String RGBtoString(int rgb) {
	return Integer.toHexString(rgb & 0xffffff);
    }

    static String toString(long n) {
	return Long.toString(n);
    }

    static String toString(double d) {
	return Double.toString(d);
    }

    static String toString(boolean b) {
	return b ? "true" : "false";
    }

    static void printOpenTag(PrintStream ps, String tag) {
	printOpenTag(ps, tag, true, null);
    }

    static void printOpenTag(PrintStream ps, String tag, String extra) {
	printOpenTag(ps, tag, true, extra);
    }

    static void printCloseTag(PrintStream ps, String tag) {
	printCloseTag(ps, tag, true);
    }

    static void printOpenTag(PrintStream ps, String tag,
			     boolean nl) {
	printOpenTag(ps, tag, nl, null);
    }

    static void printOpenTag(PrintStream ps, String tag,
			     boolean nl, String extra) {
	ps.print("<" + tag + (extra != null ? extra : "") + ">");
	if (nl)
	    ps.print('\n');
    }

    static void printCloseTag(PrintStream ps, String tag,
			      boolean nl) {
	ps.print("</" + tag + ">");
	if (nl)
	    ps.print('\n');
    }

    static void printTag(PrintStream ps, String tag, String content) {
	printOpenTag(ps, tag, false);
	ps.print(content);
	printCloseTag(ps, tag, true);
    }

    static void printHeader(PrintStream ps) {
	ps.println("<?xml version='1.0' encoding='iso-8859-1'?>");
    }

    static void printComment(PrintStream ps, File file) {
	ps.println("<!-- VAMP Saved File " + file.getAbsolutePath() +
		   " generated at " + (new Date()).toString() + " -->");
    }

    private static boolean isOptional(String aName) {
	return aName.charAt(aName.length()-1) == '?';
    }

    static String[] getAttrValues(String appli, String tag, Attributes attrs,
				  String attrNames[])
	throws SAXException {
	String attrValues[] = new String[attrNames.length];
		
	int len = (attrs == null ? 0 : attrs.getLength());
	for (int n = 0; n < len; n++) {
	    String aName = attrs.getQName(n);
	    boolean found = false;
	    for (int j = 0; j < attrNames.length; j++) {
		String s = attrNames[j];
		if (isOptional(s))
		    s = s.substring(0, s.length()-1);
		if (aName.equals(s)) {
		    if (aName.equalsIgnoreCase(aName)) {
			attrValues[j] = attrs.getValue(n);
		    }
		    found = true;
		    break;
		}
	    }

	    if (!found)
		throw new SAXException(appli + ": tag " + tag +
				       ", attribute " + aName +
				       " not expected");
	}

	String missing = null;
	for (int j = 0; j < attrNames.length; j++)
	    if (attrValues[j] == null) {
		if (isOptional(attrNames[j])) continue;
		if (missing == null)
		    missing = "missing attribute(s) ";
		else
		    missing += ", ";
		missing += attrNames[j];
	    }
	    
	if (missing != null)
	    throw new SAXException(appli + ": tag " + tag + ", " + missing);

	return attrValues;
    }

    private static String importDataBaseURL;
    private static String importDataRedirectURL;

    static String getImportDataBaseURL(GlobalContext globalContext) {
	if (importDataBaseURL == null) {
	    SystemConfig systemConfig =
		(SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);
	    if (systemConfig == null)
		return null;

	    importDataBaseURL =
		systemConfig.getParameter("importData:baseURL");
	}
	return importDataBaseURL;
    }

    static String getImportDataRedirectURL(GlobalContext globalContext) {
	if (importDataRedirectURL == null) {
	    SystemConfig systemConfig =
		(SystemConfig)globalContext.get(SystemConfig.SYSTEM_CONFIG);
	    if (systemConfig == null)
		return null;
	    importDataRedirectURL =
		systemConfig.getParameter("importData:redirectURL");
	}
	return importDataRedirectURL;
    }

    static String makeURL(GlobalContext globalContext, String url) {
	String importDataBaseURL = XMLUtils.getImportDataBaseURL(globalContext);

	if (url.indexOf(':') < 0)
	    url = importDataBaseURL + url;
	else {
	    String importDataRedirectURL = XMLUtils.getImportDataRedirectURL(globalContext);
	    if (importDataRedirectURL != null &&
		url.startsWith(importDataRedirectURL)) {
		url = url.replaceAll(importDataRedirectURL,
				     importDataBaseURL);
	    }
	}

	return url;
    }

    static void displayAttrs(Attributes attrs) {

	int len = (attrs == null ? 0 : attrs.getLength());

	for (int n = 0; n < len; n++) {
	    String aName = attrs.getQName(n);
	    String value = attrs.getValue(n);
	    System.out.print((n > 0 ? " " : "") + aName + "=\"" + value + "\"");
	}
    }
}
