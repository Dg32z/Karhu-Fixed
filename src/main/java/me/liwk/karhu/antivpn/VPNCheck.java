/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.antivpn;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.util.json.JsonReader;

import java.net.InetAddress;
import java.util.HashMap;

public class VPNCheck {
    private static final HashMap<String, Boolean> cachedIPs = new HashMap();

    public static boolean checkAddress(InetAddress inetAddress) {
        String ip = inetAddress.getHostAddress();
        String address = inetAddress.getHostName();
        if (cachedIPs.containsKey(ip)) {
            return cachedIPs.get(ip);
        }
        try {
            VPNCheck.checkVPN(ip);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return cachedIPs.containsKey(address) && cachedIPs.get(address) != false;
    }

    private static void checkVPN(String address) throws Exception {
        String[] dataFromIP = JsonReader.getData(address);
        if (dataFromIP[0] != null && dataFromIP[2] != null) {
            boolean proxy = Boolean.parseBoolean(dataFromIP[0]);
            boolean risk = Boolean.parseBoolean(dataFromIP[2]);
            if (proxy && Karhu.getInstance().getConfigManager().isProxycheck()) {
                cachedIPs.put(address, true);
            } else if (risk && Karhu.getInstance().getConfigManager().isMaliciouscheck()) {
                cachedIPs.put(address, true);
            } else {
                cachedIPs.put(address, false);
            }
        }
    }
}

