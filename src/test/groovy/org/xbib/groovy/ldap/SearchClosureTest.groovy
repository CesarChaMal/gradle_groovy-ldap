package org.xbib.groovy.ldap

ldap = LDAP.newInstance('ldap://zanzibar:10389/dc=example,dc=com')

ldap.eachEntry ('(objectClass=person)') { person ->  
    println "${person.cn} (${person.dn})"
}

