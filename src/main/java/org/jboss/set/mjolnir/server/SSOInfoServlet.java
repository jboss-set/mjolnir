package org.jboss.set.mjolnir.server;

import org.jboss.set.mjolnir.shared.domain.RegisteredUser;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.saml.SamlPrincipal;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Shows information about authenticated user.
 */
@WebServlet({"/sso-info"})
public class SSOInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        printInfo(req, resp);
    }

    private void printInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletOutputStream os = resp.getOutputStream();

        RegisteredUser registeredUser = (RegisteredUser) req.getSession(true).getAttribute(AuthenticationFilter.AUTHENTICATED_USER_SESSION_KEY);
        os.println("User: " + (registeredUser != null ? registeredUser.getKrbName() : "[empty]"));
        os.println("Authenticated: " + (registeredUser != null ? registeredUser.isLoggedIn() : "[empty]"));
        os.println("Principal: " +
                (req.getUserPrincipal() != null ? req.getUserPrincipal().getClass().getName() : null));

        try {
            if (req.getUserPrincipal() instanceof SamlPrincipal) {
                SamlPrincipal samlPrincipal = (SamlPrincipal) req.getUserPrincipal();
                os.println("SamlPrincipal:");
                os.println("Name: " + samlPrincipal.getName());
                os.println("NameIDFormat: " + samlPrincipal.getNameIDFormat());
                for (Map.Entry<String, List<String>> entry : samlPrincipal.getAttributes().entrySet()) {
                    for (String value : entry.getValue()) {
                        os.println("attribute " + entry.getKey() + ": " + value);
                    }
                }
            }
        } catch (NoClassDefFoundError e) {
            os.println("NoClassDefFoundError: " + e.getMessage());
        }

        try {
            if (req.getUserPrincipal() instanceof KeycloakPrincipal) {
                @SuppressWarnings("rawtypes")
                KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) req.getUserPrincipal();
                KeycloakSecurityContext securityContext = keycloakPrincipal.getKeycloakSecurityContext();
                os.println("KeycloakSecurityContext:");
                os.println("Preferred Username: " + securityContext.getIdToken().getPreferredUsername());
                os.println("ID: " + securityContext.getIdToken().getId());
                os.println("Name: " + securityContext.getIdToken().getName());
                os.println("Principal Name: " + keycloakPrincipal.getName());
            }
        } catch (NoClassDefFoundError e) {
            os.println("NoClassDefFoundError: " + e.getMessage());
        }
    }
}
