package ru.netology.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLHelper {
    private static final QueryRunner QUERY_RUNNER = new QueryRunner();

    private SQLHelper() {
    }

    @SneakyThrows
    private static Connection getConnection() {
        var url = System.getProperty("spring.datasource.url");
        var username = System.getProperty("spring.datasource.username");
        var password = System.getProperty("spring.datasource.password");
        return DriverManager.getConnection(url, username, password);
    }

    @Data
    @NoArgsConstructor
    public static class SQLPaymentEntity {
        private String id;
        private String amount;
        private String created;
        private String status;
        private String transaction_id;
    }

    @SneakyThrows
    public static SQLPaymentEntity getInfoFromDebitPayment() {
        var code = "SELECT * FROM payment_entity WHERE created >= (SELECT MAX(created) FROM payment_entity);";
        var conn = getConnection();
        return QUERY_RUNNER.query(conn, code, new BeanHandler<>(SQLPaymentEntity.class));
    }

    @Data
    @NoArgsConstructor
    public static class SQLOrderEntity {
        private String id;
        private String created;
        private String credit_id;
        private String payment_id;
    }

    @SneakyThrows
    public static SQLOrderEntity getInfoFromOrder() {
        var code = "SELECT * FROM order_entity WHERE created >= (SELECT MAX(created) FROM order_entity);";
        var conn = getConnection();
        return QUERY_RUNNER.query(conn, code, new BeanHandler<>(SQLOrderEntity.class));
    }

    @Data
    @NoArgsConstructor
    public static class SQLCreditRequestEntity {
        private String id;
        private String bank_id;
        private String created;
        private String status;
    }

    @SneakyThrows
    public static SQLCreditRequestEntity getInfoFromCreditPayment() {
        var code =  "SELECT * FROM credit_request_entity WHERE created >= (SELECT MAX(created) FROM credit_request_entity);";
        var conn = getConnection();
        return QUERY_RUNNER.query(conn, code, new BeanHandler<>(SQLCreditRequestEntity.class));
    }

}
