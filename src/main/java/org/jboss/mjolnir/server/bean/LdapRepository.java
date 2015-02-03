package org.jboss.mjolnir.server.bean;

/**
 * Allows executing LDAP queries.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface LdapRepository {

    /**
     * Checks that user has record in directory server.
     *
     * @param uid user id
     * @return record exists?
     */
    boolean checkUserRecord(String uid);

}
