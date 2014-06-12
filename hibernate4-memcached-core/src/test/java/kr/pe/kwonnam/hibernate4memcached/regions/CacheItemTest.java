package kr.pe.kwonnam.hibernate4memcached.regions;

import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.cache.spi.entry.CollectionCacheEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CacheItemTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private CacheEntry cacheEntry;

    @Mock
    private CollectionCacheEntry collectionCacheEntry;

    private Map<String,Object> structuredCacheEntry = new HashMap<String, Object>();

    @Test
    public void checkIfClassVersionApplicable_useStructuredCache_false() throws Exception {
        assertThat(CacheItem.checkIfClassVersionApplicable(cacheEntry, false)).isTrue();
    }

    @Test
    public void checkIfClassVersionApplicable_useStructuredCache_false_no_CacheEntry() throws Exception {
        assertThat(CacheItem.checkIfClassVersionApplicable(structuredCacheEntry, false)).isFalse();
    }

    @Test
    public void checkIfClassVersionApplicable_useStructuredCache_true() throws Exception {
        assertThat(CacheItem.checkIfClassVersionApplicable(structuredCacheEntry, true)).isTrue();
    }

    @Test
    public void checkIfClassVersionApplicable_useStructuredCache_true_no_Map() throws Exception {
        assertThat(CacheItem.checkIfClassVersionApplicable(cacheEntry, true)).isFalse();
    }

    @Test
    public void constructor_when_checkIfClassVersionApplicable_false() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        CacheItem cacheItem = new CacheItem("some cache object", false);
    }

    @Test
    public void parseTargetClass_useStructuredCache_false_not_Serializable() throws Exception {
        givenCacheEntrySubclass(NotSerializableFakeEntity.class);

        expectedException.expect(IllegalArgumentException.class);
        new CacheItem(cacheEntry, false);
    }

    @Test
    public void parseTargetClass_useStructuredCache_true_not_Serializable() throws Exception {
        givenStructuredCacheEntrySubclass(NotSerializableFakeEntity.class);

        expectedException.expect(IllegalArgumentException.class);
        new CacheItem(structuredCacheEntry, true);
    }

    private void givenCacheEntrySubclass(Class<?> clazz) {
        when(cacheEntry.getSubclass()).thenReturn(clazz.getName());
    }

    private void givenStructuredCacheEntrySubclass(Class<NotSerializableFakeEntity> clazz) {
        structuredCacheEntry.put(CacheItem.STRUCTURED_CACHE_ENTRY_SUBCLASS_KEY, clazz.getName());
    }

    @Test
    public void parseTargetClass_parse_serialVersionUID() throws Exception {
        givenCacheEntrySubclass(FakeEntity.class);

        CacheItem cacheItem = new CacheItem(cacheEntry, false);
        assertThat(cacheItem.getTargetClassSerialVersionUID()).isEqualTo(12345L);
        assertThat(cacheItem.getTargetClassName()).isEqualTo(FakeEntity.class.getName());
    }

    @Test
    public void parseTargetClass_parse_serialVersionUid_of_FakeEntityWithoutSerialVersionUID() throws Exception {
        givenCacheEntrySubclass(FakeEntityWithoutSerialVersionUID.class);

        CacheItem cacheItem = new CacheItem(cacheEntry, false);

        assertThat(cacheItem.getTargetClassName()).isEqualTo(FakeEntityWithoutSerialVersionUID.class.getName());
        System.out.println(cacheItem.getTargetClassSerialVersionUID());
    }

    @Test
    public void isTargetClassAndCurrentJvmTargetClassMatch_jvm_has_no_targetClass() throws Exception {
        CacheItem cacheItem = createCacheItem("kr.pe.kwonnam.hibernate4memcached.NotExistingClass", 1L);

        assertThat(cacheItem.isTargetClassAndCurrentJvmTargetClassMatch()).isFalse();
    }

    @Test
    public void isTargetClassAndCurrentJvmTargetClassMatch_class_exist_but_Not_Serializable() throws Exception {
        CacheItem cacheItem = createCacheItem(NotSerializableFakeEntity.class.getName(), 1L);

        assertThat(cacheItem.isTargetClassAndCurrentJvmTargetClassMatch()).isFalse();
    }

    @Test
    public void isTargetClassAndCurrentJvmTargetClassMatch_class_exist_but_different_serialversionUID() throws Exception {
        CacheItem cacheItem = createCacheItem(FakeEntity.class.getName(), FakeEntity.serialVersionUID + 1L);

        assertThat(cacheItem.isTargetClassAndCurrentJvmTargetClassMatch()).isFalse();
    }

    @Test
    public void isTargetClassAndCurrentJvmTargetClassMatch_match_true() throws Exception {
        CacheItem cacheItem = createCacheItem(FakeEntity.class.getName(), FakeEntity.serialVersionUID);

        assertThat(cacheItem.isTargetClassAndCurrentJvmTargetClassMatch()).isTrue();
    }

    private CacheItem createCacheItem(String targetClassName, long targetClassSerialVersionUID) {
        CacheItem cacheItem = new CacheItem();

        cacheItem.setTargetClassName(targetClassName);
        cacheItem.setTargetClassSerialVersionUID(targetClassSerialVersionUID);

        return cacheItem;
    }

    public static class NotSerializableFakeEntity {
        private int id;
        private String value;

        public NotSerializableFakeEntity() {
        }

        public NotSerializableFakeEntity(int id, String value) {
            this.id = id;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class FakeEntity implements Serializable {
        private static final long serialVersionUID = 12345L;

        private int id;
        private String value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class FakeEntityWithoutSerialVersionUID implements Serializable {
        private int id;
        private String value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }
}