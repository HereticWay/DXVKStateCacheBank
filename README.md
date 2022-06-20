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

## Passwords are now stored in plain text,but I plan to encrypt them and use http basic authentication too for simplicity. Requests aren't authenticated right now.

# How to run the project
### To run in Docker:
```shell
# Navigate to the project's directory and then run the following command
$ docker compose up
```

### To run on Linux:
**OpenJDK 18** must be installed and you should have
[**dxvk-cache-tool**](https://github.com/DarkTigrus/dxvk-cache-tool/releases) on your PATH before
trying to run the program on your system!
```shell
# Navigate to the project's directory and then run the following commands
$ ./mvnw clean package -DskipTests
$ java -jar target/DXVKStateCacheBank-0.0.1-SNAPSHOT.jar
```

### To run on Windows:
Running on Windows is currently only supported through **WSL** and **Docker** because dxvk-cache-tool
does not have a native Windows build.
