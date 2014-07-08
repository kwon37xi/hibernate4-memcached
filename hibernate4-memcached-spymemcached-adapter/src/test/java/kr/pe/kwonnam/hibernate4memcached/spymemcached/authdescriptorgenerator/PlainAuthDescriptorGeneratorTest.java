package kr.pe.kwonnam.hibernate4memcached.spymemcached.authdescriptorgenerator;

import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

public class PlainAuthDescriptorGeneratorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PlainAuthDescriptorGenerator generator;

    @Before
    public void setUp() throws Exception {
        generator = new PlainAuthDescriptorGenerator();
    }

    @Test
    public void generate_username_missing() throws Exception {
        expectedException.expect(IllegalStateException.class);

        Properties props = new Properties();
        props.setProperty(PlainAuthDescriptorGenerator.PASSWORD_PROPERTY_KEY, "password");

        generator.generate(new OverridableReadOnlyPropertiesImpl(props));
    }

    @Test
    public void generate_password_missing() throws Exception {
        expectedException.expect(IllegalStateException.class);

        Properties props = new Properties();
        props.setProperty(PlainAuthDescriptorGenerator.USERNAME_PROPERTY_KEY, "username");

        generator.generate(new OverridableReadOnlyPropertiesImpl(props));
    }

    @Test
    public void generate() throws Exception {
        Properties props = new Properties();
        props.setProperty(PlainAuthDescriptorGenerator.USERNAME_PROPERTY_KEY, "username");
        props.setProperty(PlainAuthDescriptorGenerator.PASSWORD_PROPERTY_KEY, "password");

        AuthDescriptor authDescriptor = generator.generate(new OverridableReadOnlyPropertiesImpl(props));

        assertThat(authDescriptor).isNotNull();
        assertThat(authDescriptor.getMechs()).isEqualTo(new String[]{"PLAIN"});
        assertThat(authDescriptor.getCallback()).isExactlyInstanceOf(PlainCallbackHandler.class);
    }
}