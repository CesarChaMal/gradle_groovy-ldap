package org.xbib.groovy.ldap;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import javax.naming.directory.SearchControls;

/**
 * Contains all parameters for an LDAP search.
 */
public class Search {

    public static final int DEFAULT_TIME_LIMIT = 5000;

    public static final int DEFAULT_COUNT_LIMIT = 20000;

    private final SearchControls searchControls;

    private final String base;

    private final SearchScope scope;

    private final String filter;

    private final Object[] filterArgs;

    private final String[] attrs;

    private int timeLimit;

    private int countLimit;

    public Search() {
        this("", SearchScope.SUB, "(objectClass=*)", DEFAULT_TIME_LIMIT, DEFAULT_COUNT_LIMIT);
    }

    public Search(String base, SearchScope scope, String filter) {
        this(base, scope, filter, DEFAULT_TIME_LIMIT, DEFAULT_COUNT_LIMIT);
    }

    public Search(String base, SearchScope scope, String filter, int timeLimit, int countLimit) {
        this.base = base;
        this.scope = scope;
        this.filter = filter;
        this.filterArgs = null;
        this.attrs = null;
        this.timeLimit = timeLimit;
        this.countLimit = countLimit;
        this.searchControls = getSearchControls(scope, null, timeLimit, countLimit);
    }

    public Search(Map<String, Object> map) {
        this.base = map.containsKey("base") ? map.get("base").toString() : "";
        this.scope = map.containsKey("scope") ?  SearchScope.valueOf(map.get("scope").toString()) : SearchScope.SUB;
        this.filter = map.containsKey("filter") ? map.get("filter").toString() : "(objectClass=*)";
        this.filterArgs = map.containsKey("filterArgs") ? toArray(Object.class, map.get("filterArgs")) : null;
        this.attrs = map.containsKey("attrs") ? toArray(String.class, map.get("attrs")) : null;
        this.timeLimit = map.containsKey("timeLimit") ?
                Integer.parseInt((String) map.get("timeLimit")) : DEFAULT_TIME_LIMIT;
        this.countLimit = map.containsKey("countLimit") ?
                Integer.parseInt((String) map.get("countLimit")) : DEFAULT_COUNT_LIMIT;
        this.searchControls = getSearchControls(scope, attrs, timeLimit, countLimit);
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

    public SearchControls getSearchControls() {
        return searchControls;
    }

    private static SearchControls getSearchControls(SearchScope searchScope,
                                                    String[] attrs,
                                                    int timeLimit,
                                                    int countLimit) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(searchScope.getValue());
        searchControls.setReturningAttributes(attrs);
        searchControls.setReturningObjFlag(true);
        searchControls.setTimeLimit(timeLimit);
        searchControls.setCountLimit(countLimit);
        return searchControls;
    }
}
