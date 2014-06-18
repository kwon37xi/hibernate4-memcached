package kr.pe.kwonnam.hibernate4memcached.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

public class PropertiesUtilsTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void loadFromClasspath_emptyConfigPath() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        PropertiesUtils.loadFromClasspath("");
    }

    @Test
    public void loadFromClasspath_normal_properties() throws Exception {
        Properties properties = PropertiesUtils.loadFromClasspath("kr/pe/kwonnam/hibernate4memcached/util/normal.properties");
        assertThat(properties).hasSize(1);
        assertThat(properties.getProperty("normal.key")).isEqualTo("normal properties value");
    }

    @Test
    public void loadFromClasspath_xml_properteis() throws Exception {
        Properties properties = PropertiesUtils.loadFromClasspath("kr/pe/kwonnam/hibernate4memcached/util/xml_properties.xml");
        assertThat(properties).hasSize(1);
        assertThat(properties.getProperty("xml.key")).isEqualTo("xml properties value");
    }

    @Test
    public void loadFromClasspath_illegal_resourcePath() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(containsString("You might set illegal resource path. Leading slash(/) is not allowed."));
        PropertiesUtils.loadFromClasspath("/kr/something/not/exists/class/path/some.properties");
    }
}