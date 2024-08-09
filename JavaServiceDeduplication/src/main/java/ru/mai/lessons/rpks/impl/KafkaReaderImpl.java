package ru.mai.lessons.rpks.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import ru.mai.lessons.rpks.KafkaReader;
import ru.mai.lessons.rpks.KafkaWriter;
import ru.mai.lessons.rpks.RuleProcessor;
import ru.mai.lessons.rpks.model.Message;
import ru.mai.lessons.rpks.model.Rule;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Setter
@Getter
public class KafkaReaderImpl implements KafkaReader {
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final KafkaWriter kafkaWriter;
    private final RuleProcessor ruleProcessor;
    private Rule[] rules;

    public KafkaReaderImpl(Config config) {
        Config consumerConfig = config.getConfig("kafka.consumer");
        Properties consumerConfigProperties = new Properties();
        for (Map.Entry<String, ConfigValue> entry : consumerConfig.entrySet()) {
            consumerConfigProperties.setProperty(entry.getKey(), consumerConfig.getString(entry.getKey()));
        }

        this.kafkaConsumer = new KafkaConsumer<>(
                consumerConfigProperties,
                new StringDeserializer(),
                new StringDeserializer()
        );

        this.kafkaWriter = new KafkaWriterImpl(config);
        this.ruleProcessor = new MessageFiltering(config);

        kafkaConsumer.subscribe(Collections.singleton(consumerConfigProperties.getProperty("topic")));
    }

    @Override
    public void processing() {
        while (true) {
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                Message message = ruleProcessor.processing(Message.builder().value(consumerRecord.value()).build(), rules);
                if (message.isDeduplicationState()) {
                    kafkaWriter.processing(message);
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        kafkaConsumer.close();
        kafkaWriter.close();
    }
}
