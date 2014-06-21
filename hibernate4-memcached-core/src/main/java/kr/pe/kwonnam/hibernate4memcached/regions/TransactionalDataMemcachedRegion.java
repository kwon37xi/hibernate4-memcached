package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.timestamper.HibernateCacheTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.TransactionalDataRegion;
import org.hibernate.cfg.Settings;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class TransactionalDataMemcachedRegion extends GeneralDataMemcachedRegion implements TransactionalDataRegion {

    public TransactionalDataMemcachedRegion(CacheNamespace cacheNamespace, OverridableReadOnlyProperties properties, CacheDataDescription metadata, Settings settings,
                                            MemcachedAdapter memcachedAdapter, HibernateCacheTimestamper hibernateCacheTimestamper) {
        super(cacheNamespace, properties, metadata, settings, memcachedAdapter, hibernateCacheTimestamper);
    }

    @Override
    public boolean isTransactionAware() {
        return false;
    }

    @Override
    public CacheDataDescription getCacheDataDescription() {
        return getMetadata();
    }
}
