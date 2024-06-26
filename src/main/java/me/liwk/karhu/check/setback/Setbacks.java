/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 */
package me.liwk.karhu.check.setback;

import com.github.retrooper.packetevents.protocol.world.BlockFace;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.MathHelper;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import me.liwk.karhu.util.player.BlockUtil;
import me.liwk.karhu.util.task.Tasker;
import org.bukkit.Location;

public final class Setbacks {
    public static Location forgeToRotatedLocation(Location loc, KarhuPlayer data) {
        Location locMod = loc.clone();
        locMod.setYaw(data.getLocation().yaw);
        locMod.setPitch(data.getLocation().pitch);
        return locMod;
    }

    public static CustomLocation forgeToRotatedLocation(CustomLocation loc, KarhuPlayer data) {
        CustomLocation locMod = loc.clone();
        locMod.setYaw(data.getLocation().yaw);
        locMod.setPitch(data.getLocation().pitch);
        return locMod;
    }

    public static Location moveOutOfBlockSafely(double x, double z, KarhuPlayer data) {
        int blockZ;
        int blockX = MathHelper.floor(x);
        if (!Setbacks.suffocatesAt(blockX, blockZ = MathHelper.floor(z), data)) {
            return null;
        }
        double relativeXMovement = x - (double)blockX;
        double relativeZMovement = z - (double)blockZ;
        BlockFace direction = null;
        double lowestValue = Double.MAX_VALUE;
        for (BlockFace direction2 : new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH}) {
            boolean doesSuffocate;
            double d7 = direction2 != BlockFace.WEST && direction2 != BlockFace.EAST ? relativeZMovement : relativeXMovement;
            double d6 = direction2 != BlockFace.EAST && direction2 != BlockFace.SOUTH ? d7 : 1.0 - d7;
            switch (direction2) {
                case EAST: {
                    doesSuffocate = Setbacks.suffocatesAt(blockX + 1, blockZ, data);
                    break;
                }
                case WEST: {
                    doesSuffocate = Setbacks.suffocatesAt(blockX - 1, blockZ, data);
                    break;
                }
                case NORTH: {
                    doesSuffocate = Setbacks.suffocatesAt(blockX, blockZ - 1, data);
                    break;
                }
                default: {
                    doesSuffocate = Setbacks.suffocatesAt(blockX, blockZ + 1, data);
                }
            }
            if (d6 >= lowestValue || doesSuffocate) continue;
            lowestValue = d6;
            direction = direction2;
        }
        if (direction != null) {
            Location locAddition;
            Location loc = data.getLocation().toLocation(data.getWorld());
            if (direction != BlockFace.WEST && direction != BlockFace.EAST) {
                Location locSubtract = loc.clone();
                locAddition = loc.clone();
                locSubtract.setZ(loc.getZ() - 0.1 * (double)direction.getModZ());
                locAddition.setZ(loc.getZ() + 0.1 * (double)direction.getModZ());
                if (BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                    Tasker.run(() -> data.getBukkitPlayer().teleport(locSubtract));
                    return locSubtract;
                }
                if (BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                    Tasker.run(() -> data.getBukkitPlayer().teleport(locAddition));
                    return locAddition;
                }
            } else {
                Location locSubtract = loc.clone();
                locAddition = loc.clone();
                locSubtract.setX(loc.getX() - 0.1 * (double)direction.getModX());
                locAddition.setX(loc.getX() + 0.1 * (double)direction.getModX());
                if (BlockUtil.chunkLoaded(locSubtract) && !locSubtract.getBlock().getType().isSolid()) {
                    Tasker.run(() -> data.getBukkitPlayer().teleport(locSubtract));
                    return locSubtract;
                }
                if (BlockUtil.chunkLoaded(locAddition) && !locAddition.getBlock().getType().isSolid()) {
                    Tasker.run(() -> data.getBukkitPlayer().teleport(locAddition));
                    return locAddition;
                }
            }
        }
        return null;
    }

    public static boolean suffocatesAt(int x, int z, KarhuPlayer data) {
        BoundingBox boundingBox = new BoundingBox(data, x, data.getBoundingBox().minY, z, (double)x + 1.0, data.getBoundingBox().maxY, (double)z + 1.0).expand(-1.0E-7);
        return !boundingBox.getCollidingBlocks().isEmpty();
    }
}

