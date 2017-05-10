package org.xbib.groovy.ldap;

import javax.naming.directory.SearchControls;

/**
 * Enumeration for the search scope options. To be used in LDAP search operations.
 */
public enum SearchScope {

    BASE(SearchControls.OBJECT_SCOPE),
    ONE(SearchControls.ONELEVEL_SCOPE),
    SUB(SearchControls.SUBTREE_SCOPE);

    private int value;

    SearchScope(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
