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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@inheritDoc}
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Stateless
public class LdapRepositoryBean implements LdapRepository {

    private final static String CONTEXT_NAME = "ou=users,dc=redhat,dc=com";
    private final static int GROUPING_FACTOR = 100; // query for so many users at a time

    private InitialDirContext ctx;
    private SearchControls searchControls;

    @EJB
    private ApplicationParameters applicationParameters;

    @PostConstruct
    public void init() {
        // fetch ldap url
        final String ldapUrl = applicationParameters.getMandatoryParameter(ApplicationParameters.LDAP_URL_KEY);

        // prepare naming context
        final Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);

        try {
            ctx = new InitialDirContext(env);
        } catch (NamingException e) {
            throw new ApplicationException("Couldn't create directory context.", e);
        }

        // prepare SearchControls instance
        searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(new String[] {"uid"});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkUserExists(String uid) {
        try {
            final NamingEnumeration<SearchResult> results =
                    ctx.search(CONTEXT_NAME, "uid=" + uid, searchControls);
            final boolean found = results.hasMore();
            results.close();
            return found;
        } catch (NamingException e) {
            throw new ApplicationException("Couldn't perform directory search.", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Note: This implementation tries to minimize number of LDAP requests by searching for set of users in every query.
     * Exact number of users every query is searching for is determined by static property GROUPING_FACTOR.
     */
    @Override
    public Map<String, Boolean> checkUsersExists(Set<String> users) {
        final Map<String, Boolean> result = new HashMap<String, Boolean>();
        final Iterator<String> iterator = users.iterator();
        final List<String> tempUserList = new ArrayList<String>(GROUPING_FACTOR);
        while (iterator.hasNext()) {
            tempUserList.add(iterator.next());
            if (tempUserList.size() >= GROUPING_FACTOR || !iterator.hasNext()) {
                final Map<String, Boolean> tempResultMap = checkUsersSubsetExists(tempUserList);
                result.putAll(tempResultMap);
                tempUserList.clear();
            }
        }
        return result;
    }

    private Map<String, Boolean> checkUsersSubsetExists(List<String> users) {
        try {
            // build a query
            final StringBuilder query = new StringBuilder("(|");
            for (String uid: users) {
                query.append("(uid=")
                        .append(uid)
                        .append(")");
            }
            query.append(")");

            final NamingEnumeration<SearchResult> searchResults =
                    ctx.search(CONTEXT_NAME, query.toString(), searchControls);

            // fill the result map with found users
            final Map<String, Boolean> result = new HashMap<String, Boolean>();
            while (searchResults.hasMore()) {
                final SearchResult next = searchResults.next();
                String uid = (String) next.getAttributes().get("uid").get();
                result.put(uid, true);
            }
            searchResults.close();

            // fill the result map with users that weren't found
            for (String uid: users) {
                if (!result.containsKey(uid)) {
                    result.put(uid, false);
                }
            }

            return result;
        } catch (NamingException e) {
            throw new ApplicationException("Couldn't perform directory search.", e);
        }
    }

}
