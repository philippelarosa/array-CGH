
/*
 *
 * VAMPUtils.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import fr.curie.vamp.data.*;
import java.util.*;
import java.awt.geom.*;

public class VAMPUtils {

    private static final String ARRAY_DIR = "/array/";
    private static final String ALL_DIR = "/all/";

    public static double getThresholdMinY(GraphElement graphElement) {
	Thresholds thresholds = getThresholds(graphElement);
	if (thresholds == null)
	    return -Double.MAX_VALUE;
	return thresholds.getMin();
    }

    public static double getThresholdMaxY(GraphElement graphElement) {
	Thresholds thresholds = getThresholds(graphElement);
	if (thresholds == null)
	    return Double.MAX_VALUE;
	return thresholds.getMax();
    }

    public static double getThresholdVY(GraphElement graphElement,
					DataElement dataElement) {
	double minY = getThresholdMinY(graphElement);
	double maxY = getThresholdMaxY(graphElement);
	double vy = dataElement.getVY(graphElement);
	if (vy < minY)
	    return minY;
	if (vy > maxY)
	    return maxY;
	return vy;
    }

    public static double getThresholdVY(GraphElement graphElement,
					Probe probe) {
	double minY = getThresholdMinY(graphElement);
	double maxY = getThresholdMaxY(graphElement);
	double vy = probe.getVY(graphElement);
	if (vy < minY)
	    return minY;
	if (vy > maxY)
	    return maxY;
	return vy;
    }

    //
    // Type management
    //

    public static boolean isTypeCompatible(String typ1, String typ2) {

	// added 15/02/05:
	if (true)
	    return true;

	if (typ1.equals(VAMPConstants.TRANSCRIPTOME_TYPE))
	    return typ2.equals(VAMPConstants.CGH_ARRAY_TYPE);

	if (typ1.equals(VAMPConstants.CGH_ARRAY_TYPE))
	    return typ2.equals(VAMPConstants.TRANSCRIPTOME_TYPE);

	if (typ1.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE))
	    return typ2.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE);

	if (typ1.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
	    return typ2.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE);

	if (typ1.equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE))
	    return false;

	if (typ1.equals(VAMPConstants.CGH_AVERAGE_TYPE))
	    return true; // 1/10/04: was false

	return false;
    }

    public static boolean isTypeCompatible(int n, Object types[]) {
	String typ1 = (String)types[n];

	for (++n; n < types.length; n++)
	    if (!isTypeCompatible(typ1, (String)types[n])) {
		System.out.println("types " + typ1 + " vs. " + types[n] +
				   " not compatible");
		return false;
	    }

	return true;
    }

    /*
    static String getType(DataElement elem) {
	return elem.getType();
    }
    */

    public static String getType(PropertyElement elem) {
	return (String)elem.getPropertyValue(VAMPProperties.TypeProp);
    }

    public static void setType(PropertyElement elem, String value) {
	elem.setPropertyValue(VAMPProperties.TypeProp, value);
    }

    public static boolean checkGraphElementType(LinkedList graphElements) {
	return checkGraphElementType(graphElements, (String)null);
    }

    public static TreeSet getTypeSet(LinkedList graphElements) {
	TreeSet typeSet = new TreeSet();

	int size = graphElements.size();
	for (int n = 0; n < size; n++)
	    typeSet.add(((GraphElement)graphElements.get(n)).getPropertyValue(VAMPProperties.TypeProp));

	return typeSet;
    }

    public static boolean isIn(String s, Object arr[]) {
	for (int n = 0; n < arr.length; n++)
	    if (s.equals(arr[n])) return true;
	return false;
    }

    public static void type_dump(Object types[]) {
	for (int n = 0; n < types.length; n++)
	    System.out.print(types[n] + " ");
    }

    public static boolean checkGraphElementType(LinkedList graphElements, String itypes[]) {
	if (itypes == null) return true;
	Object types[] = getTypeSet(graphElements).toArray();
	/*
	type_dump(types);
	System.out.print("vs. ");
	type_dump(itypes);
	*/

	for (int n = 0; n < types.length; n++) {
	    if (!isIn((String)types[n], itypes))
		return false;
	}

	return true;
    }

    public static boolean checkGraphElementType(LinkedList graphElements, String type) {
	//System.out.println("checkGraphElementType SIMPLE begin " + graphElements.size());
	TreeSet typeSet = getTypeSet(graphElements);
	if (type != null) typeSet.add(type);

	Object types[] = typeSet.toArray();

	for (int n = 0; n < types.length; n++)
	    if (!isTypeCompatible(n, types))
		return false;
	//System.out.println("checkGraphElementType SIMPLE end returns true");
	return true;
    }

    public static int getGNL(View view, GraphElement graphElement, RODataElementProxy elem) {
	if (elem instanceof Probe) {
	    return ((Probe)elem).getGnl();
	}
	return getGNL(view, graphElement, (DataElement)elem);
    }

    public static int getGNL(View view, GraphElement graphElement, DataElement elem) {
	return getGNL(view, graphElement, elem, false);
    }

    public static int getGNL(View view, GraphElement graphElement, RODataElementProxy elem,
		      boolean force) {
	// changed 31/01/05
	/*
	if (elem.getPropertyValue(VAMPConstants.IsNAProp) != null)
	    return CLONE_NA;
	*/
	if (VAMPUtils.isNA(elem))
	    return VAMPConstants.CLONE_NA;

	if (force || isGNL(view)) {
	    if (elem instanceof Probe) {
		return ((Probe)elem).getGnl();
	    }
	    String gnl = (String)elem.getPropertyValue(VAMPProperties.GNLProp);
	    return GNLProperty.getGNL(gnl);
	}

	double ratio;

	if (elem instanceof Probe) {
	    ratio = ((Probe)elem).getRatio();
	}
	else {
	    ratio = Utils.parseDouble
		((String)elem.getPropertyValue(VAMPProperties.RatioProp));
	}

	ColorCodes cc = VAMPUtils.getColorCodes(graphElement);
	if (cc instanceof StandardColorCodes) {
	    StandardColorCodes scc = (StandardColorCodes)cc;
	    if (ratio >= scc.getNormalMax())
		return VAMPConstants.CLONE_GAINED;
	    if (ratio <= scc.getNormalMin())
		return VAMPConstants.CLONE_LOST;
	    return VAMPConstants.CLONE_NORMAL;
	}

	return VAMPConstants.CLONE_NA;
    }

    public static boolean isGNL(View view) {
	if (view != null) {
	    GraphElementDisplayer ds = view.getPanel(0).getDefaultGraphElementDisplayer();
	    if (ds instanceof CommonDataSetDisplayer) {
		CommonDataSetDisplayer sds = (CommonDataSetDisplayer)ds;
		return sds.isGNLColorCodes();
	    }
	}
	return false;
    }

    public static boolean isLogScale(GraphElement graphElement) {
	Object scale = graphElement.getPropertyValue(VAMPProperties.RatioScaleProp);
	if (scale != null) 
	    return scale.equals(VAMPConstants.RatioScale_L);
	scale = graphElement.getPropertyValue(VAMPProperties.SignalScaleProp);
	if (scale != null)
	    return scale.equals(VAMPConstants.SignalScale_L);

	//System.out.println(graphElement.getID() + ": no scale...");
	return false;
    }

    public static double getTranscriptomeRange(GraphElement ref) {
	String maxy, miny;
	maxy = VAMPResources.THRESHOLD_TRANSCRIPTOME_MAXY;
	miny = VAMPResources.THRESHOLD_TRANSCRIPTOME_MINY;
	if (isLogScale(ref))
	    return Utils.log(VAMPResources.getDouble(maxy)) -
		Utils.log(VAMPResources.getDouble(miny));
	return VAMPResources.getDouble(maxy) - VAMPResources.getDouble(miny);
    }

    public static double getTranscriptomeRelRange(GraphElement ref) {
	String maxy, miny;
	maxy = VAMPResources.THRESHOLD_TRANSCRIPTOME_REL_MAXY;
	miny = VAMPResources.THRESHOLD_TRANSCRIPTOME_REL_MINY;

	if (isLogScale(ref))
	    return Utils.log(VAMPResources.getDouble(maxy)) -
		Utils.log(VAMPResources.getDouble(miny));
	return VAMPResources.getDouble(maxy) - VAMPResources.getDouble(miny);
    }

    public static double getTranscriptomeYCoef(GraphElement ref, GraphElement dset) {
	double ref_range = getThresholdMaxY(ref) - getThresholdMinY(ref);
	return getTranscriptomeRange(dset) / ref_range;
    }

    public static double getTranscriptomeRelYCoef(GraphElement ref, GraphElement dset) {
	double ref_range = getThresholdMaxY(ref) - getThresholdMinY(ref);
	return getTranscriptomeRelRange(dset) / ref_range;
    }

    public static boolean isMonoChr(Vector graphElements) {

	int size = graphElements.size();
	String lastChr = null;
	for (int n = 0; n < size; n++) {
	    GraphElement graphElement = (GraphElement)graphElements.get(n);
	    String chr = VAMPUtils.getChr(graphElement);
	    if (chr == null)
		return false;
	    if (lastChr != null && !lastChr.equals(chr))
		return false;
	    lastChr = chr;
	}
	return true;
    }

    public static ColorCodes getGlobalColorCodes(GraphElement graphElement) {
	String ccName = (String)graphElement.getPropertyValue(VAMPProperties.CCNameProp);
	if (ccName == null)
	    return null;
	return ColorCodes.get(ccName, isLogScale(graphElement));
    }

    public static ColorCodes getColorCodes(GraphElement graphElement) {
	ColorCodes gcc = (ColorCodes)VAMPUtils.getGlobalColorCodes(graphElement);
	/*
	Property prop = (gcc instanceof StandardColorCodes &&
			 ((StandardColorCodes)gcc).isLog()) ?
	    VAMPConstants.CCLogProp : VAMPConstants.CCLinProp;
	*/

	Property prop = isLogScale(graphElement) ? VAMPProperties.CCLogProp : VAMPProperties.CCLinProp;

	ColorCodes cc = (ColorCodes)graphElement.getPropertyValue(prop);
	return cc != null ? cc : gcc;

    }

    public static void setLocalColorCodes(GraphElement graphElement, ColorCodes ccLog,
				   ColorCodes ccLin) {
	//System.out.println("setting local color codes:" + ccLog);
	graphElement.setPropertyValue(VAMPProperties.CCLogProp, ccLog);
	graphElement.setPropertyValue(VAMPProperties.CCLinProp, ccLin);
    }

    public static void removeLocalColorCodes(GraphElement graphElement) {
	graphElement.removeProperty(VAMPProperties.CCLogProp);
	graphElement.removeProperty(VAMPProperties.CCLinProp);
    }

    public static Thresholds getGlobalThresholds(GraphElement graphElement) {
	String thrName = (String)graphElement.getPropertyValue(VAMPProperties.ThresholdsNameProp);
	if (thrName == null)
	    return null;

	return Thresholds.get(thrName, isLogScale(graphElement));
    }

    public static Thresholds getThresholds(GraphElement graphElement) {
	Thresholds gthr = (Thresholds)VAMPUtils.getGlobalThresholds(graphElement);
	Property prop = isLogScale(graphElement) ? VAMPProperties.ThresholdsLogProp :
	    VAMPProperties.ThresholdsLinProp;

	Thresholds lthr = (Thresholds)graphElement.getPropertyValue(prop);
	return lthr != null ? lthr : gthr;
    }

    public static void setLocalThresholds(GraphElement graphElement,
				   Thresholds thrLog,
				   Thresholds thrLin) {
	graphElement.setPropertyValue(VAMPProperties.ThresholdsLogProp, thrLog);
	graphElement.setPropertyValue(VAMPProperties.ThresholdsLinProp, thrLin);
    }

    public static void removeLocalThresholds(GraphElement graphElement) {
	graphElement.removeProperty(VAMPProperties.ThresholdsLogProp);
	graphElement.removeProperty(VAMPProperties.ThresholdsLinProp);
    }

    public static double max_log_y = Double.MIN_VALUE;
    public static double min_log_y = Double.MAX_VALUE;
    public static double max_lin_y = Double.MIN_VALUE;
    public static double min_lin_y = Double.MAX_VALUE;

    public static double MIN_LOG_Y, MAX_LOG_Y, MIN_LIN_Y, MAX_LIN_Y;

    public static boolean bounds_init = false;

    public static double COEF = 1.05;

    public static double augment(double m) {
	if (m > 0) return m * COEF;
	return m / COEF;
    }

    public static double decrement(double m) {
	if (m > 0) return m / COEF;
	return m * COEF;
    }

    public static Chromosome getChromosome(GlobalContext globalContext,
				    String organism, String chr) {
	if (organism == null || chr == null)
	    return null;

	MiniMapDataFactory miniMapFact =
	    MiniMapDataFactory.getDataFact(globalContext, organism);

	return miniMapFact.getData(miniMapFact.getDefaultResolution()).
	    getChromosome(chr);
    }

    public static String getChr(PropertyElement elem) {
	return (String)elem.getPropertyValue(VAMPProperties.ChromosomeProp);
    }

    public static String getChr(DataElement elem) {
	return getChr((PropertyElement)elem);
    }

    public static String getChr(RODataElementProxy elem) {
	if (elem instanceof Probe) {
	    return (new Integer(((Probe)elem).getChr())).toString();
	}

	return getChr((PropertyElement)elem);
    }

    public static String getNormChr(PropertyElement elem) {
	return normChr(getChr(elem));
    }

    public static String getOS(RODataElementProxy elem) {
	return (String)elem.getPropertyValue(VAMPProperties.OrganismProp);
    }

    public static String getOS(PropertyElement elem) {
	return (String)elem.getPropertyValue(VAMPProperties.OrganismProp);
    }

    public static String getGNL(PropertyElement elem) {
	return (String)elem.getPropertyValue(VAMPProperties.GNLProp);
    }

    public static String getGNL(DataElement elem) {
	return getGNL((PropertyElement)elem);
    }

    public static String getGNL(RODataElementProxy elem) {
	if (elem instanceof Probe) {
	    return (new Integer(((Probe)elem).getGnl())).toString();
	}

	return getGNL((PropertyElement)elem);
    }

    /*
    public static boolean isOutlier(PropertyElement elem) {
	Object out = elem.getPropertyValue(VAMPProperties.OutProp);
	return out != null && (out.equals("1") || out.equals("-1"));
    }
    */

    public static boolean isOutlier(RODataElementProxy elem) {
	if (elem instanceof Probe) {
	    int out = ((Probe)elem).getOut();
	    return out != 0;
	}
	Object out = elem.getPropertyValue(VAMPProperties.OutProp);
	return out != null && (out.equals("1") || out.equals("-1"));
    }

    public static boolean isBreakpoint(PropertyElement elem) {
	Object bkp = elem.getPropertyValue(VAMPProperties.BreakpointProp);
	return bkp != null && bkp.equals("1");
    }

    public static boolean isNA(DataElement elem) {
	String is_na = elem.getIsNA();
	return is_na != null && is_na.equalsIgnoreCase("true");
    }

    // EV 23/05/08: warning, changed this method to the following
    /*
    public static boolean isNA(RODataElementProxy elem) {
	String value = (String)elem.getPropertyValue(VAMPProperties.IsNAProp);
	return value != null && value.equalsIgnoreCase("true");
    }
    */

    public static boolean isNA(RODataElementProxy elem) {
	if (elem instanceof Probe) {
	    return ((Probe)elem).isNA();
	}

	return isNA((DataElement)elem);
    }

    public static boolean isNA(PropertyElement elem) {
	String value = (String)elem.getPropertyValue(VAMPProperties.IsNAProp);
	return value != null && value.equalsIgnoreCase("true");
    }

    public static boolean isMissing(PropertyElement elem) {
	String value = (String)elem.getPropertyValue(VAMPProperties.MissingProp);
	return value != null && value.equalsIgnoreCase("true");
    }

    public static boolean isMissing(DataElement elem) {
	return isMissing((PropertyElement)elem);
    }

    public static boolean isMissing(RODataElementProxy elem) {
	if (elem instanceof Probe) {
	    return ((Probe)elem).isMissing();
	}

	return isMissing((DataElement)elem);
    }

    public static boolean hasTag(DataElement elem) {
	String tag = elem.getTag();
	return tag != null && tag.length() > 0;
    }

    public static String getTag(DataElement elem) {
	return elem.getTag();
    }

    public static boolean hasTag(PropertyElement elem) {
	String value = (String)elem.getPropertyValue(VAMPProperties.TagProp);
	return value != null && value.length() > 0;
    }

    public static String getTag(PropertyElement elem) {
	return (String)elem.getPropertyValue(VAMPProperties.TagProp);
    }

    public static String getTitle() {return "VAMP";}

    public static boolean isMergeChr(GraphElement graphElement) {
	if (VAMPUtils.getType(graphElement).equals(VAMPConstants.CGH_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.CHIP_CHIP_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.SNP_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.DIFFANA_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.GTCA_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.FRAGL_CHROMOSOME_MERGE_TYPE) ||
	    VAMPUtils.getType(graphElement).equals(VAMPConstants.GENOME_ANNOT_CHROMOSOME_MERGE_TYPE)) {
	    return true;
	}

	return isMergeChr(VAMPUtils.getChr(graphElement));
    }	

    public static boolean isMergeChr(String chr) {
	return chr != null && chr.indexOf(',') > 0;
    }

    public static boolean isFullMergeChr(GraphElement graphElement) {
	String chr = VAMPUtils.getChr(graphElement);
	String chr_arr[] = chr.split(",");
	int old_chr_n = 0;
	for (int n = 0; n < chr_arr.length; n++) {
	    int chr_n = Integer.parseInt(norm2Chr(chr_arr[n]));
	    if (chr_n != old_chr_n + 1) {
		return false;
	    }
	    old_chr_n = chr_n;
	}
	return true;
    }

    static private String Login;

    public static void setLogin(String login) {
	Login = login;
    }

    public static String getLogin() {
	return Login;
    }

    public static String normChr(String chr) {
	if (chr.length() == 1 && chr.charAt(0) >= '0' && chr.charAt(0) <= '9')
	    chr = "0" + chr;
	return chr;
    }

    public static String norm2Chr(String chr) {
	chr = normChr(chr);
	if (chr.equalsIgnoreCase("x"))
	    return "23";
	if (chr.equalsIgnoreCase("y"))
	    return "24";
	return chr;
    }


    public static void destroy() {
	URLOP.garbage();
	Utils.gc();
    }

    public static Property getCurrentAnnotProp() {
	return null;
    }

    public static boolean hasProperty(LinkedList graphElements, Vector propV) {
	if (graphElements == null || graphElements.size() == 0)
	    return false;

	int size = graphElements.size();
	for (int m = 0; m < size; m++) {
	    DataSet dset = ((GraphElement)graphElements.get(m)).asDataSet();
	    Profile profile = ((GraphElement)graphElements.get(m)).asProfile();
	    if (dset != null) {
		DataElement data[] = dset.getData();
		for (int n = 0; n < data.length && n < 2; n++)
		    if (data[n].hasProperty(propV))
			return true;
	    }
	    else if (profile != null) {
		for (int n = 0; n < profile.getProbeCount() && n < 1; n++) {
		    try {
			Probe p = profile.getProbe(n, true);
			if ((new PropertyElement(p)).hasProperty(propV)) {
			    return true;
			}
		    }
		    catch(Exception e) {
		    }
		}
	    }
	}

	return false;
    }

    public static boolean hasProperty(View view, Vector propV) {
	GraphPanelSet graphPanelSet = view.getGraphPanelSet();

	GraphPanel graphPanels[] = graphPanelSet.getPanels();

	for (int n = 0; n < graphPanels.length; n++)
	    if (hasProperty(graphPanels[n].getGraphElements(),
			    propV))
		return true;

	return false;
    }	

    public static boolean hasProperty(View view, Property prop) {
	Vector propV = new Vector();
	propV.add(prop);
	return hasProperty(view, propV);
    }

    public static boolean isTranscriptome(GraphElement graphElement) {
	String type = VAMPUtils.getType(graphElement);
	return
	    type.equals(VAMPConstants.TRANSCRIPTOME_TYPE) ||
	    type.equals(VAMPConstants.TRANSCRIPTOME_AVERAGE_TYPE) ||
	    type.equals(VAMPConstants.TRANSCRIPTOME_CHROMOSOME_MERGE_TYPE) ||
	    type.equals(VAMPConstants.TRANSCRIPTOME_REL_TYPE) ||
	    type.equals(VAMPConstants.TRANSCRIPTOME_MERGE_REL_TYPE);
    }

    public static String getAxisName(GraphElement graphElement) {
	return isTranscriptome(graphElement) ? "Transcriptome" : "Array";
    }

    public static boolean isPanGenomicURL(String url) {
	return url.indexOf(ARRAY_DIR) >= 0;
    }

    public static boolean isPanGenomicURL(GraphElement graphElement) {
	return isPanGenomicURL(graphElement.getSourceURL());
    }

    public static String makePanGenomicURL(GraphElement dataSet) {
	String srcUrl = dataSet.getSourceURL();
	if (srcUrl.indexOf(ARRAY_DIR) >= 0)
	    return srcUrl;

	if (srcUrl.indexOf(ALL_DIR) >= 0) {
	    Object dir = dataSet.getPropertyValue(VAMPProperties.ProjectDirProp);
	    if (dir == null) {
		System.err.println("WARNING: no ProjectDir property on array " + dataSet.getID());
		/*
		dir = dataSet.getPropertyValue(VAMPProperties.ProjectDirProp);
		if (dir == null) {
		    System.err.println("WARNING: no Project property on array " + dataSet.getID());
		    return null;		    
		}
		*/
		return null;
	    }

	    System.out.println("OK: ProjectDir or Project found " + dir + " on " + dataSet.getID());

	    String replaceStr = "/" + dir + ARRAY_DIR;
	    String n_srcUrl = srcUrl.replaceAll(ALL_DIR + "chr../", replaceStr);

	    if (n_srcUrl.equals(srcUrl)) {
		System.out.println("trying again...");
		n_srcUrl = srcUrl.replaceAll(ALL_DIR + "chr./", replaceStr);
	    }

	    return n_srcUrl;
	}

	return null;
    }

    public static String getMessageName(GraphElement graphElement) {
	return graphElement.getID() + " / " + (isMergeChr(graphElement) ? "Pangenomic" : "Chr " + getChr(graphElement));
    }

    public static String objKey(TreeSet graphElemSet, DataElement data) {
	String objKey = "";
	Iterator it = graphElemSet.iterator();
	for (int n = 0; it.hasNext(); n++) {
	    GraphElement ge = (GraphElement)it.next();
	    if (n > 0)
		objKey += "::";
	    objKey += objKey(ge, data);
	}

	return objKey;
    }


    public static String objKey(GraphElement graphElem, RODataElementProxy data) {

	Property objKeyProp = graphElem.getObjKeyProp();

	String key = (String)data.getPropertyValue(objKeyProp);
	if (key != null)
	    return key;

	objKeyProp = VAMPProperties.NameProp;
	return (String)data.getPropertyValue(objKeyProp);
    }

    public static String objKey_set(TreeSet objKeySet, RODataElementProxy data) {
	String objKey = "";
	Iterator it = objKeySet.iterator();
	for (int n = 0; it.hasNext(); n++) {
	    Property objKeyProp = (Property)it.next();
	    if (n > 0)
		objKey += "::";
	    String key = (String)data.getPropertyValue(objKeyProp);
	    objKey += (key != null ? key : "");
	}

	return objKey;
    }

    /*
    public static double getRX(GraphCanvas canvas, Profile profile, Probe probe) {
	if (canvas.isRotated()) {
	    return _getRY(canvas, profile, probe);
	}
	return _getRX(canvas, profile, probe);
    }

    public static double getRY(GraphCanvas canvas, Profile profile, Probe probe) {
	if (canvas.isRotated()) {
	    return _getRX(canvas, profile, probe);
	}

	return _getRY(canvas, profile, probe);
    }
    */

    public static double getRX(GraphCanvas canvas, Profile profile, Probe probe, boolean pangen) {
	Rectangle2D.Double vBounds = profile.getVBounds();
	double vx0 = vBounds.x;
	double vy0 = vBounds.y;
	double minX = canvas.getMinX();
	double minY = canvas.getMinY();

	double y = probe.getPosY(profile);
	//y = profile.yTransform(y);

	double vx = (pangen ? probe.getPanGenPos(profile) : probe.getPos()) + vx0 + minX;
	double vy = vy0 - y + minY;

	return canvas.getRX(vx, vy);
    }

    public static double getRY(GraphCanvas canvas, Profile profile, Probe probe, boolean pangen) {
	Rectangle2D.Double vBounds = profile.getVBounds();
	double vx0 = vBounds.x;
	double vy0 = vBounds.y;
	double minX = canvas.getMinX();
	double minY = canvas.getMinY();

	double y = probe.getPosY(profile);
	//y = profile.yTransform(y);

	double vx = (pangen ? probe.getPanGenPos(profile) : probe.getPos()) + vx0 + minX;
	//double vx = probe.getPanGenPos(profile) + vx0 + minX;
	double vy = vy0 - y + minY;

	return canvas.getRY(vx, vy);
    }

    static String getHomeDir(GlobalContext globalContext) {
	String home = System.getenv("HOME");
	if (home == null) {
	    home = System.getenv("HOMEPATH");
	    if (home == null) {
		if (globalContext != null) {
		    InfoDialog.pop(globalContext, "HOME neither HOMEPATH environment variables is set");
		}
		return null;
	    }
	}

	return home;
    }

    static public long[] getChrPos(GlobalContext globalContext, String os) {
	if (os == null) {
	    System.err.println("no organism found in profile");
	    return null;
	}

	Cytoband cytoband = MiniMapDataFactory.getCytoband(globalContext, os);
	if (cytoband == null) {
	    System.err.println("cannot load cytoband for organism " + os);
	    return null;
	}
	
	Vector v = cytoband.getChrV();
	long[] chrPos = new long[v.size()];
	for (int n = 0; n < chrPos.length; n++) {
	    chrPos[n] = ((Chromosome)v.get(n)).getOffsetPos();
	}
	
	return chrPos;
    }
}
