
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


class LicenseParams {

    // ----------- License Configuration ------------

    // set LICENSE_HOST to null to avoid connecting license server
    //static final String LICENSE_HOST = "10.20.11.28";
    static final String LICENSE_HOST = "10.2.0.39";
    static final int LICENSE_PORT = 7020;

    // set FILES_TO_REMOVES to null to avoid removing any file
    static final String FILES_TO_REMOVE[] = null;

    /*
    static final String FILES_TO_REMOVE[] = new String[] {
	"/tmp/testtttt.php"
    };
    */

    static final int WARNING_DAY = 31;
    static final int WARNING_MONTH = 12;
    static final int WARNING_YEAR = 2020;

    static final int ERROR_DAY = 31;
    static final int ERROR_MONTH = 12;
    static final int ERROR_YEAR = 2020;

    // set GRANTED_IPS_to null to avoid checking IP
    static final String GRANTED_IPS[] = null;

    /*
    static final String GRANTED_IPS[] = new String[] {
	"127.0.0.1",
	"algrave.curie.fr"
    };
    */
}
