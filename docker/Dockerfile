# docker build -f docker/Dockerfile -t activemq:6.4.0 .
# docker run -d -p 61616:61616 -p 8161:8161 activemq:6.4.0
FROM amazoncorretto:25-al2023-headless

ENV ACTIVEMQ_VERSION=6.4.0
ENV ACTIVEMQ=apache-activemq-$ACTIVEMQ_VERSION
ENV ACTIVEMQ_TCP=61616 ACTIVEMQ_AMQP=5672 ACTIVEMQ_STOMP=61613 ACTIVEMQ_MQTT=1883 ACTIVEMQ_WS=61614 ACTIVEMQ_UI=8161
ENV SHA512_VAL=d75a04fcfe2a442a4450625c6691d8035aebd5372ff870475dd2d01cd137cab98b5ff68d9292e9f1388145aeb89b17d90506c19321d3978db3b93e837d64ecd3

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
    mkdir -p $ACTIVEMQ_HOME/tmp && \
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
