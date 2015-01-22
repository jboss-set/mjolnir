package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.KerberosUser;

import java.sql.SQLException;

/**
 * Manages registered users.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface UserRepository {

    /**
     * Returns user by its krb name.
     *
     * @param kerberosName krb name
     * @return user instance or null
     */
    KerberosUser getUser(String kerberosName) throws SQLException;

    /**
     * Updates existing user.
     *
     * @param kerberosUser user
     */
    void updateUser(KerberosUser kerberosUser) throws SQLException;

    /**
     * Returns user by its krb name. If user doesn't exists, creates new.
     */
    KerberosUser getOrCreateUser(String kerberosName) throws SQLException;

}
