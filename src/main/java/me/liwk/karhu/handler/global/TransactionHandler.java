/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.apache.commons.math3.util.FastMath
 *  org.bukkit.ChatColor
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.util.Vector
 */
package me.liwk.karhu.handler.global;

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.data.EntityData;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.RespawnEvent;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.Teleport;
import me.liwk.karhu.util.TeleportPosition;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.pending.VelocityPending;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.task.Tasker;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TransactionHandler {
    public static com.github.retrooper.packetevents.protocol.entity.data.EntityData getIndex(List<com.github.retrooper.packetevents.protocol.entity.data.EntityData> objects, int index) {
        for (com.github.retrooper.packetevents.protocol.entity.data.EntityData object : objects) {
            if (object.getIndex() != index) continue;
            return object;
        }
        return null;
    }

    public void handleTransaction(short number, long nanoTime, KarhuPlayer data) {
        if (data.getScheduledTransactions().containsKey(number)) {
            ++data.sentConfirms;
        }
        boolean wasFirst = false;
        if (number == 6688) {
            wasFirst = !data.sentPingRequest;
            data.sentPingRequest = true;
        }
        if (number >= -20000 && number <= -3000 || wasFirst) {
            data.sentPingRequest = true;
            if (data.hasSentTickFirst) {
                data.hasSentTickFirst = false;
            } else {
                data.hasSentTickFirst = true;
                data.transactionTime.put(number, nanoTime);
                data.useOldTransaction(uid -> data.setServerTick(data.getServerTick() + 1L), number);
            }
            data.sendingPledgePackets = true;
        } else if (!data.sendingPledgePackets && data.getTotalTicks() > 300) {
            Tasker.run(() -> data.getBukkitPlayer().kickPlayer(ChatColor.translateAlternateColorCodes((char)'&', (String)Karhu.getInstance().getConfigManager().getUninjectedKick()) + " (Time out)"));
        }
        int absT = Math.abs(data.getCurrentServerTransaction());
        int absTL = Math.abs(number);
        if (absT - absTL > 1 && absT - absTL < 50 && Karhu.getInstance().getConfigManager().isNethandler() && (Karhu.getInstance().getConfigManager().isDelay() || Karhu.getInstance().getConfigManager().isSpoof())) {
            Karhu.getInstance().printCool("&b> &fTransactions have been skipped, proxy issue? " + absT + " | " + absTL);
        }
        if (number < 0) {
            data.setCurrentServerTransaction(number);
        }
        data.getNetHandler().handleServerTransaction(number, nanoTime);
        data.setFirstTransactionSent(true);
    }

    public void handlePacketPlaySend(PacketPlaySendEvent e, long nanoTime, KarhuPlayer data) {
        PacketType.Play.Server type = e.getPacketType();
        if (data.isObjectLoaded()) {
            switch (type) {
                case PLAYER_POSITION_AND_LOOK: {
                    WrapperPlayServerPlayerPositionAndLook position = new WrapperPlayServerPlayerPositionAndLook(e);
                    Vector3d pos = new Vector3d(position.getX(), position.getY(), position.getZ());
                    CustomLocation locationPlayer = data.getLocation();
                    if (!data.isNewerThan8()) {
                        if (position.isRelativeFlag(RelativeFlag.X)) {
                            pos = pos.add(new Vector3d(locationPlayer.x, 0.0, 0.0));
                        }
                        if (position.isRelativeFlag(RelativeFlag.Y)) {
                            pos = pos.add(new Vector3d(0.0, locationPlayer.y, 0.0));
                        }
                        if (position.isRelativeFlag(RelativeFlag.Z)) {
                            pos = pos.add(new Vector3d(0.0, 0.0, locationPlayer.z));
                        }
                        position.setX(pos.getX());
                        position.setY(pos.getY());
                        position.setZ(pos.getZ());
                        position.setRelativeMask((byte)0);
                    }
                    boolean checkBB = Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_7_10);
                    double x = pos.getX();
                    double y = !checkBB ? pos.getY() : pos.getY() - (double)1.62f;
                    double z = pos.getZ();
                    Teleport teleport = new Teleport(new TeleportPosition(x, y, z));
                    data.queueToPrePing(uid -> {
                        data.getTeleportManager().locations.add(teleport);
                        data.setInventoryOpen(false);
                    });
                    data.queueToPostPing(uid -> data.queueToFlying(1, tick -> {
                        data.getTeleportManager().locations.remove(teleport);
                        --data.getTeleportManager().teleportsPending;
                    }));
                    data.setLastTeleportPacket(data.getServerTick());
                    break;
                }
                case ENTITY_VELOCITY: {
                    WrapperPlayServerEntityVelocity velocity = new WrapperPlayServerEntityVelocity(e);
                    Vector3d vecVelo = velocity.getVelocity();
                    if (velocity.getEntityId() != e.getUser().getEntityId()) break;
                    int tickToUse = data.getCurrentServerTransaction();
                    data.setConfirmingVelocity(true);
                    data.queueToPrePing(uid -> {
                        int uidInt = uid;
                        if (uidInt != tickToUse) {
                            Karhu.getInstance().printCool("&b> &fDEBUG: " + uidInt + "/" + tickToUse);
                        }
                        Vector vector = new Vector(vecVelo.getX(), vecVelo.getY(), vecVelo.getZ());
                        ConcurrentLinkedDeque<VelocityPending> velocities = data.getVelocityPending().getOrDefault(uidInt, new ConcurrentLinkedDeque());
                        velocities.add(new VelocityPending((short)tickToUse, vector, false));
                        data.getVelocityPending().put(uidInt, velocities);
                    });
                    data.queueToPostPing(uid -> {
                        ConcurrentLinkedDeque<VelocityPending> velos = data.getTickVelocities(tickToUse);
                        if (velos != null) {
                            for (VelocityPending velocityPending : velos) {
                                if (!velocityPending.isMarkedSent()) {
                                    data.velocityTick(velocityPending.getVelocity());
                                }
                                velos.remove(velocityPending);
                                if (!velos.isEmpty()) continue;
                                data.getVelocityPending().remove(tickToUse);
                            }
                        }
                    });
                    data.setPlayerVelocityCalled(false);
                    data.setPlayerExplodeCalled(false);
                    break;
                }
                case EXPLOSION: {
                    WrapperPlayServerExplosion explosion = new WrapperPlayServerExplosion(e);
                    Vector3f playerMotion = explosion.getPlayerMotion();
                    if (playerMotion.getX() == 0.0f && playerMotion.getY() == 0.0f && playerMotion.getZ() == 0.0f) {
                        return;
                    }
                    data.queueToPrePing(uid -> {
                        data.setLastVelocityTaken(data.getTotalTicks());
                        data.setVelocityXZTicks(0);
                        data.setVelocityYTicks(0);
                        data.setVelocityX(playerMotion.getX());
                        data.setVelocityY(playerMotion.getY());
                        data.setVelocityZ(playerMotion.getZ());
                        int velocityH = (int)Math.ceil((double)(Math.abs(playerMotion.getX()) + Math.abs(playerMotion.getZ())) / 2.0 + 2.0) * 4;
                        int velocityV = (int)Math.ceil(FastMath.pow((double)(Math.abs(playerMotion.getY()) + 2.0f), (int)2)) * 2;
                        data.setMaxVelocityXZTicks(velocityH + velocityV + 5);
                        data.setMaxVelocityYTicks(velocityV);
                        data.setTakingVertical(true);
                        data.setNeedExplosionAdditions(true);
                        data.setVelocityHorizontal(MathUtil.hypot(playerMotion.getX(), playerMotion.getZ()));
                        data.setConfirmingVelocity(true);
                    });
                    data.queueToPostPing(uid -> data.setConfirmingVelocity(false));
                    break;
                }
                case ENTITY_ROTATION: {
                    WrapperPlayServerEntityRotation look = new WrapperPlayServerEntityRotation(e);
                    data.queueToPrePing(uid -> EntityLocationHandler.updateEntityLook(data, look.getEntityId()));
                    break;
                }
                case ENTITY_RELATIVE_MOVE_AND_ROTATION: 
                case ENTITY_RELATIVE_MOVE: {
                    double deltaZ;
                    double deltaY;
                    double deltaX;
                    int entityId;
                    if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
                        WrapperPlayServerEntityRelativeMove entity = new WrapperPlayServerEntityRelativeMove(e);
                        entityId = entity.getEntityId();
                        deltaX = entity.getDeltaX();
                        deltaY = entity.getDeltaY();
                        deltaZ = entity.getDeltaZ();
                    } else {
                        WrapperPlayServerEntityRelativeMoveAndRotation entity = new WrapperPlayServerEntityRelativeMoveAndRotation(e);
                        entityId = entity.getEntityId();
                        deltaX = entity.getDeltaX();
                        deltaY = entity.getDeltaY();
                        deltaZ = entity.getDeltaZ();
                    }
                    if (!(SpigotReflectionUtil.getEntityById(entityId) instanceof Player)) break;
                    data.queueToPrePing(uid -> EntityLocationHandler.updateEntityRelMove2(data, entityId, deltaX, deltaY, deltaZ));
                    data.queueToPostPing(uid -> {
                        EntityData edata = data.getEntityData().get(entityId);
                        if (edata != null) {
                            edata.postTransaction();
                        }
                    });
                    break;
                }
                case SPAWN_LIVING_ENTITY: {
                    WrapperPlayServerSpawnLivingEntity wrapper = new WrapperPlayServerSpawnLivingEntity(e);
                    double newX = wrapper.getPosition().getX();
                    double newY = wrapper.getPosition().getY();
                    double newZ = wrapper.getPosition().getZ();
                    data.queueToPrePing(uid -> EntityLocationHandler.addEntity(data, wrapper.getEntityType(), newX, newY, newZ, wrapper.getEntityId()));
                    break;
                }
                case SPAWN_ENTITY: {
                    WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(e);
                    double newX = wrapper.getPosition().getX();
                    double newY = wrapper.getPosition().getY();
                    double newZ = wrapper.getPosition().getZ();
                    data.queueToPrePing(uid -> EntityLocationHandler.addEntity(data, wrapper.getEntityType(), newX, newY, newZ, wrapper.getEntityId()));
                    break;
                }
                case SPAWN_PLAYER: {
                    WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(e);
                    double newX = wrapper.getPosition().getX();
                    double newY = wrapper.getPosition().getY();
                    double newZ = wrapper.getPosition().getZ();
                    data.queueToPrePing(uid -> EntityLocationHandler.addEntity(data, EntityTypes.PLAYER, newX, newY, newZ, wrapper.getEntityId()));
                    break;
                }
                case ENTITY_TELEPORT: {
                    WrapperPlayServerEntityTeleport teleport = new WrapperPlayServerEntityTeleport(e);
                    Vector3d pos = teleport.getPosition();
                    int entityId = teleport.getEntityId();
                    data.queueToPrePing(uid -> {
                        EntityData edata = data.getEntityData().get(entityId);
                        if (edata != null) {
                            if (Math.abs(edata.newX - pos.getX()) < 0.03125 && Math.abs(edata.newY - pos.getY()) < 0.015625 && Math.abs(edata.newZ - pos.getZ()) < 0.03125) {
                                EntityLocationHandler.updateEntityTeleport2(data, entityId, edata.newX, edata.newY, edata.newZ);
                            } else {
                                EntityLocationHandler.updateEntityTeleport2(data, entityId, pos.getX(), pos.getY(), pos.getZ());
                            }
                        }
                    });
                    data.queueToPostPing(uid -> {
                        EntityData edata = data.getEntityData().get(entityId);
                        if (edata != null) {
                            edata.postTransaction();
                        }
                    });
                    break;
                }
                case DESTROY_ENTITIES: {
                    WrapperPlayServerDestroyEntities wrapper = new WrapperPlayServerDestroyEntities(e);
                    int[] entityIds = wrapper.getEntityIds();
                    data.queueToPrePing(uid -> EntityLocationHandler.destroyEntity(data, entityIds));
                    break;
                }
                case USE_BED: {
                    WrapperPlayServerUseBed bed = new WrapperPlayServerUseBed(e);
                    if (data.getEntityId() != bed.getEntityId()) break;
                    data.queueToPrePing(uid -> {
                        data.setInBed(true);
                        data.setBedPos(new Vec3((double)bed.getPosition().getX() + 0.5, bed.getPosition().getY(), (double)bed.getPosition().getZ() + 0.5));
                    });
                    break;
                }
                case ENTITY_ANIMATION: {
                    WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(e);
                    if (data.getEntityId() != animation.getEntityId() || animation.getType() != WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP) break;
                    data.queueToPrePing(uid -> data.setInBed(false));
                    break;
                }
                case OPEN_WINDOW: {
                    data.queueToPrePing(uid -> {
                        data.setInventoryOpen(true);
                        data.setInvStamp(data.getTotalTicks());
                        data.setUsingItem(false);
                        data.setEating(false);
                    });
                    break;
                }
                case CLOSE_WINDOW: {
                    data.queueToPrePing(uid -> {
                        data.setInventoryOpen(false);
                        data.setInvStamp(data.getTotalTicks());
                    });
                    break;
                }
                case ENTITY_EFFECT: {
                    WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(e);
                    int entityId = wrapper.getEntityId();
                    if (data.getEntityId() != entityId) {
                        return;
                    }
                    int effectId = wrapper.getPotionType().getId(data.getClientVersion());
                    int amplifier = wrapper.getEffectAmplifier();
                    data.queueToPrePing(uid -> data.getEffectManager().addPotionEffect(effectId, amplifier));
                    break;
                }
                case REMOVE_ENTITY_EFFECT: {
                    WrapperPlayServerRemoveEntityEffect wrapper = new WrapperPlayServerRemoveEntityEffect(e);
                    if (data.getEntityId() != wrapper.getEntityId()) {
                        return;
                    }
                    data.queueToPrePing(uid -> data.getEffectManager().removePotionEffect(wrapper.getPotionType().getId(data.getClientVersion())));
                    break;
                }
                case UPDATE_ATTRIBUTES: {
                    WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(e);
                    if (packet.getEntityId() != data.getEntityId()) {
                        return;
                    }
                    data.queueToPrePing(task -> {
                        for (WrapperPlayServerUpdateAttributes.Property property : packet.getProperties()) {
                            if (!property.getKey().startsWith("minecraft:generic.movement") && !property.getKey().startsWith("generic.movementSpeed")) continue;
                            data.setWalkSpeed((float)PlayerUtil.getMovementSpeed(property.getModifiers(), property.getValue()));
                        }
                    });
                    break;
                }
                case ENTITY_METADATA: {
                    WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(e);
                    if (packet.getEntityId() != data.getEntityId() || !Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_14)) break;
                    int id = 12;
                    if (Karhu.SERVER_VERSION.isOlderThanOrEquals(ServerVersion.V_1_16_5)) {
                        id = 13;
                    } else if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_17)) {
                        id = 14;
                    }
                    com.github.retrooper.packetevents.protocol.entity.data.EntityData bedBlock = TransactionHandler.getIndex(packet.getEntityMetadata(), id);
                    if (bedBlock == null) break;
                    data.queueToPrePing(task -> {
                        Optional bedBlockValue = (Optional)bedBlock.getValue();
                        if (bedBlockValue.isPresent()) {
                            Vector3i bedPos = (Vector3i)bedBlockValue.get();
                            data.setInBed(true);
                            data.setBedPos(new Vec3((double)bedPos.getX() + 0.5, bedPos.getY(), (double)bedPos.getZ() + 0.5));
                        } else {
                            data.setInBed(false);
                        }
                    });
                    break;
                }
                case JOIN_GAME: {
                    WrapperPlayServerJoinGame packet = new WrapperPlayServerJoinGame(e);
                    if (packet.getGameMode() != null) {
                        data.gameMode = packet.getGameMode();
                        data.setSpectating(data.getGameMode() == GameMode.SPECTATOR);
                    }
                    data.setEntityId(packet.getEntityId());
                    break;
                }
                case CHANGE_GAME_STATE: {
                    WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(e);
                    if (packet.getReason() != WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE) break;
                    data.queueToPrePing(uid -> {
                        data.gameMode = GameMode.values()[(int)packet.getValue()];
                        data.setSpectating(data.getGameMode() == GameMode.SPECTATOR);
                    });
                    break;
                }
                case PLAYER_ABILITIES: {
                    WrapperPlayServerPlayerAbilities packet = new WrapperPlayServerPlayerAbilities(e);
                    data.getAbilityManager().onAbilityServer(packet);
                    break;
                }
                case HELD_ITEM_CHANGE: {
                    WrapperPlayServerHeldItemChange packet = new WrapperPlayServerHeldItemChange(e);
                    data.queueToPrePing(uid -> {
                        data.lastServerSlot = packet.getSlot();
                        data.setCurrentSlot(packet.getSlot());
                        data.setUsingItem(false);
                        data.setEating(false);
                    });
                    break;
                }
                case ATTACH_ENTITY: {
                    WrapperPlayServerAttachEntity packet = new WrapperPlayServerAttachEntity(e);
                    if (Karhu.SERVER_VERSION.isNewerThanOrEquals(ServerVersion.V_1_9)) {
                        return;
                    }
                    if (packet.isLeash()) break;
                    if (packet.getAttachedId() == data.getEntityId()) {
                        int vehicleId = packet.getHoldingId();
                        Entity entity = SpigotReflectionUtil.getEntityById(vehicleId);
                        boolean riding = vehicleId != -1;
                        data.queueToPrePing(uid -> {
                            data.setRiding(riding);
                            data.setLastUnmount(!riding ? data.getTotalTicks() : data.getLastUnmount());
                            data.setVehicleId(vehicleId);
                            data.setVehicle(entity);
                        });
                        data.setRidingUncertain(vehicleId != -1);
                        break;
                    }
                    int vehicleId = packet.getHoldingId();
                    boolean riding = vehicleId != -1;
                    data.queueToPrePing(uid -> {
                        EntityData edata = data.getEntityData().get(packet.getAttachedId());
                        if (edata != null) {
                            edata.setVehicleId(vehicleId);
                            edata.setRiding(riding);
                        }
                    });
                    break;
                }
                case SET_PASSENGERS: {
                    WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(e);
                    data.queueToPrePing(uid -> {
                        EntityData eData = data.getEntityData().get(data.getEntityId());
                        if (packet.getPassengers().length == 0) {
                            data.setRiding(false);
                            data.setLastUnmount(data.getTotalTicks());
                            data.setVehicleId(-1);
                            data.setVehicle(null);
                            if (eData != null) {
                                eData.setRiding(false);
                                eData.setVehicleId(-1);
                            }
                        }
                        for (int passengerId : packet.getPassengers()) {
                            EntityData passengerData = data.getEntityData().get(passengerId);
                            if (passengerId == data.getEntityId()) {
                                data.setRiding(true);
                                data.setVehicleId(packet.getEntityId());
                                data.setVehicle(SpigotReflectionUtil.getEntityById(packet.getEntityId()));
                            }
                            if (passengerData == null) continue;
                            passengerData.setRiding(true);
                            passengerData.setVehicleId(packet.getEntityId());
                        }
                    });
                    break;
                }
                case RESPAWN: {
                    data.queueToPrePing(uid -> data.getCheckManager().runChecks(data.getCheckManager().getPacketChecks(), new RespawnEvent(), null));
                    break;
                }
                case KEEP_ALIVE: {
                    data.setLastPingTime(nanoTime);
                }
            }
        }
    }

    public void handlePlayReceive(PacketPlayReceiveEvent e, long nanoTime, KarhuPlayer data) {
        PacketType.Play.Client type = e.getPacketType();
        long now = e.getTimestamp();
        switch (type) {
            case PONG: 
            case WINDOW_CONFIRMATION: {
                ObjectArrayList<Consumer<Short>> packetList;
                short number = type == PacketType.Play.Client.PONG ? (short)new WrapperPlayClientPong(e).getId() : new WrapperPlayClientWindowConfirmation(e).getActionId();
                data.setLastLastClientTransaction(data.getLastClientTransaction());
                data.setLastClientTransaction(data.getCurrentClientTransaction());
                if (number <= -3000) {
                    data.setCurrentClientTransaction(number);
                }
                if (data.getScheduledTransactions().containsKey(number)) {
                    ((Consumer)data.getScheduledTransactions().remove(number)).accept(number);
                    ++data.receivedConfirms;
                }
                if ((packetList = data.getWaitingConfirms().get(number)) != null && !packetList.isEmpty()) {
                    for (Consumer consumer : packetList) {
                        consumer.accept(number);
                    }
                    packetList.clear();
                    data.getWaitingConfirms().remove(number);
                }
                Deque<Integer> slots = data.getBackSwitchSlots().get(number);
                if (data.isPendingBackSwitch() && slots != null) {
                    data.setPendingBackSwitch(false);
                    if (slots.peekFirst() != null) {
                        int slot = (Integer)slots.peekFirst();
                        PlayerUtil.sendPacket(data.getBukkitPlayer(), new WrapperPlayServerHeldItemChange(slot));
                    }
                    slots.remove(number);
                }
                if (data.getTransactionTime().containsKey(number)) {
                    long transactionStamp = data.getTransactionTime().get(number);
                    if (!data.isHasReceivedTransaction()) {
                        data.setTransactionClock(transactionStamp);
                    }
                    data.setHasReceivedTransaction(true);
                    data.setLastTransactionPing(data.getTransactionPing());
                    data.setTransactionPing(TimeUnit.NANOSECONDS.toMillis(nanoTime - transactionStamp));
                    data.lastC0F = transactionStamp;
                    data.getTransactionTime().remove(number);
                    data.setLastTransactionPingUpdate(transactionStamp);
                    data.setPingInTicks(Math.min(15, MathUtil.getPingInTicks(data.getTransactionPing() + 50L)));
                }
                data.getNetHandler().handleClientTransaction(number);
                data.setLastTransaction(now);
            }
        }
    }
}

