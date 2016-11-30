package org.xbib.groovy.ldap

ldap = LDAP.newInstance('ldap://localhost:389', 'uid=admin,ou=system' ,'secret')

ldap.delete('cn=Joe Doe,dc=example,dc=com')
assert !ldap.exists('cn=Joe Doe,dc=example,dc=com')