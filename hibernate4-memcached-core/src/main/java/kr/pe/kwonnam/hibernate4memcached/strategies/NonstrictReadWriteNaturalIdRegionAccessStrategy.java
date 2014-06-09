package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.NaturalIdMemcachedRegion;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.SoftLock;

/**
 *
 */
public class NonstrictReadWriteNaturalIdRegionAccessStrategy extends BaseNaturalIdMemcachedRegionAccessStrategy {
    public NonstrictReadWriteNaturalIdRegionAccessStrategy(NaturalIdMemcachedRegion naturalIdMemcachedRegion) {
        super(naturalIdMemcachedRegion);
    }

    /**
     * nostrict-read-write에서는 불필요한 작업
     *
     * @see org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy
     */
    @Override
    public boolean insert(Object key, Object value) throws CacheException {
        return false;
    }

    /**
     * nostrict-read-write에서는 불필요한 작업.
     * <p/>
     * insert 직후 자동으로 evict가 일어나며, 여기서는 evict후 다시 캐시에 넣는 역할을 하는데, 그렇게 처리하지 않고, 다시
     * 데이터를 읽는 작업이 발생할  때 캐시하도록 그냥 넘긴다..
     *
     * @see org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy
     */
    @Override
    public boolean afterInsert(Object key, Object value) throws CacheException {
        return false;
    }

    /**
     * nostrict-read-write에서는 불필요한 작업
     *
     * @see org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy
     */
    @Override
    public boolean update(Object key, Object value) throws CacheException {
        return false;
    }

    /**
     * update 후에 기존 캐시를 삭제해야 한다. 캐시를 갱신하는 작업은 읽기 요청이 들어왔을 때 한다.
     *
     * @see org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy
     */
    @Override
    public boolean afterUpdate(Object key, Object value, SoftLock lock) throws CacheException {
        getInternalRegion().evict(key);
        return false;
    }
}
