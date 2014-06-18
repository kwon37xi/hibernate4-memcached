package kr.pe.kwonnam.hibernate4memcached.util;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class OverridableReadOnlyPropertiesImpl implements OverridableReadOnlyProperties {

    private List<Properties> propertieses;

    public OverridableReadOnlyPropertiesImpl(Properties... propertieses) {
        this(Arrays.asList(propertieses));
    }

    public OverridableReadOnlyPropertiesImpl(List<Properties> propertieses) {
        if (propertieses == null || propertieses.size() == 0) {
            throw new IllegalArgumentException("propertieses  can not be null.");
        }

        if (propertieses.contains(null)) {
            throw new IllegalArgumentException("propertieses can not contain null properties");
        }

        this.propertieses = propertieses;
    }

    @Override
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        int size = propertieses.size();
        for (int i = size - 1; i >= 0; i--) {
            Properties properties = propertieses.get(i);

            String value = properties.getProperty(key);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public String getRequiredProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            throw new IllegalStateException(key + " property is required!");
        }
        return value;
    }
}
