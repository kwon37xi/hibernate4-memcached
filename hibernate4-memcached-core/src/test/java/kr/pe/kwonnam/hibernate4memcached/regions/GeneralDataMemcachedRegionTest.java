package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.timestamper.HibernateCacheTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.TestingSettingsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory.REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GeneralDataMemcachedRegionTest {
    public static final String REFINED_KEY_PREFIX = "RefinedKey.";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MemcachedAdapter memcachedAdapter;

    @Mock
    private HibernateCacheTimestamper timestamper;

    @Mock
    private CacheDataDescription cacheDataDescription;

    @Mock
    private CacheItem cacheItem;

    private GeneralDataMemcachedRegion generalDataMemcachedRegion;

    private Settings settings;

    private Properties givenProperties;
    private CacheNamespace cacheNamespace;

    @Before
    public void setUp() throws Exception {
        cacheNamespace = new CacheNamespace("books", true);
        settings = new TestingSettingsBuilder().build();
    }

    private void givenRegionWithProperties(Properties properties) throws Exception {
        this.givenProperties = properties;

        generalDataMemcachedRegion = new RefineKeyOverridedGeneralDataMemcachedRegion(cacheNamespace,
                new OverridableReadOnlyPropertiesImpl(properties), cacheDataDescription, settings, memcachedAdapter,
                timestamper);
    }

    private void givenRegionWithDefaultProperties() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX, String.valueOf(12345));

        givenRegionWithProperties(properties);
    }

    @Test
    public void populateExpirySeconds_no_expiry_properties() throws Exception {
        try {
            givenRegionWithProperties(new Properties());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (Exception ex) {
            assertThat(ex).isInstanceOf(IllegalStateException.class).hasMessageContaining
                    (REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX + "." + cacheNamespace.getName()).hasMessageContaining
                    (REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX + "(for default expiry seconds) required!");
        }
    }

    @Test
    public void populateExpirySeconds_default_properties() throws Exception {
        int expected = 12345;

        Properties properties = new Properties();
        properties.setProperty(REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX, String.valueOf(expected));

        givenRegionWithProperties(properties);

        assertThat(generalDataMemcachedRegion.getExpiryInSeconds()).isEqualTo(expected);
    }

    @Test
    public void populateExpirySeconds_region_properties() throws Exception {
        int expected = 98765;

        Properties properties = new Properties();
        properties.setProperty(REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX, "12345");
        properties.setProperty(REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX + "." + cacheNamespace.getName(),
                String.valueOf(expected));

        givenRegionWithProperties(properties);

        assertThat(generalDataMemcachedRegion.getExpiryInSeconds()).isEqualTo(expected);
    }

    @Test
    public void get_null() throws Exception {
        givenRegionWithDefaultProperties();

        String key = "someKey";
        when(memcachedAdapter.get(cacheNamespace, refineKey(key))).thenReturn(null);

        assertThat(generalDataMemcachedRegion.get(key)).isNull();

        verify(memcachedAdapter).get(cacheNamespace, refineKey(key));
    }

    @Test
    public void get_no_CacheItem() throws Exception {
        givenRegionWithDefaultProperties();
        String key = "aKey";
        List<Long> cacheValue = new ArrayList<Long>();
        cacheValue.add(1L);
        cacheValue.add(2L);

        when(memcachedAdapter.get(cacheNamespace, refineKey(key))).thenReturn(cacheValue);

        assertThat(generalDataMemcachedRegion.get(key)).isEqualTo(cacheValue);
    }

    @Test
    public void get_CacheItem_classMatch_true() throws Exception {
        givenRegionWithDefaultProperties();
        String cacheValue = "hello world!";
        givenCacheItem(true, cacheValue);

        String key = "greeting";
        when(memcachedAdapter.get(cacheNamespace, refineKey(key))).thenReturn(cacheItem);

        assertThat(generalDataMemcachedRegion.get(key)).isEqualTo(cacheValue);
    }

    private void givenCacheItem(boolean classMatch, String cacheEntry) {
        when(cacheItem.isTargetClassAndCurrentJvmTargetClassMatch()).thenReturn(classMatch);
        when(cacheItem.getCacheEntry()).thenReturn(cacheEntry);
    }

    @Test
    public void get_CacheItem_classMatch_false() throws Exception {
        givenRegionWithDefaultProperties();
        String cacheValue = "you are not in my world";
        givenCacheItem(false, cacheValue);

        String key = "greeting";
        when(memcachedAdapter.get(cacheNamespace, refineKey(key))).thenReturn(cacheItem);

        assertThat(generalDataMemcachedRegion.get(key)).isNull();

    }

    @Test
    public void put_classVersionApplicable_false() throws Exception {
        givenRegionWithDefaultProperties();
        String key = "greeting";
        String cacheValue = "hello baby~";

        generalDataMemcachedRegion.put(key, cacheValue);

        verify(memcachedAdapter).set(cacheNamespace, refineKey(key), cacheValue, generalDataMemcachedRegion
                .getExpiryInSeconds());
    }

    @Test
    public void put_classVersionApplicable_true() throws Exception {
        givenRegionWithDefaultProperties();
        String key = "greeting";
        CacheEntry cacheEntry = mock(CacheEntry.class);
        when(cacheEntry.getSubclass()).thenReturn(String.class.getName());

        ArgumentCaptor<CacheItem> cacheItemArgumentCaptor = ArgumentCaptor.forClass(CacheItem.class);

        generalDataMemcachedRegion.put(key, cacheEntry);

        verify(memcachedAdapter).set(eq(cacheNamespace), eq(refineKey(key)), cacheItemArgumentCaptor.capture(),
                eq(generalDataMemcachedRegion.getExpiryInSeconds()));

        CacheItem cacheItem = cacheItemArgumentCaptor.getValue();
        assertThat(cacheItem.getCacheEntry()).isEqualTo(cacheEntry);
        assertThat(cacheItem.getTargetClassName()).isEqualTo(String.class.getName());
    }

    public class RefineKeyOverridedGeneralDataMemcachedRegion extends GeneralDataMemcachedRegion {

        public RefineKeyOverridedGeneralDataMemcachedRegion(CacheNamespace cacheNamespace,
                                                            OverridableReadOnlyProperties properties,
                                                            CacheDataDescription metadata, Settings settings,
                                                            MemcachedAdapter memcachedAdapter,
                                                            HibernateCacheTimestamper hibernateCacheTimestamper) {
            super(cacheNamespace, properties, metadata, settings, memcachedAdapter, hibernateCacheTimestamper);
        }

        @Override
        public String refineKey(Object key) {
            return GeneralDataMemcachedRegionTest.this.refineKey(key);
        }
    }

    String refineKey(Object key) {
        return REFINED_KEY_PREFIX + key;
    }
}