package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * {@link org.hibernate.cache.spi.TimestampsRegion}은 strategy가 없다.
 * <code>[cache-region-prefix.]org.hibernate.cache.spi.UpdateTimestampsCache</code>를 다룬다.

 * 해당 region에 대해 expirySeconds를 최대로 잡아줄 것.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class TimestampMemcachedRegion extends GeneralDataMemcachedRegion implements TimestampsRegion {
    private static final int DEFAULT_TIMESTAMP_EXPIRY_SECONDS = 60 * 60 * 24; // 24 hours

    public TimestampMemcachedRegion(String regionName, Properties properties, Settings settings,
                                    MemcachedAdapter memcachedAdapter, MemcachedTimestamper memcachedTimestamper) {
        super(new CacheNamespace(regionName, false), properties, null, settings, memcachedAdapter, memcachedTimestamper);
    }
}
