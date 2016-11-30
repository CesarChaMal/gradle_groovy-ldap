package org.xbib.groovy.ldap;

import org.junit.Assert;
import org.junit.Test;

public class JavaSearchTest extends Assert {

    @Test
    public void defaultConstructor() {
        Search search = new Search();
        assertEquals(SearchScope.SUB, search.getScope());
    }
}
