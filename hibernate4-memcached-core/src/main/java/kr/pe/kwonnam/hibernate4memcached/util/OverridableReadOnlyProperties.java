package kr.pe.kwonnam.hibernate4memcached.util;

/**
 * This property can contain many properties object.
 * When getProperty is called, it calls from the first properties object to the last properties object.
 * When it finds the key which requested, it returns the value immediately.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public interface OverridableReadOnlyProperties {
    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    String getRequiredProperty(String key);
}
