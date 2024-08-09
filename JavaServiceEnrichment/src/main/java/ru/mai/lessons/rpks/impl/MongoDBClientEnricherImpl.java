package ru.mai.lessons.rpks.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import ru.mai.lessons.rpks.MongoDBClientEnricher;
import ru.mai.lessons.rpks.model.Message;
import ru.mai.lessons.rpks.model.Rule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
public class MongoDBClientEnricherImpl implements MongoDBClientEnricher {
    private final MongoCollection<Document> mongoCollection;
    private final ObjectMapper objectMapper;

    public MongoDBClientEnricherImpl(Config config, MongoClient mongoClient) {
        this.mongoCollection = mongoClient
                .getDatabase(config.getString("mongo.database"))
                .getCollection(config.getString("mongo.collection"));
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Message enrich(Message message, Rule[] rules) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message.getValue());

            for (Rule rule : rules) {
                JsonNode valueEnrich;
                var actualDocument = Optional.ofNullable(mongoCollection
                        .find(eq(rule.getFieldNameEnrichment(), rule.getFieldValue()))
                        .sort(Sorts.descending("_id"))
                        .first());

                if (actualDocument.isPresent()) {
                    valueEnrich = objectMapper.readTree(actualDocument.get().toJson());

                } else {
                    valueEnrich = objectMapper.readTree(objectMapper.writeValueAsString(rule.getFieldValueDefault()));
                }

                ((ObjectNode) jsonNode).replace(rule.getFieldName(), valueEnrich);
            }

            message.setValue(jsonNode.toString());
            return message;
        } catch (IOException ex) {
            log.error("Exception while reading json message");
            log.error(Arrays.deepToString(ex.getStackTrace()));
        }

        return message;
    }
}
