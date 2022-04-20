
/*
 *
 * LicenseLib.java
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

class LicenseLib {

    static final int SUCCESS = 37;
    static final int WARNING_DATE = 39;
    static final int ERROR_DATE = 82;
    static final int INVALID_IP = 73;
    static final String HEADER = "vamp-granted?";
    static final boolean TRACE = false;

    static final String CHALLENGE_KEY = "?38#jka-";

    static byte buffer[] = new byte[1024];

    static String getIP() throws Exception {
	InetAddress i = java.net.InetAddress.getLocalHost();
	return i.getCanonicalHostName();
    }

   static String makeChallenge(byte challenge[]) throws Exception {

	MessageDigest md5 = MessageDigest.getInstance("MD5");

	if (TRACE) {
	    System.out.println("Making challenge for " + challenge.length + " bytes");
	    for (int i = 0; i < challenge.length; i++)
		System.out.print(challenge[i] + " ");
	    System.out.println("");
	}

	md5.update(challenge);

	byte b[] = md5.digest(CHALLENGE_KEY.getBytes());
	if (TRACE) {
	    System.out.println("obtains " + b.length + " bytes");
	    for (int i = 0; i < b.length; i++)
		System.out.print(b[i] + " ");
	    System.out.println("");
	}

	StringBuffer strb = new StringBuffer();
	for (int i = 0; i < b.length; i++)
	    strb.append(Integer.toHexString(0xFF & b[i]));

	return strb.toString();
    }

    static String readString(InputStream is) throws IOException {
	int n = is.read(buffer);
	if (n <= 0)
	    return null;

	for (int j = n-1; j >= 0; j--) {
	    if (buffer[j] != '\n' && buffer[j] != '\r')
		break;
	    n--;
	}

	byte b[] = new byte[n];
	System.arraycopy(buffer, 0, b, 0, n);
	String s =  new String(b);
	return s;
    }

    static byte [] readBytes(InputStream is) throws IOException {
	int n = is.read(buffer);
	if (n <= 0)
	    return null;


	byte b[] = new byte[n];
	System.arraycopy(buffer, 0, b, 0, n);
	return b;
    }

    static byte[] makeRandom() throws Exception {
	SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
	byte b[] = new byte[32];
	random.nextBytes(b);
	return b;
    }

}
