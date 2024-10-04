# Use maven:3.8.5-openjdk-11-slim as the build image
FROM maven:3.8.5-openjdk-11-slim AS builder

# Create and `cd` into a folder called "app" inside the virtual machine
WORKDIR /app

# Copy everything in the current folder into the "app" folder (src/, WebContent/, etc.)
COPY . .

# Compile the application inside the "app" folder to generate the WAR file
RUN mvn clean package

# Use tomcat:10-jdk11 as the base image
FROM tomcat:10-jdk11

# `cd` into the "app" folder inside the machine
WORKDIR /app

# Copy the WAR file that we have generated earlier into the Tomcat webapps folder inside the container
COPY --from=builder /app/target/cs122b-project1-api-example.war /usr/local/tomcat/webapps/cs122b-project1-api-example.war


# Open the 8080 port of the container, so that outside requests can reach the Tomcat server
EXPOSE 8080

# Start the Tomcat server in the foreground
CMD ["catalina.sh", "run"]

# Side note: The final image would only contain the `tomcat` base image but not the `maven` base image.
# Learn more about Docker multi-stage build at (https://docs.docker.com/build/building/multi-stage/).
