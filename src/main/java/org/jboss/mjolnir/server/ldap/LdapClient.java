package org.jboss.mjolnir.server.ldap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class LdapClient {

    private InitialDirContext ctx;
    private SearchControls searchControls;

    public LdapClient(String ldapUrl) throws NamingException {
        // prepare naming context
        final Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put("com.sun.jndi.ldap.connect.pool", "true"); // enable connection pooling, prevents "Connection closed" problems

        ctx = new InitialDirContext(env);

        // prepare SearchControls instance
        searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[] {"uid"});
    }

    public NamingEnumeration<SearchResult> search(String contextName, String filter) throws NamingException {
        return ctx.search(contextName, filter, searchControls);
    }
}
