<h1>Все сервисы и инфраструктура поднимаются в Docker через Docker Compose.</h1>

**Собрать образы**
```shell
docker build -t filtering ./JavaServiceFiltering
```
```shell
docker build -t deduplication ./JavaServiceDeduplication
```
```shell
docker build -t enrichment ./JavaServiceEnrichment
```
```shell
docker build -t management ./JavaServiceManagment
```

**Запустить контейнеры**
```shell
docker compose up
```

**Проверить работоспособность**
```shell
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic input
```
```shell
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic output
```
```shell
{"name":"danila", "age":21, "sex":"M"}