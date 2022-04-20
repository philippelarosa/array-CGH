
/*
 *
 * URLTemplate.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

abstract class URLTemplate {

    GlobalContext globalContext;

    URLTemplate(GlobalContext globalContext) {
	this.globalContext = globalContext;
    }

    abstract public String eval(PropertyElement elem);
}
