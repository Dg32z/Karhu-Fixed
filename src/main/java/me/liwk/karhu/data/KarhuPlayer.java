package me.liwk.karhu.data;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.CheckManager;
import me.liwk.karhu.handler.AbilityManager;
import me.liwk.karhu.handler.MovementHandler;
import me.liwk.karhu.handler.VehicleHandler;
import me.liwk.karhu.handler.collision.CollisionHandler;
import me.liwk.karhu.handler.crash.CrashHandler;
import me.liwk.karhu.handler.global.DesyncedBlockHandler;
import me.liwk.karhu.handler.global.EffectManager;
import me.liwk.karhu.handler.global.PlaceManager;
import me.liwk.karhu.handler.global.TeleportManager;
import me.liwk.karhu.handler.interfaces.AbstractPredictionHandler;
import me.liwk.karhu.handler.interfaces.ICrashHandler;
import me.liwk.karhu.handler.interfaces.IVehicleHandler;
import me.liwk.karhu.handler.interfaces.KarhuHandler;
import me.liwk.karhu.handler.net.NetHandler;
import me.liwk.karhu.handler.net.TaskData;
import me.liwk.karhu.manager.alert.MiscellaneousAlertPoster;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.benchmark.Benchmark;
import me.liwk.karhu.util.benchmark.BenchmarkType;
import me.liwk.karhu.util.benchmark.KarhuBenchmarker;
import me.liwk.karhu.util.gui.Callback;
import me.liwk.karhu.util.location.CustomLocation;
import me.liwk.karhu.util.mc.boundingbox.BoundingBox;
import me.liwk.karhu.util.mc.vec.Vec3;
import me.liwk.karhu.util.pair.Pair;
import me.liwk.karhu.util.pending.VelocityPending;
import me.liwk.karhu.util.player.PlayerUtil;
import me.liwk.karhu.util.task.Tasker;
import me.liwk.karhu.util.thread.Thread;
import me.liwk.karhu.world.nms.NMSValueParser;
import me.liwk.karhu.world.nms.wrap.WrappedEntityPlayer;
import me.liwk.karhu.world.packet.KarhuWorld;
import me.liwk.karhu.world.packet.WorldTracker;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class KarhuPlayer {
   private boolean objectLoaded;
   private boolean removingObject = false;
   private boolean configChecked;
   private IVehicleHandler vehicleHandler;
   private UUID uuid;
   private boolean kicked;
   public boolean sentPingRequest = false;
   private CustomLocation location;
   private CustomLocation lastLocation;
   private CustomLocation lastLastLocation;
   private CustomLocation lastLastLastLocation;
   private boolean forceRunCollisions;
   private boolean placedInside;
   private boolean placedCancel;
   private int lastPlacedInside;
   private int predictionTicks;
   private int moveTicks;
   private int noMoveTicks;
   private PlaceManager placeManager;
   private final Map<Integer, TaskData> tasks = new LinkedHashMap<>();
   private CheckManager checkManager;
   private Map<Check, Set<Long>> checkViolationTimes;
   private Map<Check, Double> checkVlMap;
   private boolean cancelNextHitR;
   private boolean cancelNextHitH;
   private boolean reduceNextDamage;
   private boolean cancelBreak;
   private boolean forceCancelReach;
   private boolean cancelTripleHit;
   private boolean abusingVelocity;
   private int entityIdCancel;
   private int cancelHitsTick;
   private float jumpMovementFactor = 0.02F;
   private float speedInAir = 0.02F;
   private float lastJumpMovementFactor;
   private float attributeSpeed = 0.1F;
   private float lastAttributeSpeed;
   private float walkSpeed = 0.1F;
   private float jumpFactor;
   private boolean jumpedCurrentTick;
   private boolean jumpedLastTick;
   private boolean jumped;
   private String pressedKey;
   private Block blockBelow;
   private Block blockInside;
   private Block lastBlockInside;
   private boolean exitingVehicle;
   private String keyCombo;
   private ClientVersion clientVersion;
   private boolean isNewerThan8;
   private boolean isNewerThan12;
   private boolean isNewerThan13;
   private boolean isNewerThan16;
   private boolean confirmedVersion;
   private boolean confirmedVersion2;
   private boolean isViaMCP;
   private KarhuHandler collisionHandler;
   private DesyncedBlockHandler desyncedBlockHandler;
   private MovementHandler movementHandler;
   private AbstractPredictionHandler predictionHandler;
   private NetHandler netHandler;
   private ICrashHandler crashHandler;
   private WrappedEntityPlayer wrappedEntity;
   private boolean hasReceivedTransaction = false;
   private boolean hasReceivedKeepalive = false;
   private boolean isHasTeleportedOnce = false;
   public int sentConfirms;
   public int receivedConfirms;
   private final Short2ObjectMap<Consumer<Short>> scheduledTransactions = new Short2ObjectArrayMap();
   public Map<Short, ObjectArrayList<Consumer<Short>>> waitingConfirms = new HashMap<>();
   private short transactionId;
   private short confirmId;
   private short tickTransactionId;
   private int tickFirstConfirmationUid = -1;
   private int tickSecondConfirmationUid = -2;
   private int lastTickFirstConfirmationUid = -1;
   private int lastTickSecondConfirmationUid = -2;
   public boolean hasSentTickFirst = false;
   public boolean sendingPledgePackets;
   public boolean hasSentFirst;
   public boolean brokenVelocityVerify;
   private final TaskManager<KarhuPlayer> tasksManager = new TaskManager<>();
   public short lastPostId = -100;
   public final Map<Integer, ConcurrentLinkedDeque<VelocityPending>> velocityPending = new HashMap<>();
   private boolean takingVertical;
   private boolean confirmingVelocity;
   private boolean needExplosionAdditions;
   private int lastVelocityYReset;
   private int lastVelocityXZReset;
   private int lastVelocityTaken;
   private int velocityYTicks;
   private int maxVelocityXZTicks;
   private int maxVelocityYTicks;
   private int explosionExempt;
   private double velocityX;
   private double velocityY;
   private double velocityZ;
   private double velocityHorizontal;
   private double confirmingY;
   private Vector tickedVelocity = null;
   private boolean playerVelocityCalled;
   private boolean playerExplodeCalled;
   private int totalTicks;
   private int lastSprintTick;
   private int lastSneakTick;
   private int airTicks;
   private int digTicks;
   private int fastDigTicks;
   private int digStopTicks;
   private int placeTicks;
   private int underPlaceTicks;
   private int bucketTicks;
   private int spoofPlaceTicks;
   private int useTicks;
   private int lClientAirTicks;
   private int clientAirTicks;
   private int lastFlyTick;
   private int lastAllowFlyTick;
   private int elapsedOnLiquid;
   private int elapsedUnderBlock;
   private int elapsedSinceBridgePlace;
   private int serverGroundTicks;
   private int clientGroundTicks;
   private int weirdTicks;
   private int lastInBerry;
   private int lastInUnloadedChunk;
   private int lastInLiquid;
   private int lastOnSlime;
   private int lastOnSoul;
   private int lastOnIce;
   private int lastOnClimbable;
   private int lastOnBed;
   private int lastInWeb;
   private int lastCollided;
   private int lastCollidedWithEntity;
   private int lastOnHalfBlock;
   private int lastCollidedV;
   private int lastCollidedVGhost;
   private int lastOnBoat;
   private int lastInPowder;
   private int lastPossibleInUnloadedChunk;
   private int lastCollidedGhost;
   private int positionPackets;
   private int lastInLiquidOffset;
   private int lastConfirmingState;
   private int lastCollidedH;
   private int lastSneakEdge;
   private int lastFence;
   private boolean digging;
   private boolean diggingBasic;
   private boolean traceDigging;
   private boolean placing;
   private boolean wasPlacing;
   private boolean skipNextSwing;
   private boolean noMoveNextFlying;
   private boolean collidedHorizontally;
   private boolean wasCollidedHorizontally;
   private boolean collidedHorizontalClient;
   private boolean collidedWithFence;
   private boolean edgeOfFence;
   private boolean collidedWithPane;
   private boolean collidedWithCactus;
   private boolean insideTrapdoor;
   private boolean insideBlock;
   private boolean wasFullyInsideBlock;
   private boolean fullyInsideBlock;
   private boolean inWeb;
   private boolean isWasInWeb;
   private boolean isWasWasInWeb;
   private boolean blocking;
   private boolean shit;
   private boolean bowing;
   private boolean finalCollidedH;
   private Vector lastAbortLoc;
   private int lastPistonPush;
   private int lastSlimePistonPush;
   public int moveCalls;
   private long serverTick;
   private long createdOnTick;
   private BoundingBox boundingBox;
   private BoundingBox mcpCollision;
   private BoundingBox lastBoundingBox;
   private double vehicleX;
   private double vehicleY;
   private double vehicleZ;
   public int ticksOnGhostBlock;
   public int ticksOnBlockHandlerNotEnabled;
   public int updateBuf;
   private LivingEntity lastTarget;
   private List<Integer> lastTargets = new ArrayList<>();
   public int lastAttackTick = 99;
   public int attacks;
   private long lastAttackPacket;
   public boolean attackedSinceVelocity;
   private boolean reachBypass;
   private boolean sprinting;
   private boolean wasSprinting;
   private boolean wasWasSprinting;
   private boolean sneaking;
   private boolean wasSneaking;
   private boolean wasWasSneaking;
   private boolean usingItem;
   private boolean lastUsingItem;
   private boolean eating;
   private boolean lastEating;
   private boolean recorrectingSprint;
   private boolean desyncSprint = true;
   private boolean inventoryOpen;
   private boolean crouching;
   private boolean inBed;
   private boolean lastInBed;
   private Vec3 bedPos;
   private int invStamp;
   private int slotSwitchTick;
   private int lastWorldChange;
   public boolean cinematic;
   public int lastCinematic;
   public final Deltas deltas = new Deltas();
   public float fallDistance;
   public float lFallDistance;
   public ConcurrentHashMap<Integer, EntityData> entityData = new ConcurrentHashMap<>();
   public int lastPos;
   public double attackerX;
   public double attackerY;
   public double attackerZ;
   public float attackerYaw;
   public float attackerPitch;
   public int teleports;
   public int addedTeleports;
   private List<Double> eyesFourteen = Arrays.asList(0.4, 1.27, 1.62);
   private List<Double> eyesNine = Arrays.asList(0.4, 1.54, 1.62);

   private List<Double> eyesLegacy = Arrays.asList(1.54F, 1.62F).stream().map(Double::valueOf).collect(Collectors.toList());

   private boolean onGroundServer;
   private boolean onBoat;
   private boolean groundNearBox;
   private boolean wasOnGroundServer;
   private boolean onWater;
   private boolean wasOnWater;
   private boolean aboveButNotInWater;
   private boolean waterAlmostOnFeet;
   private boolean onLava;
   private boolean wasOnLava;
   private boolean onIce;
   private boolean onLiquid;
   private boolean onSlab;
   private boolean wasOnSlab;
   private boolean onDoor;
   private boolean wasOnDoor;
   private boolean onFence;
   private boolean wasOnFence;
   private boolean onStairs;
   private boolean wasOnStairs;
   private boolean onBed;
   private boolean wasOnBed;
   private boolean underBlock;
   private boolean underBlockStrict;
   private boolean wasUnderBlock;
   private boolean underWeb;
   private boolean onWeb;
   private boolean onSoulsand;
   private boolean wasOnSoulSand;
   private boolean wasSlimeLand;
   private boolean slimeLand;
   private boolean onSlime;
   private boolean wasOnSlime;
   private boolean wasWasOnSlime;
   private boolean onCarpet;
   private boolean onComparator;
   private boolean wasOnComparator;
   private boolean onClimbable;
   private boolean wasOnClimbable;
   private boolean wasWasOnClimbable;
   private boolean onLadder;
   private boolean lastLadder;
   private boolean nearClimbable;
   private boolean onHoney;
   private boolean onSweet;
   private boolean wasOnHoney;
   private boolean onScaffolding;
   private boolean onPiston;
   private boolean onTopGhostBlock;
   private boolean atButton;
   private boolean onWaterOffset;
   private boolean lastOnWaterOffset;
   private boolean inPowder;
   private boolean sneakEdge;
   private boolean lastBlockSneak;
   private boolean atSign;
   private Block movementBlock;
   private boolean collidedWithLivingEntity;
   private float currentFriction;
   private float lastTickFriction;
   private boolean onGroundPacket;
   private boolean lastOnGroundPacket;
   private boolean lastLastOnGroundPacket;
   private boolean onGroundMath;
   private boolean lastOnGroundMath;
   private boolean lastLastOnGroundMath;
   private boolean onGhostBlock;
   private boolean underGhostBlock;
   private boolean isWasUnderGhostBlock;
   private int lastInGhostLiquid;
   public double ghostBlockSetbacks;
   private long ping;
   private long lastPingTime;
   private long transactionPing;
   private long lastTransactionPing;
   public final Map<Short, Long> transactionTime = new ConcurrentHashMap<>();
   public short timerTransactionSent;
   private boolean inUnloadedChunk;
   private boolean wasInUnloadedChunk;
   private boolean wasWasInUnloadedChunk;
   private long lastTransaction;
   private long lastTransactionPingUpdate;
   public int badPingTicks;
   public int pingInTicks;
   private TeleportManager teleportManager;
   private boolean possiblyTeleporting;
   private boolean seventeenPlacing;
   private int lastTeleport;
   private long lastTeleportPacket;
   private CustomLocation firstChunkMove;
   private boolean joining;
   private boolean fuckedTeleport;
   private boolean banned;
   private Player bukkitPlayer;
   private User user;
   private int entityId = -1;
   private String brand = "vanilla (not set)";
   private String cleanBrand = "vanilla (not set)";
   private boolean brandPosted = false;
   private EffectManager effectManager;
   public int jumpBoost;
   public int cacheBoost;
   public int speedBoost;
   public int slowness;
   public int haste;
   public int fatigue;
   public GameMode gameMode;
   public boolean allowFlying;
   public boolean flyingS;
   public boolean flyingC;
   public boolean flying;
   public boolean wasFlyingC;
   public boolean initedFlying;
   public boolean confirmingFlying;
   public boolean correctedFly;
   private AbilityManager abilityManager;
   private WorldTracker worldTracker;
   private KarhuWorld karhuWorld;
   public int lastServerSlot;
   public long lastFlying;
   public long flyingTime;
   public long lastJoinTime;
   public int lastFlyingTicks;
   public int velocityXZTicks;
   public int lastTransactionTick;
   public int trackCount;
   public int currentServerTransaction = -1;
   public int currentClientTransaction = -1;
   public int lastClientTransaction;
   public int lastLastClientTransaction;
   private boolean firstTransactionSent;
   public int lastDroppedPackets;
   public int lastPacketDrop;
   public int hurtTicks;
   private long lastFast;
   private boolean movementDesynced;
   private boolean riding;
   private boolean brokenVehicle;
   private boolean ridingUncertain;
   private int vehicleId;
   private Entity vehicle;
   private int lastUnmount;
   public final List<Pair<Integer, Integer>> exemptMap = new ArrayList<>();
   public final Map<SubCategory, Pair<Integer, Integer>> exemptCategoryMap = new HashMap<>();
   private double cps;
   private double lastCps;
   private double highestCps;
   private double highestReach;
   private boolean didFlagMovement;
   private int lastMovementFlag;
   private CustomLocation safeSetback;
   private CustomLocation safeGroundSetback;
   private CustomLocation flyCancel;
   private CustomLocation teleportLocation;
   private long lastLocationUpdate;
   public int invalidMovementTicks;
   private int sensitivity = -1;
   private float sensitivityY = -1.0F;
   private float sensitivityX = -1.0F;
   private float smallestRotationGCD;
   private float pitchGCD = 9999.0F;
   private float yawGCD;
   private float predictPitch;
   private float predictYaw;
   public Vec3 eyeLocation;
   public Vec3 look;
   public Vec3 lookMouseDelayFix;
   public boolean locationInited;
   public boolean boundingBoxInited;
   public int locationInitedAt;
   public long createdAt;
   public long transactionClock;
   private int currentSlot;
   private boolean pendingBackSwitch;
   public final Map<Integer, Deque<Integer>> backSwitchSlots = new HashMap<>();
   private boolean timerKicked = false;
   private Thread thread;
   private boolean gliding = false;
   private boolean riptiding = false;
   private boolean spectating = false;
   private int lastGlide = 0;
   private int lastRiptide = 0;
   public int dolphinLevel = 0;
   public int soulSpeedLevel = 0;
   public int depthStriderLevel = 0;
   public int slowFallingLevel = 0;
   public int levitationLevel = 0;
   public long lastC0F = 0;

   public KarhuPlayer(UUID uuid, Karhu karhu, long now) {
      this.createdAt = now;
      this.forceRunCollisions = true;
      this.karhuWorld = new KarhuWorld(this);
      this.thread = Karhu.getInstance().getThreadManager().generate();
      this.teleportManager = new TeleportManager(this);
      this.abilityManager = new AbilityManager(this);
      this.worldTracker = new WorldTracker(this);
      this.bukkitPlayer = Bukkit.getPlayer(uuid);
      this.entityId = this.bukkitPlayer.getEntityId();
      this.transactionId = -32768;
      this.checkViolationTimes = new HashMap<>();
      this.checkVlMap = new HashMap<>();
      this.location = new CustomLocation(0.0, 0.0, 0.0, 0.0F, 0.0F);
      this.lastLocation = this.location;
      this.lastLastLocation = this.lastLocation;
      this.lastLastLastLocation = this.lastLastLocation;
      this.checkManager = new CheckManager(this, karhu);
      this.gameMode = GameMode.getById(this.bukkitPlayer.getGameMode().getValue());
      User user = this.bukkitPlayer != null ? PacketEvents.getAPI().getPlayerManager().getUser(this.bukkitPlayer) : null;
      if (user != null) {
         this.updateClientVersion(user.getClientVersion());
         this.user = user;
      }

      this.collisionHandler = new CollisionHandler(this);
      this.desyncedBlockHandler = new DesyncedBlockHandler(this);
      this.movementHandler = new MovementHandler(this);
      this.netHandler = new NetHandler(this);
      this.crashHandler = new CrashHandler(this);
      this.vehicleHandler = new VehicleHandler(this);
      this.wrappedEntity = new WrappedEntityPlayer(this);
      this.placeManager = new PlaceManager(this);
      this.teleportManager = new TeleportManager(this);
      this.effectManager = new EffectManager(this);
      this.inUnloadedChunk = true;
      BoundingBox BB = new BoundingBox(
         this, this.location.x - 0.3, this.location.y, this.location.z - 0.3, this.location.x + 0.3, this.location.y + 1.8, this.location.z + 0.3
      );
      this.boundingBox = BB.clone();
      this.lastBoundingBox = BB.clone();
      this.mcpCollision = BB.clone();
      this.confirmedVersion = false;
      this.locationInited = false;
      this.boundingBoxInited = false;
      this.lastJoinTime = System.currentTimeMillis();
      this.uuid = uuid;
      this.serverTick = Karhu.getInstance().getServerTick();
      this.createdOnTick = Karhu.getInstance().getServerTick();
      this.objectLoaded = true;
   }

   public String getName() {
      return this.bukkitPlayer == null ? "null_player" : this.bukkitPlayer.getName();
   }

   public CustomLocation getLocation() {
      return this.location;
   }

   public World getWorld() {
      return this.bukkitPlayer == null ? this.findWorld() : this.bukkitPlayer.getWorld();
   }

   public long getPing() {
      return this.ping;
   }

   public User getUser() {
      return this.user;
   }

   public void setCleanBrand(String cleanBrand) {
      this.cleanBrand = cleanBrand;
   }

   public void setBukkitPlayer(Player bukkitPlayer) {
      this.bukkitPlayer = bukkitPlayer;
   }

   public boolean isBrandPosted() {
      return this.brandPosted;
   }

   public void setBrand(String brand) {
      this.brand = brand;
   }

   public void setBrandPosted(boolean brandPosted) {
      this.brandPosted = brandPosted;
   }

   public void handleKickAlert(String type) {
      if (!this.kicked) {
         Tasker.run(() -> this.bukkitPlayer.kickPlayer(Karhu.getInstance().getConfigManager().getAnticrashKickMsg()));
         MiscellaneousAlertPoster.postMisc(
            Karhu.getInstance().getConfigManager().getAntiCrashMessage().replaceAll("%debug%", type).replaceAll("%player%", this.bukkitPlayer.getName()),
            this,
            "Crash"
         );
         Karhu.getInstance().getPlug().getLogger().warning("-----------------Karhu Anticrash-----------------");
         Karhu.getInstance().getPlug().getLogger().warning(this.bukkitPlayer.getName() + " was kicked for suspicious packets (" + type + ")");
         Karhu.getInstance().getPlug().getLogger().warning("Keep an eye on the player!");
         Karhu.getInstance().getPlug().getLogger().warning("-----------------Karhu Anticrash-----------------");
         this.kicked = true;
      }
   }

   public Player getBukkitPlayer() {
      return this.bukkitPlayer;
   }

   public boolean isRemovingObject() {
      return this.removingObject;
   }

   public int getLastSneakTick() {
      return this.lastSneakTick;
   }

   public void setLastSneakTick(int lastSneakTick) {
      this.lastSneakTick = lastSneakTick;
   }

   public boolean isOnGroundPacket() {
      return this.onGroundPacket;
   }

   public void setLastLocation(CustomLocation lastLocation) {
      this.lastLocation = lastLocation;
   }

   public boolean isInsideBlock() {
      return this.insideBlock;
   }

   public boolean isUsingItem() {
      return this.usingItem;
   }

   public void setLastInBed(boolean lastInBed) {
      this.lastInBed = lastInBed;
   }

   public int getTotalTicks() {
      return this.totalTicks;
   }

   public void setMoveTicks(int moveTicks) {
      this.moveTicks = moveTicks;
   }

   public void setLastFlying(long lastFlying) {
      this.lastFlying = lastFlying;
   }

   public Map<Integer, TaskData> getTasks() {
      return this.tasks;
   }

   public ItemStack getStackInHand() {
      if (this.bukkitPlayer == null) {
         return new ItemStack(Material.AIR);
      } else {
         ItemStack stack = this.bukkitPlayer.getInventory().getItem(this.currentSlot);
         return stack == null ? new ItemStack(Material.AIR) : stack;
      }
   }

   public long getCreatedOnTick() {
      return this.createdOnTick;
   }

   public void setLastFast(long lastFast) {
      this.lastFast = lastFast;
   }

   public boolean isAllowFlying() {
      return this.allowFlying;
   }

   public void setViaMCP(boolean isViaMCP) {
      this.isViaMCP = isViaMCP;
   }

   public boolean isCorrectedFly() {
      return this.correctedFly;
   }

   public List<Integer> getLastTargets() {
      return this.lastTargets;
   }

   public CustomLocation getLastLocation() {
      return this.lastLocation;
   }

   public boolean isInBed() {
      return this.inBed;
   }

   public boolean isOnGroundServer() {
      return this.onGroundServer;
   }

   public boolean isNewerThan12() {
      return this.isNewerThan12;
   }

   public int getMoveTicks() {
      return this.moveTicks;
   }

   public boolean isNewerThan8() {
      return this.isNewerThan8;
   }

   public void setSafeSetback(CustomLocation safeSetback) {
      this.safeSetback = safeSetback;
   }

   public void setAttacks(int attacks) {
      this.attacks = attacks;
   }

   public void setTotalTicks(int totalTicks) {
      this.totalTicks = totalTicks;
   }

   public void setFlyingTime(long flyingTime) {
      this.flyingTime = flyingTime;
   }

   public void setNoMoveTicks(int noMoveTicks) {
      this.noMoveTicks = noMoveTicks;
   }

   public int getNoMoveTicks() {
      return this.noMoveTicks;
   }

   public void setLastUsingItem(boolean lastUsingItem) {
      this.lastUsingItem = lastUsingItem;
   }

   public boolean isEating() {
      return this.eating;
   }

   public void setLastEating(boolean lastEating) {
      this.lastEating = lastEating;
   }

   public void setUsingItem(boolean usingItem) {
      this.usingItem = usingItem;
   }

   public void setEating(boolean eating) {
      this.eating = eating;
   }

   public boolean isFlyingBukkit() {
      return this.bukkitPlayer == null ? false : this.bukkitPlayer.isFlying();
   }

   public boolean isViaMCP() {
      return this.isViaMCP;
   }

   public void setLastFlyTick(int lastFlyTick) {
      this.lastFlyTick = lastFlyTick;
   }

   public boolean isSpectating() {
      return this.spectating;
   }

   public CustomLocation getSafeSetback() {
      return this.safeSetback;
   }

   public long getLastFlying() {
      return this.lastFlying;
   }

   public CustomLocation getFlyCancel() {
      return this.flyCancel;
   }

   public ClientVersion getClientVersion() {
      return this.clientVersion == null ? ClientVersion.getById(Karhu.SERVER_VERSION.getProtocolVersion()) : this.clientVersion;
   }

   public void setFlyCancel(CustomLocation flyCancel) {
      this.flyCancel = flyCancel;
   }

   public void setVelocityZ(double velocityZ) {
      this.velocityZ = velocityZ;
   }

   public void setVelocityY(double velocityY) {
      this.velocityY = velocityY;
   }

   public boolean isPlacedInside() {
      return this.placedInside;
   }

   public void setLastTeleport(int lastTeleport) {
      this.lastTeleport = lastTeleport;
   }

   public CheckManager getCheckManager() {
      return this.checkManager;
   }

   public void setVelocityX(double velocityX) {
      this.velocityX = velocityX;
   }

   public int getViolations(Check<?> check, Long time) {
      Set<Long> timestamps = this.checkViolationTimes.get(check);
      long sys = System.currentTimeMillis();
      int vl = 0;
      if (timestamps != null) {
         for(Long man : timestamps) {
            if (sys - man <= time) {
               ++vl;
            } else {
               timestamps.remove(man);
            }
         }

         return vl;
      } else {
         return 0;
      }
   }

   public int getLastFlyTick() {
      return this.lastFlyTick;
   }

   public void setPlacedInside(boolean placedInside) {
      this.placedInside = placedInside;
   }

   public double getVelocityX() {
      return this.velocityX;
   }

   public boolean isPlacing() {
      return this.placing;
   }

   public double getVelocityY() {
      return this.velocityY;
   }

   public void setWasPlacing(boolean wasPlacing) {
      this.wasPlacing = wasPlacing;
   }

   public void setPlacing(boolean placing) {
      this.placing = placing;
   }

   public int getPingInTicks() {
      return this.pingInTicks;
   }

   public WrappedEntityPlayer getWrappedEntity() {
      return this.wrappedEntity;
   }

   public boolean isLocationInited() {
      return this.locationInited;
   }

   public boolean isUnderBlock() {
      return this.underBlock;
   }

   public boolean isWasSprinting() {
      return this.wasSprinting;
   }

   public void setWasSprinting(boolean wasSprinting) {
      this.wasSprinting = wasSprinting;
   }

   public boolean isWasSneaking() {
      return this.wasSneaking;
   }

   public void setWasSneaking(boolean wasSneaking) {
      this.wasSneaking = wasSneaking;
   }

   public ICrashHandler getCrashHandler() {
      return this.crashHandler;
   }

   public void checkVelocity() {
      for(Entry<Integer, ConcurrentLinkedDeque<VelocityPending>> myMapEntry : this.velocityPending.entrySet()) {
         for(VelocityPending velocityCheck : myMapEntry.getValue()) {
            Vector velocity = velocityCheck.getVelocity();
            if (!velocityCheck.isMarkedSent()) {
               long nanoStart = System.nanoTime();
               double ogHz = MathUtil.hypot(velocity.getX(), velocity.getZ());
               double min = this.getClientVersion().getProtocolVersion() > 47 ? 0.003 : 0.005;
               if (this.attacks != 0) {
                  Pair<Double, Double> kbs = NMSValueParser.bruteforceAttack(this, velocity.getX(), velocity.getZ());
                  double kbY = Math.abs(velocity.getY()) < min ? 0.0 : velocity.getY();
                  Vector knockbackVector = new Vector(kbs.getX(), kbY, kbs.getY());
                  Vector playerVector = new Vector(this.deltas.deltaX, this.deltas.motionY, this.deltas.deltaZ);
                  double horizontalDistance = MathUtil.horizontalDistance(knockbackVector, playerVector);
                  double verticalDistance = MathUtil.verticalDistance(knockbackVector, playerVector);
                  double precisionY = this.elapsed(this.lastCollidedV) <= 2 ? 0.205 : 0.03125;
                  double precisionH = this.elapsed(this.lastCollidedH) <= 2 ? Math.max(ogHz, horizontalDistance) : 0.03125;
                  if (horizontalDistance <= precisionH && verticalDistance <= precisionY) {
                     this.velocityTick(velocity);
                     velocityCheck.markSent();
                  }
               } else {
                  double kbToUseX = velocity.getX();
                  double kbToUseY = velocity.getY();
                  double kbToUseZ = velocity.getZ();
                  double kbX = Math.abs(kbToUseX) < min ? 0.0 : kbToUseX;
                  double kbY = Math.abs(kbToUseY) < min ? 0.0 : kbToUseY;
                  double kbZ = Math.abs(kbToUseZ) < min ? 0.0 : kbToUseZ;
                  Pair<Double, Double> sheesh = NMSValueParser.loopKeysGetKeys(this, kbX, kbZ);
                  Vector knockbackVector = new Vector(sheesh.getX(), kbY, sheesh.getY());
                  Vector playerVector = new Vector(this.deltas.deltaX, this.deltas.motionY, this.deltas.deltaZ);
                  double horizontalDistance = MathUtil.horizontalDistance(knockbackVector, playerVector);
                  double verticalDistance = MathUtil.verticalDistance(knockbackVector, playerVector);
                  double precisionY = this.elapsed(this.lastCollidedV) <= 2 ? 0.43F : 0.03125;
                  double precisionH = this.elapsed(this.lastCollidedH) <= 2 ? Math.max(ogHz, horizontalDistance) : 0.031;
                  if (horizontalDistance <= precisionH && (verticalDistance <= precisionY || this.isJumped())) {
                     this.velocityTick(velocity);
                     velocityCheck.markSent();
                  }
               }

               long nanoStop = System.nanoTime();
               Benchmark profileData = KarhuBenchmarker.getProfileData(BenchmarkType.PHYSICS_SIMULATOR);
               profileData.insertResult(nanoStart, nanoStop);
            }
         }
      }
   }

   public void setPlacedCancel(boolean placedCancel) {
      this.placedCancel = placedCancel;
   }

   public boolean isOnLiquid() {
      return this.onLiquid;
   }

   public double getVelocityZ() {
      return this.velocityZ;
   }

   public int getBadPingTicks() {
      return this.badPingTicks;
   }

   public EffectManager getEffectManager() {
      return this.effectManager;
   }

   public void setUseTicks(int useTicks) {
      this.useTicks = useTicks;
   }

   public void setDesyncSprint(boolean desyncSprint) {
      this.desyncSprint = desyncSprint;
   }

   public void setDigStopTicks(int digStopTicks) {
      this.digStopTicks = digStopTicks;
   }

   public void setCorrectedFly(boolean correctedFly) {
      this.correctedFly = correctedFly;
   }

   public Vector getLastAbortLoc() {
      return this.lastAbortLoc;
   }

   public void setBlocking(boolean blocking) {
      this.blocking = blocking;
   }

   public void setDigTicks(int digTicks) {
      this.digTicks = digTicks;
   }

   public void setFastDigTicks(int fastDigTicks) {
      this.fastDigTicks = fastDigTicks;
   }

   public long getLastPingTime() {
      return this.lastPingTime;
   }

   public void setLastAbortLoc(Vector lastAbortLoc) {
      this.lastAbortLoc = lastAbortLoc;
   }

   public void setSkipNextSwing(boolean skipNextSwing) {
      this.skipNextSwing = skipNextSwing;
   }

   public ConcurrentHashMap<Integer, EntityData> getEntityData() {
      return this.entityData;
   }

   public void setVehicleX(double vehicleX) {
      this.vehicleX = vehicleX;
   }

   public void setInventoryOpen(boolean inventoryOpen) {
      this.inventoryOpen = inventoryOpen;
   }

   public void setBowing(boolean bowing) {
      this.bowing = bowing;
   }

   public void setGliding(boolean gliding) {
      this.gliding = gliding;
   }

   public boolean isSkipNextSwing() {
      return this.skipNextSwing;
   }

   public void setPlaceTicks(int placeTicks) {
      this.placeTicks = placeTicks;
   }

   public void setDigging(boolean digging) {
      this.digging = digging;
   }

   public boolean isRiding() {
      return this.riding;
   }

   public boolean isHasDig2() {
      return this.elapsed(this.digStopTicks) <= 8 || this.digging;
   }

   public void setLastTarget(LivingEntity lastTarget) {
      this.lastTarget = lastTarget;
   }

   public void setPing(long ping) {
      this.ping = ping;
   }

   public void setDiggingBasic(boolean diggingBasic) {
      this.diggingBasic = diggingBasic;
   }

   public void setInvStamp(int invStamp) {
      this.invStamp = invStamp;
   }

   public void setBucketTicks(int bucketTicks) {
      this.bucketTicks = bucketTicks;
   }

   public void setCurrentSlot(int currentSlot) {
      this.currentSlot = currentSlot;
   }

   public void setVehicleY(double vehicleY) {
      this.vehicleY = vehicleY;
   }

   public void setVehicleZ(double vehicleZ) {
      this.vehicleZ = vehicleZ;
   }

   public boolean isObjectLoaded() {
      return this.objectLoaded;
   }

   public boolean isLastOnGroundPacket() {
      return this.lastOnGroundPacket;
   }

   @Deprecated
   public boolean recentlyTeleported(int ticks) {
      return this.totalTicks - this.lastTeleport <= ticks;
   }

   public TeleportManager getTeleportManager() {
      return this.teleportManager;
   }

   public void setTickedVelocity(Vector tickedVelocity) {
      this.tickedVelocity = tickedVelocity;
   }

   public Vector getTickedVelocity() {
      return this.tickedVelocity;
   }

   public boolean isPossiblyTeleporting() {
      return this.possiblyTeleporting;
   }

   public boolean isSeventeenPlacing() {
      return this.seventeenPlacing;
   }

   public DesyncedBlockHandler getDesyncedBlockHandler() {
      return this.desyncedBlockHandler;
   }

   public long getLastLocationUpdate() {
      return this.lastLocationUpdate;
   }

   public void setSafeGroundSetback(CustomLocation safeGroundSetback) {
      this.safeGroundSetback = safeGroundSetback;
   }

   public boolean isDidFlagMovement() {
      return this.didFlagMovement;
   }

   public void setCheckVl(double vl, Check<?> check) {
      if (vl < 0.0) {
         vl = 0.0;
      }

      this.checkVlMap.put(check, vl);
   }

   public void addViolation(Check<?> check) {
      Set<Long> timestamps = this.checkViolationTimes.get(check);
      if (timestamps == null) {
         timestamps = ConcurrentHashMap.newKeySet();
      }

      timestamps.add(System.currentTimeMillis());
      this.checkViolationTimes.put(check, timestamps);
   }

   public String getBrand() {
      return this.brand;
   }

   public String getCleanBrand() {
      return this.cleanBrand;
   }

   public boolean isCinematic() {
      return this.cinematic;
   }

   public int getSensitivity() {
      return this.sensitivity;
   }

   public int getPlaceTicks() {
      return this.placeTicks;
   }

   public boolean isHasDig() {
      return this.elapsed(this.digStopTicks) <= 8 || this.elapsed(this.digTicks) <= 3 || this.digging;
   }

   public int getLastCinematic() {
      return this.lastCinematic;
   }

   public LivingEntity getLastTarget() {
      return this.lastTarget;
   }

   public float getSensitivityY() {
      return this.sensitivityY;
   }

   public float getPitchGCD() {
      return this.pitchGCD;
   }

   public long elapsedMS(long now, long time) {
      return (long)((double)(now - time) / 1000000.0);
   }

   public long elapsedMS(long i) {
      return System.currentTimeMillis() - i;
   }

   public void setHighestCps(double highestCps) {
      this.highestCps = highestCps;
   }

   public int getDigTicks() {
      return this.digTicks;
   }

   public void setCps(double cps) {
      this.cps = cps;
   }

   public double getHighestCps() {
      return this.highestCps;
   }

   public double getCps() {
      return this.cps;
   }

   public void setLastCps(double lastCps) {
      this.lastCps = lastCps;
   }

   public int getAttacks() {
      return this.attacks;
   }

   public List<Double> getEyePositions() {
      if (this.isNewerThan13) {
         return this.eyesFourteen;
      } else {
         return this.isNewerThan8 ? this.eyesNine : this.eyesLegacy;
      }
   }

   public boolean isWasWasSneaking() {
      return this.wasWasSneaking;
   }

   public boolean isGliding() {
      return this.gliding;
   }

   public boolean isRiptiding() {
      return this.riptiding;
   }

   public double getHighestReach() {
      return this.highestReach;
   }

   public void setReachBypass(boolean reachBypass) {
      this.reachBypass = reachBypass;
   }

   public void setHighestReach(double highestReach) {
      this.highestReach = highestReach;
   }

   public double offsetMove() {
      return this.clientVersion.isNewerThanOrEquals(ClientVersion.V_1_18_2) ? 2.0E-4 : 0.03;
   }

   public boolean hasFast() {
      return this.lastFlying != 0L && this.lastFast != 0L && (double)(this.lastFlying - this.lastFast) / 1000000.0 < 100.0;
   }

   public int getLastCollided() {
      return this.lastCollided;
   }

   public boolean isJumped() {
      return this.jumped;
   }

   public int getLastOnBoat() {
      return this.lastOnBoat;
   }

   public int getLastInWeb() {
      return this.lastInWeb;
   }

   public int getLastOnSlime() {
      return this.lastOnSlime;
   }

   public boolean isInWeb() {
      return this.inWeb;
   }

   public boolean isWasInWeb() {
      return this.isWasInWeb;
   }

   public int getLastSneakEdge() {
      return this.lastSneakEdge;
   }

   public int getLastInLiquid() {
      return this.lastInLiquid;
   }

   public int getLastCollidedV() {
      return this.lastCollidedV;
   }

   public boolean isOnSlime() {
      return this.onSlime;
   }

   public Deltas getDeltas() {
      return this.deltas;
   }

   public boolean isLagging(int ticks, int time) {
      return ticks - this.lastDroppedPackets < time;
   }

   public boolean isLagging(int ticks) {
      return ticks - this.lastDroppedPackets < 2;
   }

   public int getJumpBoost() {
      return this.jumpBoost;
   }

   public boolean isTakingVertical() {
      return this.takingVertical;
   }

   public int getLastOnIce() {
      return this.lastOnIce;
   }

   public boolean isNearClimbable() {
      return this.nearClimbable;
   }

   public boolean isOnWeb() {
      return this.onWeb;
   }

   public boolean isOnWater() {
      return this.onWater;
   }

   public boolean isWasSlimeLand() {
      return this.wasSlimeLand;
   }

   public double getVehicleX() {
      return this.vehicleX;
   }

   public int getLastInBerry() {
      return this.lastInBerry;
   }

   public boolean isOnGhostBlock() {
      return this.onGhostBlock;
   }

   public int getLastGlide() {
      return this.lastGlide;
   }

   public boolean isOnPiston() {
      return this.onPiston;
   }

   public int getLastRiptide() {
      return this.lastRiptide;
   }

   public boolean isOnFence() {
      return this.onFence;
   }

   public boolean isOnLava() {
      return this.onLava;
   }

   public boolean isLastInBed() {
      return this.lastInBed;
   }

   public boolean isWasOnFence() {
      return this.wasOnFence;
   }

   public int getLastOnBed() {
      return this.lastOnBed;
   }

   public boolean isGroundNearBox() {
      return this.groundNearBox;
   }

   public boolean isOnScaffolding() {
      return this.onScaffolding;
   }

   public boolean isWasWasInWeb() {
      return this.isWasWasInWeb;
   }

   public boolean isWasOnLava() {
      return this.wasOnLava;
   }

   public boolean isOnBoat() {
      return this.onBoat;
   }

   public boolean isWasOnWater() {
      return this.wasOnWater;
   }

   public boolean isLastLadder() {
      return this.lastLadder;
   }

   public boolean isOnCarpet() {
      return this.onCarpet;
   }

   public int getLastInPowder() {
      return this.lastInPowder;
   }

   public boolean isOnLadder() {
      return this.onLadder;
   }

   public boolean isOnClimbable() {
      return this.onClimbable;
   }

   public int getFastDigTicks() {
      return this.fastDigTicks;
   }

   public int getVehicleId() {
      return this.vehicleId;
   }

   public boolean isWasOnHoney() {
      return this.wasOnHoney;
   }

   public boolean isOnHoney() {
      return this.onHoney;
   }

   public boolean isWasUnderBlock() {
      return this.wasUnderBlock;
   }

   public double getVehicleZ() {
      return this.vehicleZ;
   }

   public boolean isBowing() {
      return this.bowing;
   }

   public int getCurrentSlot() {
      return this.currentSlot;
   }

   public int getDolphinLevel() {
      return this.dolphinLevel;
   }

   public double getVehicleY() {
      return this.vehicleY;
   }

   public boolean isOnBed() {
      return this.onBed;
   }

   public int getLastOnSoul() {
      return this.lastOnSoul;
   }

   public boolean isWasOnBed() {
      return this.wasOnBed;
   }

   public double getConfirmingY() {
      return this.confirmingY;
   }

   public boolean isOnSoulsand() {
      return this.onSoulsand;
   }

   public boolean isWasOnSoulSand() {
      return this.wasOnSoulSand;
   }

   public boolean isWasOnDoor() {
      return this.wasOnDoor;
   }

   public int getLastCollidedH() {
      return this.lastCollidedH;
   }

   public int getSpeedBoost() {
      return this.speedBoost;
   }

   public boolean isUnderWeb() {
      return this.underWeb;
   }

   public int getSlowness() {
      return this.slowness;
   }

   public int getLastFence() {
      return this.lastFence;
   }

   public boolean isWasOnSlime() {
      return this.wasOnSlime;
   }

   public boolean isTimerKicked() {
      return this.timerKicked;
   }

   public void setTimerKicked(boolean timerKicked) {
      this.timerKicked = timerKicked;
   }

   public boolean isExitingVehicle() {
      return this.exitingVehicle;
   }

   public boolean isOnStairs() {
      return this.onStairs;
   }

   public boolean isJumpedLastTick() {
      return this.jumpedLastTick;
   }

   public boolean isAtButton() {
      return this.atButton;
   }

   public boolean isInsideTrapdoor() {
      return this.insideTrapdoor;
   }

   public boolean isOnSlab() {
      return this.onSlab;
   }

   public boolean isNewerThan16() {
      return this.isNewerThan16;
   }

   public boolean isAtSign() {
      return this.atSign;
   }

   public int getHaste() {
      return this.haste;
   }

   public int getFatigue() {
      return this.fatigue;
   }

   public void setCancelBreak(boolean cancelBreak) {
      this.cancelBreak = cancelBreak;
   }

   public boolean isOnComparator() {
      return this.onComparator;
   }

   public boolean isBlocking() {
      return this.blocking;
   }

   public boolean isInitialized() {
      return this.bukkitPlayer != null;
   }

   public void setLocation(CustomLocation location) {
      this.location = location;
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void teleport(CustomLocation location) {
      if (this.bukkitPlayer != null) {
         this.invalidMovementTicks = 0;
         this.bukkitPlayer.teleport(location.toLocation(this.getWorld()));
      }
   }

   public void teleport(Location location) {
      if (this.bukkitPlayer != null) {
         if (this.getWorld() == location.getWorld()) {
            this.invalidMovementTicks = 0;
            this.bukkitPlayer.teleport(location);
         }
      }
   }

   public void setFallDistance(float fallDistance) {
      this.fallDistance = fallDistance;
   }

   public Entity getVehicle() {
      return this.vehicle;
   }

   public float getFallDistance() {
      return this.fallDistance;
   }

   public void setConfirmingVelocity(boolean confirmingVelocity) {
      this.confirmingVelocity = confirmingVelocity;
   }

   public void setCurrentServerTransaction(int currentServerTransaction) {
      this.currentServerTransaction = currentServerTransaction;
   }

   public void setLastVelocityTaken(int lastVelocityTaken) {
      this.lastVelocityTaken = lastVelocityTaken;
   }

   public void setPlayerVelocityCalled(boolean playerVelocityCalled) {
      this.playerVelocityCalled = playerVelocityCalled;
   }

   public void setMaxVelocityYTicks(int maxVelocityYTicks) {
      this.maxVelocityYTicks = maxVelocityYTicks;
   }

   public void setPendingBackSwitch(boolean pendingBackSwitch) {
      this.pendingBackSwitch = pendingBackSwitch;
   }

   public void setTransactionPing(long transactionPing) {
      this.transactionPing = transactionPing;
   }

   public void setFirstTransactionSent(boolean firstTransactionSent) {
      this.firstTransactionSent = firstTransactionSent;
   }

   public void setLastTransactionPingUpdate(long lastTransactionPingUpdate) {
      this.lastTransactionPingUpdate = lastTransactionPingUpdate;
   }

   public Map<Integer, Deque<Integer>> getBackSwitchSlots() {
      return this.backSwitchSlots;
   }

   public boolean isPendingBackSwitch() {
      return this.pendingBackSwitch;
   }

   public void setLastTransactionPing(long lastTransactionPing) {
      this.lastTransactionPing = lastTransactionPing;
   }

   public void setLastTeleportPacket(long lastTeleportPacket) {
      this.lastTeleportPacket = lastTeleportPacket;
   }

   public void setTransactionClock(long transactionClock) {
      this.transactionClock = transactionClock;
   }

   public Map<Short, Long> getTransactionTime() {
      return this.transactionTime;
   }

   public void useOldTransaction(Consumer<Short> callback, short uid) {
      ObjectArrayList<Consumer<Short>> map = (ObjectArrayList)this.waitingConfirms.computeIfAbsent(uid, k -> new ObjectArrayList());
      map.add(callback);
      this.waitingConfirms.put(uid, map);
   }

   public void setHasReceivedTransaction(boolean hasReceivedTransaction) {
      this.hasReceivedTransaction = hasReceivedTransaction;
   }

   public boolean isHasReceivedTransaction() {
      return this.hasReceivedTransaction;
   }

   public int getCurrentServerTransaction() {
      return this.currentServerTransaction;
   }

   public void setRidingUncertain(boolean ridingUncertain) {
      this.ridingUncertain = ridingUncertain;
   }

   public ConcurrentLinkedDeque<VelocityPending> getTickVelocities(int transactionId) {
      return this.velocityPending.get(transactionId);
   }

   public void setVelocityXZTicks(int velocityXZTicks) {
      this.velocityXZTicks = velocityXZTicks;
   }

   public void setPlayerExplodeCalled(boolean playerExplodeCalled) {
      this.playerExplodeCalled = playerExplodeCalled;
   }

   public void setVelocityYTicks(int velocityYTicks) {
      this.velocityYTicks = velocityYTicks;
   }

   public void setMaxVelocityXZTicks(int maxVelocityXZTicks) {
      this.maxVelocityXZTicks = maxVelocityXZTicks;
   }

   public void setLastTransaction(long lastTransaction) {
      this.lastTransaction = lastTransaction;
   }

   public void setSneaking(boolean sneaking) {
      this.sneaking = sneaking;
   }

   public void setSprinting(boolean sprinting) {
      this.sprinting = sprinting;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public void setAirTicks(int airTicks) {
      this.airTicks = airTicks;
   }

   public void tick() {
      this.tasksManager.doTasks();
   }

   public boolean isFlying() {
      return this.flying;
   }

   public void closeInventory() {
      if (this.bukkitPlayer != null) {
         this.bukkitPlayer.closeInventory();
         this.queueToPrePing(task -> this.inventoryOpen = false);
      }
   }

   public Map<Short, ObjectArrayList<Consumer<Short>>> getWaitingConfirms() {
      return this.waitingConfirms;
   }

   public int getLastClientTransaction() {
      return this.lastClientTransaction;
   }

   public void setLastLastClientTransaction(int lastLastClientTransaction) {
      this.lastLastClientTransaction = lastLastClientTransaction;
   }

   public void setCurrentClientTransaction(int currentClientTransaction) {
      this.currentClientTransaction = currentClientTransaction;
   }

   public Short2ObjectMap<Consumer<Short>> getScheduledTransactions() {
      return this.scheduledTransactions;
   }

   public int getCurrentClientTransaction() {
      return this.currentClientTransaction;
   }

   public void setLastClientTransaction(int lastClientTransaction) {
      this.lastClientTransaction = lastClientTransaction;
   }

   public double clamp() {
      return this.clientVersion.getProtocolVersion() > 47 ? 0.003 : 0.005;
   }

   public boolean isSneaking() {
      return this.sneaking;
   }

   public BoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   public boolean isSprinting() {
      return this.sprinting;
   }

   public int getAirTicks() {
      return this.airTicks;
   }

   public boolean isCancelNextHitR() {
      return this.cancelNextHitR;
   }

   public boolean isCancelNextHitH() {
      return this.cancelNextHitH;
   }

   public boolean isCancelBreak() {
      return this.cancelBreak;
   }

   public boolean isNewerThan13() {
      return this.isNewerThan13;
   }

   public void addViolations(Check<?> check, int vl) {
      long sys = System.currentTimeMillis();

      for(int i = 0; i < vl; ++i) {
         Set<Long> timestamps = this.checkViolationTimes.get(check);
         if (timestamps == null) {
            timestamps = ConcurrentHashMap.newKeySet();
         }

         timestamps.add(sys + (long)i);
         this.checkViolationTimes.put(check, timestamps);
      }
   }

   public short sendTransaction() {
      short id = this.getNextTransactionId();
      PlayerUtil.sendPacket(this.bukkitPlayer, id);
      return id;
   }

   public void sendTransaction(Consumer<Short> callback) {
      short id = this.getNextTransactionId();
      this.scheduledTransactions.put(id, callback);
      PlayerUtil.sendPacket(this.bukkitPlayer, id);
   }

   private World findWorld() {
      if (this.entityId != -1) {
         for(World world : Bukkit.getWorlds()) {
            for(Entity entity : SpigotReflectionUtil.getEntityList(world)) {
               if (entity.getEntityId() == this.entityId) {
                  return world;
               }
            }
         }
      }

      return Bukkit.getWorld(((World)Bukkit.getWorlds().get(0)).getUID());
   }

   public boolean isKicked() {
      return this.kicked;
   }

   public boolean isPlacedCancel() {
      return this.placedCancel;
   }

   public Map<Check, Double> getCheckVlMap() {
      return this.checkVlMap;
   }

   public float getSpeedInAir() {
      return this.speedInAir;
   }

   public float getJumpFactor() {
      return this.jumpFactor;
   }

   public String getPressedKey() {
      return this.pressedKey;
   }

   public boolean hasExempt() {
      return this.exemptMap.size() > 0 || this.exemptCategoryMap.size() > 0;
   }

   public PlaceManager getPlaceManager() {
      return this.placeManager;
   }

   public boolean isConfigChecked() {
      return this.configChecked;
   }

   public Block getBlockBelow() {
      return this.blockBelow;
   }

   public Block getBlockInside() {
      return this.blockInside;
   }

   public int mostRecentPing() {
      return this.netHandler.mostRecentPing();
   }

   public String getKeyCombo() {
      return this.keyCombo;
   }

   public int getSentConfirms() {
      return this.sentConfirms;
   }

   public boolean isWasWasOnSlime() {
      return this.wasWasOnSlime;
   }

   public int getBucketTicks() {
      return this.bucketTicks;
   }

   public boolean isEdgeOfFence() {
      return this.edgeOfFence;
   }

   public int getInvStamp() {
      return this.invStamp;
   }

   public boolean isFlyingS() {
      return this.flyingS;
   }

   public boolean isInitedFlying() {
      return this.initedFlying;
   }

   public int getUpdateBuf() {
      return this.updateBuf;
   }

   public boolean isOnIce() {
      return this.onIce;
   }

   public boolean isLastEating() {
      return this.lastEating;
   }

   public KarhuWorld getKarhuWorld() {
      return this.karhuWorld;
   }

   public TaskManager<KarhuPlayer> getTasksManager() {
      return this.tasksManager;
   }

   public boolean isFinalCollidedH() {
      return this.finalCollidedH;
   }

   public boolean isDesyncSprint() {
      return this.desyncSprint;
   }

   public int getLastPos() {
      return this.lastPos;
   }

   public Block getMovementBlock() {
      return this.movementBlock;
   }

   public boolean isWasOnSlab() {
      return this.wasOnSlab;
   }

   public boolean isOnGroundMath() {
      return this.onGroundMath;
   }

   public float getAttackerYaw() {
      return this.attackerYaw;
   }

   public int getLastTeleport() {
      return this.lastTeleport;
   }

   public boolean isReachBypass() {
      return this.reachBypass;
   }

   public Vec3 getBedPos() {
      return this.bedPos;
   }

   public double getAttackerX() {
      return this.attackerX;
   }

   public boolean isOnDoor() {
      return this.onDoor;
   }

   public boolean isWasOnStairs() {
      return this.wasOnStairs;
   }

   public List<Double> getEyesNine() {
      return this.eyesNine;
   }

   public boolean isJoining() {
      return this.joining;
   }

   public boolean isFuckedTeleport() {
      return this.fuckedTeleport;
   }

   public BoundingBox getMcpCollision() {
      return this.mcpCollision;
   }

   public int getCacheBoost() {
      return this.cacheBoost;
   }

   public boolean isSneakEdge() {
      return this.sneakEdge;
   }

   public short getConfirmId() {
      return this.confirmId;
   }

   public float getLFallDistance() {
      return this.lFallDistance;
   }

   public int getDigStopTicks() {
      return this.digStopTicks;
   }

   public boolean isDigging() {
      return this.digging;
   }

   public List<Double> getEyesLegacy() {
      return this.eyesLegacy;
   }

   public boolean isOnWaterOffset() {
      return this.onWaterOffset;
   }

   public boolean isFlyingC() {
      return this.flyingC;
   }

   public int getWeirdTicks() {
      return this.weirdTicks;
   }

   public WorldTracker getWorldTracker() {
      return this.worldTracker;
   }

   public boolean isWasPlacing() {
      return this.wasPlacing;
   }

   public short getLastPostId() {
      return this.lastPostId;
   }

   public int getMoveCalls() {
      return this.moveCalls;
   }

   public boolean isWasOnClimbable() {
      return this.wasOnClimbable;
   }

   public boolean isShit() {
      return this.shit;
   }

   public boolean isOnSweet() {
      return this.onSweet;
   }

   public long getFlyingTime() {
      return this.flyingTime;
   }

   public int getUseTicks() {
      return this.useTicks;
   }

   public List<Double> getEyesFourteen() {
      return this.eyesFourteen;
   }

   public boolean isLastBlockSneak() {
      return this.lastBlockSneak;
   }

   public boolean isHasSentFirst() {
      return this.hasSentFirst;
   }

   public boolean isDiggingBasic() {
      return this.diggingBasic;
   }

   public int getTeleports() {
      return this.teleports;
   }

   public boolean isInPowder() {
      return this.inPowder;
   }

   public long getLastJoinTime() {
      return this.lastJoinTime;
   }

   public boolean isSlimeLand() {
      return this.slimeLand;
   }

   public boolean isWasFlyingC() {
      return this.wasFlyingC;
   }

   public float getAttackerPitch() {
      return this.attackerPitch;
   }

   public boolean isTraceDigging() {
      return this.traceDigging;
   }

   public short getTransactionId() {
      return this.transactionId;
   }

   public boolean isInventoryOpen() {
      return this.inventoryOpen;
   }

   public boolean isCrouching() {
      return this.crouching;
   }

   public boolean isLastUsingItem() {
      return this.lastUsingItem;
   }

   public double getAttackerY() {
      return this.attackerY;
   }

   public double getAttackerZ() {
      return this.attackerZ;
   }

   public void setInsideBlock(boolean insideBlock) {
      this.insideBlock = insideBlock;
   }

   public void setInWeb(boolean inWeb) {
      this.inWeb = inWeb;
   }

   public void setShit(boolean shit) {
      this.shit = shit;
   }

   public int getHurtTicks() {
      return this.hurtTicks;
   }

   public void setMoveCalls(int moveCalls) {
      this.moveCalls = moveCalls;
   }

   public void setConfirmId(short confirmId) {
      this.confirmId = confirmId;
   }

   public void setLastCollidedH(int lastCollidedH) {
      this.lastCollidedH = lastCollidedH;
   }

   public void setKeyCombo(String keyCombo) {
      this.keyCombo = keyCombo;
   }

   public void setLastFence(int lastFence) {
      this.lastFence = lastFence;
   }

   public void setWasWasInWeb(boolean isWasWasInWeb) {
      this.isWasWasInWeb = isWasWasInWeb;
   }

   public float getSensitivityX() {
      return this.sensitivityX;
   }

   public void setWeirdTicks(int weirdTicks) {
      this.weirdTicks = weirdTicks;
   }

   public void setCreatedOnTick(long createdOnTick) {
      this.createdOnTick = createdOnTick;
   }

   public void setNetHandler(NetHandler netHandler) {
      this.netHandler = netHandler;
   }

   public void setBlockBelow(Block blockBelow) {
      this.blockBelow = blockBelow;
   }

   public void setBoundingBox(BoundingBox boundingBox) {
      this.boundingBox = boundingBox;
   }

   public void setPlaceManager(PlaceManager placeManager) {
      this.placeManager = placeManager;
   }

   public void setTransactionId(short transactionId) {
      this.transactionId = transactionId;
   }

   public void setHasSentFirst(boolean hasSentFirst) {
      this.hasSentFirst = hasSentFirst;
   }

   public void setLastOnSlime(int lastOnSlime) {
      this.lastOnSlime = lastOnSlime;
   }

   public long getCreatedAt() {
      return this.createdAt;
   }

   public void setLastCollided(int lastCollided) {
      this.lastCollided = lastCollided;
   }

   public void setWasInWeb(boolean isWasInWeb) {
      this.isWasInWeb = isWasInWeb;
   }

   public long getLastFast() {
      return this.lastFast;
   }

   public void setNewerThan12(boolean isNewerThan12) {
      this.isNewerThan12 = isNewerThan12;
   }

   public boolean isBrokenVehicle() {
      return this.brokenVehicle;
   }

   public void setBlockInside(Block blockInside) {
      this.blockInside = blockInside;
   }

   public void setPressedKey(String pressedKey) {
      this.pressedKey = pressedKey;
   }

   public void setKicked(boolean kicked) {
      this.kicked = kicked;
   }

   public void setNewerThan8(boolean isNewerThan8) {
      this.isNewerThan8 = isNewerThan8;
   }

   public void setWrappedEntity(WrappedEntityPlayer wrappedEntity) {
      this.wrappedEntity = wrappedEntity;
   }

   public void setNewerThan16(boolean isNewerThan16) {
      this.isNewerThan16 = isNewerThan16;
   }

   public List<Pair<Integer, Integer>> getExemptMap() {
      return this.exemptMap;
   }

   public void setConfigChecked(boolean configChecked) {
      this.configChecked = configChecked;
   }

   public double getLastCps() {
      return this.lastCps;
   }

   public Vec3 getLook() {
      return this.look;
   }

   public void setSentConfirms(int sentConfirms) {
      this.sentConfirms = sentConfirms;
   }

   public void setLastOnIce(int lastOnIce) {
      this.lastOnIce = lastOnIce;
   }

   public void setLastOnBed(int lastOnBed) {
      this.lastOnBed = lastOnBed;
   }

   public float getYawGCD() {
      return this.yawGCD;
   }

   public void setObjectLoaded(boolean objectLoaded) {
      this.objectLoaded = objectLoaded;
   }

   public void setUuid(UUID uuid) {
      this.uuid = uuid;
   }

   public void setNewerThan13(boolean isNewerThan13) {
      this.isNewerThan13 = isNewerThan13;
   }

   public void setJumped(boolean jumped) {
      this.jumped = jumped;
   }

   public void setLastInLiquid(int lastInLiquid) {
      this.lastInLiquid = lastInLiquid;
   }

   public void setCrashHandler(ICrashHandler crashHandler) {
      this.crashHandler = crashHandler;
   }

   public float getPredictPitch() {
      return this.predictPitch;
   }

   public void setLastPostId(short lastPostId) {
      this.lastPostId = lastPostId;
   }

   public void setJumpFactor(float jumpFactor) {
      this.jumpFactor = jumpFactor;
   }

   public void setCheckVlMap(Map<Check, Double> checkVlMap) {
      this.checkVlMap = checkVlMap;
   }

   public void setLastInBerry(int lastInBerry) {
      this.lastInBerry = lastInBerry;
   }

   public void setLastOnSoul(int lastOnSoul) {
      this.lastOnSoul = lastOnSoul;
   }

   public void setLastInWeb(int lastInWeb) {
      this.lastInWeb = lastInWeb;
   }

   public void setLastCollidedV(int lastCollidedV) {
      this.lastCollidedV = lastCollidedV;
   }

   public void setLastOnBoat(int lastOnBoat) {
      this.lastOnBoat = lastOnBoat;
   }

   public float getPredictYaw() {
      return this.predictYaw;
   }

   public void setLastInPowder(int lastInPowder) {
      this.lastInPowder = lastInPowder;
   }

   public void setLastSneakEdge(int lastSneakEdge) {
      this.lastSneakEdge = lastSneakEdge;
   }

   public void setTraceDigging(boolean traceDigging) {
      this.traceDigging = traceDigging;
   }

   public void setSpeedInAir(float speedInAir) {
      this.speedInAir = speedInAir;
   }

   public void setEdgeOfFence(boolean edgeOfFence) {
      this.edgeOfFence = edgeOfFence;
   }

   public int getTrackCount() {
      return this.trackCount;
   }

   public void setConfirmingY(double confirmingY) {
      this.confirmingY = confirmingY;
   }

   public void setCheckManager(CheckManager checkManager) {
      this.checkManager = checkManager;
   }

   public void setGroundNearBox(boolean groundNearBox) {
      this.groundNearBox = groundNearBox;
   }

   public void setUnderBlock(boolean underBlock) {
      this.underBlock = underBlock;
   }

   public void setWasOnSoulSand(boolean wasOnSoulSand) {
      this.wasOnSoulSand = wasOnSoulSand;
   }

   public void setAttackerYaw(float attackerYaw) {
      this.attackerYaw = attackerYaw;
   }

   public void setEyesFourteen(List<Double> eyesFourteen) {
      this.eyesFourteen = eyesFourteen;
   }

   public void setOnDoor(boolean onDoor) {
      this.onDoor = onDoor;
   }

   public void setOnFence(boolean onFence) {
      this.onFence = onFence;
   }

   public void setWasOnDoor(boolean wasOnDoor) {
      this.wasOnDoor = wasOnDoor;
   }

   public void setOnCarpet(boolean onCarpet) {
      this.onCarpet = onCarpet;
   }

   public void setOnComparator(boolean onComparator) {
      this.onComparator = onComparator;
   }

   public void setOnClimbable(boolean onClimbable) {
      this.onClimbable = onClimbable;
   }

   public void setUpdateBuf(int updateBuf) {
      this.updateBuf = updateBuf;
   }

   public void setOnIce(boolean onIce) {
      this.onIce = onIce;
   }

   public void setCinematic(boolean cinematic) {
      this.cinematic = cinematic;
   }

   public void setOnScaffolding(boolean onScaffolding) {
      this.onScaffolding = onScaffolding;
   }

   public void setOnWaterOffset(boolean onWaterOffset) {
      this.onWaterOffset = onWaterOffset;
   }

   public void setOnWater(boolean onWater) {
      this.onWater = onWater;
   }

   public void setLastCinematic(int lastCinematic) {
      this.lastCinematic = lastCinematic;
   }

   public void setWasOnStairs(boolean wasOnStairs) {
      this.wasOnStairs = wasOnStairs;
   }

   public void setLastPos(int lastPos) {
      this.lastPos = lastPos;
   }

   public void setCrouching(boolean crouching) {
      this.crouching = crouching;
   }

   public void setOnBoat(boolean onBoat) {
      this.onBoat = onBoat;
   }

   public void setLFallDistance(float lFallDistance) {
      this.lFallDistance = lFallDistance;
   }

   public void setOnSlime(boolean onSlime) {
      this.onSlime = onSlime;
   }

   public void setSneakEdge(boolean sneakEdge) {
      this.sneakEdge = sneakEdge;
   }

   public void setAtSign(boolean atSign) {
      this.atSign = atSign;
   }

   public void setWasOnLava(boolean wasOnLava) {
      this.wasOnLava = wasOnLava;
   }

   public void setMovementBlock(Block movementBlock) {
      this.movementBlock = movementBlock;
   }

   public void setOnLava(boolean onLava) {
      this.onLava = onLava;
   }

   public void setWasOnFence(boolean wasOnFence) {
      this.wasOnFence = wasOnFence;
   }

   public void setWasOnSlab(boolean wasOnSlab) {
      this.wasOnSlab = wasOnSlab;
   }

   public void setOnLadder(boolean onLadder) {
      this.onLadder = onLadder;
   }

   public void setWasWasOnSlime(boolean wasWasOnSlime) {
      this.wasWasOnSlime = wasWasOnSlime;
   }

   public void setOnSweet(boolean onSweet) {
      this.onSweet = onSweet;
   }

   public void setInPowder(boolean inPowder) {
      this.inPowder = inPowder;
   }

   public void setWasUnderBlock(boolean wasUnderBlock) {
      this.wasUnderBlock = wasUnderBlock;
   }

   public void setLastTargets(List<Integer> lastTargets) {
      this.lastTargets = lastTargets;
   }

   public void setEntityData(ConcurrentHashMap<Integer, EntityData> entityData) {
      this.entityData = entityData;
   }

   public void setOnLiquid(boolean onLiquid) {
      this.onLiquid = onLiquid;
   }

   public void setSlimeLand(boolean slimeLand) {
      this.slimeLand = slimeLand;
   }

   public void setOnWeb(boolean onWeb) {
      this.onWeb = onWeb;
   }

   public void setAttackerY(double attackerY) {
      this.attackerY = attackerY;
   }

   public void setEyesNine(List<Double> eyesNine) {
      this.eyesNine = eyesNine;
   }

   public void setOnSoulsand(boolean onSoulsand) {
      this.onSoulsand = onSoulsand;
   }

   public void setLastLadder(boolean lastLadder) {
      this.lastLadder = lastLadder;
   }

   public void setNearClimbable(boolean nearClimbable) {
      this.nearClimbable = nearClimbable;
   }

   public void setWasSlimeLand(boolean wasSlimeLand) {
      this.wasSlimeLand = wasSlimeLand;
   }

   public void setOnHoney(boolean onHoney) {
      this.onHoney = onHoney;
   }

   public void setAttackerPitch(float attackerPitch) {
      this.attackerPitch = attackerPitch;
   }

   public void setMcpCollision(BoundingBox mcpCollision) {
      this.mcpCollision = mcpCollision;
   }

   public void setAttackerX(double attackerX) {
      this.attackerX = attackerX;
   }

   public void setWasOnBed(boolean wasOnBed) {
      this.wasOnBed = wasOnBed;
   }

   public void setWasOnSlime(boolean wasOnSlime) {
      this.wasOnSlime = wasOnSlime;
   }

   public void setWasOnHoney(boolean wasOnHoney) {
      this.wasOnHoney = wasOnHoney;
   }

   public void setOnPiston(boolean onPiston) {
      this.onPiston = onPiston;
   }

   public void setAtButton(boolean atButton) {
      this.atButton = atButton;
   }

   public void setUnderWeb(boolean underWeb) {
      this.underWeb = underWeb;
   }

   public void setOnBed(boolean onBed) {
      this.onBed = onBed;
   }

   public void setOnSlab(boolean onSlab) {
      this.onSlab = onSlab;
   }

   public void setEyesLegacy(List<Double> eyesLegacy) {
      this.eyesLegacy = eyesLegacy;
   }

   public void setTeleports(int teleports) {
      this.teleports = teleports;
   }

   public void setWasOnWater(boolean wasOnWater) {
      this.wasOnWater = wasOnWater;
   }

   public void setOnStairs(boolean onStairs) {
      this.onStairs = onStairs;
   }

   public void setAttackerZ(double attackerZ) {
      this.attackerZ = attackerZ;
   }

   public void setYawGCD(float yawGCD) {
      this.yawGCD = yawGCD;
   }

   public void setCreatedAt(long createdAt) {
      this.createdAt = createdAt;
   }

   public void setOnGhostBlock(boolean onGhostBlock) {
      this.onGhostBlock = onGhostBlock;
   }

   public void setJoining(boolean joining) {
      this.joining = joining;
   }

   public void setThread(Thread thread) {
      this.thread = thread;
   }

   public void setLastRiptide(int lastRiptide) {
      this.lastRiptide = lastRiptide;
   }

   public void setHurtTicks(int hurtTicks) {
      this.hurtTicks = hurtTicks;
   }

   public void setSensitivityX(float sensitivityX) {
      this.sensitivityX = sensitivityX;
   }

   public void setJumpBoost(int jumpBoost) {
      this.jumpBoost = jumpBoost;
   }

   public void setSensitivity(int sensitivity) {
      this.sensitivity = sensitivity;
   }

   public void setRiptiding(boolean riptiding) {
      this.riptiding = riptiding;
   }

   public void setOnGroundMath(boolean onGroundMath) {
      this.onGroundMath = onGroundMath;
   }

   public void setHaste(int haste) {
      this.haste = haste;
   }

   public void setCacheBoost(int cacheBoost) {
      this.cacheBoost = cacheBoost;
   }

   public void setWorldTracker(WorldTracker worldTracker) {
      this.worldTracker = worldTracker;
   }

   public void setLastGlide(int lastGlide) {
      this.lastGlide = lastGlide;
   }

   public void setWasFlyingC(boolean wasFlyingC) {
      this.wasFlyingC = wasFlyingC;
   }

   public void setDolphinLevel(int dolphinLevel) {
      this.dolphinLevel = dolphinLevel;
   }

   public void setBrokenVehicle(boolean brokenVehicle) {
      this.brokenVehicle = brokenVehicle;
   }

   public void setEffectManager(EffectManager effectManager) {
      this.effectManager = effectManager;
   }

   public void setInitedFlying(boolean initedFlying) {
      this.initedFlying = initedFlying;
   }

   public void setFlyingS(boolean flyingS) {
      this.flyingS = flyingS;
   }

   public void setBadPingTicks(int badPingTicks) {
      this.badPingTicks = badPingTicks;
   }

   public void setPitchGCD(float pitchGCD) {
      this.pitchGCD = pitchGCD;
   }

   public void setLook(Vec3 look) {
      this.look = look;
   }

   public void setPredictYaw(float predictYaw) {
      this.predictYaw = predictYaw;
   }

   public void setLastJoinTime(long lastJoinTime) {
      this.lastJoinTime = lastJoinTime;
   }

   public void setTrackCount(int trackCount) {
      this.trackCount = trackCount;
   }

   public void setFlyingC(boolean flyingC) {
      this.flyingC = flyingC;
   }

   public void setPredictPitch(float predictPitch) {
      this.predictPitch = predictPitch;
   }

   public void setKarhuWorld(KarhuWorld karhuWorld) {
      this.karhuWorld = karhuWorld;
   }

   public void setAllowFlying(boolean allowFlying) {
      this.allowFlying = allowFlying;
   }

   public void setSensitivityY(float sensitivityY) {
      this.sensitivityY = sensitivityY;
   }

   public void setFatigue(int fatigue) {
      this.fatigue = fatigue;
   }

   public void setSpeedBoost(int speedBoost) {
      this.speedBoost = speedBoost;
   }

   public void setSlowness(int slowness) {
      this.slowness = slowness;
   }

   public void setEyeLocation(Vec3 eyeLocation) {
      this.eyeLocation = eyeLocation;
   }

   public void setLocationInited(boolean locationInited) {
      this.locationInited = locationInited;
   }

   public void setNeedExplosionAdditions(boolean needExplosionAdditions) {
      this.needExplosionAdditions = needExplosionAdditions;
   }

   public void setOnGroundPacket(boolean onGroundPacket) {
      this.onGroundPacket = onGroundPacket;
   }

   public void setLastInUnloadedChunk(int lastInUnloadedChunk) {
      this.lastInUnloadedChunk = lastInUnloadedChunk;
   }

   public void setWasInUnloadedChunk(boolean wasInUnloadedChunk) {
      this.wasInUnloadedChunk = wasInUnloadedChunk;
   }

   public void setLastLocationUpdate(long lastLocationUpdate) {
      this.lastLocationUpdate = lastLocationUpdate;
   }

   public int getLastSprintTick() {
      return this.lastSprintTick;
   }

   public void setFuckedTeleport(boolean fuckedTeleport) {
      this.fuckedTeleport = fuckedTeleport;
   }

   public AbilityManager getAbilityManager() {
      return this.abilityManager;
   }

   public boolean isAllowFlyingBukkit() {
      return this.bukkitPlayer == null ? false : this.bukkitPlayer.getAllowFlight();
   }

   public void updateClientVersion(ClientVersion version) {
      if (version == null) {
         version = ClientVersion.getById(Karhu.SERVER_VERSION.getProtocolVersion());
      }

      this.clientVersion = version;
      this.isNewerThan8 = version.isNewerThan(ClientVersion.V_1_8);
      this.isNewerThan12 = version.isNewerThan(ClientVersion.V_1_12_2);
      this.isNewerThan13 = version.isNewerThan(ClientVersion.V_1_13_2);
      this.isNewerThan16 = version.isNewerThan(ClientVersion.V_1_16_4);
   }

   public void setLastLastOnGroundPacket(boolean lastLastOnGroundPacket) {
      this.lastLastOnGroundPacket = lastLastOnGroundPacket;
   }

   public void setLastLastLastLocation(CustomLocation lastLastLastLocation) {
      this.lastLastLastLocation = lastLastLastLocation;
   }

   public int getLastInUnloadedChunk() {
      return this.lastInUnloadedChunk;
   }

   public void setLocationInitedAt(int locationInitedAt) {
      this.locationInitedAt = locationInitedAt;
   }

   public KarhuHandler getCollisionHandler() {
      return this.collisionHandler;
   }

   public void setDidFlagMovement(boolean didFlagMovement) {
      this.didFlagMovement = didFlagMovement;
   }

   public void setSeventeenPlacing(boolean seventeenPlacing) {
      this.seventeenPlacing = seventeenPlacing;
   }

   public void setLastLastLocation(CustomLocation lastLastLocation) {
      this.lastLastLocation = lastLastLocation;
   }

   public void setLastOnGroundPacket(boolean lastOnGroundPacket) {
      this.lastOnGroundPacket = lastOnGroundPacket;
   }

   public void setLastSprintTick(int lastSprintTick) {
      this.lastSprintTick = lastSprintTick;
   }

   public boolean isWasInUnloadedChunk() {
      return this.wasInUnloadedChunk;
   }

   public CustomLocation getSafeGroundSetback() {
      return this.safeGroundSetback;
   }

   public void setLastAllowFlyTick(int lastAllowFlyTick) {
      this.lastAllowFlyTick = lastAllowFlyTick;
   }

   public boolean isBadClientVersion() {
      return this.clientVersion == null || this.clientVersion == ClientVersion.UNKNOWN;
   }

   public void setLastFlyingTicks(int lastFlyingTicks) {
      this.lastFlyingTicks = lastFlyingTicks;
   }

   public void setWasWasInUnloadedChunk(boolean wasWasInUnloadedChunk) {
      this.wasWasInUnloadedChunk = wasWasInUnloadedChunk;
   }

   public void setInUnloadedChunk(boolean inUnloadedChunk) {
      this.inUnloadedChunk = inUnloadedChunk;
   }

   public MovementHandler getMovementHandler() {
      return this.movementHandler;
   }

   public boolean isForceRunCollisions() {
      return this.forceRunCollisions;
   }

   public void setPositionPackets(int positionPackets) {
      this.positionPackets = positionPackets;
   }

   public boolean isInUnloadedChunk() {
      return this.inUnloadedChunk;
   }

   public CustomLocation getLastLastLocation() {
      return this.lastLastLocation;
   }

   public void setClientAirTicks(int clientAirTicks) {
      this.clientAirTicks = clientAirTicks;
   }

   public void setForceRunCollisions(boolean forceRunCollisions) {
      this.forceRunCollisions = forceRunCollisions;
   }

   public boolean isNeedExplosionAdditions() {
      return this.needExplosionAdditions;
   }

   public void setVelocityHorizontal(double velocityHorizontal) {
      this.velocityHorizontal = velocityHorizontal;
   }

   public int getPositionPackets() {
      return this.positionPackets;
   }

   public void setBanned(boolean banned) {
      this.banned = banned;
   }

   public float getWalkSpeed() {
      return this.walkSpeed;
   }

   public void setFlying(boolean flying) {
      this.flying = flying;
   }

   public void setWalkSpeed(float walkSpeed) {
      this.walkSpeed = walkSpeed;
   }

   public void setAbilityManager(AbilityManager abilityManager) {
      this.abilityManager = abilityManager;
   }

   public void setConfirmingFlying(boolean confirmingFlying) {
      this.confirmingFlying = confirmingFlying;
   }

   public void setSlowFallingLevel(int slowFallingLevel) {
      this.slowFallingLevel = slowFallingLevel;
   }

   public void setLastDroppedPackets(int lastDroppedPackets) {
      this.lastDroppedPackets = lastDroppedPackets;
   }

   public void setBoundingBoxInited(boolean boundingBoxInited) {
      this.boundingBoxInited = boundingBoxInited;
   }

   public void setPossiblyTeleporting(boolean possiblyTeleporting) {
      this.possiblyTeleporting = possiblyTeleporting;
   }

   public void setLevitationLevel(int levitationLevel) {
      this.levitationLevel = levitationLevel;
   }

   public void setSmallestRotationGCD(float smallestRotationGCD) {
      this.smallestRotationGCD = smallestRotationGCD;
   }

   public void setFirstChunkMove(CustomLocation firstChunkMove) {
      this.firstChunkMove = firstChunkMove;
   }

   public void setLastPacketDrop(int lastPacketDrop) {
      this.lastPacketDrop = lastPacketDrop;
   }

   public void setWasUnderGhostBlock(boolean isWasUnderGhostBlock) {
      this.isWasUnderGhostBlock = isWasUnderGhostBlock;
   }

   public void setTimerTransactionSent(short timerTransactionSent) {
      this.timerTransactionSent = timerTransactionSent;
   }

   public void setLastTransactionTick(int lastTransactionTick) {
      this.lastTransactionTick = lastTransactionTick;
   }

   public void setInvalidMovementTicks(int invalidMovementTicks) {
      this.invalidMovementTicks = invalidMovementTicks;
   }

   public void setMovementDesynced(boolean movementDesynced) {
      this.movementDesynced = movementDesynced;
   }

   public void setSoulSpeedLevel(int soulSpeedLevel) {
      this.soulSpeedLevel = soulSpeedLevel;
   }

   public void setLastInGhostLiquid(int lastInGhostLiquid) {
      this.lastInGhostLiquid = lastInGhostLiquid;
   }

   public void setGhostBlockSetbacks(double ghostBlockSetbacks) {
      this.ghostBlockSetbacks = ghostBlockSetbacks;
   }

   public void setTeleportManager(TeleportManager teleportManager) {
      this.teleportManager = teleportManager;
   }

   public void setDepthStriderLevel(int depthStriderLevel) {
      this.depthStriderLevel = depthStriderLevel;
   }

   public void setLookMouseDelayFix(Vec3 lookMouseDelayFix) {
      this.lookMouseDelayFix = lookMouseDelayFix;
   }

   public void setLastServerSlot(int lastServerSlot) {
      this.lastServerSlot = lastServerSlot;
   }

   public float getCurrentFriction() {
      return this.currentFriction;
   }

   public void setElapsedOnLiquid(int elapsedOnLiquid) {
      this.elapsedOnLiquid = elapsedOnLiquid;
   }

   public void setRecorrectingSprint(boolean recorrectingSprint) {
      this.recorrectingSprint = recorrectingSprint;
   }

   public void setSlotSwitchTick(int slotSwitchTick) {
      this.slotSwitchTick = slotSwitchTick;
   }

   public void setLastPossibleInUnloadedChunk(int lastPossibleInUnloadedChunk) {
      this.lastPossibleInUnloadedChunk = lastPossibleInUnloadedChunk;
   }

   public void setWasWasSneaking(boolean wasWasSneaking) {
      this.wasWasSneaking = wasWasSneaking;
   }

   public void setExplosionExempt(int explosionExempt) {
      this.explosionExempt = explosionExempt;
   }

   public Map<Integer, ConcurrentLinkedDeque<VelocityPending>> getVelocityPending() {
      return this.velocityPending;
   }

   public int getLastPlacedInside() {
      return this.lastPlacedInside;
   }

   public int getLastPossibleInUnloadedChunk() {
      return this.lastPossibleInUnloadedChunk;
   }

   public int getElapsedUnderBlock() {
      return this.elapsedUnderBlock;
   }

   public void setUnderPlaceTicks(int underPlaceTicks) {
      this.underPlaceTicks = underPlaceTicks;
   }

   public void setTeleportLocation(CustomLocation teleportLocation) {
      this.teleportLocation = teleportLocation;
   }

   public int getLastAttackTick() {
      return this.lastAttackTick;
   }

   public int getElapsedOnLiquid() {
      return this.elapsedOnLiquid;
   }

   public void setLastPlacedInside(int lastPlacedInside) {
      this.lastPlacedInside = lastPlacedInside;
   }

   public void setLastAttackPacket(long lastAttackPacket) {
      this.lastAttackPacket = lastAttackPacket;
   }

   public void setPredictionTicks(int predictionTicks) {
      this.predictionTicks = predictionTicks;
   }

   public void setSpoofPlaceTicks(int spoofPlaceTicks) {
      this.spoofPlaceTicks = spoofPlaceTicks;
   }

   public long getTransactionPing() {
      return this.transactionPing;
   }

   public void setTakingVertical(boolean takingVertical) {
      this.takingVertical = takingVertical;
   }

   public void setElapsedUnderBlock(int elapsedUnderBlock) {
      this.elapsedUnderBlock = elapsedUnderBlock;
   }

   public void setHasReceivedKeepalive(boolean hasReceivedKeepalive) {
      this.hasReceivedKeepalive = hasReceivedKeepalive;
   }

   public int elapsed(int i) {
      return this.totalTicks - i == this.totalTicks ? 1000 : this.totalTicks - i;
   }

   public double getCheckVl(Check<?> check) {
      if (!this.checkVlMap.containsKey(check)) {
         this.checkVlMap.put(check, 0.0);
      }

      return this.checkVlMap.get(check);
   }

   public Vec3 getEyeLocation() {
      return this.eyeLocation;
   }

   public void setGameMode(GameMode gameMode) {
      this.gameMode = gameMode;
   }

   public boolean isBanned() {
      return this.banned;
   }

   public void setEntityIdCancel(int entityIdCancel) {
      this.entityIdCancel = entityIdCancel;
   }

   public boolean isForceCancelReach() {
      return this.forceCancelReach;
   }

   public int getLastOnClimbable() {
      return this.lastOnClimbable;
   }

   public boolean isCollidedHorizontally() {
      return this.collidedHorizontally;
   }

   public boolean isWasCollidedHorizontally() {
      return this.wasCollidedHorizontally;
   }

   public BoundingBox getLastBoundingBox() {
      return this.lastBoundingBox;
   }

   public void setCancelTripleHit(boolean cancelTripleHit) {
      this.cancelTripleHit = cancelTripleHit;
   }

   public int getLastCollidedVGhost() {
      return this.lastCollidedVGhost;
   }

   public void setCancelNextHitR(boolean cancelNextHitR) {
      this.cancelNextHitR = cancelNextHitR;
   }

   public int getLastPistonPush() {
      return this.lastPistonPush;
   }

   public int getLastCollidedGhost() {
      return this.lastCollidedGhost;
   }

   public void setCancelNextHitH(boolean cancelNextHitH) {
      this.cancelNextHitH = cancelNextHitH;
   }

   public void setLastMovementFlag(int lastMovementFlag) {
      this.lastMovementFlag = lastMovementFlag;
   }

   public void setReduceNextDamage(boolean reduceNextDamage) {
      this.reduceNextDamage = reduceNextDamage;
   }

   public int getLastVelocityTaken() {
      return this.lastVelocityTaken;
   }

   public int getUnderPlaceTicks() {
      return this.underPlaceTicks;
   }

   public int getLastMovementFlag() {
      return this.lastMovementFlag;
   }

   public boolean isWasWasInUnloadedChunk() {
      return this.wasWasInUnloadedChunk;
   }

   public long getServerTick() {
      return this.serverTick;
   }

   public int getLastOnHalfBlock() {
      return this.lastOnHalfBlock;
   }

   public float getLastTickFriction() {
      return this.lastTickFriction;
   }

   public int getVelocityXZTicks() {
      return this.velocityXZTicks;
   }

   public boolean isConfirmingFlying() {
      return this.confirmingFlying;
   }

   public double getVelocityHorizontal() {
      return this.velocityHorizontal;
   }

   public int getPredictionTicks() {
      return this.predictionTicks;
   }

   public int getDepthStriderLevel() {
      return this.depthStriderLevel;
   }

   public int getSlotSwitchTick() {
      return this.slotSwitchTick;
   }

   public int getExplosionExempt() {
      return this.explosionExempt;
   }

   public boolean isConfirmingVelocity() {
      return this.confirmingVelocity;
   }

   public int getLastCollidedWithEntity() {
      return this.lastCollidedWithEntity;
   }

   public int getLastVelocityYReset() {
      return this.lastVelocityYReset;
   }

   public int getSlowFallingLevel() {
      return this.slowFallingLevel;
   }

   public int getClientAirTicks() {
      return this.clientAirTicks;
   }

   public int getLastPacketDrop() {
      return this.lastPacketDrop;
   }

   public boolean isAboveButNotInWater() {
      return this.aboveButNotInWater;
   }

   public int getLastInGhostLiquid() {
      return this.lastInGhostLiquid;
   }

   public boolean isUnderGhostBlock() {
      return this.underGhostBlock;
   }

   public boolean isWasOnGroundServer() {
      return this.wasOnGroundServer;
   }

   public int getLastConfirmingState() {
      return this.lastConfirmingState;
   }

   public int getVelocityYTicks() {
      return this.velocityYTicks;
   }

   public int getLevitationLevel() {
      return this.levitationLevel;
   }

   public boolean isLastLastOnGroundPacket() {
      return this.lastLastOnGroundPacket;
   }

   public void setRemovingObject(boolean removingObject) {
      this.removingObject = removingObject;
   }

   public int getLastSlimePistonPush() {
      return this.lastSlimePistonPush;
   }

   public boolean isWasOnComparator() {
      return this.wasOnComparator;
   }

   public boolean isHasReceivedKeepalive() {
      return this.hasReceivedKeepalive;
   }

   public int getSoulSpeedLevel() {
      return this.soulSpeedLevel;
   }

   public long getTransactionClock() {
      return this.transactionClock;
   }

   public GameMode getGameMode() {
      return this.gameMode;
   }

   public void setUser(User user) {
      this.user = user;
   }

   public void setSpectating(boolean spectating) {
      this.spectating = spectating;
   }

   public void setEntityId(int entityId) {
      this.entityId = entityId;
   }

   public void queueToPrePing(Callback<Integer> callback) {
      this.netHandler.queueToPrePing(callback);
   }

   public NetHandler getNetHandler() {
      return this.netHandler;
   }

   public void setPingInTicks(int pingInTicks) {
      this.pingInTicks = pingInTicks;
   }

   public void queueToPostPing(Callback<Integer> callback) {
      this.netHandler.queueToPostPing(callback);
   }

   public void setLastPingTime(long lastPingTime) {
      this.lastPingTime = lastPingTime;
   }

   public void setVehicleId(int vehicleId) {
      this.vehicleId = vehicleId;
   }

   public void setRiding(boolean riding) {
      this.riding = riding;
   }

   public int getLastUnmount() {
      return this.lastUnmount;
   }

   public void velocityTick(Vector vector) {
      this.setLastVelocityTaken(this.getTotalTicks());
      this.setVelocityXZTicks(0);
      this.setVelocityYTicks(0);
      this.setVelocityX(vector.getX());
      this.setVelocityY(vector.getY());
      this.setVelocityZ(vector.getZ());
      int velocityH = (int)Math.ceil((Math.abs(vector.getX()) + Math.abs(vector.getZ())) / 2.0 + 2.0) * 4;
      int velocityV = (int)Math.ceil(FastMath.pow(Math.abs(vector.getY()) + 2.0, 2)) * 2;
      this.setMaxVelocityXZTicks(velocityH + velocityV + 5);
      this.setMaxVelocityYTicks(velocityV);
      this.setTakingVertical(true);
      this.setVelocityHorizontal(MathUtil.hypot(vector.getX(), vector.getZ()));
      this.setTickedVelocity(vector.clone());
      this.setConfirmingVelocity(false);
      this.setNeedExplosionAdditions(false);
   }

   public void setInBed(boolean inBed) {
      this.inBed = inBed;
   }

   public void setLastUnmount(int lastUnmount) {
      this.lastUnmount = lastUnmount;
   }

   public void setClientVersion(ClientVersion clientVersion) {
      this.clientVersion = clientVersion;
   }

   public void setVehicle(Entity vehicle) {
      this.vehicle = vehicle;
   }

   public void queueToFlying(int delay, Callback<Integer> callback) {
      int key = this.totalTicks + delay;
      if (this.tasks.containsKey(key)) {
         this.tasks.get(key).addTask(callback);
      } else {
         this.tasks.put(key, new TaskData(key, callback));
      }
   }

   public void setServerTick(long serverTick) {
      this.serverTick = serverTick;
   }

   public void setBedPos(Vec3 bedPos) {
      this.bedPos = bedPos;
   }

   public Thread getThread() {
      return this.thread;
   }

   public void setSendingPledgePackets(boolean sendingPledgePackets) {
      this.sendingPledgePackets = sendingPledgePackets;
   }

   public void setServerGroundTicks(int serverGroundTicks) {
      this.serverGroundTicks = serverGroundTicks;
   }

   public void setFinalCollidedH(boolean finalCollidedH) {
      this.finalCollidedH = finalCollidedH;
   }

   public void setLastBoundingBox(BoundingBox lastBoundingBox) {
      this.lastBoundingBox = lastBoundingBox;
   }

   public void setTicksOnGhostBlock(int ticksOnGhostBlock) {
      this.ticksOnGhostBlock = ticksOnGhostBlock;
   }

   public void setReceivedConfirms(int receivedConfirms) {
      this.receivedConfirms = receivedConfirms;
   }

   public void setDesyncedBlockHandler(DesyncedBlockHandler desyncedBlockHandler) {
      this.desyncedBlockHandler = desyncedBlockHandler;
   }

   public void setLastCollidedWithEntity(int lastCollidedWithEntity) {
      this.lastCollidedWithEntity = lastCollidedWithEntity;
   }

   public void setMovementHandler(MovementHandler movementHandler) {
      this.movementHandler = movementHandler;
   }

   public void setWasFullyInsideBlock(boolean wasFullyInsideBlock) {
      this.wasFullyInsideBlock = wasFullyInsideBlock;
   }

   public void setTicksOnBlockHandlerNotEnabled(int ticksOnBlockHandlerNotEnabled) {
      this.ticksOnBlockHandlerNotEnabled = ticksOnBlockHandlerNotEnabled;
   }

   public void setLastAttributeSpeed(float lastAttributeSpeed) {
      this.lastAttributeSpeed = lastAttributeSpeed;
   }

   public void setAttackedSinceVelocity(boolean attackedSinceVelocity) {
      this.attackedSinceVelocity = attackedSinceVelocity;
   }

   public void setBrokenVelocityVerify(boolean brokenVelocityVerify) {
      this.brokenVelocityVerify = brokenVelocityVerify;
   }

   public void setLastOnClimbable(int lastOnClimbable) {
      this.lastOnClimbable = lastOnClimbable;
   }

   public void setWasCollidedHorizontally(boolean wasCollidedHorizontally) {
      this.wasCollidedHorizontally = wasCollidedHorizontally;
   }

   public void setLastTickSecondConfirmationUid(int lastTickSecondConfirmationUid) {
      this.lastTickSecondConfirmationUid = lastTickSecondConfirmationUid;
   }

   public void setWasWasSprinting(boolean wasWasSprinting) {
      this.wasWasSprinting = wasWasSprinting;
   }

   public void setLastAttackTick(int lastAttackTick) {
      this.lastAttackTick = lastAttackTick;
   }

   public void setJumpedLastTick(boolean jumpedLastTick) {
      this.jumpedLastTick = jumpedLastTick;
   }

   public void setExitingVehicle(boolean exitingVehicle) {
      this.exitingVehicle = exitingVehicle;
   }

   public void setHasSentTickFirst(boolean hasSentTickFirst) {
      this.hasSentTickFirst = hasSentTickFirst;
   }

   public void setOnGroundServer(boolean onGroundServer) {
      this.onGroundServer = onGroundServer;
   }

   public void setWasOnGroundServer(boolean wasOnGroundServer) {
      this.wasOnGroundServer = wasOnGroundServer;
   }

   public void setAboveButNotInWater(boolean aboveButNotInWater) {
      this.aboveButNotInWater = aboveButNotInWater;
   }

   public void setWaterAlmostOnFeet(boolean waterAlmostOnFeet) {
      this.waterAlmostOnFeet = waterAlmostOnFeet;
   }

   public void setLastOnHalfBlock(int lastOnHalfBlock) {
      this.lastOnHalfBlock = lastOnHalfBlock;
   }

   public void setLastBlockInside(Block lastBlockInside) {
      this.lastBlockInside = lastBlockInside;
   }

   public void setWaitingConfirms(Map<Short, ObjectArrayList<Consumer<Short>>> waitingConfirms) {
      this.waitingConfirms = waitingConfirms;
   }

   public void setLastTickFirstConfirmationUid(int lastTickFirstConfirmationUid) {
      this.lastTickFirstConfirmationUid = lastTickFirstConfirmationUid;
   }

   public void setLastVelocityXZReset(int lastVelocityXZReset) {
      this.lastVelocityXZReset = lastVelocityXZReset;
   }

   public void setCollidedHorizontalClient(boolean collidedHorizontalClient) {
      this.collidedHorizontalClient = collidedHorizontalClient;
   }

   public void setCollidedWithPane(boolean collidedWithPane) {
      this.collidedWithPane = collidedWithPane;
   }

   public void setFullyInsideBlock(boolean fullyInsideBlock) {
      this.fullyInsideBlock = fullyInsideBlock;
   }

   public void setAddedTeleports(int addedTeleports) {
      this.addedTeleports = addedTeleports;
   }

   public void setUnderBlockStrict(boolean underBlockStrict) {
      this.underBlockStrict = underBlockStrict;
   }

   public void setWasOnComparator(boolean wasOnComparator) {
      this.wasOnComparator = wasOnComparator;
   }

   public void setWasOnClimbable(boolean wasOnClimbable) {
      this.wasOnClimbable = wasOnClimbable;
   }

   public void setWasWasOnClimbable(boolean wasWasOnClimbable) {
      this.wasWasOnClimbable = wasWasOnClimbable;
   }

   public void setJumpMovementFactor(float jumpMovementFactor) {
      this.jumpMovementFactor = jumpMovementFactor;
   }

   public void setTickSecondConfirmationUid(int tickSecondConfirmationUid) {
      this.tickSecondConfirmationUid = tickSecondConfirmationUid;
   }

   public void setLClientAirTicks(int lClientAirTicks) {
      this.lClientAirTicks = lClientAirTicks;
   }

   public void setCollisionHandler(KarhuHandler collisionHandler) {
      this.collisionHandler = collisionHandler;
   }

   public void setLastCollidedVGhost(int lastCollidedVGhost) {
      this.lastCollidedVGhost = lastCollidedVGhost;
   }

   public void setJumpedCurrentTick(boolean jumpedCurrentTick) {
      this.jumpedCurrentTick = jumpedCurrentTick;
   }

   public void setLastInLiquidOffset(int lastInLiquidOffset) {
      this.lastInLiquidOffset = lastInLiquidOffset;
   }

   public void setLastConfirmingState(int lastConfirmingState) {
      this.lastConfirmingState = lastConfirmingState;
   }

   public void setConfirmedVersion(boolean confirmedVersion) {
      this.confirmedVersion = confirmedVersion;
   }

   public void setCollidedWithFence(boolean collidedWithFence) {
      this.collidedWithFence = collidedWithFence;
   }

   public void setAttributeSpeed(float attributeSpeed) {
      this.attributeSpeed = attributeSpeed;
   }

   public void setLastVelocityYReset(int lastVelocityYReset) {
      this.lastVelocityYReset = lastVelocityYReset;
   }

   public void setConfirmedVersion2(boolean confirmedVersion2) {
      this.confirmedVersion2 = confirmedVersion2;
   }

   public void setPredictionHandler(AbstractPredictionHandler predictionHandler) {
      this.predictionHandler = predictionHandler;
   }

   public void setTickTransactionId(short tickTransactionId) {
      this.tickTransactionId = tickTransactionId;
   }

   public void setClientGroundTicks(int clientGroundTicks) {
      this.clientGroundTicks = clientGroundTicks;
   }

   public void setLastCollidedGhost(int lastCollidedGhost) {
      this.lastCollidedGhost = lastCollidedGhost;
   }

   public void setNoMoveNextFlying(boolean noMoveNextFlying) {
      this.noMoveNextFlying = noMoveNextFlying;
   }

   public void setCollidedHorizontally(boolean collidedHorizontally) {
      this.collidedHorizontally = collidedHorizontally;
   }

   public void setHasTeleportedOnce(boolean isHasTeleportedOnce) {
      this.isHasTeleportedOnce = isHasTeleportedOnce;
   }

   public void setElapsedSinceBridgePlace(int elapsedSinceBridgePlace) {
      this.elapsedSinceBridgePlace = elapsedSinceBridgePlace;
   }

   public void setTickFirstConfirmationUid(int tickFirstConfirmationUid) {
      this.tickFirstConfirmationUid = tickFirstConfirmationUid;
   }

   public void setCollidedWithCactus(boolean collidedWithCactus) {
      this.collidedWithCactus = collidedWithCactus;
   }

   public void setInsideTrapdoor(boolean insideTrapdoor) {
      this.insideTrapdoor = insideTrapdoor;
   }

   public void setLastJumpMovementFactor(float lastJumpMovementFactor) {
      this.lastJumpMovementFactor = lastJumpMovementFactor;
   }

   public void setOnTopGhostBlock(boolean onTopGhostBlock) {
      this.onTopGhostBlock = onTopGhostBlock;
   }

   public void setLastOnWaterOffset(boolean lastOnWaterOffset) {
      this.lastOnWaterOffset = lastOnWaterOffset;
   }

   public void setCollidedWithLivingEntity(boolean collidedWithLivingEntity) {
      this.collidedWithLivingEntity = collidedWithLivingEntity;
   }

   public void setCurrentFriction(float currentFriction) {
      this.currentFriction = currentFriction;
   }

   public void setLastBlockSneak(boolean lastBlockSneak) {
      this.lastBlockSneak = lastBlockSneak;
   }

   public void setLastTickFriction(float lastTickFriction) {
      this.lastTickFriction = lastTickFriction;
   }

   public void setLastOnGroundMath(boolean lastOnGroundMath) {
      this.lastOnGroundMath = lastOnGroundMath;
   }

   public void setLastLastOnGroundMath(boolean lastLastOnGroundMath) {
      this.lastLastOnGroundMath = lastLastOnGroundMath;
   }

   public void setUnderGhostBlock(boolean underGhostBlock) {
      this.underGhostBlock = underGhostBlock;
   }

   public int getCancelHitsTick() {
      return this.cancelHitsTick;
   }

   public boolean isReduceNextDamage() {
      return this.reduceNextDamage;
   }

   public void setLastWorldChange(int lastWorldChange) {
      this.lastWorldChange = lastWorldChange;
   }

   public boolean isHasTeleportedOnce() {
      return this.isHasTeleportedOnce;
   }

   public boolean isAbusingVelocity() {
      return this.abusingVelocity;
   }

   public int getEntityIdCancel() {
      return this.entityIdCancel;
   }

   public void setAbusingVelocity(boolean abusingVelocity) {
      this.abusingVelocity = abusingVelocity;
   }

   public void setLastSlimePistonPush(int lastSlimePistonPush) {
      this.lastSlimePistonPush = lastSlimePistonPush;
   }

   public boolean isRecorrectingSprint() {
      return this.recorrectingSprint;
   }

   public boolean isCancelTripleHit() {
      return this.cancelTripleHit;
   }

   public void setLastPistonPush(int lastPistonPush) {
      this.lastPistonPush = lastPistonPush;
   }

   public void setForceCancelReach(boolean forceCancelReach) {
      this.forceCancelReach = forceCancelReach;
   }

   public short getNextTransactionId() {
      ++this.transactionId;
      if (this.transactionId > -20001) {
         this.transactionId = -32768;
      }

      return this.transactionId;
   }

   public short getNextTransactionIdSilent() {
      short predict = this.transactionId;
      if (++predict > -20001) {
         predict = -32768;
      }

      return predict;
   }

   public boolean isWasUnderGhostBlock() {
      return this.isWasUnderGhostBlock;
   }

   public int getLastVelocityXZReset() {
      return this.lastVelocityXZReset;
   }

   public int getSpoofPlaceTicks() {
      return this.spoofPlaceTicks;
   }

   public int getLClientAirTicks() {
      return this.lClientAirTicks;
   }

   public float getAttributeSpeed() {
      return this.attributeSpeed;
   }

   public int getTickSecondConfirmationUid() {
      return this.tickSecondConfirmationUid;
   }

   public short getTickTransactionId() {
      return this.tickTransactionId;
   }

   public int getTicksOnBlockHandlerNotEnabled() {
      return this.ticksOnBlockHandlerNotEnabled;
   }

   public int getTickFirstConfirmationUid() {
      return this.tickFirstConfirmationUid;
   }

   public boolean isWasWasSprinting() {
      return this.wasWasSprinting;
   }

   public boolean isWasWasOnClimbable() {
      return this.wasWasOnClimbable;
   }

   public boolean isCollidedWithLivingEntity() {
      return this.collidedWithLivingEntity;
   }

   public boolean isWaterAlmostOnFeet() {
      return this.waterAlmostOnFeet;
   }

   public boolean isLastOnWaterOffset() {
      return this.lastOnWaterOffset;
   }

   public boolean isOnTopGhostBlock() {
      return this.onTopGhostBlock;
   }

   public double getGhostBlockSetbacks() {
      return this.ghostBlockSetbacks;
   }

   public long getLastTransactionPing() {
      return this.lastTransactionPing;
   }

   public boolean isConfirmedVersion() {
      return this.confirmedVersion;
   }

   public CustomLocation getLastLastLastLocation() {
      return this.lastLastLastLocation;
   }

   public boolean isHasSentTickFirst() {
      return this.hasSentTickFirst;
   }

   public boolean isCollidedWithCactus() {
      return this.collidedWithCactus;
   }

   public int getTicksOnGhostBlock() {
      return this.ticksOnGhostBlock;
   }

   public boolean isPlayerVelocityCalled() {
      return this.playerVelocityCalled;
   }

   public boolean isAttackedSinceVelocity() {
      return this.attackedSinceVelocity;
   }

   public long getLastAttackPacket() {
      return this.lastAttackPacket;
   }

   public boolean isLastOnGroundMath() {
      return this.lastOnGroundMath;
   }

   public short getTimerTransactionSent() {
      return this.timerTransactionSent;
   }

   public int getReceivedConfirms() {
      return this.receivedConfirms;
   }

   public long getLastTransactionPingUpdate() {
      return this.lastTransactionPingUpdate;
   }

   public float getLastJumpMovementFactor() {
      return this.lastJumpMovementFactor;
   }

   public int getLastTickFirstConfirmationUid() {
      return this.lastTickFirstConfirmationUid;
   }

   public int getElapsedSinceBridgePlace() {
      return this.elapsedSinceBridgePlace;
   }

   public int getServerGroundTicks() {
      return this.serverGroundTicks;
   }

   public int getLastAllowFlyTick() {
      return this.lastAllowFlyTick;
   }

   public boolean isCollidedWithPane() {
      return this.collidedWithPane;
   }

   public int getAddedTeleports() {
      return this.addedTeleports;
   }

   public boolean isLastLastOnGroundMath() {
      return this.lastLastOnGroundMath;
   }

   public int getMaxVelocityYTicks() {
      return this.maxVelocityYTicks;
   }

   public int getLastInLiquidOffset() {
      return this.lastInLiquidOffset;
   }

   public int getLastWorldChange() {
      return this.lastWorldChange;
   }

   public long getLastTransaction() {
      return this.lastTransaction;
   }

   public long getLastTeleportPacket() {
      return this.lastTeleportPacket;
   }

   public int getClientGroundTicks() {
      return this.clientGroundTicks;
   }

   public boolean isJumpedCurrentTick() {
      return this.jumpedCurrentTick;
   }

   public boolean isWasFullyInsideBlock() {
      return this.wasFullyInsideBlock;
   }

   public AbstractPredictionHandler getPredictionHandler() {
      return this.predictionHandler;
   }

   @Deprecated
   public boolean couldBeTeleporting(int ticks) {
      return this.totalTicks - this.lastTeleport <= ticks || this.isPossiblyTeleporting();
   }

   public IVehicleHandler getVehicleHandler() {
      return this.vehicleHandler;
   }

   public float getJumpMovementFactor() {
      return this.jumpMovementFactor;
   }

   public int getMaxVelocityXZTicks() {
      return this.maxVelocityXZTicks;
   }

   public Map<Check, Set<Long>> getCheckViolationTimes() {
      return this.checkViolationTimes;
   }

   public boolean isPlayerExplodeCalled() {
      return this.playerExplodeCalled;
   }

   public boolean isCollidedHorizontalClient() {
      return this.collidedHorizontalClient;
   }

   public boolean isCollidedWithFence() {
      return this.collidedWithFence;
   }

   public boolean isFullyInsideBlock() {
      return this.fullyInsideBlock;
   }

   @Deprecated
   public boolean couldBeUnloadedClient() {
      return this.elapsed(this.getLastInUnloadedChunk()) <= MathUtil.getPingInTicks(this.getTransactionPing()) + 2
         || this.elapsed(this.getLastWorldChange()) <= MathUtil.getPingInTicks(this.getTransactionPing()) + 10
         || this.elapsed(this.getLastPossibleInUnloadedChunk()) <= 2;
   }

   public float getLastAttributeSpeed() {
      return this.lastAttributeSpeed;
   }

   public boolean isSendingPledgePackets() {
      return this.sendingPledgePackets;
   }

   public boolean isNoMoveNextFlying() {
      return this.noMoveNextFlying;
   }

   public boolean isUnderBlockStrict() {
      return this.underBlockStrict;
   }

   public int getLastTickSecondConfirmationUid() {
      return this.lastTickSecondConfirmationUid;
   }

   public boolean isConfirmedVersion2() {
      return this.confirmedVersion2;
   }

   public boolean isBrokenVelocityVerify() {
      return this.brokenVelocityVerify;
   }

   public Block getLastBlockInside() {
      return this.lastBlockInside;
   }

   public CustomLocation getFirstChunkMove() {
      return this.firstChunkMove;
   }

   public int getLastServerSlot() {
      return this.lastServerSlot;
   }

   public int getLastFlyingTicks() {
      return this.lastFlyingTicks;
   }

   public int getLastLastClientTransaction() {
      return this.lastLastClientTransaction;
   }

   public int getLastDroppedPackets() {
      return this.lastDroppedPackets;
   }

   public boolean isFirstTransactionSent() {
      return this.firstTransactionSent;
   }

   public boolean isRidingUncertain() {
      return this.ridingUncertain;
   }

   public float getSmallestRotationGCD() {
      return this.smallestRotationGCD;
   }

   public int getLastTransactionTick() {
      return this.lastTransactionTick;
   }

   public Map<SubCategory, Pair<Integer, Integer>> getExemptCategoryMap() {
      return this.exemptCategoryMap;
   }

   public int getInvalidMovementTicks() {
      return this.invalidMovementTicks;
   }

   public boolean isMovementDesynced() {
      return this.movementDesynced;
   }

   public CustomLocation getTeleportLocation() {
      return this.teleportLocation;
   }

   public Vec3 getLookMouseDelayFix() {
      return this.lookMouseDelayFix;
   }

   public boolean isBoundingBoxInited() {
      return this.boundingBoxInited;
   }

   public void setCheckViolationTimes(Map<Check, Set<Long>> checkViolationTimes) {
      this.checkViolationTimes = checkViolationTimes;
   }

   public int getLocationInitedAt() {
      return this.locationInitedAt;
   }

   public void setVehicleHandler(IVehicleHandler vehicleHandler) {
      this.vehicleHandler = vehicleHandler;
   }

   public void setCancelHitsTick(int cancelHitsTick) {
      this.cancelHitsTick = cancelHitsTick;
   }
}
