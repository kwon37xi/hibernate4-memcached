package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory;
import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.timestamper.HibernateCacheTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.TestingSettingsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractFinalMemcachedRegionTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    protected CacheNamespace cacheNamespace;

    @Mock
    protected CacheDataDescription metadata;

    @Mock
    protected MemcachedAdapter memcachedAdapter;

    @Mock
    protected HibernateCacheTimestamper hibernateCacheTimestamper;

    protected OverridableReadOnlyProperties properties;

    protected Settings settings;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(Hibernate4MemcachedRegionFactory.REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX, "300");
        properties = new OverridableReadOnlyPropertiesImpl(props);

        settings = new TestingSettingsBuilder().build();

        afterSetUp();
    }

    protected abstract void afterSetUp() throws Exception;
}
