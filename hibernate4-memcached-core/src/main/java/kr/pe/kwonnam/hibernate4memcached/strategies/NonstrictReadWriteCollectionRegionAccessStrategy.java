package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.CollectionMemcachedRegion;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class NonstrictReadWriteCollectionRegionAccessStrategy extends BaseCollectionMemcachedRegionAccessStrategy {
    public NonstrictReadWriteCollectionRegionAccessStrategy(CollectionMemcachedRegion collectionMemcachedRegion) {
        super(collectionMemcachedRegion);
    }
}