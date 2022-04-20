
 /*
  *
  * DataElementRange.java
  *
  * Project : VAMP Application
  *
  * Eric Viara for Institut Curie copyright (c) 2004
  *
  */

package fr.curie.vamp;

class DataElementRange {
    private int ind_min, ind_max;
    private double percent;
    private GraphElement graphElement;
    public static final int INVALID_IND = -1;

    DataElementRange(GraphElement graphElement, int ind_min, int ind_max,
		      double percent) {
	this.graphElement = graphElement;
	this.ind_min = ind_min;
	this.ind_max = ind_max;
	this.percent = percent;
    }

    DataElementRange(GraphElement graphElement, int ind_min) {
	this(graphElement, ind_min, INVALID_IND, 0.);
    }

    void setGraphElement(GraphElement graphElement) {
	this.graphElement = graphElement;
    }

    public double computePosX() {
	return computePosX(graphElement);
    }

    public double computePosX(GraphElement graphElement) {
	if (graphElement == null) return -1;
	DataSet dataSet = (DataSet)graphElement;
	DataElement data[] = dataSet.getData();
	double posx;
	if (ind_max == INVALID_IND)
	    posx = getPosX(data, graphElement, ind_min);
	else
	    posx = getPosX(data, graphElement, ind_min) +
		(getPosX(data, graphElement, ind_max) -
		 getPosX(data, graphElement, ind_min)) * percent;

	//System.out.println(this + " -> posx = " + posx);
	return posx;
    }

    public double computeVX() {
	return computeVX(graphElement);
    }

    public double computeVX(GraphElement graphElement) {
	if (graphElement == null) return -1;
	DataSet dataSet = (DataSet)graphElement;
	DataElement data[] = dataSet.getData();
	double vx;
	if (ind_max == INVALID_IND)
	    vx = getVX(data, graphElement, ind_min);
	else
	    vx = getVX(data, graphElement, ind_min) +
		(getVX(data, graphElement, ind_max) -
		 getVX(data, graphElement, ind_min)) * percent;
	//System.out.println(this + " -> vx = " + vx);
	return vx;
    }

    private double getPosX(DataElement data[], GraphElement graphElement,
			   int ind) {
	return ind >= 0 ? data[ind].getPosMiddle(graphElement) : 0;
    }

    private double getVX(DataElement data[], GraphElement graphElement,
			 int ind) {
	return ind >= 0 ? data[ind].getVMiddle(graphElement) : 0;
    }

    public boolean equalsTo(DataElementRange range) {
	return
	    ind_min == range.ind_min &&
	    ind_max == range.ind_max &&
	    percent == range.percent;
    }

    public boolean lessThan(DataElementRange range) {
	if (ind_min != range.ind_min)
	    return ind_min < range.ind_min;

	if (ind_max != range.ind_max)
	    return ind_max < range.ind_max;

	return percent < range.percent;
    }

    public int getIndMin() { return ind_min; }
    public int getIndMax() { return ind_max; }
    public double getPercent() { return percent; }

    public boolean isIndMaxSet() { return ind_max != INVALID_IND; }

    public String toString() {
	return ind_min + ":" + ind_max + ":" + percent;
    }
}
