package kr.pe.kwonnam.hibernate4memcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.regions.*;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.PropertiesUtils;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.*;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static kr.pe.kwonnam.hibernate4memcached.util.PropertiesUtils.getRequiredProeprties;

/**
 * Hibernate 4 Memcached Cache Region Factory
 * <p/>
 * 설정할 것
 * memcached 최대 expiry second는 30일(days)
 * <p/>
 * - h4m.adapter.class=kr.pe.kwonnam.hibernate4memcached.spymemcached.SpyMemcachedAdapter
 * <p/>
 * - h4m.expiry.seconds=600 # default value
 * - h4m.expiry.seconds.[regionprefix].org.hibernate.cache.internal.StandardQueryCache=600
 * - h4m.expiry.seconds.[regionprefix].org.hibernate.cache.spi.UpdateTimestampsCache=60 * 60 * 24 * 30;
 * - h4m.expiry.seconds.[regionprefix].xxx.yyy=300
 * - h4m.expiry.seconds.[regionprefix].xxx.zzz=500
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class Hibernate4MemcachedRegionFactory implements RegionFactory {
    private final Logger log = LoggerFactory.getLogger(Hibernate4MemcachedRegionFactory.class);

    public static final String REGION_EXPIARY_SECONDS_PROPERTY_KEY_PREFIX = "h4m.expiry.seconds";
    public static final String MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY = "h4m.adapter.class";

    private Settings settings;

    private MemcachedAdapter memcachedAdapter;

    private MemcachedTimestamper memcachedTimestamper;

    public Hibernate4MemcachedRegionFactory() {
        // no op
    }

    public Hibernate4MemcachedRegionFactory(Properties properties) {
    }

    @Override
    public void start(Settings settings, Properties properties) throws CacheException {
        log.debug("# start Hibernate4MemcachedRegionFactory.");

        this.settings = settings;

        memcachedAdapter = populateMemcachedProvider(properties);

        memcachedAdapter.init(properties);

        memcachedTimestamper = new MemcachedTimestamper(settings, properties, memcachedAdapter);
    }

    MemcachedAdapter populateMemcachedProvider(Properties properties) {
        String adapterClass = getRequiredProeprties(properties, MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY);

        MemcachedAdapter adapter = null;
        try {
            Class<?> clazz = Class.forName(adapterClass);
            adapter = (MemcachedAdapter) clazz.newInstance();

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return adapter;
    }


    @Override
    public void stop() {
        log.debug("# stop Hibernate4MemcachedRegionFactory.");
        memcachedAdapter.destroy();
    }

    @Override
    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }

    @Override
    public AccessType getDefaultAccessType() {
        return AccessType.NONSTRICT_READ_WRITE;
    }

    @Override
    public long nextTimestamp() {
        return memcachedTimestamper.next();
    }

    @Override
    public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
        return new EntityMemcachedRegion(regionName, properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public NaturalIdRegion buildNaturalIdRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
        return new NaturalIdMemcachedRegion(regionName, properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public CollectionRegion buildCollectionRegion(String regionName, Properties properties, CacheDataDescription metadata)
            throws CacheException {
        return new CollectionMemcachedRegion(regionName, properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {
        return new QueryResultsMemcachedRegion(regionName, properties, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {
        return new TimestampMemcachedRegion(regionName, properties, settings, memcachedAdapter, memcachedTimestamper);
    }
}
