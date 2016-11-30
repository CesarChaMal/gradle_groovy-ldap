package org.xbib.groovy.ldap

ldap = LDAP.newInstance('ldap://localhost:389', 'uid=admin,ou=system' ,'secret')

assert ! ldap.exists('cn=Joe Doe,dc=example,dc=com')

attrs = [
  objectclass: ['top', 'person'],              
  sn: 'Doe',
  cn: 'Joe Doe',
  userPassword: 'secret'
]
ldap.add('cn=Joe Doe,dc=example,dc=com', attrs)
assert ldap.exists('cn=Joe Doe,dc=example,dc=com')
assert ldap.compare('cn=Joe Doe,dc=example,dc=com', [cn: 'Joe Doe'] )
assert ldap.compare('cn=Joe Doe,dc=example,dc=com', [cn: 'JOE DOE'] )
assert ldap.compare('cn=Joe Doe,dc=example,dc=com', [userPassword: 'secret'] )
assert ! ldap.compare('cn=Joe Doe,dc=example,dc=com', [userPassword: 'SECRET'] )
ldap.delete('cn=Joe Doe,dc=example,dc=com')