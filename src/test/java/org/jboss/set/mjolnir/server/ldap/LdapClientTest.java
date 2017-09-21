package org.jboss.set.mjolnir.server.ldap;

import org.junit.Ignore;
import org.junit.Test;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Ignore // this is only for development purposes
public class LdapClientTest {

    private final static String CONTEXT_NAME = "ou=users,dc=redhat,dc=com";
    private final static String LDAP_URL = "ldap://ldap.nrt.redhat.com";


    @Test
    public void test() throws NamingException {
        LdapClient ldapClient = new LdapClient(LDAP_URL);
        NamingEnumeration<SearchResult> result = ldapClient.search(CONTEXT_NAME, "uid=thofman@REDHAT.COM");
        while (result.hasMore()) {
            System.out.println(result.next().toString());
        }
    }
}
