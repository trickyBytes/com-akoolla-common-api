package com.akoolla.commons.config;

import java.util.List;

/**
 * EnvironmentSettings.
 * 
 * @author tiffir
 * @version $Id: EnvironmentSettings.java 111 2011-03-14 11:22:01Z tiffir $
 */
public class EnvironmentSettings {

    public enum Platform {
        DEV,
        SANDBOX,
        INCUBATOR,
        CI,
        INT,
        TEST,
        PREPROD,
        PROD,
        UKNOWN;
    }

    public enum Profile {
        SMALL,
        MEDIUM,
        LARGE,
        UKNOWN;
    }

    private final Platform platform;

    private final Profile profile;

    public final String SERVER_ENV;

    /**
     * The 'actualValue is the setting from $SERVER_ENV - may not be one of the supported platforms
     */
    public EnvironmentSettings(Platform platform, Profile profile, final String serverEnv) {
        super();
        this.platform = platform;
        this.profile = profile;
        SERVER_ENV = serverEnv;
    }

    public boolean platformIsOneOf(List<Platform> platforms) {
        return platforms.contains(platform);
    }

    public Platform getPlatform() {
        return platform;
    }

    public Profile getProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "Platform : " + platform + ", profile : " + profile;
    }

}
