package kr.pe.kwonnam.hibernate4memcached.util;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Generates increasing identifier for {@link org.hibernate.cache.spi.RegionFactory#nextTimestamp()}.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class MemcachedTimestamper {
    private Logger log = LoggerFactory.getLogger(MemcachedTimestamper.class);

    private Settings settings;

    private Properties properties;

    private MemcachedAdapter memcachedAdapter;

    public MemcachedTimestamper() {
        // do nothing;
    }

    public MemcachedTimestamper(Settings settings, Properties properties, MemcachedAdapter memcachedAdapter) {
        this.settings = settings;
        this.properties = properties;
        this.memcachedAdapter = memcachedAdapter;

        log.debug("timestamper initialized.");
    }

    public long next() {
        return System.currentTimeMillis();
    }
}
