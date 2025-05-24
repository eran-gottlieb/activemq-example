# docker build . -t activemq:6.1.6
# docker run -d -p 61616:61616 -p 8161:8161 activemq:6.1.6
FROM amazoncorretto:21-al2023-headless

ENV ACTIVEMQ_VERSION=6.1.6
ENV ACTIVEMQ=apache-activemq-$ACTIVEMQ_VERSION
ENV ACTIVEMQ_TCP=61616 ACTIVEMQ_AMQP=5672 ACTIVEMQ_STOMP=61613 ACTIVEMQ_MQTT=1883 ACTIVEMQ_WS=61614 ACTIVEMQ_UI=8161
ENV SHA512_VAL=0ec7809387dc83a08d5b6ceb7adad9dc999d302088f882919d155bf228962a80582695abcd6e8f43f3f6e6d4639aa73ed064b1b28b131f6228d516e7422296b0

ENV ACTIVEMQ_HOME=/opt/activemq

RUN curl "https://downloads.apache.org/activemq/$ACTIVEMQ_VERSION/$ACTIVEMQ-bin.tar.gz" -o $ACTIVEMQ-bin.tar.gz

# Validate checksum
RUN if [ "$SHA512_VAL" != "$(sha512sum $ACTIVEMQ-bin.tar.gz | awk '{print($1)}')" ];\
    then \
        echo "sha512 values doesn't match! exiting."  && \
        exit 1; \
    fi;
RUN dnf -y install tar gzip shadow-utils procps
RUN tar xzf $ACTIVEMQ-bin.tar.gz -C  /opt && \
    mv /opt/$ACTIVEMQ $ACTIVEMQ_HOME && \
    useradd -r -M -d $ACTIVEMQ_HOME activemq && \
    chown -R activemq:activemq $ACTIVEMQ_HOME && \
    chown -h activemq:activemq $ACTIVEMQ_HOME 

RUN  sed -i "s|127.0.0.1|0.0.0.0|g" $ACTIVEMQ_HOME/conf/jetty.xml

USER activemq

WORKDIR $ACTIVEMQ_HOME
EXPOSE $ACTIVEMQ_TCP $ACTIVEMQ_AMQP $ACTIVEMQ_STOMP $ACTIVEMQ_MQTT $ACTIVEMQ_WS $ACTIVEMQ_UI

CMD ["/bin/sh", "-c", "bin/activemq console"]
