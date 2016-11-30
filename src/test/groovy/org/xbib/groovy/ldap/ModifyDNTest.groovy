package org.xbib.groovy.ldap

ldap = LDAP.newInstance('ldap://zanzibar:10389', 'uid=admin,ou=system' ,'secret')

superior='dc=example,dc=com'
dn = 'cn=Myra Ellen Amos,dc=example,dc=com'
newRdn = 'cn=Tori Amos'
newDn = 'cn=Tori Amos,dc=example,dc=com'

assert !ldap.exists(dn)
attrs = [
  objectclass: ['top', 'person'],              
  sn: 'Amos',
  cn: ['Tori Amos', 'Myra Ellen Amos'],
]
ldap.add(dn, attrs)
assert ldap.exists(dn)
ldap.modifyDn(dn, newRdn, true, superior)
assert ldap.exists(newDn)
tori = ldap.read(newDn)
assert tori.cn == 'Tori Amos'
ldap.delete(newDn)