package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.EntityMemcachedRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.SoftLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReadOnly concurrency strategy.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
 */
public class ReadOnlyEntityRegionAccessStrategy extends BaseEntityMemcachedRegionAccessStrategy {
    private Logger log = LoggerFactory.getLogger(ReadOnlyEntityRegionAccessStrategy.class);

    public ReadOnlyEntityRegionAccessStrategy(EntityMemcachedRegion entityMemcachedRegion) {
        super(entityMemcachedRegion);
    }

    @Override
    public boolean insert(Object key, Object value, Object version) throws CacheException {
        log.debug("region access strategy readonly entity insert() {} {}", getInternalRegion().getCacheNamespace(), key);
        // On read-only, Hibernate never calls this method.
        return false;
    }

    @Override
    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {
        log.debug("region access strategy readonly entity afterInsert() {} {}", getInternalRegion().getCacheNamespace(), key);
        // On read-only, Hibernate never calls this method.
        return false;
    }

    /**
     * read-onluy does not support update.
     */
    @Override
    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion) throws CacheException {
        log.debug("region access strategy readonly entity update() {} {}", getInternalRegion().getCacheNamespace(), key);
        throw new UnsupportedOperationException("ReadOnly strategy does not support update.");
    }

    /**
     * read-only does not support update.
     */
    @Override
    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock) throws CacheException {
        log.debug("region access strategy readonly entity afterUpdate() {} {}", getInternalRegion().getCacheNamespace(), key);
        throw new UnsupportedOperationException("ReadOnly strategy does not support update.");
    }
}
