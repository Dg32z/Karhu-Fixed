/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Player
 *  org.geysermc.floodgate.api.FloodgateApi
 */
package me.liwk.karhu.util.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.KarhuPlayer;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PlayerUtil {
    private static final UUID SPRINTING_SPEED_BOOST = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    public static final String legacyMovementSpeed = "generic.movementSpeed";
    public static final String movementSpeed = "minecraft:generic.movement";

    public static float getScaledFriction(KarhuPlayer data) {
        float f4 = data.getCurrentFriction();
        float f = 0.16277136f / (f4 * f4 * f4);
        float f5 = data.isLastOnGroundPacket() ? data.getAttributeSpeed() * f : (data.isWasWasSprinting() ? 0.025999999f : 0.02f);
        return f5;
    }

    public static float getScaledFriction(KarhuPlayer data, boolean sprinting) {
        float f4 = data.getCurrentFriction();
        float f = 0.16277136f / (f4 * f4 * f4);
        float f5 = data.isLastOnGroundPacket() ? data.getAttributeSpeed() * f : (sprinting ? 0.025999999f : 0.02f);
        return f5;
    }

    public static double getMovementSpeed(List<WrapperPlayServerUpdateAttributes.PropertyModifier> collection, double base) {
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : PlayerUtil.getModifiers(Operation.ADDITION, collection)) {
            base += modifier.getAmount();
        }
        double moveSpeed = base;
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : PlayerUtil.getModifiers(Operation.MULTIPLY_BASE, collection)) {
            moveSpeed += base * modifier.getAmount();
        }
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : PlayerUtil.getModifiers(Operation.MULTIPLY_TOTAL, collection)) {
            moveSpeed *= 1.0 + modifier.getAmount();
        }
        return moveSpeed;
    }

    private static List<WrapperPlayServerUpdateAttributes.PropertyModifier> getModifiers(Operation operation, List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers) {
        ArrayList<WrapperPlayServerUpdateAttributes.PropertyModifier> results = new ArrayList<WrapperPlayServerUpdateAttributes.PropertyModifier>();
        for (WrapperPlayServerUpdateAttributes.PropertyModifier modifier : modifiers) {
            if (modifier.getUUID().equals(SPRINTING_SPEED_BOOST) || PlayerUtil.getOperation(modifier.getOperation()) != operation) continue;
            results.add(modifier);
        }
        return results;
    }

    private static Operation getOperation(WrapperPlayServerUpdateAttributes.PropertyModifier.Operation operation) {
        switch (operation) {
            case ADDITION: {
                return Operation.ADDITION;
            }
            case MULTIPLY_BASE: {
                return Operation.MULTIPLY_BASE;
            }
            case MULTIPLY_TOTAL: {
                return Operation.MULTIPLY_TOTAL;
            }
        }
        return null;
    }

    public static float getJumpHeight(KarhuPlayer data, float base) {
        return base + PlayerUtil.getJumpBooster(data);
    }

    public static float getJumpHeight(KarhuPlayer data) {
        return Math.max(0.0f, 0.42f * data.getJumpFactor() + PlayerUtil.getJumpBooster(data, false));
    }

    public static float getJumpBooster(KarhuPlayer data) {
        return (float)PlayerUtil.getJumpBoostLevel(data) * 0.1f;
    }

    public static float getJumpBooster(KarhuPlayer data, boolean maxed) {
        return (float)PlayerUtil.getJumpBoostLevel(data, maxed) * 0.1f;
    }

    public static int getJumpBoostLevel(KarhuPlayer data) {
        return Math.max(0, Math.max(data.getJumpBoost(), data.getCacheBoost()));
    }

    public static int getJumpBoostLevel(KarhuPlayer data, boolean maxed) {
        return maxed ? Math.max(0, Math.max(data.getJumpBoost(), data.getCacheBoost())) : Math.max(data.getJumpBoost(), data.getCacheBoost());
    }

    public static float getBaseSpeedAttribute(KarhuPlayer data, float mult) {
        return data.getWalkSpeed() * mult;
    }

    public static float getBaseSpeedAirAttribute(KarhuPlayer data, float multi) {
        return 0.35f + data.getAttributeSpeed() * multi;
    }

    public static void sendPacket(Player player, PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, wrapper);
    }

    public static void sendPacket(Player player, short id) {
        if (Karhu.PING_PONG_MODE) {
            WrapperPlayServerPing ping = new WrapperPlayServerPing(id);
            PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, ping);
        } else {
            WrapperPlayServerWindowConfirmation transaction = new WrapperPlayServerWindowConfirmation(0, id, false);
            PacketEvents.getAPI().getPlayerManager().sendPacket((Object)player, transaction);
        }
    }

    public static void sendPacket(Object channel, PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(channel, wrapper);
    }

    public static boolean isGeyserPlayer(Player player) {
        return !Karhu.getInstance().isFloodgate() ? false : FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

    public static boolean isGeyserPlayer(UUID uuid) {
        return !Karhu.getInstance().isFloodgate() ? false : FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }

    public static enum Operation {
        ADDITION,
        MULTIPLY_BASE,
        MULTIPLY_TOTAL;

    }
}

