
/*
 *
 * AlgorithmsTest.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2007
 *
 */

package fr.curie.vamp;

import java.io.*;
import java.util.*;

class AlgorithmsTest {

    static final int START_COL = 5;

    public static void main(String args[]) {
	if (args.length != 1 && args.length != 2) {
	    System.err.println("usage: AlgorithmsTest <file> [verbose]");
	    System.exit(1);
	}

	try {
	    BufferedReader in = new BufferedReader(new FileReader(args[0]));
	    Vector line_v = new Vector();
	    int nbrarrays = 0;
	    int nbrregions = 0;
	    for (;;) {
		String line = in.readLine();
		if (line == null)
		    break;
		String split[] = line.split("\t");
		int c = split.length;
		line_v.add(split);
		if (nbrarrays != 0 && c != nbrarrays) {
		    System.err.println("invalid columns number (" +
				       nbrarrays + " vs. " + c + ") at line #" +
				       nbrregions);
		    System.exit(1);
		}
		nbrarrays = c;
		nbrregions++;
	    }

	    nbrarrays -= START_COLL; //N
	    nbrregions--; //N
	    int alldata[][] = new int[nbrregions][nbrarrays];

	    for (int i = 0; i < nbrregions; i++) { //N
		String values[] = (String[])line_v.get(i+1); //N
		for (int j = START_COL; j < values.length; j++) {
		    if (values[j].equals("NA"))
			alldata[i][j-START_COL] = 0;
		    else if (values[j].equals("-1"))
			alldata[i][j-START_COL] = 0;
		    else
			alldata[i][j-START_COL] = Integer.parseInt(values[j]);
		}
	    }
	    BFAssoAlgorithm.verbose = args.length == 2;
	    double dataassoc[][] = BFAssoAlgorithm.algo(BFAssoAlgorithm.CO_OCCURENCE, alldata);
	    int cpt=0;
	    for (int i = 0; i < dataassoc.length; i++) {
		for (int j = 0; j < dataassoc[i].length; j++) {
		    //if (dataassoc[i][j] != 1.)
		    if (dataassoc[i][j] <= 0.05){
			System.out.println("dataassoc[" + i + ", " + j + "] = " +
					   dataassoc[i][j]);
			cpt++;
		    }
		}
	    }
	System.out.println("Number of associations detected = " + cpt/2);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
