package org.xbib.groovy.ldap;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * Contains all parameters for an LDAP search
 */
public class Search {
    private final String base;

    private final SearchScope scope;

    private final String filter;

    private final Object[] filterArgs;

    private final String[] attrs;

    public Search() {
        this("", SearchScope.SUB, "(objectClass=*)");
    }

    public Search(String base, SearchScope scope, String filter) {
        this.base = base;
        this.scope = scope;
        this.filter = filter;
        this.filterArgs = null;
        this.attrs = null;
    }

    public Search(Map<String, Object> map) {
        this.base = map.containsKey("base") ? map.get("base").toString() : "";
        this.scope = map.containsKey("scope") ?  SearchScope.valueOf(map.get("scope").toString()) : SearchScope.SUB;
        this.filter = map.containsKey("filter") ? map.get("filter").toString() : "(objectClass=*)";
        this.filterArgs = map.containsKey("filterArgs") ? toArray(Object.class, map.get("filterArgs")) : null;
        this.attrs = map.containsKey("attrs") ? toArray(String.class, map.get("attrs")) : null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] toArray(Class<T> target, Object value) {
        T[] values = null;
        if (value.getClass().isArray()) {
            values = (T[]) value;
        } else if (value instanceof Collection) {
            Collection<T> c = (Collection<T>) value;
            values = c.toArray((T[]) Array.newInstance(target, c.size()));
        } else {
            values = (T[]) Array.newInstance(target, 1);
            values[0] = (T) value;
        }
        return values;
    }

    public String[] getAttrs() {
        return attrs;
    }

    public String getBase() {
        return base;
    }

    public String getFilter() {
        return filter;
    }

    public Object[] getFilterArgs() {
        return filterArgs;
    }

    public SearchScope getScope() {
        return scope;
    }
}
