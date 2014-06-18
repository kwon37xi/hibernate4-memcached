package kr.pe.kwonnam.hibernate4memcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.regions.*;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import kr.pe.kwonnam.hibernate4memcached.util.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.*;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Hibernate 4 Memcached Cache Region Factory
 * <p/>
 * 설정할 것
 * memcached 최대 expiry second는 30일(days)
 * <p/>
 * - h4m.adapter.class=kr.pe.kwonnam.hibernate4memcached.spymemcached.SpyMemcachedAdapter
 * <p/>
 * <p/>
 * {@link org.hibernate.cfg.AvailableSettings#CACHE_PROVIDER_CONFIG}에 클래스패스로 설정파일 지정.
 * 아래 expiry 설정은 별도의 프라퍼티파일과 일반적인 JPA Property 혹은 Hibernate Property 모두로 설정가능하다.
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

    public static final String REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX = "h4m.expiry.seconds";
    public static final String MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY = "h4m.adapter.class";

    private Settings settings;

    private Properties cacheProviderConfigProperties;

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
        cacheProviderConfigProperties = populateCacheProviderConfigProperties(properties);
        OverridableReadOnlyProperties mergedConfigProperties = new OverridableReadOnlyPropertiesImpl(properties, cacheProviderConfigProperties);
        memcachedAdapter = populateMemcachedProvider(mergedConfigProperties);
        memcachedAdapter.init(mergedConfigProperties);
        memcachedTimestamper = new MemcachedTimestamper(settings, mergedConfigProperties, memcachedAdapter);
    }

    Properties populateCacheProviderConfigProperties(Properties properties) {
        String configPath = properties.getProperty(AvailableSettings.CACHE_PROVIDER_CONFIG);
        if (StringUtils.isEmpty(configPath)) {
            return new Properties();
        }

        return PropertiesUtils.loadFromClasspath(configPath);
    }

    MemcachedAdapter populateMemcachedProvider(OverridableReadOnlyProperties properties) {
        String adapterClass = properties.getRequiredProperty(MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY);

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
        return new EntityMemcachedRegion(regionName, new OverridableReadOnlyPropertiesImpl(properties, cacheProviderConfigProperties), metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public NaturalIdRegion buildNaturalIdRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
        return new NaturalIdMemcachedRegion(regionName, new OverridableReadOnlyPropertiesImpl(properties, cacheProviderConfigProperties), metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public CollectionRegion buildCollectionRegion(String regionName, Properties properties, CacheDataDescription metadata)
            throws CacheException {
        return new CollectionMemcachedRegion(regionName, new OverridableReadOnlyPropertiesImpl(properties, cacheProviderConfigProperties), metadata, settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {
        return new QueryResultsMemcachedRegion(regionName, new OverridableReadOnlyPropertiesImpl(properties, cacheProviderConfigProperties), settings, memcachedAdapter, memcachedTimestamper);
    }

    @Override
    public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {
        return new TimestampMemcachedRegion(regionName, new OverridableReadOnlyPropertiesImpl(properties, cacheProviderConfigProperties), settings, memcachedAdapter, memcachedTimestamper);
    }
}
