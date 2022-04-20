
package fr.curie.vamp.utils;

import java.io.*;
import java.util.*;

public class FileInputManager {

    private int fisMax;
    private static FileInputManager instance;
    private HashMap<String, FIS> fis_map = new HashMap();
    private static long LAST_REFCNT = 0;

    private static boolean VERBOSE = false;
    private static int DEFAULT_OPENED_FILES = 256;
    //private static int DEFAULT_OPENED_FILES = 512;

    class FIS {
	long lastRefCnt;
	FileInputStream fis;
	ObjectInputStream ois;
	String name;

	FIS(FileInputStream fis, String name) throws IOException {
	    this.fis = fis;
	    ois = null;
	    this.name = name;
	    touch();
	    //System.out.println("FIS: " + this + " " + fis);
	}

	void close() throws IOException {
	    //System.out.println("Closing: " + this + " " + fis);
	    if (ois != null) {
		ois.close();
		ois = null;
	    }
	    fis.close();
	    fis = null;
	}

	FileInputStream getFIS() {
	    touch();
	    return fis;
	}

	ObjectInputStream getOIS() throws IOException {
	    try {
		fis.getChannel().position(0);
		ois = new ObjectInputStream(fis);
		return ois;
	    }
	    catch(IOException e) {
		System.out.println("getOIS: " + name + " " + fis.getChannel().size());
		throw e;
	    }
	}

	private void touch() {
	    lastRefCnt = ++LAST_REFCNT;
	}
    }

    private FileInputManager(int fisMax) {
	this.fisMax = fisMax;
    }

    public static FileInputManager getInstance() {
	if (instance == null) {
	    instance = new FileInputManager(DEFAULT_OPENED_FILES);
	}

	return instance;
    }

    public synchronized ObjectInputStream getOIS(String name, int id) {
	FIS fis = fis_map.get(makeKey(name, id));
	if (fis == null) {
	    getFIS(name, id);
	    fis = fis_map.get(makeKey(name, id));
	    if (fis == null) {
		return null;
	    }
	}

	try {
	    return fis.getOIS();
	}
	catch(IOException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public synchronized FileInputStream getFIS(String name, int id) {
	FIS fis = fis_map.get(makeKey(name, id));
	if (fis != null) {
	    return fis.getFIS();
	}

	if (VERBOSE) {
	    System.out.println(name + "[" + id + "] not opened");
	}

	if (fis_map.size() >= fisMax) {
	    long min = Long.MAX_VALUE;
	    String rm_key = null;
	    FIS rm_fis = null;

	    Iterator it = fis_map.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry<String, FIS> entry = (Map.Entry)it.next();
		if (entry.getValue().lastRefCnt < min) {
		    min = entry.getValue().lastRefCnt;
		    rm_key = entry.getKey();
		    rm_fis = entry.getValue();
		}
	    }

	    fis_map.remove(rm_key);
	    try {
		rm_fis.close();
	    }
	    catch(IOException e) {
		e.printStackTrace();
	    }
	    if (VERBOSE) {
		System.out.println(rm_key + " removed " + min + " " + LAST_REFCNT);
	    }
	}

	try {
	    FileInputStream is = new FileInputStream(name);
	    fis_map.put(makeKey(name, id), new FIS(is, name));
	    return is;
	}
	catch(IOException e) {
	    System.out.println("fis_map: " + fis_map.size());
	    e.printStackTrace();
	    return null;
	}
    }

    public synchronized void close(String name, int id) throws Exception {
	// EV: disconnected 25/7/08
	/*
	if (true) {
	    return;
	}
	*/

	FIS fis = fis_map.get(makeKey(name, id));
	if (fis != null) {
	    fis.close();
	    fis_map.remove(makeKey(name, id));
	}
    }

    public synchronized void clear() {
	Iterator it = fis_map.entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry<String, FIS> entry = (Map.Entry)it.next();
	    try {
		entry.getValue().close();
	    }
	    catch(IOException e) {
		e.printStackTrace();
	    }
	}

	fis_map.clear();
    }

    private String makeKey(String name, int id) {
	return name + "::" + id;
    }
}
