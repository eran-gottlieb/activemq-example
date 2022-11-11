package maven.amqexample;

import java.text.MessageFormat;
import java.util.Enumeration;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Simple test case that sends and receives messages to/from a JMS broker.
 *
 * @author Ivan Krizsan
 */
@TestInstance(Lifecycle.PER_CLASS)
 public class MyJmsTestIT {
    /* Constant(s): */
    public static final String AMQ_BROKER_URL = "tcp://localhost:61617";
    public static final String QUEUE_NAME = "testQueue";

    /* Instance variable(s): */
    protected ConnectionFactory mActiveMQConnectionFactory;
    protected JmsTemplate mJmsTemplate;

    @BeforeEach
    public void setUp() {
        mActiveMQConnectionFactory = new ActiveMQConnectionFactory(AMQ_BROKER_URL);
        mJmsTemplate = new JmsTemplate(mActiveMQConnectionFactory);
        final Destination theTestDestination = new ActiveMQQueue(QUEUE_NAME);
        mJmsTemplate.setDefaultDestination(theTestDestination);
        mJmsTemplate.setReceiveTimeout(500L);
    }

    //@Disabled
    @ParameterizedTest
    @ValueSource(ints = {1,10,100})
    public void someIntegrationTest(int num) throws Exception {
        
        System.out.println("Test starting...");
        sendMessages(num);
        browseMessages(num);
        receiveMessages(num);
        System.out.println("Test done!");
        
    }

    protected void sendMessages(int num) {
        for (int i = 1; i <= num; i++) {
            final int theMessageIndex = i;
            final String theMessageString = "Message: " + theMessageIndex;
            // System.out.println("Sending message with text: " + theMessageString);

            mJmsTemplate.send(new MessageCreator() {
                public Message createMessage(Session inJmsSession) throws JMSException {
                    TextMessage theTextMessage = inJmsSession.createTextMessage(theMessageString);
                    theTextMessage.setIntProperty("messageNumber", theMessageIndex);

                    return theTextMessage;
                }
            });
        }
    }

    public void browseMessages(int expected) throws JMSException {

        int actual = mJmsTemplate.browse(new BrowserCallback<Integer>() {
            public Integer doInJms(final Session session, final QueueBrowser browser) throws JMSException {
                Enumeration<?> enumeration = browser.getEnumeration();
                int counter = 0;
                while (enumeration.hasMoreElements()) {
                    Message msg = (Message)enumeration.nextElement();
                    // System.out.println(MessageFormat.format("\tFound : {0}", msg));
                    counter += 1;
                }
                return counter;
            }
        });

        if (actual == 0)
            System.out.println("There are no messages");
        else if (actual == 1)
            System.out.println("There is one message");
        else if (actual > 1)    
            System.out.println(MessageFormat.format("There are {0} messages", actual));

        //Assertions.assertEquals(expected, actual);
    }

    protected void receiveMessages(int expected) throws Exception {
        int actual = 0;
        Message theReceivedMessage = mJmsTemplate.receive();

        while (theReceivedMessage != null) {
            if (theReceivedMessage instanceof TextMessage) {
                actual++;
                // final TextMessage theTextMessage = (TextMessage)theReceivedMessage;
                // System.out.println("Received a message with text: " + theTextMessage.getText());
            }

            theReceivedMessage = mJmsTemplate.receive();
        }
        //Assertions.assertEquals(expected, actual);
        System.out.println("All messages received!");
    }
}
