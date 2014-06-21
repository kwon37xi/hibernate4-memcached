package kr.pe.kwonnam.hibernate4memcached.timestamper;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * next timestamp based System.currentTimeMillis
 * <p/>
 * If strictly increasing timestamp required, use {@link HibernateCacheTimestamperMemcachedImpl}.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class HibernateCacheTimestamperJvmImpl implements HibernateCacheTimestamper {
    private Logger log = LoggerFactory.getLogger(HibernateCacheTimestamperJvmImpl.class);

    @Override
    public void setSettings(Settings settings) {
        // no op
    }

    @Override
    public void setProperties(OverridableReadOnlyProperties properties) {
        // no op
    }

    @Override
    public void setMemcachedAdapter(MemcachedAdapter memcachedAdapter) {
        // no op
    }

    @Override
    public void init() {
        log.debug("hibernate cache timestamper jvm implementation linitialized.");
    }

    @Override
    public long next() {
        long next = System.currentTimeMillis();
        log.debug("hibernate cache timestamper next : {}", next);
        return next;
    }
}
