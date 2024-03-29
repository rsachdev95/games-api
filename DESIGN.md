## Introduction

The Games API retrieves data about games. Furthermore, games can be created, updated and deleted provided the user performing the action is authorised to do so.

## Assumptions

- A connection to MongoDB is up and running on port `27017`
- The user has access to the AWS instance where a list of all authorised developers can be found
- There are currently no authentication mechanisms in place except for checking the request headers for a `developer` value. This will be enhanced in future iterations.

## Technology Choices

- Language: Java 8
- Build tool: Maven
- Framework: Spring Framework, Spring Boot
- Database: MongoDB
- Libraries:
    - Spring Data MongoDB
    - Spring Boot Web
    - Spring Cloud AWS
    - JUnit5 + Mockito
    
## Configuration

All configurations for the application will be held in the `application.properties` file. The only properties that need to be configured are the following: `cloud.aws.credentials.accessKey` and `loud.aws.credentials.secretKey`

## Data Layer & Architecture

The data will be stored in MongoDB in a collection called `games` in a database also named `games`.

The game resource in the application will have the following data structure:

```json
"_id" : "3d9266d3-5a2b-42dd-9517-f4215ba0203d",
"title" : "Fifa 19",
"genres" : [ 
    "Sports", 
    "Football"
],
"developer" : "EA",
"release_date" : "2019-07-16"
```

On creation of a game resource, the `genres` and `release_date` fields are optional when creating a game. The ID field also does not need to be supplied as this will be auto-generated by the application.

When listing all games, the following structure will be used:

```json
{
    "items_per_page": 5,
    "start_index": 0,
    "total_results": 2,
    "items": [
        {
            "_id" : "3d9266d3-5a2b-42dd-9517-f4215ba0203d",
            "title" : "Fifa 19",
            "genres" : [ 
                "Sports", 
                "Football"
            ],
            "developer" : "EA",
            "release_date" : "2019-07-16"
        },
        {
            "id": "2e6c63c6-fe6c-4f3e-b882-61edb653d06c",
            "title": "Cricket 19",
            "release_date": "2019-07-16",
            "genres": [
                "Sports",
                "Cricket"
            ],
            "developer": "EA"
        }
    ]
}
```
## Application Layer

The application structure consists of the following classes:

##### GameController: 
Class consisting of standard spring controllers which consist of all the endpoint mappings for the application. 

The controller consists of the following requests:
- Retrieve a list of all games - GET: `localhost:8080/games`
- Fetch a specific game - GET: `localhost:8080/games/{gameId}`
- Note: the following requests will need to consist of a header with key `developer` and value of an authorised developer.
     - Create a new game - POST: `localhost:8080/games`
     - Update a game - PUT: `localhost:8080/games/{gameId}`
     - Delete a game - DELETE: `localhost:8080/games/{gameId}

##### GameService: 
This class is injected into the GameController and completes business logic to ensure that data supplied by the client or the application are valid and appropriately processed.

##### AmazonS3Service: 
This class is injected into GameService in order to retrieve the list of authorised developers from the prerequisite file stored in the S3 bucket. The service also unmarshalls the JSON into the model classes for developers, which is in the GameService to compare the developer making a PUT, POST or DELETE request.

##### AWSConfig: 
This class defines a bean which makes use of the AWS credentials in the application.properties file to create an instance of an AWS S3 client.

##### GameRepository: 
Interface that is injected into the GameService in order to create database calls to store and retrieve data from the collections in MongoDB.

##### GameValidator:
Class injected into GameService to validate the game resource in the request body of PUT and POST requests made by the user. As mentioned previously, the `title` and `developer` fields should not be empty or null.

##### Models:
- Game: representation of the object stored in the database as well as the resource presented to the user.
- Games: wrapper object consisting of the list of all games as well as page metadata, namely `items_per_page`, `total_results` and `start_index`.
- Developer: Representation of the developer information stored in S3
- Developers: Wrapper object used to unmarshall developer JSON object

## Future Iterations

##### Transformers: 
Transformers would be introduced in the application in order to accomplish versioning. This would then result in the models having a representation for request bodies sent by the clients as well as a representation for storing the data. The transformer would then be concerned with mapping the different versions of client models to the storage model.

##### HATEOAS Links: 
RESTful resources presented to the client also include relationships between the resources. For the purposes of this application, HATEOAS links were not required as the primary resource, game, was the only resource that would be presented to the client. However, in the future, if more resources such as the inclusion of developer profile information, then a link to the relevant developer will be provided to the client.

##### GlobalExceptionHandler:
This class would be added to handle and prevent any unexpected exceptions being sent to the client.
 