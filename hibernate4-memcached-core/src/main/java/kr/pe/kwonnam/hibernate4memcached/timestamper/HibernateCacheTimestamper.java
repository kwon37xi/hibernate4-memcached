package kr.pe.kwonnam.hibernate4memcached.timestamper;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import org.hibernate.cfg.Settings;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public interface HibernateCacheTimestamper {

    void setSettings(Settings settings);

    void setProperties(OverridableReadOnlyProperties properties);

    void setMemcachedAdapter(MemcachedAdapter memcachedAdapter);

    /** initialize timestamp object */
    void init();

    /** get next timestamp */
    long next();
}
