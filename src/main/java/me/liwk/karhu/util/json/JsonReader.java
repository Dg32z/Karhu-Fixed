/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Player
 */
package me.liwk.karhu.util.json;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.util.BanData;
import me.liwk.karhu.util.haste.Hastebin;
import me.liwk.karhu.util.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class JsonReader {
    private static JsonObject readIp(String address) throws IOException {
        String sURL = "http://proxycheck.io/v2/" + address + "?key=0q2s38-80419r-466076-h10l90&risk=1&vpn=1&asn=1";
        URL url = new URL(sURL);
        URLConnection request = url.openConnection();
        request.setRequestProperty("User-Agent", "KarhuAC");
        request.setReadTimeout(4000);
        request.connect();
        JsonParser jp = new JsonParser();
        JsonElement root = jp.parse(new InputStreamReader((InputStream)request.getContent()));
        return root.getAsJsonObject();
    }

    public static JsonObject sendBan(BanData ban) throws IOException {
        JsonObject json;
        HttpsURLConnection request;
        block13: {
            String sURL = "https://karhu.cc/api/bandata";
            request = (HttpsURLConnection)new URL(sURL).openConnection();
            request.setDoOutput(true);
            request.setRequestMethod("POST");
            request.addRequestProperty("User-Agent", "KarhuAC");
            request.addRequestProperty("content-type", "application/json");
            request.addRequestProperty("Authorization", "gdY2P44WZtacyQQ4xmefvZxhUX6YcZEhmv6XEkA6");
            request.setReadTimeout(10000);
            request.connect();
            json = new JsonObject();
            json.addProperty("license", ban.license);
            json.addProperty("karhu_version", ban.karhuVer);
            json.addProperty("server_version", ban.serverVer);
            json.addProperty("tps", ban.tps);
            json.addProperty("player", ban.player);
            json.addProperty("banned_for", ban.type);
            json.addProperty("client", ban.client);
            json.addProperty("time_played", ban.sessionTime);
            json.addProperty("coordinates", ban.coordinates);
            json.addProperty("ping", ban.ping);
            String[] paska = JsonReader.getHaste(ban);
            if (paska[0].length() > 1) {
                try {
                    String hasteURL = Hastebin.uploadPaste(paska[0]);
                    if (hasteURL != null) {
                        json.addProperty("logs", hasteURL);
                        break block13;
                    }
                    json.addProperty("logs", "Couldn't paste logs, maybe the file is too big?");
                }
                catch (Exception var19) {
                    json.addProperty("logs", "Couldn't paste logs, maybe the file is too big? (" + var19.getMessage() + ")");
                }
            } else {
                json.addProperty("logs", paska[1]);
            }
        }
        byte[] out = json.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = request.getOutputStream()){
            os.write(out);
            os.flush();
        }
        catch (Exception var21) {
            var21.printStackTrace();
        }
        Gson gson = new Gson();
        return gson.fromJson(new InputStreamReader(request.getInputStream(), Charsets.UTF_8), JsonObject.class);
    }

    public static String[] getData(String address) throws IOException {
        JsonObject object = JsonReader.readIp(address);
        if (object.entrySet() != null) {
            for (Map.Entry obj : object.entrySet()) {
                if (!((String)obj.getKey()).equalsIgnoreCase(address)) continue;
                JsonObject data = ((JsonElement)obj.getValue()).getAsJsonObject();
                String proxy = data.get("proxy").getAsString().equalsIgnoreCase("yes") ? "true" : "false";
                String geoLocation = data.get("country").getAsString();
                JsonElement testRisk = data.get("risk");
                int riskLevel = testRisk == null ? 0 : testRisk.getAsInt();
                String risk = riskLevel > 50 ? "true" : "false";
                return new String[]{proxy, geoLocation, risk};
            }
        }
        return new String[]{null, null, null};
    }

    private static String[] getHaste(BanData data) {
        Player target;
        String uuid = Karhu.getInstance().getConfigManager().isCrackedServer() ? ((target = data.playerObj) != null ? target.getName() : data.player) : ((target = data.playerObj) != null ? target.getUniqueId().toString() : Bukkit.getOfflinePlayer(data.player).getUniqueId().toString());
        List<ViolationX> vls = Karhu.storage.getAllViolations(uuid);
        if (vls.isEmpty()) {
            return new String[]{"", "Player has no logs!"};
        }
        StringBuilder end = new StringBuilder("Anticheat logs for player " + data.player + " pasted with " + Karhu.getInstance().getConfigManager().getName() + " " + Karhu.getInstance().getVersion());
        try {
            for (ViolationX v : vls) {
                String logline = TextUtils.formatMillis(System.currentTimeMillis() - v.time) + " ago | " + ChatColor.stripColor(v.type).replaceAll("\n", " ") + " [" + ChatColor.stripColor(v.data.replaceAll("\n", " ") + "] [" + v.ping + "ms]/[" + v.TPS + " TPS] (x" + v.vl + ")");
                end.append("\n").append(logline);
            }
            return new String[]{end.toString(), "Pasted logs to: "};
        }
        catch (Exception var7) {
            return new String[]{"", "Couldn't paste logs, maybe the file is too big? (" + var7.getMessage() + ")"};
        }
    }
}

