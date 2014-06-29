package kr.pe.kwonnam.hibernate4memcached.strategies;

import kr.pe.kwonnam.hibernate4memcached.regions.EntityMemcachedRegion;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class ReadOnlyEntityRegionAccessStrategyTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private EntityMemcachedRegion entityMemcachedRegion;

    private ReadOnlyEntityRegionAccessStrategy readOnlyEntityRegionAccessStrategy;

    @Before
    public void setUp() throws Exception {
        readOnlyEntityRegionAccessStrategy = new ReadOnlyEntityRegionAccessStrategy(entityMemcachedRegion);
    }

    @Test
    public void insert() throws Exception {
        assertThat(readOnlyEntityRegionAccessStrategy.insert("key", "value", "version")).isFalse();
    }

    @Test
    public void afterInsert() throws Exception {
        assertThat(readOnlyEntityRegionAccessStrategy.afterInsert("key", "value", "version")).isFalse();
    }

    @Test
    public void update() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        readOnlyEntityRegionAccessStrategy.update("key", "value", "currentVersion", "previousVersion");
    }

    @Test
    public void afterUpdate() throws Exception {
        expectedException.expect(UnsupportedOperationException.class);
        readOnlyEntityRegionAccessStrategy.afterUpdate("key", "value", "currentVersion", "previousVersion", null);
    }
}