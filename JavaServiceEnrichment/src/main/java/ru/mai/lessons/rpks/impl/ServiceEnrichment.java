package ru.mai.lessons.rpks.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.DbReader;
import ru.mai.lessons.rpks.Service;
import ru.mai.lessons.rpks.model.Rule;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServiceEnrichment implements Service {
    private DbReader dbReader;
    KafkaReaderImpl kafkaReader;

    @Override
    public void start(Config config) {
        dbReader = new DatabaseReader(config);
        MongoClient mongoClient = MongoClients.create(config.getString("mongo.connectionString"));
        kafkaReader = new KafkaReaderImpl(config, mongoClient);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        try {
            executorService.scheduleAtFixedRate(this::updateRules, 0, config.getInt("application.updateIntervalSec"), TimeUnit.SECONDS);
            kafkaReader.processing();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
                log.error(Arrays.deepToString(ex.getStackTrace()));
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
            }
        }
    }

    private void updateRules () {
        Rule[] rules = dbReader.readRulesFromDB();
        kafkaReader.setRules(rules);
    }
}

