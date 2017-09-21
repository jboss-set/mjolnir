package org.jboss.set.mjolnir.server.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Martin Stefanko (mstefank@redhat.com)
 */
@Entity
@Table(name="application_parameters")
public class ApplicationParameterEntity {

    @Id
    @Column(name="param_name")
    private String paramName;

    @Column(name="param_value")
    private String paramValue;

    public ApplicationParameterEntity() {
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
