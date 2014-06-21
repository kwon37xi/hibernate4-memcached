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
    private String name;

    /**
     * if this value is true, the memcached adapter should implement namespace pattern
     * or ignore namespace pattern.
     * <p/>
     * see <a href="https://code.google.com/p/memcached/wiki/NewProgrammingTricks#Namespacing">Memcached Namespacing</a>
     */
    private boolean namespaceExpirationRequired;

    public CacheNamespace(String name, boolean namespaceExpirationRequired) {
        this.name = name;
        this.namespaceExpirationRequired = namespaceExpirationRequired;
    }

    public String getName() {
        return name;
    }

    public boolean isNamespaceExpirationRequired() {
        return namespaceExpirationRequired;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheNamespace that = (CacheNamespace) o;

        if (namespaceExpirationRequired != that.namespaceExpirationRequired) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (namespaceExpirationRequired ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CacheNamespace{" +
                "name='" + name + '\'' +
                ", namespaceExpirationRequired=" + namespaceExpirationRequired +
                '}';
    }
}
