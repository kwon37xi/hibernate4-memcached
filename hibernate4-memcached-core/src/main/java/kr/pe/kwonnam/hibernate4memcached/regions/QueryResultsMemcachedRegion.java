package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * {@link org.hibernate.cache.spi.QueryResultsRegion}은 strategy가 없다.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class QueryResultsMemcachedRegion extends GeneralDataMemcachedRegion implements QueryResultsRegion {
    public QueryResultsMemcachedRegion(String regionName, Properties properties, Settings settings,
                                       MemcachedAdapter memcachedAdapter, MemcachedTimestamper memcachedTimestamper) {
        super(new CacheNamespace(regionName, false), properties, null, settings, memcachedAdapter, memcachedTimestamper);
    }
}
