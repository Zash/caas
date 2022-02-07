package im.conversations.compliance.xmpp;

import im.conversations.compliance.persistence.DBOperations;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Server;
import java.time.Duration;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.version.SoftwareVersionManager;
import rocks.xmpp.extensions.version.model.SoftwareVersion;

public class ServerMetadataChecker {
    private static final XmppSessionConfiguration configuration;

    static {
        configuration =
                XmppSessionConfiguration.builder()
                        .defaultResponseTimeout(Duration.ofSeconds(10))
                        .initialPresence(null)
                        .build();
    }

    public static boolean updateServerMetadataFor(XmppClient xmppClient, Credential credential) {
        return updateServer(
                xmppClient, DBOperations.getServer(credential.getDomain()).get(), credential);
    }

    private static boolean updateServer(
            XmppClient xmppClient, Server server, Credential credential) {
        final SoftwareVersionManager softwareVersionManager =
                xmppClient.getManager(SoftwareVersionManager.class);
        try {
            SoftwareVersion softwareVersion =
                    softwareVersionManager.getSoftwareVersion(xmppClient.getDomain()).getResult();
            if (softwareVersion == null) {
                DBOperations.addServer(server);
                return true;
            }
            Server newServer =
                    new Server(
                            credential.getDomain(),
                            softwareVersion.getName(),
                            softwareVersion.getVersion(),
                            server.isListed());
            DBOperations.updateServer(newServer);
            return true;
        } catch (XmppException e) {
            e.printStackTrace();
            // No software version found
        }
        return false;
    }
}
