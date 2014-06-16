package kr.pe.kwonnam.hibernate4memcached.example;

import kr.pe.kwonnam.hibernate4memcached.example.entity.Author;
import kr.pe.kwonnam.hibernate4memcached.example.entity.Book;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class NonstrictReadwriteTest {
    private Logger log = LoggerFactory.getLogger(NonstrictReadwriteTest.class);

    @Before
    public void setUp() throws Exception {
        EntityTestUtils.init();
    }

    @Test
    public void nonstrictReadWrite() throws Exception {
        log.debug("#### First load");
        EntityManager em = EntityTestUtils.start();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        log.debug("Author 1 first load.");
        Author author1 = em.find(Author.class, 1L);
        log.debug("Author first load : {}", author1);
        transaction.commit();
        EntityTestUtils.stop(em);

        log.debug("#### Cached Load");
        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();
        author1 = em.find(Author.class, 1L);
        log.debug("Author 1 reload : {}", author1);
        transaction.commit();
        EntityTestUtils.stop(em);

        log.debug("#### Insert!");
        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();

        Author newAuthor = new Author();
        newAuthor.setName("Some one famous");
        newAuthor.setCountry("Some where over the rainbow");

        em.persist(newAuthor);
        log.debug("new Author inserted : {}", newAuthor);

        transaction.commit();
        EntityTestUtils.stop(em);

        log.debug("### Update!");
        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();

        Author authorToBeUpdated = em.find(Author.class, 1L);
        authorToBeUpdated.setName(authorToBeUpdated.getName() + " Postfix");
        em.merge(authorToBeUpdated);
        transaction.commit();
        EntityTestUtils.stop(em);

        log.debug("### Delete!");
        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();

        Author authorToBeDeleted = em.getReference(Author.class, newAuthor.getId());
        em.remove(authorToBeDeleted);
        transaction.commit();
        EntityTestUtils.stop(em);
    }

    @After
    public void tearDown() throws Exception {
        EntityTestUtils.destroy();
    }
}
