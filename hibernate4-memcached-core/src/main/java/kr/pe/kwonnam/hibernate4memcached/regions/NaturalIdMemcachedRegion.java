package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.strategies.NonstrictReadWriteNaturalIdRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.strategies.ReadOnlyNaturalIdRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class NaturalIdMemcachedRegion extends TransactionalDataMemcachedRegion implements NaturalIdRegion {
    public NaturalIdMemcachedRegion(String regionName, Properties properties, CacheDataDescription metadata, Settings settings, MemcachedAdapter memcachedAdapter, MemcachedTimestamper memcachedTimestamper) {
        super(new CacheNamespace(regionName, true), properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
        switch (accessType) {
            case READ_ONLY:
                return new ReadOnlyNaturalIdRegionAccessStrategy(this);
            case NONSTRICT_READ_WRITE:
                return new NonstrictReadWriteNaturalIdRegionAccessStrategy(this);
            default:
                throw new IllegalStateException("Unsupported access strategy : " + accessType + ".");
        }
    }
}
