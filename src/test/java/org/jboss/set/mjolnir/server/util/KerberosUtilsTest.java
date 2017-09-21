package org.jboss.set.mjolnir.server.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
public class KerberosUtilsTest {

    @Test
    public void testNormalizeSimpleName() {
        String normalized = KerberosUtils.normalizeUsername("test");
        Assert.assertEquals("test", normalized);
    }

    @Test
    public void testNormalizeQualifiedName() {
        String normalized = KerberosUtils.normalizeUsername("test@REDHAT.COM");
        Assert.assertEquals("test", normalized);
    }

    @Test
    public void testNormalizeQualifiedName2() {
        String normalized = KerberosUtils.normalizeUsername("test@");
        Assert.assertEquals("test", normalized);
    }

}
