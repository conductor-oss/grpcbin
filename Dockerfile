FROM alpine:3.20

MAINTAINER Orkes Inc <builds@orkes.io>

# DO NOT REMOVE THESE WHILE WE ARE RUNNING ALPINE!
# grpc-java does not officially test alpine and they recommend to simply use a glibc-based image
RUN apk add gcompat
RUN apk add libunwind
ENV LD_PRELOAD=/lib/libgcompat.so.0:/usr/lib/libunwind.so.8


RUN apk add openjdk21
RUN java --version

# Make app folders
RUN mkdir -p /app/config /app/logs /app/libs /app/info

# Add user for conductor server
RUN addgroup -S conductor && adduser -S conductor -G conductor

# Make app folders
RUN mkdir -p /app/config /app/logs /app/libs /app/info
RUN chown -R conductor:conductor /app
RUN chgrp -R 0 /app && chmod -R g=u /app


# Startup script
COPY dockerFiles/startup.sh /app/

# JAR files
COPY build/libs/grpcbin-*server.jar /app/libs/server.jar

RUN chmod +x /app/startup.sh

EXPOSE 50051

USER conductor

CMD ["/app/startup.sh"]
ENTRYPOINT ["/bin/sh"]
