*Read this in other languages: [English](README.md), [Hungarian](README.hu.md).*

# About
This project aims to solve the problem of storing and automatically merging dxvk pipeline
state cache files to a single "incremental" cache file per application that can be distributed
later across multiple devices.

The idea is that users can register to the api and contribute state cache files that their
application generated and my backend would store it and automatically merge it using the
dxvk-cache-tool to the latest "incremental" cache of the application. The latest incremental
cache can be downloaded by anybody, user account is not required.

> *Note:* that there's no UI for this program, it only exposes REST endpoints

# Endpoints
See the endpoints at the [**Swagger documentation**](http://127.0.0.1:8080/swagger-ui.html) while the program is running.

# Running the program
Currently the program can only run under Docker or Linux. Windows is not supported, because
the dxvk-cache-tool does not have a native Windows build.

## Running with Docker
Requirements:
- docker
- docker compose plugin (usually bundled together with docker installation)
```shell
# Navigate to the project's directory and run the following command:
$ docker compose up
```

## Running under Linux
Requirements:
- docker
- docker compose plugin (usually bundled together with docker installation)
- [**dxvk-cache-tool**](https://github.com/DarkTigrus/dxvk-cache-tool/releases/tag/v1.1.2)
  with **run permissions**:
  - in any **PATH directory** (recommended) 
  - or any custom directory (needs more configuration, more about that later)
- a **PostgreSQL database server** (can be started with docker, see: [Starting the PostgreSQL server](#starting-the-postgresql-server))
- choosing an **active run profile**.

### Starting the PostgreSQL server
```shell
$ docker run \
    -it \
    --rm \
    -p "5430:5432" \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=pass \
    -e POSTGRES_DB=dxvk-cache-bank-db \
    postgres:14.3-alpine3.16
```

### Run the program using Maven
```shell
# If dxvk-cache-tool is located in a PATH directory:
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# If dxvk-cache-tool is located elsewhere, then you must specify the location as a PATH directory for the appliaction and run it like this:
$ env PATH=$PATH:/path/to/dxvk-cache-tool-directory ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Run the program using Intellij IDEA
- Start the PostgreSQL server (see: [Starting the PostgreSQL server](#starting-the-postgresql-server))
- Load the project as a Maven project
- Set the **"Run/Debug Configurations"** like below:<br>
![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/intellij-run-configuration.png)
- If dxvk-cache-tool **is not** in a PATH directory, then in the **"Run/DebugConfigurations"->"Environment Variables"**
  field you must set the path of dxvk-cache-tool, for example: *"PATH=$PATH:/path/to/dxvk-cache-tool-directory"*
- Now you can run the program as usual with IDEA.

### Running tests with Intellij IDEA
- Start the PostgreSQL server (see: [Starting the PostgreSQL server](#starting-the-postgresql-server))
- Load the project as a Maven project
- Add a new test run configuration: **"Run/DebugConfigurations"->"Add new configuration"->"JUnit"
- Set it up like this:<br>
  Setting profile **is not** required here!<br>
  ![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/intellij-test-configuration.png)
- If dxvk-cache-tool **is not** in a PATH directory, then in the **"Environment Variables"**
  field you must set it to the path of dxvk-cache-tool's directory, for example:
  *"PATH=$PATH:/path/to/dxvk-cache-tool-directory"*
- Sometimes the test/resources directory doesn't get marked as a test resources folder. To correct that,
  you must mark it as test resources root like this:<br>
  "right click on test/resources"->"Mark directory as"->"Resources root"<br>
  ![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/mark-resource-1.png)
  ![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/mark-resource-2.png)
- Now you can run the tests as usual with IDEA

## Run under Windows
Running under Windows is only supported through **Docker/WSL**.<br>
See: [*Running the program*](#running-the-program), [*Running with Docker*](#running-with-docker)