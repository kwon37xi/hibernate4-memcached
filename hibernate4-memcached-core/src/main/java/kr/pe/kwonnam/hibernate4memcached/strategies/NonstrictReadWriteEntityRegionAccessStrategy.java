package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.EntityMemcachedRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.SoftLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class NonstrictReadWriteEntityRegionAccessStrategy extends BaseEntityMemcachedRegionAccessStrategy {
    private Logger log = LoggerFactory.getLogger(NonstrictReadWriteEntityRegionAccessStrategy.class);

    public NonstrictReadWriteEntityRegionAccessStrategy(EntityMemcachedRegion entityMemcachedRegion) {
        super(entityMemcachedRegion);
    }

    @Override
    public boolean insert(Object key, Object value, Object version) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity insert() {} {}", getInternalRegion().getCacheNamespace(), key);
        // On nonstrict-read-write, Hibernate never calls this method.
        return false;
    }

    @Override
    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity afterInsert() {} {}", getInternalRegion().getCacheNamespace(), key);
        // On nonstrict-read-write, Hibernate never calls this method.
        return false;
    }

    /**
     * not necessary in nostrict-read-write
     *
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     */
    @Override
    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity update() {} {}", getInternalRegion().getCacheNamespace(), key);
        return false;
    }

    /**
     * need evict the key, after update.
     *
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     */
    @Override
    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity afterUpdate() {} {}", getInternalRegion().getCacheNamespace(), key);
        getInternalRegion().evict(key);
        return false;
    }
}
