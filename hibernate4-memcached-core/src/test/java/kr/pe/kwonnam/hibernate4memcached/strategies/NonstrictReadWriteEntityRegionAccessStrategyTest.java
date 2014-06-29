package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.EntityMemcachedRegion;
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
public class NonstrictReadWriteEntityRegionAccessStrategyTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private EntityMemcachedRegion entityMemcachedRegion;

    private NonstrictReadWriteEntityRegionAccessStrategy nonstrictReadWriteEntityRegionAccessStrategy;

    @Before
    public void setUp() throws Exception {
        nonstrictReadWriteEntityRegionAccessStrategy = new NonstrictReadWriteEntityRegionAccessStrategy(entityMemcachedRegion);
    }

    @Test
    public void insert() throws Exception {
        assertThat(nonstrictReadWriteEntityRegionAccessStrategy.insert("key", "value", "version")).isFalse();
    }

    @Test
    public void afterInsert() throws Exception {
        assertThat(nonstrictReadWriteEntityRegionAccessStrategy.afterInsert("key", "value", "version")).isFalse();
    }

    @Test
    public void update() throws Exception {
        assertThat(nonstrictReadWriteEntityRegionAccessStrategy.update("key", "value", "currentVersion", "previousVersion")).isFalse();
    }

    @Test
    public void afterUpdate() throws Exception {
        assertThat(nonstrictReadWriteEntityRegionAccessStrategy.afterUpdate("key", "value", "currentVersion", "previousVersion", null)).isFalse();

        verify(entityMemcachedRegion).evict("key");
    }
}