/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.mjolnir.server;

import org.jboss.logging.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author: navssurtani
 * @since: 0.1
 */

public class TokenUtil {

    private static final Logger logger = Logger.getLogger(TokenUtil.class);

    private static final char[] HEX_CHARS = new char[]  { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D','E', 'F' };

    private static final ThreadLocal<MessageDigest> perThreadMd5 = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (final NoSuchAlgorithmException e) {
                if (logger.isEnabled(Logger.Level.WARN)) logger.warn("Could not get instance of MessageDigest");
                return null;
            }
        };
    };

    public static String getToken(final String sessionCookie) {
        if(logger.isInfoEnabled()) logger.info("getToken() called on TokenUtil. Session Cookie is: " + sessionCookie);
        final byte[] cookieBytes = sessionCookie.getBytes();
        return toHexString(getMd5Digest(cookieBytes));

    }

    private static String toHexString(final byte[] digest) {
        final char[] hex = new char[2 * digest.length];
        int i = 0;
        for (final byte b : digest) {
            hex[i++] = HEX_CHARS[(b & 0xF0) >> 4];
            hex[i++] = HEX_CHARS[(b & 0x0F)];
        }
        return new String(hex);
    }

    private static byte[] getMd5Digest(final byte[] cookieBytes) {
        final MessageDigest md5 = perThreadMd5.get();
        md5.reset();
        md5.update(cookieBytes);
        return md5.digest();
    }
}
