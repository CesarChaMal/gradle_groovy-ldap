package org.xbib.groovy.ldap;

import groovy.lang.Closure;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper class which provides LDAP functionality to Groovy.
 */
public class LDAP {

    private static final Logger logger = Logger.getLogger(LDAP.class.getName());

    private static final String DEFAULT_URL = "ldap://localhost:389/";

    private final String url;

    private final String bindUser;

    private final String bindPassword;

    private LDAP(String url, String bindUser, String bindPassword) {
        this.url = url;
        this.bindUser = bindUser;
        this.bindPassword = bindPassword;
    }

    public static LDAP newInstance() {
        return new LDAP(DEFAULT_URL, null, null);
    }

    public static LDAP newInstance(String url) {
        return new LDAP(url, null, null);
    }

    public static LDAP newInstance(String url, String bindUser, String bindPassword) {
        return new LDAP(url, bindUser, bindPassword);
    }

    /**
     * LDAP add operation. Adds a new entry to the directory. The attributes have to be provided as a map.
     *
     * @param dn DN of the entry
     * @param attributes attributes of the entry
     * @throws NamingException if DN can not be resolved
     */
    public void add(final String dn, final Map<String, Object> attributes) throws NamingException {
        WithContext<Object> action = ctx -> {
            BasicAttributes attrs = new BasicAttributes();
            for (Map.Entry<String,Object> entry : attributes.entrySet()) {
                logger.log(Level.FINE, MessageFormat.format("entry {0} {1}", entry, entry.getValue().getClass()));
                Attribute attr = createAttribute(entry.getKey(), entry.getValue());
                logger.log(Level.FINE, MessageFormat.format("attr {0} {1}", attr, attr.get().getClass()));
                attrs.put(attr);
            }
            ctx.createSubcontext(dn, attrs);
            return null;
        };
        performWithContext(action);
    }

    /**
     * LDAP delete operation. Deletes an entry from the directory.
     *
     * @param dn  DN of the entry
     * @throws NamingException  if DN can not be resolved
     */
    public void delete(final String dn) throws NamingException {
        if (!exists(dn)) {
            throw new NameNotFoundException("Entry " + dn + " does not exist!");
        }
        WithContext<Object> action = ctx -> {
            ctx.destroySubcontext(dn);
            return null;
        };
        performWithContext(action);
    }

    /**
     * Reads an entry by its DN.
     * @param dn distinguished name
     * @return object
     * @throws NamingException if DN can not be resolved
     */
    public Object read(final String dn) throws NamingException {
        return performWithContext(ctx -> ctx.lookup(dn));
    }

    /**
     * Check whether an entry with the given DN exists. The method performs a search to check this, which is not so
     * efficient than just reading the entry.
     * @param dn distinguished name
     * @return true if exists
     * @throws NamingException if DN can not be resolved
     */
    public boolean exists(final String dn) throws NamingException {
        WithContext<Boolean> action = ctx -> {
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
            searchControls.setReturningAttributes(new String[0]);
            searchControls.setReturningObjFlag(false);
            try {
                ctx.search(dn, "(objectClass=*)", searchControls);
                return true;
            } catch (NameNotFoundException e) {
                logger.log(Level.FINEST, e.getMessage(), e);
            }
            return false;
        };
        return performWithContext(action);
    }

