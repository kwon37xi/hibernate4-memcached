package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.GeneralDataMemcachedRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.RegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class MemcachedRegionAccessStrategy implements RegionAccessStrategy {
    private Logger log = LoggerFactory.getLogger(MemcachedRegionAccessStrategy.class);

    private GeneralDataMemcachedRegion internalRegion;

    public MemcachedRegionAccessStrategy(GeneralDataMemcachedRegion internalRegion) {
        this.internalRegion = internalRegion;
    }

    protected GeneralDataMemcachedRegion getInternalRegion() {
        return internalRegion;
    }

    @Override
    public Object get(Object key, long txTimestamp) throws CacheException {
        log.debug("region access strategy get() {} {}", getInternalRegion().getCacheNamespace(), key);
        return getInternalRegion().get(key);
    }

    @Override
    public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version) throws CacheException {
        return putFromLoad(key, value, txTimestamp, version, isMinimalPutsEnabled());
    }

    @Override
    public boolean putFromLoad(Object key, Object value, long txTimestamp, Object version, boolean minimalPutOverride) throws CacheException {
        log.debug("region access strategy putFromLoad() {} {}", getInternalRegion().getCacheNamespace(), key);
        if (key == null || value == null) {
            return false;
        }

        // isMinimalPutsEnabled ignored. spymemcached does not support contains(key)

        getInternalRegion().put(key, value);
        return true;
    }

    @Override
    public SoftLock lockItem(Object key, Object version) throws CacheException {
        log.debug("region access strategy lockItem() {} {}", getInternalRegion().getCacheNamespace(), key);
        return null;
    }

    /**
     * Region locks are not supported.
     */
    @Override
    public SoftLock lockRegion() throws CacheException {
        log.debug("region access strategy lockRegion() {}", getInternalRegion().getCacheNamespace());
        return null;
    }

    /**
     * 캐시 삭제를 수행한다.
     *
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     */
    @Override
    public void unlockItem(Object key, SoftLock lock) throws CacheException {
        log.debug("region access strategy unlockItem() {} {}", getInternalRegion().getCacheNamespace(), key);
    }

    /**
     * HQL/SQL 실행시 region 을 evict할 때 실행해야 한다.
     *
     * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
     * @see org.hibernate.cache.spi.access.RegionAccessStrategy#unlockRegion(org.hibernate.cache.spi.access.SoftLock)
     */
    @Override
    public void unlockRegion(SoftLock lock) throws CacheException {
        log.debug("region access strategy unlockRegion lock() {} {}", getInternalRegion().getCacheNamespace(), lock);
        evictAll();
    }

    /**
     * @see org.hibernate.cache.spi.access.RegionAccessStrategy#remove(java.lang.Object)
     */
    @Override
    public void remove(Object key) throws CacheException {
        log.debug("region access strategy remove() {} {}", getInternalRegion().getCacheNamespace(), key);
        evict(key);
    }

    /**
     * Transaction 지원할 때는 evictAll()을 호출하는데 그렇게 하면 트랜잭션 시작전에 한번, 트랜잭션 종료후에 한 번 두번 evict가 호출된다.
     * memcached와는 무관하다.
     *
     * @see org.hibernate.cache.spi.access.RegionAccessStrategy#removeAll()
     */
    @Override
    public void removeAll() throws CacheException {
        log.debug("region access strategy removeAll() {}", getInternalRegion().getCacheNamespace());
        // do nothing
    }

    /**
     * @see org.hibernate.cache.spi.access.RegionAccessStrategy#evict(java.lang.Object)
     */
    @Override
    public void evict(Object key) throws CacheException {
        log.debug("region access strategy evict() {} {}", getInternalRegion().getCacheNamespace(), key);
        getInternalRegion().evict(key);
    }

    @Override
    public void evictAll() throws CacheException {
        log.debug("region access strategy evictAll() {}", getInternalRegion().getCacheNamespace());
        getInternalRegion().evictAll();
    }

    public boolean isMinimalPutsEnabled() {
        return getInternalRegion().getSettings().isMinimalPutsEnabled();
    }
}