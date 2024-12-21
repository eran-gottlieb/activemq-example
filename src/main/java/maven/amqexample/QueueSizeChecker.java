package maven.amqexample;

import java.util.Enumeration;

import org.apache.activemq.ActiveMQConnectionFactory;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;

public class QueueSizeChecker {

        // ActiveMQ connection URL
//        String brokerURL = "tcp://localhost:61616"; // Replace with your broker URL
//        String queueName = "yourQueueName"; // Replace with your queue name
    public static int getPendingMessages(String brokerURL, String queueName) {

        // Create a connection factory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);

        try {
            // Create a connection
            Connection connection = connectionFactory.createConnection();
            connection.start();

            // Create a session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create a queue
            Queue queue = session.createQueue(queueName);

            // Create a queue browser
            QueueBrowser browser = session.createBrowser(queue);

            // Count the number of messages
            Enumeration<?> messages = browser.getEnumeration();
            int messageCount = 0;
            while (messages.hasMoreElements()) {
                messages.nextElement();
                messageCount++;
            }

            // Output the number of pending messages
            System.out.println("Pending messages in queue '" + queueName + "': " + messageCount);

            // Clean up
            browser.close();
            session.close();
            connection.close();

            return messageCount;

        } catch (JMSException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
