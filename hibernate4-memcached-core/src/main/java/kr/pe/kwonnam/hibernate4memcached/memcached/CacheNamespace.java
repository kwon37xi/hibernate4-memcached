package kr.pe.kwonnam.hibernate4memcached.memcached;

import java.io.Serializable;

/**
 * Cache Region(namespace).
 *
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
public class CacheNamespace implements Serializable {
    /**
     * name of region
     */
    private String regionName;

    /**
     * if this value is true, the memcached adapter should implement namespace pattern
     * or ignore namespace pattern.
     * <p/>
     * see <a href="https://code.google.com/p/memcached/wiki/NewProgrammingTricks#Namespacing">Memcached Namespacing</a>
     */
    private boolean regionExpirationRequired;

    public CacheNamespace(String regionName, boolean regionExpirationRequired) {
        this.regionName = regionName;
        this.regionExpirationRequired = regionExpirationRequired;
    }

    public String getRegionName() {
        return regionName;
    }

    public boolean isRegionExpirationRequired() {
        return regionExpirationRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheNamespace that = (CacheNamespace) o;

        if (!regionName.equals(that.regionName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return regionName.hashCode();
    }

    @Override
    public String toString() {
        return "CacheNamespace{" +
                "regionName='" + regionName + '\'' +
                ", regionExpirationRequired=" + regionExpirationRequired +
                '}';
    }
}
