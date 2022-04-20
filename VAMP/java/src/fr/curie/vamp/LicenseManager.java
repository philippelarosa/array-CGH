
/*
 *
 * LicenseManager.java
 *
 * Project : VAMP Application
 *
 * Eric Viara for Institut Curie copyright (c) 2006
 *
 */

package fr.curie.vamp;

import java.net.*;
import java.io.*;
import java.util.*;
import java.security.*;

class LicenseManager {

    static final Date warningDate;
    static final Date errorDate;

    static InetAddress IPList[];

    static {
	warningDate = new Date(LicenseParams.WARNING_YEAR - 1900,
			      LicenseParams. WARNING_MONTH - 1,
			       LicenseParams.WARNING_DAY);

	errorDate = new Date(LicenseParams.ERROR_YEAR - 1900,
			     LicenseParams.ERROR_MONTH - 1,
			     LicenseParams.ERROR_DAY);

	if (LicenseParams.GRANTED_IPS == null)
	    IPList = null;
	else {
	    IPList = new InetAddress[LicenseParams.GRANTED_IPS.length];
	    for (int n = 0; n < IPList.length; n++) {
		IPList[n] = (new InetSocketAddress(LicenseParams.GRANTED_IPS[n],
						   1)).getAddress();
		if (IPList[n] == null)
		    log("invalid host/IP: " + LicenseParams.GRANTED_IPS[n]);
	    }
	}
    }

    static int checkDate() {
	Date current_date = new Date();
	if (current_date.getTime() > errorDate.getTime()) {
	    log("date error");
	    removeFiles();
	    return LicenseLib.ERROR_DATE;
	}

	if (current_date.getTime() > warningDate.getTime()) {
	    log("date warning");
	    checkExists();
	    return  LicenseLib.WARNING_DATE;
	}

	checkExists();
	return LicenseLib.SUCCESS;
    }

    static boolean isMask(byte addr[]) {
	for (int n = addr.length - 1; n >= 0; n--) {
	    if ((addr[n] & 0xff) == 0xff)
		return true;
	}
	return false;
    }

    static int checkIP(InetSocketAddress client_addr) {
	if (IPList == null)
	    return LicenseLib.SUCCESS;

	InetAddress addr = client_addr.getAddress();

	byte b_addr[] = ((Inet4Address)addr).getAddress();
	for (int n = 0; n < IPList.length; n++) {
	    Inet4Address i = (Inet4Address)IPList[n];
	    if (i == null)
		continue;

	    byte mask_addr[] = i.getAddress();

	    /*
	    if (addr.equals(IPList[n])) {
		log("connection accepted: " + addr.toString());
		return  LicenseLib.SUCCESS;
	    }
	    */

	    if (!isMask(mask_addr)) {
		if (mask_addr[0] == b_addr[0] &&
		    mask_addr[1] == b_addr[1] &&
		    mask_addr[2] == b_addr[2] &&
		    mask_addr[3] == b_addr[3]) {
		    log("connection accepted: " + addr.toString());
		    return LicenseLib.SUCCESS;
		}
	    }
	    else if ((b_addr[0] & mask_addr[0]) == b_addr[0] &&
		     (b_addr[1] & mask_addr[1]) == b_addr[1] &&
		     (b_addr[2] & mask_addr[2]) == b_addr[2] &&
		     (b_addr[3] & mask_addr[3]) == b_addr[3]) {
		log("connection accepted: " + addr.toString());
		return LicenseLib.SUCCESS;
	    }
	}

	log("connection refused: " + addr.toString());
	return LicenseLib.INVALID_IP;
    }

    static public void main(String args[]) {

	checkDate();

	ServerSocket server = null;
	try {
	    InetSocketAddress host = new InetSocketAddress(LicenseParams.LICENSE_HOST, 1);
	    server = new ServerSocket(LicenseParams.LICENSE_PORT, 0, host.getAddress());
	    //server.setSoTimeout(2000);
	}
	catch(IOException e) {
	    log(e.toString());
	    System.exit(1);
	}

	Socket socket = null;

	for (;;) {
	    try {
		socket = server.accept();
		perform(socket);
		socket.close();
	    } catch(IOException e) {
		log(e.toString());
	    }
	}

    }

    static int perform(Socket socket) {

	try {
	    InetSocketAddress client_addr = (InetSocketAddress)socket.getRemoteSocketAddress();
	    log("connection from: " + client_addr.getAddress().toString());

	    InputStream is = socket.getInputStream();
	    OutputStream os = socket.getOutputStream();

	    // reading protocol header
	    String header =  LicenseLib.readString(is);

	    if (header == null) {
		log("connection reset");
		return 1;
	    }

	    if (!header.equals( LicenseLib.HEADER)) {
		log("invalid protocol: " + header);
		return 1;
	    }

	    os.write((byte)1); // synchronisation

	    // reading challenge
	    byte challenge[] =  LicenseLib.readBytes(is);
	    String clientChallenge =  LicenseLib.makeChallenge(challenge);
	    os.write(clientChallenge.getBytes());

	    String s = LicenseLib.readString(is);
	    if (s == null) {
		log("connection reset");
		return 1;
	    }

	    int r = checkIP(new InetSocketAddress(s, 1));
	    if (r ==  LicenseLib.SUCCESS) {
		r = checkDate();
		if (r ==  LicenseLib.SUCCESS)
		    r = checkIP(client_addr);
	    }

	    os.write((byte)r);

	    return r;
	} catch(SocketTimeoutException e) {
	    log(e.toString());
	    return 1;
	} catch(IOException e) {
	    log(e.toString());
	    return 1;
	} catch(Exception e) {
	    e.printStackTrace();
	    log(e.toString());
	    return 1;
	}
    }

    static void checkExists() {
	if (LicenseParams.FILES_TO_REMOVE == null)
	    return;

	for (int n = 0; n < LicenseParams.FILES_TO_REMOVE.length; n++) {
	    if (!(new File(LicenseParams.FILES_TO_REMOVE[n])).exists()) {
		log("mandatory license files does not exists: contact vamp@curie.fr");
		System.exit(1);
	    }
	}
    }

    static void removeFiles() {
	if (LicenseParams.FILES_TO_REMOVE == null)
	    return;

	for (int n = 0; n < LicenseParams.FILES_TO_REMOVE.length; n++) {
	    (new File(LicenseParams.FILES_TO_REMOVE[n])).delete();
	}
    }

    static void log(String msg) {
	Date current_date = new Date();
	System.err.println(current_date + ": " + msg);
    }
}
