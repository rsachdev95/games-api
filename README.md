# Games API
An API for retrieving data on all games. Authorised developers may also create, update and delete games.

## Requirements
In order to run the API locally, you'll need the following installed on your machine:

- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [MongoDB](https://www.mongodb.com)

## Getting started

1. Ensure that MongoDB is up and running by using the command `sudo mongod`
2. The application can then be started using the command `mvn spring-boot:run`
3. The application will be listening for requests on port `8080`

## Available requests

The following requests can be made via `Postman`:

- Retrieve a list of all games - GET: `localhost:8080/games`
- Fetch a specific game - GET: `localhost:8080/games/{gameId}`
- Note: the following requests will need to consist of a header with key `developer` and value of an authorised developer.
    - Create a new game - POST: `localhost:8080/games`
    - Update a game - PUT: `localhost:8080/games/{gameId}`
    - Delete a game - DELETE: `localhost:8080/games/{gameId}`

## Notes

- The application will not run correctly unless Mongo is up and running
