package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.strategies.NonstrictReadWriteEntityRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.strategies.ReadOnlyEntityRegionAccessStrategy;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class EntityMemcachedRegionTest extends AbstractFinalMemcachedRegionTest {

    private EntityMemcachedRegion entityMemcachedRegion;

    @Override
    protected void afterSetUp() throws Exception {
        entityMemcachedRegion = new EntityMemcachedRegion("Books", properties, metadata, settings, memcachedAdapter, hibernateCacheTimestamper);
    }

    @Test
    public void buildAccessStrategy_AccessType_READ_ONLY() throws Exception {
        assertThat(entityMemcachedRegion.buildAccessStrategy(AccessType.READ_ONLY))
                .isExactlyInstanceOf(ReadOnlyEntityRegionAccessStrategy.class);
    }

    @Test
    public void buildAccessStrategy_AccessType_NONSTRICT_READ_WRITE() throws Exception {
        assertThat(entityMemcachedRegion.buildAccessStrategy(AccessType.NONSTRICT_READ_WRITE))
                .isExactlyInstanceOf(NonstrictReadWriteEntityRegionAccessStrategy.class);
    }

    @Test
    public void buildAccessStrategy_AccessType_READ_WRITE() throws Exception {
        expectedException.expect(CacheException.class);
        expectedException.expectMessage(containsString(AccessType.READ_WRITE.toString()));
        entityMemcachedRegion.buildAccessStrategy(AccessType.READ_WRITE);
    }


    @Test
    public void buildAccessStrategy_AccessType_TRANSACTIONAL() throws Exception {
        expectedException.expect(CacheException.class);
        expectedException.expectMessage(containsString(AccessType.TRANSACTIONAL.toString()));
        entityMemcachedRegion.buildAccessStrategy(AccessType.TRANSACTIONAL);
    }
}