package org.jboss.mjolnir.server.bean;

import org.jboss.logging.Logger;
import org.jboss.mjolnir.client.exception.ApplicationException;
import org.jboss.mjolnir.server.ldap.LdapClient;
import org.jboss.mjolnir.server.util.KerberosUtils;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.HashMap;
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

    private static final String CONTEXT_NAME = "ou=users,dc=redhat,dc=com";
    private static final int GROUPING_FACTOR = 50; // query for so many users at a time
    private static final String LDAP_SEARCH_ERROR = "Couldn't create LDAP client instance";
    private static final Logger logger = Logger.getLogger(LdapRepositoryBean.class);

    @EJB
    private ApplicationParameters applicationParameters;

    private LdapClient ldapClient;

    @PostConstruct
    public void init() {
        // fetch ldap url
        final String ldapUrl = applicationParameters.getMandatoryParameter(ApplicationParameters.LDAP_URL_KEY);

        try {
            ldapClient = new LdapClient(ldapUrl);
        } catch (NamingException e) {
            throw new ApplicationException("Couldn't create LDAP client instance", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkUserExists(String uid) {
        try {
            final NamingEnumeration<SearchResult> results =
                    ldapClient.search(CONTEXT_NAME, "uid=" + KerberosUtils.normalizeUsername(uid));
            final boolean found = results.hasMore();
            results.close();
            return found;
        } catch (NamingException e) {
            logger.error(LDAP_SEARCH_ERROR, e);
            throw new ApplicationException(LDAP_SEARCH_ERROR + e.getMessage(), e);
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
                        .append(KerberosUtils.normalizeUsername(uid))
                        .append(")");
            }
            query.append(")");

            final NamingEnumeration<SearchResult> searchResults =
                    ldapClient.search(CONTEXT_NAME, query.toString());

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
            logger.error(LDAP_SEARCH_ERROR, e);
            throw new ApplicationException(LDAP_SEARCH_ERROR + e.getMessage(), e);
        }
    }

}
