package me.liwk.karhu.database.mysql;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.database.Query;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQL {
    private static Connection conn;

    public static void init() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.jdbc.Driver");
                FileConfiguration config = Karhu.getInstance().getConfigManager().getConfig();
                conn = DriverManager.getConnection(
                        "jdbc:mysql://" + config.getString("mysql.address") + ":" + config.getString("mysql.port") + "/?useSSL=false",
                        config.getString("mysql.user"),
                        config.getString("mysql.password")
                );
                conn.setAutoCommit(true);
                Query.use(conn);
                Query.prepare("CREATE DATABASE IF NOT EXISTS `" + config.getString("mysql.database") + "`").execute();
                Query.prepare("USE `" + config.getString("mysql.database") + "`").execute();
                Karhu.getInstance().printCool("&b> &aConnection to MySQL has been established.");
            }
        } catch (Exception var11) {
            Karhu.getInstance().printCool("&b> &cConnection to SQLite has failed.");
            var11.printStackTrace();
        }
    }

    public static void use() {
        init();
    }
}