/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.block.Block
 */
package me.liwk.karhu.handler.collision;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.handler.collision.enums.CollisionType;
import me.liwk.karhu.handler.collision.type.MaterialChecks;
import me.liwk.karhu.handler.interfaces.AbstractPredictionHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class BlockCollisionHandler {
    public static void run(Block b, CollisionType type, AbstractPredictionHandler data) {
        if (Karhu.SERVER_VERSION.getProtocolVersion() >= 47 && b.getType() != Material.AIR) {
            switch (type) {
                case LANDED: {
                    if (!MaterialChecks.SLIME.contains(b.getType()) || !(Math.abs(data.motY) < 0.1) || data.getData().isSneaking()) break;
                    double d0 = 0.4 + Math.abs(data.motY) * 0.2;
                    data.motX *= d0;
                    data.motZ *= d0;
                }
                case WALKING: {
                    if (MaterialChecks.SLIME.contains(b.getType())) {
                        if (data.getData().isSneaking()) {
                            data.motY = 0.0;
                            break;
                        }
                        if (!(data.motY < 0.0)) break;
                        data.motY = -data.motY;
                        break;
                    }
                    data.motY = 0.0;
                }
            }
        }
    }
}

