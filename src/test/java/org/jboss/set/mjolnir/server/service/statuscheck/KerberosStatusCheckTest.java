package org.jboss.set.mjolnir.server.service.statuscheck;

import org.jboss.set.mjolnir.server.bean.ApplicationParameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class KerberosStatusCheckTest {

    @Mock
    private ApplicationParameters applicationParameters;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testKdcDoesntExist() {
        Mockito.when(applicationParameters.getParameter(ApplicationParameters.KRB5_KDC_KEY))
                .thenReturn("non.existing.host");
        Mockito.when(applicationParameters.getMandatoryParameter(ApplicationParameters.KRB5_KDC_KEY))
                .thenReturn("non.existing.host");

        KerberosStatusCheck check = new KerberosStatusCheck();
        check.setApplicationParameters(applicationParameters);
        StatusCheckResult result = check.checkStatus();
        Assert.assertFalse(result.isSuccess());
    }
}
