package com.akoolla.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PropertiesMuncher.
 * 
 * @author tiffir
 * @version $Id: PropertiesMuncher.java 111 2011-03-14 11:22:01Z tiffir $
 */
public class PropertiesMuncher {
	
    /** The logger for this class. **/
    private static Log log = LogFactory.getLog(PropertiesMuncher.class);

    // private static final Log log = Log.getLogger(PropertiesMuncher.class);
    static String globalProperties = "config/environment.properties";
    static String globalPropertiesDir = "config/environments/";
    static String appDataDirProperty = "app.datadir";
    static String SERVER_CONFIG = "SERVER_CONFIG";

    private String parentPackage = "";
    private String parentPath = "";

    /**
     * Obtain the environment based properties from the classpath
     * 
     * @return
     */
    public Properties munchClassPathBasedProperties() {
        return munch(getParentPackage(), true, getGlobalProperties(), getGlobalPropertiesDir());
    }

    /**
     * Obtain the environment based properties from the file system
     * 
     * @return
     */
    public Properties munchFileSystemBasedProperties() {
        return munch(getParentPath(), false, getGlobalProperties(), getGlobalPropertiesDir());
    }

    /**
     * Reads the properties files, global and environment specific properties, from either the classpath or filesystem
     * 
     * @param parent The parent file path or package from which to find 'config/environment.properties' and
     *        config/environments/
     * @param classpath Are we obtaining the properties from the filesystem or the classpath
     * @param globalProps The name of the global environment.properties to read within the specified parent parameter
     * @param globalPropsEnvDir The name of the directory with in the parent parameter specified directory, which
     *        contains the environment specific properties files.
     * @return
     */
    public static Properties munch(String parent, boolean classpath, String globalProps, String globalPropsEnvDir) {

        Properties global = null;
        if (classpath) {
            global = munchFromClasspath(parent + globalProps);
        } else {
            global = munchFromFile(new File(parent), globalProps);
        }

        EnvironmentSettings settings = Environment.load();
        String currentServerEnv = settings.getPlatform().toString().toLowerCase();

        try {

            String localFileName = globalPropsEnvDir + currentServerEnv + ".properties";
            Properties local = null;
            if (classpath) {
                local = munchFromClasspath(parent + localFileName);
            } else {
                local = munchFromFile(new File(parent), localFileName);

            }

            Properties result = strictMergeProperties(local, global);

            result = getAppDataDirPropertyOverrides(result, currentServerEnv);

            return result;

        } catch (UnmatchedPropertyException e) {

            // this should be caught during development
            throw e;

        } catch (RuntimeException e) {
            // logger.warn("No platform-specfic properties found", e);
        }

        return global;

    }

    /**
     * Looks for a file in the specified directory. If the directory is empty then the method looks in the classpath
     * instead.
     * 
     * @param filename
     * @param directory
     * @return
     */
    public static Properties munch(final File directory, final String filename) {
        if ((directory == null) || !directory.exists() || (filename == null)
                || !(new File(directory, filename)).exists()) {
            return munchFromClasspath(filename);
        } else {
            return munchFromFile(directory, filename);
        }
    }

    public static Properties munch() {

        Properties global = munchFromClasspath(getGlobalProperties());

        EnvironmentSettings settings = Environment.load();
        String currentServerEnv = settings.SERVER_ENV.toLowerCase();

        try {

            Properties local = munchFromClasspath(getGlobalPropertiesDir() + currentServerEnv + ".properties");

            Properties result = strictMergeProperties(local, global);

            result = getAppDataDirPropertyOverrides(result, currentServerEnv);

            return result;

        } catch (UnmatchedPropertyException e) {

            // this should be caught during development
            throw e;

        } catch (RuntimeException e) {

            // logger.warn("No platform-specfic properties found", e);

        }

        return global;

    }

    /**
     * Checks if an override directory has been specified in the given properties. If an override directory has been
     * specified, a search will be performed in this override directory for a matching environmental based properties
     * file. e.g. if we are running on the SERVER_ENV=int; and in either: config/environment.properties
     * config/environments/int.properties The property "app.datadir" has been specified, then the path in this property
     * value will be searched for an environmental property file. e.g: app.datadir=/data/myapp/ops-overrides Then a
     * search will be made for /data/myapp/ops-overrides/config/environments/int.properties Values in this file will
     * override any other properties
     */
    private static Properties getAppDataDirPropertyOverrides(Properties currentMergedProperties,
                                                             String serverEnv) {
        String dataDir = currentMergedProperties.getProperty(appDataDirProperty);
        
        if (dataDir != null && dataDir.equals(SERVER_CONFIG)) {
        	dataDir = Environment.getSystemProperty(SERVER_CONFIG, false);
        }

        if ((dataDir != null) && exists(dataDir)) {
            // Check for overridden global property file i.e. environment.properties
            File overriddenGlobalProps = new File(dataDir, getGlobalProperties());
            if (overriddenGlobalProps.exists()) {
                Properties data = munchFromFile(new File(dataDir), getGlobalProperties(), false);
                currentMergedProperties = strictMergeProperties(data, currentMergedProperties);
            }

            Properties data = munchFromFile(new File(dataDir), getGlobalPropertiesDir() + serverEnv + ".properties",
                    false);

            currentMergedProperties = strictMergeProperties(data, currentMergedProperties);

        }

        return currentMergedProperties;
    }
    
