package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.MemcachedTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.GeneralDataRegion;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory.REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX;

/**
 * 실제 캐시를 제어하는 일반 Region
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class GeneralDataMemcachedRegion extends MemcachedRegion implements GeneralDataRegion {
    private Logger log = LoggerFactory.getLogger(GeneralDataMemcachedRegion.class);

    private int expirySeconds;

    public GeneralDataMemcachedRegion(CacheNamespace cacheNamespace, OverridableReadOnlyProperties properties, CacheDataDescription metadata,
                                      Settings settings, MemcachedAdapter memcachedAdapter, MemcachedTimestamper memcachedTimestamper) {
        super(cacheNamespace, properties, metadata, settings, memcachedAdapter, memcachedTimestamper);
        populateExpirySeconds(properties);
    }

    void populateExpirySeconds(OverridableReadOnlyProperties properties) {
        String regionExpirySecondsKey = REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX + "." + getCacheNamespace().getRegionName();
        String expirySecondsProperty = properties.getProperty(regionExpirySecondsKey);
        if (expirySecondsProperty == null) {
            expirySecondsProperty = properties.getProperty(REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX);
        }
        if (expirySecondsProperty == null) {
            throw new IllegalStateException(regionExpirySecondsKey + " or " + REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX
                    + "(for default expiry seconds) required!");
        }

        expirySeconds = Integer.parseInt(expirySecondsProperty);
    }

    @Override
    public Object get(Object key) throws CacheException {
        Object cachedData = getMemcachedAdapter().get(getCacheNamespace(), String.valueOf(key));

        if (cachedData == null) {
            return null;
        }

        if (!(cachedData instanceof CacheItem)) {
            log.debug("get cachedDate is not CacheItem.");
            return cachedData;
        }

        CacheItem cacheItem = (CacheItem) cachedData;
        boolean targetClassAndCurrentJvmTargetClassMatch = cacheItem.isTargetClassAndCurrentJvmTargetClassMatch();
        log.debug("cacheItem and targetClassAndCurrentJvmTargetClassMatch : {} / {}", targetClassAndCurrentJvmTargetClassMatch, cacheItem);

        if (cacheItem.isTargetClassAndCurrentJvmTargetClassMatch()) {
            return cacheItem.getCacheEntry();
        }

        return null;
    }

    @Override
    public void put(Object key, Object value) throws CacheException {
        log.debug("Cache put [{}] : key[{}], value[{}]", getCacheNamespace(), key, value);

        Object valueToCache = value;

        boolean classVersionApplicable = CacheItem.checkIfClassVersionApplicable(value, getSettings().isStructuredCacheEntriesEnabled());
        log.debug("Cache put classVersionApplicable : {}, {}", classVersionApplicable, value);

        if (classVersionApplicable) {
            valueToCache = new CacheItem(value, getSettings().isStructuredCacheEntriesEnabled());
        }
        getMemcachedAdapter().set(getCacheNamespace(), String.valueOf(key), valueToCache, getExpiryInSeconds());
    }

    @Override
    public void evict(Object key) throws CacheException {
        log.debug("Cache evict[{}] : key[{}]", getCacheNamespace(), key);
        getMemcachedAdapter().delete(getCacheNamespace(), String.valueOf(key));
    }

    @Override
    public void evictAll() throws CacheException {
        log.debug("Cache evictAll [{}].", getCacheNamespace());
        getMemcachedAdapter().evictAll(getCacheNamespace());
    }

    /**
     * 설정 정보에서 캐시의 Expiry 초를 읽어서 리턴한다.
     */
    protected int getExpiryInSeconds() {
        return expirySeconds;
    }
}
