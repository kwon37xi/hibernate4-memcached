package kr.pe.kwonnam.hibernate4memcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;

/**
* @author KwonNam Son (kwon37xi@gmail.com)
*/
public class FakeMemcachedAdapter implements MemcachedAdapter {

    @Override
    public void init(OverridableReadOnlyProperties properties) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public Object get(CacheNamespace cacheNamespace, String key) {
        return null;
    }

    @Override
    public void set(CacheNamespace cacheNamespace, String key, Object value, int expirySeconds) {

    }

    @Override
    public void delete(CacheNamespace cacheNamespace, String key) {
    }

    @Override
    public void evictAll(CacheNamespace cacheNamespace) {
    }
}
