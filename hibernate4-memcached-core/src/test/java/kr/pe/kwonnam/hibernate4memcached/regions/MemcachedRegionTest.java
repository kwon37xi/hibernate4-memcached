package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.timestamper.HibernateCacheTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.TestingSettingsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MemcachedRegionTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private CacheNamespace cacheNamespace;

    @Mock
    private OverridableReadOnlyProperties properties;

    @Mock
    private CacheDataDescription metadata;

    @Mock
    private MemcachedAdapter memcachedAdapter;

    @Mock
    private HibernateCacheTimestamper hibernateCacheTimestamper;

    private Settings settings;

    private MemcachedRegion memcachedRegion;

    @Before
    public void setUp() throws Exception {
        settings = new TestingSettingsBuilder().build();
        memcachedRegion = new MemcachedRegion(cacheNamespace, properties, metadata, settings, memcachedAdapter, hibernateCacheTimestamper);
    }

    @Test
    public void getName() throws Exception {
        when(cacheNamespace.getName()).thenReturn("myCacheRegion");

        assertThat(memcachedRegion.getName()).isEqualTo("myCacheRegion");
    }

    @Test
    public void nextTimestamp() {
        when(hibernateCacheTimestamper.next()).thenReturn(123L);
        assertThat(memcachedRegion.nextTimestamp()).isEqualTo(123L);
    }
}