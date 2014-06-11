package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.memcached.CacheNamespace;
import kr.pe.kwonnam.hibernate4memcached.memcached.MemcachedAdapter;
import net.spy.memcached.*;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Properties;

import static kr.pe.kwonnam.hibernate4memcached.util.PropertiesUtils.getRequiredProeprties;

/**
 * SpymemcachedAdapter for hibernate4memcached.
 * <p/>
 * - h4m.adapter.spymemcached.hosts=localhost:11211,somehost:11211
 * * h4m.adapter.spymemcached.hashalgorithm=KETMA_HASH # DefaultHashAlgorithm 의 값
 * * h4m.adapter.spymemcached.operation.timeout.millis=10000
 * * h4m.adapter.spymemcached.transcoder=kr.pe.kwonnam.hibernate4memcached.spymemcached.KryoTranscoder
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class SpyMemcachedAdapter implements MemcachedAdapter {
    public static final String PROPERTY_KEY_PREFIX = "h4m.adapter.spymemcached";
    public static final String HOST_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".hosts";
    public static final String HASH_ALGORITHM_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".hashalgorithm";
    public static final String OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".operation.timeout.millis";
    public static final String TRANSCODER_PROPERTY_KEY = PROPERTY_KEY_PREFIX + ".transcoder";

    public static final int MAX_MEMCACHED_KEY_SIZE = 250;
    public static final String REGION_NAME_SQUENCE_SEPARATOR = "@";
    public static final int DEFAULT_REGION_SEQUENCE_EXPIRY_SECONDS = 60 * 60 * 24 * 30; // Memcached는 MAX_EXPIRY_SECONDS는 30 days를 넘을 수 없다.

    private Logger log = LoggerFactory.getLogger(SpyMemcachedAdapter.class);

    private MemcachedClientIF memcachedClient;

    @Override
    public void init(Properties properties) {
        ConnectionFactoryBuilder builder = getConnectionFactoryBuilder(properties);

        try {
            String addresses = getRequiredProeprties(properties, HOST_PROPERTY_KEY);
            memcachedClient = new MemcachedClient(builder.build(), AddrUtil.getAddresses(addresses));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * ConnectionFactoryBuilder 생성. 더 상세 설정이 필요할 경우, 상속하여
     * 이 부분을 Override 한다.
     *
     * @return
     */
    protected ConnectionFactoryBuilder getConnectionFactoryBuilder(Properties properties) {
        ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
        // BINARY Only!!! spymemcached incr/decr correctly supports only BINARY mode.
        builder.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY);

        builder.setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT);
        builder.setUseNagleAlgorithm(false);
        builder.setFailureMode(FailureMode.Redistribute);

        String hashAlgorithmProeprty = getRequiredProeprties(properties, HASH_ALGORITHM_PROPERTY_KEY);
        builder.setHashAlg(DefaultHashAlgorithm.valueOf(hashAlgorithmProeprty));

        String operationTimeoutProperty = getRequiredProeprties(properties, OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY);
        builder.setOpTimeout(Long.parseLong(operationTimeoutProperty));

        String transcoderClassProperty = getRequiredProeprties(properties, TRANSCODER_PROPERTY_KEY);
        try {
            Class<Transcoder<Object>> transcoderClass = (Class<Transcoder<Object>>) Class.forName(transcoderClassProperty);
            Constructor<Transcoder<Object>> constructor = transcoderClass.getConstructor(Properties.class);

            builder.setTranscoder(constructor.newInstance(properties));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return builder;
    }

    @Override
    public void destroy() {
        memcachedClient.shutdown();
    }

    /**
     * Cache Region과 key를 조합하여 새로운 key를 생성한다.
     */
    protected String createRegionPrefixedKey(CacheNamespace cacheNamespace, String key) {
        String regionNameWithSequence = getRegionNameWithSequence(cacheNamespace);

        String regionPrefixedKey = regionNameWithSequence + ":" + key;
        if (regionPrefixedKey.length() <= MAX_MEMCACHED_KEY_SIZE) {
            return regionPrefixedKey;
        }

        String hashedRegionPrefixedKey = regionNameWithSequence + ":" + hashKey(key);
        log.debug("region key is too long[{}]. hashing key by MD5+hashCode() [{}]", regionPrefixedKey, hashedRegionPrefixedKey);

        return hashedRegionPrefixedKey;
    }

    String getRegionNameWithSequence(CacheNamespace cacheNamespace) {
        if (!cacheNamespace.isRegionExpirationRequired()) {
            return cacheNamespace.getRegionName();
        }

        String regionNameSequenceKey = getRegionNameSequenceKey(cacheNamespace);
        Long sequence = memcachedClient.incr(regionNameSequenceKey, 0L, 1L, DEFAULT_REGION_SEQUENCE_EXPIRY_SECONDS);
        return cacheNamespace.getRegionName() + REGION_NAME_SQUENCE_SEPARATOR + sequence;
    }


    String getRegionNameSequenceKey(CacheNamespace cacheNamespace) {
        return cacheNamespace.getRegionName() + REGION_NAME_SQUENCE_SEPARATOR;
    }

    protected String hashKey(String key) {
        return DigestUtils.md5Hex(key) + "_" + String.valueOf(key.hashCode());
    }

    @Override
    public Object get(CacheNamespace cacheNamespace, String key) {
        String regionPrefixedKey = createRegionPrefixedKey(cacheNamespace, key);

        Object value = memcachedClient.get(regionPrefixedKey);
        log.debug("Spymemcached Get key [{}], hit {}.", regionPrefixedKey, value != null);
        return value;
    }

    @Override
    public void set(CacheNamespace cacheNamespace, String key, Object value, int expirySeconds) {
        String regionPrefixedKey = createRegionPrefixedKey(cacheNamespace, key);

        log.debug("Spymemcached Set key [{}], value [{}], expirySeconds [{}] .", regionPrefixedKey, value, expirySeconds);
        memcachedClient.set(regionPrefixedKey, expirySeconds, value);
    }

    @Override
    public void delete(CacheNamespace cacheNamespace, String key) {
        String regionPrefixedKey = createRegionPrefixedKey(cacheNamespace, key);
        log.debug("Spymemcached Delete key [{}].", regionPrefixedKey);

        memcachedClient.delete(regionPrefixedKey);
    }

    @Override
    public void evictAll(CacheNamespace cacheNamespace) {
        if (!cacheNamespace.isRegionExpirationRequired()) {
            log.debug("Spymemcached region Evict {} requested but did nothing because regionExpirationRequired == false.", cacheNamespace);
            return;
        }

        long nextSequence = memcachedClient.incr(getRegionNameSequenceKey(cacheNamespace), 1, 1L, DEFAULT_REGION_SEQUENCE_EXPIRY_SECONDS);
        log.debug("Spymemcached region Evicted {}, nextSequence {}", cacheNamespace, nextSequence);
    }
}