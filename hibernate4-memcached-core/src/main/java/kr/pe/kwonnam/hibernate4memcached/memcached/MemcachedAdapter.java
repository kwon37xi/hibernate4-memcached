package kr.pe.kwonnam.hibernate4memcached.memcached;

import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;

/**
 * Adapter for memcached operation.
 * <p/>
 * When Hibernate Session factory closed, {@link #destroy} method will be called automatically.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public interface MemcachedAdapter {
    /**
     * Lifecycle callback to perform initialization.
     *
     * @param properties the defined cfg properties
     */
    void init(OverridableReadOnlyProperties properties);

    /**
     * Lifecycle callback to perform cleanup.
     */
    void destroy();

    /**
     * get value from memcached
     */
    Object get(CacheNamespace cacheNamespace, String key);

    /**
     * set value to memcache with expirySeconds
     */
    void set(CacheNamespace cacheNamespace, String key, Object value, int expirySeconds);

    /**
     * delete key from memcached
     */
    void delete(CacheNamespace cacheNamespace, String key);

    /**
     * increase given increment couter and return new value.
     *
     * @param cacheNamespace cache namespace
     * @param key            counter key
     * @param by             the amount of increment
     * @param defaultValue   default value when the key missing
     * @return increased value
     */
    long increaseCounter(CacheNamespace cacheNamespace, String key, long by, long defaultValue, int expirySeconds);

    /**
     * get current value from increment counter
     *
     * @param cacheNamespace cache namespace
     * @param key            counter key
     * @param defaultValue   default value when the key missing
     * @return current value of counter without increment
     */
    long getCounter(CacheNamespace cacheNamespace, String key, long defaultValue, int expirySeconds);

    /**
     * Evict namespace
     *
     * @param cacheNamespace cache namespace
     */
    void evictAll(CacheNamespace cacheNamespace);
}