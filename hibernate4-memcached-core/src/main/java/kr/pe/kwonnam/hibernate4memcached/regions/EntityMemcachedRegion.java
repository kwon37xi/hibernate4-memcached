package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.strategies.NonstrictReadWriteEntityRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class EntityMemcachedRegion extends TransactionalDataMemcachedRegion implements EntityRegion {
    public EntityMemcachedRegion(String regionName, Properties properties, CacheDataDescription metadata, Settings settings, MemcachedAdapter memcachedAdapter, MemcachedTimestamper memcachedTimestamper) {
        super(new CacheNamespace(regionName, true), properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public EntityRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
        switch (accessType) {
            case NONSTRICT_READ_WRITE:
                return new NonstrictReadWriteEntityRegionAccessStrategy(this);
            default:
                throw new IllegalStateException("Unsupported access strategy : " + accessType + ".");
        }
    }
}
