package kr.pe.kwonnam.hibernate4memcached.spymemcached;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import de.javakaffee.kryoserializers.KryoReflectionFactorySupport;
import kr.pe.kwonnam.hibernate4memcached.util.PropertiesUtils;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import java.util.Properties;

/**
 * * h4m.adapter.spymemcached.transcoder.compression.threashold.bytes=20000 # TODO LZ4 압축
 */
public class KryoTranscoder implements Transcoder<Object> {
    public static final int MAX_MEMCACHED_SIZE = 1024 * 1024 * 2;
    public static final String COMPRESSION_THREASHOLD_PROPERTY_KEY = SpyMemcachedAdapter.PROPERTY_KEY_PREFIX + ".transcoder.compression.threashold.bytes";

    private int bufferSize = MAX_MEMCACHED_SIZE;
    private int maxBufferSize = bufferSize;

    private Properties properties;
    private int compressionThreasholdBytes = 0;

    /**
     * Transcoder must have a constructor with a Properties argument.
     */
    public KryoTranscoder(Properties properties) {
        this.properties = properties;

        String compressionThreasholdBytesProperty = PropertiesUtils.getRequiredProeprties(properties, COMPRESSION_THREASHOLD_PROPERTY_KEY);
        compressionThreasholdBytes = Integer.parseInt(compressionThreasholdBytesProperty);
    }

    @Override
    public boolean asyncDecode(CachedData d) {
        return false;
    }

    @Override
    public CachedData encode(Object o) {
        Kryo kryo = createKryo();

        Output output = new Output(bufferSize, maxBufferSize);
        kryo.writeClassAndObject(output, o);
        byte[] bytes = output.toBytes();
        return new CachedData(0, bytes, getMaxSize());
    }

    private Kryo createKryo() {
        Kryo kryo = new KryoReflectionFactorySupport();
        kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
        return kryo;
    }

    @Override
    public Object decode(CachedData d) {
        Kryo kryo = createKryo();
        return kryo.readClassAndObject(new Input(d.getData()));
    }

    @Override
    public int getMaxSize() {
        return MAX_MEMCACHED_SIZE;
    }
}