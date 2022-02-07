package im.conversations.compliance.xmpp;

import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.Credential;
import im.conversations.compliance.pojo.Result;
import im.conversations.compliance.xmpp.extensions.extservices.Services;
import im.conversations.compliance.xmpp.extensions.omemo.Bundle;
import im.conversations.compliance.xmpp.extensions.upload.Get;
import im.conversations.compliance.xmpp.extensions.upload.Put;
import im.conversations.compliance.xmpp.extensions.upload.Request;
import im.conversations.compliance.xmpp.extensions.upload.Slot;
import im.conversations.compliance.xmpp.tests.AbstractTest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.Extension;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;

public class TestExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutor.class);

    private static final XmppSessionConfiguration configuration;

    static {
        configuration =
                XmppSessionConfiguration.builder()
                        .extensions(
                                Extension.of(
                                        Bundle.class,
                                        Request.class,
                                        Slot.class,
                                        Get.class,
                                        Put.class,
                                        Services.class))
                        .defaultResponseTimeout(Duration.ofSeconds(10))
                        // .debugger(ConsoleDebugger.class)
                        .initialPresence(null)
                        .build();
    }

    public static List<Result> executeTestsFor(Credential credential, Hook... hooks)
            throws XmppException, TestFactory.TestCreationException {

        final ArrayList<Result> results = new ArrayList<>();
        XmppClient client = XmppClient.create(credential.getDomain(), configuration);
        client.getManager(EntityCapabilitiesManager.class).setEnabled(false);
        client.connect(credential.getJid());
        client.login(credential.getJid().getLocal(), credential.getPassword(), "caas");

        for (Hook hook : hooks) {
            hook.run(client);
        }

        // Run tests
        List<Class<? extends AbstractTest>> testClasses = Tests.getTests();
        for (Class<? extends AbstractTest> testClass : testClasses) {
            ComplianceTest test = testClass.getAnnotation(ComplianceTest.class);
            LOGGER.debug("running " + testClass.getName());
            Result result = new Result(test, TestFactory.create(testClass, client).run());
            results.add(result);
        }
        client.close();
        return results;
    }

    public interface Hook {
        void run(XmppClient client);
    }
}
