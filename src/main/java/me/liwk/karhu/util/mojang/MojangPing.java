/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonObject
 */
package me.liwk.karhu.util.mojang;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

public class MojangPing {
    private static final Gson gson = new Gson();

    public static UUID getUUID(String name) {
        try {
            Scanner scanner = new Scanner(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream(), "UTF-8").useDelimiter("\\A");
            String json = scanner.next();
            String uuid = ((JsonObject)gson.fromJson(json, JsonObject.class)).get("id").getAsString();
            return MojangPing.toUUID(uuid);
        }
        catch (Exception var41) {
            return null;
        }
    }

    public static UUID toUUID(String uuid) {
        if (uuid.contains("-")) {
            return UUID.fromString(uuid);
        }
        return UUID.fromString(uuid.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
    }
}

