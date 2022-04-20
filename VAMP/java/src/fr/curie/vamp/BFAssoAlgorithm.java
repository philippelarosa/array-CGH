
/*
 *
 * BFAssoAlgorithm.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2005
 *
 * Modification Nicolas Servant 18/07/05
 *
 */

package fr.curie.vamp;

import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.math.BigDecimal;

class BFAssoAlgorithm {

    static private PrintStream out = System.out;
    static private boolean verbose = false;

    static final int CO_OCCURENCE = 1;
    static final int EXCLUSION = 2;

    static void setPrintStream(PrintStream out) {
	BFAssoAlgorithm.out = out;
	verbose = (out != null);
    }

    static int[][] createContingencyTable(int data1[], int data2[]) {
	int tab[][] = new int[2][2];

	tab[0][0] = 0;
	tab[0][1] = 0;
	tab[1][0] = 0;
	tab[1][1] = 0;

	for (int n = 0; n < data1.length; n++) {
	    if (data1[n] == 0 && data2[n] == 0)
		tab[0][0]++;
	    else if (data1[n] == 1 && data2[n] == 1)
		tab[1][1]++;
	    else if (data1[n] == 0 && data2[n] == 1)
		tab[0][1]++; //N
	    else if (data1[n] == 1 && data2[n] == 0)
		tab[1][0]++; //N
	}

	return tab;
    }


   
    static int[][][] createAllContingencyTable(int N, int H1, int V1) {
	int ppv = min(H1, V1);
	ppv = min(N-H1, ppv); 
	ppv = min(N-V1, ppv);

	int t[][][] = new int[ppv+1][2][2];

	int a, b, c, d;

	a = H1;
	b = 0;
	c = V1-H1;
	d = N-V1;

	int j = 0;

	while (j <= ppv) {
	    if (a >= 0 && b >= 0 && c >= 0 && d >= 0) {
		 t[j][0][0] = a;
		 t[j][0][1] = b;
		 t[j][1][0] = c;
		 t[j][1][1] = d;
		 j++;
	    }
	    a--;b++;c++;d--;
	    
	    if (verbose)
		out.println(a + " " + b + " " + c + " " + " " + d);
	   
	}
	
	/*
	  for (int i = H1, j = 0; i >= H1-ppv; i--, j++)
	  t[j][0][0] = i;
	  
	  for (int i = 0, j = 0; i <= ppv; i++, j++)
	  t[j][0][1] = i;
	  
	  for (int i = V1-H1, j = 0; i <= V1-H1+ppv; i++, j++)
	  t[j][1][0] = i;
	  
	  for (int i = N-V1, j = 0; i >= N-V1-ppv; i--, j++)
	  t[j][1][1] = i;
	*/

	return t;
    }

    static int min(int N1, int N2) {
	return N1 < N2 ? N1 : N2;
    }

    static long fact(int val) {
	long f = 1;
	for (int n = 2; n <= val; n++) 
	    f *= n;
	return f;
    }

    static BigInteger fact(BigInteger val_b) {
	BigInteger f_b = BigInteger.valueOf(1);
	int val = val_b.intValue();
	for (int n = 2; n <= val; n++) 
	    f_b = BigInteger.valueOf(n).multiply(f_b);
	return f_b;
    }

    static double dhypgeo(int tab[][]) {
	int H1 = sigma(tab[0]);
	int H2 = sigma(tab[1]);
	int V1 = 0;
	for (int k = 0; k < 2; k++)
	    V1 += tab[k][0];
	int V2 = 0;
	for (int k = 0; k < 2; k++)
	    V2 += tab[k][1];
	int N = H1 + H2;
	
	BigInteger a_bi = BigInteger.valueOf(tab[0][0]);
	BigInteger b_bi = BigInteger.valueOf(tab[0][1]);
	BigInteger c_bi = BigInteger.valueOf(tab[1][0]);
	BigInteger d_bi = BigInteger.valueOf(tab[1][1]);

	BigInteger H1_bi = BigInteger.valueOf(H1);
	BigInteger H2_bi = BigInteger.valueOf(H2);
	BigInteger V1_bi = BigInteger.valueOf(V1);
	BigInteger V2_bi = BigInteger.valueOf(V2);
	BigInteger N_bi = BigInteger.valueOf(N);

	BigInteger N_a_b_c_d = fact(N_bi).multiply(fact(a_bi)).
	    multiply(fact(b_bi)).multiply(fact(c_bi)).multiply(fact(d_bi));

	BigInteger H1_H2_V1_V2 = fact(H1_bi).multiply(fact(H2_bi)).
	    multiply(fact(V1_bi)).multiply(fact(V2_bi));

	BigDecimal num = new BigDecimal(H1_H2_V1_V2, 8);
	BigDecimal den = new BigDecimal(N_a_b_c_d, 8);
	
	if (verbose) {
	    double r = num.divide(den, BigDecimal.ROUND_UP).doubleValue();
	    out.println("dhypeo: " + r);
	}
	
	return num.divide(den, BigDecimal.ROUND_UP).doubleValue();
    }

