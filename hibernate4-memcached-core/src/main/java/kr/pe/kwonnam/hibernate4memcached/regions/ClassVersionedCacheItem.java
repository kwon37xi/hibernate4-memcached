package kr.pe.kwonnam.hibernate4memcached.regions;

import org.hibernate.cfg.Settings;

import java.io.Serializable;

/**
 * Entity Cache에 데이터를 저장할 때
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class ClassVersionedCacheItem implements Serializable {
    private Object cacheData;

    /** Entity class FQCN */
    private String targetClassName;

    /** Entity class serialVersionUID */
    private long targetClassSerialVersionUID;

    public ClassVersionedCacheItem(Object cacheData, boolean useStructuredCache) {
        this.cacheData = cacheData;

        parseTargetClass(cacheData, useStructuredCache);
    }

    private void parseTargetClass(Object cacheData, boolean useStructuredCache) {

    }

    /**
     * Compare targetClassSerialVersionUID and current JVM's targetClass serialVersionUID.
     * If they are same return true else return false.
     */
    public boolean isTargetClassVersionMatch() {
        return false;
    }

    /**
     * Check if class version comparable.
     *
     * @param value
     * @param hibernateSettings
     * @return
     */
    public static boolean checkIfClassVersionApplicable(Object value, Settings hibernateSettings) {
        return false;
    }
}
