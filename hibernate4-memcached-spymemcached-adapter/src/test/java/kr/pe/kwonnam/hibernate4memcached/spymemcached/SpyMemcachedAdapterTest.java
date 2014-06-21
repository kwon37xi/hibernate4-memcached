package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import net.spy.memcached.MemcachedClientIF;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class SpyMemcachedAdapterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MemcachedClientIF memcachedClient;

    private SpyMemcachedAdapter spyMemcachedAdapter;

}