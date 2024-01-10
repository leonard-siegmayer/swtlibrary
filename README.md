# SWT Library

A software for managing a small library

## Local deployment
- `docker-compose build`
- `docker-compose up mariadb`
- `docker-compose up elastic`
- Wait until elasticsearch is running 
- `docker-compose up backend`
- `docker-compose up frontend`
- The system is now accessible at `localhost:80`

## Development Frontend
- cd to `frontend`
- execute command `ng serve --open --port 80`
- the frontend can now be accessed on port 80
- autoreload is activated

## Development Backend
- `docker-compose up mariadb` to launch the database
- wait until the database is running
- `docker-compose up elastic` to launch elasticSearch
- import backend as maven project to run spring boot
- cd to `backend`
- run `SwtApplication.java`

## Notes
- Make sure that sufficient virtual mmap counts are available on the host system. See https://www.elastic.co/guide/en/elasticsearch/reference/current/vm-max-map-count.html
- The first person who logs into the system is granted the `ADMIN` role, afterwards, the role `STUDENT` is granted by default. 
- The directory [delicious_import](delicious_import) contains information on how to import data from the previous system.