    private static boolean exists(String fileName) {
    	try {
    		if (log.isInfoEnabled()) {
    			log.info("PropertiesMuncher root directory:" + fileName);
    		}
    		boolean fileExists = new File(fileName).exists();
    		if (log.isInfoEnabled()) {
    			log.info("  and file exists: " + fileExists);
    		}    		
    		return fileExists;
    	}
    	catch (Exception e) {
    		log.error("PropertiesMuncher external properties failed with security exception", e);
    		return false;
    	}
    }

    /**
     * Basically looks for the given properties filename on the classpath, and returns a Properties object for that
     * file.
     * 
     * @param filename the name of the file to find in the classpath. Could be: config/environment.properties or
     *        config/environments/int.properties
     * @return The properties object containing the properties read from the given file.
     */
    public static Properties munchFromClasspath(final String filename) {

        // if (logger.isDebugEnabled())
        // logger.debug("Munching properties from classpath using filename : " + filename);

        if (filename == null) {
            throw new IllegalArgumentException("Cannot supply null parameter : filename");
        }

        try {

            Properties prop = new Properties();

            try {

                prop = load(PropertiesMuncher.class.getClassLoader().getResourceAsStream(filename));

            } catch (NullPointerException e) {
                try {
                    prop = load(PropertiesMuncher.class.getClassLoader().getParent().getResourceAsStream(filename));
                } catch (NullPointerException ex) {
                    // logger.warn("Could not load file from classpath " + filename + " : " + ex.getMessage());
                }

            }
            return prop;

        } catch (IOException e) {
            throw new RuntimeException("Couldn't load file from classpath " + filename, e);
        }

    }

    /**
     * Loads the properties from a text file, but this also looks for the same file in the classpath and will use that
     * too, merging the two before returning the result. If conflicting properties are found then it uses the property
     * from the file rather than in the classpath so the config can be easily updated without rebuilding the
     * application.
     * 
     * @param directory
     * @param filename
     * @return
     */
    public static Properties munchFromFile(final File directory, final String filename) {
        return munchFromFile(directory, filename, true);
    }

    /**
     * Loads the properties from a text file, and if the <code>classpath</code> parameter is <code>true</code> will also
     * looks for the same file in the classpath and will use that too, merging the two before returning the result. If
     * conflicting properties are found then it uses the property from the file rather than in the classpath so the
     * config can be easily updated without rebuilding the application.
     * 
     * @param directory
     * @param filename
     * @param classpath
     * @return
     */
    public static Properties munchFromFile(final File directory, final String filename, boolean classpath) {

        // if (logger.isDebugEnabled())
        // logger.debug("Munching properties from file using path/filename : " + directory.getAbsolutePath() + "/" +
        // filename);

        if ((directory == null) || !directory.exists()) {
            throw new RuntimeException("Couldn't find directory : " + directory);
        }

        if (filename == null) {
            throw new IllegalArgumentException("Cannot supply null parameter : filename");
        }

        final File target = new File(directory, filename);

        Properties prop = new Properties();

        try {

            if (target.exists()) {

                final InputStream in = new FileInputStream(target);

                prop = load(in);

            } else {

                // logger.warn("Couldn't load properties file called [" + filename + "] from [" + directory + "]");
            }

            if (classpath) {
                final Properties propertiesFromClasspath = munchFromClasspath(filename);

                /*
                 * We can tolerate nulls, but lets not bother trying to merge unless we have too
                 */
                return ((propertiesFromClasspath != null) && (propertiesFromClasspath.size() > 0)) ? mergeProperties(
                        prop, propertiesFromClasspath) : prop;
            } else {
                return prop;
            }

        } catch (IOException e) {
            throw new RuntimeException("Couldn't load file from file " + target, e);
        }

    }

