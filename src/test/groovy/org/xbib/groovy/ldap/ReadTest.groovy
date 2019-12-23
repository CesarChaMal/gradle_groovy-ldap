package org.xbib.groovy.ldap

ldap = LDAP.newInstance("ldap://localhost:10389")

// Simple entry lookup via dn
heather = ldap.read('cn=Heather Nova,dc=example,dc=com')

print """
DN: ${heather.dn}
Common name: ${heather.cn}
Object classes: ${heather.objectclass}
"""
