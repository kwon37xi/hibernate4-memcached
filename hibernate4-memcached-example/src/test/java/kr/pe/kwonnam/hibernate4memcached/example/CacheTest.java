package kr.pe.kwonnam.hibernate4memcached.example;

import kr.pe.kwonnam.hibernate4memcached.Hibernate4MemcachedRegionFactory;
import kr.pe.kwonnam.hibernate4memcached.example.entity.Author;
import kr.pe.kwonnam.hibernate4memcached.example.entity.Book;
import kr.pe.kwonnam.hibernate4memcached.spymemcached.KryoTranscoder;
import kr.pe.kwonnam.hibernate4memcached.spymemcached.SpyMemcachedAdapter;
import net.spy.memcached.DefaultHashAlgorithm;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.cfg.Environment;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test hibernate4memcached
 */
public class CacheTest {

    private static EntityManagerFactory emf = null;

    public static final Logger log = LoggerFactory.getLogger(CacheTest.class);

    @Test
    public void testHibernateCache() {

//        populateEntityManagerFactory();
        populateMemcachedEntityManagerFactory();

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                Book book = em.find(Book.class, 1L);
                log.warn("First book : {}", book);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                Book book = em.find(Book.class, 1L);
                log.warn("First book again : {}", book);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                TypedQuery<Book> query = em.createNamedQuery("Book.byEdition", Book.class);
                query.setParameter("edition", 3);

                List<Book> resultList = query.getResultList();
                log.warn("Query  result : {}", resultList);
            }
        });


        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                TypedQuery<Book> query = em.createNamedQuery("Book.byEdition", Book.class);
                query.setParameter("edition", 3);

                List<Book> resultList = query.getResultList();
                log.warn("Query AGAIN result : {}", resultList);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Author> cq = cb.createQuery(Author.class);
                Root<Author> author = cq.from(Author.class);
                cq.select(author);
                cq.where(cb.equal(author.get("country"), "대한민국"));

                TypedQuery<Author> query = em.createQuery(cq);
                query.setHint("org.hibernate.cacheable", true);
                query.setHint("org.hibernate.cacheRegion", "author-by-country");

                log.warn("Author Criteria : {}", query.getResultList());
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Author> cq = cb.createQuery(Author.class);
                Root<Author> author = cq.from(Author.class);
                cq.select(author);
                cq.where(cb.equal(author.get("country"), "대한민국"));

                TypedQuery<Author> query = em.createQuery(cq);
                query.setHint("org.hibernate.cacheable", true);
                query.setHint("org.hibernate.cacheRegion", "author-by-country");

                log.warn("Author Criteria Again : {}", query.getResultList());
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                // Book 1 update
                Book book = em.find(Book.class, 1L);
                book.setDescription(book.getDescription() + " 설명추가");
                em.merge(book);
            }
        });


        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                // 1번 다시 읽기
                Book book = em.find(Book.class, 1L);
                log.warn("First book again after update : {}", book);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                TypedQuery<Book> query = em.createNamedQuery("Book.byEdition", Book.class);
                query.setParameter("edition", 3);

                List<Book> resultList = query.getResultList();
                log.warn("Query result after update : {}", resultList);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                // author 1 after update query
                Author author = em.find(Author.class, 1L);
                log.warn("Author 1 find before update query : {}", author);
            }
        });
        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                Book book = em.find(Book.class, 1L);
                log.warn("find book 1 Again before author 1 updated : {}", book);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                // update query
                Query query = em.createQuery("update Author set country = :newcountry  where country = :country ");
                query.setParameter("newcountry", "Republic Of Korea");
                query.setParameter("country", "대한민국");

                query.executeUpdate();
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                Book book = em.find(Book.class, 1L);
                log.warn("find book 1 Again after author 1 updated : {}", book);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                // author 1 after update query
                Author author = em.find(Author.class, 1L);
                log.warn("Author find after update query : {}", author);
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Author> cq = cb.createQuery(Author.class);
                Root<Author> author = cq.from(Author.class);
                cq.select(author);
                cq.where(cb.equal(author.get("country"), "대한민국"));

                TypedQuery<Author> query = em.createQuery(cq);
                query.setHint("org.hibernate.cacheable", true);
                query.setHint("org.hibernate.cacheRegion", "author-by-country");

                log.warn("Criteria Again after update query 값이 없는게 정상 : {}", query.getResultList());
            }
        });


        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                Author author3 = em.find(Author.class, 3L);
                em.remove(author3);
                log.warn("Author 3 deleted.");
            }
        });

        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                Author author3 = em.find(Author.class, 3L);

                log.warn("Author 3 after delete {}", author3);
            }
        });
        emf.close();
    }

    private static void populateMemcachedEntityManagerFactory() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(Environment.USE_SECOND_LEVEL_CACHE, true);
        props.put(Environment.USE_QUERY_CACHE, true);
        props.put(Environment.DEFAULT_CACHE_CONCURRENCY_STRATEGY, CacheConcurrencyStrategy.NONSTRICT_READ_WRITE);
        props.put(Environment.CACHE_REGION_FACTORY, Hibernate4MemcachedRegionFactory.class.getName());
        props.put(Environment.CACHE_REGION_PREFIX, "cachetest");
        props.put(Environment.HBM2DDL_AUTO, "create-drop");
        props.put(Hibernate4MemcachedRegionFactory.MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY, SpyMemcachedAdapter.class.getName());
        props.put(SpyMemcachedAdapter.HOST_PROPERTY_KEY, "localhost:11211");
        props.put(SpyMemcachedAdapter.HASH_ALGORITHM_PROPERTY_KEY, DefaultHashAlgorithm.KETAMA_HASH.name());
        props.put(SpyMemcachedAdapter.OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY, "5000");
        props.put(SpyMemcachedAdapter.TRANSCODER_PROPERTY_KEY, KryoTranscoder.class.getName());
        props.put(KryoTranscoder.COMPRESSION_THREASHOLD_PROPERTY_KEY, "20000");

        emf = Persistence.createEntityManagerFactory("cachetest", props);
    }

//    private static void populateEntityManagerFactory() {
//        Map<String, Object> props = new HashMap<String, Object>();
//        props.put(Environment.USE_SECOND_LEVEL_CACHE, true);
//        props.put(Environment.USE_QUERY_CACHE, true);
//        props.put(Environment.CACHE_REGION_FACTORY, CachingRegionFactory.class.getName());
//        props.put(Environment.CACHE_REGION_PREFIX, "cachetest");
//        props.put(Environment.DEFAULT_CACHE_CONCURRENCY_STRATEGY, CacheConcurrencyStrategy.NONSTRICT_READ_WRITE);
//        props.put(Environment.HBM2DDL_AUTO, "create-drop");
//
//        emf = Persistence.createEntityManagerFactory("cachetest", props);
//    }

    protected static void withEM(WithEM withEM) {
        System.out.println("#############################################################################");

        EntityManager em = emf.createEntityManager();

        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        try {
            withEM.process(em);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            transaction.commit();
            em.close();
        }
    }

    public static interface WithEM {
        void process(EntityManager em);
    }
}
