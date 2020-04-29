ARG RESIN_VERSION=4.0.62
ARG RESIN_SRC=resin-${RESIN_VERSION}-src.tar.gz
ARG OPENJDK_IMAGE=openjdk:8u191-jdk-alpine
ARG OPENJDK_BUILD_IMAGE=${OPENJDK_IMAGE}

# build image
FROM ${OPENJDK_BUILD_IMAGE} AS build-image

ARG RESIN_VERSION
ARG RESIN_SRC

RUN apk add gcc make musl-dev linux-headers

# build Resin
RUN mkdir /tmp/resin-build
COPY files/resin-sources/${RESIN_SRC} /tmp/resin-build/
RUN tar xzvf /tmp/resin-build/${RESIN_SRC} -C /tmp/resin-build/
WORKDIR /tmp/resin-build/resin-${RESIN_VERSION}

RUN chmod 755 ./configure && \
    ./configure --enable-64bit \
                --prefix=/opt/resin \
                    --with-resin-root=/var/resin \
                --with-resin-log=/var/log/resin \
                --with-resin-conf=/etc/resin && \
    make && \
    make install
RUN echo ${RESIN_VERSION} > /opt/resin/VERSION.txt


# runtime image
FROM ${OPENJDK_IMAGE}

COPY --from=build-image /opt/resin/ /opt/resin/

RUN mkdir -p /var/resin/webapps \
             /var/log/resin \
             /var/log/polijlogs \
             /etc/resin \
             /etc/polij.d/_empty

COPY files/resin-conf/* /etc/resin/
COPY files/webapps-jars/ /opt/resin/webapps-jars/

COPY files/docker_entrypoint.sh /
RUN chmod 755 /docker_entrypoint.sh

ENV PATH=${PATH}:/opt/resin/bin

EXPOSE 80
EXPOSE 443

ENTRYPOINT ["/docker_entrypoint.sh"]
CMD ["resinctl", "console"]
