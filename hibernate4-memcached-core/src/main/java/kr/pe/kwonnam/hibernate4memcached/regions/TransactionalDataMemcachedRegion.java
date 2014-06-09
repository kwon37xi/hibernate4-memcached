package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.TransactionalDataRegion;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class TransactionalDataMemcachedRegion extends GeneralDataMemcachedRegion implements TransactionalDataRegion {

    public TransactionalDataMemcachedRegion(CacheNamespace cacheNamespace, Properties properties, CacheDataDescription metadata, Settings settings,
                                            MemcachedAdapter memcachedAdapter, MemcachedTimestamper memcachedTimestamper) {
        super(cacheNamespace, properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
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
