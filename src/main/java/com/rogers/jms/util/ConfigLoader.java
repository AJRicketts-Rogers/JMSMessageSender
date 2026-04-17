package com.rogers.jms.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {

    private static final String DEFAULT_CLASSPATH_CONFIG = "/loadtest.properties";
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    public static Properties load() {
        Properties props = new Properties();

        // Try external config file via -Dconfig.file=...
        String configPath = System.getProperty("config.file");
        if (configPath != null && !configPath.trim().isEmpty()) {
            try (InputStream in = new FileInputStream(configPath)) {
                props.load(in);
                logger.info(configPath != null ? "Loaded config from file: " + configPath 
                		: "Loaded config from classpath: " + DEFAULT_CLASSPATH_CONFIG);
                return props;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load config file from path: " + configPath, e);
            }
        }

        // Fallback to classpath resource
        try (InputStream in =
                 ConfigLoader.class.getResourceAsStream(DEFAULT_CLASSPATH_CONFIG)) {

            if (in == null) {
                throw new RuntimeException("No configuration found. Either:\n" + "  - supply -Dconfig.file=/path/to/loadtest.properties\n" +
                    "  - or place loadtest.properties on the classpath");
            }

            props.load(in);
            logger.info(configPath != null ? "Loaded config from file: " + configPath 
            		: "Loaded config from classpath: " + DEFAULT_CLASSPATH_CONFIG);
            return props;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from classpath: " + DEFAULT_CLASSPATH_CONFIG, e);
        }
    }
}