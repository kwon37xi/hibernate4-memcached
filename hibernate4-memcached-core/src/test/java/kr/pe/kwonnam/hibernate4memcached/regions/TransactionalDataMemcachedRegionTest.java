package kr.pe.kwonnam.hibernate4memcached.regions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalDataMemcachedRegionTest extends AbstractFinalMemcachedRegionTest {

    private TransactionalDataMemcachedRegion transactionalDataMemcachedRegion;

    @Override
    public void afterSetUp() throws Exception {
        transactionalDataMemcachedRegion = new TransactionalDataMemcachedRegion(cacheNamespace, properties, metadata, settings, memcachedAdapter, hibernateCacheTimestamper);
    }

    @Test
    public void isTransactionAware() throws Exception {
        assertThat(transactionalDataMemcachedRegion.isTransactionAware()).isFalse();
    }

    @Test
    public void getCacheDataDescription() throws Exception {
        assertThat(transactionalDataMemcachedRegion.getCacheDataDescription()).isEqualTo(metadata);
    }
}