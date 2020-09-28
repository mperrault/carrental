Problem:

Design and implement a car rental system using object-oriented principles.
The system should allow reserving a car of a given type at a desired date and time for a given number of days.
There are 3 types of cars (sedan, SUV and van).
The number of cars of each type is limited.
Use Java as the implementation language. Use unit tests to prove the system satisfies the requirements.

Readme:

This project was built using the following:

- Maven 3.3.9
- Java 1.8

To build the application, issue the following command:

mvn clean install

To run the application:

java -jar ./target/carrental-0.0.1-SNAPSHOT.jar

Note that this web application runs on port 8080 - if you have another application running on this port, it will
fail to start.

Once application is up, point your browser to:
http://localhost:8080

You can view the H2 database by pointing your browser to:
http://localhost:8080/h2-console

To login, leave the default entries and select Connect. You can browse the database schema, and issue SQL commands to view the data.

The tech stack of the application is:

Java 1.8
H2 (in memory database)
Hibernate
SpringBoot
Thymeleaf
JQuery

It was tested against H2, but should be able to run against any relational database (like mysql).
Note that the H2 database was initialized with the following inventory of cars:
3 Vans
4 Sedans
6 SUVs
Additionally, 3 reservations were made for 2021.



