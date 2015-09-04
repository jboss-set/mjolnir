package org.jboss.mjolnir.client.component.util;

import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class HTMLUtil {

    public static String toUl(List<String> items) {
        StringBuilder sb = new StringBuilder("<ul>");
        for (String item: items) {
            sb.append("<li>").append(item).append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }
}
