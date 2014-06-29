package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.strategies.NonstrictReadWriteEntityRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.strategies.NonstrictReadWriteNaturalIdRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.strategies.ReadOnlyEntityRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.strategies.ReadOnlyNaturalIdRegionAccessStrategy;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class NaturalIdMemcachedRegionTest extends AbstractFinalMemcachedRegionTest {

    private NaturalIdMemcachedRegion naturalIdMemcachedRegion;

    @Override
    protected void afterSetUp() throws Exception {
        naturalIdMemcachedRegion = new NaturalIdMemcachedRegion("books#naturalId", properties, metadata, settings, memcachedAdapter, hibernateCacheTimestamper);
    }

    @Test
    public void buildAccessStrategy_AccessType_READ_ONLY() throws Exception {
        assertThat(naturalIdMemcachedRegion.buildAccessStrategy(AccessType.READ_ONLY))
                .isExactlyInstanceOf(ReadOnlyNaturalIdRegionAccessStrategy.class);
    }

    @Test
    public void buildAccessStrategy_AccessType_NONSTRICT_READ_WRITE() throws Exception {
        assertThat(naturalIdMemcachedRegion.buildAccessStrategy(AccessType.NONSTRICT_READ_WRITE))
                .isExactlyInstanceOf(NonstrictReadWriteNaturalIdRegionAccessStrategy.class);
    }

    @Test
    public void buildAccessStrategy_AccessType_READ_WRITE() throws Exception {
        expectedException.expect(CacheException.class);
        expectedException.expectMessage(containsString(AccessType.READ_WRITE.toString()));
        naturalIdMemcachedRegion.buildAccessStrategy(AccessType.READ_WRITE);
    }


    @Test
    public void buildAccessStrategy_AccessType_TRANSACTIONAL() throws Exception {
        expectedException.expect(CacheException.class);
        expectedException.expectMessage(containsString(AccessType.TRANSACTIONAL.toString()));
        naturalIdMemcachedRegion.buildAccessStrategy(AccessType.TRANSACTIONAL);
    }
}