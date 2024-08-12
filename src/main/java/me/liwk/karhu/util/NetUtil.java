package me.liwk.karhu.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;

public class NetUtil {
    public static ClassLoader injectorClassLoader = JavaPlugin.class.getClassLoader();

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Throwable var3) {
        }
    }

    public static void close(AutoCloseable... closeables) {
        try {
            for (AutoCloseable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }
    }


    public static void download(File file, String from) throws Exception {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.getChannel().transferFrom(Channels.newChannel(new URL(from).openStream()), 0L, Long.MAX_VALUE);
        }
    }

}
