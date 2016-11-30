package org.xbib.groovy.ldap;

import javax.naming.directory.DirContext;

public enum ModificationType {
    ADD(DirContext.ADD_ATTRIBUTE),
    DELETE(DirContext.REMOVE_ATTRIBUTE),
    REPLACE(DirContext.REPLACE_ATTRIBUTE);

    private int jndiValue;

    ModificationType(int jndiValue) {
        this.jndiValue = jndiValue;
    }

    public int getJndiValue() {
        return jndiValue;
    }
}
