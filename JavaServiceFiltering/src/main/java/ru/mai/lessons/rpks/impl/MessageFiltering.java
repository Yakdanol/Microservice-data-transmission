package ru.mai.lessons.rpks.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.RuleProcessor;
import ru.mai.lessons.rpks.model.Message;
import ru.mai.lessons.rpks.model.Rule;

import java.io.IOException;

@Slf4j
public class MessageFiltering implements RuleProcessor {
    private final ObjectMapper objectMapper;
    public MessageFiltering() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Message processing(Message message, Rule[] rules) {
        if (message.getValue().isEmpty() || rules == null || rules.length == 0) {
            message.setFilterState(false);
            return message;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(message.getValue());

            for (Rule rule : rules) {
                String filterFunctionName = rule.getFilterFunctionName(); // (not) equals, (not) contains
                String filterValue = rule.getFilterValue(); // значение поля: alex, 18, M
                String fieldName = rule.getFieldName(); // имя поля, которое сравнивается: name, age, sex
                log.info(filterFunctionName + " " + filterValue + " " + fieldName);
                JsonNode field = jsonNode.get(fieldName); // получаем значение по полю сравнения fieldName

                if (field == null || !field.isValueNode() || !checkFilter(field.asText(), filterFunctionName, filterValue)) {
                    message.setFilterState(false);
                    return message;
                }
            }

            message.setFilterState(true);
            return message;

        } catch (IOException ex) {
            log.error("Exception while reading json message:", ex);
        }

        return message;
    }

    private boolean checkFilter(String fieldValue, String filterFunctionName, String filterValue) {
        return switch (filterFunctionName.toLowerCase()) {
            case "equals" -> fieldValue.equals(filterValue);
            case "contains" -> fieldValue.contains(filterValue);
            case "not_equals" -> !fieldValue.equals(filterValue);
            case "not_contains" -> !fieldValue.contains(filterValue);
            default -> false;
        };
    }
}

