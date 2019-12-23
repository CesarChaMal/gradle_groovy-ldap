package org.xbib.groovy.ldap

ldap = LDAP.newInstance('ldap://localhost:10389/')

results = ldap.search('dc=example,dc=com', SearchScope.ONE, '(objectClass=person)')
println " ${results.size} entries found ".center(40,'-')
for (entry in results) {
  println entry.dn
}

println ""

results = ldap.search(filter: '(objectClass=person)', base: 'dc=example,dc=com', scope: 'ONE')
println " ${results.size} entries found ".center(40,'-')
for (entry in results) {
  println entry.dn
}

println ""

def params = new JavaSearchTest()
params.filter='(objectClass=person)'
params.base='dc=example,dc=com'
params.scope=SearchScope.ONE

results = ldap.search(params)
println " ${results.size} entries found ".center(40,'-')
for (entry in results) {
  println entry.dn
}