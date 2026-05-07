package com.rogers.jms.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigLoader {

    private static final String DEFAULT_CLASSPATH_CONFIG = "/loadtest.properties";
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    // ✅ Single shared instance (JVM-wide)
    private static volatile Properties props;

    private ConfigLoader() {
        // Prevent instantiation
    }

    public static Properties load() {

        // Fast-path: already loaded
        if (props != null) {
            return props;
        }

        synchronized (ConfigLoader.class) {
            // Double-check inside lock
            if (props != null) {
                return props;
            }

            Properties loaded = new Properties();

            // Try external config via -Dconfig.file
            String configPath = System.getProperty("config.file");
            if (configPath != null && !configPath.trim().isEmpty()) {
                try (InputStream in = new FileInputStream(configPath)) {
                    loaded.load(in);
                    logger.info("Loaded config from file: {}", configPath);
                    props = loaded;
                    return props;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load config file from path: " + configPath, e);
                }
            }

            // Fallback to classpath
            try (InputStream in = ConfigLoader.class.getResourceAsStream(DEFAULT_CLASSPATH_CONFIG)) {
                if (in == null) {
                    throw new RuntimeException("No configuration found. Either:\n" + " - supply -Dconfig.file=/path/to/loadtest.properties\n" +
                        " - or place loadtest.properties on the classpath");
                }

                loaded.load(in);
                logger.info("Loaded config from classpath: {}", DEFAULT_CLASSPATH_CONFIG);
                props = loaded;
                return props;

            } catch (Exception e) {
                throw new RuntimeException("Failed to load configuration from classpath: " + DEFAULT_CLASSPATH_CONFIG, e);
            }
        }
    }
}