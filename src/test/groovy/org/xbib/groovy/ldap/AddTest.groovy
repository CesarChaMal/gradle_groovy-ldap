package org.xbib.groovy.ldap

ldap = LDAP.newInstance('ldap://localhost:10389', 'uid=admin,ou=system' ,'secret')

assert ! ldap.exists('cn=Joe Doe,dc=example,dc=com')

attrs = [
  objectclass: ['top', 'person'],              
  sn: 'Doe',
  cn: 'Joe DOe'
]
ldap.add('cn=Joe Doe,dc=example,dc=com', attrs)

assert ldap.exists('cn=Joe Doe,dc=example,dc=com')