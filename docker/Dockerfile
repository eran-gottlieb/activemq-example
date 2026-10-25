# docker build . -t activemq:6.3.2
# docker run -d -p 61616:61616 -p 8161:8161 activemq:6.3.2
FROM amazoncorretto:25-al2023-headless

ENV ACTIVEMQ_VERSION=6.3.2
ENV ACTIVEMQ=apache-activemq-$ACTIVEMQ_VERSION
ENV ACTIVEMQ_TCP=61616 ACTIVEMQ_AMQP=5672 ACTIVEMQ_STOMP=61613 ACTIVEMQ_MQTT=1883 ACTIVEMQ_WS=61614 ACTIVEMQ_UI=8161
ENV SHA512_VAL=30df66a98916fd448db6f29d591b24dd75584f58bafbe059377df923a65190a9dabfa036503bd35bc1469546360704e6942c5f922322e2b89e261bd45cad128a

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

RUN printf '\njetty.http.host=0.0.0.0\n' >> $ACTIVEMQ_HOME/conf/jetty-spring.properties && \
    sed -i 's|<Item>127.0.0.1</Item>|<Item>0.0.0.0/0</Item>|; s|<Item>::1</Item>|<Item>::/0</Item>|' \
        $ACTIVEMQ_HOME/conf/jetty/jetty-security.xml

USER activemq

WORKDIR $ACTIVEMQ_HOME
EXPOSE $ACTIVEMQ_TCP $ACTIVEMQ_AMQP $ACTIVEMQ_STOMP $ACTIVEMQ_MQTT $ACTIVEMQ_WS $ACTIVEMQ_UI

CMD ["/bin/sh", "-c", "bin/activemq console"]
