package kr.pe.kwonnam.hibernate4memcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.regions.*;
import kr.pe.kwonnam.hibernate4memcached.timestamper.FakeHibernateCacheTimestamper;
import kr.pe.kwonnam.hibernate4memcached.timestamper.HibernateCacheTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import org.hamcrest.CoreMatchers;
import org.hibernate.cache.spi.*;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.TestingSettingsBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Hibernate4MemcachedRegionFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MemcachedAdapter memcachedAdapter;

    @Mock
    private HibernateCacheTimestamper hibernateCacheTimestamper;

    @Mock
    private CacheDataDescription metadata;

    @Spy
    private Hibernate4MemcachedRegionFactory regionFactory = new Hibernate4MemcachedRegionFactory();

    private Settings settings;
    private Properties properties;

    private void givenStart() {
        properties = new Properties();
        properties.setProperty(Hibernate4MemcachedRegionFactory.REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX, "300");
        settings = new TestingSettingsBuilder().build();

        doReturn(new Properties()).when(regionFactory).populateCacheProviderConfigProperties(properties);
        doReturn(memcachedAdapter).when(regionFactory).populateMemcachedProvider(any(OverridableReadOnlyProperties.class));
        doReturn(hibernateCacheTimestamper).when(regionFactory).populateTimestamper(eq(settings), any(OverridableReadOnlyProperties.class));

        regionFactory.start(settings, properties);
    }

    @Test
    public void populateMemcachedProvider_no_property() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(CoreMatchers.containsString(Hibernate4MemcachedRegionFactory
                .MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY));

        regionFactory.populateMemcachedProvider(new OverridableReadOnlyPropertiesImpl(new Properties()));
    }

    @Test
    public void populateMemcachedProvider() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Hibernate4MemcachedRegionFactory.MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY,
                FakeMemcachedAdapter.class.getName());

        MemcachedAdapter memcachedAdapter = regionFactory.populateMemcachedProvider(new
                OverridableReadOnlyPropertiesImpl(properties));

        assertThat(memcachedAdapter).isNotNull().isInstanceOf(FakeMemcachedAdapter.class);
        assertThat(((FakeMemcachedAdapter) memcachedAdapter).isInitCalled()).isTrue();

    }

    @Test
    public void popualteCacheConfigProperties_no_cacheProviderConfig() throws Exception {
        Properties sessionFactoryProperties = new Properties();

        Properties properties = regionFactory.populateCacheProviderConfigProperties(sessionFactoryProperties);
        assertThat(properties).hasSize(0);
    }

    @Test
    public void populateCacheProviderConfigProperties_normal_properties() throws Exception {
        Properties sessionFactoryProperties = new Properties();
        sessionFactoryProperties.setProperty(AvailableSettings.CACHE_PROVIDER_CONFIG,
                "kr/pe/kwonnam/hibernate4memcached/util/normal.properties");

        Properties properties = regionFactory.populateCacheProviderConfigProperties(sessionFactoryProperties);
        assertThat(properties).hasSize(1);
    }

    @Test
    public void populateTimestamper_default() throws Exception {
        Properties properties = new Properties();
        Settings settings = new TestingSettingsBuilder().build();

        HibernateCacheTimestamper timestamper = regionFactory.populateTimestamper(settings,
                new OverridableReadOnlyPropertiesImpl(properties));

        assertThat(timestamper).isExactlyInstanceOf(Hibernate4MemcachedRegionFactory.DEFAULT_TIMESTAMPER_CLASS);
    }

    @Test
    public void populateTimestamper() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Hibernate4MemcachedRegionFactory.TIMESTAMPER_PROPERTY_KEY,
                FakeHibernateCacheTimestamper.class.getName());

        Settings settings = new TestingSettingsBuilder().build();

        HibernateCacheTimestamper timestamper = regionFactory.populateTimestamper(settings,
                new OverridableReadOnlyPropertiesImpl(properties));

        assertThat(timestamper).isExactlyInstanceOf(FakeHibernateCacheTimestamper.class);
        assertThat(((FakeHibernateCacheTimestamper) timestamper).isInitCalled()).isTrue();
    }

    @Test
    public void stop() throws Exception {
        givenStart();
        regionFactory.stop();

        verify(memcachedAdapter).destroy();
    }

    @Test
    public void isMinimalPutsEnabledByDefault() throws Exception {
        assertThat(regionFactory.isMinimalPutsEnabledByDefault()).isFalse();
    }

    @Test
    public void getDefaultAccessType() throws Exception {
        assertThat(regionFactory.getDefaultAccessType()).isEqualTo(Hibernate4MemcachedRegionFactory.DEFAULT_ACCESS_TYPE);
    }

    @Test
    public void nextTimestamp() throws Exception {
        givenStart();
        when(hibernateCacheTimestamper.next()).thenReturn(98765L);

        assertThat(regionFactory.nextTimestamp()).isEqualTo(98765L);
    }

    @Test
    public void buildEntityRegion() throws Exception {
        givenStart();

        EntityRegion entityRegion = regionFactory.buildEntityRegion("books", properties, metadata);
        assertThat(entityRegion.getName()).isEqualTo("books");
        assertThat(entityRegion).isExactlyInstanceOf(EntityMemcachedRegion.class);
    }

    @Test
    public void buildNaturalIdRegion() throws Exception {
        givenStart();

        NaturalIdRegion naturalIdRegion = regionFactory.buildNaturalIdRegion("books#naturalId", properties, metadata);
        assertThat(naturalIdRegion.getName()).isEqualTo("books#naturalId");
        assertThat(naturalIdRegion).isExactlyInstanceOf(NaturalIdMemcachedRegion.class);
    }

    @Test
    public void buildCollectionRegion() throws Exception {
        givenStart();

        CollectionRegion collectionRegion = regionFactory.buildCollectionRegion("collectionRegion", properties, metadata);
        assertThat(collectionRegion.getName()).isEqualTo("collectionRegion");
        assertThat(collectionRegion).isExactlyInstanceOf(CollectionMemcachedRegion.class);
    }

    @Test
    public void buildQueryResultsRegion() throws Exception {
        givenStart();

        QueryResultsRegion queryResultsRegion = regionFactory.buildQueryResultsRegion("StandardQueryCache", properties);
        assertThat(queryResultsRegion.getName()).isEqualTo("StandardQueryCache");
        assertThat(queryResultsRegion).isExactlyInstanceOf(QueryResultsMemcachedRegion.class);
    }

    @Test
    public void buildTimestampsRegion() throws Exception {
        givenStart();

        TimestampsRegion timestampsRegion = regionFactory.buildTimestampsRegion("UpdateTimestamps", properties);
        assertThat(timestampsRegion.getName()).isEqualTo("UpdateTimestamps");
        assertThat(timestampsRegion).isExactlyInstanceOf(TimestampMemcachedRegion.class);
    }
}