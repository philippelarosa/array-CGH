
/*
 *
 * ColorCodes.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.awt.*;
import java.util.*;

public abstract class ColorCodes {

    private String name;
    private String codeName;
    protected GlobalContext globalContext;

    static Vector ccTemplate;

    protected ColorCodes(GlobalContext globalContext,
			 String codeName, String name) {
	this.globalContext = globalContext;
	this.codeName = codeName;
	this.name = name;
    }

    static {
	ccTemplate = new Vector();

	ccTemplate.add(new String[]{VAMPConstants.CC_CGH, VAMPConstants.CC_LOG_SUFFIX, "Log CGH"});
	ccTemplate.add(new String[]{VAMPConstants.CC_CGH, VAMPConstants.CC_LIN_SUFFIX, "Linear CGH"});

	ccTemplate.add(new String[]{VAMPConstants.CC_TRSREL, VAMPConstants.CC_LOG_SUFFIX, "Log TRSREL"});
	ccTemplate.add(new String[]{VAMPConstants.CC_TRSREL, VAMPConstants.CC_LIN_SUFFIX, "Linear TRSREL"});

	ccTemplate.add(new String[]{VAMPConstants.CC_CHIP_CHIP, VAMPConstants.CC_LOG_SUFFIX, "Log ChIp-chip"});
	ccTemplate.add(new String[]{VAMPConstants.CC_CHIP_CHIP, VAMPConstants.CC_LIN_SUFFIX, "Linear ChIP-chip"});

	ccTemplate.add(new String[]{VAMPConstants.CC_SNP, VAMPConstants.CC_LOG_SUFFIX, "Log SNP"});
	ccTemplate.add(new String[]{VAMPConstants.CC_SNP, VAMPConstants.CC_LIN_SUFFIX, "Linear SNP"});

	ccTemplate.add(new String[]{VAMPConstants.CC_ABS_TRSCLS, VAMPConstants.CC_LIN_SUFFIX, "Absolute TRSCLS"});
	ccTemplate.add(new String[]{VAMPConstants.CC_REL_TRSCLS, VAMPConstants.CC_LIN_SUFFIX, "Relative TRSCLS"});

	ccTemplate.add(new String[]{VAMPConstants.CC_GTCA, VAMPConstants.CC_LOG_SUFFIX, "Log GTCA"});
	ccTemplate.add(new String[]{VAMPConstants.CC_GTCA, VAMPConstants.CC_LIN_SUFFIX, "Linear GTCA"});
    }

    private static Hashtable colorCodesTable = new Hashtable();

    static boolean init(GlobalContext globalContext) {

	ColorCodes cc;

	int size = ccTemplate.size();
	for (int n = 0; n < size; n++) {
	    String templ[] = (String[])ccTemplate.get(n);
	    String resPrefix;
	    resPrefix = VAMPResources.getResPrefix(templ[0]);
	    
	    if (templ[1].equals(VAMPConstants.CC_LOG_SUFFIX))
		cc = new StandardColorCodes
		    (globalContext,
		     true,
		     templ[0] + templ[1],
		     templ[2],
		     Utils.log(VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YMIN)),
		     Utils.log(VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MIN)),
		     Utils.log(VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MAX)),
		     Utils.log(VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YMAX)),
		     Utils.log(VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_AMPLICON)),
		     VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
		     
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_MIN_FG),
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_NORMAL_FG),
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_MAX_FG),
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_AMPLICON_FG),
		     VAMPResources.getBool(resPrefix + VAMPResources.COLOR_CODE_CONTINUOUS_MODE));
	    else
		cc = new StandardColorCodes
		    (globalContext,
		     false,
		     templ[0] + templ[1],
		     templ[2],
		     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YMIN),
		     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MIN),
		     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YNORMAL_MAX),
		     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YMAX),
		     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_AMPLICON),
		     VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
	     
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_MIN_FG),
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_NORMAL_FG),
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_MAX_FG),
		     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_AMPLICON_FG),
		     VAMPResources.getBool(resPrefix + VAMPResources.COLOR_CODE_CONTINUOUS_MODE));

	    if (!((StandardColorCodes)cc).isOK()) return false;
	}

	String resPrefix = VAMPResources.getResPrefix(VAMPConstants.CC_LOH);
	cc = new LOHColorCodes
	    (globalContext,
	     VAMPConstants.CC_LOH + VAMPConstants.CC_LIN_SUFFIX,
	     VAMPConstants.CC_LOH,
	     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YMIN),
	     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_LOH),
	     VAMPResources.getDouble(resPrefix + VAMPResources.COLOR_CODE_YMAX),
	     VAMPResources.getInt(VAMPResources.COLOR_CODE_COUNT),
	     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_MIN_FG),
	     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_LOH_FG),
	     VAMPResources.getColor(resPrefix + VAMPResources.COLOR_CODE_MAX_FG));
	
	return true;
    }

    protected void register() {
	if (codeName.length() > 0) {
	    colorCodesTable.put(codeName, this);
	}
    }

    public String getCodeName() {return codeName;}
    public String getName() {return name;}

    public static ColorCodes get(String codeName, boolean is_log) {
	return (ColorCodes)colorCodesTable.get
	    (codeName + (is_log ? VAMPConstants.CC_LOG_SUFFIX : VAMPConstants.CC_LIN_SUFFIX));
    }

    public static Hashtable getColorCodeTable() {
	return colorCodesTable;
    }

    public abstract Color getColor(double value);
}
