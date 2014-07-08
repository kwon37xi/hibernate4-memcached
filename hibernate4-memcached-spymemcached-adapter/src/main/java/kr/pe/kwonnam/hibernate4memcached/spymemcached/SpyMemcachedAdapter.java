package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory;
import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import net.spy.memcached.*;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * SpymemcachedAdapter for hibernate4memcached.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class SpyMemcachedAdapter implements MemcachedAdapter {
    public static final String PROPERTY_KEY_PREFIX = "h4m.adapter.spymemcached";
    public static final String HOST_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".hosts";
    public static final String HASH_ALGORITHM_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".hashalgorithm";
    public static final String OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".operation.timeout.millis";
    public static final String TRANSCODER_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".transcoder";
    public static final String CACHE_KEY_PREFIX_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".cachekey.prefix";
    public static final String AUTH_GENERATOR_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".auth.generator";
    public static final String AUTH_WAIT_TIME_MILLIS_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".auth.waittime.millis";

    public static final String NAMESPACE_NAME_SQUENCE_SEPARATOR = "@";

    public static final int DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS = Hibernate4MemcachedRegionFactory.MEMCACHED_MAX_EPIRY_SECONDS;
    public static final long DEFAULT_AUTH_WAIT_TIME_MILLIS = 1000L;

    private Logger log = LoggerFactory.getLogger(SpyMemcachedAdapter.class);

    private MemcachedClientIF memcachedClient;

    private String cacheKeyPrefix = "";

    public void setMemcachedClient(MemcachedClientIF memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public void setCacheKeyPrefix(String cacheKeyPrefix) {
        this.cacheKeyPrefix = cacheKeyPrefix;
    }

    @Override
    public void init(OverridableReadOnlyProperties properties) {
        memcachedClient = createMemcachedClient(properties);
        cacheKeyPrefix = properties.getProperty(CACHE_KEY_PREFIX_PROPERTY_KEY, "");

        log.debug("spymemcachedadapter cachekeyprefix : [{}]", cacheKeyPrefix);
    }

    protected MemcachedClientIF createMemcachedClient(OverridableReadOnlyProperties properties) {
        ConnectionFactoryBuilder builder = createConnectionFactoryBuilder(properties);

        try {
            String addresses = properties.getRequiredProperty(HOST_PROPERTY_KEY);
            return new MemcachedClient(builder.build(), AddrUtil.getAddresses(addresses));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * creating ConnectionFactoryBuilder object. Override thid method if you need.
     */
    protected ConnectionFactoryBuilder createConnectionFactoryBuilder(OverridableReadOnlyProperties properties) {
        ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
        // BINARY Only!!! spymemcached incr/decr correctly supports only BINARY mode.
        builder.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY);

        builder.setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT);
        builder.setUseNagleAlgorithm(false);
        builder.setFailureMode(FailureMode.Redistribute);

        String hashAlgorithmProeprty = properties.getRequiredProperty(HASH_ALGORITHM_PROPERTY_KEY);
        builder.setHashAlg(DefaultHashAlgorithm.valueOf(hashAlgorithmProeprty));

        String operationTimeoutProperty = properties.getRequiredProperty(OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY);
        builder.setOpTimeout(Long.parseLong(operationTimeoutProperty));

        String transcoderClassProperty = properties.getRequiredProperty(TRANSCODER_PROPERTY_KEY);
        builder.setTranscoder(createTranscoder(properties, transcoderClassProperty));

        authenticate(builder, properties);
        return builder;
    }

    void authenticate(ConnectionFactoryBuilder builder, OverridableReadOnlyProperties properties) {
        String authGeneratorClassName = properties.getProperty(AUTH_GENERATOR_PROPERTY_KEY);
        if (StringUtils.isEmpty(authGeneratorClassName)) {
            return;
        }
        try {
            Class<AuthDescriptorGenerator> authGeneratorClass = (Class<AuthDescriptorGenerator>) Class.forName(authGeneratorClassName);
            AuthDescriptorGenerator authDescriptorGenerator = authGeneratorClass.newInstance();
            builder.setAuthDescriptor(authDescriptorGenerator.generate(properties));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(authGeneratorClassName + " does not exists.", e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(authGeneratorClassName + " class can not be instanticated", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(authGeneratorClassName + " class can not be instanticated", e);
        }

        String authWaitTimeMillis = properties.getProperty(AUTH_WAIT_TIME_MILLIS_PROPERTY_KEY, String.valueOf(DEFAULT_AUTH_WAIT_TIME_MILLIS));
        builder.setAuthWaitTime(Long.parseLong(authWaitTimeMillis));
    }

    private Transcoder<Object> createTranscoder(OverridableReadOnlyProperties properties, String transcoderClassProperty) {
        try {
            @SuppressWarnings("unchecked")
            Class<InitializableTranscoder<Object>> transcoderClass = (Class<InitializableTranscoder<Object>>) Class.forName(transcoderClassProperty);
            InitializableTranscoder<Object> transcoder = transcoderClass.newInstance();
            transcoder.init(properties);
            return transcoder;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Transcoder object.", e);
        }
    }

    /**
     * Return cache namespace decorated key.
     *
     * @param cacheNamespace cache namespace
     * @param key cache key
     * @return namespace infomation prefixed cache key
     */
    String getNamespacedKey(CacheNamespace cacheNamespace, String key) {
        String namespaceIndicator = getNamespaceIndicator(cacheNamespace);

        if (cacheNamespace.isNamespaceExpirationRequired() == false) {
            return namespaceIndicator + ":" + key;
        }

        String namespaceIndicatorKey = namespaceIndicator + NAMESPACE_NAME_SQUENCE_SEPARATOR;
        long namespaceSquence = memcachedClient.incr(namespaceIndicatorKey, 0L, System.currentTimeMillis(),
                DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS);

        return namespaceIndicatorKey + namespaceSquence + ":" + key;
    }

    private String getNamespaceIndicator(CacheNamespace cacheNamespace) {
        return (StringUtils.isBlank(cacheKeyPrefix) ? "" : cacheKeyPrefix + ".") + cacheNamespace.getName();
    }

    @Override
    public void destroy() {
        memcachedClient.shutdown();
    }

    @Override
    public Object get(CacheNamespace cacheNamespace, String key) {
        String namespacedKey = getNamespacedKey(cacheNamespace, key);

        Object value = memcachedClient.get(namespacedKey);
        log.debug("spymemcachedadapter get key [{}], hit {}.", namespacedKey, value != null);
        return value;
    }

    @Override
    public void set(CacheNamespace cacheNamespace, String key, Object value, int expirySeconds) {
        String namespacedKey = getNamespacedKey(cacheNamespace, key);

        log.debug("spymemcachedadapter set key [{}], value [{}], expirySeconds [{}] .", namespacedKey, value,
                expirySeconds);
        memcachedClient.set(namespacedKey, expirySeconds, value);
    }

    @Override
    public void delete(CacheNamespace cacheNamespace, String key) {
        String namespacedKey = getNamespacedKey(cacheNamespace, key);

        log.debug("spymemcachedadapter delete key [{}].", namespacedKey);
        memcachedClient.delete(namespacedKey);
    }

    @Override
    public long increaseCounter(CacheNamespace cacheNamespace, String key, long by, long defaultValue, int expirySeconds) {
        String namespacedKey = getNamespacedKey(cacheNamespace, key);
        long counterValue = memcachedClient.incr(namespacedKey, by, defaultValue, expirySeconds);
        log.debug("spymemcachedadapter increase counter key [{}] with by {} default value {} returns {}", namespacedKey, by, defaultValue, counterValue);

        return counterValue;
    }

    @Override
    public long getCounter(CacheNamespace cacheNamespace, String key, long defaultValue, int expirySeconds) {
        String namespacedKey = getNamespacedKey(cacheNamespace, key);
        long counterValue = memcachedClient.incr(namespacedKey, 0, defaultValue, expirySeconds);

        log.debug("spymemcachedadapter get counter key [{}] with default value {} returns {}", namespacedKey, defaultValue, counterValue);

        return counterValue;
    }

    @Override
    public void evictAll(CacheNamespace cacheNamespace) {
        if (!cacheNamespace.isNamespaceExpirationRequired()) {
            log.debug("spymemcachedadapter region evict {} nothins done, because regionExpirationRequired == false.", cacheNamespace);
            return;
        }

        String namespaceIndicatorKey = getNamespaceIndicator(cacheNamespace) + NAMESPACE_NAME_SQUENCE_SEPARATOR;
        long nextSequence = memcachedClient.incr(namespaceIndicatorKey, 1, System.currentTimeMillis(), DEFAULT_NAMESPACE_SEQUENCE_EXPIRY_SECONDS);
        log.debug("spymemcachedadapter region evicted namespaceIndicatorKey : {}, cacheNamespace: {}, nextSequence {}",
                namespaceIndicatorKey, cacheNamespace, nextSequence);
    }
}