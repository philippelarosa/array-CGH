
/*
 *
 * XMLElement.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import org.xml.sax.*;

class XMLElement {

    private String name;
    private String data;
    private HashMap attrMap;
    private XMLElement parent;
    private LinkedList child_list = new LinkedList();

    public XMLElement(String name, Attributes attrs) {
	this.name = name;
	makeAttrMap(attrs);
	data = "";
    }

    private void makeAttrMap(Attributes attrs) {
	attrMap = new HashMap();
	int len = (attrs == null ? 0 : attrs.getLength());
	for (int n = 0; n < len; n++)
	    setAttrValue(attrs.getQName(n), attrs.getValue(n));
    }

    public XMLElement(String elem) {
	this(elem, null);
    }

    public void pushData(String data) {
	this.data += data;
    }

    public String getAttrValue(String attrName) {
	return (String)attrMap.get(attrName.toUpperCase());
    }

    // can be used also for user data
    public void setAttrValue(String attrName, String value) {
	attrMap.put(attrName.toUpperCase(), value);
    }

    public String getName() { return name; }

    public String getData() { return data.trim(); }

    public void setParent(XMLElement parent) {
	this.parent = parent;
	this.parent.child_list.add(this);
    }

    public XMLElement getParent() { return parent; }
    public LinkedList getChildList() { return child_list; }

    private int parentCount() {
	int cnt = 0;
	XMLElement p = parent;
	while (p != null) {
	    cnt++;
	    p = p.parent;
	}
	return cnt;
    }

    private String makeIndent() {
	String indent = "";
	int cnt = parentCount();
	while (cnt-- > 0)
	    indent += "  ";
	return indent;
    }

    public String toString() {
	String indent = makeIndent();
	String s = indent + "<" + name;
	Iterator it = attrMap.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry)it.next();
	    s += " " +
		(String)entry.getKey() + "=\"" + (String)entry.getValue() +
		"\"";
	}	
	data = data.trim();
	if (data.length() == 0 && child_list.size() == 0)
	    s += "/>\n";
	else {
	    s += ">\n";
	    if (data.length() == 0)
		//		indent = "";
		;
	    else
		s += indent + data + "\n";
	    for (int n = 0; n < child_list.size(); n++)
		s += ((XMLElement)child_list.get(n)).toString();
	    s += indent + "</" + name + ">\n";
	}
	return s;
    }
}
