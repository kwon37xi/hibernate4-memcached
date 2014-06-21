package kr.pe.kwonnam.hibernate4memcached.timestamper;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.TestingSettingsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HibernateCacheTimestamperMemcachedImplTest {
    public static final String CACHE_REGION_PREFIX_FIELD_NAME = "cacheRegionPrefix";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MemcachedAdapter memcachedAdapter;

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor;

    private TestingSettingsBuilder settingsBuilder;

    private Settings settings;

    private OverridableReadOnlyProperties properties;

    private HibernateCacheTimestamperMemcachedImpl timestamper;

    @Before
    public void setUp() throws Exception {
        settingsBuilder = new TestingSettingsBuilder();
        properties = new OverridableReadOnlyPropertiesImpl(new Properties());
    }

    @Test
    public void constructor() throws Exception {
        givenTimestamperWithCacheRegionPrefix("cachetest");

        CacheNamespace cacheNamespace = timestamper.getCacheNamespace();

        assertThat(cacheNamespace.getName()).isEqualTo("cachetest." + HibernateCacheTimestamperMemcachedImpl.class
                .getSimpleName());
        assertThat(cacheNamespace.isNamespaceExpirationRequired()).isFalse();
    }

    private void givenTimestamperWithCacheRegionPrefix(String cacheRegionPrefix) {
        settings = settingsBuilder.setField(CACHE_REGION_PREFIX_FIELD_NAME, cacheRegionPrefix).build();

        timestamper = new HibernateCacheTimestamperMemcachedImpl();
        timestamper.setSettings(settings);
        timestamper.setProperties(properties);
        timestamper.setMemcachedAdapter(memcachedAdapter);

        timestamper.init();
    }

    @Test
    public void constructor_without_cacheRegionPrefix() throws Exception {
        givenTimestamperWithCacheRegionPrefix(null);

        CacheNamespace cacheNamespace = timestamper.getCacheNamespace();

        assertThat(cacheNamespace.getName()).isEqualTo(HibernateCacheTimestamperMemcachedImpl.class.getSimpleName());
        assertThat(cacheNamespace.isNamespaceExpirationRequired()).isFalse();
    }

    @Test
    public void next() throws Exception {
        givenTimestamperWithCacheRegionPrefix("myservice");

        CacheNamespace cacheNamespace = timestamper.getCacheNamespace();

        long expected = 12345L;
        when(memcachedAdapter.increaseCounter(eq(cacheNamespace), eq(HibernateCacheTimestamperMemcachedImpl
                                                                             .TIMESTAMP_KEY),
                                              eq(HibernateCacheTimestamperMemcachedImpl.INCREASE_BY),
                                              anyLong(), eq(HibernateCacheTimestamperMemcachedImpl.EXPIRY_SECONDS)))
                .thenReturn(expected);

        long next = timestamper.next();

        assertThat(next).isEqualTo(expected);
    }

    @Test
    public void next_use_currentTimemillis_for_defaultValue() throws Exception {
        givenTimestamperWithCacheRegionPrefix("myservice");

        long startTimestamp = System.currentTimeMillis();
        timestamper.next();
        long endTimestamp = System.currentTimeMillis();

        verify(memcachedAdapter).increaseCounter(eq(timestamper.getCacheNamespace()),
                                                 eq(HibernateCacheTimestamperMemcachedImpl.TIMESTAMP_KEY),
                                                 eq(HibernateCacheTimestamperMemcachedImpl.INCREASE_BY),
                                                 longArgumentCaptor.capture(),
                                                 eq(HibernateCacheTimestamperMemcachedImpl.EXPIRY_SECONDS));

        assertThat(longArgumentCaptor.getValue()).isGreaterThanOrEqualTo(startTimestamp).isLessThanOrEqualTo
                (endTimestamp);
    }
}