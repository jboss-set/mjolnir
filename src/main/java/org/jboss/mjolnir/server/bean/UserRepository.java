package org.jboss.mjolnir.server.bean;

import org.jboss.mjolnir.authentication.KerberosUser;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Manages registered users.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
public interface UserRepository {

    /**
     * Returns user by his krb name.
     *
     * @param kerberosName krb name
     * @return user instance or null
     */
    KerberosUser getUser(String kerberosName) throws SQLException;

    /**
     * Returns user by his GitHub name.
     *
     * @param gitHubName krb name
     * @return user instance or null
     */
    KerberosUser getUserByGitHubName(String gitHubName) throws SQLException;

    /**
     * Updates existing user.
     *
     * @param kerberosUser user
     */
    void saveUser(KerberosUser kerberosUser) throws SQLException;

    /**
     * Returns user by its krb name. If user doesn't exists, creates new.
     */
    KerberosUser getOrCreateUser(String kerberosName) throws SQLException;

    /**
     * Retrieves all users in database.
     *
     * @return users
     * @throws SQLException
     */
    List<KerberosUser> getAllUsers() throws SQLException;

    /**
     * Removes user from database.
     *
     * @param kerberosName krb name
     * @throws SQLException
     */
    void deleteUser(String kerberosName) throws SQLException;

    /**
     * Removes users from database.
     *
     * @param users users to delete
     * @throws SQLException
     */
    void deleteUsers(Collection<KerberosUser> users) throws SQLException;

}
