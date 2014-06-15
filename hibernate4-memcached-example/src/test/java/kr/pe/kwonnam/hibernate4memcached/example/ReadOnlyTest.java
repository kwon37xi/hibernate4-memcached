package kr.pe.kwonnam.hibernate4memcached.example;

import kr.pe.kwonnam.hibernate4memcached.example.entity.Person;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
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

        log.debug("person 1 first load.");
        Person person = em.find(Person.class, 1L);
        log.debug("person first load : {}", person);

        Person newPerson = new Person();
        newPerson.setName("Cogito Ergo Sum");
        newPerson.setBirthdate(new Date());

        em.persist(newPerson);
        transaction.commit();
        EntityTestUtils.stop(em);

        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();
        log.debug("person 2 update.");
        Person person2 = em.find(Person.class, newPerson.getId());
        em.remove(person2);
        transaction.commit();
        EntityTestUtils.stop(em);
    }

    @After
    public void tearDown() throws Exception {
        EntityTestUtils.destroy();
    }
}
