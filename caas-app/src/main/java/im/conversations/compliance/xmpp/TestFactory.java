package im.conversations.compliance.xmpp;

import im.conversations.compliance.xmpp.tests.AbstractTest;
import rocks.xmpp.core.session.XmppClient;

public class TestFactory {

    public static AbstractTest create(Class<? extends AbstractTest> clazz, XmppClient client)
            throws TestCreationException {
        if (client == null) {
            throw new TestCreationException();
        }
        try {
            return clazz.getDeclaredConstructor(XmppClient.class).newInstance(client);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TestCreationException();
        }
    }

    public static class TestCreationException extends Exception {}
}
