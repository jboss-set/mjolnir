package org.jboss.set.mjolnir.server.bean;

import org.jboss.set.mjolnir.shared.domain.RegisteredUser;

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
    RegisteredUser getUser(String kerberosName);

    /**
     * Returns user by his GitHub name.
     *
     * @param gitHubName krb name
     * @return user instance or null
     */
    RegisteredUser getUserByGitHubName(String gitHubName);

    /**
     * Saves new user.
     *
     * @param registeredUser user
     */
    void saveUser(RegisteredUser registeredUser);

    /**
     * Save or updates existing user.
     *
     * @param registeredUser user
     */
    void saveOrUpdateUser(RegisteredUser registeredUser);

    /**
     * Returns user by its krb name. If user doesn't exists, creates new.
     */
    RegisteredUser getOrCreateUser(String kerberosName);

    /**
     * Retrieves all users in database.
     *
     * @return users
     */
    List<RegisteredUser> getAllUsers();

    /**
     * Removes user from database.
     *
     * @param user user to delete
     */
    void deleteUser(RegisteredUser user);

    /**
     * Removes users from database.
     *
     * @param users users to delete
     */
    void deleteUsers(Collection<RegisteredUser> users);

}
