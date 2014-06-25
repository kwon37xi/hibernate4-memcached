package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyProperties;
import kr.pe.kwonnam.hibernate4memcached.util.OverridableReadOnlyPropertiesImpl;
import net.spy.memcached.CachedData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

public class KryoTranscoderTest {

    private Logger log = LoggerFactory.getLogger(KryoTranscoderTest.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private KryoTranscoder kryoTranscoder;


    private void givenKryoTranscoder(int compressionThreadHold) {
        Properties properties = new Properties();
        properties.setProperty(KryoTranscoder.COMPRESSION_THREASHOLD_PROPERTY_KEY, String.valueOf(compressionThreadHold));

        kryoTranscoder = new KryoTranscoder();
        kryoTranscoder.init(new OverridableReadOnlyPropertiesImpl(properties));
    }

    @Test
    public void kryoEncodeDecode() throws Exception {
        givenKryoTranscoder(LOREM_IPSUM_BYTES.length * 2);
        CachedData cachedData = kryoTranscoder.encode(LOREM_IPSUM);

        assertThat(cachedData.getFlags()).isEqualTo(KryoTranscoder.BASE_FLAG);

        log.debug("normal data size : {} / original {}", cachedData.getData().length, LOREM_IPSUM_BYTES.length);
        assertThat(cachedData.getData().length).isGreaterThan(LOREM_IPSUM_BYTES.length); //not compressed

        Object decoded = kryoTranscoder.decode(cachedData);
        assertThat(decoded).isEqualTo(LOREM_IPSUM);
    }

    @Test
    public void kryoEncodeDecode_with_compression() throws Exception {
        givenKryoTranscoder(LOREM_IPSUM_BYTES.length / 2);

        CachedData cachedData = kryoTranscoder.encode(LOREM_IPSUM);
        assertThat(cachedData.getFlags() & KryoTranscoder.COMPRESS_FLAG).isGreaterThanOrEqualTo(1);

        log.debug("compressed data size : {} / original : {}", cachedData.getData().length, LOREM_IPSUM_BYTES.length);
        assertThat(cachedData.getData().length).isLessThan(LOREM_IPSUM_BYTES.length);

        Object decoded = kryoTranscoder.decode(cachedData);
        assertThat(decoded).isEqualTo(LOREM_IPSUM);
    }

    private static final String LOREM_IPSUM = "\n" +
            "\n" +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque pellentesque non mi et tempor." +
            " Etiam venenatis orci in lectus consectetur rutrum. Aliquam erat volutpat. Donec non mauris massa. " +
            "Mauris suscipit, arcu vitae ornare rutrum, odio odio ullamcorper lorem, at dignissim purus erat vel " +
            "odio. Nullam quis mollis augue. Nullam sit amet rhoncus eros, at varius nisi. Vestibulum lobortis, " +
            "justo vitae placerat vestibulum, leo felis condimentum ante, mattis aliquet felis velit in leo. " +
            "Praesent at dolor in sapien laoreet vulputate vitae in justo. Duis gravida elit arcu, at tempor sem " +
            "scelerisque ac.\n" +
            "\n" +
            "Nunc pharetra sollicitudin quam, id egestas nisi dignissim ac. Nam lorem orci, laoreet eget metus non," +
            " tincidunt hendrerit justo. Quisque nisl enim, semper quis mauris sed, rhoncus cursus tortor. Fusce " +
            "malesuada libero eu condimentum blandit. Nullam mollis lacus sit amet justo dictum, eu egestas lorem " +
            "pretium. Maecenas varius est non arcu fermentum mattis. Donec et risus enim. Proin quis fermentum odio." +
            " Nam luctus mi at risus pharetra fringilla. Suspendisse arcu nisl, fermentum vitae accumsan gravida, " +
            "sodales in dolor. Sed ante tortor, convallis sed commodo non, molestie eu elit. Praesent diam quam, " +
            "convallis sed rutrum ut, sagittis vitae nisl. Donec non dolor tincidunt est fermentum sagittis. " +
            "Pellentesque quis quam arcu. Curabitur volutpat lacinia fermentum. Donec luctus, lacus nec gravida " +
            "facilisis, urna velit faucibus turpis, id placerat quam nulla sed lectus.\n" +
            "\n" +
            "Praesent facilisis velit risus, sed facilisis metus convallis non. Nulla convallis mauris vitae" +
            " urna hendrerit ultrices in quis augue. Nulla iaculis risus ligula, at congue lorem varius vitae. " +
            "Nulla ut odio dapibus, cursus turpis in, vestibulum felis. Nunc cursus nisl id leo euismod placerat." +
            " Aenean luctus tellus sit amet dolor dignissim, in lacinia libero blandit. Donec sem magna, volutpat " +
            "ut odio tempus, sollicitudin hendrerit risus. Quisque fringilla nunc tristique tellus volutpat venenatis. " +
            "Nulla at cursus erat, at bibendum purus. Aenean vitae lorem urna. Sed et libero quis mauris sodales porta. " +
            "Duis euismod arcu nec velit aliquet volutpat. Donec id placerat nibh.\n" +
            "\n" +
            "Donec varius ipsum ut tellus gravida, ut feugiat risus commodo. Aliquam scelerisque, erat " +
            "sollicitudin sagittis hendrerit, urna justo vehicula augue, nec lobortis dolor mauris eget est. " +
            "Proin est elit, tristique a metus vel, sollicitudin iaculis justo. Duis sodales est quis pretium " +
            "fringilla. Nunc commodo malesuada velit. Vestibulum a dictum diam. Duis tristique massa porttitor, " +
            "pulvinar eros malesuada, venenatis nisl. Phasellus eu velit quis dui lacinia tempus.\n" +
            "\n" +
            "Praesent viverra magna at purus consequat convallis. Nulla semper cursus porttitor. Sed malesuada " +
            "sollicitudin tortor. Curabitur interdum porttitor risus, at feugiat urna ultrices ac. Proin augue nulla, " +
            "commodo blandit convallis at, placerat sed elit. Integer porta ipsum eu sollicitudin hendrerit. Duis in " +
            "leo semper, euismod dolor quis, tincidunt felis. Class aptent taciti sociosqu ad litora torquent per " +
            "conubia nostra, per inceptos himenaeos. Donec libero sem, tempus ut elit at, luctus laoreet lacus. Morbi" +
            " quis porttitor eros. Sed congue lacus ipsum, id pulvinar felis malesuada ut. Donec nec turpis molestie, " +
            "viverra lectus in, iaculis sapien.";

    private static final byte[] LOREM_IPSUM_BYTES = LOREM_IPSUM.getBytes(Charset.forName("UTF-8"));
}