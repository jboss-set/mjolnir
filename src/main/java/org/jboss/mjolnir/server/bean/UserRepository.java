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
    KerberosUser getUser(String kerberosName);

    /**
     * Returns user by his GitHub name.
     *
     * @param gitHubName krb name
     * @return user instance or null
     */
    KerberosUser getUserByGitHubName(String gitHubName);

    /**
     * Updates existing user.
     *
     * @param kerberosUser user
     */
    void saveUser(KerberosUser kerberosUser);

    /**
     * Returns user by its krb name. If user doesn't exists, creates new.
     */
    KerberosUser getOrCreateUser(String kerberosName);

    /**
     * Retrieves all users in database.
     *
     * @return users
     * @throws SQLException
     */
    List<KerberosUser> getAllUsers();

    /**
     * Removes user from database.
     *
     * @param user user to delete
     * @throws SQLException
     */
    void deleteUser(KerberosUser user);

    /**
     * Removes users from database.
     *
     * @param users users to delete
     * @throws SQLException
     */
    void deleteUsers(Collection<KerberosUser> users);

}
