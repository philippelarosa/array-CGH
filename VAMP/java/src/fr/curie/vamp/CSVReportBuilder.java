
/*
 *
 * CSVReportBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.io.*;

class CSVReportBuilder extends ReportBuilder {

    String sep;
    int cols;

    CSVReportBuilder(PrintStream ps, String title, String sep) {
	super(ps, title);
	this.sep = sep;
    }

    CSVReportBuilder(PrintStream ps, String title) {
	this(ps, title, ",");
    }

    void startDocument() {
	startDocument(new java.util.Date());
    }

    void startDocument(java.util.Date date) {
	ps.println("VAMP Report '" + title + "' at " + date.toString());
	ps.println("");
    }

    void endDocument() {
    }

    void addTitle1(String title) {
	ps.println(title);
    }

    void addTitle2(String title) {
	ps.println(title);
    }

    void addTitle3(String title) {
	ps.println(title);
    }

    void startCenter() {
    }

    void endCenter() {
    }

    void addHLine() {
	ps.print('\n');
    }

    void addText(String text) {
	ps.print(text);
    }

    void addText(String text, String attrs) {
	addText(text);
    }

    void addVPad(int h) {
	while(h-- > 0)
	    ps.print('\n');
    }

    void startTable() {
    }

    void startTable(String attrs) {
    }

    void startTable(String cols[]) {
	startRow();
	for (int n = 0; n < cols.length; n++)
	    addCell(cols[n]);
	endRow();
    }

    void startTable(String cols[], String attrs) {
	startTable(cols);
    }

    void endTable() {
	addVPad();
    }

    void startRow() {
	cols = 0;
    }

    void startRow(String attrs) {
	cols = 0;
    }

    void endRow() {
	ps.print('\n');
    }

    void startCell() {
	if (cols != 0)
	    ps.print(sep);
    }

    void startCell(String attrs) {
	if (cols != 0)
	    ps.print(sep);
    }

    void endCell() {
	cols++;
    }

    String getUnsecableSpace() {
	return " ";
    }

    void addEmptyCell() {
	addCell("");
    }

    void addCell(String text) {
	if (cols != 0)
	    ps.print(sep);
	ps.print(text);
	cols++;
    }

    void addCell(String text, int colspan) {
	addCell(text);
	while (colspan-- > 1)
	    ps.print(sep);
    }

    void addCell(String text, String attrs) {
	addCell(text);
    }

    void addCell(String text, int colspan, String attrs) {
	addCell(text, colspan);
    }

    String replaceNL(String value) {
	return value;
    }

    String replaceSP(String value) {
	return value;
    }
}
