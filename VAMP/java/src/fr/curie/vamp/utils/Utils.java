
package fr.curie.vamp.utils;

import java.io.*;

public class Utils {

    static Runtime runtime = Runtime.getRuntime();

    public static String KB(long mem) {
	return mem/1024 + " Kb";
    }

    public static void dumpMemory(String msg) {
	runtime.gc();
	System.out.println("\n" + msg);
	System.out.println("max memory: " + KB(runtime.maxMemory()));
	System.out.println("total memory: " + KB(runtime.totalMemory()));
	System.out.println("free memory: " + KB(runtime.freeMemory()));
	System.out.println("busy memory: " + KB(runtime.totalMemory() - runtime.freeMemory()));
    }

    public static long busyMemory() {
	runtime.gc();
	return runtime.totalMemory() - runtime.freeMemory();
    }
}

