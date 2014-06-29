package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.NaturalIdMemcachedRegion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NonstrictReadWriteNaturalIdRegionAccessStrategyTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private NaturalIdMemcachedRegion naturalIdMemcachedRegion;

    private NonstrictReadWriteNaturalIdRegionAccessStrategy nonstrictReadWriteNaturalIdRegionAccessStrategy;

    @Before
    public void setUp() throws Exception {
        nonstrictReadWriteNaturalIdRegionAccessStrategy = new NonstrictReadWriteNaturalIdRegionAccessStrategy(naturalIdMemcachedRegion);
    }


    @Test
    public void insert() throws Exception {
        assertThat(nonstrictReadWriteNaturalIdRegionAccessStrategy.insert("key", "value")).isFalse();
    }

    @Test
    public void afterInsert() throws Exception {
        assertThat(nonstrictReadWriteNaturalIdRegionAccessStrategy.afterInsert("key", "value")).isFalse();
    }

    @Test
    public void update() throws Exception {
        assertThat(nonstrictReadWriteNaturalIdRegionAccessStrategy.update("key", "value")).isFalse();
    }

    @Test
    public void afterUpdate() throws Exception {
        assertThat(nonstrictReadWriteNaturalIdRegionAccessStrategy.afterUpdate("key", "value", null)).isFalse();

        verify(naturalIdMemcachedRegion).evict("key");
    }
}