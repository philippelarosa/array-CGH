
/*
 *
 * XMLElementStack.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;
import org.xml.sax.*;

class XMLElementStack {

    String name;
    Stack stack;

    public XMLElementStack(String name) {
	this.name = name;
	stack = new Stack();
    }

    public void push(XMLElement elem) throws SAXException {
	if (stack.size() > 0)
	    elem.setParent(peek());

	stack.push(elem);
    }

    public XMLElement pop() throws SAXException {
	if (stack.size() == 0)
	    throw new SAXException(name + ": missing top XML element");
	/*
	if (stack.size() == 1)
	    System.out.println(((XMLElement)stack.peek()).toString());
	*/
	return (XMLElement)stack.pop();
    }

    public XMLElement peek() throws SAXException {
	if (stack.size() == 0)
	    throw new SAXException(name + ": missing top XML element");
	return (XMLElement)stack.peek();
    }
}
