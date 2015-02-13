package org.jboss.mjolnir.client.domain;

import java.io.Serializable;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class Report<D> implements Serializable {

    private transient D data;
    private String content;
    private String uuid;

    public Report() {
    }

    public Report(String uuid, D data, String content) {
        this.uuid = uuid;
        this.data = data;
        this.content = content;
    }

    public Object getData() {
        return data;
    }

    public String getContent() {
        return content;
    }

    public String getUuid() {
        return uuid;
    }
}
