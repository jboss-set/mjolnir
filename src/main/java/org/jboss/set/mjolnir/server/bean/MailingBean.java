package org.jboss.set.mjolnir.server.bean;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tomas Hofman (thofman@redhat.com)
 */
@Singleton
public class MailingBean {

    private static final Logger logger = Logger.getLogger("");

    @Resource(mappedName="java:jboss/mail/Default")
    private Session mailSession;

    public void sendEmail(String sender, String recipient, String subject, String body) {
        try {
            MimeMessage m = new MimeMessage(mailSession);
            Address from = new InternetAddress(sender);
            Address[] to = new InternetAddress[]{new InternetAddress(recipient)};

            m.setFrom(from);
            m.setRecipients(Message.RecipientType.TO, to);
            m.setSubject(subject);
            m.setSentDate(new java.util.Date());
            m.setContent(body, "text/plain");
            Transport.send(m);
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "Couldn't send email.", e);
        }
    }

}
