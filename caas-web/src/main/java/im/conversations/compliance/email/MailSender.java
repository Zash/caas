package im.conversations.compliance.email;

import im.conversations.compliance.pojo.Configuration;
import im.conversations.compliance.pojo.MailConfig;
import java.util.List;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailSender.class);
    private static boolean init = false;
    private static MailConfig mailConfig;
    private static TransportStrategy transportStrategy;
    private static Mailer mailer;

    public static void init() {
        if (!init) {
            mailConfig = Configuration.getInstance().getMailConfig();
            transportStrategy =
                    mailConfig.getSSL() ? TransportStrategy.SMTP_TLS : TransportStrategy.SMTP;
            mailer =
                    MailerBuilder.withSMTPServer(
                                    mailConfig.getHost(),
                                    mailConfig.getPort(),
                                    mailConfig.getUsername(),
                                    mailConfig.getPassword())
                            .withTransportStrategy(transportStrategy)
                            .buildMailer();
        }
        init = true;
    }

    public static void sendMail(Email email) {
        init();
        if (mailer.validate(email)) {
            mailer.sendMail(email);
        } else {
            LOGGER.error("Invalid email");
        }
    }

    public static void sendMails(List<Email> emails) {
        for (Email email : emails) {
            sendMail(email);
        }
    }
}
