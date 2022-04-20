
package fr.curie.vamp.data.serial;

public class ProfileSerialUtils {

    // in fact should be in another package
    public static final int DISPLAY_INDEX = 0;
    public static final int PROP_INDEX = 1;
    public static final String DISPLAY_SUFFIX = ".dsp";
    public static final String PROP_SUFFIX = ".prp";

    static final int CHR_POS_MAP_SERIAL_VERSION = 2;
    static final int PROPMAP_HEADER_SERIAL_VERSION = 3;
    static final int SERIAL_VERSION = 4;

    static final long PORTABLE_MAGIC = 0x56ED120CABE578CDL;

    // tools (future use)
    static final int TOOLS_INFO_CNT = 32;

    static final int TOOL_INFO_GENOME_ALTERATION = 0;
    static final int TOOL_INFO_FRAGL = 1;
    // and so on -> 31
}
