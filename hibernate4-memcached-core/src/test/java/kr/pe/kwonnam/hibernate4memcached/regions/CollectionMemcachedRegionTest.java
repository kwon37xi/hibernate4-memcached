package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.strategies.NonstrictReadWriteCollectionRegionAccessStrategy;
import kr.pe.kwonnam.hibernate4memcached.strategies.ReadOnlyCollectionRegionAccessStrategy;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class CollectionMemcachedRegionTest extends AbstractFinalMemcachedRegionTest {

    private CollectionMemcachedRegion collectionMemcachedRegion;

    @Override
    protected void afterSetUp() throws Exception {
        collectionMemcachedRegion = new CollectionMemcachedRegion("books.authors", properties, metadata, settings, memcachedAdapter, hibernateCacheTimestamper);
    }

    @Test
    public void buildAccessStrategy_AccessType_READ_ONLY() throws Exception {
        assertThat(collectionMemcachedRegion.buildAccessStrategy(AccessType.READ_ONLY))
                .isExactlyInstanceOf(ReadOnlyCollectionRegionAccessStrategy.class);
    }

    @Test
    public void buildAccessStrategy_AccessType_NONSTRICT_READ_WRITE() throws Exception {
        assertThat(collectionMemcachedRegion.buildAccessStrategy(AccessType.NONSTRICT_READ_WRITE))
                .isExactlyInstanceOf(NonstrictReadWriteCollectionRegionAccessStrategy.class);
    }

    @Test
    public void buildAccessStrategy_AccessType_READ_WRITE() throws Exception {
        expectedException.expect(CacheException.class);
        expectedException.expectMessage(containsString(AccessType.READ_WRITE.toString()));
        collectionMemcachedRegion.buildAccessStrategy(AccessType.READ_WRITE);
    }


    @Test
    public void buildAccessStrategy_AccessType_TRANSACTIONAL() throws Exception {
        expectedException.expect(CacheException.class);
        expectedException.expectMessage(containsString(AccessType.TRANSACTIONAL.toString()));
        collectionMemcachedRegion.buildAccessStrategy(AccessType.TRANSACTIONAL);
    }
}