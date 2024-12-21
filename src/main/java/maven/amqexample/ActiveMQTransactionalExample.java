package maven.amqexample;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class ActiveMQTransactionalExample {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String DESTINATION = "TEST.QUEUE";
    private static final int NUM_OF_PRODUCERS = 1;
    private static final int NUM_OF_CONSUMERS = 1;
    private static final int MAX_MESSAGES = 2000;

    public static void main(String[] args) throws InterruptedException {
        //comment to use standalone broker.
       startBroker();

        int count = NUM_OF_CONSUMERS;
        CountDownLatch consumersLatch = new CountDownLatch(count);

        while (--count >= 0) {
            // Start the consumer thread
            Thread consumerThread = new Thread(() -> {
                try {
                    consumeMessages();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    consumersLatch.countDown(); // Notify that the consumer has finished
                }
            }, "ActiveMQ Consumer task - " + count);
            consumerThread.start();
        }

        count = NUM_OF_PRODUCERS;
        CountDownLatch producersLatch = new CountDownLatch(count);

        // Give the consumer a moment to start up
        Thread.sleep(1000);

        while (--count >= 0) {
            // Start the producer thread
            Thread producerThread = new Thread(() -> {
                try {
                    producersLatch.countDown(); // Notify that the producer about to start
                    produceMessages();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }, "ActiveMQ Producer task - " + count);
            producerThread.start();
        }

        producersLatch.await();
        System.out.println("Wait for consumers to finish");
        // Wait for the consumer to finish
        consumersLatch.await();

        System.out.println("Done");
    }

    private static void startBroker() {
        BrokerService broker = new BrokerService();
        // configure the broker
        try {
            broker.setDataDirectory("ActiveMQData");
            broker.getPersistenceAdapter().setDirectory(new File("ActiveMQData/KahaDB"));
            broker.addConnector(BROKER_URL);
            broker.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void produceMessages() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        // https://activemq.apache.org/components/classic/documentation/redelivery-policy
        System.out.println(connectionFactory.getRedeliveryPolicy());
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

            for (int i = 0; i < (MAX_MESSAGES / NUM_OF_PRODUCERS); i++) {
                // Create and send a message
                TextMessage message = session.createTextMessage("Hello from producer! " + (i + 1));
                message.setJMSCorrelationID("message" + "_" + (i+1));
                producer.send(message);
                sentCount++;
                if (sentCount % 1 == 0) {
                    // System.out.println("Sent message: " + message.getText() + " " + sentCount);
                    session.commit();
                }

            }

            // Commit the transaction
            session.commit();
            System.out.println("Message sent and transaction committed.");

        } finally {
            if (connection != null)
                connection.close();
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
                    if (textMessage.getJMSCorrelationID().equals("message_100")) {
                        System.out.println("doRollback");
                        session.rollback();
                    } else if (receiveCount % 1 == 0) {
                        // System.out.println("Received message: " + textMessage.getText() + " " +
                        // receiveCount);
                        session.commit();
                    }
                    // System.out.println("Received message: " + textMessage.getText());
                } else {
                    System.out.println("Received non-text message. " + receiveCount);
                    session.commit();
                    pendingMessages = 0;//QueueSizeChecker.getPendingMessages(BROKER_URL, DESTINATION);
                }

            }

            // Commit the transaction
            session.commit();
            System.out.println("Message received and transaction committed.");

        } finally {
            if (connection != null)
                connection.close();
        }
    }
}