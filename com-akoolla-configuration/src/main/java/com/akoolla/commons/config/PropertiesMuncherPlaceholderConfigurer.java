package com.akoolla.commons.config;

import java.io.File;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class PropertiesMuncherPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private final static String TMP_DIR = "java.io.tmpdir";

    private String filename;

    private String directory;

    private Properties properties;

    // Should property values be trimmed.
    private boolean trim = false;

    private PropertiesMuncher propertiesMuncher = new PropertiesMuncher();

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {

        if (properties == null) {
            init();
        }

        // if (logger.isDebugEnabled())
        // logger.debug("Searching for " + placeholder + " in " + properties);

        String s = properties.getProperty(placeholder);
        if (isTrim()) {
            if (s != null) {
                return s.trim();
            } else {
                return s;
            }
        } else {
            return s;
        }

    }

    /**
     * This is the actual method that loads up the properties files, that are to be used for processing the property
     * placeholders within an application context file
     */
    public void init() {
        PropertiesMuncher muncher = getPropertiesMuncher();

        if (muncher.hasParentPackage()) {
            // if (logger.isDebugEnabled())
            // logger.debug("Using muncher munchClassPathBasedProperties to load props");
            properties = muncher.munchClassPathBasedProperties();
        } else if (muncher.hasParentPath()) {
            // if (logger.isDebugEnabled())
            // logger.debug("Using muncher munchFileSystemBasedProperties to load props");
            properties = muncher.munchFileSystemBasedProperties();
        } else if (directory != null) {

            // if (logger.isDebugEnabled())
            // logger.debug("Using directory to load props [" + directory + "] and filename [" + filename + "]");

            properties = PropertiesMuncher.munch(new File(directory), filename);

        } else if (filename != null) {

            // if (logger.isDebugEnabled())
            // logger.debug("Using file to load props [" + filename + "]");

            properties = PropertiesMuncher.munchFromClasspath(filename);

        } else {
            properties = PropertiesMuncher.munch();
        }

        if (properties == null) {
            throw new RuntimeException("Couldn't load properties");
        }

    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * this will normally loads the directory that is specified, checking that is exists before returning. It also
     * allows for a special case where "java.io.tmpdir" is used, in which case it loads the env value.
     */
    public void setDirectory(String directory) {

        if (directory == null) {
            throw new IllegalArgumentException("directory cannot be null");
        }

        if (TMP_DIR.equals(directory)) {
            this.directory = System.getProperty(TMP_DIR);
        } else {
            this.directory = directory;
        }

        if (!new File(this.directory).exists()) {
            throw new RuntimeException("Directory for props config isn't valid [" + directory + "]");
        }

    }

    /**
     * This property is to be overridden, when you are using the more advanced features to the properties muncher: <ul>
     * <li>Specifying a difference package to obtain the properties from, other than the root of classpath</li>
     * <li>Specifying a location on the filesystem to obtain properties from</li> </ul>
     * 
     * @param propertiesMuncher
     */
    public void setPropertiesMuncher(PropertiesMuncher propertiesMuncher) {
        this.propertiesMuncher = propertiesMuncher;
    }

    public PropertiesMuncher getPropertiesMuncher() {
        return propertiesMuncher;
    }

    /**
     * Sets if property values should be trimmed or not.
     * 
     * @param trim True if property values should be trimmed.
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Are property values to be trimmed or not.
     * 
     * @return
     */
    public boolean isTrim() {
        return trim;
    }

    /**
     * Retrieve the fully resolved and merged properties.
     * 
     * @return the internal properties data
     */
    public Properties getResolvedProperties() {
        return properties;
    }
}
