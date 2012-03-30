package com.akoolla.commons.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.akoolla.commons.config.EnvironmentSettings.Platform;
import com.akoolla.commons.config.EnvironmentSettings.Profile;

/**
 * Environment.
 * 
 * @author tiffir
 * @version $Id: Environment.java 144 2011-03-15 16:36:54Z tiffir $
 */
public class Environment {
	
    /** The logger for this class. **/
    private static Log log = LogFactory.getLog(Environment.class);

    // private static final Logger logger = Logger.getLogger(Environment.class);

    public static EnvironmentSettings load() {

        Platform platform = null;

        String serverEnv = getSystemProperty("SERVER_ENV");

        if (serverEnv != null) {
            try {
                platform = EnvironmentSettings.Platform.valueOf(serverEnv.toUpperCase());
            } catch (IllegalArgumentException e) {
                // logger.warn("System property 'SERVER_ENV'  is not recognised [" + serverEnv + "]");
                platform = EnvironmentSettings.Platform.UKNOWN;
            }
        } else {
            platform = EnvironmentSettings.Platform.DEV;
            serverEnv = platform.toString().toLowerCase();
        }

        Profile profile = null;

        /*
         * try and use the value from the system or the environment. if no value is found then default to small, if
         * something is found, but isn't recognised then use UNKOWN
         */

        String systemProfile = getSystemProperty("SERVER_PROFILE");

        if (systemProfile != null) {
            try {
                profile = EnvironmentSettings.Profile.valueOf(systemProfile.toUpperCase());
            } catch (IllegalArgumentException e) {
                // logger.warn("System property 'SERVER_PROFILE'  is not valid [" + systemProfile + "]");
                profile = EnvironmentSettings.Profile.UKNOWN;
            }
        } else {
            profile = EnvironmentSettings.Profile.SMALL;
        }

        EnvironmentSettings name = new EnvironmentSettings(platform, profile, serverEnv);

        return name;
    }

    /**
     * tries to get the value of SERVER_ENV from the system's properties, then from the environment or returns null
     */
    public static String getSystemProperty(String name, boolean upper) {
        if (log.isInfoEnabled()) {
        	log.info("name: " + name + ", uppercase: " + upper);
        }
    	String prop = System.getProperty(name);

        if ((prop != null) && (prop.length() > 0)) {
            if (log.isInfoEnabled()) {
            	log.info("  system: " + (upper ? prop.toUpperCase() : prop));
            }
            return upper ? prop.toUpperCase() : prop;
        }

        String env = System.getenv(name);

        if ((env != null) && (env.length() > 0)) {
            if (log.isInfoEnabled()) {
            	log.info("  env: " + (upper ? env.toUpperCase() : env));
            }
            return upper ? env.toUpperCase() : env;
        }
        return null;
    }

    /**
     * tries to get the value of SERVER_ENV from the system's properties, then from the environment or returns null
     */
    public static String getSystemProperty(String name) {
    	return getSystemProperty(name, true);
    }
}
