package kr.pe.kwonnam.hibernate4memcached.example;

import kr.pe.kwonnam.hibernate4memcached.example.entity.Employee;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class NonstrictReadWriteSelfRelationTest {
    private Logger log = LoggerFactory.getLogger(NonstrictReadWriteSelfRelationTest.class);

    @Before
    public void setUp() throws Exception {
        EntityTestUtils.init();
    }

    @After
    public void tearDown() throws Exception {
        EntityTestUtils.destroy();
    }

    @Test
    public void selfRelation() throws Exception {
        log.debug("#### Load data");

        EntityManager em = EntityTestUtils.start();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        Employee boss = new Employee();
        boss.setName("김부장");
        boss.setPosition("부장");

        em.persist(boss);

        Employee worker1 = new Employee();
        worker1.setName("이씨");
        worker1.setPosition("사원");

        Employee worker2 = new Employee();
        worker2.setName("박씨");
        worker2.setPosition("사원");

        em.persist(worker1);
        em.persist(worker2);
        transaction.commit();
        EntityTestUtils.stop(em);

        log.debug("#### 데이터 읽기 최초");
        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();

        boss = em.find(Employee.class, 1L);
        log.debug("boss from db : {}", boss);
        transaction.commit();
        EntityTestUtils.stop(em);

        log.debug("#### 데이터 읽기 두번째 - 캐시에서 읽어야함, 그러면서 workers 추가");

        em = EntityTestUtils.start();
        transaction = em.getTransaction();
        transaction.begin();

        boss = em.find(Employee.class, 1L);
        log.debug("boss from cache : {}", boss);

        assertThat(boss.getName()).isEqualTo("김부장");
        assertThat(boss.getPosition()).isEqualTo("부장");

        worker1 = em.find(Employee.class, 2L);
        boss.addWorker(worker1);
        transaction.commit();
        EntityTestUtils.stop(em);
    }
}
