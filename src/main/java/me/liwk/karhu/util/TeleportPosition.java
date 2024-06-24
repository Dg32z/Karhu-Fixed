/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.World
 *  org.bukkit.util.NumberConversions
 *  org.bukkit.util.Vector
 *  org.jetbrains.annotations.NotNull
 */
package me.liwk.karhu.util;

import me.liwk.karhu.util.location.CustomLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class TeleportPosition {
    protected final double x;
    protected final double y;
    protected final double z;

    public TeleportPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double horizontal(Vector vector) {
        return Math.sqrt(NumberConversions.square((double)(this.x - vector.getX())) + NumberConversions.square((double)(this.z - vector.getZ())));
    }

    public double distance(Vector vector) {
        return Math.sqrt(NumberConversions.square((double)(this.x - vector.getX())) + NumberConversions.square((double)(this.y - vector.getY())) + NumberConversions.square((double)(this.z - vector.getZ())));
    }

    public double vertical(Vector vector) {
        return Math.sqrt(NumberConversions.square((double)(this.y - vector.getY())));
    }

    public String toString() {
        return "X " + this.x + ", Y " + this.y + ", Z " + this.z;
    }

    @NotNull
    public Location toLocation(@NotNull World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    @NotNull
    public CustomLocation toCLocation() {
        return new CustomLocation(this.x, this.y, this.z);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }
}

