FROM ubuntu:16.04

MAINTAINER llepak

RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install software-properties-common
RUN add-apt-repository ppa:openjdk-r/ppa
RUN add-apt-repository ppa:cwchien/gradle

RUN apt-get update \
 && apt-get -y install git ant openjdk-8-jdk gradle unzip mc nano \
    wget curl  docker.io jq \
 && apt-get clean

RUN update-java-alternatives -s java-1.8.0-openjdk-amd64

EXPOSE 8080

ENV TERM xterm

ADD pik.war /pik.war
CMD java -jar /pik.war
