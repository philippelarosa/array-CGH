
/*
 *
 * CorrelationInfo.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.applet.*;
import java.net.*;
import java.awt.*;

class CorrelationInfo {

    static final boolean TRACE = false;
    static final boolean ECHO_R = false;

    static final int MAX_PERMS = 1000;

    static final int PEARSON_I = 1;
    static final int SPEARMAN_I = 2;

    static Random rand;

    static void init() {
	rand = new Random(System.currentTimeMillis());
    }

    static private class CorrelationItemInfo {
	double xi, yi;

	CorrelationItemInfo(double xi, double yi) {
	    this.xi = xi;
	    this.yi = yi;
	}
    }

    private DataElement data;
    private DataSet dataSet;
    private Vector info_v;
    private double correl;
    private double pvalue;

    CorrelationInfo(DataSet dataSet, DataElement data) {
	this.dataSet = dataSet;
	this.data = data;
	info_v = new Vector();
	correl = Double.NaN;
	pvalue = Double.NaN;
    }

    CorrelationInfo() {
	this(null, null);
    }

    DataSet getDataSet() {return dataSet;}
    DataElement getData() {return data;}

    void add(double xi, double yi) {
	info_v.add(new CorrelationItemInfo(xi, yi));
    }
	
    double getCorrelation() {return correl;}
    double getPValue() {return pvalue;}
    void setCorrelation(double correl) {this.correl = correl;}
    void setPValue(double pvalue) {this.pvalue = pvalue;}
    int getCorrelSize() {return info_v.size();}

    double computeCorrel(int which) {
	if (which == PEARSON_I)
	    return computePearson();
	if (which == SPEARMAN_I)
	    return computeSpearman();
	return -1;
    }

    private int rank_x(double d) {
	int size = info_v.size();
	int rank = 0;
	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    if (item_info.xi < d)
		rank++;
	}

	return rank;
    }

    private int rank_y(double d) {
	int size = info_v.size();
	int rank = 0;
	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    if (item_info.yi < d)
		rank++;
	}

	return rank;
    }

    static final int IRANK = -1;

    // compute_rank_1
    // traitement des ex aequos : 
    // 0 0 2 3 4 4 6
    // devient :
    // 0 1 2 3 4 5 6

    private int[] compute_rank_1(boolean is_x) {
	int size = info_v.size();

	int rank[] = new int[size];
	for (int i = 0; i < size; i++)
	    rank[i] = IRANK;

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    rank[i] = (is_x ? rank_x(item_info.xi) : rank_y(item_info.yi));
	}

	for (int i = 1; i < size; i++) {
	    for (int j = 0; j < i; j++) {
		if (rank[i] == rank[j]) {
		    int val = rank[j];
		    for (int k = 0, inc = 1; k < size; k++) {
			if (rank[k] == val && k != j) {
			    rank[k] += inc;
			    inc++;
			}
		    }
		}
	    }
	}

	if (TRACE) {
	    for (int i = 0; i < size; i++) {
		CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
		System.out.println("rank[" + i + "] = " + rank[i] +
				   " (" + (is_x ? item_info.xi : item_info.yi) + ")");
	    }
	}

	return rank;
    }

    // compute_rank_2
    // traitement des ex aequos : 
    // 0 0 2 3 4 4 6
    // devient :
    // 0.5 2 3 4.5 6

    private double[] compute_rank_2(boolean is_x) {
	int size = info_v.size();

	double rank[] = new double[size];
	for (int i = 0; i < size; i++)
	    rank[i] = IRANK;

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    rank[i] = (is_x ? rank_x(item_info.xi) : rank_y(item_info.yi));
	}

	for (int i = 1; i < size; i++) {
	    for (int j = 0; j < i; j++) {
		if (rank[i] == rank[j] && rank[j] >= 0) {
		    double val = rank[j];
		    int cnt = 0;
		    double v = 0;
		    for (int k = 0, inc = 0; k < size; k++) {
			if (rank[k] == val) { 
			    cnt++;
			    v += val + inc;
			    inc++;
			}
		    }

		    v = -v/cnt;
		    for (int k = 0; k < size; k++)
			if (rank[k] == val)
			    rank[k] = v;
		}
	    }
	}

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    if (rank[i] < 0)
		rank[i] = -rank[i];
	    if (TRACE)
		System.out.println("rank[" + i + "] = " + rank[i] +
				   " (" + (is_x ? item_info.xi : item_info.yi) + ")");
	}

	return rank;
    }

    // replaced by computePHSpearman()
    /*
    private double computeSpearman() {
	int size = info_v.size();
	if (size == 1)
	    return 0.;

	double sum = 0.;

	int rank_x[] = compute_rank_1(true);
	int rank_y[] = compute_rank_1(false);

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    int di = rank_x[i] - rank_y[i];
	    sum += di * di;
	}

	return 1. - (6. * sum) / (size * (size * size - 1));
    }
    */

    double computeSpearman() {
	int size = info_v.size();
	if (size == 1)
	    return 0.;

	double rank_x[] = compute_rank_2(true);
	double rank_y[] = compute_rank_2(false);

	// for R
	if (ECHO_R) {
	    System.out.print("x <- c(");
	    for (int i = 0; i < size; i++) {
		CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
		System.out.print((i > 0 ? "," : "") + item_info.xi);
	    }
	    System.out.println(")");

	    System.out.print("y <- c(");
	    for (int i = 0; i < size; i++) {
		CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
		System.out.print((i > 0 ? "," : "") + item_info.yi);
	    }
	    System.out.println(")");
	    System.out.println("cor(x, y, method=\"s\")");
	}

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    item_info.xi = rank_x[i];
	    item_info.yi = rank_y[i];
	}

	return computePearson();
    }

    double computePearson() {
	int size = info_v.size();
	if (size == 1)
	    return 0.;

	double x_sum = 0.;
	double y_sum = 0.;
	double x_y_sum = 0.;

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    x_sum += item_info.xi;
	    y_sum += item_info.yi;
	    x_y_sum += item_info.xi * item_info.yi;
	}

	double E_x_y = x_y_sum / size;
	double E_x = x_sum / size;
	double E_y = y_sum / size;

	double S_x = 0.;
	double S_y = 0.;

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    double x = item_info.xi - E_x;
	    S_x += x * x;
	    double y = item_info.yi - E_y;
	    S_y += y * y;
	}

	/*
	  S_x = S_x / (size-1);
	  S_y = S_y / (size-1);
	*/
	S_x = S_x / size;
	S_y = S_y / size;

	if (S_x == 0 || S_y == 0)
	    return Double.NaN;
	/*
	  System.out.println("x_sum:" + x_sum);
	  System.out.println("y_sum: " + y_sum);
	  System.out.println("x_y_sum: " + x_y_sum);
	  System.out.println("E_x_y: " + E_x_y);
	  System.out.println("E_x: " + E_x);
	  System.out.println("E_y: " + E_y);
	  System.out.println("S_x: " + S_x);
	  System.out.println("S_y: " + S_y);
	*/

	double pearson = (E_x_y - E_x * E_y) / Math.sqrt(S_x * S_y);
	if (pearson > 1.)
	    return 1.;

	if (pearson < -1.)
	    return -1.;

	return pearson;
    }

    double computePValue(double r0) {
	int size = info_v.size();
	if (size == 1)
	    return 0.;

	if (r0 == Double.NaN)
	    return 1.;

	r0 = Math.abs(r0);
	double x_sum = 0.;
	double y_sum = 0.;

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    x_sum += item_info.xi;
	    y_sum += item_info.yi;
	}

	double E_x = x_sum / size;
	double E_y = y_sum / size;

	double S_x = 0.;
	double S_y = 0.;

	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    double x = item_info.xi - E_x;
	    S_x += x * x;
	    double y = item_info.yi - E_y;
	    S_y += y * y;
	}

	S_x = S_x / size;
	S_y = S_y / size;

	if (S_x == 0 || S_y == 0)
	    return Double.NaN;

	double E_x_E_y = E_x * E_y;
	double sqrt_S_x_S_y = Math.sqrt(S_x * S_y);

	double xi[] = new double[size];
	double yi[] = new double[size];
	for (int i = 0; i < size; i++) {
	    CorrelationItemInfo item_info = (CorrelationItemInfo)info_v.get(i);
	    xi[i] = item_info.xi;
	    yi[i] = item_info.yi;
	}

	double xi_r[] = new double[size];

	int r_lt_r0_cnt = 0;
	int r_gt_r0_cnt = 0;

	int perm_count = getPermCount(size);
	//System.out.println("perm_count: " + perm_count);

	if (rand == null)
	    init();

	for (int n = 0; n < perm_count; n++) {
	    compute_perm(rand, xi, xi_r);
	    double x_y_sum = 0.;
	    for (int i = 0; i < size; i++)
		x_y_sum += xi_r[i] * yi[i];

	    double E_x_y = x_y_sum / size;

	    double r = (E_x_y - E_x_E_y) / sqrt_S_x_S_y;
	    r = Math.abs(r);
	    if (r < r0)
		r_lt_r0_cnt++;
	    else if (r > r0)
		r_gt_r0_cnt++;
	}

	/*
	  System.out.println("r_gt_r0 " + r_gt_r0_cnt + "/" + perm_count);
	  System.out.println("r_lt_r0 " + r_lt_r0_cnt + "/" + perm_count);
	  System.out.print("\nyi: ");
	  for (int n = 0; n < size; n++) {
	  System.out.print((n > 0 ? ", " : "") + yi[n]);
	  }
	  System.out.print("\nxi: ");
	  for (int n = 0; n < size; n++) {
	  System.out.print((n > 0 ? ", " : "") + xi[n]);
	  }
	  System.out.print("\n");
	*/

	return (double)r_gt_r0_cnt/perm_count;
    }

    static int getPermCount(int val) {
	int f = 1;
	for (int n = 2; n <= val; n++) {
	    f *= n;
	    if (f >= MAX_PERMS)
		return MAX_PERMS;
	}
	return f;
    }

    static void compute_perm(Random rand, double xi[], double xi_r[]) {
	int size = xi.length;

	for (int i = 0; i < size; i++)
	    xi_r[i] = Double.NaN;

	for (int i = 0; i < size; i++) {
	    for (;;) {
		int xi_ind = rand.nextInt(size);
		if (Double.isNaN(xi_r[xi_ind])) {
		    xi_r[xi_ind] = xi[i];
		    break;
		}
	    }		
	}

	// check
	for (int i = 0; i < size; i++) {
	    if (Double.isNaN(xi_r[i]))
		System.err.println("ERROR: xi_r " + i);
	}
    }
}
