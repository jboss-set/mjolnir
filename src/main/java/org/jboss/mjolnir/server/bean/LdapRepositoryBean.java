package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.client.exception.ApplicationException;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class LdapRepositoryBean implements LdapRepository {

    private InitialDirContext ctx;

    @EJB
    private ApplicationParameters applicationParameters;

    @PostConstruct
    public void init() {
        final String ldapUrl = applicationParameters.getParameter(ApplicationParameters.LDAP_URL_KEY);

        final Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);

        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new ApplicationException("Couldn't create directory context.", e);
        }
    }

    @Override
    public boolean checkUserRecord(String uid) {
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[] {"uid"});

        try {
            final NamingEnumeration<SearchResult> results =
                    ctx.search("ou=users,dc=redhat,dc=com", "uid=" + uid, searchControls);
            final boolean found = results.hasMore();
            results.close();
            return found;
        } catch (NamingException e) {
            throw new ApplicationException("Couldn't perform directory search.", e);
        }
    }

}
