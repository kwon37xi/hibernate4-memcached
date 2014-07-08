package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import net.spy.memcached.*;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.transcoders.Transcoder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static kr.pe.kwonnam.hibernate4memcached.spymemcached.SpyMemcachedAdapter.*;
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
    public void createConnectionFactoryBuilder() throws Exception {
        Properties props = new Properties();
        props.setProperty(HASH_ALGORITHM_PROPERTY_KEY, DefaultHashAlgorithm.NATIVE_HASH.name());
        props.setProperty(OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY, String.valueOf(13579));
        props.setProperty(TRANSCODER_PROPERTY_KEY, FakeTranscoder.class.getName());
        props.setProperty(AUTH_GENERATOR_PROPERTY_KEY, FakeAuthDescriptorGenerator.class.getName());

        ConnectionFactoryBuilder builder = spyMemcachedAdapter.createConnectionFactoryBuilder(new OverridableReadOnlyPropertiesImpl(props));

        ConnectionFactory connectionFactory = builder.build();
        assertThat(connectionFactory.getHashAlg()).isEqualTo(DefaultHashAlgorithm.NATIVE_HASH);
        assertThat(connectionFactory.getOperationTimeout()).isEqualTo(13579);

        Transcoder<Object> transcoder = connectionFactory.getDefaultTranscoder();
        assertThat(transcoder).isExactlyInstanceOf(FakeTranscoder.class);
        FakeTranscoder fakeTranscoder = (FakeTranscoder) transcoder;
        assertThat(fakeTranscoder.isInitialized()).isTrue();

        AuthDescriptor authDescriptor = connectionFactory.getAuthDescriptor();
        assertThat(authDescriptor.getMechs()).isEqualTo(FakeAuthDescriptorGenerator.FAKE_MECHS);
    }

    @Test
    public void authenticate_no_authentication_property() throws Exception {
        Properties props = new Properties();
        ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
        spyMemcachedAdapter.authenticate(builder, new OverridableReadOnlyPropertiesImpl(props));

        assertThat(builder.build().getAuthDescriptor()).isNull();
    }

    @Test
    public void authenticate_with_authentication_proerty() throws Exception {
        Properties props = new Properties();
        props.setProperty(SpyMemcachedAdapter.AUTH_GENERATOR_PROPERTY_KEY, FakeAuthDescriptorGenerator.class.getName());

        ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
        spyMemcachedAdapter.authenticate(builder, new OverridableReadOnlyPropertiesImpl(props));

        ConnectionFactory connectionFactory = builder.build();
        assertThat(connectionFactory.getAuthDescriptor().getMechs()).isEqualTo(FakeAuthDescriptorGenerator.FAKE_MECHS);
        assertThat(connectionFactory.getAuthWaitTime()).isEqualTo(SpyMemcachedAdapter.DEFAULT_AUTH_WAIT_TIME_MILLIS);
    }

    @Test
    public void authenticate_with_authWaitTimeMillis_property() throws Exception {
        Properties props = new Properties();
        props.setProperty(SpyMemcachedAdapter.AUTH_GENERATOR_PROPERTY_KEY, FakeAuthDescriptorGenerator.class.getName());
        props.setProperty(SpyMemcachedAdapter.AUTH_WAIT_TIME_MILLIS_PROPERTY_KEY, String.valueOf(9999L));

        ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
        spyMemcachedAdapter.authenticate(builder, new OverridableReadOnlyPropertiesImpl(props));

        ConnectionFactory connectionFactory = builder.build();
        assertThat(connectionFactory.getAuthDescriptor().getMechs()).isEqualTo(FakeAuthDescriptorGenerator.FAKE_MECHS);
        assertThat(connectionFactory.getAuthWaitTime()).isEqualTo(9999L);
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

        assertThat(spyMemcachedAdapter.get(givenCacheNamespace, givenKey)).isEqualTo(cachedValue);
    }

    @Test
    public void get_null() throws Exception {
        givenNamespaceAndKey(new CacheNamespace("authors", true), "authors#10", "hXm.authors@1:authors#10");

        when(memcachedClient.get(givenNamespacedKey)).thenReturn(null);
        assertThat(spyMemcachedAdapter.get(givenCacheNamespace, givenKey)).isNull();

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

    public static class FakeTranscoder implements InitializableTranscoder<Object> {
        private boolean initialized = false;
        public boolean isInitialized() {
            return initialized;
        }

        @Override
        public boolean asyncDecode(CachedData d) {
            return false;
        }

        @Override
        public CachedData encode(Object o) {
            return null;
        }

        @Override
        public Object decode(CachedData d) {
            return null;
        }

        @Override
        public int getMaxSize() {
            return 0;
        }

        @Override
        public void init(OverridableReadOnlyProperties properties) {
            initialized = true;
        }
    }

    public static class FakeAuthDescriptorGenerator implements AuthDescriptorGenerator {
        public static final String[] FAKE_MECHS = new String[] {"fake", "mechs"};

        @Override
        public AuthDescriptor generate(OverridableReadOnlyProperties properties) {
            return new AuthDescriptor(FAKE_MECHS, null);
        }
    }
}