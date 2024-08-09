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
    private final HikariDataSource dataConfig;

    public DatabaseReader(Config config) {
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
            String tableName = "deduplication_rules";
            var rulesFromTable = context.select(
                            field("deduplication_id"),
                            field("rule_id"),
                            field("field_name"),
                            field("time_to_live_sec"),
                            field("is_active")
                    )
                    .from(table(tableName))
                    .fetch();

            if (!rulesFromTable.isEmpty()) {
                log.debug("Found rules from database");
                Rule[] rules = new Rule[rulesFromTable.size()];

                for (int i = 0; i < rulesFromTable.size(); i++) {
                    Rule rule = new Rule();

                    rule.setDeduplicationId(rulesFromTable.get(i).getValue("deduplication_id", Long.class));
                    rule.setRuleId(rulesFromTable.get(i).getValue("rule_id", Long.class));
                    rule.setFieldName(rulesFromTable.get(i).getValue("field_name", String.class));
                    rule.setTimeToLiveSec(rulesFromTable.get(i).getValue("time_to_live_sec", Long.class));
                    rule.setIsActive(rulesFromTable.get(i).getValue("is_active", Boolean.class));
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
