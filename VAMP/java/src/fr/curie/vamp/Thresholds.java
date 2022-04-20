
/*
 *
 * Thresholds.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2004
 *
 */

package fr.curie.vamp;

import java.util.*;

class Thresholds {

    private boolean global;
    private String canname;
    private String name;
    private boolean is_log;
    private double min, max;
    private int flags;
    private final static int HIDDEN = 0x1;

    private static String suffix(boolean is_log) {
	return is_log ? " Log" : " Lin";
    }

    public Thresholds(boolean global, String name, boolean is_log,
		      double min, double max, int flags) {
	this.global = global;
	this.is_log = is_log;
	this.canname = name;
	this.name = name + suffix(is_log);
	this.min = min;
	this.max = max;
	this.flags = flags;
	register();
    }

    public Thresholds(boolean global, String name, boolean is_log,
		      double min, double max) {
	this(global, name, is_log, min, max, 0);
    }

    public static void init(GlobalContext globalContext) {
	// CGH
	double cgh_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CGH_ARRAY_MINY);
	double cgh_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CGH_ARRAY_MAXY);

	double cgh_log_min =
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CGH_ARRAY_LOG_MINY);
	double cgh_log_max =
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CGH_ARRAY_LOG_MAXY);

	new Thresholds
	    (true, VAMPConstants.THR_CGH, true,
	     cgh_log_min, cgh_log_max);

	new Thresholds
	    (true, VAMPConstants.THR_CGH, false,
	     cgh_lin_min, cgh_lin_max);

	// LOH
	double loh_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_LOH_MINY);
	double loh_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_LOH_MAXY);

	double loh_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_LOH_LOG_MINY);
	double loh_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_LOH_LOG_MAXY);

	new Thresholds
	    (true, VAMPConstants.THR_LOH, true,
	     loh_log_min, loh_log_max /*, HIDDEN*/);

	new Thresholds
	    (true, VAMPConstants.THR_LOH, false,
	     loh_lin_min, loh_lin_max /*, HIDDEN */);

	// SNiP
	double snp_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_SNP_MINY);
	double snp_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_SNP_MAXY);

	double snp_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_SNP_LOG_MINY);
	double snp_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_SNP_LOG_MAXY);

	new Thresholds
	    (true, VAMPConstants.THR_SNP, true,
	     snp_log_min, snp_log_max);

	new Thresholds
	    (true, VAMPConstants.THR_SNP, false,
	     snp_lin_min, snp_lin_max);

	// Transcriptome
	double trs_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_MINY);
	double trs_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_MAXY);

	double trs_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_LOG_MINY);
	double trs_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_LOG_MAXY);
	new Thresholds
	    (true, VAMPConstants.THR_TRS, true,
	     trs_log_min, trs_log_max);

	new Thresholds
	    (true, VAMPConstants.THR_TRS, false,
	     trs_lin_min, trs_lin_max);

	// Transcriptome Relative
	double trsrel_lin_min =
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_REL_MINY);
	double trsrel_lin_max =
	     VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_REL_MAXY);
	double trsrel_log_min =
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_REL_LOG_MINY);
	double trsrel_log_max =
	     VAMPResources.getDouble(VAMPResources.THRESHOLD_TRANSCRIPTOME_REL_LOG_MAXY);

	new Thresholds
	    (true, VAMPConstants.THR_TRSREL, true,
	     trsrel_log_min, trsrel_log_max);

	new Thresholds
	    (true, VAMPConstants.THR_TRSREL, false,
	     trsrel_lin_min, trsrel_lin_max);

	// Chip-Chip
	double chip_chip_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CHIP_CHIP_MINY);
	double chip_chip_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CHIP_CHIP_MAXY);

	double chip_chip_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CHIP_CHIP_LOG_MINY);
	double chip_chip_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_CHIP_CHIP_LOG_MAXY);

	new Thresholds(true, VAMPConstants.THR_CHIP_CHIP, true,
		       chip_chip_log_min, chip_chip_log_max);

	new Thresholds(true, VAMPConstants.THR_CHIP_CHIP, false,
		       chip_chip_lin_min, chip_chip_lin_max);

	// Breakpoint frequency
	double brk_frq_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_BRK_FRQ_MINY);
	double brk_frq_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_BRK_FRQ_MAXY);

	double brk_frq_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_BRK_FRQ_LOG_MINY);
	double brk_frq_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_BRK_FRQ_LOG_MAXY);

	new Thresholds(true, VAMPConstants.THR_BRK_FRQ, false, brk_frq_lin_min, brk_frq_lin_max);
	new Thresholds(true, VAMPConstants.THR_BRK_FRQ, true, brk_frq_log_min, brk_frq_log_max);

	// Karyotype
	double karyo_fragl_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_KARYO_FRAGL_MINY);
	double karyo_fragl_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_KARYO_FRAGL_MAXY);

	double karyo_fragl_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_KARYO_FRAGL_LOG_MINY);
	double karyo_fragl_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_KARYO_FRAGL_LOG_MAXY);
	new Thresholds(true, VAMPConstants.THR_KARYO_FRAGL, false, karyo_fragl_lin_min, karyo_fragl_lin_max);
	new Thresholds(true, VAMPConstants.THR_KARYO_FRAGL, true, karyo_fragl_log_min, karyo_fragl_log_max);

	// FrAGL
	double fragl_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_FRAGL_MINY);
	double fragl_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_FRAGL_MAXY);

	double fragl_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_FRAGL_LOG_MINY);
	double fragl_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_FRAGL_LOG_MAXY);

	new Thresholds(true, VAMPConstants.THR_FRAGL, false, fragl_lin_min, fragl_lin_max);
	new Thresholds(true, VAMPConstants.THR_FRAGL, true, fragl_lin_min, fragl_lin_max);

	// Differential analysis
	double diffana_lin_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_DIFFANA_MINY);
	double diffana_lin_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_DIFFANA_MAXY);

	double diffana_log_min = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_DIFFANA_LOG_MINY);
	double diffana_log_max = 
	    VAMPResources.getDouble(VAMPResources.THRESHOLD_DIFFANA_LOG_MAXY);

	new Thresholds(true, VAMPConstants.THR_DIFFANA, false,
		       diffana_lin_min, diffana_lin_max);

	new Thresholds(true, VAMPConstants.THR_DIFFANA, true,
		       diffana_log_min, diffana_log_max);
    }

    public String getName() {return name;}
    public String getCanName() {return canname;}
    public double getMin() {return min;}
    public double getMax() {return max;}
    public boolean isLog() {return is_log;}
    public boolean isHidden() {return (flags & HIDDEN) != 0;}

    private static Hashtable thresholdsTable = new Hashtable();

    protected void register() {
	if (global)
	    thresholdsTable.put(name, this);
    }

    public static Thresholds get(String name, boolean is_log) {
	return (Thresholds)thresholdsTable.get(name + suffix(is_log));
    }

    public String toString() {
	return "Thresholds[name=" + name + ", min=" + min + ", max=" + max +
	    ", is_log=" + is_log + "]";
    }
}
