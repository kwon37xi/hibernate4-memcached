package kr.pe.kwonnam.hibernate4memcached.util;

import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class PropertiesUtils {

    public static String getRequiredProeprties(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException(key + " property is required!");
        }
        return value;
    }
}
