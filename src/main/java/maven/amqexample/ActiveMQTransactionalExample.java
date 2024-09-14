package maven.amqexample;

import org.apache.activemq.ActiveMQConnectionFactory;

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
import jakarta.jms.Destination;

import java.util.concurrent.CountDownLatch;

public class ActiveMQTransactionalExample {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String DESTINATION = "TEST.QUEUE"; 
    
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // Start the consumer thread
        Thread consumerThread = new Thread(() -> {
            try {
                consumeMessages();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown(); // Notify that the consumer has finished
            }
        });
        consumerThread.start();

        // Give the consumer a moment to start up
        Thread.sleep(1000);

        // Start the producer
        try {
            produceMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wait for the consumer to finish
        latch.await();
    }

    private static void produceMessages() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Destination destination = session.createQueue(DESTINATION);
            producer = session.createProducer(destination);
            int sentCount = 0;

            for (int i=0;i<(.2*1000000); i++) {
                // Create and send a message
                TextMessage message = session.createTextMessage("Hello from producer! " + (i+1));
                producer.send(message);
                sentCount++;
                if (sentCount % 1000 == 0) {
                    //System.out.println("Sent message: " + message.getText() + " " + sentCount);
                    session.commit();
                }

            }

            // Commit the transaction
            session.commit();
            System.out.println("Message sent and transaction committed.");

        } finally {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }

    private static void consumeMessages() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Destination destination = session.createQueue(DESTINATION);
            consumer = session.createConsumer(destination);
            int receiveCount = 0;
            int pendingMessages = 1;

            while (pendingMessages > 0) {
                // Receive a message
                Message receivedMessage = consumer.receive(10000); // Wait up to 10 seconds

                if (receivedMessage instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) receivedMessage;
                    receiveCount++;
                    if (receiveCount % 1000 == 0) {
                        //System.out.println("Received message: " + textMessage.getText() + " " + receiveCount);
                        session.commit();
                    }
                    //System.out.println("Received message: " + textMessage.getText());
                } else {
                    System.out.println("Received non-text message. " + receiveCount);
                    session.commit();
                    pendingMessages =  QueueSizeChecker.getPendingMessages(BROKER_URL, DESTINATION);
                }

            }

            // Commit the transaction
            session.commit();
            System.out.println("Message received and transaction committed.");

        } finally {
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }
}