
/*
 *
 * VAMPConstants.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;


public class VAMPConstants {
    static final String COPYRIGHT = 
	"VAMP is developped and\nregistered by Institut Curie";
    
    static final String COPYRIGHT_S[] =
	new String[]{"VAMP is developped and registered by",
		     "Institut Curie"};

    static final String WEBSITE = "http://bioinfo.curie.fr/vamp";
    static final String CONTACT = "vamp@curie.fr";

    public static boolean _DISPLAY_ = false;

    public static final int CLONE_UNKNOWN = -100;
    public static final int CLONE_NA = -2;
    public static final int CLONE_LOST = -1;
    public static final int CLONE_NORMAL = 0;
    public static final int CLONE_GAINED = 1;
    public static final int CLONE_AMPLICON = 2;
    public static final String CLONE_UNKNOWN_STR = Utils.toString(CLONE_UNKNOWN);

    public final static String RatioScale_M = "M";
    public final static String RatioScale_L = "L";
    public final static String SignalScale_M = "M";
    public final static String SignalScale_L = "L";

    public final static String THR_CGH = "CGH";
    public final static String THR_CHIP_CHIP = "ChIP-chip";
    public final static String THR_TRS = "TRS";
    public final static String THR_TRSREL = "TRSREL";
    public final static String THR_SNP = "SNP";
    public final static String THR_LOH = "LOH";
    public final static String THR_BRK_FRQ = "BRK_FRQ";
    public final static String THR_FRAGL = "FrAGL";
    public final static String THR_KARYO_FRAGL = "KaryoFrAGL";
    public final static String THR_DIFFANA = "DiffAna";
    public final static String THR_GTCA = "GTCA";

    public final static String IMPORT_DATA_URL = "ImportDataURL";

    public final static String CC_LOG_SUFFIX = "_LOG";
    public final static String CC_LIN_SUFFIX = "_LIN";
    public final static int CC_SUFFIX_LENGTH = CC_LOG_SUFFIX.length();

    public final static String CC_CGH = "CGH";
    public final static String CC_CHIP_CHIP = "ChIP-chip";

    public final static String CC_LOH = "LOH";
    public final static String CC_SNP = "SNP";
    public final static String CC_GTCA = "GTCA";

    public final static String CC_ABS_TRSCLS = "Absolute TRSCLS";
    public final static String CC_REL_TRSCLS = "Relative TRSCLS";

    public final static String CC_TRSREL = "TRSREL";

    public final static String CGH_ARRAY_TYPE = "CGH Array";

    public final static String CGH_CHROMOSOME_MERGE_TYPE =
	"CGH Chromosome Merge";
    public final static String CGH_ARRAY_MERGE_TYPE = "CGH Array Merge";
    public final static String CGH_AVERAGE_TYPE = "CGH Average";

    public final static String CHIP_CHIP_TYPE = "ChIP-chip";

    public final static String CHIP_CHIP_CHROMOSOME_MERGE_TYPE =
	"ChIP-chip Chromosome Merge";
    public final static String CHIP_CHIP_ARRAY_MERGE_TYPE =
	"ChIP-chip Array Merge";
    public final static String CHIP_CHIP_AVERAGE_TYPE = 
	"ChIP-chip Average";

    public final static String GTCA_TYPE = "GTCA";
    public final static String GTCA_CHROMOSOME_MERGE_TYPE = "GTCA Chromosome Merge";
    public final static String TCM_TYPE = "TCM";
    public final static String TCM_CHROMOSOME_MERGE_TYPE = "TCM Chromosome Merge";

    public final static String TRANSCRIPTOME_TYPE = "Transcriptome";
    public final static String TRANSCRIPTOME_CLUSTER_TYPE = "Transcriptome Cluster";
    public final static String TRANSCRIPTOME_AVERAGE_TYPE =
	"Transcriptome Average";
    public final static String TRANSCRIPTOME_MERGE_TYPE =
	"Transcriptome Merge";
    public final static String TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE =
	"Transcriptome Chromosome Merge";
    public final static String TRANSCRIPTOME_REL_TYPE =
	"Transcriptome Relative";
    public final static String TRANSCRIPTOME_MERGE_REL_TYPE =
	"Transcriptome Merge Relative";

    public final static String SNP_TYPE = "Affy-SNPs";
    public final static String SNP_CHROMOSOME_MERGE_TYPE =
	"Affy-SNPs Chromosome Merge";
    public final static String SNP_AVERAGE_TYPE = "SNP Average";
    public final static String GENOME_ANNOT_TYPE = "GenomeAnnotation";
    public final static String GENOME_ANNOT_CHROMOSOME_MERGE_TYPE =
	"GenomeAnnotation Chromosome Merge";

    public final static String SIGNAL_DENSITY_TYPE = "Signal Density";
    public final static String SIGNAL_DENSITY_ITEM_TYPE = "Signal Density Item";

    public final static String FRAGL_CHROMOSOME_MERGE_TYPE =
	"FrAGL Chromosome Merge";
    public final static String FRAGL_ARRAY_MERGE_TYPE =
	"FrAGL Array Merge";

    public final static String FRAGL_TYPE = "FrAGL";

    public final static String DIFFANA_CHROMOSOME_MERGE_TYPE =
	"Differential Analysis Chromosome Merge";
    public final static String DIFFANA_TYPE = "Differential Analysis";

    public final static String LOH_TYPE = "LOH";
    public final static String LOH_CHROMOSOME_MERGE_TYPE =
	"LOH Chromosome Merge";
    public final static String MINIMAL_REGION_TYPE = "Minimal Region";
    public final static String RECURRENT_REGION_TYPE = "Recurrent Region";
    public final static String RECURRENT_BREAKPOINT_TYPE = "Recurrent Breakpoint";
    public final static String DENDROGRAM_BRANCH_TYPE = "Dendrogram Branch";

    public final static String BREAKPOINT_FREQUENCY_TYPE =
	"Breakpoint Frequency";

    public final static String BREAKPOINT_FREQUENCY_CHROMOSOME_MERGE_TYPE =
	"Breakpoint Frequency Chromosome Merge";

    public final static double LOH_POS_Y = 0.5;
}
