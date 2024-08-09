package ru.mai.lessons.rpks.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.mai.lessons.rpks.KafkaWriter;
import ru.mai.lessons.rpks.model.Message;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public class KafkaWriterImpl implements KafkaWriter {
    private final KafkaProducer<String, String> kafkaProducer;
    private final Config config;
    private final Properties producerConfigProperties = new Properties();

    public KafkaWriterImpl(Config config) {
        Config producerConfig = config.getConfig("kafka.producer");
        for (Map.Entry<String, ConfigValue> entry : producerConfig.entrySet()) {
            producerConfigProperties.setProperty(entry.getKey(), producerConfig.getString(entry.getKey()));
        }
        producerConfigProperties.setProperty(ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString());

        this.kafkaProducer = new KafkaProducer<>(
                producerConfigProperties,
                new StringSerializer(),
                new StringSerializer()
        );

        this.config = config;
    }

    @Override
    public void processing(Message message) {
        log.debug("Processing message: {}", message);
        String topic = producerConfigProperties.getProperty("topic");
        kafkaProducer.send(new ProducerRecord<>(topic, message.getValue()));
    }

    @Override
    public void close() {
        kafkaProducer.close();
    }
}

