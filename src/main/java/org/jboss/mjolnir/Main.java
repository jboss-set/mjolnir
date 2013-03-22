package org.jboss.mjolnir;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Main class to be used for this project. In time, we might not need a Main class.
 *
 * @author: navssurtani
 * @since: 0.1
 */
public class Main {

    private static final String BASE_URL = "api.github.com";
    private static final String PROPS_FILE = "users.properties";

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            properties.load(streamFromFile());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        GitHubRequest request = new GitHubRequest();
        request.setUri(BASE_URL);

        GitHubClient client = new GitHubClient();
        GitHubResponse response = null;
        try {
            response = client.get(request);
            Object body = response.getBody();
            System.out.println("Response body is looks like: " + body.toString());

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException("There has been an error in executing the request to Github. " +
                    "Your URL might be wrong. " + BASE_URL);
        }
    }

    private static InputStream streamFromFile() {
        String propsFile = Main.class.getResource(PROPS_FILE).getFile();
        System.out.println("Properties file: " + propsFile);

        return null;
    }

}
