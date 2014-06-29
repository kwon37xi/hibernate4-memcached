package kr.pe.kwonnam.hibernate4memcached.example;

import kr.pe.kwonnam.hibernate4memcached.example.entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.Date;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class ReadOnlyTest {
    private Logger log = LoggerFactory.getLogger(ReadOnlyTest.class);

    @Before
    public void setUp() throws Exception {
        EntityTestUtils.init();
    }

    @Test
    public void readonly() throws Exception {
        EntityManager em = EntityTestUtils.start();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        log.debug("### First load and save");
        log.debug("person 1 first load.");
        Person person = em.find(Person.class, 1L);
        log.debug("person first load : {}", person);

        Person newPerson = new Person();
        newPerson.setName("Cogito Ergo Sum");
        newPerson.setBirthdate(new Date());

        em.persist(newPerson);
        transaction.commit();
        EntityTestUtils.stop(em);

        log.debug("### Delete!");
        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();
        log.debug("person 2 delete.");
        Person person2 = em.find(Person.class, newPerson.getId());
        em.remove(person2);
        transaction.commit();
        EntityTestUtils.stop(em);

        try {
            // update시에는 exception 발생.
            log.debug("### Update!");
            em = EntityTestUtils.start();
            transaction = em.getTransaction();
            transaction.begin();
            person = em.find(Person.class, 1L);
            person.setBirthdate(new Date());
            em.merge(person);
            transaction.commit();
        } catch (Exception ex) {
            log.error("Update 중 오류 발생", ex);
        } finally {
            EntityTestUtils.stop(em);
        }

        log.debug("### Delete with query!");
        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();
        Query query = em.createQuery("delete from Person where id = :id");
        query.setParameter("id", 1L);
        query.executeUpdate();
        transaction.commit();
        EntityTestUtils.stop(em);
    }

    @After
    public void tearDown() throws Exception {
        EntityTestUtils.destroy();
    }
}
