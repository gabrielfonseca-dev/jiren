FROM amazoncorretto:17
EXPOSE 443
ADD /target/jiren-1.0.0.jar jiren.jar
ENTRYPOINT ["java","-jar","jiren.jar"]