
/*
 *
 * ReportBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.io.*;

abstract class ReportBuilder {

    protected PrintStream ps;
    protected String title;

    protected ReportBuilder(PrintStream ps, String title) {
	this.ps = ps;
	this.title = title;
    }

    PrintStream getPrintStream() {return ps;}

    abstract void startDocument();
    abstract void startDocument(java.util.Date date);
    abstract void endDocument();

    abstract void addTitle1(String title);
    abstract void addTitle2(String title);
    abstract void addTitle3(String title);

    abstract void addText(String text);

    abstract void addText(String text, String attrs);

    abstract void addVPad(int h);
    void addVPad() {addVPad(1);}

    abstract void startCenter();
    abstract void endCenter();
    abstract void addHLine();

    abstract void startTable();
    abstract void startTable(String attrs);
    abstract void startTable(String cols[]);
    abstract void startTable(String cols[], String attrs);
    abstract void endTable();

    abstract void startRow();
    abstract void startRow(String attrs);
    abstract void endRow();

    abstract void startCell();
    abstract void startCell(String attrs);
    abstract void endCell();

    abstract void addCell(String text);
    abstract String getUnsecableSpace();
    abstract void addEmptyCell();
    abstract void addCell(String text, int colspan);
    abstract void addCell(String text, String attrs);
    abstract void addCell(String text, int colspan, String attrs);
    abstract String replaceNL(String value);
    abstract String replaceSP(String value);
}
