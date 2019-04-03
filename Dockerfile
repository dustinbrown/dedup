FROM maven:alpine
WORKDIR /usr/src/app

COPY leads* /usr/src/app/
COPY pom.xml /usr/src/app/
COPY src/ /usr/src/app/src/

RUN mvn install

ENV FILE leads.json
CMD ["sh", "-c", "mvn --batch-mode -q spring-boot:run -Dspring-boot.run.arguments=${FILE}"]

