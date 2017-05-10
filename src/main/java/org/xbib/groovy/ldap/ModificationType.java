package org.xbib.groovy.ldap;

import javax.naming.directory.DirContext;

/**
 * Modification types for LDAP attributes.
 */
public enum ModificationType {

    ADD(DirContext.ADD_ATTRIBUTE),
    DELETE(DirContext.REMOVE_ATTRIBUTE),
    REPLACE(DirContext.REPLACE_ATTRIBUTE);

    private int value;

    ModificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
