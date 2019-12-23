package org.xbib.groovy.ldap

ldap = LDAP.newInstance('ldap://localhost:10389', 'uid=admin,ou=system' ,'secret')

ldap.eachEntry(base:'dc=example,dc=com', filter:'(objectClass=person)', scope:'ONE') { entry ->
    ldap.delete(entry.dn)
}
