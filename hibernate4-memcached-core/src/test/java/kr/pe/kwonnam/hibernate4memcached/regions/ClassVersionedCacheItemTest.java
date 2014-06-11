package kr.pe.kwonnam.hibernate4memcached.regions;

import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.cache.spi.entry.StandardCacheEntryImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.service.ServiceRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class ClassVersionedCacheItemTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private CacheEntry cacheEntry;

    @Test
    public void checkIfClassVersionApplicable_CacheEntry() throws Exception {
        ClassVersionedCacheItem.checkIfClassVersionApplicable(cacheEntry, settings);

    }
}