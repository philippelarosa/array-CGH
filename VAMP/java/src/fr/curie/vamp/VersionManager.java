
/*
 *
 * VersionManager.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class VersionManager {

    // ---------- TO BE CHANGED IN CASE OF A NEW VERSION ----------
    private static int VERSION_V = 2;
    private static int VERSION_R = 1;
    private static int VERSION_M = 9;
    private static String PATCH_STR = null;
    // ------------------------------------------------------------

    public static int getNumVersion() {
	return VERSION_NUM;
    }

    public static String getStringVersion() {
	return VERSION_STR;
    }

    public static String getPatchLevel() {
	return (PATCH_STR != null ? "Patch Level " + PATCH_STR : "");
    }

    private static int buildNumVersion(String version) {
	String s[] = version.split("\\.");
	if (s.length != 3) return 0;
	return buildNumVersion(Utils.parseInt(s[0]), 
			       Utils.parseInt(s[1]), 
			       Utils.parseInt(s[2]));
    }

    private static int buildNumVersion(int v, int r, int m) {
	return v * VERSION_V_COEF + r * VERSION_R_COEF + m * VERSION_M_COEF;
    }

    private static String buildStringVersion(int v, int r, int m) {
	return v + "." + r + "." + m;
    }

    private static String buildStringVersion(int num) {
	int v = (num / VERSION_V_COEF);
	int r = (num - v * VERSION_V_COEF) / VERSION_R_COEF;
	int m = (num - v * VERSION_V_COEF - r * VERSION_R_COEF) /
	    VERSION_M_COEF;
	return buildStringVersion(v, r, m);
    }

    // private methods
    private static int buildNumVersion() {
	return buildNumVersion(VERSION_V, VERSION_R, VERSION_M);
    }

    private static String buildStringVersion() {
	return buildStringVersion(VERSION_V, VERSION_R, VERSION_M);
    }

    // private fields
    private static final int VERSION_V_COEF = 10000;
    private static final int VERSION_R_COEF = 100;
    private static final int VERSION_M_COEF = 1;

    private static final int VERSION_NUM = buildNumVersion();
    private static final String VERSION_STR = buildStringVersion();
}

// exemple in case of dates are needed for version management
/*
  Date d = new Date();
  String s = DateFormat.getInstance().format(d);
  System.out.println("date: " + s);
  try {
  d = DateFormat.getInstance().parse(s);
  System.out.println("date2: " + d);
  } catch(java.text.ParseException e) {
  System.err.println(e);
  }
*/

