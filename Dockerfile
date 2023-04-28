FROM openjdk:11-jdk

RUN apt-get update

RUN apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common


ADD "target/scala-2.13/cdnize-assembly-0.1.jar" "/app.jar"

VOLUME "/data/base"
VOLUME "/data/cache"

EXPOSE 8080
EXPOSE 80
EXPOSE 8001

CMD java -jar /app.jar

