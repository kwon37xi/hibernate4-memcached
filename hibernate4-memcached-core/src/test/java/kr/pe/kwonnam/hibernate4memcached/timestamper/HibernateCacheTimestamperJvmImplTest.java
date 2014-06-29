package kr.pe.kwonnam.hibernate4memcached.timestamper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.fest.assertions.api.Assertions.*;

public class HibernateCacheTimestamperJvmImplTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private HibernateCacheTimestamperJvmImpl hibernateCacheTimestamperJvm;

    @Before
    public void setUp() throws Exception {
        hibernateCacheTimestamperJvm = new HibernateCacheTimestamperJvmImpl();
    }

    @Test
    public void next() throws Exception {
        long start = System.currentTimeMillis();

        Thread.sleep(1);
        long next = hibernateCacheTimestamperJvm.next();

        assertThat(next).isGreaterThanOrEqualTo(start).isLessThanOrEqualTo(System.currentTimeMillis());
    }
}