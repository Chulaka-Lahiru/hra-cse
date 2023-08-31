# Hybrid-Refresh-Ahead-Caching Solution and Application

Spring Boot - Mysql & Redis Cache Implementation using REST API

### Prerequisites

- JDK 1.8
- Maven
- Mysql
- Redis (Using Lettuce)

## Quick Start

```
start MySQL service and create database
```

```
start Redis service
```

### Build

```
mvn clean package
```

### Run

```
java -jar target/hra-cache-web-app-0.0.1-SNAPSHOT.jar
```

##

### Get information about system health, configurations, etc.

```
http://localhost:8091/env
http://localhost:8091/health
http://localhost:8091/info
http://localhost:8091/metrics
```

### Test using CURL

- data add

```
curl --location --request POST 'http://localhost:8080/hracachecontroller/write-back-telco-resources' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id" : "35"
    "name" : "Sanath Jayasuriya",
    "age"  : 48 ,
    "salary" : 2700000 
}'
```

- data get cache - No expiration time set

```
curl --location --request GET 'http://localhost:8080/hracachecontroller/read-through-telco-resources/35'
```

- response :

```
{
    "id": 35,
    "createdDate": "XXXX-XX-XXTXX:XX:XX",
    "modifiedDate": "XXXX-XX-XXTXX:XX:XX",
    "name": "Sanath Jayasuriya",
    "age": 48,
    "salary": 2700000.0
}
```

### Swagger-ui REST API Reference & Test

- http://localhost:8080/swagger-ui.html
- Response Content Type : application/json
