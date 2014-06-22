package kr.pe.kwonnam.hibernate4memcached.regions;

import kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.timestamper.HibernateCacheTimestamper;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.cfg.TestingSettingsBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class QueryResultsMemcachedRegionTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MemcachedAdapter memcachedAdapter;

    @Mock
    private HibernateCacheTimestamper timestamper;

    private QueryResultsMemcachedRegion queryResultsMemcachedRegion;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Hibernate4MemcachedRegionFactory.REGION_EXPIRY_SECONDS_PROPERTY_KEY_PREFIX, "300");
        queryResultsMemcachedRegion = new QueryResultsMemcachedRegion("queryRegion",
                new OverridableReadOnlyPropertiesImpl(properties), new TestingSettingsBuilder().build(),
                memcachedAdapter,
                timestamper);
    }

    @Test
    public void refineKey() throws Exception {
        String key = "select something from Entity where many many conditions are satisfied.";
        String refinedKey = queryResultsMemcachedRegion.refineKey(key);

        assertThat(refinedKey).isEqualTo(DigestUtils.md5Hex(key) + "_" + String.valueOf(key.hashCode()));
    }
}