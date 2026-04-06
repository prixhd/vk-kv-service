# VK KV Service

gRPC Key-Value сервис на Java с хранением данных в Tarantool.

## Запуск

```bash
# Запустить Tarantool
docker-compose up -d

# Собрать и запустить сервис
mvn compile exec:java -Dexec.mainClass=org.example.KvServiceMain
```

## API

| Метод | Описание |
|-------|----------|
| Put(key, value) | Сохранить/перезаписать значение |
| Get(key) | Получить значение |
| Delete(key) | Удалить запись |
| Range(key_since, key_to) | Stream пар ключ-значение |
| Count() | Количество записей |

## Примеры

```bash
# Put
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  -d '{"key": "hello", "value": "d29ybGQ="}' \
  localhost:9090 kv.KVService/Put

# Get  
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  -d '{"key": "hello"}' \
  localhost:9090 kv.KVService/Get

# Count
./grpcurl -plaintext -proto src/main/proto/kv.proto \
  localhost:9090 kv.KVService/Count
```