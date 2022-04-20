
/*
 *
 * LicenseClient.java
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

class LicenseClient extends Thread {

    static final String contact_msg = "Please contact vamp@cubism.fr";

    static final int CONNECTION_TIMEOUT = 4; // in seconds

    class CheckConnection extends Thread {

	private LicenseClient client;
	private GlobalContext globalContext;
	private String host, port;

	CheckConnection(GlobalContext globalContext, LicenseClient client,
			String host, String port) {
	    this.client = client;
	    this.globalContext = globalContext;
	    this.host = host;
	    this.port = port;
	}

	public void run() {
	    try {
		Thread.sleep(CONNECTION_TIMEOUT * 1000);

		if (!client.isFailed() && !client.isConnectionEstablish()) {
		    client.setTimedOut();
		    InfoDialog.pop(globalContext,
				   "Connection timeout to license server on host " +
				   host + ", port " + port);
		    client.interrupt();
		}
	    }
	    catch(Exception e) {
		e.printStackTrace();
	    }

	}
    }

    private boolean connEstablish = false;
    private boolean timedout = false;
    private boolean failed = false;
    private boolean ok = false;
    private GlobalContext globalContext;
    private SystemConfig sysCfg;
    private boolean licenseOn;

    synchronized boolean isConnectionEstablish() {
	return connEstablish;
    }

    synchronized void setConnectionEstablish() {
	connEstablish = true;
    }

    synchronized void setTimedOut() {
	timedout = true;
    }

    synchronized boolean isTimedOut() {
	return timedout;
    }

    synchronized void setFailed() {
	failed = true;
    }

    synchronized boolean isFailed() {
	return failed;
    }

    synchronized boolean isOK() {
	return ok;
    }

    LicenseClient(GlobalContext globalContext,
		  SystemConfig sysCfg, boolean licenseOn) {
	this.globalContext = globalContext;
	this.sysCfg = sysCfg;
	this.licenseOn = licenseOn;
    }

    public void run() {
	if (!licenseOn) {
	    ok = true;
	    return;
	}

	String host = sysCfg.getParameter("LicenseHost");
	String port = sysCfg.getParameter("LicensePort");

	if (host == null) {
	    InfoDialog.pop(globalContext,
			   "LicenseHost is not set in syscfg.xml");
	    
	    return;
	}

	Socket socket = null;
	try {
	    System.err.println("Trying to connect to license server on " +
			       host + ":" + port);
	    CheckConnection checkConnection = new CheckConnection
		(globalContext, this, host, port);
	    checkConnection.start();
	    socket = new Socket(host, Integer.parseInt(port));
	    if (isTimedOut())
		return;
	    setConnectionEstablish();
	    System.err.println("Connection established");
	}
	catch(Exception e) {
	    setFailed();
	    if (!isTimedOut())
		InfoDialog.pop(globalContext,
			       "Connection refused to license server on host " +
			       host + ", port " + port);
	    return;
	}
	
	try {
	    InputStream is = socket.getInputStream();
	    OutputStream os = socket.getOutputStream();
	    os.write(LicenseLib.HEADER.getBytes());
	    LicenseLib.readString(is);
	    byte random[] = LicenseLib.makeRandom();
	    os.write(random);

	    String chal = LicenseLib.makeChallenge(random);
	    String rchal = LicenseLib.readString(is);

	    if (!rchal.equals(chal)) {
		InfoDialog.pop(globalContext, "Invalid challenge: untrusted server");
		System.err.println(chal + " vs. " + rchal);
		return;
	    }

	    os.write(LicenseLib.getIP().getBytes());

	    byte rb[] = LicenseLib.readBytes(is);
	    if (rb.length == 1) {
		byte b = rb[0];
		if (b == LicenseLib.SUCCESS) {
		    ok = true;
		    return;
		}

		if (b == LicenseLib.WARNING_DATE) {
		    /*
		    InfoDialog.pop(globalContext,
				   "Your license will expired the " +
				   LicenseParams.ERROR_DAY + "/" +
				   LicenseParams.ERROR_MONTH + "/" +
				   LicenseParams.ERROR_YEAR + ". " +
				   contact_msg);
		    */
		    InfoDialog.pop(globalContext,
				   "Your license will expired soon. " +
				   contact_msg);
		    ok = true;
		    return;
		}
		else if (b == LicenseLib.ERROR_DATE)
		    InfoDialog.pop(globalContext, "Your license expired. " +
				   contact_msg);
		else if (b == LicenseLib.INVALID_IP)
		    InfoDialog.pop(globalContext, "Your IP address is invalid. "
				   + contact_msg);
	    }
	    else
		InfoDialog.pop(globalContext, "Internal error. " + contact_msg);
	}
	catch(Exception e) {
	    InfoDialog.pop(globalContext, e.getMessage() + ". " + contact_msg);
	}

	return;
    }

    static boolean checkLicense(GlobalContext globalContext,
				SystemConfig sysCfg, boolean licenseOn) {
	try {
	    LicenseClient client = new LicenseClient(globalContext, sysCfg, licenseOn);
	    client.start();
	    client.join();
	    return client.isOK();
	}
	catch(Exception e) {
	    e.printStackTrace();
	    return false;
	}
    }
}
