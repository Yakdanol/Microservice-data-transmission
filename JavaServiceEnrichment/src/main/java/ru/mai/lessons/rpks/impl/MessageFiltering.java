package ru.mai.lessons.rpks.impl;

import com.mongodb.client.MongoClient;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.MongoDBClientEnricher;
import ru.mai.lessons.rpks.RuleProcessor;
import ru.mai.lessons.rpks.model.Message;
import ru.mai.lessons.rpks.model.Rule;

@Slf4j
public class MessageFiltering implements RuleProcessor {
    private final MongoDBClientEnricher mongoDBClient;

    public MessageFiltering(Config config, MongoClient mongoClient) {
        this.mongoDBClient = new MongoDBClientEnricherImpl(config, mongoClient);
    }

    @Override
    public Message processing(Message message, Rule[] rules, MongoClient mongoClient) {
        if (message.getValue().isEmpty() || rules.length == 0) {
            return message;
        }

        return mongoDBClient.enrich(message, rules);
    }
}
