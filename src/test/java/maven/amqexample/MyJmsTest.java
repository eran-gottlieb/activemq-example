package maven.amqexample;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

/**
 * Simple test case that sends and receives messages to/from a JMS broker.
 *
 * @author Ivan Krizsan
 */
@TestInstance(Lifecycle.PER_CLASS)
 public class MyJmsTest {
    /* Constant(s): */
    public static final String QUEUE_NAME = "testQueue";
    public static final String RESOURCES_STRING = "./src/test/resources/";

    /* Instance variable(s): */
    protected ConnectionFactory activeMQConnectionFactory;
    protected BrokerService broker;
    private Connection c;
    private Session s;
    private Queue q;
    private MessageConsumer consumer;
    private MessageProducer producer;
    private QueueBrowser browser;

    @BeforeAll
    public void setUp() {
 
        System.setProperty("javax.net.ssl.keyStore",RESOURCES_STRING + "broker.ks");
        System.setProperty("javax.net.ssl.keyStorePassword","password");
        System.setProperty("javax.net.ssl.trustStore",RESOURCES_STRING + "broker.ts");
        System.setProperty("javax.net.ssl.trustStorePassword","password");

        //System.getProperties().list(System.out);
        System.getProperties().entrySet().forEach(System.out::println);

        String brokerURL;
        try {
            broker = BrokerFactory.createBroker(new URI("xbean:file:" + RESOURCES_STRING + "activemq.xml"));
            broker.start();
            brokerURL = broker.getTransportConnectorByName("nio+ssl").getPublishableConnectString();
            activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
            c = activeMQConnectionFactory.createConnection();
            c.start();
            s = c.createSession(true, Session.SESSION_TRANSACTED);
            //s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            q = s.createQueue(QUEUE_NAME);
            producer = s.createProducer(q);
            consumer = s.createConsumer(q);
            browser  = s.createBrowser(q);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int count() throws JMSException {
        browser  = s.createBrowser(q);
        int leftOver = 0;
        Enumeration<?> e = browser.getEnumeration();
        while( e.hasMoreElements() ) {
            e.nextElement();
            leftOver++;
        }
        browser.close();
        return leftOver;
    }

    @ParameterizedTest
    @ValueSource(ints = {1000})
    void simpleTest(int num) throws Exception {        
        System.out.println("Test starting...");
        System.out.println(count() + " browse messages in queue");
        long t1 = System.currentTimeMillis();
        sendFastMessages(num);
        if (s.getTransacted()) s.commit();
        System.out.println(count() + " browse messages in queue");
        long t2 = System.currentTimeMillis();
        receiveMessages(num);
        if (s.getTransacted()) s.commit();
        System.out.println(count() + " browse messages in queue");
        long t3 = System.currentTimeMillis();

        System.out.println("send " + (t2-t1) +", "+ num) ;
        System.out.println("receive " + (t3-t2) +", "+ num);
        System.out.println("Test done!");
    }

    protected void sendFastMessages(int num) throws JMSException {
        final int batchSize = 100; // Adjust this value based on your needs
        List<TextMessage> batch = new ArrayList<>(batchSize);

        for (int i = 1; i <= num; i++) {
            final String theMessageString = "Message: " + i;
            TextMessage message = s.createTextMessage(theMessageString);
            batch.add(message);

            if (i % batchSize == 0 || i == num) {
                // Send all messages in the batch
                for (TextMessage msg : batch) {
                    producer.send(msg);
                }
                // If the session is transacted, commit it to ensure that all messages in the batch are sent
                if (s.getTransacted()) {
                    s.commit();
                }
                // Clear the batch
                batch.clear();
            }
        }
        System.out.println(num + " messages sent!");
    }

    protected void sendSlowMessages(int num) throws JMSException {
        for (int i = 1; i <= num; i++) {
            final int theMessageIndex = i;
            final String theMessageString = "Message: " + theMessageIndex;
            //System.out.println("Sending message with text: " + theMessageString);
            producer.send(s.createTextMessage(theMessageString));
        }
        System.out.println(num + " messages sent!");
    }

    protected void receiveMessages(int expected) throws Exception {
        AtomicInteger actual = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(expected);

        MessageListener listener = new MessageListener() {
            public void onMessage(Message message) {
                if (message instanceof TextMessage) {
                    actual.incrementAndGet();
                    latch.countDown();
                }
            }
        };

        consumer.setMessageListener(listener);

        // Wait for all messages to be received
        latch.await();

        Assertions.assertEquals(expected, actual.get());
        System.out.println(actual.get() + " messages received!");

        // Don't forget to remove the listener when you're done
        consumer.setMessageListener(null);
    } 

    @AfterAll
    protected void shutdown() {
        try {
            consumer.close();
            s.close();
            c.close();
            broker.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
