package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import net.spy.memcached.auth.AuthDescriptor;

/**
 * When authentication required, use AuthDescriptorGenerator implementations.
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 * @since 0.6
 */
public interface AuthDescriptorGenerator {
    AuthDescriptor generate(OverridableReadOnlyProperties properties);
}
