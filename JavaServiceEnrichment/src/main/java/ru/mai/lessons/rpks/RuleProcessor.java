package ru.mai.lessons.rpks;

import com.mongodb.client.MongoClient;
import ru.mai.lessons.rpks.model.Message;
import ru.mai.lessons.rpks.model.Rule;

public interface RuleProcessor {
    public Message processing(Message message, Rule[] rules, MongoClient mongoClient); // применяет правила обогащения к сообщениям и вставляет документы из MongoDB в указанные поля сообщения, если сообщение удовлетворяет условиям всех правил.
}
