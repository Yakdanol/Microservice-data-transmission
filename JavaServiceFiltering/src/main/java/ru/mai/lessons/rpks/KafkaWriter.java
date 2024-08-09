package ru.mai.lessons.rpks;

import ru.mai.lessons.rpks.model.Message;

public interface KafkaWriter extends AutoCloseable {
    public void processing(Message message);
    // отправляет сообщения с filterState = true в выходной топик.
    // Конфигурация берется из файла *.conf
}
