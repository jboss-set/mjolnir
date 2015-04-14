package org.jboss.mjolnir.server.bean;

import java.util.Map;
import java.util.Set;

/**
 * Allows executing LDAP queries.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface LdapRepository {

    /**
     * Checks if a user has record in directory server.
     *
     * @param uid user id
     * @return record exists?
     */
    boolean checkUserExists(String uid);

    /**
     * Checks which users has records in directory server.
     *
     * @param users list of user uids
     * @return map with uid as key and boolean indicating indicating existing record as value
     */
    Map<String, Boolean> checkUsersExists(Set<String> users);
}
