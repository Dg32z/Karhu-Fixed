/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.java.JavaPlugin
 */
package me.liwk.karhu.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Metrics {
    private final Plugin plugin;
    private final MetricsBase metricsBase;

    public Metrics(JavaPlugin plugin, int serviceId) {
        this.plugin = plugin;
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration((File)configFile);
        if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", (Object)true);
            config.addDefault("serverUuid", (Object)UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", (Object)false);
            config.addDefault("logSentData", (Object)false);
            config.addDefault("logResponseStatusText", (Object)false);
            config.options().header("bStats (https://bStats.org) collects some basic information for plugin authors, like how\nmany people use their plugin and their total player count. It's recommended to keep bStats\nenabled, but if you're not comfortable with this, you can turn this setting off. There is no\nperformance penalty associated with having metrics enabled, and data sent to bStats is fully\nanonymous.").copyDefaults(true);
            try {
                config.save(configFile);
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUuid");
        boolean logErrors = config.getBoolean("logFailedRequests", false);
        boolean logSentData = config.getBoolean("logSentData", false);
        boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        this.metricsBase = new MetricsBase("bukkit", serverUUID, serviceId, enabled, this::appendPlatformData, this::appendServiceData, submitDataTask -> Bukkit.getScheduler().runTask((Plugin)plugin, submitDataTask), () -> ((JavaPlugin)plugin).isEnabled(), (message, error) -> this.plugin.getLogger().log(Level.WARNING, (String)message, (Throwable)error), message -> this.plugin.getLogger().log(Level.INFO, (String)message), logErrors, logSentData, logResponseStatusText);
    }

    public void addCustomChart(CustomChart chart) {
        this.metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", 10000);
        builder.appendField("onlineMode", 1);
        builder.appendField("bukkitVersion", Bukkit.getVersion());
        builder.appendField("bukkitName", Bukkit.getName());
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", this.plugin.getDescription().getVersion());
    }

    private int getPlayerAmount() {
        return 10000;
    }

    public static class JsonObjectBuilder {
        private StringBuilder builder = new StringBuilder();
        private boolean hasAtLeastOneField = false;

        public JsonObjectBuilder() {
            this.builder.append("{");
        }

        public JsonObjectBuilder appendNull(String key) {
            this.appendFieldUnescaped(key, "null");
            return this;
        }

        public JsonObjectBuilder appendField(String key, String value) {
            if (value == null) {
                throw new IllegalArgumentException("JSON value must not be null");
            }
            this.appendFieldUnescaped(key, "\"" + JsonObjectBuilder.escape(value) + "\"");
            return this;
        }

        public JsonObjectBuilder appendField(String key, int value) {
            this.appendFieldUnescaped(key, String.valueOf(value));
            return this;
        }

        public JsonObjectBuilder appendField(String key, JsonObject object) {
            if (object == null) {
                throw new IllegalArgumentException("JSON object must not be null");
            }
            this.appendFieldUnescaped(key, object.toString());
            return this;
        }

        public JsonObjectBuilder appendField(String key, String[] values) {
            if (values == null) {
                throw new IllegalArgumentException("JSON values must not be null");
            }
            String escapedValues = Arrays.stream(values).map(value -> "\"" + JsonObjectBuilder.escape(value) + "\"").collect(Collectors.joining(","));
            this.appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        public JsonObjectBuilder appendField(String key, int[] values) {
            if (values == null) {
                throw new IllegalArgumentException("JSON values must not be null");
            }
            String escapedValues = Arrays.stream(values).mapToObj(String::valueOf).collect(Collectors.joining(","));
            this.appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        public JsonObjectBuilder appendField(String key, JsonObject[] values) {
            if (values == null) {
                throw new IllegalArgumentException("JSON values must not be null");
            }
            String escapedValues = Arrays.stream(values).map(JsonObject::toString).collect(Collectors.joining(","));
            this.appendFieldUnescaped(key, "[" + escapedValues + "]");
            return this;
        }

        private void appendFieldUnescaped(String key, String escapedValue) {
            if (this.builder == null) {
                throw new IllegalStateException("JSON has already been built");
            }
            if (key == null) {
                throw new IllegalArgumentException("JSON key must not be null");
            }
            if (this.hasAtLeastOneField) {
                this.builder.append(",");
            }
            this.builder.append("\"").append(JsonObjectBuilder.escape(key)).append("\":").append(escapedValue);
            this.hasAtLeastOneField = true;
        }

        public JsonObject build() {
            if (this.builder == null) {
                throw new IllegalStateException("JSON has already been built");
            }
            JsonObject object = new JsonObject(this.builder.append("}").toString());
            this.builder = null;
            return object;
        }

        private static String escape(String value) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < value.length(); ++i) {
                char c = value.charAt(i);
                if (c == '\"') {
                    builder.append("\\\"");
                    continue;
                }
                if (c == '\\') {
                    builder.append("\\\\");
                    continue;
                }
                if (c <= '\u000f') {
                    builder.append("\\u000").append(Integer.toHexString(c));
                    continue;
                }
                if (c <= '\u001f') {
                    builder.append("\\u00").append(Integer.toHexString(c));
                    continue;
                }
                builder.append(c);
            }
            return builder.toString();
        }

        public static class JsonObject {
            private final String value;

            private JsonObject(String value) {
                this.value = value;
            }

            public String toString() {
                return this.value;
            }
        }
    }

    public static class DrilldownPie
    extends CustomChart {
        private final Callable<Map<String, Map<String, Integer>>> callable;

        public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Map<String, Integer>> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean reallyAllSkipped = true;
            for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
                JsonObjectBuilder valueBuilder = new JsonObjectBuilder();
                boolean allSkipped = true;
                for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    valueBuilder.appendField(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                }
                if (allSkipped) continue;
                reallyAllSkipped = false;
                valuesBuilder.appendField(entryValues.getKey(), valueBuilder.build());
            }
            if (reallyAllSkipped) {
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    public static class SimplePie
    extends CustomChart {
        private final Callable<String> callable;

        public SimplePie(String chartId, Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            String value = this.callable.call();
            if (value == null || value.isEmpty()) {
                return null;
            }
            return new JsonObjectBuilder().appendField("value", value).build();
        }
    }

    public static class SingleLineChart
    extends CustomChart {
        private final Callable<Integer> callable;

        public SingleLineChart(String chartId, Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            int value = this.callable.call();
            if (value == 0) {
                return null;
            }
            return new JsonObjectBuilder().appendField("value", value).build();
        }
    }

    public static abstract class CustomChart {
        private final String chartId;

        protected CustomChart(String chartId) {
            if (chartId == null) {
                throw new IllegalArgumentException("chartId must not be null");
            }
            this.chartId = chartId;
        }

        public JsonObjectBuilder.JsonObject getRequestJsonObject(BiConsumer<String, Throwable> errorLogger, boolean logErrors) {
            JsonObjectBuilder builder = new JsonObjectBuilder();
            builder.appendField("chartId", this.chartId);
            try {
                JsonObjectBuilder.JsonObject data = this.getChartData();
                if (data == null) {
                    return null;
                }
                builder.appendField("data", data);
            }
            catch (Throwable t) {
                if (logErrors) {
                    errorLogger.accept("Failed to get data for custom chart with id " + this.chartId, t);
                }
                return null;
            }
            return builder.build();
        }

        protected abstract JsonObjectBuilder.JsonObject getChartData() throws Exception;
    }

    public static class AdvancedPie
    extends CustomChart {
        private final Callable<Map<String, Integer>> callable;

        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) continue;
                allSkipped = false;
                valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    public static class MultiLineChart
    extends CustomChart {
        private final Callable<Map<String, Integer>> callable;

        public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) continue;
                allSkipped = false;
                valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    public static class SimpleBarChart
    extends CustomChart {
        private final Callable<Map<String, Integer>> callable;

        public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, Integer> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                valuesBuilder.appendField(entry.getKey(), new int[]{entry.getValue()});
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    public static class AdvancedBarChart
    extends CustomChart {
        private final Callable<Map<String, int[]>> callable;

        public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JsonObjectBuilder.JsonObject getChartData() throws Exception {
            JsonObjectBuilder valuesBuilder = new JsonObjectBuilder();
            Map<String, int[]> map = this.callable.call();
            if (map == null || map.isEmpty()) {
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, int[]> entry : map.entrySet()) {
                if (entry.getValue().length == 0) continue;
                allSkipped = false;
                valuesBuilder.appendField(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                return null;
            }
            return new JsonObjectBuilder().appendField("values", valuesBuilder.build()).build();
        }
    }

    public static class MetricsBase {
        public static final String METRICS_VERSION = "2.2.1";
        private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, task -> new Thread(task, "bStats-Metrics"));
        private static final String REPORT_URL = "https://bStats.org/api/v2/data/%s";
        private final String platform;
        private final String serverUuid;
        private final int serviceId;
        private final Consumer<JsonObjectBuilder> appendPlatformDataConsumer;
        private final Consumer<JsonObjectBuilder> appendServiceDataConsumer;
        private final Consumer<Runnable> submitTaskConsumer;
        private final Supplier<Boolean> checkServiceEnabledSupplier;
        private final BiConsumer<String, Throwable> errorLogger;
        private final Consumer<String> infoLogger;
        private final boolean logErrors;
        private final boolean logSentData;
        private final boolean logResponseStatusText;
        private final Set<CustomChart> customCharts = new HashSet<CustomChart>();
        private final boolean enabled;

        public MetricsBase(String platform, String serverUuid, int serviceId, boolean enabled, Consumer<JsonObjectBuilder> appendPlatformDataConsumer, Consumer<JsonObjectBuilder> appendServiceDataConsumer, Consumer<Runnable> submitTaskConsumer, Supplier<Boolean> checkServiceEnabledSupplier, BiConsumer<String, Throwable> errorLogger, Consumer<String> infoLogger, boolean logErrors, boolean logSentData, boolean logResponseStatusText) {
            this.platform = platform;
            this.serverUuid = serverUuid;
            this.serviceId = serviceId;
            this.enabled = enabled;
            this.appendPlatformDataConsumer = appendPlatformDataConsumer;
            this.appendServiceDataConsumer = appendServiceDataConsumer;
            this.submitTaskConsumer = submitTaskConsumer;
            this.checkServiceEnabledSupplier = checkServiceEnabledSupplier;
            this.errorLogger = errorLogger;
            this.infoLogger = infoLogger;
            this.logErrors = logErrors;
            this.logSentData = logSentData;
            this.logResponseStatusText = logResponseStatusText;
            this.checkRelocation();
            if (enabled) {
                this.startSubmitting();
            }
        }

        public void addCustomChart(CustomChart chart) {
            this.customCharts.add(chart);
        }

        private void startSubmitting() {
            Runnable submitTask = () -> {
                if (!this.enabled || !this.checkServiceEnabledSupplier.get().booleanValue()) {
                    scheduler.shutdown();
                    return;
                }
                if (this.submitTaskConsumer != null) {
                    this.submitTaskConsumer.accept(this::submitData);
                } else {
                    this.submitData();
                }
            };
            long initialDelay = (long)(60000.0 * (3.0 + Math.random() * 3.0));
            long secondDelay = (long)(60000.0 * (Math.random() * 30.0));
            scheduler.schedule(submitTask, initialDelay, TimeUnit.MILLISECONDS);
            scheduler.scheduleAtFixedRate(submitTask, initialDelay + secondDelay, 1800000L, TimeUnit.MILLISECONDS);
        }

        private void submitData() {
            JsonObjectBuilder baseJsonBuilder = new JsonObjectBuilder();
            this.appendPlatformDataConsumer.accept(baseJsonBuilder);
            JsonObjectBuilder serviceJsonBuilder = new JsonObjectBuilder();
            this.appendServiceDataConsumer.accept(serviceJsonBuilder);
            JsonObjectBuilder.JsonObject[] chartData = (JsonObjectBuilder.JsonObject[])this.customCharts.stream().map(customChart -> customChart.getRequestJsonObject(this.errorLogger, this.logErrors)).filter(Objects::nonNull).toArray(JsonObjectBuilder.JsonObject[]::new);
            serviceJsonBuilder.appendField("id", this.serviceId);
            serviceJsonBuilder.appendField("customCharts", chartData);
            baseJsonBuilder.appendField("service", serviceJsonBuilder.build());
            baseJsonBuilder.appendField("serverUUID", this.serverUuid);
            baseJsonBuilder.appendField("metricsVersion", METRICS_VERSION);
            JsonObjectBuilder.JsonObject data = baseJsonBuilder.build();
            scheduler.execute(() -> {
                block2: {
                    try {
                        this.sendData(data);
                    }
                    catch (Exception e) {
                        if (!this.logErrors) break block2;
                        this.errorLogger.accept("Could not submit bStats metrics data", e);
                    }
                }
            });
        }

        private void sendData(JsonObjectBuilder.JsonObject data) throws Exception {
            if (this.logSentData) {
                this.infoLogger.accept("Sent bStats metrics data: " + data.toString());
            }
            String url = String.format(REPORT_URL, this.platform);
            HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
            byte[] compressedData = MetricsBase.compress(data.toString());
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Connection", "close");
            connection.addRequestProperty("Content-Encoding", "gzip");
            connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Metrics-Service/1");
            connection.setDoOutput(true);
            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());){
                outputStream.write(compressedData);
            }
            StringBuilder builder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));){
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
            }
            if (this.logResponseStatusText) {
                this.infoLogger.accept("Sent data to bStats and received response: " + builder);
            }
        }

        private void checkRelocation() {
            if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
                String defaultPackage = new String(new byte[]{111, 114, 103, 46, 98, 115, 116, 97, 116, 115});
                String examplePackage = new String(new byte[]{121, 111, 117, 114, 46, 112, 97, 99, 107, 97, 103, 101});
                if (MetricsBase.class.getPackage().getName().startsWith(defaultPackage) || MetricsBase.class.getPackage().getName().startsWith(examplePackage)) {
                    throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
                }
            }
        }

        private static byte[] compress(String str) throws IOException {
            if (str == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(outputStream);){
                gzip.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return outputStream.toByteArray();
        }
    }
}

