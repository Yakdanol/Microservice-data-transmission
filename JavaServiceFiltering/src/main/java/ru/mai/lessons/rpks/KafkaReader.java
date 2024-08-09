package ru.mai.lessons.rpks;

public interface KafkaReader extends AutoCloseable {
    public void processing();
    // запускает KafkaConsumer в бесконечном цикле и читает сообщения.
    // Внутри метода происходит обработка сообщений
    // по правилам и отправка сообщений в Kafka выходной топик.
    // Конфигурация для консюмера из файла *.conf
}
