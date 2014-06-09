package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.strategies.NonstrictReadWriteCollectionRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class CollectionMemcachedRegion extends TransactionalDataMemcachedRegion implements CollectionRegion {
    public CollectionMemcachedRegion(String regionName, Properties properties, CacheDataDescription metadata, Settings settings, MemcachedAdapter memcachedAdapter, MemcachedTimestamper memcachedTimestamper) {
        super(new CacheNamespace(regionName, true), properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public CollectionRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
        switch (accessType) {
            case NONSTRICT_READ_WRITE:
                return new NonstrictReadWriteCollectionRegionAccessStrategy(this);
            default:
                throw new IllegalStateException("Unsupported access strategy : " + accessType + ".");
        }
    }
}