    /**
     * LDAP compare operation.
     *
     * @param dn        Distinguished name of the entry.
     * @param assertion attribute assertion.
     * @return true is comparison matches
     * @throws NamingException if DN can not be resolved
     */
    public boolean compare(final String dn, final Map<String, Object> assertion) throws NamingException {
        if (assertion.size() != 1) {
            throw new IllegalArgumentException("Assertion may only include one attribute");
        }
        WithContext<Boolean> action = ctx -> {
            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(new String[0]);
            searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);
            searchControls.setReturningObjFlag(false);
            String attrName = assertion.keySet().iterator().next();
            String filter = "(" + attrName + "={0})";
            Object value = assertion.get(attrName);
            NamingEnumeration<SearchResult> enumeration = ctx.search(dn, filter, new Object[]{value}, searchControls);
            return enumeration.hasMore();
        };
        return performWithContext(action);
    }

    /**
     * LDAP modify DN operation.
     *
     * @param dn Distinguished name of the entry.
     * @param newRDN new realtive distinguished name of the entry.
     * @param deleteOldRDN if old relative distinguished name should be deleted
     * @param newSuperior new superior DN
     * @throws NamingException  if DN can not be resolved
     */
    public void modifyDn(final String dn, final String newRDN, final boolean deleteOldRDN, final String newSuperior)
            throws NamingException {
        WithContext<Object> action = ctx -> {
            LdapName source = new LdapName(dn);
            LdapName target = new LdapName(newSuperior);
            target.add(newRDN);
            ctx.addToEnvironment("java.naming.ldap.deleteRDN", Boolean.toString(deleteOldRDN));
            ctx.rename(source, target);
            return null;
        };
        performWithContext(action);
    }

    public void eachEntry(String filter, String base, SearchScope scope, Closure closure) throws NamingException {
        eachEntry(new Search(base, scope, filter), closure);
    }

    public void eachEntry(Map<String, Object> searchParams, Closure closure) throws NamingException {
        eachEntry(new Search(searchParams), closure);
    }

    public void eachEntry(String filter, Closure closure) throws NamingException {
        eachEntry(filter, "", SearchScope.SUB, closure);
    }

    public void eachEntry(Search search, Closure closure) throws NamingException {
        WithContext<Object> action = ctx -> {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(search.getScope().getJndiValue());
            ctls.setReturningAttributes(search.getAttrs());
            ctls.setReturningObjFlag(true);
            NamingEnumeration<SearchResult> results = ctx.search(search.getBase(), search.getFilter(), search
                    .getFilterArgs(), ctls);
            while (results != null && results.hasMore()) {
                SearchResult sr = results.next();
                String dn = sr.getNameInNamespace();
                Attributes attrs = sr.getAttributes();
                NamingEnumeration<? extends Attribute> en = attrs.getAll();
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("dn", dn);
                while (en.hasMore()) {
                    Attribute attr = en.next();
                    String key = attr.getID();
                    map.put(key, attr.get(0).toString());
                }
                closure.call(map);
            }
            return null;
        };
        performWithContext(action);
    }

    public void modify(String dn, String modType, Map<String, Object> attributes) throws NamingException {
        modify(dn, ModificationType.valueOf(modType), attributes);
    }

    public void modify(String dn, ModificationType modType, Map<String, Object> attributes) throws NamingException {
        List<ModificationItem> mods = new ArrayList<>();
        for (String key : attributes.keySet()) {
            Attribute attr = createAttribute(key, attributes.get(key));
            ModificationItem item = new ModificationItem(modType.getJndiValue(), attr);
            mods.add(item);
        }
        ModificationItem[] modItems = mods.toArray(new ModificationItem[mods.size()]);
        WithContext<Object> action = ctx -> {
            ctx.modifyAttributes(dn, modItems);
            return null;
        };
        performWithContext(action);
    }

    public void modify(String dn, List<List> modificationItem) throws NamingException {
        List<ModificationItem> mods = new ArrayList<>();
        for (List pair : modificationItem) {
            if (pair.size() != 2) {
                throw new IllegalArgumentException("parameter 2 is not a list of pairs");
            }
            Object oModType = pair.get(0);
            ModificationType modType;
            if (oModType instanceof ModificationType) {
                modType = (ModificationType) oModType;
            } else if (oModType instanceof String) {
                modType = ModificationType.valueOf((String) oModType);
            } else {
                throw new IllegalArgumentException("parameter is not o valid ModificationType: " + oModType);
            }
            if (pair.get(1) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> attributes = (Map<String, Object>) pair.get(1);
                for (String key : attributes.keySet()) {
                    Attribute attr = createAttribute(key, attributes.get(key));
                    ModificationItem item = new ModificationItem(modType.getJndiValue(), attr);
                    mods.add(item);
                }
            }
        }
        ModificationItem[] modItems = mods.toArray(new ModificationItem[mods.size()]);
        WithContext<Object> action = ctx -> {
            ctx.modifyAttributes(dn, modItems);
            return null;
        };
        performWithContext(action);
    }

    public List<Map<String,Object>> search(String filter) throws NamingException {
        return search(new Search("", SearchScope.SUB, filter));
    }

    public List<Map<String,Object>> search(String base, SearchScope scope, String filter) throws NamingException {
        return search(new Search(base, scope, filter));
    }

    public List<Map<String,Object>> search(Map<String, Object> searchParams) throws NamingException {
        return search(new Search(searchParams));
    }

    public List<Map<String,Object>> search(Search search) throws NamingException {
        List<Map<String,Object>> result = new ArrayList<>();
        WithContext<Object> action = ctx -> {
            NamingEnumeration<SearchResult> results =
                    ctx.search(search.getBase(), search.getFilter(), search.getFilterArgs(), search.getSearchControls());
            while (results != null && results.hasMore()) {
                SearchResult sr = results.next();
                String dn = sr.getNameInNamespace();
                Attributes attrs = sr.getAttributes();
                NamingEnumeration<? extends Attribute> en = attrs.getAll();
                Map<String,Object> map = new LinkedHashMap<>();
                map.put("dn", dn);
                while (en.hasMore()) {
                    Attribute attr = en.next();
                    String key = attr.getID();
                    if (attr.size() == 1) {
                        map.put(key, attr.get());
                    } else {
                        List<Object> l = new ArrayList<>();
                        for (int i = 0; i < attr.size(); ++i) {
                            l.add(attr.get(i));
                        }
                        map.put(key, l);
                    }
                }
                result.add(map);
            }
            return null;
        };
        performWithContext(action);
        return result;
    }

    /**
     * Open an LDAP context and perform a given task within this context.
     *
     * @param <T> parameter type
     * @param action action
     * @return an action result
     * @throws NamingException naming exception
     */
    private <T> T performWithContext(WithContext<T> action) throws NamingException {
        LdapContext ctx = null;
        try {
            ctx = new InitialLdapContext(createEnvironment(url, bindUser, bindPassword), null);
            return action.perform(ctx);
        } finally {
            try {
                if (ctx != null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                logger.log(Level.FINEST, e.getMessage(), e);
            }
        }
    }

    private static Properties createEnvironment(String url, String bindUser, String bindPassword) {
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.setProperty(Context.PROVIDER_URL, url);
        if (bindUser != null) {
            env.setProperty(Context.SECURITY_PRINCIPAL, bindUser);
            env.setProperty(Context.SECURITY_CREDENTIALS, bindPassword);
        }
        return env;
    }

    private static Attribute createAttribute(String name, Object value) {
        Attribute attr = new BasicAttribute(name);
        if (value instanceof Collection) {
            Collection<?> values = (Collection<?>) value;
            for (Object val : values) {
                attr.add(val);
            }
        } else {
            attr.add(value);
        }
        return attr;
    }
}
