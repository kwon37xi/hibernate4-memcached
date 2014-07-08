package kr.pe.kwonnam.hibernate4memcached.spymemcached.authdescriptorgenerator;

import kr.pe.kwonnam.hibernate4memcached.spymemcached.AuthDescriptorGenerator;
import kr.pe.kwonnam.hibernate4memcached.spymemcached.SpyMemcachedAdapter;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

/**
 * {@link AuthDescriptor} for Plain Sasl authentication generator.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 * @since 0.6
 */
public class PlainAuthDescriptorGenerator implements AuthDescriptorGenerator {
    public static final String USERNAME_PROPERTY_KEY = SpyMemcachedAdapter.PROPERTY_KEY_PREFIX + ".auth.plain.username";
    public static final String PASSWORD_PROPERTY_KEY = SpyMemcachedAdapter.PROPERTY_KEY_PREFIX + ".auth.plain.password";

    @Override
    public AuthDescriptor generate(OverridableReadOnlyProperties properties) {
        String username = properties.getRequiredProperty(USERNAME_PROPERTY_KEY);
        String password = properties.getRequiredProperty(PASSWORD_PROPERTY_KEY);

        AuthDescriptor authDescriptor = new AuthDescriptor(new String[]{"PLAIN"}, new PlainCallbackHandler(username, password));
        return authDescriptor;
    }
}
