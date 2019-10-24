# BookingGo Techincal Test Submission

## Setup
```
To download all dependencies and create the executable jar, run the following command:

./mvnw clean install
The above command will create a target folder with the jar file rideways-0.0.1-SNAPSHOT.jar,
and it will also run all the tests.
To run the tests separately, run: ./mvnw test
Note: please do not run the tests while the app is running or when ports 8080 and 8081 are busy.

The above commands can also be run from the IntelliJ maven tool.

Install maven (if not installed, and if preferred in further steps)

JDK 1.8 is a minimum requirement.

The program can be run using the terminal or IntelliJ IDEA 2018.2

In order to run the commands from the terminal, you must be in the folder of the project (rideways) where the ’target’
folder will be located.

If you want to import the project into IntelliJ IDEA, these are the steps as explained on the official website:

1. From the main menu, choose VCS | Checkout from Version Control | Git, or, if no project is currently opened,
choose Checkout from Version Control | Git on the Welcome screen. 
2. In the Clone Repository dialog, specify the URL of the remote repository you want to clone (you can click Test 
to make sure that connection to the remote can be established). 
3. In the Directory field, specify the path where the folder for your local Git repository will be created into which
the remote repository will be cloned. 
4. Click Clone. If you want to create a IntelliJ IDEA project based on the sources you have cloned, click Yes in the
confirmation dialog. Git root mapping will be automatically set to the project root directory.
```

## Part 1

### Console application to print the search results for Dave's Taxis

```
Command structure:
java -jar target/rideways-0.0.1-SNAPSHOT.jar -s {supplier} {pickup} {dropoff}

An example using this command for Dave's Taxis:
java -jar target/rideways-0.0.1-SNAPSHOT.jar -s dave 51.470020,-0.454295 53.470020,-0.454295

NOTE: the program ignores searches in case a request lasts more than 2 seconds, as it was instructed in the
test requirements. The program will inform you whether a timeout occurred, thus, please try again until a
successful result is given.

NOTE: the program logs any error (timeout of 2 seconds, server is unavailable, API is not responding, bad request)
in the terminal before the result is displayed. The errors are handled and appropriate messages are delivered.
```

### Console application to filter by number of passengers

```
For a specific supplier:
java -jar target/rideways-0.0.1-SNAPSHOT.jar -s {supplier} {pickup} {dropoff} {number of passengers}

An example using this command for Dave's Taxis:
java -jar target/rideways-0.0.1-SNAPSHOT.jar -s dave 51.470020,-0.454295 53.470020,-0.454295 6

For all the suppliers (it gives the options filtered by the cheapest supplier):
java -jar target/rideways-0.0.1-SNAPSHOT.jar {pickup} {dropoff} {number of passengers (optional)}

An example using this command:
java -jar target/rideways-0.0.1-SNAPSHOT.jar 51.470020,-0.454295 53.470020,-0.454295 6

Or (in case you want to try without the number of passengers)
java -jar target/rideways-0.0.1-SNAPSHOT.jar 51.470020,-0.454295 53.470020,-0.454295
```

## Part 2

```
Start the API from the terminal:

java -jar target/rideways-0.0.1-SNAPSHOT.jar

OR (if you have maven installed and prefer to use it)

mvn spring-boot:run

When the console says ‘Application is running…’, you can start sending requests using the links described below.
(In order to terminate the application, press ctrl+c)

If you want to start the API from IntelliJ IDEA:

Create a simple run configuration and insert in the ‘Main class’ field the class: com.tech.rideways.Application
Run the configuration.

Sending requests:

Requests can be sent using the link: http://localhost:8080/swagger-ui.html#!
Using the Swagger interface, you can either access /ride to get the cheapest offers from all the supplier APIs,
or /ride/{supplier} to get results from a specific supplier.
Just insert the required parameters and press ‘try it out!’.

Or, you can directly send requests to the link: http://localhost:8080/ride or http://localhost:8080/ride/{supplier}
Example requests:
http://localhost:8080/ride?pickup=51.470020,-0.454295&dropoff=53.470020,-0.454295
http://localhost:8080/ride?pickup=51.470020,-0.454295&dropoff=53.470020,-0.454295&passengers=6
http://localhost:8080/ride/dave?pickup=51.470020,-0.454295&dropoff=53.470020,-0.454295
http://localhost:8080/ride/dave?pickup=51.470020,-0.454295&dropoff=53.470020,-0.454295&passengers=6

NOTE: an empty list returned means that there were no available ride options found, or a problem occurred such as
a timeout or an external server issue (the type of the problem is logged in the terminal). In case there are missing
or invalid parameters, the API will return a BAD_REQUEST code with an adequate message in the JSON format.
```
