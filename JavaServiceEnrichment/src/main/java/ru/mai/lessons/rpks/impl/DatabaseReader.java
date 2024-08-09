package ru.mai.lessons.rpks.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import ru.mai.lessons.rpks.DbReader;
import ru.mai.lessons.rpks.model.Rule;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Slf4j
public class DatabaseReader implements DbReader {
    private final Config config;
    private final HikariDataSource dataConfig;

    public DatabaseReader(Config config) {
        this.config = config;
        Config dbConfig = config.getConfig("db");
        Properties dbConfigProperties = new Properties();
        for (Map.Entry<String, ConfigValue> entry : dbConfig.entrySet()) {
            dbConfigProperties.setProperty(entry.getKey(), dbConfig.getString(entry.getKey()));
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbConfigProperties.getProperty("jdbcUrl"));
        hikariConfig.setUsername(dbConfigProperties.getProperty("user"));
        hikariConfig.setPassword(dbConfigProperties.getProperty("password"));
        hikariConfig.setDriverClassName(dbConfigProperties.getProperty("driver"));

        this.dataConfig = new HikariDataSource(hikariConfig);
    }

    @Override
    public Rule[] readRulesFromDB() {
        try {
            DSLContext context = DSL.using(dataConfig.getConnection(), SQLDialect.POSTGRES);
            String tableName = "enrichment_rules";
            var rulesFromTable = context.select(
                            field("enrichment_id"),
                            field("rule_id"),
                            field("field_name"),
                            field("field_name_enrichment"),
                            field("field_value"),
                            field("field_value_default")
                    )
                    .from(table(tableName))
                    .where(field("enrichment_id").eq(config.getInt("application.enrichmentId")))
                    .orderBy(field("rule_id"))
                    .fetch();

            if (!rulesFromTable.isEmpty()) {
                log.debug("Found rules from database");
                Rule[] rules = new Rule[rulesFromTable.size()];

                for (int i = 0; i < rulesFromTable.size(); i++) {
                    Rule rule = new Rule();

                    rule.setEnricherId(rulesFromTable.get(i).getValue("enrichment_id", Long.class));
                    rule.setRuleId(rulesFromTable.get(i).getValue("rule_id", Long.class));
                    rule.setFieldName(rulesFromTable.get(i).getValue("field_name", String.class));
                    rule.setFieldNameEnrichment(rulesFromTable.get(i).getValue("field_name_enrichment", String.class));
                    rule.setFieldValue(rulesFromTable.get(i).getValue("field_value", String.class));
                    rule.setFieldValueDefault(rulesFromTable.get(i).getValue("field_value_default", String.class));
                    rules[i] = rule;
                }

                return rules;
            }
        } catch (SQLException ex) {
            log.error("Error in DatabaseReader:", ex);
        }

        return new Rule[0];
    }

    @Override
    public void close() {
        dataConfig.close();
    }
}
