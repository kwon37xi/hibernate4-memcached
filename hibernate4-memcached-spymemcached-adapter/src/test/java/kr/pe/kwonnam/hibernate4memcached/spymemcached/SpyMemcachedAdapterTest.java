package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import net.spy.memcached.MemcachedClientIF;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class SpyMemcachedAdapterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MemcachedClientIF memcachedClient;

    private SpyMemcachedAdapter spyMemcachedAdapter;

}