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
import java.util.*;

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

        System.out.println("####### LZ4 compression test");
        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {

                Book book = new Book();
                book.setTitle("Very big book");
                book.setDescription("Big Big book");
                book.setPublishedAt(new Date());
                book.setEdition(2);

                Author author = em.find(Author.class, 1L);
                ArrayList<Author> authors = new ArrayList<Author>();
                authors.add(author);
                book.setContents(LOREM_IPSUM);
                book.setAuthors(authors);

                em.persist(book);
                log.debug("book saved : {}", book);
            }
        });
        withEM(new WithEM() {
            @Override
            public void process(EntityManager em) {
                log.debug("book after saved read again : {}", em.find(Book.class, 3L));
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
        props.put(Environment.USE_STRUCTURED_CACHE, "false");
        props.put(Hibernate4MemcachedRegionFactory.MEMCACHED_ADAPTER_CLASS_PROPERTY_KEY, SpyMemcachedAdapter.class.getName());
        props.put(SpyMemcachedAdapter.HOST_PROPERTY_KEY, "localhost:11211");
        props.put(SpyMemcachedAdapter.HASH_ALGORITHM_PROPERTY_KEY, DefaultHashAlgorithm.KETAMA_HASH.name());
        props.put(SpyMemcachedAdapter.OPERATION_TIMEOUT_MILLIS_PROPERTY_KEY, "5000");
        props.put(SpyMemcachedAdapter.TRANSCODER_PROPERTY_KEY, KryoTranscoder.class.getName());
        props.put(SpyMemcachedAdapter.CACHE_KEY_PREFIX_PROPERTY_KEY, "h4m");
        props.put(KryoTranscoder.COMPRESSION_THREASHOLD_PROPERTY_KEY, "20000");

        emf = Persistence.createEntityManagerFactory("cachetest", props);
    }

    protected static void withEM(WithEM withEM) {
        System.out.println("#############################################################################");

        EntityManager em = emf.createEntityManager();

        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        try {
            withEM.process(em);
            transaction.commit();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            transaction.rollback();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public static interface WithEM {
        void process(EntityManager em);
    }

    public static final String LOREM_IPSUM = "\n" +
            "\n" +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla aliquam elit eu aliquam bibendum. Ut dictum est sem, a bibendum elit suscipit at. Aenean cursus sagittis sem, in dictum lectus bibendum vel. Donec elementum sapien ut vulputate interdum. Morbi a vestibulum nibh. Aliquam mollis suscipit luctus. Curabitur sed eros vel orci sagittis elementum. Phasellus sed lectus ante.\n" +
            "\n" +
            "Etiam cursus facilisis tincidunt. Donec elementum vel enim non semper. Ut nec quam ut odio placerat scelerisque. Maecenas fermentum blandit felis at pulvinar. Cras rhoncus elit ante, nec volutpat erat blandit vitae. Fusce est velit, cursus et libero placerat, accumsan aliquet nibh. Mauris euismod dapibus dolor sit amet tristique.\n" +
            "\n" +
            "Morbi vitae tortor sit amet mauris ullamcorper semper. Quisque porta ante a volutpat accumsan. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Fusce blandit ligula eget sodales iaculis. Vestibulum varius mi sed luctus blandit. Aenean cursus lectus et sem consectetur facilisis. Vivamus fringilla imperdiet eros viverra venenatis. Aliquam sodales cursus mi, eget tempor neque mattis nec.\n" +
            "\n" +
            "Vestibulum vitae justo suscipit, ornare neque venenatis, cursus risus. Cras ut tincidunt nisl. Nullam eget lorem ultrices, malesuada libero et, molestie risus. In hac habitasse platea dictumst. Vivamus tortor metus, bibendum nec neque sit amet, ornare commodo augue. Nullam consectetur nibh vitae aliquet posuere. Sed ultricies metus et felis euismod egestas. In congue malesuada cursus. Ut dictum augue sem, in dapibus lorem viverra nec.\n" +
            "\n" +
            "Pellentesque nunc sapien, pulvinar quis lacinia a, commodo ut est. Aliquam erat volutpat. Quisque mattis, mauris vel placerat mattis, massa risus suscipit magna, non venenatis neque leo ut augue. Integer tincidunt suscipit purus non venenatis. Nulla ac lectus ultrices, vulputate mauris vitae, faucibus nunc. Nullam molestie ullamcorper lectus eu vehicula. Donec congue tellus mattis eros gravida, eu malesuada metus porttitor. Curabitur mollis tortor ut posuere lobortis. Pellentesque malesuada nisl a mauris semper, sit amet molestie metus malesuada. Curabitur at nunc eu dui vulputate semper. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Aliquam aliquam tincidunt neque, sed fringilla ante facilisis in. Morbi mattis fermentum tortor, non consequat odio hendrerit vitae. Phasellus dictum lorem sed dolor aliquam tempus.\n" +
            "\n" +
            "Morbi blandit ante velit, sed lobortis orci accumsan vel. Nam consequat lectus sit amet tempus varius. Pellentesque eu ligula porttitor, adipiscing leo vestibulum, ultricies mauris. Quisque eget lobortis sapien. Nulla consectetur dapibus orci eget iaculis. Fusce feugiat sem in lobortis bibendum. Etiam pharetra lobortis interdum. Donec gravida nunc accumsan nunc lobortis rutrum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Aliquam id lorem venenatis, gravida felis quis, tempus urna. In fermentum varius ornare. Duis at sodales elit. Fusce interdum massa rutrum, porta risus ac, hendrerit orci.\n" +
            "\n" +
            "Quisque purus dui, varius vitae lacus sed, auctor mollis libero. Aliquam porta at erat at volutpat. Quisque vulputate tristique sodales. Duis orci quam, rhoncus at orci eget, semper auctor neque. Proin pellentesque tempor ligula ut placerat. Mauris malesuada, odio ut euismod porttitor, nunc ante aliquam eros, in volutpat quam dui in libero. Interdum et malesuada fames ac ante ipsum primis in faucibus. In purus magna, ornare non iaculis nec, semper sed enim.\n" +
            "\n" +
            "Vestibulum quis ante non lacus interdum faucibus. Quisque venenatis rhoncus velit non ullamcorper. Suspendisse potenti. Vivamus pretium tellus sed nunc auctor, a ornare lorem gravida. Aliquam ac lectus in erat lacinia viverra. Quisque nec risus orci. Nullam ac lacus tellus. Vivamus ultricies, nunc ac fermentum commodo, quam felis sodales lorem, eu fermentum leo arcu id sapien. Maecenas aliquam lectus lacus, sed pretium odio tempor nec.\n" +
            "\n" +
            "Etiam facilisis in nunc at sodales. Cras dapibus elementum arcu ac tempor. Etiam volutpat libero id condimentum rhoncus. Nulla aliquet sollicitudin magna, at rutrum nibh eleifend et. Donec purus nibh, consequat et velit a, porta ultricies urna. Nulla iaculis cursus massa, in euismod massa malesuada at. Phasellus malesuada mauris sit amet diam adipiscing, sollicitudin adipiscing diam luctus. Etiam vulputate egestas gravida. Suspendisse dui nisi, facilisis ac fermentum quis, ornare at augue. Vivamus consequat augue in faucibus vestibulum. Integer ut aliquet felis, vel hendrerit nisi. Suspendisse in libero fermentum, ornare metus sed, ultrices odio. Nullam pharetra metus quis lobortis venenatis. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras fringilla luctus velit vel tempor.\n" +
            "\n" +
            "Mauris porttitor convallis diam varius pretium. Integer vitae pretium tellus, non commodo ipsum. Aenean quis molestie arcu. Aliquam ultrices velit sit amet neque commodo mattis. Integer dictum euismod tellus sit amet hendrerit. Nunc malesuada leo urna, at vehicula lorem suscipit eu. Praesent ipsum est, cursus a rhoncus fermentum, hendrerit ac nisi. Vivamus id nunc pellentesque ligula aliquam porta. Proin ut metus tincidunt lorem elementum bibendum. Aliquam a leo vehicula, lobortis lorem quis, dapibus libero. Maecenas vitae erat dapibus, faucibus erat eget, rutrum nunc. Sed sit amet semper lacus, ac mattis lectus. Pellentesque leo quam, iaculis eu ornare in, pellentesque non ante.\n" +
            "\n" +
            "Proin egestas rhoncus pretium. Fusce tincidunt feugiat est, et tristique justo placerat nec. Nulla facilisi. Donec eget enim diam. Suspendisse sit amet metus diam. Fusce nec sapien semper, eleifend enim ut, consequat ante. Curabitur quis est eget nibh feugiat varius. Cras semper sapien non nisi bibendum, id tempus velit ullamcorper. Vestibulum mi nunc, laoreet non cursus rutrum, convallis non orci. Integer id tristique sapien. Aliquam consequat lobortis urna ac mollis. Phasellus dignissim neque eu libero auctor, eu semper nunc convallis. Morbi iaculis nisi at tortor tincidunt, in vulputate leo vulputate.\n" +
            "\n" +
            "Ut tincidunt urna quis erat adipiscing faucibus. Sed ultricies tellus nec nunc luctus, id posuere tellus egestas. In eu nisi in elit varius accumsan. Nam non malesuada quam. Pellentesque tristique iaculis lacus in luctus. Nam sit amet libero dapibus, fermentum magna quis, dictum orci. Maecenas sit amet tortor lacinia, elementum nibh vitae, porttitor felis. Proin vehicula nulla diam, et convallis elit laoreet in. Mauris posuere dolor convallis diam elementum porttitor. Mauris mauris leo, porttitor quis sem sed, pharetra blandit ante. Duis eget ipsum non orci aliquet pretium. Nulla pulvinar mauris a felis tempor adipiscing.\n" +
            "\n" +
            "Ut massa nulla, imperdiet nec porttitor vitae, cursus eget massa. Duis cursus lobortis arcu, ac lobortis arcu cursus eu. Suspendisse sit amet suscipit mi. Mauris in convallis magna. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nam sodales urna orci, in lobortis eros fringilla quis. Duis aliquam libero et ante commodo, sed faucibus felis cursus. Etiam eget bibendum enim. Cras non pellentesque nulla.\n" +
            "\n" +
            "Aliquam porttitor arcu id nulla rutrum rutrum. Nullam eu dui nec felis interdum imperdiet nec ac odio. Cras vitae malesuada elit. Aliquam pharetra orci at ligula varius, ut posuere quam consequat. Mauris convallis convallis eros, ac pharetra orci sollicitudin id. Quisque dignissim eu mauris ut condimentum. Ut vitae dapibus felis, id vehicula arcu. Quisque in lectus dolor. Fusce quis neque quis odio sodales tincidunt sit amet et urna. Donec id dui porttitor, auctor erat id, laoreet lorem. Quisque id tempor nisl, et auctor nisl. Mauris tempus euismod libero, id rhoncus neque dapibus sed.\n" +
            "\n" +
            "Nullam interdum ut arcu ac euismod. Fusce tincidunt lorem dictum ornare pellentesque. Maecenas euismod at magna id condimentum. Cras consequat ante ligula, quis tempus augue volutpat ut. Duis hendrerit ante massa, in convallis sem pellentesque sit amet. In pulvinar, nunc iaculis interdum ornare, nunc lectus cursus purus, in luctus justo arcu vitae est. Donec consectetur, tortor a interdum scelerisque, ligula diam porta ligula, interdum iaculis dolor lacus non erat. Donec in quam placerat nunc pulvinar euismod rhoncus id arcu. Nunc non est eu arcu consectetur eleifend in a neque.\n" +
            "\n" +
            "Suspendisse in posuere neque, vitae rutrum dui. Fusce hendrerit, risus eu vulputate gravida, ligula orci convallis lacus, sed vestibulum tellus mi quis odio. Donec egestas orci vulputate nibh fringilla, vel placerat lectus bibendum. In gravida, leo at posuere laoreet, justo purus bibendum elit, et ornare quam neque eu nisi. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Mauris pellentesque quam varius tortor consequat pharetra. Nullam et metus ultrices, facilisis tellus consectetur, mattis eros.\n" +
            "\n" +
            "Nulla tristique ligula vel sapien aliquet, nec interdum metus malesuada. Sed pretium, erat quis dignissim scelerisque, ligula felis viverra orci, nec sagittis felis massa a mi. Maecenas iaculis feugiat nisl ut viverra. Etiam at ultrices eros. Vestibulum semper velit a lorem suscipit, id rutrum tortor imperdiet. Maecenas tincidunt laoreet nisi. Integer nisl odio, lobortis at lectus et, pulvinar rhoncus nulla. Integer non ante augue. Mauris pulvinar justo et dignissim euismod. Suspendisse id neque scelerisque, tincidunt magna quis, interdum justo. Nunc lobortis volutpat diam, eu lacinia ipsum auctor ac. Fusce mollis laoreet mauris, sed sagittis tortor aliquet et. Cras sed lobortis risus. Sed sit amet porttitor tortor. Proin eget tellus vel est vulputate facilisis. Suspendisse potenti.\n" +
            "\n" +
            "Suspendisse lacinia leo eu mollis iaculis. Donec sagittis augue id lacus interdum, quis placerat orci vestibulum. Vestibulum adipiscing mauris eget velit interdum dapibus. Sed lacinia malesuada nisi, non accumsan magna. Phasellus id est aliquet, sagittis risus at, tempor nisi. Morbi massa nisl, dapibus ac vestibulum aliquet, mattis a leo. Donec ultricies ac ipsum quis laoreet. Etiam lobortis, augue ac gravida iaculis, neque mauris euismod turpis, ultrices rhoncus augue leo id libero. Nulla blandit purus at ligula egestas porta sed pharetra justo. Aliquam nec convallis mi. Proin enim dolor, venenatis sit amet ante sit amet, facilisis ultricies orci. Morbi nec massa mi. Duis congue mauris at lacus semper suscipit. Pellentesque consectetur leo non dignissim dapibus.\n" +
            "\n" +
            "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Suspendisse erat tellus, auctor sit amet tellus sit amet, tristique rhoncus dui. Cras ac felis sit amet nunc auctor tempus quis vel velit. Cras lectus tortor, lacinia vitae facilisis nec, pulvinar nec lacus. In tristique augue a nibh sagittis rhoncus. Duis pharetra congue cursus. Quisque ultricies posuere nulla. Integer tortor eros, tincidunt non consectetur in, sodales vel est. Maecenas ipsum dolor, interdum id diam porttitor, ultrices interdum magna.\n" +
            "\n" +
            "Duis commodo consectetur dolor, non convallis ligula rhoncus ac. Praesent feugiat ligula et elit dignissim laoreet. Morbi vel libero vel enim vestibulum laoreet. Praesent consequat vitae diam eget tincidunt. Morbi at ligula ut magna malesuada feugiat. Ut a scelerisque erat. Donec nec lorem in odio ullamcorper ultricies. Nullam eleifend malesuada aliquam. Nunc sodales in tortor a dictum. Nunc varius, ante sed cursus egestas, diam odio mollis lacus, ultrices suscipit massa metus sed quam.\n" +
            "\n" +
            "Integer ornare eros vitae sapien aliquet fermentum. Sed posuere, diam non posuere cursus, augue arcu rhoncus quam, ut adipiscing nisi enim sed nisl. Suspendisse lobortis tincidunt nisl nec fringilla. Sed eget ornare urna, lobortis fringilla turpis. Duis elementum auctor est, et fermentum diam dignissim in. Pellentesque vel enim nec risus convallis volutpat. Nam fringilla lorem congue nisl pellentesque scelerisque. Cras consequat lacinia sapien, ut consequat felis vestibulum at.\n" +
            "\n" +
            "Quisque ipsum felis, tempor eget tellus sit amet, mattis rutrum orci. Nullam dui nisi, faucibus et posuere sed, consequat eget ipsum. Donec aliquam malesuada nisi id iaculis. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec rhoncus faucibus aliquet. Nam in erat non felis lobortis molestie eget nec ante. Aliquam turpis ante, malesuada et sapien in, volutpat varius enim. Mauris ultricies, quam et lacinia blandit, sapien sem euismod augue, non euismod enim lorem at enim. Suspendisse potenti. Integer sit amet varius dolor, at elementum felis. Maecenas bibendum nibh nec diam aliquet vestibulum. Aliquam tincidunt arcu eget est placerat, eget placerat quam fringilla. In vitae nisi blandit, varius nunc sit amet, lacinia ipsum. Integer at eros lacus. Morbi sit amet facilisis nisi.\n" +
            "\n" +
            "Praesent eu elit orci. Aliquam et feugiat lacus, sed sodales nunc. Suspendisse potenti. Donec iaculis urna sit amet viverra varius. Nullam eu ultrices lorem. Pellentesque sollicitudin placerat dui, sit amet egestas massa bibendum eget. Nunc rhoncus rutrum viverra. Donec in mattis ligula.\n" +
            "\n" +
            "Curabitur nulla erat, vehicula ac dolor id, cursus sagittis felis. Integer dapibus vulputate fermentum. Morbi quis scelerisque tortor. Sed ut ipsum adipiscing mi scelerisque accumsan faucibus non turpis. Phasellus rhoncus sodales mi sed semper. Cras vehicula, est nec volutpat tincidunt, ligula tortor elementum lacus, et dignissim felis enim at felis. Vivamus ac ligula vitae metus laoreet lacinia quis ac libero. Cras ornare tincidunt tincidunt. Donec aliquam tortor at condimentum porttitor. Ut at dui vitae dui feugiat gravida nec sed leo. Suspendisse potenti.\n" +
            "\n" +
            "Curabitur quis mauris tincidunt, blandit ligula nec, malesuada tortor. Vestibulum nec elementum dolor. Nam rutrum enim eu purus lacinia, at adipiscing elit luctus. Aliquam ornare eleifend erat quis sodales. Phasellus metus odio, mattis ut molestie sit amet, faucibus rhoncus est. Integer consequat aliquet lectus ac dictum. Proin pharetra massa ut venenatis feugiat. In felis erat, blandit at arcu in, euismod cursus neque. Nulla lacinia, velit sed volutpat consectetur, orci libero auctor libero, at rutrum quam mi eu orci. Vestibulum suscipit fermentum eros, eget commodo erat consequat quis. Nam sit amet luctus libero, at imperdiet nunc. Curabitur nisl dui, porttitor ac semper eu, iaculis sit amet nisi. Mauris vehicula purus ac semper elementum. Aenean mollis, dui et sagittis sagittis, nisl ligula sollicitudin sapien, at pellentesque tortor dui id elit. Ut egestas placerat felis, non vehicula lorem.\n" +
            "\n" +
            "Mauris elit neque, mattis nec adipiscing eu, pharetra et elit. In bibendum diam eu faucibus elementum. In vehicula leo et dolor varius, ut dignissim lacus tristique. Duis vestibulum convallis porta. Phasellus hendrerit mollis eros sed sagittis. Praesent id odio sagittis, rutrum quam eu, accumsan sem. Fusce rhoncus tempor purus id rutrum.\n" +
            "\n" +
            "Proin quis augue orci. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nam eget tincidunt est. Aenean feugiat risus ante, quis vulputate massa tristique eu. Nam a tincidunt sapien. Morbi ullamcorper a odio sit amet tempor. Quisque tincidunt risus lacus, at tincidunt libero accumsan a. Aenean vel tellus feugiat, fringilla libero vitae, blandit dui. Phasellus vel quam ac massa egestas egestas. Nunc eget tincidunt tellus. Nunc ut dui sed ligula posuere consequat. Phasellus a ante quam.\n" +
            "\n" +
            "Mauris ultricies felis vulputate fermentum ultrices. Nam gravida porta consequat. Vestibulum sed lacus condimentum, euismod diam nec, vestibulum ipsum. In hac habitasse platea dictumst. Fusce fringilla, dolor sed ullamcorper venenatis, nibh erat tincidunt orci, sed imperdiet ligula lorem sed purus. Donec id turpis lorem. Aenean orci elit, faucibus a rutrum sed, lobortis nec mi. Ut faucibus urna vel scelerisque tristique. Cras imperdiet egestas placerat. Curabitur pulvinar nulla vitae erat varius consectetur. Nulla facilisi. Vestibulum id est tortor. Sed ullamcorper orci a justo vehicula sollicitudin. Cras vitae massa a metus dignissim ultrices. Donec nisi diam, tincidunt at porttitor at, laoreet non enim. Ut sodales aliquet nisi id dapibus.\n" +
            "\n" +
            "Pellentesque auctor enim tellus, et pharetra orci laoreet vel. Ut vehicula semper mi, sit amet malesuada quam malesuada non. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Aliquam eleifend rutrum mauris, vitae laoreet nibh interdum sed. Praesent at nulla tellus. Fusce orci libero, sagittis vel nulla vitae, convallis laoreet est. Phasellus a magna eget dui hendrerit pharetra. Nunc commodo, massa sed molestie molestie, nunc mi cursus metus, quis viverra tortor justo vel elit. Maecenas lacinia tempor ligula, id semper massa. Aliquam at turpis ante. Curabitur ut mauris nulla. Duis eu adipiscing nisi. Pellentesque scelerisque viverra eros, eget sollicitudin odio vehicula vitae. Integer eu luctus felis.\n" +
            "\n" +
            "In sit amet nisi vestibulum, interdum arcu euismod, interdum diam. Phasellus at nibh vitae tortor pretium pharetra. Curabitur fermentum tempus enim aliquet vulputate. Cras rutrum sem quam, at faucibus nulla hendrerit eget. Sed faucibus sem at arcu egestas, a pharetra augue porta. Pellentesque porta sem risus, vel sagittis arcu semper a. Aliquam leo diam, ullamcorper eget purus at, tincidunt fermentum turpis. Maecenas posuere dolor sit amet pulvinar porttitor. Pellentesque ac posuere odio, nec consectetur metus. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque vulputate felis non sollicitudin molestie. Quisque felis ante, adipiscing vel auctor vel, hendrerit volutpat arcu. Suspendisse id iaculis neque. Cras ultricies eget sapien nec dignissim. Sed in mauris sagittis, scelerisque ante nec, viverra nisi. Duis dignissim vitae nunc ut euismod.\n" +
            "\n" +
            "Cras id nisl et magna placerat faucibus at vitae est. Vivamus orci lacus, placerat vitae dolor ac, rhoncus semper neque. Vivamus consequat, lectus non tincidunt vulputate, metus lorem placerat sapien, eget interdum ipsum mauris vitae purus. Nunc ac aliquam mi. Quisque suscipit scelerisque elit quis viverra. Fusce eget augue dolor. Duis rhoncus, nibh non suscipit euismod, tellus felis iaculis nisl, sit amet fringilla ligula libero sit amet sapien. Aenean sodales purus nec est feugiat, quis euismod lorem volutpat. Duis ac varius leo, sit amet eleifend turpis. Morbi tincidunt augue consectetur suscipit porttitor. Etiam posuere feugiat felis, ac tincidunt quam vestibulum in. Vivamus vel ultrices sem. Phasellus consequat erat non sem molestie tincidunt pulvinar sit amet tortor. Praesent at quam tortor. Pellentesque sollicitudin turpis at turpis sollicitudin accumsan. Proin quis egestas ligula.\n" +
            "\n" +
            "Proin sed erat tristique, fermentum turpis eu, facilisis risus. Ut quis dictum metus. Vivamus ullamcorper turpis sed ligula dignissim porta. Maecenas pellentesque dapibus ligula condimentum condimentum. Maecenas rutrum varius enim, at rutrum orci laoreet vitae. Nam aliquet at ante quis volutpat. Vivamus mollis varius magna quis rutrum.\n" +
            "\n" +
            "Proin hendrerit erat et purus lobortis iaculis. Cras vitae consectetur diam. Phasellus imperdiet congue velit non egestas. Vestibulum condimentum erat arcu, tempor porttitor ligula hendrerit a. Proin leo ante, bibendum vitae neque a, rutrum lacinia dui. Etiam egestas feugiat orci, non placerat orci euismod quis. Fusce varius lacinia lorem quis egestas. Fusce et orci quis est tempus fermentum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Donec ultrices lacus eleifend odio sollicitudin bibendum. Aenean commodo nunc ac est condimentum, at tempus erat malesuada. Curabitur lacinia tellus mauris, a imperdiet nisl cursus non. Morbi eu odio purus. Curabitur luctus sodales turpis nec blandit. Curabitur auctor orci sed sapien facilisis suscipit.\n" +
            "\n" +
            "Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nulla purus lacus, cursus ut sodales sit amet, suscipit ac dui. Morbi quis scelerisque tortor, nec blandit tellus. Nam interdum pretium orci vitae pharetra. Donec sit amet magna eu mauris venenatis varius. Sed in fermentum sapien. Curabitur luctus, nulla elementum tristique mollis, ligula ligula pellentesque dui, sit amet placerat diam magna ac velit. Interdum et malesuada fames ac ante ipsum primis in faucibus. Mauris risus tortor, malesuada tincidunt augue eget, luctus eleifend nibh.\n" +
            "\n" +
            "Sed vitae auctor risus. Mauris accumsan libero justo, non mollis justo dignissim eget. Donec varius bibendum purus vel interdum. In faucibus tortor non dui bibendum, non blandit risus volutpat. Cras neque odio, rhoncus vel molestie sed, eleifend nec elit. Sed enim metus, interdum vitae purus eu, accumsan facilisis lectus. Sed porttitor tincidunt purus nec vulputate. Morbi posuere accumsan euismod. Sed eleifend, velit vitae venenatis ullamcorper, justo odio mollis neque, ut ullamcorper felis nunc et quam. Integer id accumsan risus. Morbi imperdiet hendrerit justo, vitae vehicula ligula dignissim rutrum. Duis commodo posuere ipsum, gravida condimentum metus posuere eget. Etiam at consequat lectus. Vivamus ac lorem est. Aliquam arcu turpis, tincidunt et lacus vitae, ultrices vehicula ipsum.\n" +
            "\n" +
            "Cras rutrum eros sed dolor laoreet, eu egestas sem rutrum. Aliquam posuere massa quis ligula imperdiet, sit amet vulputate lorem pretium. Nunc cursus mi eget tellus fringilla, vel elementum arcu auctor. Cras pharetra tellus fermentum, ultricies ligula ut, mattis augue. Suspendisse sit amet rhoncus diam. Aenean odio ligula, dignissim vehicula elementum vitae, accumsan non nibh. Vivamus dictum lacinia mi, vitae imperdiet turpis sagittis ut. Nam quis quam sit amet risus lobortis tempus et ac mi. Maecenas at laoreet leo. Suspendisse eleifend neque lacus, nec scelerisque est malesuada ut.\n" +
            "\n" +
            "Curabitur elementum ipsum nec est lacinia iaculis. Praesent in mauris velit. Fusce vel tempus est, non blandit risus. Morbi vitae leo eu nisl imperdiet lobortis. Proin scelerisque blandit odio at ornare. Vivamus in fringilla orci. Duis porttitor sodales libero in pharetra. Vestibulum dui magna, consectetur eget lorem ut, bibendum tincidunt sem. Etiam eget velit vitae felis adipiscing semper. Aliquam et diam eget dui posuere laoreet. Phasellus accumsan erat faucibus lacus condimentum, eu tempus nisi iaculis.\n" +
            "\n" +
            "Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Duis condimentum turpis vel nunc bibendum malesuada. Cras viverra condimentum ligula sed pellentesque. In dapibus molestie lectus at sollicitudin. Mauris aliquet lorem eu lacus fringilla condimentum. Pellentesque dignissim leo euismod ipsum commodo, tincidunt interdum risus blandit. Fusce placerat purus in sem vehicula, et blandit massa ultrices. Cras sit amet convallis elit. Quisque ullamcorper massa vitae mollis tempor. Vivamus mauris justo, semper quis vulputate ut, facilisis vitae velit. Nulla facilisi.\n" +
            "\n" +
            "Vestibulum ullamcorper auctor ultrices. Nam congue condimentum nulla vitae pharetra. Aliquam sed metus gravida, feugiat mi non, blandit ante. Donec felis eros, dignissim vel tortor in, luctus condimentum mi. Quisque sem felis, porttitor eu turpis non, ullamcorper condimentum augue. Integer porta ullamcorper pellentesque. Vestibulum commodo scelerisque eros, ac sagittis dolor porttitor in. Phasellus sodales, quam a rhoncus venenatis, mi nunc sollicitudin turpis, et pharetra nunc nulla at purus. Curabitur neque erat, ullamcorper non pellentesque nec, pretium quis erat. Mauris pharetra dui quam, ut mollis quam scelerisque vitae. Aenean commodo ultricies viverra. Integer massa libero, fringilla nec interdum nec, faucibus non quam. Sed varius augue id tellus hendrerit adipiscing. Nullam iaculis, sapien a convallis condimentum, turpis mi scelerisque purus, in consequat metus ipsum sed leo. Integer sodales hendrerit dolor, quis tincidunt enim venenatis vitae. Quisque vitae justo luctus, scelerisque enim vel, consectetur dolor.\n" +
            "\n" +
            "Nullam aliquet risus ut turpis laoreet, non lobortis nunc ultricies. Ut tincidunt fermentum urna, a tincidunt quam imperdiet id. Curabitur rutrum luctus aliquam. Etiam at nulla purus. In semper mi eu vestibulum interdum. Fusce sodales tortor mauris, a volutpat enim rutrum eget. In vitae consequat tellus. Nulla ut libero sit amet orci mattis pulvinar. Suspendisse ac ornare urna. Fusce tristique dapibus dolor non luctus.\n" +
            "\n" +
            "Integer consectetur non elit vel tempus. Mauris dignissim ut augue eu accumsan. Maecenas convallis at justo at laoreet. In accumsan porttitor mi, a vehicula magna sodales in. Vestibulum sed justo sollicitudin, condimentum nisi at, rhoncus ante. In hac habitasse platea dictumst. Mauris sapien lorem, molestie vitae laoreet sit amet, lacinia at nisl. Curabitur sapien lectus, feugiat sed elementum eu, imperdiet fringilla turpis. Praesent tempus luctus turpis vitae imperdiet. Sed varius augue metus, sed porttitor nisi mollis non. Phasellus placerat nunc id elit commodo, convallis blandit augue dictum. Nulla congue quis velit in eleifend. Vestibulum eleifend eu ligula at sagittis.\n" +
            "\n" +
            "Nullam eu sapien mi. Proin nisi felis, aliquet at justo in, congue fermentum neque. Phasellus ut condimentum neque, vel sodales justo. Vivamus vitae lectus lorem. Nunc feugiat turpis odio. Pellentesque condimentum sagittis augue a eleifend. Nullam dictum fringilla mollis. Aliquam aliquam, leo at dictum suscipit, libero lacus dignissim metus, ut eleifend libero nibh at arcu. Nullam ac nisi lacus. Proin eros massa, dignissim volutpat interdum vel, semper id erat. Vestibulum vitae gravida massa. Nunc scelerisque, urna nec placerat pretium, diam est porttitor orci, sit amet bibendum enim tellus at libero. Maecenas quis elit cursus, molestie metus in, tempus velit.\n" +
            "\n" +
            "Phasellus ullamcorper sollicitudin dolor eget aliquet. Pellentesque consequat, diam eu sollicitudin imperdiet, arcu ante malesuada ligula, ac lacinia dolor leo in purus. Aliquam ac turpis velit. Nam id nunc sed mauris placerat euismod. Ut venenatis feugiat faucibus. Integer nunc neque, consequat ut nulla vel, eleifend sodales odio. Etiam iaculis consectetur convallis.\n" +
            "\n" +
            "Mauris ullamcorper risus id velit facilisis suscipit. Donec varius sit amet sapien sed bibendum. Integer porttitor sit amet elit euismod volutpat. Curabitur eu eros nec mi faucibus hendrerit. Cras eu elit risus. Nam ac sodales metus. Vestibulum adipiscing a sapien malesuada sollicitudin.\n" +
            "\n" +
            "Vivamus cursus, neque eget sodales elementum, lectus odio blandit nunc, id cursus lacus elit porta elit. Duis commodo tellus sem. Cras sollicitudin accumsan tristique. Integer nec facilisis ligula. Suspendisse sodales nunc sollicitudin nisi volutpat posuere. Aliquam sit amet lacus libero. In eu venenatis odio, at feugiat tortor. Morbi feugiat neque at justo cursus, sit amet gravida magna consequat. Etiam condimentum velit nec feugiat pulvinar. Vestibulum ut felis lacus. Phasellus mattis tortor sapien, at malesuada mauris bibendum in. Ut pharetra mauris ut odio porta porttitor.\n" +
            "\n" +
            "Maecenas aliquet arcu in est molestie, mollis convallis nisi aliquet. Phasellus in velit tortor. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Curabitur sed vulputate turpis. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nullam malesuada sapien nulla. Donec luctus risus sit amet lorem mattis, et congue nibh sollicitudin. Nulla eget hendrerit orci. Duis eleifend placerat felis, eget condimentum tortor congue id. Praesent magna diam, tempor ac lectus ac, iaculis vulputate diam.\n" +
            "\n" +
            "Duis facilisis, diam et dictum imperdiet, magna dolor consectetur ligula, ut sodales turpis tortor non augue. Aenean imperdiet quam non dapibus laoreet. Nam sit amet enim sed leo fermentum ornare. Vivamus quis convallis arcu. Praesent ut mi vel ipsum convallis imperdiet. Cras iaculis ligula et orci viverra, in mollis orci pretium. Curabitur scelerisque sapien non libero porta, eget fringilla diam mattis. Phasellus pulvinar ante sit amet metus semper blandit. Etiam lectus nunc, aliquet ut elit et, varius cursus urna. Maecenas lobortis tellus sed purus imperdiet, eu placerat diam fermentum. Nunc at porttitor mi. Maecenas placerat auctor volutpat. Vestibulum eu justo vel tellus varius dictum.\n" +
            "\n" +
            "Sed auctor massa sapien, eget feugiat arcu malesuada id. Phasellus fermentum, dui non sagittis hendrerit, lorem purus auctor est, ut dapibus dolor metus sed turpis. Praesent euismod dui quis tellus pulvinar, sit amet tempus mi fringilla. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. In hac habitasse platea dictumst. Nulla vitae eros semper, porttitor purus non, dictum dolor. In suscipit sagittis ipsum, vitae sollicitudin erat porttitor id. Pellentesque lobortis nibh feugiat, varius mi a, eleifend urna. Quisque semper lacinia leo, vitae faucibus nibh cursus quis. Aliquam malesuada volutpat condimentum. In eget mauris quam. Maecenas et dictum orci. Nullam sit amet euismod risus.\n" +
            "\n" +
            "Curabitur viverra dapibus ipsum a convallis. Curabitur ut luctus felis. Nulla vitae scelerisque enim, non fringilla est. In convallis vel ipsum hendrerit mollis. Quisque tempor sapien ac mauris fringilla, nec laoreet purus mollis. Aenean blandit massa ut ultricies tincidunt. Etiam a fermentum justo. Cras sed est adipiscing ante lobortis hendrerit. Suspendisse potenti.\n" +
            "\n" +
            "Ut fermentum tellus at ante ornare auctor. Nulla a enim facilisis, vehicula turpis a, tincidunt orci. Etiam scelerisque, libero ut vestibulum varius, magna neque placerat est, eget vehicula massa turpis nec quam. Proin non arcu pulvinar, feugiat libero quis, pretium odio. Ut est nunc, viverra eget placerat eget, varius vel augue. Nulla porta interdum elit, pellentesque lobortis arcu facilisis id. Aliquam quis malesuada quam. Donec rutrum libero id placerat fringilla.\n";
}
