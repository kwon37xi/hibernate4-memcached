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

    /**
     * nostrict-read-write에서는 불필요한 작업
     *
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     */
    @Override
    public boolean insert(Object key, Object value, Object version) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity insert() {} {}", getInternalRegion().getCacheNamespace(), key);
        return false;
    }

    /**
     * nostrict-read-write에서는 불필요한 작업.
     *
     * insert 직후 자동으로 evict가 일어나며, 여기서는 evict후 다시 캐시에 넣는 역할을 하는데, 그렇게 처리하지 않고, 다시
     * 데이터를 읽는 작업이 발생할  때 캐시하도록 그냥 넘긴다..
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     */
    @Override
    public boolean afterInsert(Object key, Object value, Object version) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity afterInsert() {} {}" , getInternalRegion().getCacheNamespace(), key);
        return false;
    }

    /**
     * nostrict-read-write에서는 불필요한 작업
     *
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     */
    @Override
    public boolean update(Object key, Object value, Object currentVersion, Object previousVersion) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity update() {} {}", getInternalRegion().getCacheNamespace(), key);
        return false;
    }

    /**
     * update 후, 기존 캐시를 삭제해줘야 한다.
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     */
    @Override
    public boolean afterUpdate(Object key, Object value, Object currentVersion, Object previousVersion, SoftLock lock) throws CacheException {
        log.debug("region access strategy nonstrict-read-write entity afterUpdate() {} {}", getInternalRegion().getCacheNamespace(), key);
        getInternalRegion().evict(key);
        return false;
    }
}
