
/*
 *
 * VAMPProperties.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

public class VAMPProperties {

    public final static Property ArrayProp =
	Property.getProperty("Array");

    public final static Property GeneSymbolProp =
	Property.getProperty("GeneSymbol", true);

    public final static Property GeneIDProp =
	//Property.getProperty("GeneId", true);
	Property.getProperty("GeneId");

    public final static Property NmcProp =
	Property.getProperty("Nmc",
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.SHARED,
			     false);

    public final static Property ObjKeyProp =
	Property.getProperty("ObjKey");

    public final static Property ClinicalDataProp =
	Property.getProperty("ClinicalData");

    public final static Property SampleAdditionalDataProp =
	Property.getProperty("SampleAdditionalData");

    public final static Property OtherDataProp =
	Property.getProperty("OtherData");

    public final static Property NameProp =
	Property.getProperty("Name",
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.SHARED,
			     true);

    public final static Property TypeProp =
	Property.getProperty("Type",
			     Property.SHARED |
			     Property.SERIALIZABLE);

    public final static Property ArrayCountProp =
	Property.getProperty("Array Count",
			     PropertyIntegerType.getInstance());

    public final static Property TeamProp = Property.getProperty("Team");

    public final static Property ProjectProp =
	Property.getProperty("Project");

    public final static Property ProjectIdProp =
	Property.getProperty("ProjectId");

    public final static Property ProjectDirProp =
	//Property.getHiddenProperty("ProjectDir");
	Property.getProperty("ProjectDir");

    public final static Property LargeProfileProp =
	Property.getHiddenProperty("LargeProfile");

    public final static Property OrderProp =
	Property.getProperty("Order");

    public final static Property MissingProp =
	Property.getProperty("Missing",
			     PropertyBooleanType.getInstance(),
			     Property.INFOABLE |
			     Property.SERIALIZABLE |
			     Property.FINDABLE);

    public final static Property CommentProp =
	Property.getProperty("Comment",
			     PropertyTextType.getInstance(),
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.EDITABLE);

    public final static Property TagProp =
	Property.getProperty("Tag",
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.EDITABLE);

    public final static Property ArmProp =
	Property.getProperty("Arm", Property.INFOABLE|Property.SERIALIZABLE , false);

    public final static Property GlobalContextProp =
	Property.getHiddenProperty("Global Context");

    public final static Property VectorArrayProp =
	Property.getHiddenProperty("Vector Array");

    public final static Property SerialVectorArrayProp =
	Property.getHiddenProperty("Serial Vector Array");

    public final static Property LostVectorArrayProp =
	Property.getHiddenProperty("Lost Vector Array");

    public final static Property GainedVectorArrayProp =
	Property.getHiddenProperty("Gained Vector Array");

    public final static Property MergeOffsetProp =
	Property.getHiddenProperty("Merge Offset");

    public final static PointCountProperty PointCountProp = 
	new PointCountProperty();

    public final static RatioProperty RatioProp = new RatioProperty();
    public final static RatioProperty CopyNBProp = new RatioProperty("CopyNb");
    public final static RatioProperty ColorCodeProp = new RatioProperty("ColorCode", true);

    public final static Property NBPProp =
	Property.getProperty("NbP");

    public final static Property FlagProp =
	Property.getProperty("Flag");

    public final static Property ScoreProp =
	Property.getProperty("Score");

    public final static Property PositionProp =
	Property.getProperty("Position",
			     PropertyIntegerType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);
    // 22/06/05: Property.SHARED); no more shareable because of synteny

    public final static Property PositionBeginProp =
	Property.getProperty("Position Begin",
			     PropertyIntegerType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static Property PositionEndProp =
	Property.getProperty("Position End",
			     PropertyIntegerType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static Property PositionChrProp =
	Property.getProperty("Position Chr",
			     PropertyIntegerType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static Property PositionChrBeginProp =
	Property.getProperty("Position Chr Begin",
			     PropertyIntegerType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static Property PositionChrEndProp =
	Property.getProperty("Position Chr End",
			     PropertyIntegerType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static Property CloneProp =
	Property.getProperty("Clone",
			     PropertyStringType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static Property CloneBeginProp =
	Property.getProperty("Clone Begin",
			     PropertyStringType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static Property CloneEndProp =
	Property.getProperty("Clone End",
			     PropertyStringType.getInstance(),
			     Property.INFOABLE | Property.SERIALIZABLE);

    public final static String NA = "NA";

    public final static Property IsNAProp =
	Property.getProperty(NA,
			     PropertyBooleanType.getInstance(),
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.EDITABLE);

    public final static Property OrganismProp =
	Property.getProperty("Organism");

    public final static Property LinkedDataProp =
	Property.getHiddenProperty("LinkedData");

    public final static Property LinkedDataSetProp =
	Property.getHiddenProperty("LinkedDataSet");

    public final static Property SyntenyOrganismProp =
	Property.getProperty("SyntenyOrganism");

    public final static Property SyntenyProp =
	Property.getProperty("Synteny");

    public final static Property SyntenyOrigProp =
	Property.getProperty("Synteny Origin");

    public final static Property SyntenyRegionProp =
	Property.getHiddenProperty("Synteny Region");

    public final static Property SyntenyOPProp =
	Property.getHiddenProperty("Synteny OP");

    public final static Property CloneCountProp =
	Property.getProperty("Clone Count",
			     PropertyIntegerType.getInstance());

    public final static Property RatioScaleProp =
	Property.getProperty("Ratio Scale");

    public final static Property CenterPosProp =
	Property.getHiddenProperty("CenterPos");

    public final static Property OrigArrayProp =
	Property.getHiddenProperty("OrigArray");

    public final static Property ChromosomeProp =
	Property.getProperty("Chr",
			     Property.INFOABLE |
			     Property.SERIALIZABLE |
			     Property.FINDABLE
			     // 10/05/05: disconnected because of synteny bug
			     /*| Property.SHARED*/,
			     false);

    public final static Property ChrAliasProp =
	Property.getHiddenProperty("ChrAlias");
    public final static Property ChrAlias2Prop =
	Property.getHiddenProperty("ChrAlias2");

    public final static GNLProperty GNLProp = new GNLProperty();

    public final static Property SplitChrProp =
	Property.getHiddenProperty("SplitChr");

    public final static Property IDProjectProp =
	Property.getHiddenProperty("IDProject");

    /*
    public final static Property SignalProp =
	Property.getProperty("Signal",
			     PropertyFloatNAType.getInstance(),
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.EDITABLE);
    */

    public final static SignalProperty SignalProp = new SignalProperty();

    public final static RSignalProperty RSignalProp = new RSignalProperty();

    public final static Property SignalScaleProp =
	Property.getProperty("Signal Scale");

    public final static Property SmoothingProp =
	Property.getProperty("Smt",
			     PropertyFloatNAType.getInstance(),
			     Property.INFOABLE |
			     Property.SERIALIZABLE |
			     Property.EDITABLE);

    public final static Property CentromereProp =
	Property.getProperty("Ctm");

    public final static Property BreakpointProp =
	Property.getProperty("Bkp",
			     new PropertyChoiceType
			     ("breakpoint",
			      new String[]{"NA", "-1", "0", "1"}),
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.EDITABLE);

    public final static Property OutProp =
	Property.getProperty("Out",
			     new PropertyChoiceType
			     ("outlier",
			      new String[]{"NA", "-1", "0", "1"}),
			     Property.INFOABLE |
			     Property.FINDABLE |
			     Property.SERIALIZABLE |
			     Property.EDITABLE);

    public final static Property SizeProp =
	Property.getProperty("Size",
			     PropertyIntegerNAType.getInstance(),
			     Property.INFOABLE |
			     Property.SERIALIZABLE |
			     Property.FINDABLE);
    // 22/06/05: Property.SHARED); no more shareable because of synteny

    public final static Property RegionSizeProp =
	Property.getProperty("Size ",
			     PropertyIntegerType.getInstance(),
			     Property.SERIALIZABLE |
			     Property.INFOABLE);

    public final static Property ReferenceProp =
	Property.getProperty("Reference");

    public final static Property SyntenyReferenceProp =
	Property.getHiddenProperty("SyntenyReference");

    public final static Property RegionProp =
	Property.getProperty("Region");

    public final static Property TransProp =
	Property.getHiddenProperty("Trans");

    public final static Property TransRefProp =
	Property.getHiddenProperty("TransRef");

    public final static Property TNamesProp =
	Property.getHiddenProperty("TNames");

    public final static Property ArrayRefProp =
	Property.getHiddenProperty("ArrayRef");

    public final static Property ArraysRefProp =
	Property.getHiddenProperty("ArraysRef");

    public final static Property ArrayRefNameProp =
	Property.getProperty("ArrayRefName");

    public final static Property NumHistoProp =
	Property.getProperty("NumHisto");

    public final static Property URLProp =
	Property.getHiddenProperty("Url");

    public final static Property URLMapProp =
	Property.getHiddenProperty("URLMapProp");

    public final static Property ExonsProp =
	Property.getProperty("Exons", 0);

    public final static Property IntronsProp =
	Property.getProperty("Introns", 0);

    static Property ProbeSetCountProp =
	Property.getProperty("ProbeSet Count",
			     PropertyIntegerType.getInstance());

    static Property MicrosatCountProp =
	Property.getProperty("Microsat Count",
			     PropertyIntegerType.getInstance());

    // ColorCodes
    public final static Property CCLogProp =
	Property.getHiddenProperty("ColorCodesLog");
    public final static Property CCLinProp =
	Property.getHiddenProperty("ColorCodesLin");
    public final static Property CCNameProp =
	Property.getHiddenProperty("ColorCodeName");

    public final static Property AffineTransformProp =
	Property.getHiddenProperty("AffineTransform");

    // Thresholds
    public final static Property ThresholdsLinProp =
	Property.getHiddenProperty("ThresholdsLin");
    public final static Property ThresholdsLogProp =
	Property.getHiddenProperty("ThresholdsLog");
    public final static Property ThresholdsNameProp =
	Property.getHiddenProperty("ThresholdsName");

    public final static Property LoginProp =
	Property.getHiddenProperty("Login");

    public final static Property ToolResultInfoProp =
	//Property.getHiddenProperty("ToolResultInfo");
	Property.getProperty("ToolResultInfo");

    public final static Property MaskedRatioProp = Property.getMaskedProperty(RatioProp);
    public final static Property MaskedGNLProp = Property.getMaskedProperty(GNLProp);
    public final static Property MaskedBreakpointProp = Property.getMaskedProperty(BreakpointProp);
    public final static Property MaskedFlagProp = Property.getMaskedProperty(FlagProp);
    public final static Property MaskedNBPProp = Property.getMaskedProperty(NBPProp);
    public final static Property MaskedOutProp = Property.getMaskedProperty(OutProp);
    public final static Property MaskedSmoothingProp = Property.getMaskedProperty(SmoothingProp);

    static void init() {
	ReferenceProp.setTrigger(new PropertyTriggerWrapper() {
		public void add_after(Property prop, PropertyElement elem,
				      Object value) {
		    ((DataSet)elem).setAxisDisplayer
			(Config.defaultTranscriptomeReferenceAxisDisplayer);
		}

		public void remove_after(Property prop, PropertyElement elem) {
		    ((DataSet)elem).setAxisDisplayer
			(Config.defaultTranscriptomeAxisDisplayer);
		}
	    });

	/*
	RatioProp.setTrigger(new PropertyTriggerWrapper() {
		public void add_after(Property prop,
				      PropertyElement elem, Object value) {
		    if (!(elem instanceof DataElement))
			return;
		}
	    });
	*/

	ChrAliasProp.setTrigger(new PropertyTriggerWrapper() {
		public Object get_property_value(Property prop,
						 PropertyElement elem) {
		    String chr =  (String)elem.getPropertyValue_r(ChromosomeProp);
		    if (chr.equals("23")) return "X";
		    if (chr.equals("24")) return "Y";
		    return chr;
		}
	    });

	ChrAlias2Prop.setTrigger(new PropertyTriggerWrapper() {
		public Object get_property_value(Property prop,
						 PropertyElement elem) {
		    String chr =  (String)elem.getPropertyValue_r(ChromosomeProp);
		    chr = VAMPUtils.normChr(chr);
		    if (chr.equals("23")) return "X";
		    if (chr.equals("24")) return "Y";
		    return chr;
		}
	    });

    }
}
