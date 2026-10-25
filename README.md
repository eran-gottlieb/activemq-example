# ActiveMQ Example

This project is a Java/Maven example for using Apache ActiveMQ with JMS. It demonstrates how to start an embedded broker, send and consume messages from a queue, and validate behavior through JUnit-based tests.

The code is mainly a learning/demo project and is designed to show common ActiveMQ patterns such as transactional sends, message consumption, queue browsing, and broker configuration.

## What this project demonstrates

- Starting an embedded ActiveMQ broker
- Sending transactional JMS messages
- Consuming messages from a queue
- Browsing queue contents
- Running ActiveMQ tests in a Maven build
- Running ActiveMQ via Docker
- SSL and NIO transport configuration examples

## Project structure

```text
docker/
  Dockerfile                 Docker image for running ActiveMQ

scripts/
  setup-java.sh              Helper script to set JDK 25 and run Maven

src/
  main/
    java/
      maven/amqexample/
        ActiveMQTransactionalExample.java
        QueueSizeChecker.java
  test/
    java/
      maven/amqexample/
        MyJmsTest.java
        MyJmsTestIT.java
    resources/
      activemq.xml
```

## Prerequisites

- JDK 25
- Maven 3.9.1 or newer
- Optional: Docker

This project enforces Java 25 in the Maven build configuration.

## Running the project

Use the helper script:

```bash
./scripts/setup-java.sh
```

This script sets `JAVA_HOME` to JDK 25 and runs:

```bash
mvn -B clean verify
```

You can also run Maven directly:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v25)
export PATH="$JAVA_HOME/bin:$PATH"
mvn clean verify
```

## Main classes

### ActiveMQTransactionalExample
This class starts a local broker and runs producer/consumer threads against a queue named `TEST.QUEUE`. It demonstrates:
- broker startup
- transactional JMS sessions
- sending messages
- receiving messages
- rollback behavior for specific message conditions

### MyJmsTest
This test class configures an embedded ActiveMQ broker from `src/test/resources/activemq.xml`, creates a queue, and verifies fast message sending and receipt using a JMS listener.

### MyJmsTestIT
This integration test uses Spring JMS support (`JmsTemplate`) and parameterized tests to send, browse, and receive messages across several batch sizes.

## Broker configuration

The embedded broker is configured in:

```text
src/test/resources/activemq.xml
```

It defines:
- `nio` transport on port `61617`
- `nio+ssl` transport on port `61619`
- KahaDB persistence directory
- system usage limits

## Docker

The repository includes a Dockerfile that builds an ActiveMQ image based on Amazon Corretto 25 and exposes the default ActiveMQ ports:

- 61616
- 5672
- 61613
- 1883
- 61614
- 8161

Build the image with:

```bash
docker build -t activemq:6.3.2 docker
```

Run it:

```bash
docker run -d -p 61616:61616 -p 8161:8161 activemq:6.3.2
```

## Notes

This repository is primarily a demonstration and test harness rather than a production application. It is useful for understanding ActiveMQ setup, JMS semantics, queue behavior, and broker configuration in a Java environment.

## License

No explicit license file is present in the repository, so license terms are not defined here unless added separately.
