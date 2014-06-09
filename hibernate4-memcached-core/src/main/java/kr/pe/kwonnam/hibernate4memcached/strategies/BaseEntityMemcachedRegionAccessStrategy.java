package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.EntityMemcachedRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;

/**
 * @see org.hibernate.cache.spi.access.EntityRegionAccessStrategy
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public abstract class BaseEntityMemcachedRegionAccessStrategy extends MemcachedRegionAccessStrategy implements EntityRegionAccessStrategy {
    private EntityMemcachedRegion entityMemcachedRegion;

    public BaseEntityMemcachedRegionAccessStrategy(EntityMemcachedRegion entityMemcachedRegion) {
        super(entityMemcachedRegion);
        this.entityMemcachedRegion = entityMemcachedRegion;
    }

    @Override
    public EntityRegion getRegion() {
        return entityMemcachedRegion;
    }
}
