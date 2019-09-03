FROM openjdk:11-jre

RUN apt-get update

RUN apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common

RUN curl -sL 'https://getenvoy.io/gpg' | apt-key add -

RUN add-apt-repository \
    "deb [arch=amd64] https://dl.bintray.com/tetrate/getenvoy-deb \
    $(lsb_release -cs) \
    stable"

RUN apt-get update &&  apt-get install -y getenvoy-envoy

ADD "target/scala-2.12/cdnize-assembly-0.1.jar" "/app.jar"
COPY envoy/runner.sh /
COPY envoy/service.yaml /

RUN chmod +x "/runner.sh"
VOLUME "/data/base"
VOLUME "/data/cache"

EXPOSE 8080
EXPOSE 80
EXPOSE 8001

CMD /runner.sh app.jar $SERVICE_NAME $CUSTOM_CONFIG