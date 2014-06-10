package kr.pe.kwonnam.hibernate4memcached.util;

import net.jpountz.lz4.LZ4Exception;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

import static org.fest.assertions.api.Assertions.assertThat;

public class Lz4CompressUtilsTest {
    private Logger log = LoggerFactory.getLogger(Lz4CompressUtilsTest.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public static final String ORIGINAL_DATA = "\n" +
            "\n" +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sed nibh vitae eros imperdiet feugiat. Nulla consequat pharetra tellus vel tempor. In viverra dui ligula, eget tincidunt nisl laoreet sit amet. Duis fermentum luctus eros varius pellentesque. Etiam gravida sodales elit quis mattis. Nullam turpis felis, luctus a fringilla in, rutrum non dui. Ut a eros rutrum, interdum est vitae, porta mauris. Nulla semper tellus felis, sed varius tortor consequat ut. Aliquam suscipit ornare metus, at tincidunt dolor porttitor a. Integer eu lacus et ligula lacinia rutrum. Integer porta leo et lectus elementum placerat.\n" +
            "\n" +
            "Aenean imperdiet cursus tellus vitae auctor. Aliquam erat volutpat. Donec vel turpis porttitor, convallis erat id, semper nisi. Aenean blandit mollis turpis, luctus blandit lectus vehicula in. In hac habitasse platea dictumst. Nullam accumsan eros nisi, vel ullamcorper lorem facilisis eget. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Phasellus nec sem eu quam consectetur pretium et in purus.\n" +
            "\n" +
            "Nunc ut dignissim erat, in commodo urna. Integer auctor suscipit nulla non feugiat. Morbi sed tincidunt leo, et condimentum magna. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nulla suscipit quam in urna varius, nec volutpat diam interdum. Vivamus nec scelerisque sem, vitae gravida diam. Praesent vitae adipiscing ipsum. Praesent pulvinar sem mi, gravida cursus libero fringilla a.\n" +
            "\n" +
            "Donec et sapien id diam tincidunt tincidunt ac et nisi. Maecenas viverra erat mattis convallis placerat. Curabitur augue elit, vehicula eu porttitor ultricies, vehicula eget ipsum. Suspendisse at sodales mauris, non ultrices nulla. Aliquam hendrerit lobortis purus, at rutrum lectus porta et. Etiam venenatis venenatis congue. Ut in ante pretium lacus fermentum tempus. Praesent in ante dignissim, porttitor mauris sed, vestibulum tortor. Suspendisse ultrices consectetur nibh, et venenatis enim elementum et. Nam tincidunt dictum turpis. Vestibulum bibendum sem ac eros varius bibendum.\n" +
            "\n" +
            "Cras ac massa quis odio laoreet tristique. Donec at luctus nunc, vel posuere felis. Vestibulum vulputate congue varius. Suspendisse orci erat, commodo vel fringilla quis, fringilla ac mauris. Maecenas interdum congue purus quis ornare. Donec sit amet hendrerit eros. Vestibulum elementum accumsan porta. Aenean dignissim quam nunc, id elementum massa molestie sed. Pellentesque faucibus odio tempor orci ultricies blandit. ";

    public static final byte[] ORIGINAL_DATA_BYTES;

    static {
        try {
            ORIGINAL_DATA_BYTES = ORIGINAL_DATA.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void compress_and_decompressSafe() {
        byte[] compressed = Lz4CompressUtils.compress(ORIGINAL_DATA_BYTES);

        assertThat(compressed).isNotNull();

        assertThat(compressed.length).isLessThan(ORIGINAL_DATA_BYTES.length);

        byte[] decompressed = Lz4CompressUtils.decompressSafe(compressed, ORIGINAL_DATA_BYTES.length);
        assertThat(decompressed).isEqualTo(ORIGINAL_DATA_BYTES);
    }

    @Test
    public void compress_and_decompressSafe_over_decompressLength() {
        byte[] compressed = Lz4CompressUtils.compress(ORIGINAL_DATA_BYTES);

        byte[] decompressed = Lz4CompressUtils.decompressSafe(compressed, ORIGINAL_DATA_BYTES.length + 100);
        assertThat(decompressed).isEqualTo(ORIGINAL_DATA_BYTES);
        assertThat(decompressed.length).isEqualTo(ORIGINAL_DATA_BYTES.length);
    }

    @Test
    public void compress_and_decompressSafe_less_decompressLength() {
        byte[] compressed = Lz4CompressUtils.compress(ORIGINAL_DATA_BYTES);

        expectedException.expect(LZ4Exception.class);
        Lz4CompressUtils.decompressSafe(compressed, ORIGINAL_DATA_BYTES.length - 100);
    }

    @Test
    public void compress_and_decompressFast() {
        byte[] compressed = Lz4CompressUtils.compress(ORIGINAL_DATA_BYTES);

        byte[] decompressed = Lz4CompressUtils.decompressFast(compressed, 0, ORIGINAL_DATA_BYTES.length);
        assertThat(decompressed).isEqualTo(ORIGINAL_DATA_BYTES);
    }

    @Test
    public void compress_and_decompressFast_srcOffset() {
        byte[] compressed = Lz4CompressUtils.compress(ORIGINAL_DATA_BYTES);

        byte[] additionalData = new byte[]{0, 1, 2, 3, 4, 5, 6,};
        int srcOffset = additionalData.length;
        byte[] bytesAddedCompressed = ArrayUtils.addAll(additionalData, compressed);
        log.debug("bytesAddedCompressed size : {}, compressed size : {}", bytesAddedCompressed.length, compressed.length);
        assertThat(bytesAddedCompressed).hasSize(compressed.length + srcOffset);

        byte[] decompressed = Lz4CompressUtils.decompressFast(bytesAddedCompressed, srcOffset, ORIGINAL_DATA_BYTES.length);

        assertThat(decompressed).isEqualTo(ORIGINAL_DATA_BYTES);
    }

    @Test
    public void decompressFast_less_exactDecompressedSize() {
        byte[] compressed = Lz4CompressUtils.compress(ORIGINAL_DATA_BYTES);

        expectedException.expect(LZ4Exception.class);
        Lz4CompressUtils.decompressFast(compressed, ORIGINAL_DATA_BYTES.length - 100);
    }

    @Test
    public void decompressFast_over_exactDecompressedSize() {
        byte[] compressed = Lz4CompressUtils.compress(ORIGINAL_DATA_BYTES);

        expectedException.expect(LZ4Exception.class);
        Lz4CompressUtils.decompressFast(compressed, ORIGINAL_DATA_BYTES.length + 100);
    }

    @Test
    public void decompressSafe_maxDecompressedSize_is_0() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        Lz4CompressUtils.decompressSafe(new byte[]{1, 2, 3}, 0);
    }

    @Test
    public void decompressFast_srcOffset_is_lessthan_0() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        Lz4CompressUtils.decompressFast(new byte[]{1, 2, 3}, -1, 10);
    }

    @Test
    public void decompressFast_exactDecompressedSize_is_lessthan_0() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        Lz4CompressUtils.decompressFast(new byte[]{1, 2, 3}, 0, -1);

    }
}
