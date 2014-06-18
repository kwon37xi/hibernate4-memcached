package kr.pe.kwonnam.hibernate4memcached.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

public class OverridableReadOnlyPropertiesImplTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private OverridableReadOnlyPropertiesImpl overridableReadOnlyPropertiesImpl;
    private Properties formerProperties;
    private Properties latterProperties;

    @Before
    public void setUp() throws Exception {
        formerProperties = new Properties();
        formerProperties.setProperty("former.value", "former property value");
        formerProperties.setProperty("overriden.value", "former overriden value.");

        latterProperties = new Properties();
        latterProperties.setProperty("latter.value", "latter property value");
        latterProperties.setProperty("overriden.value", "latter properties overrides former properties");

        overridableReadOnlyPropertiesImpl = new OverridableReadOnlyPropertiesImpl(formerProperties, latterProperties);
    }

    @Test
    public void constructor_null_array() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        new OverridableReadOnlyPropertiesImpl();
    }

    @Test
    public void constructor_array_contains_null_properties() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        new OverridableReadOnlyPropertiesImpl(new Properties(), null, new Properties());
    }

    @Test
    public void getProperty_not_exists() throws Exception {
        assertThat(overridableReadOnlyPropertiesImpl.getProperty("key.not.exists")).isNull();
    }

    @Test
    public void getProperty_former() throws Exception {
        assertThat(overridableReadOnlyPropertiesImpl.getProperty("former.value")).isEqualTo(formerProperties.getProperty("former.value"));
    }

    @Test
    public void getProperty_latter() throws Exception {
        assertThat(overridableReadOnlyPropertiesImpl.getProperty("latter.value")).isEqualTo(latterProperties.getProperty("latter.value"));
    }

    @Test
    public void getProperty_overriden() throws Exception {
        assertThat(overridableReadOnlyPropertiesImpl.getProperty("overriden.value")).isEqualTo(latterProperties.getProperty("overriden.value"));
    }

    @Test
    public void getProperty_not_exists_default() throws Exception {
        assertThat(overridableReadOnlyPropertiesImpl.getProperty("key.not.exists", "this is default")).isEqualTo("this is default");
    }

    @Test
    public void getRequiredProperty_no_value() throws Exception {
        expectedException.expect(IllegalStateException.class);
        
        overridableReadOnlyPropertiesImpl.getRequiredProperty("some.not.exists.key");
    }
}