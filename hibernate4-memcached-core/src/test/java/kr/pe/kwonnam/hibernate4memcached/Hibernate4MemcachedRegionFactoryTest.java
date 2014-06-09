package kr.pe.kwonnam.hibernate4memcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

public class Hibernate4MemcachedRegionFactoryTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Hibernate4MemcachedRegionFactory regionFactory;

    @Before
    public void setUp() throws Exception {
        regionFactory = new Hibernate4MemcachedRegionFactory();
    }

    @Test
    public void populateMemcachedProvider_no_property() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(CoreMatchers.containsString(Hibernate4MemcachedRegionFactory.MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY));

        regionFactory.populateMemcachedProvider(new Properties());
    }

    @Test
    public void populateMemcachedProvider() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Hibernate4MemcachedRegionFactory.MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY, FakeMemcachedAdapter.class.getName());

        MemcachedAdapter memcachedAdapter = regionFactory.populateMemcachedProvider(properties);

        assertThat(memcachedAdapter).isNotNull().isInstanceOf(FakeMemcachedAdapter.class);
    }
}