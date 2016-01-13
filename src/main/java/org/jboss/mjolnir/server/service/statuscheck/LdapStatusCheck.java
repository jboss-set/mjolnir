package org.jboss.mjolnir.server.service.statuscheck;

import javax.ejb.EJB;
import javax.ejb.Singleton;

import org.jboss.mjolnir.server.bean.ApplicationParameters;
import org.jboss.mjolnir.server.bean.LdapRepository;

/**
 * Checks that LDAP server is configured and reachable.
 *
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
public class LdapStatusCheck extends AbstractStatusCheck {

    private static final String TITLE = "LDAP";

    @EJB
    private LdapRepository ldapRepository;

    @EJB
    private ApplicationParameters applicationParameters;

    public LdapStatusCheck() {
        super(TITLE);
    }

    @Override
    protected StatusCheckResult doCheckStatus() throws Exception {
        StatusCheckResult result = new StatusCheckResult();

        String ldapUrl = applicationParameters.getParameter(ApplicationParameters.LDAP_URL_KEY);
        if (ldapUrl == null) {
            result.addProblem("LDAP URL is not configured.");
        }

        ldapRepository.checkUserExists("testuser");
        return result;
    }
}