    static double fisherTest(int tab_init[][], int all_tab[][][],
			     int analysis) {
	double p = 0;
	int ref = tab_init[1][1];
	for (int i = 0; i < all_tab.length; i++) {
	    if (analysis == CO_OCCURENCE && all_tab[i][1][1] >= ref)
		p += dhypgeo(all_tab[i]);
	    else if (analysis == EXCLUSION && all_tab[i][1][1] <= ref)
		p += dhypgeo(all_tab[i]);
	}

	return p;
    }

    static int sigma(int data[]) {
	int sum = 0;
	for (int i = 0; i < data.length; i++)
	    sum += data[i];
	return sum;
    }

    
    static double sigma(double data[]) {
	double sum = 0;
	for (int i = 0; i < data.length; i++)
	    sum += data[i];
	return sum;
    }
    
    static void appGausCooccurence(double p[], int alldata[][]) {
	if (verbose)
	    out.println("appGausCoocurence:");
	for (int i = 0; i < p.length; i++) {
	    double sum = 0;
	    for (int j = 0; j < alldata.length; j++)
		if (alldata[j][i]==1)
		    sum++;

	    //sum += alldata[j][i];  //N
	    p[i] = (sum/alldata.length)*((sum-1)/(alldata.length-1));
	    if (verbose)
		out.println("p[" + i + "] = " +p[i]);
	}
    }	

    static void appGausExclusion(double p[], int alldata[][]) {
	for (int i = 0; i < p.length; i++) {
	    double sum = 0;
	    for (int j = 0; j < alldata.length; j++)
		if (alldata[j][i]==1)
		    sum++;

	    //sum += alldata[j][i]; //N
	    p[i] = 2*(sum/alldata.length)*(1-(sum/alldata.length)); //N
	    if (verbose)
		out.println("p[" + i + "] = " +p[i]);
	}
    }	

    static double[] appGaus(int analysis, int alldata[][]) {
	double p[] = new double[alldata[0].length];
	if (analysis == CO_OCCURENCE)
	    appGausCooccurence(p, alldata);
	else if (analysis == EXCLUSION)
	    appGausExclusion(p, alldata);

	double Ex[] = new double[2];
	Ex[0] = sigma(p);
	double sum = 0;
	for (int i = 0; i < alldata[0].length; i++)
	    sum += p[i]*(1-p[i]);
	Ex[1] = Math.sqrt(sum);

	return Ex;
    }

    static Object[] [][] algo(int analysis, int alldata[][]) {
	double Ex[] = appGaus(analysis, alldata);
	double E = Ex[0];
	double ET = Ex[1];

	int nbrregion = alldata.length;
	
	if (verbose) {
	    out.println("Region Count:" + nbrregion);
	    out.println("Array Count:" + alldata[0].length);
	    out.println("E: " + E);
	    out.println("ET: " + ET);
	}
	
	Object dataassoc[][][] = new Object[nbrregion][nbrregion][];
	for (int i = 0; i < nbrregion; i++)
	    for (int j = 0; j < nbrregion; j++)
		dataassoc[i][j] = new Object[]{new Double(1.), null};

	for (int i = 0; i < nbrregion; i++) {
	    for (int j = i+1; j < nbrregion; j++) {
		int tab_init[][] = createContingencyTable(alldata[i], alldata[j]);
		
		  if (verbose)
		    out.println("tab_init[" + i + "][" + j + "] = " +
				       tab_init[0][0] + ", " +
				       tab_init[0][1] + ", " +
				       tab_init[1][0] + ", " +
				       tab_init[1][1]);
		
		double nobs =  0;
		if (analysis == CO_OCCURENCE)
		    nobs = tab_init[1][1];
		else if (analysis == EXCLUSION)
		    nobs = tab_init[1][0] + tab_init[0][1];
		
		if (verbose)
		    out.println("nobs: " + nobs);
		
		if (nobs > E + 2 * ET) {
		    
		    if (verbose)
			out.println("nobs > E + 2 * ET");
		    
		    int H1 = sigma(tab_init[0]);
		    int H2 = sigma(tab_init[1]);
		    int V1 = 0;
		    for (int k = 0; k < 2; k++)
			V1 += tab_init[k][0];
		    int N = H1 + H2;
		    
		    if (verbose)
			out.println("H1: " + H1 + ", H2: " + H2 +
					   ", V1: " + V1);
		    
		    int all_tab[][][] = createAllContingencyTable(N, H1, V1);
		    
		    if (verbose) {
			out.println("all_tab -> " + all_tab.length);
			for (int k = 0; k < all_tab.length; k++)
			    out.println("all_tab[" + k + "] = " +
					       all_tab[k][0][0] + ", " +
					       all_tab[k][0][1] + ", " +
					       all_tab[k][1][0] + ", " +
					       all_tab[k][1][1]);
		    }
		    
		    double pvalue = fisherTest(tab_init, all_tab, analysis);
		    if (verbose) {
			if (pvalue < 0.05)
			    out.println("pvalue[" + i + ", " + j + "]: " + pvalue);
		    }

		    // ajouter all_tab dans dataassoc
		    //dataassoc[i][j] = dataassoc[j][i] = pvalue;
		    dataassoc[i][j] = dataassoc[j][i] =
			new Object[]{new Double(pvalue), tab_init};
		    
		    if (verbose)
			out.println("");   
		    
		}
		else {
		    if (verbose)
			out.println("nobs <= E + 2 * ET");
		}
	    }
	}
	return dataassoc;
    }
}
