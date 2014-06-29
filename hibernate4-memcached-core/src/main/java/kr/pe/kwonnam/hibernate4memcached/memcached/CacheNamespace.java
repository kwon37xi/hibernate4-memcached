package kr.pe.kwonnam.hibernate4memcached.memcached;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        CacheNamespace otherCacheNamespace = (CacheNamespace) o;

        return new EqualsBuilder().append(name, otherCacheNamespace.name)
                .append(namespaceExpirationRequired, otherCacheNamespace.namespaceExpirationRequired).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(namespaceExpirationRequired).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("namespaceExpirationRequired", namespaceExpirationRequired).toString();
    }
}
