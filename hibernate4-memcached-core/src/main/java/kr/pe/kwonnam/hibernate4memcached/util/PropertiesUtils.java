package kr.pe.kwonnam.hibernate4memcached.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class PropertiesUtils {
    private static Logger LOG = LoggerFactory.getLogger(PropertiesUtils.class);

    public static Properties loadFromClasspath(String resourcePath) {
        if (StringUtils.isEmpty(resourcePath)) {
            throw new IllegalArgumentException("resourcePath must not be empty.");
        }

        LOG.debug("resourcePath to load : {}", resourcePath);

        Properties properties = new Properties();

        try {
            InputStream resourceStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(resourcePath);
            if (resourceStream == null) {
                throw new IllegalStateException("Failed to load properties from '" + resourcePath + "'. You might set illegal resource path. Leading slash(/) is not allowed.");
            }

            if (resourcePath.toLowerCase().endsWith(".xml")) {
                properties.loadFromXML(resourceStream);
            } else {
                properties.load(resourceStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load properties from '" + resourcePath + "'.");
        }
        return properties;
    }
}
