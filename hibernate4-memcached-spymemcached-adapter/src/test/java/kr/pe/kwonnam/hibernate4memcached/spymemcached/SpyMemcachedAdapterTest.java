package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import net.spy.memcached.MemcachedClientIF;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static kr.pe.kwonnam.hibernate4memcached.spymemcached.SpyMemcachedAdapter.DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpyMemcachedAdapterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private MemcachedClientIF memcachedClient;

    @Captor
    private ArgumentCaptor<Long> longCaptor;

    private SpyMemcachedAdapter spyMemcachedAdapter;

    private long startTimeMills;

    private CacheNamespace givenCacheNamespace;
    private String givenKey;
    private String givenNamespacedKey;

    @Before
    public void setUp() throws Exception {
        spyMemcachedAdapter = new SpyMemcachedAdapter();
        spyMemcachedAdapter.setMemcachedClient(memcachedClient);
        spyMemcachedAdapter.setCacheKeyPrefix("hXm");
        spyMemcachedAdapter = spy(spyMemcachedAdapter);

        startTimeMills = System.currentTimeMillis();
    }

    @Test
    public void getNamespacedKey_namespaceExpirationRequired_true() throws Exception {
        CacheNamespace cacheNamespace = new CacheNamespace("books", true);

        when(memcachedClient.incr(eq("hXm.books@"), eq(0L), anyLong(), eq(DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS)))
                .thenReturn(123L);

        String namspacedKey = spyMemcachedAdapter.getNamespacedKey(cacheNamespace, "books#1");

        assertThat(namspacedKey).isEqualTo("hXm.books@123:books#1");
        verify(memcachedClient).incr(eq("hXm.books@"), eq(0L), longCaptor.capture(), eq(DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS));
        assertSystemCurrentTimeMillis(longCaptor.getValue());
    }

    private void assertSystemCurrentTimeMillis(long value) {
        assertThat(value).isGreaterThanOrEqualTo(startTimeMills).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    @Test
    public void getNamespacedKey_namespaceExpirationRequired_false() throws Exception {
        CacheNamespace cacheNamespace = new CacheNamespace("authors", false);

        String namespacedKey = spyMemcachedAdapter.getNamespacedKey(cacheNamespace, "authors#4");

        assertThat(namespacedKey).isEqualTo("hXm.authors:authors#4");
    }

    @Test
    public void getNamespacedKey_no_cacheKeyPrefix() throws Exception {
        spyMemcachedAdapter.setCacheKeyPrefix(null);
        CacheNamespace cacheNamespace = new CacheNamespace("UpdateTimestamp", false);
        String namespacedKey = spyMemcachedAdapter.getNamespacedKey(cacheNamespace, "authors");
        assertThat(namespacedKey).isEqualTo("UpdateTimestamp:authors");
    }

    @Test
    public void destroy() throws Exception {
        spyMemcachedAdapter.destroy();

        verify(memcachedClient).shutdown();
    }

    private void givenNamespaceAndKey(CacheNamespace cacheNamespace, String key, String namespacedKey) {
        this.givenCacheNamespace = cacheNamespace;
        this.givenKey = key;
        this.givenNamespacedKey = namespacedKey;
        doReturn(givenNamespacedKey).when(spyMemcachedAdapter).getNamespacedKey(givenCacheNamespace, givenKey);
    }

    @Test
    public void get() throws Exception {
        givenNamespaceAndKey(new CacheNamespace("books", true), "book#1", "hXm.books@111:book#1");

        String cachedValue = "cached value";
        when(memcachedClient.get(givenNamespacedKey)).thenReturn(cachedValue);

        Object actual = spyMemcachedAdapter.get(givenCacheNamespace, givenKey);

        assertThat(actual).isEqualTo(cachedValue);
    }

    @Test
    public void set() throws Exception {
        givenNamespaceAndKey(new CacheNamespace("authors", true), "authors#1223", "hXm.books@1234:authors#1223");

        String value = "my value";
        spyMemcachedAdapter.set(givenCacheNamespace, givenKey, value, 300);

        verify(memcachedClient).set(givenNamespacedKey, 300, value);
    }

    @Test
    public void delete() throws Exception {
        givenNamespaceAndKey(new CacheNamespace("StandardQueryCache", false), "laptops", "hXm.StandardQueryCache:laptops");

        spyMemcachedAdapter.delete(givenCacheNamespace, givenKey);

        verify(memcachedClient).delete(givenNamespacedKey);
    }

    @Test
    public void increaseCounter() throws Exception {
        givenNamespaceAndKey(new CacheNamespace("MemcachedTimestamper", false), "timestamper", "hXm.MemcachedTimestamper:timestamper");

        long by = 5L;
        long defaultValue = 1L;
        int expirySeconds = 123;

        when(memcachedClient.incr(givenNamespacedKey, by, defaultValue, expirySeconds)).thenReturn(98765L);
        long actual = spyMemcachedAdapter.increaseCounter(givenCacheNamespace, givenKey, by, defaultValue, expirySeconds);

        assertThat(actual).isEqualTo(98765L);
    }

    @Test
    public void getCounter() throws Exception {
        givenNamespaceAndKey(new CacheNamespace("MemcachedTimestamper", false), "timestamper", "hXm.MemcachedTimestamper:timestamper");

        long defaultValue = 10L;
        int expirySeconds = 123;
        when(memcachedClient.incr(givenNamespacedKey, 0, defaultValue, expirySeconds)).thenReturn(100L);

        long actual = spyMemcachedAdapter.getCounter(givenCacheNamespace, givenKey, defaultValue, expirySeconds);

        assertThat(actual).isEqualTo(100L);
    }

    @Test
    public void evictAll_namespaceExpirationRequired_true() throws Exception {
        CacheNamespace cacheNamespace = new CacheNamespace("people", true);

        spyMemcachedAdapter.evictAll(cacheNamespace);

        verify(memcachedClient).incr(eq("hXm.people@"), eq(1), longCaptor.capture(), eq(DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS));
        assertSystemCurrentTimeMillis(longCaptor.getValue());
    }

    @Test
    public void evictAll_namespaceExpirationRequired_false() throws Exception {
        CacheNamespace cacheNamespace = new CacheNamespace("StandardQueryCache", false);

        spyMemcachedAdapter.evictAll(cacheNamespace);

        verify(memcachedClient, never()).incr(anyString(), anyInt(), anyLong(), anyInt());
    }

    @Test
    public void evictAll_no_cacheKeyPrefix() throws Exception {
        spyMemcachedAdapter.setCacheKeyPrefix("");

        CacheNamespace cacheNamespace = new CacheNamespace("people", true);

        spyMemcachedAdapter.evictAll(cacheNamespace);

        verify(memcachedClient).incr(eq("people@"), eq(1), longCaptor.capture(), eq(DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS));
        assertSystemCurrentTimeMillis(longCaptor.getValue());
    }
}