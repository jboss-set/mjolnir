package org.jboss.set.mjolnir.shared.domain;

import java.io.Serializable;
import java.util.List;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class Report<D> implements Serializable {


    private static final long serialVersionUID = -6959204799863942608L;
    private transient D data;
    private String content;
    private String uuid;
    private List<String> actions;

    public Report() {
    }

    public Report(String uuid, D data, String content, List<String> actions) {
        this.uuid = uuid;
        this.data = data;
        this.content = content;
        this.actions = actions;
    }

    public D getData() {
        return data;
    }

    public String getContent() {
        return content;
    }

    public String getUuid() {
        return uuid;
    }

    public List<String> getActions() {
        return actions;
    }

}
