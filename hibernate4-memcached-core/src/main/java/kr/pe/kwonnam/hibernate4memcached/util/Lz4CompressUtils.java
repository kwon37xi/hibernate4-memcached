package kr.pe.kwonnam.hibernate4memcached.util;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;

/**
 * Lz4 Compression Utils. L4z is much faster than gzip
 *
 * <a href="https://github.com/jpountz/lz4-java">lz4-java</a>
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class Lz4CompressUtils {
    private static final LZ4Factory factory =  LZ4Factory.fastestInstance();

    public static byte[] compress(final byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }

        LZ4Compressor compressor = factory.fastCompressor();

        return compressor.compress(data);
    }

    /**
     * When the exact decompressed size is unknown.
     * Decompress data size cannot be larger then maxDecompressedSize
     */
    public static byte[] decompressSafe(final byte[] src, int maxDecompressedSize) {
        if (src == null) {
            throw new IllegalArgumentException("src must not be null.");
        }

        if (maxDecompressedSize <= 0) {
            throw new IllegalArgumentException("maxDecompressedSize must be larger than 0 but " + maxDecompressedSize);
        }

        LZ4SafeDecompressor decompressor = factory.safeDecompressor();

        return decompressor.decompress(src, maxDecompressedSize);
    }

    /**
     * When the exact decompressed size is known, use this method to decompress. It's faster.
     *
     * @see net.jpountz.lz4.LZ4FastDecompressor
     */
    public static byte[] decompressFast(byte[] src, int srcOffset, int exactDecompressedSize) {
        if (src == null) {
            throw new IllegalArgumentException("src must not be null.");
        }

        if (srcOffset < 0) {
            throw new IllegalArgumentException("srcOffset must equal to or larger than 0 but " + srcOffset);
        }

        if (exactDecompressedSize < 0) {
            throw new IllegalArgumentException("exactDecompressedSize must equal to or larger than 0 but " + exactDecompressedSize);
        }

        LZ4FastDecompressor decompressor = factory.fastDecompressor();

        return decompressor.decompress(src, srcOffset, exactDecompressedSize);
    }

    /**
     * @see #decompressFast(byte[], int, int)
     */
    public static byte[] decompressFast(final byte[] src, int exactDecompressedSize) {
        return decompressFast(src, 0, exactDecompressedSize);
    }
}
