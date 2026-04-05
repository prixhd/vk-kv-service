# VK KV Storage Service

gRPC сервис для хранения Key-Value данных на Tarantool 3.2.

## Запуск

### 1. Запуск Tarantool

```bash
docker-compose up -d
```

Проверка (должно быть "Stored procedures загружены"):
```bash
docker-compose logs tarantool | grep "Stored procedures"
```


### 2. Сборка и запуск Java сервиса

```bash
mvn clean package
java -jar target/vk-kv-service-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Сервис запустится на порту `9090`.

### 3. Проверка работы

#### PUT 
```bash
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  -d '{"key": "user:1", "value": "aGVsbG8="}' \
  localhost:9090 kv.KVService/Put
```

#### PUT с null значением
```bash
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  -d '{"key": "user:2"}' \
  localhost:9090 kv.KVService/Put
```

#### GET 
```bash
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  -d '{"key": "user:1"}' \
  localhost:9090 kv.KVService/Get
```

#### COUNT 
```bash
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  localhost:9090 kv.KVService/Count
```

#### RANGE 
```bash
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  -d '{"key_since": "user:1", "key_to": "user:9"}' \
  localhost:9090 kv.KVService/Range
```

#### DELETE 
```bash
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  -d '{"key": "user:1"}' \
  localhost:9090 kv.KVService/Delete
```

## Технологии

- Tarantool 3.2
- Java 17
- gRPC
- Maven

## В конце остановка 

```bash
docker-compose down -v 
```
