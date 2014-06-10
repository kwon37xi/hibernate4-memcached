package kr.pe.kwonnam.hibernate4memcached.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.fest.assertions.api.Assertions.assertThat;

public class IntToBytesUtilsTest {
    private Logger log = LoggerFactory.getLogger(IntToBytesUtilsTest.class);

    @Test
    public void intToBytes() {
        assertIntBytesConvert(0);
        assertIntBytesConvert(1);
        assertIntBytesConvert(-1);
        assertIntBytesConvert(500);
        assertIntBytesConvert(-500);
        assertIntBytesConvert(Integer.MAX_VALUE);
        assertIntBytesConvert(Integer.MIN_VALUE);

    }

    private void assertIntBytesConvert(final int value) {
        byte[] bytes = IntToBytesUtils.intToBytes(value);

        log.debug("int {} to bytes : {}", value, bytes);
        assertThat(bytes).hasSize(4); // 32bit

        assertThat(IntToBytesUtils.bytesToInt(bytes)).isEqualTo(value);
    }

}