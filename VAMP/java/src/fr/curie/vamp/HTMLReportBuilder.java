
/*
 *
 * HTMLReportBuilder.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.io.*;

class HTMLReportBuilder extends ReportBuilder {

    static final String BOLD_STYLE = "style='font-weight: bold;'";

    HTMLReportBuilder(PrintStream ps, String title) {
	super(ps, title);
    }

    void startDocument() {
	startDocument(new java.util.Date());
    }

    void startDocument(java.util.Date date) {
	ps.println("<!-- VAMP Report '" + title + "' at " + date.toString() + " -->\n");
	ps.println("<html><head><title>" + title +
		   "</title><body bgcolor=white>");
	ps.println("VAMP Report <i>" + title + "</i> at " + date.toString() + "\n");
	ps.println("<hr>");
    }

    void endDocument() {
	ps.println("</body></html>");
    }

    void addTitle1(String title) {
	ps.println("<h1>" + title + "</h1>");
    }

    void addTitle2(String title) {
	ps.println("<h2>" + title + "</h2>");
    }

    void addTitle3(String title) {
	ps.println("<h3>" + title + "</h3>");
    }

    void startCenter() {
	ps.print("<div align='center'>");
    }

    void endCenter() {
	ps.println("</div>");
    }

    void addHLine() {
	ps.println("<hr>");
    }

    void addText(String text) {
	ps.println(text);
    }

    void addText(String text, String attrs) {
	ps.println("<span " + attrs + ">" + text + "</span>");
    }

    void addVPad(int h) {
	while(h-- > 0)
	    ps.println("<br>");
    }

    void startTable() {
	startTable("border=1 cellpadding=2 cellspacing=0");
    }

    void startTable(String attrs) {
	ps.println("<table " + attrs + ">");
    }

    void startTable(String cols[]) {
	startTable(cols, "border=1 cellpadding=2 cellspacing=0");
    }

    void startTable(String cols[], String attrs) {
	startTable(attrs);
	startRow();
	for (int n = 0; n < cols.length; n++)
	    addCell(cols[n], "align=center " + BOLD_STYLE);
	endRow();
    }

    void endTable() {
	ps.println("</table>");
    }

    void startRow() {
	ps.println("<tr>");
    }

    void startRow(String attrs) {
	ps.println("<tr " + attrs + ">");
    }

    void endRow() {
	ps.println("</tr>");
    }

    void startCell() {
	ps.println("<td>");
    }

    void startCell(String attrs) {
	ps.println("<td " + attrs + ">");
    }

    void endCell() {
	ps.println("</td>");
    }

    String getUnsecableSpace() {
	return "&nbsp;";
    }

    void addEmptyCell() {
	addCell("&nbsp;");
    }

    void addCell(String text) {
	ps.println("<td>" + text + "</td>");
    }

    void addCell(String text, int colspan) {
	ps.println("<td colspan=" + colspan + ">" + text + "</td>");
    }

    void addCell(String text, String attrs) {
	if (attrs == null || attrs.length() == 0)
	    addCell(text);
	else
	    ps.println("<td " + attrs + ">" + text + "</td>");
    }

    void addCell(String text, int colspan, String attrs) {
	if (attrs == null || attrs.length() == 0)
	    addCell(text, colspan);
	else
	    ps.println("<td colspan=" + colspan + " " + attrs + ">" + text + "</td>");
    }

    String replaceNL(String value) {
	return value.replaceAll("\n", "<br>");
    }

    String replaceSP(String value) {
	return value.replaceAll(" ", "&nbsp;");
    }
}
