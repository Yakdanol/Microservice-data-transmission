package ru.mai.lessons.rpks.impl;

import com.typesafe.config.Config;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.mai.lessons.rpks.RedisClient;
import ru.mai.lessons.rpks.RuleProcessor;
import ru.mai.lessons.rpks.model.Message;
import ru.mai.lessons.rpks.model.Rule;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class MessageFiltering implements RuleProcessor {
    private final RedisClient redisClient;
    private final ObjectMapper objectMapper;

    public MessageFiltering(Config config) {
        this.redisClient = new RedisClientImpl(config);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Message processing(Message message, Rule[] rules) {
        if (message.getValue().isEmpty()) {
            message.setDeduplicationState(false);
            return message;
        }

        // при пустых правилах пропускаем сообщения напрямую
        if (rules.length == 0) {
            message.setDeduplicationState(true);
            return message;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(message.getValue());
            StringBuilder stringBuilder = new StringBuilder();

            for (Rule rule : rules) {
                if (rule.getIsActive().equals(Boolean.TRUE)) {
                    stringBuilder.append(jsonNode.get(rule.getFieldName()));
                }
            }

            if (!stringBuilder.toString().isEmpty()) {
                long maxTime = Arrays.stream(rules).mapToLong(Rule::getTimeToLiveSec).max().orElse(0);
                redisClient.processing(stringBuilder.toString(), message, maxTime);
            } else {
                message.setDeduplicationState(true);
            }

            return message;
        } catch (IOException ex) {
            log.error("Exception while reading json message");
            log.error(Arrays.deepToString(ex.getStackTrace()));
        }

        return message;
    }

    @SneakyThrows
    @Override
    public void close() {
        redisClient.close();
    }
}

