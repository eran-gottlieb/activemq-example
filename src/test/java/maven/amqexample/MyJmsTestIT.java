package maven.amqexample;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;
import java.text.MessageFormat;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Simple test case that sends and receives messages to/from a JMS broker.
 *
 * @author Ivan Krizsan
 */
@TestInstance(Lifecycle.PER_CLASS)
 class MyJmsTestIT {
    /* Constant(s): */
    static final String AMQ_BROKER_URL = "auto://0.0.0.0:61617";
    static final String QUEUE_NAME = "testQueue";

    /* Instance variable(s): */
    protected ConnectionFactory mActiveMQConnectionFactory;
    protected JmsTemplate mJmsTemplate;

    @BeforeEach
    void setUp() {
        mActiveMQConnectionFactory = new ActiveMQConnectionFactory(AMQ_BROKER_URL);
        mJmsTemplate = new JmsTemplate(mActiveMQConnectionFactory);
        final Destination theTestDestination = new ActiveMQQueue(QUEUE_NAME);
        mJmsTemplate.setDefaultDestination(theTestDestination);
        mJmsTemplate.setReceiveTimeout(500L);
    }

    //@Disabled
    @ParameterizedTest
    @ValueSource(ints = {1,10,100})
    void someIntegrationTest(int num) throws JMSException {
        System.out.println("Test starting...");
        sendMessages(num);
        browseMessages(num);
        receiveMessages(num);
        System.out.println("Test done!");
    }
    /**
     * 
     * @param num number of sent messages.
     */
    void sendMessages(int num) {
        for (int i = 1; i <= num; i++) {
            final int theMessageIndex = i;
            final String theMessageString = "Message: " + theMessageIndex;
            // System.out.println("Sending message with text: " + theMessageString);

            mJmsTemplate.send(inJmsSession -> {
                TextMessage theTextMessage = inJmsSession.createTextMessage(theMessageString);
                theTextMessage.setIntProperty("messageNumber", theMessageIndex);

                return theTextMessage;
            });
        }
    }

    /**
     * 
     * @param expected number of messages in queue
     */
    void browseMessages(int expected) {
        Integer actual = mJmsTemplate.browse((session, browser) -> {
            Enumeration<?> enumeration = browser.getEnumeration();
            int counter = 0;
            while (enumeration.hasMoreElements()) {
                Message msg = (Message) enumeration.nextElement() ;
                System.out.println(MessageFormat.format("\tFound : {0}", msg));
                counter += 1;
            }
            return counter;
        });

        if (actual == null) {
            throw new NullPointerException();
        }

        if (actual == 0)
            System.out.println("There are no messages");
        else if (actual == 1)
            System.out.println("There is one message");
        else if (actual > 1)    
            System.out.println(MessageFormat.format("There are {0} messages", actual));

        assertEquals(expected, actual);
    }

    /**
     * 
     * @param expected num of messages to receive
     */
    void receiveMessages(int expected) throws JMSException {
        int actual = 0;
        Message theReceivedMessage = mJmsTemplate.receive();

        while (theReceivedMessage != null) {
            if (theReceivedMessage instanceof TextMessage) {
                actual++;
                final TextMessage theTextMessage = (TextMessage)theReceivedMessage;
                System.out.println("Received a message with text: " + theTextMessage.getText());
            }

            theReceivedMessage = mJmsTemplate.receive();
        }
        assertEquals(expected, actual);
        System.out.println("All messages received!");
    }
}
