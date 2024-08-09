package ru.mai.lessons.rpks;

import ru.mai.lessons.rpks.model.Message;

public interface RedisClient extends AutoCloseable {
    public void processing(String key, Message message, Long time);

    /** Нужно реализовать этот интерфейс таким образом:
     Чтение данных по ключу, чтобы проверить есть ли уже такой ключ в Redis.
     Если есть, значит это дупликат и устанавливаем deduplicationState = false.
     Если нет, значит вставляем это значение в Redis, устанавливаем время жизни сообщения по правилу из PostgreSQL и проставляем deduplicationState = true.
     Реализация RedisClient должна работать в RuleProcessor.
    */
}

