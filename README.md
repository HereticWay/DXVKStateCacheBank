# About
This project aims to solve the problem of storing and automatically
merging dxvk pipeline state cache files to a single "incremental" cache file
per game that can be distributed later across multiple devices.

The idea is that users can register to the api and contribute their latest
state cache files for their game and my backend would store it and automatically
merge it with the latest "incremental" cache.

## Endpoints
See the endpoints at the [Swagger documentation](http://127.0.0.1:8080/swagger-ui.html)
while the program runs.

## Warning:
Binary uploads may not work through swagger's "try it out" feature for some reason.
I recommend you use `curl` or Postman instead.

## Passwords are now stored in plain text, but I plan to encrypt them and use http basic authentication too for simplicity. Requests aren't authenticated right now.

# How to run the project
On Linux:
```shell
# ./mvnw clean package -DskipTests
# java -jar target/DXVKStateCacheBank-0.0.1-SNAPSHOT.jar
```

On Windows (I did not test this):
```shell
# mvnw.cmd clean package -DskipTests
# java -jar target/DXVKStateCacheBank-0.0.1-SNAPSHOT.jar
```
