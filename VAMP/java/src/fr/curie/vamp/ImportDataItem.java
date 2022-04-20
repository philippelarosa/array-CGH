
/*
 *
 * ImportDataItem.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

class ImportDataItem {

    private String label, url, type;
    private int import_mode;
    private boolean pangen;
    private String chrList[];

    public static final String PANGEN_STR = "pangen";

    ImportDataItem(String label, String url, String type, int import_mode, String chr) {
	this.label = label;
	this.url = url;
	this.type = type;
	this.import_mode = import_mode;

	pangen = isPangen(chr);

	chrList = getChrList(pangen, chr);
    }

    static boolean isPangen(String chr) {
	return chr.startsWith(PANGEN_STR);
    }

    static String[] getChrList(boolean pangen, String chr) {
	String chrList_s;
	String chrList[];

	if (pangen && chr.length() == PANGEN_STR.length()) {
	    chrList = null;
	}
	else {
	    if (pangen) {
		chrList_s = chr.substring(PANGEN_STR.length()+1);
	    }
	    else {
		chrList_s = chr;
	    }
	    
	    chrList = chrList_s.split(",");
	}

	return chrList;
    }

    String getLabel() {return label;}

    String getURL() {return url;}

    String getType() {return type;}

    int getImportMode() {return import_mode;}

    String[] getChrList() {return chrList;}

    boolean isPangen() {return pangen;}

    public String toString() {return label;}
}
