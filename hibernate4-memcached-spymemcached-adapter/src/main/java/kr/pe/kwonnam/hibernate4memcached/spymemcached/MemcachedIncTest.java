package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.LongTranscoder;

import java.io.IOException;

/**
 *
 */
public class MemcachedIncTest {

    public static final String KEY = "testincr";

    public static void main(String[] args) throws IOException {
        ConnectionFactory cf = new ConnectionFactoryBuilder().setProtocol(ConnectionFactoryBuilder.Protocol.BINARY).build();
        MemcachedClient mc = new MemcachedClient(cf, AddrUtil.getAddresses("localhost:11211"));

        mc.delete(KEY);

        long currentincr = 0;

        for (int i = 1; i < 2000; i++) {

            currentincr = increase(mc, currentincr);
            System.out.println(i + " : " + currentincr);

        }
        mc.shutdown();
    }

    private static long increase(MemcachedClient mc, long currentincr) {
//        if (currentincr == Long.MAX_VALUE) {
//            mc.delete(KEY);
//            return mc.incr(KEY, 1L, 1, 1000);
//        }

        return mc.incr(KEY, 1L, Long.MAX_VALUE - 1000, 1000);
    }

}
