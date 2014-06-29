package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import net.spy.memcached.transcoders.Transcoder;

/**
 * {@link SpyMemcachedAdapter} takes this interface implementation as it's transcoder.
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public interface InitializableTranscoder<T> extends Transcoder<T> {
    void init(OverridableReadOnlyProperties properties);
}