package me.liwk.karhu.database;

import lombok.SneakyThrows;

import java.sql.Connection;

public class Query {
    private static Connection conn;

    @SneakyThrows
    public static ExecutableStatement prepare(String query, Connection con) {
        return new ExecutableStatement(con.prepareStatement(query));
    }

    @SneakyThrows
    public static ExecutableStatement prepare(String query) {
        return new ExecutableStatement(conn.prepareStatement(query));
    }

    public static void use(Connection conn) {
        Query.conn = conn;
    }
}
