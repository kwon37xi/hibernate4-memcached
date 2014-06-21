package kr.pe.kwonnam.hibernate4memcached.timestamper;

/**
 * next timestamp based System.currentTimeMillis
 * <p/>
 * If strictly increasing timestamp required, use {@link HibernateCacheTimestamperMemcachedImpl}.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class HibernateCacheTimestamperJvmImpl implements HibernateCacheTimestamper {

    @Override
    public long next() {
        return System.currentTimeMillis();
    }
}
