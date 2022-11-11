package maven.amqexample;

import java.net.URI;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

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

/**
 * Simple test case that sends and receives messages to/from a JMS broker.
 *
 * @author Ivan Krizsan
 */
@TestInstance(Lifecycle.PER_CLASS)
 public class MyJmsTest {
    /* Constant(s): */
    public static final String AMQ_BROKER_URL = "tcp://localhost:61616";
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

        String brokerURL = AMQ_BROKER_URL;
        try {
            broker = BrokerFactory.createBroker(new URI("xbean:file:" + RESOURCES_STRING + "activemq.xml"));
            broker.start();
            brokerURL = broker.getTransportConnectorByName("ssl").getPublishableConnectString();
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
    public void simpleTest(int num) throws Exception {        System.out.println("Test starting...");
        System.out.println(count() + " browse messages in queue");
        long t1 = System.currentTimeMillis();
        sendMessages(num);
        if (s.getTransacted()) s.commit();
        System.out.println(count() + " browse messages in queue");
        long t2 = System.currentTimeMillis();
        receiveMessages(num);
        if (s.getTransacted()) s.commit();
        System.out.println(count() + " browse messages in queue");
        long t3 = System.currentTimeMillis();
        //System.out.println(count() + " browse messages in queue");

        String tx = String.format("%d min, %d sec", 
        TimeUnit.MILLISECONDS.toMinutes(t2-t1),
        TimeUnit.MILLISECONDS.toSeconds(t2-t1) - 
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t2-t1)));

        String rx = String.format("%d min, %d sec", 
        TimeUnit.MILLISECONDS.toMinutes(t3-t2),
        TimeUnit.MILLISECONDS.toSeconds(t3-t2) - 
        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(t3-t2)));

        System.out.println("send " + (t2-t1) +", "+ num);
        System.out.println("receive " + (t3-t2) +", "+ num);
        System.out.println("Test done!");
    }

    protected void sendMessages(int num) throws JMSException {
        for (int i = 1; i <= num; i++) {
            final int theMessageIndex = i;
            final String theMessageString = "Message: " + theMessageIndex;
            //System.out.println("Sending message with text: " + theMessageString);
            producer.send(s.createTextMessage(theMessageString));
        }
        System.out.println(num + " messages sent!");
    }
    protected void receiveMessages(int expected) throws Exception {
        int actual = 0;
        Message theReceivedMessage = consumer.receive(1000);
        while (theReceivedMessage != null) {
            if (theReceivedMessage instanceof TextMessage) {                
                actual++;
                //final TextMessage theTextMessage = (TextMessage)theReceivedMessage;
                //System.out.println("Received a message with text: " + theTextMessage.getText());
            }
            if (expected == actual) break;
            theReceivedMessage = consumer.receive(1000);
        }
        Assertions.assertEquals(expected, actual);
        System.out.println(actual + " messages received!");
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
