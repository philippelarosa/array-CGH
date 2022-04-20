
/*
 *
 * XMLDOM.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.net.*;
import java.io.*;
import java.util.*;

class XMLDOM {

    static XMLDOM instance;

    public static XMLDOM getInstance() {
	if (instance == null)
	    instance = new XMLDOM();

	return instance;
    }

    private XMLDOM() {
    }

    Document parse(GlobalContext globalContext, String uri,
		   boolean throw_exc) {
	try {
	    String url = XMLUtils.makeURL(globalContext, uri);
	    /*
	    String url = uri;

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
	    */
	
	    InputStream is = Utils.openStream(url, throw_exc);
	    if (is == null)
		return null;
	    return parse(globalContext, is);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, e.getMessage());
	    return null;
	}
    }

    Document parse(GlobalContext globalContext, InputStream is) {

	try {
	    DocumentBuilderFactory dbf =
		DocumentBuilderFactory.newInstance();

	    dbf.setValidating(false);
	    dbf.setIgnoringComments(true);
	    dbf.setIgnoringElementContentWhitespace(true);
	    dbf.setCoalescing(false);
	    dbf.setExpandEntityReferences(true);

	    DocumentBuilder db = null;
	    db = dbf.newDocumentBuilder();
	    return db.parse(is);
	}
	catch(Exception e) {
	    e.printStackTrace();
	    InfoDialog.pop(globalContext, e.getMessage());
	    return null;
	}
    }
}
