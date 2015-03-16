package kr.pe.kwonnam.hibernate4memcached.example;

import kr.pe.kwonnam.hibernate4memcached.example.entity.Author;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Testing Query Cache
 */
public class QueryCacheTest {
	private Logger log = LoggerFactory.getLogger(QueryCacheTest.class);

	@Before
	public void setUp() throws Exception {
		EntityTestUtils.init();
	}

	@After
	public void tearDown() throws Exception {
		EntityTestUtils.destroy();
	}

	private List<Author> getAuthorsWithQuery(String logMessage, String country) {
		EntityManager em = EntityTestUtils.start();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Author> cq = cb.createQuery(Author.class);
		Root<Author> author = cq.from(Author.class);
		cq.select(author);
		cq.where(cb.equal(author.get("country"), country));

		TypedQuery<Author> query = em.createQuery(cq);
		query.setHint("org.hibernate.cacheable", true);
		query.setHint("org.hibernate.cacheRegion", "author-by-country");

		log.warn("before call {} --", logMessage);
		List<Author> beforeResults = query.getResultList();
		log.warn("{} : {}", logMessage, beforeResults);
		EntityTestUtils.stop(em);
		return beforeResults;
	}

	@Test
	public void createQueryCacheAndRetry() throws Exception {
		List<Author> beforeResults = getAuthorsWithQuery("Author query", "대한민국");

		log.warn("#####################################################################");

		List<Author> againResults = getAuthorsWithQuery("Author query again", "대한민국");

		assertThat(againResults).isEqualTo(beforeResults);
		log.warn("#####################################################################");
	}

	@Test
	public void createQueryCacheAndEvictAllThenRetry() throws Exception {
		List<Author> beforeResults = getAuthorsWithQuery("Author query", "어느나라");

		log.warn("#####################################################################");

		HibernateEntityManagerFactory entityManagerFactory = (HibernateEntityManagerFactory) EntityTestUtils.getEntityManagerFactory();
		org.hibernate.Cache cache = entityManagerFactory.getSessionFactory().getCache();
		cache.evictEntityRegions();
		cache.evictQueryRegions();
		cache.evictDefaultQueryRegion();
		cache.evictCollectionRegions();

		log.warn("just eviected all.");
		List<Author> againResults = getAuthorsWithQuery("Author query again after evict all", "어느나라");

		assertThat(againResults).isEqualTo(beforeResults);
		log.warn("#####################################################################");
	}
}
