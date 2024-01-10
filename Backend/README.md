# Backend Readme

## Prerequisites

- Java 1.8

## Build:

1. Import Maven project 'SWT' into eclipse or IntelliJ and `mvn install` it

## Run

1. Start elasticsearch via `elasticsearch/bin/elasticsearch` on unix systems. For
   Windows machines it may be required to extract the respective batch files from the
   elasticsearch windows bundle and copy them into `bin`
2. Run the backend as spring boot application (or in Eclipse: `Maven build` with
   `spring-boot:run` as   a goal)
3. Consume the REST interface at `localhost:8083` with the following BasicAuth credentials:
     - username: admin
     - password: admin

## Development Backend
- `docker-compose up mariadb` to launch the database
- wait until the database is running
- `docker-compose up elastic` to launch elasticSearch
- import backend as maven project to run spring boot
- cd to `backend`
- run `SwtApplication.java`

## Import Delicious Library

Provide an array of valid media types via `POST` to the `/api/media/import` endpoint.
The indexation process takes up to 120 seconds.


## Implemented Endpoints

See `MediumController.java`.
