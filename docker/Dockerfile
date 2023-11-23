# docker build . -t activemq:6.0.0
# docker run -d -p 61616:61616 -p 8161:8161 activemq:6.0.0
FROM amazoncorretto:17-al2023-headless

ENV ACTIVEMQ_VERSION 6.0.0
ENV ACTIVEMQ apache-activemq-$ACTIVEMQ_VERSION
ENV ACTIVEMQ_TCP=61616 ACTIVEMQ_AMQP=5672 ACTIVEMQ_STOMP=61613 ACTIVEMQ_MQTT=1883 ACTIVEMQ_WS=61614 ACTIVEMQ_UI=8161
ENV SHA512_VAL=afb72c27665a0d6cb89e7b2e1d36be0232c2bc1ba0bc0e0c908cc66df7fa41352232bb6dc5b954eb0422580ebf6ed7e3289ea1be0554fdccd751be22636fd7f3

ENV ACTIVEMQ_HOME /opt/activemq

RUN curl "https://archive.apache.org/dist/activemq/$ACTIVEMQ_VERSION/$ACTIVEMQ-bin.tar.gz" -o $ACTIVEMQ-bin.tar.gz

# Validate checksum
RUN if [ "$SHA512_VAL" != "$(sha512sum $ACTIVEMQ-bin.tar.gz | awk '{print($1)}')" ];\
    then \
        echo "sha512 values doesn't match! exiting."  && \
        exit 1; \
    fi;
RUN dnf -y install tar gzip shadow-utils
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