    /**
     * <p> uses java.util.Properties.load(InputStream), but also checks to see if the logger is in debug mode and spits
     * the properties into it. Developers need to be aware that this could add sensitive information into log files so
     * they need to ensure that they are't logging at debug level on live platforms. </p>
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    // @SuppressWarnings("unchecked")
    public static Properties load(InputStream stream) throws IOException {

        Properties properties = new Properties();

        properties.load(stream);

        // if (logger.isDebugEnabled()) {
        //
        // for (Enumeration keys = properties.keys(); keys.hasMoreElements();) {
        //
        // String key = (String) keys.nextElement();
        // String value = properties.getProperty(key);
        //
        // logger.debug(key + " : " + value);
        // }
        //
        // }

        return properties;

    }

    /**
     * Merges two properties objects by creating a new object, adding all of the first set of properties and then adding
     * properties from the second as long as they do not already exist in the merged object. This method can take null
     * properties objects, it just doesn't add anything to the merged object.
     * 
     * @param first
     * @param second
     * @return At the very least an empty Properties object
     */
    public static Properties mergeProperties(Properties first, Properties second) {

        final Properties merged = new Properties();

        // lets tolerate null properties objects being submitted
        if (first != null) {
            for (final Object entry : first.keySet()) {
                merged.setProperty(entry.toString(), first.getProperty(entry.toString()));
            }
        }

        if (second != null) {
            for (final Object entry : second.keySet()) {
                final String key = entry.toString();
                if (merged.getProperty(key) == null) {
                    merged.setProperty(key, second.getProperty(key));
                }
            }
        }

        return merged;

    }

    /**
     * <p> Checks to make sure that the keys in the first exist in the second to make sure it is only over-riding the
     * second's properties. If they are consistent then it merges them into a single properties object. </p>
     * 
     * @param first
     * @param second
     * @return At the very least an empty Properties object
     */
    public static Properties strictMergeProperties(Properties first, Properties second) {

        if ((first != null) && (second != null)) {

            for (Object key : first.keySet()) {
                if (second.get(key) == null) {
                    throw new UnmatchedPropertyException(
                            "Couldn't merge properties, the first properties object contains a key that doesn't exist in the second. The key is called "
                                    + key);
                }
            }
        } else {

            // logger.warn("No properties specified, cannot merge");
        }

        final Properties merged = mergeProperties(first, second);

        return merged;

    }

    /**
     * Sets the parent file system path from which to read the configuration properties
     * 
     * @param parentPath The path from which to read the properties files.
     */
    public void setParentPath(String parentPath) {
        // if(parentPath!=null && parentPath.startsWith("/"))
        // parentPath=parentPath.substring(1);

        if ((parentPath != null) && !parentPath.endsWith("/")) {
            this.parentPath = parentPath + "/";
        } else {
            this.parentPath = parentPath;
        }
    }

    public String getParentPath() {
        return parentPath;
    }

    /**
     * The parent package from which to read the properties files.
     * 
     * @param parentPackage The parent package that contains the properties.
     */
    public void setParentPackage(String parentPackage) {
        parentPackage = parentPackage.trim();

        if (parentPackage.indexOf('.') > -1) {
            parentPackage = parentPackage.replace('.', '/');
        }

        if ((parentPackage != null) && parentPackage.startsWith("/")) {
            parentPackage = parentPackage.substring(1);
        }

        if ((parentPackage != null) && !parentPackage.endsWith("/")) {
            this.parentPackage = parentPackage + "/";
        } else {
            this.parentPackage = parentPackage;
        }

        if ((this.parentPackage.length() == 1) && this.parentPackage.equals("/")) {
            this.parentPackage = "";
        }

    }

    public String getParentPackage() {
        return parentPackage;
    }

    /**
     * Sets the name of the global properties file. This will be located within either the parentPackage
     * {@link #getParentPackage()} or parentPath: {@link #getParentPath()} This is by default set to
     * config/environment.properties.
     * 
     * @param globalProperties
     */
    // protected void setGlobalProperties(String globalProperties) {
    // this.globalProperties = globalProperties;
    // }
    public static String getGlobalProperties() {
        return globalProperties;
    }

    /**
     * Sets the name of the directory from which to retrieve the environment specific properties files. By default this
     * is config/environments This directory will exist under the parentPackage {@link #getParentPackage()} or the
     * parentPath {@link #getParentPath()}
     * 
     * @param globalPropertiesDir
     */
    // public void setGlobalPropertiesDir(String globalPropertiesDir) {
    // this.globalPropertiesDir = globalPropertiesDir;
    // }
    public static String getGlobalPropertiesDir() {
        return globalPropertiesDir;
    }

    public static String getAppDataDirProperty() {
        return appDataDirProperty;
    }

    public boolean hasParentPackage() {
        String parentPackage = getParentPackage();
        if ((parentPackage != null) && (parentPackage.trim().length() > 0)) {
            return true;
        } else {
            return false;
        }

    }

    public boolean hasParentPath() {
        String parentPath = getParentPath();
        if ((parentPath != null) && (parentPath.trim().length() > 0)) {
            return true;
        } else {
            return false;
        }
    }

}
