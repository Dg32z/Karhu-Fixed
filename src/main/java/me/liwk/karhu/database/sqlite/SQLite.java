package me.liwk.karhu.database.sqlite;

import lombok.SneakyThrows;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.database.Query;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class SQLite {
    public static Connection conn;

    public static void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + Karhu.getInstance().getPlug().getDataFolder().getAbsolutePath() + File.separator + "database.sqlite";
            conn = DriverManager.getConnection(url);
            Query.use(conn);
            Karhu.getInstance().printCool("&b> &aConnection to SQLite has been established.");
        } catch (Exception var1) {
            Karhu.getInstance().printCool("&b> &cConnection to SQLite has failed.");
            var1.printStackTrace();
        }
    }

    @SneakyThrows
    public static void use() {
        if (conn.isClosed()) {
            String url = "jdbc:sqlite:" + Karhu.getInstance().getPlug().getDataFolder().getAbsolutePath() + File.separator + "database.sqlite";
            conn = DriverManager.getConnection(url);
            Query.use(conn);
        }
    }
}
