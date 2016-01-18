#
# Dockerfile for the Timadorus AuthServer
#
#

FROM java:8-jre

WORKDIR /as-code/

ADD TimadorusAuthServer-2.4/auth-server.jar ./
ADD TimadorusAuthServer-2.4/lib ./lib


VOLUME ["/as-data"]

CMD ["java", "-jar", "/as-code/auth-server.jar","/as-data"]


