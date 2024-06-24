/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.database;

import java.sql.Connection;
import java.sql.SQLException;

public class Query {
    private static Connection conn;

    public static ExecutableStatement prepare(String query, Connection con) throws SQLException {
        return new ExecutableStatement(con.prepareStatement(query));
    }

    public static ExecutableStatement prepare(String query) throws SQLException {
        return new ExecutableStatement(conn.prepareStatement(query));
    }

    public static void use(Connection conn) {
        Query.conn = conn;
    }
}

