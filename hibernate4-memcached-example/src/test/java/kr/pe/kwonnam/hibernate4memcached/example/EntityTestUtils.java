package kr.pe.kwonnam.hibernate4memcached.example;

import kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory;
import kr.pe.kwonnam.hibernate4memcached.spymemcached.KryoTranscoder;
import kr.pe.kwonnam.hibernate4memcached.spymemcached.SpyMemcachedAdapter;
import net.spy.memcached.DefaultHashAlgorithm;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.cfg.AvailableSettings;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class EntityTestUtils {
    private static EntityManagerFactory emf;

    public static void init() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, true);
        props.put(AvailableSettings.USE_QUERY_CACHE, true);
        props.put(AvailableSettings.DEFAULT_CACHE_CONCURRENCY_STRATEGY, CacheConcurrencyStrategy.NONSTRICT_READ_WRITE);
        props.put(AvailableSettings.CACHE_REGION_FACTORY, Hibernate4MemcachedRegionFactory.class.getName());
        props.put(AvailableSettings.CACHE_REGION_PREFIX, "cachetest");
        props.put(AvailableSettings.HBM2DDL_AUTO, "create");
        props.put(AvailableSettings.USE_STRUCTURED_CACHE, "false");
        props.put(Hibernate4MemcachedRegionFactory.MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY, SpyMemcachedAdapter.class.getName());
        props.put(SpyMemcachedAdapter.HOST_PROPERTY_KEY, "localhost:11211");
        props.put(SpyMemcachedAdapter.HASH_ALGORITHM_PROPERTY_KEY, DefaultHashAlgorithm.KETAMA_HASH.name());
        props.put(SpyMemcachedAdapter.OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY, "5000");
        props.put(SpyMemcachedAdapter.TRANSCODER_PROPERTY_KEY, KryoTranscoder.class.getName());
        props.put(SpyMemcachedAdapter.CACHE_KEY_PREFIX_PROPERTY_KEY, "h4m");
        props.put(KryoTranscoder.COMPRESSION_THREASHOLD_PROPERTY_KEY, "20000");

        emf = Persistence.createEntityManagerFactory("cachetest", props);
    }


    public static void destroy() {
        if (emf == null) {
            return;
        }

        emf.close();
    }

    public static EntityManager start() {
        return emf.createEntityManager();
    }

    public static void stop(EntityManager em) {
        em.close();
    }
}
