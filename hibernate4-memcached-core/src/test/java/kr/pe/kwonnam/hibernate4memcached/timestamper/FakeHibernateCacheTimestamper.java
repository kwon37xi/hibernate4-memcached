package kr.pe.kwonnam.hibernate4memcached.timestamper;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import org.hibernate.cfg.Settings;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class FakeHibernateCacheTimestamper implements HibernateCacheTimestamper {

    private boolean initCalled = false;

    public boolean isInitCalled() {
        return initCalled;
    }

    @Override
    public void setSettings(Settings settings) {
        
    }

    @Override
    public void setProperties(OverridableReadOnlyProperties properties) {

    }

    @Override
    public void setMemcachedAdapter(MemcachedAdapter memcachedAdapter) {

    }

    @Override
    public void init() {
        initCalled = true;
    }

    @Override
    public long next() {
        return 0;
    }
}
