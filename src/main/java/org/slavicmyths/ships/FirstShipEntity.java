package org.slavicmyths.ships;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * First independent two-deck ship. Does NOT change the four saved raft formats.
 * A native-sized collision hull supplies boat motion; separate local geometry
 * supplies the floor, below-deck walls and distinct helmsman's attachment.
 *
 * Runtime multiplayer acceptance is required before enabling this on a release.
 */
public final class FirstShipEntity extends Boat {
    private static final EntityDataAccessor<Float> SURFACE = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> MODULES = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> NATIVE = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.BOOLEAN);
    /** Authoritative world-block anchor; unlike Boat.setPos, synced on EVERY voxel step. */
    private static final EntityDataAccessor<BlockPos> ANCHOR = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> TURNS = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RIG = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FABRIC = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GEOMETRY = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> MOVING = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> MOTION_CARGO_VIEW = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> STRUCTURE_VIEW = SynchedEntityData.defineId(FirstShipEntity.class, EntityDataSerializers.STRING);
    private static final float NO_SURFACE = -10000F;
    private static final double DRAFT = 0.14D;
    private static final int CELLS = FirstShipHull.WIDTH * FirstShipHull.LENGTH;
    private final Map<Integer, SimpleContainer> inventory = new HashMap<>();
    private final Map<Integer, Integer> kind = new HashMap<>();
    private final Map<Integer, SimpleContainerData> furnaces = new HashMap<>();
    private final Map<Integer, SimpleContainer> movingCargoSlots = new HashMap<>();
    private final Map<Integer, SimpleContainerData> movingCargoHeat = new HashMap<>();
    private final Set<Integer> loadingMovingCargo = new HashSet<>();
    private boolean packing;
    /** Malformed saved inventories are retained byte-for-byte; packing is blocked. */
    private ListTag quarantinedModules;
    private ListTag pendingBlockCargo = new ListTag();
    private BlockPos blockAnchor = BlockPos.ZERO;
    private int quarterTurns;
    private boolean nativeHull;
    private int rigLevel;
    private int fabricQuality;
    private int geometryVersion = 5;
    private ShipDesign design = ShipDesign.defaults();
    private ShipSuperstructurePlan superstructure;
    private double nextHelmAllowedTick = -100D;
    private boolean legacyConversionTried;
    private boolean legacySailUpgradePending;
    private boolean visualSailUpgradeTried;
    /** New ships opt into the new body; older worlds stay in their proven mode. */
    private boolean smoothEnabled;
    private ShipMovingContraption movingBody;
    private ShipRigidMotion.Control helmControl = ShipRigidMotion.Control.IDLE;
    private long helmControlTick = -100L;
    private record PendingDismount(double localX, double localZ,
                                   double deckOffsetY, long expires) {}
    private final java.util.Map<java.util.UUID, PendingDismount> pendingDismounts =
            new java.util.HashMap<>();
    private int clientMotionSteps;
    private double clientMotionX, clientMotionY, clientMotionZ;
    private float clientMotionYaw;
    private long clientPoseFrame;
    private double clientPreviousX, clientPreviousZ;
    private float clientPreviousYaw;

    public long clientPoseFrame() { return clientPoseFrame; }
    public double clientPreviousX() { return clientPreviousX; }
    public double clientPreviousZ() { return clientPreviousZ; }
    public float clientPreviousYaw() { return clientPreviousYaw; }

    /** Nearby clients receive an explicit pose even around a rider dismount. */
    public void acceptCaptainMotionPose(double x, double y, double z, float yaw) {
        if (!level().isClientSide || !isMoving() || !Double.isFinite(x)
                || !Double.isFinite(y) || !Double.isFinite(z) || !Float.isFinite(yaw)) return;
        clientMotionX = x;
        clientMotionY = y;
        clientMotionZ = z;
        clientMotionYaw = yaw;
        // Network packets can arrive in alternating client ticks. Ease toward
        // each authoritative pose instead of jumping the full missed distance.
        clientMotionSteps = 3;
    }

    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        if (!level().isClientSide || !isMoving()) {
            super.lerpTo(x, y, z, yaw, pitch, steps);
            return;
        }
        // Continuous ships use MotionPose as their sole client pose stream.
        // Vanilla Boat tracking can arrive late and must not reverse it.
    }
    public boolean isMoving() { return entityData.get(MOVING); }
    public String movingCargoView() { return entityData.get(MOTION_CARGO_VIEW); }
    public Map<BlockPos, net.minecraft.world.level.block.state.BlockState> serverMovingPalette() {
        return movingBody == null ? null : movingBody.palette();
    }
    public double serverMovingSpeed() {
        return movingBody == null ? 0D : movingBody.motion().speed();
    }
    public void enableSmoothMotion() { smoothEnabled = true; }
    public void equipStarterSail() { setRigging(1, 1); setGeometryVersion(8); }


    public BlockPos blockAnchor() {
        // Both logical sides read the same replicated anchor. The saved field
        // is only the server-side transactional source for hull relocation.
        return isMoving() ? BlockPos.containing(getX(), getY(), getZ())
                : isNativeHull() ? entityData.get(ANCHOR) : blockAnchor;
    }
    public int quarterTurns() { return entityData.get(TURNS); }
    /** Synced cardinal heading: client yaw interpolation must not move the wheel seat. */
    public float shipYaw() { return isMoving() ? getYRot() : quarterTurns() * 90F; }
    public boolean isNativeHull() { return entityData.get(NATIVE); }
    public int rigLevel() { return entityData.get(RIG); }
    public int fabricQuality() { return entityData.get(FABRIC); }
    public int geometryVersion() { return level().isClientSide ? entityData.get(GEOMETRY) : geometryVersion; }
    private void setGeometryVersion(int value) {
        geometryVersion = value;
        entityData.set(GEOMETRY, value);
    }
    /** Future customizer fields stay stable across a voyage and item packing. */
    public ShipDesign design() { return design; }
    /** A null structure means the old, preserved V2–V8 7x11 layout. */
    public ShipSuperstructurePlan superstructure() {
        if (!level().isClientSide) return superstructure;
        String view = entityData.get(STRUCTURE_VIEW);
        if (view.isEmpty()) return null;
        try { return ShipSuperstructureData.fromSync(view); }
        catch (RuntimeException invalid) { return null; }
    }
    private void setSuperstructure(ShipSuperstructurePlan plan) {
        superstructure = ShipSuperstructureData.validateBounds(plan);
        entityData.set(STRUCTURE_VIEW, ShipSuperstructureData.sync(superstructure));
    }
    public boolean isVoxelShip() { return geometryVersion() == 9 && superstructure() != null; }
    private void setRigging(int level, int fabric) {
        new ShipRigging(level, fabric); // validate before changing live state
        rigLevel = level;
        fabricQuality = fabric;
        entityData.set(RIG, level);
        entityData.set(FABRIC, fabric);
    }
    public ListTag pendingBlockCargo() { return pendingBlockCargo.copy(); }

    public void initializeNativeHull(BlockPos anchor, int turns) {
        blockAnchor = anchor.immutable();
        quarterTurns = Math.floorMod(turns, 4);
        nativeHull = true;
        entityData.set(NATIVE, true);
        entityData.set(ANCHOR, blockAnchor);
        entityData.set(TURNS, quarterTurns);
        setYRot(quarterTurns * 90F);
        setYHeadRot(quarterTurns * 90F);
        setPos(anchor.getX() + .5D, anchor.getY(), anchor.getZ() + .5D);
        setDeltaMovement(Vec3.ZERO);
    }

    public FirstShipEntity(EntityType<? extends FirstShipEntity> type, Level world) {
        super(type, world);
        setVariant(Type.SPRUCE);
        setInvulnerable(true);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SURFACE, NO_SURFACE);
        builder.define(MODULES, "0".repeat(CELLS * 2));
        builder.define(NATIVE, false);
        builder.define(ANCHOR, BlockPos.ZERO);
        builder.define(TURNS, 0);
        builder.define(RIG, 0);
        builder.define(FABRIC, 0);
        builder.define(GEOMETRY, 5);
        builder.define(MOVING, false);
        builder.define(MOTION_CARGO_VIEW, "");
        builder.define(STRUCTURE_VIEW, "");
    }

    public int helmLocalZ() {
        ShipSuperstructurePlan plan = superstructure();
        return geometryVersion() == 9 && plan != null ? -plan.hullLength() / 2
                + (plan.bridge() ? 4 : plan.cabin() ? 9 : 3) : FirstShipHull.HELM_Z;
    }
    public int helmStandingHeight() {
        ShipSuperstructurePlan plan = superstructure();
        return geometryVersion() == 9 && plan != null && plan.bridge() ? 10 : 5;
    }
    private transient ShipSuperstructurePlan voxelCachePlan;
    private transient java.util.Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> voxelCache =
            java.util.Map.of();
    private java.util.Map<ShipHullVoxelLayout.Cell, ShipHullVoxelLayout.Part> voxelParts() {
        ShipSuperstructurePlan plan = superstructure();
        if (plan == null) return java.util.Map.of();
        if (!plan.equals(voxelCachePlan)) {
            voxelCache = ShipHullVoxelLayout.generate(plan);
            voxelCachePlan = plan;
        }
        return voxelCache;
    }
    public boolean voxelLadderAt(double x, double y, double z) {
        if (!isVoxelShip()) return false;
        int xx = (int)Math.floor(x + .5D), zz = (int)Math.floor(z + .5D);
        int yy = (int)Math.floor(y);
        return voxelParts().get(new ShipHullVoxelLayout.Cell(xx, yy, zz))
                == ShipHullVoxelLayout.Part.LADDER;
    }
    public int supportedVoxelFloor(double x, double z, double localY, int preferred) {
        if (!isVoxelShip()) return -1;
        int xx = (int)Math.floor(x + .5D), zz = (int)Math.floor(z + .5D);
        for (int surface : new int[]{13, 10, 9, 5, 1}) {
            if (preferred >= 0 && preferred != surface) continue;
            var part = voxelParts().get(new ShipHullVoxelLayout.Cell(xx, surface - 1, zz));
            if (part != ShipHullVoxelLayout.Part.DECK
                    && part != ShipHullVoxelLayout.Part.BRIDGE
                    && part != ShipHullVoxelLayout.Part.HULL
                    && part != ShipHullVoxelLayout.Part.KEEL) continue;
            if (localY >= surface - .56D && localY <= surface + 2.6D) return surface;
        }
        return -1;
    }
    public double deckY() { return getY() + FirstShipHull.MAIN_DECK; }
    public double holdY() { return getY() + FirstShipHull.HOLD_FLOOR; }
    public int moduleKind(int floor, int x, int z) {
        int tile = tileId(floor, x, z);
        if (tile < 0) return 0;
        String state = entityData.get(MODULES);
        return tile < state.length() ? state.charAt(tile) - '0' : 0;
    }

    private static int tileId(int floor, int x, int z) {
        return floor < 0 || floor > 1 || x < 0 || x >= FirstShipHull.WIDTH
                || z < 0 || z >= FirstShipHull.LENGTH ? -1 : floor * CELLS + z * FirstShipHull.WIDTH + x;
    }

    public static int tileAt(double localX, double localZ, int floor) {
        int x = (int)Math.floor(localX + FirstShipHull.WIDTH / 2D);
        int z = (int)Math.floor(localZ + FirstShipHull.LENGTH / 2D);
        if (floor < 0 || floor > 1 || !FirstShipHull.withinHull(localX, localZ, 0.46D)) return -1;
        double centerX = x + .5D - FirstShipHull.WIDTH / 2D;
        double centerZ = z + .5D - FirstShipHull.LENGTH / 2D;
        // Cargo must be placed on a REAL rendered plank cell, never in a hatch,
        // outside the tapered hold or on the missing end of the curved bow.
        if (floor == 0 ? !FirstShipHull.holdSupports(localX, localZ)
                || !FirstShipHull.holdSupports(centerX, centerZ)
                : !FirstShipHull.mainDeckSupports(localX, localZ)
                || !FirstShipHull.mainDeckSupports(centerX, centerZ)) return -1;
        return tileId(floor, x, z);
    }

    private static boolean structuralCell(int tile) {
        int floor = tile / CELLS;
        int x = tile % FirstShipHull.WIDTH;
        int z = (tile % CELLS) / FirstShipHull.WIDTH;
        double cx = x + .5D - FirstShipHull.WIDTH / 2D;
        double cz = z + .5D - FirstShipHull.LENGTH / 2D;
        if (floor == 0) return FirstShipHull.onLadder(cx, cz);
        return (Math.abs(cx) < .80D && Math.abs(cz - FirstShipHull.MAST_Z) < .8D)
                || (Math.abs(cx) < 1.0D && Math.abs(cz - FirstShipHull.HELM_Z) < 1.0D);
    }

    private void syncModules() {
        char[] buffer = "0".repeat(CELLS * 2).toCharArray();
        for (var entry : kind.entrySet()) if (entry.getKey() >= 0 && entry.getKey() < buffer.length) {
            buffer[entry.getKey()] = (char)('0' + entry.getValue());
        }
        entityData.set(MODULES, new String(buffer));
    }

    public void setWaterSurface(double y) {
        if (Double.isFinite(y) && y > level().getMinBuildHeight() && y < level().getMaxBuildHeight()) {
            entityData.set(SURFACE, (float)y);
        }
    }

    private boolean afloat() {
        float surface = entityData.get(SURFACE);
        if (surface == NO_SURFACE) return false;
        BlockPos pos = BlockPos.containing(getX(), surface - DRAFT, getZ());
        return level().hasChunkAt(pos) && level().getFluidState(pos).is(FluidTags.WATER);
    }

    @Override protected int getMaxPassengers() { return 1; }

    /** The captain operates the wheel upright; rafts keep their normal seats. */
    @Override public boolean shouldRiderSit() {
        return !isNativeHull();
    }

    /** The one genuine helm seat is on the STERN, not a fake free-walking passenger. */
    @Override protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        return new Vec3(0D, FirstShipHull.MAIN_DECK + 0.08D, FirstShipHull.HELM_Z);
    }

    /**
     * Boat has a special rider offset of its own. Native ships must instead use
     * the real helm position in WORLD space, rotating around the block anchor.
     * Both logical sides MUST use the SynchedEntityData flag. The local
     * nativeHull field is deserialized only on the server and is false on
     * freshly spawned clients; using it applied vanilla Boat seating there.
     * Vanilla's passenger update invokes this every client tick as well.
     */
    @Override protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!isNativeHull()) {
            super.positionRider(passenger, move);
            return;
        }
        if (!hasPassenger(passenger)) return;
        if (isMoving()) {
            FirstShipHull.Point seat = FirstShipHull.toWorld(0D, helmLocalZ(),
                    getX(), getZ(), getYRot());
            move.accept(passenger, seat.x(), getY() + helmStandingHeight() + .08D, seat.z());
        } else {
            BlockPos anchor = blockAnchor();
            FirstShipHull.Point seat = FirstShipHull.toWorld(0D, helmLocalZ(),
                    anchor.getX() + .5D, anchor.getZ() + .5D, shipYaw());
            move.accept(passenger, seat.x(), anchor.getY() + helmStandingHeight() + .08D, seat.z());
        }
    }

    /**
     * Vanilla boats dismount next to their little waterline hitbox: on a ship
     * that is INSIDE the hold. Select a clear supported deck cell instead.
     */
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (!isNativeHull()) return super.getDismountLocationForPassenger(passenger);
        if (isVoxelShip()) {
            double y = getY() + helmStandingHeight();
            int station = helmLocalZ();
            for (int x : new int[]{1, -1, 2, -2, 0})
                for (int dz : new int[]{1, 2, -1}) {
                    int z = station + dz;
                    if (supportedVoxelFloor(x, z, helmStandingHeight(), -1) != helmStandingHeight())
                        continue;
                    FirstShipHull.Point point = FirstShipHull.toWorld(x, z,
                            getX(), getZ(), getYRot());
                    Vec3 target = new Vec3(point.x(), y + .05D, point.z());
                    if (level().noCollision(passenger,
                            passenger.getBoundingBox().move(target.subtract(passenger.position()))))
                        return target;
                }
            return new Vec3(getX(), y + 5D, getZ());
        }
        double[][] safeCells = {{1D, -2D}, {-1D, -2D}, {0D, -2D},
                {1D, -3D}, {-1D, -3D}, {0D, -3D},
                {2D, -2D}, {-2D, -2D}, {0D, 0D}, {1D, 0D}, {-1D, 0D}};
        for (double[] cell : safeCells) {
            if (!FirstShipHull.mainDeckSupports(cell[0], cell[1])) continue;
            FirstShipHull.Point point = FirstShipHull.toWorld(cell[0], cell[1],
                    getX(), getZ(), getYRot());
            Vec3 target = new Vec3(point.x(), deckY() + 0.05D, point.z());
            BlockPos floor = BlockPos.containing(point.x(), deckY() - .15D, point.z());
            if (!level().hasChunkAt(floor) || (!isMoving() && level().getBlockState(floor).isAir())) continue;
            AABB area = passenger.getBoundingBox().move(target.subtract(passenger.position()));
            if (level().noCollision(passenger, area)) return target;
        }
        // Never return the entity's waterline centre inside solid hold blocks.
        // A high, unobstructed point above the deck is safer than suffocation.
        FirstShipHull.Point stern = FirstShipHull.toWorld(1D, -2D,
                getX(), getZ(), getYRot());
        for (int above = 1; above <= 8; above++) {
            Vec3 fallback = new Vec3(stern.x(), deckY() + above + .05D, stern.z());
            if (level().noCollision(passenger,
                    passenger.getBoundingBox().move(fallback.subtract(passenger.position()))))
                return fallback;
        }
        return new Vec3(stern.x(), deckY() + 16D, stern.z());
    }

    @Override protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (isNativeHull()) {
            nextHelmAllowedTick = -100D;
            helmControl = ShipRigidMotion.Control.IDLE;
            helmControlTick = -100L;
            if (isMoving() && passenger instanceof ServerPlayer player) {
                Vec3 target = getDismountLocationForPassenger(player);
                FirstShipHull.Point local = FirstShipHull.toLocal(target.x, target.z,
                        getX(), getZ(), getYRot());
                if (isVoxelShip()
                        ? supportedVoxelFloor(local.x(), local.z(),
                                target.y - getY(), -1) >= 0
                        : FirstShipHull.mainDeckSupports(local.x(), local.z()))
                    pendingDismounts.put(player.getUUID(), new PendingDismount(
                            local.x(), local.z(), target.y - deckY(),
                            level().getGameTime() + 40L));
            }
        }
    }

    /** Resolve the one-time Shift handoff after the client has left the vehicle. */
    public boolean confirmDismount(ServerPlayer player) {
        PendingDismount landing = pendingDismounts.remove(player.getUUID());
        if (landing == null || !isMoving() || player.isPassenger()
                || level().getGameTime() > landing.expires()) return false;
        FirstShipHull.Point world = FirstShipHull.toWorld(landing.localX(), landing.localZ(),
                getX(), getZ(), getYRot());
        Vec3 target = new Vec3(world.x(), deckY() + landing.deckOffsetY(), world.z());
        AABB area = player.getBoundingBox().move(target.subtract(player.position()));
        if (!level().noCollision(player, area))
            target = getDismountLocationForPassenger(player);
        player.connection.teleport(target.x, target.y, target.z,
                player.getYRot(), player.getXRot());
        return true;
    }

    @Override protected boolean canAddPassenger(Entity passenger) {
        // Boat rejects boarding when its water-state check sees the ship's
        // waterline inside water. This hull has a dry wheel above the deck.
        return isNativeHull() ? passenger instanceof Player && !isVehicle()
                && !isRemoved() : super.canAddPassenger(passenger);
    }

    @Override public boolean canCollideWith(Entity other) {
        return !(other instanceof Player) && super.canCollideWith(other);
    }

    @Override public boolean hurt(DamageSource source, float damage) { return false; }
    @Override public ItemStack getPickResult() { return new ItemStack(RaftContent.FIRST_SHIP_ITEM.get()); }
    @Override public Item getDropItem() { return RaftContent.FIRST_SHIP_ITEM.get(); }
    @Override protected Component getTypeName() {
        ShipSuperstructurePlan plan = superstructure();
        return plan == null ? Component.literal("Деревянный корабль 7×11")
                : Component.literal("Корабль " + plan.shipClass() + " "
                        + plan.hullWidth() + "×" + plan.hullLength());
    }

    private ListTag storeModules(HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (var entry : kind.entrySet()) {
            int tile = entry.getKey(), type = entry.getValue();
            SimpleContainer slots = inventory.get(tile);
            if (slots == null) continue;
            CompoundTag data = new CompoundTag();
            data.putInt("Cell", tile);
            data.putInt("Kind", type);
            data.put("Items", slots.createTag(registries));
            SimpleContainerData heat = furnaces.get(tile);
            if (heat != null) data.putIntArray("Heat", new int[]{heat.get(0), heat.get(1), heat.get(2), heat.get(3)});
            list.add(data);
        }
        return list;
    }

    /** Validate the complete payload before changing a live entity or consuming an item. */
    public boolean loadPacked(ItemStack item) {
        CustomData custom = item.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return true;
        CompoundTag root = custom.copyTag();
        if (!root.contains("FirstShipPack", Tag.TAG_COMPOUND)) return true;
        CompoundTag pack = root.getCompound("FirstShipPack");
        int packVersion = pack.getInt("Version");
        if ((packVersion != 1 && packVersion != 2)
                || !pack.contains("Modules", Tag.TAG_LIST)) return false;
        int loadedGeometry = pack.contains("GeometryVersion", Tag.TAG_INT)
                ? pack.getInt("GeometryVersion") : 2;
        if (loadedGeometry < 2 || loadedGeometry > 9) return false;
        ShipSuperstructurePlan loadedStructure = null;
        if (loadedGeometry == 9) {
            if (!pack.contains("Superstructure", Tag.TAG_COMPOUND)) return false;
            try { loadedStructure = ShipSuperstructureData.validateBounds(
                    ShipSuperstructureData.load(pack.getCompound("Superstructure"))); }
            catch (RuntimeException invalid) { return false; }
        } else if (pack.contains("Superstructure")) return false;
        int loadedRig = packVersion == 1 ? 1 : pack.getInt("RigLevel");
        int loadedFabric = packVersion == 1 ? 1 : pack.getInt("FabricQuality");
        try { new ShipRigging(loadedRig, loadedFabric); }
        catch (IllegalArgumentException invalid) { return false; }
        ShipDesign loadedDesign;
        try {
            loadedDesign = pack.contains("Design", Tag.TAG_COMPOUND)
                    ? ShipDesign.load(pack.getCompound("Design")) : ShipDesign.defaults();
            // This entity implements a real small hull only. Never interpret a
            // saved future medium/large vessel as a small one.
            if (loadedGeometry != 9 && !loadedDesign.hullSize().equals("small")) return false;
            if (loadedGeometry == 9 && !loadedDesign.hullSize().equals(
                    loadedStructure.shipClass().name().toLowerCase(java.util.Locale.ROOT))) return false;
        } catch (IllegalArgumentException invalid) { return false; }
        ListTag list = pack.getList("Modules", Tag.TAG_COMPOUND);
        if (list.size() > CELLS * 2) return false;
        Map<Integer, SimpleContainer> loaded = new HashMap<>();
        Map<Integer, Integer> types = new HashMap<>();
        Map<Integer, SimpleContainerData> heatMap = new HashMap<>();
        Set<Integer> seen = new HashSet<>();
        try {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag value = list.getCompound(i);
                int tile = value.getInt("Cell"), type = value.getInt("Kind");
                if (tile < 0 || tile >= CELLS * 2 || type < 1 || type > 4 || !seen.add(tile)
                        || !value.contains("Items", Tag.TAG_LIST)) return false;
                int x = tile % FirstShipHull.WIDTH, z = (tile % CELLS) / FirstShipHull.WIDTH;
                double localX = x + .5D - FirstShipHull.WIDTH / 2D;
                double localZ = z + .5D - FirstShipHull.LENGTH / 2D;
                if (tileAt(localX, localZ, tile / CELLS) != tile) return false;
                ListTag stored = value.getList("Items", Tag.TAG_COMPOUND);
                if (stored.size() > (type == 4 ? 3 : 27)) return false;
                SimpleContainer slots = new SimpleContainer(type == 4 ? 3 : 27);
                slots.fromTag(stored, registryAccess());
                if (!stored.equals(slots.createTag(registryAccess()))) return false;
                loaded.put(tile, slots);
                types.put(tile, type);
                if (type == 4) {
                    SimpleContainerData heat = new SimpleContainerData(4);
                    int[] ticks = value.getIntArray("Heat");
                    for (int n = 0; n < Math.min(4, ticks.length); n++) heat.set(n, Math.max(0, ticks[n]));
                    heatMap.put(tile, heat);
                }
            }
        } catch (RuntimeException malformed) { return false; }
        if (pack.contains("BlockCargo", Tag.TAG_LIST)) {
            pendingBlockCargo = pack.getList("BlockCargo", Tag.TAG_COMPOUND).copy();
            if (loadedGeometry == 9) {
                if (!ShipHullV9.validCargo(loadedStructure, pendingBlockCargo)) return false;
            } else if (pendingBlockCargo.size() > CELLS * 2) return false;
        } else pendingBlockCargo = new ListTag();
        quarantinedModules = null;
        setRigging(loadedRig, loadedFabric);
        setGeometryVersion(loadedGeometry);
        if (loadedStructure != null) setSuperstructure(loadedStructure);
        else { superstructure = null; entityData.set(STRUCTURE_VIEW, ""); }
        design = loadedDesign;
        inventory.clear(); inventory.putAll(loaded);
        kind.clear(); kind.putAll(types);
        furnaces.clear(); furnaces.putAll(heatMap);
        syncModules();
        return true;
    }

    private ItemStack packed() {
        ItemStack stack = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        CompoundTag pack = new CompoundTag();
        pack.putInt("Version", 2);
        pack.putInt("RigLevel", rigLevel);
        pack.putInt("FabricQuality", fabricQuality);
        pack.putInt("GeometryVersion", geometryVersion);
        if (geometryVersion == 9 && superstructure != null)
            pack.put("Superstructure", ShipSuperstructureData.save(superstructure));
        pack.put("Design", design.save());
        pack.putUUID("VesselId", getUUID());
        pack.put("Modules", storeModules(registryAccess()));
        if (isMoving() && movingBody != null) pack.put("BlockCargo", movingBody.snapshot().cargo());
        else if (nativeHull && level() instanceof net.minecraft.server.level.ServerLevel server) {
            ListTag cargo = geometryVersion == 9 && superstructure != null
                    ? ShipHullV9.pack(server, blockAnchor, quarterTurns, superstructure)
                    : FirstShipBlockHull.pack(server, blockAnchor, quarterTurns,
                            rigLevel, fabricQuality, geometryVersion);
            if (cargo == null) return ItemStack.EMPTY;
            pack.put("BlockCargo", cargo);
        }
        CompoundTag root = new CompoundTag();
        root.put("FirstShipPack", pack);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return stack;
    }

    /** No packing while anybody stands in the hold or on the deck. */
    private boolean hasWalkers() {
        AABB area = getBoundingBox().inflate(isVoxelShip() ? 35D : 8D,
                isVoxelShip() ? 24D : 5D, isVoxelShip() ? 35D : 8D);
        for (Player player : level().getEntitiesOfClass(Player.class, area)) {
            if (player.isPassenger()) continue;
            FirstShipHull.Point local = FirstShipHull.toLocal(
                    player.getX(), player.getZ(), getX(), getZ(), getYRot());
            if (isVoxelShip()) {
                if (supportedVoxelFloor(local.x(), local.z(), player.getY() - getY(), -1) >= 0)
                    return true;
            } else if (FirstShipHull.withinHull(local.x(), local.z(), .0D)
                    && player.getY() >= holdY() - 0.55D
                    && player.getY() <= deckY() + 2.2D) return true;
        }
        return false;
    }

    public InteractionResult packWithKey(Player player) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer) || quarantinedModules != null || packing || isRemoved() || isVehicle()
                || hasWalkers() || getDeltaMovement().horizontalDistanceSqr() > .0005D
                || (movingBody != null && (Math.abs(movingBody.motion().speed()) > .008D
                    || Math.abs(movingBody.motion().angularVelocity()) > .15D))) {
            player.displayClientMessage(Component.literal("Судно должно стоять без экипажа и пассажиров"), true);
            return InteractionResult.FAIL;
        }
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    getBoundingBox().inflate(10D, 8D, 10D))) {
                if (nearby.containerMenu != nearby.inventoryMenu) {
                    player.displayClientMessage(Component.literal(
                            "Перед упаковкой все грузовые окна должны быть закрыты"), true);
                    return InteractionResult.FAIL;
                }
            }
        }
        ItemStack stack = packed();
        if (stack.isEmpty()) return InteractionResult.FAIL;
        if (nativeHull && !isMoving()) {
            if (!(level() instanceof net.minecraft.server.level.ServerLevel server)
                    || !(geometryVersion == 9 && superstructure != null
                            ? ShipHullV9.remove(server, blockAnchor, quarterTurns, superstructure)
                            : FirstShipBlockHull.remove(server, blockAnchor, quarterTurns,
                                    rigLevel, fabricQuality, geometryVersion))) {
                player.displayClientMessage(Component.literal("Корпус изменён или груз повреждён: упаковка заблокирована"), true);
                return InteractionResult.FAIL;
            }
        }
        packing = true;
        discard(); // Remove real blocks and ship before granting an inventory item.
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        player.displayClientMessage(Component.literal("Корабль упакован вместе с грузом"), true);
        return InteractionResult.CONSUME;
    }


    /**
     * The visible helm is ~3.6 blocks aft of the entity center; vanilla Boat
     * interaction range targets only its ~3-block core. A validated action
     * allows the actual painted helm to be used from its stern position.
     */
    public boolean tryHelm(ServerPlayer player) {
        if (isVehicle() || player.isPassenger() || player.isSpectator()) return false;
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        if (Math.abs(local.x()) > 1.65D
                || Math.abs(local.z() - helmLocalZ()) > 1.7D
                || Math.abs(player.getY() - (getY() + helmStandingHeight())) > .9D) return false;
        FirstShipHull.Point wheel = FirstShipHull.toWorld(0D, helmLocalZ(),
                getX(), getZ(), getYRot());
        Vec3 aim = new Vec3(wheel.x(), getY() + helmStandingHeight() + 1.78D, wheel.z()).subtract(player.getEyePosition());
        if (aim.lengthSqr() < .09D || aim.lengthSqr() > 25D
                || aim.normalize().dot(player.getLookAngle()) < .30D) return false;
        boolean mounted = player.startRiding(this);
        if (mounted) positionRider(player);
        return mounted;
    }

    /**
     * Install or replace sail material with a genuine crafted kit, extend the
     * mast by one tier with a rigging kit, or remove one tier with Shift+ПКМ.
     * BlockHull verifies the whole replacement footprint before mutation.
     * Inventory only changes after that transaction succeeds.
     */
    public boolean configureRigging(ServerPlayer player, int action) {
        if (!nativeHull || isVoxelShip() || isVehicle() || player.isPassenger() || player.isSpectator()
                || !(level() instanceof net.minecraft.server.level.ServerLevel server)
                || player.containerMenu != player.inventoryMenu) return false;
        FirstShipHull.Point local = FirstShipHull.toLocal(player.getX(), player.getZ(),
                getX(), getZ(), getYRot());
        if (Math.abs(local.x()) > 2.5D
                || Math.abs(local.z() - FirstShipHull.MAST_Z) > 2.5D
                || Math.abs(player.getY() - deckY()) > 1.0D) return false;
        FirstShipHull.Point mast = FirstShipHull.toWorld(0D, FirstShipHull.MAST_Z,
                getX(), getZ(), getYRot());
        Vec3 aim = new Vec3(mast.x(), deckY() + 2.5D, mast.z()).subtract(player.getEyePosition());
        if (aim.lengthSqr() < .25D || aim.lengthSqr() > 36D
                || aim.normalize().dot(player.getLookAngle()) < .62D) return false;

        int nextLevel = rigLevel, nextFabric = fabricQuality, cost = 0;
        ItemStack hand = player.getMainHandItem();
        Item consumed = null, refunded = null;
        Item additionalSailCost = null, additionalSailRefund = null;
        int refundCount = 0;
        if (action == FirstShipNetwork.RIG_REMOVE) {
            if (rigLevel == 0 || !hand.isEmpty() || !player.isShiftKeyDown()) return false;
            if (rigLevel == 1) {
                nextLevel = 0;
                nextFabric = 0;
                refunded = sailModule(fabricQuality);
            } else {
                nextLevel--;
                refunded = RaftContent.SAIL_RIGGING_KIT.get();
                additionalSailRefund = sailModule(fabricQuality);
            }
            refundCount = 1;
        } else if (action == FirstShipNetwork.RIG_CONFIGURE) {
            if (hand.is(RaftContent.SAIL_RIGGING_KIT.get())) {
                if (rigLevel == 0 || rigLevel >= 4) return false;
                nextLevel++;
                consumed = RaftContent.SAIL_RIGGING_KIT.get();
                additionalSailCost = sailModule(fabricQuality);
                cost = 1;
            } else {
                int requested = hand.is(RaftContent.SAIL_MODULE.get()) ? 1
                        : hand.is(RaftContent.SAIL_LINEN_MODULE.get()) ? 2
                        : hand.is(RaftContent.SAIL_REINFORCED_MODULE.get()) ? 3 : 0;
                if (requested == 0 || requested == fabricQuality) return false;
                nextLevel = Math.max(1, rigLevel);
                nextFabric = requested;
                consumed = sailModule(requested);
                cost = nextLevel; // Larger canvas consumes proportional fabric.
                if (rigLevel > 0) {
                    refunded = sailModule(fabricQuality);
                    refundCount = rigLevel;
                }
            }
        } else return false;
        if (consumed != null && !player.getAbilities().instabuild
                && player.getInventory().countItem(consumed) < cost) {
            player.displayClientMessage(Component.literal("Нужно больше комплектов ткани для парусов"), true);
            return false;
        }
        if (additionalSailCost != null && !player.getAbilities().instabuild
                && player.getInventory().countItem(additionalSailCost) < 1) {
            player.displayClientMessage(Component.literal(
                    "Для дополнительного яруса нужен также комплект парусной ткани"), true);
            return false;
        }
        // Changing real blocks while another player accesses a chest is unsafe.
        for (ServerPlayer other : server.getEntitiesOfClass(ServerPlayer.class,
                new AABB(blockAnchor).inflate(11D, 24D, 11D))) {
            if (other.containerMenu != other.inventoryMenu) return false;
        }
        if (!FirstShipBlockHull.move(server, blockAnchor, quarterTurns,
                blockAnchor, quarterTurns, rigLevel, fabricQuality, nextLevel, nextFabric, geometryVersion)) {
            player.displayClientMessage(Component.literal("Оснащение не установлено: мачта повреждена или нет свободного места"), true);
            return false;
        }
        setRigging(nextLevel, nextFabric);
        if (consumed != null && !player.getAbilities().instabuild) {
            int remaining = cost;
            for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.is(consumed)) continue;
                int taken = Math.min(stack.getCount(), remaining);
                stack.shrink(taken);
                remaining -= taken;
            }
        }
        if (additionalSailCost != null && !player.getAbilities().instabuild) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(additionalSailCost)) { stack.shrink(1); break; }
            }
        }
        if (refunded != null && !player.getAbilities().instabuild) {
            for (int i = 0; i < refundCount; i++)
                if (!player.getInventory().add(new ItemStack(refunded)))
                    player.drop(new ItemStack(refunded), false);
        }
        if (additionalSailRefund != null && !player.getAbilities().instabuild
                && !player.getInventory().add(new ItemStack(additionalSailRefund)))
            player.drop(new ItemStack(additionalSailRefund), false);
        player.displayClientMessage(Component.literal("Паруса: уровень " + nextLevel
                + "/4, ткань " + nextFabric + "/3, интервал "
                + String.format(java.util.Locale.ROOT, "%.2f",
                        new ShipRigging(nextLevel, nextFabric).exactCooldownTicks())
                + " тиков"), true);
        return true;
    }

    private static Item sailModule(int fabric) {
        return switch (fabric) {
            case 1 -> RaftContent.SAIL_MODULE.get();
            case 2 -> RaftContent.SAIL_LINEN_MODULE.get();
            case 3 -> RaftContent.SAIL_REINFORCED_MODULE.get();
            default -> throw new IllegalArgumentException("No sail fabric");
        };
    }

    /**
     * A single explicit boarding step from water to the open deck.
     * Once on deck the player is NOT mounted or moved between fake seats.
     */
    public boolean tryBoard(ServerPlayer player) {
        if (isVoxelShip()) return false; // V9 uses its full-sized physical deck access.
        if (player.isPassenger() || player.isSpectator()
                || getDeltaMovement().horizontalDistanceSqr() > .0025D
                || player.getY() >= deckY() - 1.1D) return false;
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        double z = FirstShipHull.clamp(local.z(), -4.7D, 4.7D);
        double half = FirstShipHull.halfBeam(z);
        if (Math.abs(local.z()) > 6.25D || Math.abs(local.x()) > half + 1.60D
                || Math.abs(local.x()) < half - 1.1D && Math.abs(local.z()) < 4.9D) return false;
        double landingX = FirstShipHull.clamp(local.x(), -half + .82D, half - .82D);
        if (!FirstShipHull.mainDeckSupports(landingX, z)) return false;
        FirstShipHull.Point target = FirstShipHull.toWorld(landingX, z,
                getX(), getZ(), getYRot());
        Vec3 delta = new Vec3(target.x() - player.getX(), deckY() - player.getY(),
                target.z() - player.getZ());
        if (!player.level().noCollision(player, player.getBoundingBox().move(delta))) return false;
        player.teleportTo(target.x(), deckY() + .01D, target.z());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0F;
        return true;
    }

    @Override public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(RaftContent.SHIPYARD_KEY.get())) return packWithKey(player);
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        // Helm can only be activated at the stern, on the MAIN deck.
        if (!player.isSecondaryUseActive() && Math.abs(local.x()) < 1.35D
                && Math.abs(local.z() - helmLocalZ()) < 1.65D
                && Math.abs(player.getY() - (getY() + helmStandingHeight())) < 1.3D) {
            if (!level().isClientSide && player.startRiding(this)) positionRider(player);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!player.isSecondaryUseActive()) return InteractionResult.PASS;
        return interactFromDeck(player, hand);
    }

    /** Also called by air-right-click while standing on any part of this large ship. */
    public InteractionResult interactFromDeck(Player player, InteractionHand hand) {
        if (isVoxelShip()) {
            if (!isMoving() || !(player instanceof ServerPlayer serverPlayer))
                return InteractionResult.PASS;
            for (int i = 0; movingBody != null && i < movingBody.cargoCount(); i++) {
                CompoundTag cargo = movingBody.cargoEntry(i);
                FirstShipHull.Point at = FirstShipHull.toWorld(
                        cargo.getInt("X"), cargo.getInt("Z"), getX(), getZ(), getYRot());
                if (player.distanceToSqr(at.x(), getY() + cargo.getInt("Y"), at.z()) <= 25D
                        && openMovingCargo(serverPlayer, i))
                    return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        FirstShipHull.Point local = FirstShipHull.toLocal(
                player.getX(), player.getZ(), getX(), getZ(), getYRot());
        int floor = player.getY() > getY() + 1.8D ? 1 : 0;
        int tile = tileAt(local.x(), local.z(), floor);
        if (tile < 0 || Math.abs(player.getY() - (floor == 1 ? deckY() : holdY())) > .72D)
            return InteractionResult.FAIL;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (held.is(Items.CHEST) || held.is(Items.BARREL)
                || held.is(Items.CRAFTING_TABLE) || held.is(Items.FURNACE)) {
            if (kind.containsKey(tile) || structuralCell(tile)) return InteractionResult.FAIL;
            int type = held.is(Items.CHEST) ? 1 : held.is(Items.BARREL) ? 2
                    : held.is(Items.CRAFTING_TABLE) ? 3 : 4;
            kind.put(tile, type);
            inventory.put(tile, new SimpleContainer(type == 4 ? 3 : 27));
            if (type == 4) furnaces.put(tile, new SimpleContainerData(4));
            syncModules();
            if (!player.getAbilities().instabuild) held.shrink(1);
            return InteractionResult.CONSUME;
        }
        SimpleContainer slots = inventory.get(tile);
        if (slots == null || !(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;
        int type = kind.get(tile);
        Component label = Component.literal(type == 1 ? "Корабельный сундук" :
                type == 2 ? "Корабельная бочка" : type == 3 ? "Верстак" : "Корабельная печь");
        if (type == 3) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) ->
                    new net.minecraft.world.inventory.CraftingMenu(id, inv,
                            net.minecraft.world.inventory.ContainerLevelAccess.create(level(), blockPosition())) {
                        @Override public boolean stillValid(Player viewer) {
                            return !FirstShipEntity.this.isRemoved() && viewer.distanceToSqr(FirstShipEntity.this) < 100D;
                        }
                    }, label));
        } else if (type == 4) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) ->
                    new FurnaceMenu(id, inv, slots, furnaces.get(tile)), label));
        } else {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) ->
                    ChestMenu.threeRows(id, inv, slots), label));
        }
        return InteractionResult.CONSUME;
    }

    /** Open a detached palette container; writes go back into the saved body. */
    public boolean openMovingCargo(ServerPlayer player, int index) {
        if (!isMoving() || movingBody == null || player.isPassenger()
                || player.containerMenu != player.inventoryMenu) return false;
        CompoundTag entry = movingBody.cargoEntry(index);
        if (entry == null) return false;
        int type = entry.getInt("Kind");
        if (type < 1 || type > 4) return false;
        FirstShipHull.Point aboard = FirstShipHull.toLocal(player.getX(), player.getZ(),
                getX(), getZ(), getYRot());
        if (isVoxelShip()) {
            if (supportedVoxelFloor(aboard.x(), aboard.z(),
                    player.getY() - getY(), -1) < 0) return false;
        } else if (!FirstShipHull.withinHull(aboard.x(), aboard.z(), .1D)
                || player.getY() < holdY() - .4D || player.getY() > deckY() + 2.2D)
            return false;
        int x = entry.getInt("X"), y = entry.getInt("Y"), z = entry.getInt("Z");
        FirstShipHull.Point point = FirstShipHull.toWorld(x, z,
                getX(), getZ(), getYRot());
        if (player.distanceToSqr(point.x(), getY() + y + .5D, point.z()) > 36D)
            return false;
        CompoundTag blockEntity = entry.getCompound("BlockEntity").copy();
        SimpleContainer slots = movingCargoSlots(index, type, blockEntity);
        Component label = Component.literal(type == 1 ? "Корабельный сундук"
                : type == 2 ? "Корабельная бочка" : type == 3 ? "Верстак" : "Корабельная печь");
        if (type == 3) {
            player.openMenu(new SimpleMenuProvider((id, inv, viewer) ->
                    new net.minecraft.world.inventory.CraftingMenu(id, inv,
                            net.minecraft.world.inventory.ContainerLevelAccess.create(level(), blockPosition())) {
                        @Override public boolean stillValid(Player p) {
                            return !FirstShipEntity.this.isRemoved() && isMoving()
                                    && p.distanceToSqr(FirstShipEntity.this) < 196D;
                        }
                    }, label));
        } else if (type == 4) {
            player.openMenu(new SimpleMenuProvider((id, inv, viewer) ->
                    new FurnaceMenu(id, inv, slots, movingCargoHeat(index, blockEntity)), label));
        } else {
            player.openMenu(new SimpleMenuProvider((id, inv, viewer) ->
                    ChestMenu.threeRows(id, inv, slots), label));
        }
        return true;
    }

    private SimpleContainer movingCargoSlots(int index, int type, CompoundTag initial) {
        SimpleContainer existing = movingCargoSlots.get(index);
        if (existing != null) return existing;
        SimpleContainer slots = new SimpleContainer(type == 4 ? 3 : 27) {
            @Override public boolean stillValid(Player viewer) {
                return movingBody != null && !FirstShipEntity.this.isRemoved()
                        && viewer.distanceToSqr(FirstShipEntity.this) < 196D;
            }
            @Override public void setChanged() {
                super.setChanged();
                if (loadingMovingCargo.contains(index) || movingBody == null) return;
                CompoundTag entry = movingBody.cargoEntry(index);
                if (entry == null) return;
                CompoundTag be = entry.getCompound("BlockEntity").copy();
                writeMovingItems(be, this);
                movingBody.updateCargoEntry(index, be);
            }
        };
        loadingMovingCargo.add(index);
        net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(
                slots.getContainerSize(), ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(initial, items, registryAccess());
        for (int i = 0; i < items.size(); i++) slots.setItem(i, items.get(i));
        loadingMovingCargo.remove(index);
        movingCargoSlots.put(index, slots);
        return slots;
    }

    private void writeMovingItems(CompoundTag be, SimpleContainer slots) {
        net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(
                slots.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) items.set(i, slots.getItem(i).copy());
        net.minecraft.world.ContainerHelper.saveAllItems(be, items, registryAccess());
    }

    private SimpleContainerData movingCargoHeat(int index, CompoundTag initial) {
        return movingCargoHeat.computeIfAbsent(index, ignored -> {
            SimpleContainerData data = new SimpleContainerData(4);
            int[] values = initial.getIntArray("FirstShipHeat");
            if (values.length == 0) values = new int[]{
                    initial.getShort("BurnTime"), initial.getShort("BurnTime"),
                    initial.getShort("CookTime"), initial.getShort("CookTimeTotal")};
            for (int i = 0; i < Math.min(4, values.length); i++) data.set(i, values[i]);
            return data;
        });
    }


    /** Same conservative, three-slot smelting semantics as the existing rafts. */
    private static int fuelTicks(ItemStack fuel) {
        if (fuel.is(Items.COAL) || fuel.is(Items.CHARCOAL)) return 1600;
        if (fuel.is(Items.BLAZE_ROD)) return 2400;
        if (fuel.is(net.minecraft.tags.ItemTags.LOGS)
                || fuel.is(net.minecraft.tags.ItemTags.PLANKS)) return 300;
        if (fuel.is(Items.STICK)) return 100;
        return 0; // no untested container fuel, no disappearing buckets
    }

    private void tickShipFurnaces() {
        for (var entry : furnaces.entrySet()) {
            SimpleContainer slots = inventory.get(entry.getKey());
            if (slots == null || slots.getContainerSize() != 3) continue;
            tickFurnace(slots, entry.getValue());
        }
        if (movingBody == null) return;
        for (int index = 0; index < movingBody.cargoCount(); index++) {
            CompoundTag entry = movingBody.cargoEntry(index);
            if (entry.getInt("Kind") != 4) continue;
            CompoundTag be = entry.getCompound("BlockEntity").copy();
            SimpleContainer slots = movingCargoSlots(index, 4, be);
            SimpleContainerData heat = movingCargoHeat(index, be);
            if (slots.getItem(0).isEmpty() && heat.get(0) == 0 && heat.get(2) == 0)
                continue;
            tickFurnace(slots, heat);
            writeMovingItems(be, slots);
            be.putIntArray("FirstShipHeat", new int[]{heat.get(0), heat.get(1),
                    heat.get(2), heat.get(3)});
            be.putShort("BurnTime", (short)heat.get(0));
            be.putShort("CookTime", (short)heat.get(2));
            be.putShort("CookTimeTotal", (short)heat.get(3));
            movingBody.updateCargoEntry(index, be);
        }
    }

    private void tickFurnace(SimpleContainer slots, SimpleContainerData heat) {
            int lit = Math.max(0, heat.get(0) - 1);
            heat.set(0, lit);
            ItemStack source = slots.getItem(0), fuel = slots.getItem(1), output = slots.getItem(2);
            if (source.isEmpty()) { heat.set(2, 0); return; }
            var match = level().getRecipeManager().getRecipeFor(
                    net.minecraft.world.item.crafting.RecipeType.SMELTING,
                    new net.minecraft.world.item.crafting.SingleRecipeInput(source), level());
            if (match.isEmpty()) { heat.set(2, 0); return; }
            net.minecraft.world.item.crafting.SmeltingRecipe recipe = match.get().value();
            ItemStack result = recipe.assemble(
                    new net.minecraft.world.item.crafting.SingleRecipeInput(source), registryAccess());
            if (result.isEmpty() || (!output.isEmpty()
                    && (!ItemStack.isSameItemSameComponents(output, result)
                    || output.getCount() + result.getCount() > output.getMaxStackSize()))) {
                heat.set(2, 0);
                return;
            }
            if (lit == 0 && !fuel.isEmpty()) {
                int burn = fuelTicks(fuel);
                if (burn > 0) {
                    fuel.shrink(1);
                    heat.set(0, burn);
                    heat.set(1, burn);
                    lit = burn;
                }
            }
            if (lit > 0) {
                int progress = heat.get(2) + 1;
                int required = Math.max(1, recipe.getCookingTime());
                heat.set(3, required);
                if (progress >= required) {
                    source.shrink(1);
                    if (output.isEmpty()) slots.setItem(2, result.copy());
                    else output.grow(result.getCount());
                    heat.set(2, 0);
                    slots.setChanged();
                } else heat.set(2, progress);
            } else heat.set(2, 0);
    }

    /**
     * Native ship is a STATIC world-block structure between helm commands.
     * Never invoke Boat.tick() movement/float code: it was responsible for
     * dragging people off the fake floors. Vanilla blocks provide real collision.
     */
    @Override public void tick() {
        // Worlds created with the rejected fake-entity ship contain no world
        // blocks. Try a one-time safe upgrade if the former module grid is
        // empty and the 7x11 water site is still unobstructed. Never overwrite
        // land, delete NBT cargo, or silently claim an unsafe conversion.
        if (!isNativeHull() && !level().isClientSide && !legacyConversionTried) {
            legacyConversionTried = true;
            if (!isVehicle() && kind.isEmpty() && quarantinedModules == null
                    && level() instanceof net.minecraft.server.level.ServerLevel server) {
                BlockPos proposed = BlockPos.containing(getX(), getY(), getZ());
                int turn = FirstShipBlockHull.rotationFromYaw(getYRot());
                if (FirstShipBlockHull.place(server, proposed, turn, new ListTag(), 1, 1)) {
                    initializeNativeHull(proposed, turn);
                    setRigging(1, 1);
                    setCustomName(null);
                    setCustomNameVisible(false);
                }
            }
            if (!isNativeHull()) {
                setCustomName(Component.literal("СТАРЫЙ КОРАБЛЬ — УПАКУЙТЕ КЛЮЧОМ И ПОСТАВЬТЕ ЗАНОВО"));
                setCustomNameVisible(true);
            }
        }
        // Existing 52c8d72 worlds have an older physical layout. Upgrade
        // in place exactly once if its saved structure and cargo are intact.
        if (nativeHull && geometryVersion == 1
                && level() instanceof net.minecraft.server.level.ServerLevel server && !isVehicle()) {
            boolean busy = false;
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(blockAnchor).inflate(10D, 9D, 10D)))
                if (nearby.containerMenu != nearby.inventoryMenu) { busy = true; break; }
            if (!busy && FirstShipBlockHull.migrateLegacy(server, blockAnchor, quarterTurns))
                setGeometryVersion(2);
        }
        // V5 uses the existing registered helm item's 3D mesh instead
        // of constructing a wheel from vanilla fences and slabs.
        if (nativeHull && geometryVersion == 4
                && level() instanceof net.minecraft.server.level.ServerLevel server
                && !isVehicle() && !hasWalkers()) {
            boolean busy = false;
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(blockAnchor).inflate(11D, 24D, 11D)))
                if (nearby.containerMenu != nearby.inventoryMenu) { busy = true; break; }
            if (!busy && FirstShipBlockHull.migrateGeometryV4(server, blockAnchor,
                    quarterTurns, rigLevel, fabricQuality)) {
                setGeometryVersion(5);
                setCustomName(null);
                setCustomNameVisible(false);
            }
        }
        // V4 replaces the former fence cluster with an actual wheel silhouette.
        // Do not reinterpret V3 block worlds as V4 until the old source has
        // been verified and the ship is empty: full cargo preservation is required.
        if (nativeHull && geometryVersion == 3
                && level() instanceof net.minecraft.server.level.ServerLevel server
                && !isVehicle() && !hasWalkers()) {
            boolean busy = false;
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(blockAnchor).inflate(11D, 24D, 11D)))
                if (nearby.containerMenu != nearby.inventoryMenu) { busy = true; break; }
            if (!busy && FirstShipBlockHull.migrateGeometryV3(server, blockAnchor,
                    quarterTurns, rigLevel, fabricQuality)) {
                setGeometryVersion(4);
                setCustomName(null);
                setCustomNameVisible(false);
            }
        }
        if (nativeHull && geometryVersion == 2
                && level() instanceof net.minecraft.server.level.ServerLevel server
                && !isVehicle() && !hasWalkers()) {
            boolean busy = false;
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(blockAnchor).inflate(11D, 24D, 11D)))
                if (nearby.containerMenu != nearby.inventoryMenu) { busy = true; break; }
            if (!busy) {
                if (FirstShipBlockHull.migrateGeometryV2(server, blockAnchor,
                        quarterTurns, rigLevel, fabricQuality)) {
                    setGeometryVersion(3);
                    setCustomName(null);
                    setCustomNameVisible(false);
                } else {
                    setCustomName(Component.literal(
                            "КОРПУС V2: переход отложен — мешают блоки или повреждена конструкция"));
                    setCustomNameVisible(true);
                }
            }
        }
        if (legacySailUpgradePending && nativeHull && geometryVersion == 5
                && rigLevel <= 1 && !isMoving() && !isVehicle() && !hasWalkers()
                && level() instanceof net.minecraft.server.level.ServerLevel server) {
            boolean busy = false;
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(blockAnchor).inflate(11D, 24D, 11D)))
                if (nearby.containerMenu != nearby.inventoryMenu) { busy = true; break; }
            if (!busy) {
                legacySailUpgradePending = false;
                if (FirstShipBlockHull.upgradeStarterSailV6(server, blockAnchor,
                        quarterTurns, rigLevel, fabricQuality)) {
                    setRigging(1, 1);
                    setGeometryVersion(6);
                }
            }
        }
        if (!level().isClientSide && !visualSailUpgradeTried && nativeHull
                && geometryVersion == 6 && rigLevel > 0 && !isMoving()
                && !isVehicle() && !hasWalkers()
                && level() instanceof net.minecraft.server.level.ServerLevel server) {
            boolean busy = false;
            for (ServerPlayer nearby : server.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(blockAnchor).inflate(11D, 24D, 11D)))
                if (nearby.containerMenu != nearby.inventoryMenu) { busy = true; break; }
            if (!busy) {
                visualSailUpgradeTried = true;
                if (FirstShipBlockHull.migrateVisualSailV7(server, blockAnchor,
                        quarterTurns, rigLevel, fabricQuality)) setGeometryVersion(7);
            }
        }
        if (isMoving()) {
            // The body is a rigid palette entity; do not spawn any chunk blocks
            // during travel. Only the authoritative server advances physics.
            baseTick();
            setDeltaMovement(Vec3.ZERO);
            if (level().isClientSide && clientMotionSteps > 0) {
                clientPreviousX = getX();
                clientPreviousZ = getZ();
                clientPreviousYaw = getYRot();
                double dx = (clientMotionX - getX()) / clientMotionSteps;
                double dz = (clientMotionZ - getZ()) / clientMotionSteps;
                double distance = Math.hypot(dx, dz);
                // Bound a delayed packet's visual catch-up by the server's
                // maximum physical speed. The rider follows this same pose.
                if (distance > ShipRigidMotion.MAX_FORWARD + .01D) {
                    double factor = (ShipRigidMotion.MAX_FORWARD + .01D) / distance;
                    dx *= factor;
                    dz *= factor;
                }
                double yawStep = ShipRigidMotion.wrapDegrees(clientMotionYaw - getYRot())
                        / clientMotionSteps;
                yawStep = Math.max(-ShipRigidMotion.MAX_ANGULAR - .1D,
                        Math.min(ShipRigidMotion.MAX_ANGULAR + .1D, yawStep));
                setPos(getX() + dx, clientMotionY, getZ() + dz);
                setYRot(getYRot() + (float)yawStep);
                setYHeadRot(getYRot());
                clientMotionSteps--;
                if (Math.abs(getX() - clientPreviousX) > 1E-6D
                        || Math.abs(getZ() - clientPreviousZ) > 1E-6D
                        || Math.abs(ShipRigidMotion.wrapDegrees(getYRot() - clientPreviousYaw)) > 1E-5D)
                    clientPoseFrame++;
            }
            if (!level().isClientSide && movingBody != null
                    && level() instanceof net.minecraft.server.level.ServerLevel server) {
                if (level().getGameTime() % 40L == 0L)
                    pendingDismounts.values().removeIf(
                            landing -> level().getGameTime() > landing.expires());
                ShipRigidMotion.Control control = level().getGameTime() - helmControlTick <= 2L
                        ? helmControl : ShipRigidMotion.Control.IDLE;
                double oldX = getX(), oldZ = getZ();
                float oldYaw = getYRot();
                ShipRigidMotion.Control powered = isVoxelShip()
                ? new ShipRigidMotion.Control(control.throttle()
                        * (superstructure.sailTiers().isEmpty() ? .16D
                                : superstructure.propulsionFactor()), control.rudder())
                : ShipSailMotion.apply(control, rigLevel, fabricQuality);
        ShipRigidMotion.Frame frame = movingBody.tick(server, powered);
                ShipRigidMotion.Pose next = movingBody.pose();
                if (frame.blocked() && !hasOpenShipMenus(server) && movingBody.park(server)) {
                    BlockPos restored = BlockPos.containing(next.x(), movingBody.waterlineY(), next.z());
                    int restoredTurns = Math.floorMod((int)Math.round(next.yaw() / 90D), 4);
                    initializeNativeHull(restored, restoredTurns);
                    movingBody = null;
                    clearMovingCargoCache();
                    entityData.set(MOVING, false);
                    syncMovingCargo();
                    helmControl = ShipRigidMotion.Control.IDLE;
                    if (getFirstPassenger() instanceof ServerPlayer driver)
                        driver.displayClientMessage(Component.literal(
                                "Корабль остановлен и восстановлен: впереди препятствие или мелководье"), true);
                } else {
                    setPos(next.x(), movingBody.waterlineY(), next.z());
                    setYRot((float)next.yaw());
                    setYHeadRot((float)next.yaw());
                    if (Math.abs(getX() - oldX) > 1E-6D
                            || Math.abs(getZ() - oldZ) > 1E-6D
                            || Math.abs(ShipRigidMotion.wrapDegrees(getYRot() - oldYaw)) > 1E-5D) {
                        FirstShipMovingDeckEvents.carryServerCrew(this, oldX, oldZ, oldYaw);
                    }
                    FirstShipNetwork.sendMotionPose(this);
                    if (frame.blocked() && getFirstPassenger() instanceof ServerPlayer driver
                            && level().getGameTime() % 40L == 0L)
                        driver.displayClientMessage(Component.literal(
                                "Движение заблокировано берегом, мелководьем или сооружением"), true);
                }
            }
            if (getFirstPassenger() != null) positionRider(getFirstPassenger());
        } else if (isNativeHull()) {
            // Boat.tick is incompatible with actual in-world hull blocks, but
            // baseTick alone does not deliver normal Boat client movement.
            baseTick();
            setDeltaMovement(Vec3.ZERO);
            BlockPos anchor = blockAnchor();
            double x = anchor.getX() + .5D, y = anchor.getY(), z = anchor.getZ() + .5D;
            // Keep the proven server-side Boat entity tracking refresh each
            // tick. The CLIENT additionally snaps from the synced anchor.
            if (!level().isClientSide || position().distanceToSqr(x, y, z) > 1.0E-6D)
                setPos(x, y, z);
            if (level().isClientSide) {
                // Entity tracking may arrive AFTER chunk block updates. The
                // synced anchor, not the delayed boat interpolation, owns pose.
                setYRot(shipYaw());
                setYHeadRot(shipYaw());
                if (getFirstPassenger() != null) positionRider(getFirstPassenger());
            }
        } else {
            // Legacy experimental entities remain identifiable for safe packing;
            // they are deliberately not silently converted mid-save.
            super.tick();
        }
        if (!level().isClientSide) tickShipFurnaces();
    }

    /** Encodes only installed cargo TYPES to clients; contents stay server-side. */
    private void syncMovingCargo() {
        if (movingBody == null) { entityData.set(MOTION_CARGO_VIEW, ""); return; }
        StringBuilder view = new StringBuilder();
        for (int n = 0; n < movingBody.cargoCount(); n++) {
            CompoundTag c = movingBody.cargoEntry(n);
            if (view.length() > 0) view.append(';');
            view.append(c.getInt("X")).append(',').append(c.getInt("Y")).append(',')
                    .append(c.getInt("Z")).append(',').append(c.getInt("Kind"));
        }
        entityData.set(MOTION_CARGO_VIEW, view.toString());
    }

    private boolean hasOpenShipMenus(net.minecraft.server.level.ServerLevel server) {
        for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class,
                getBoundingBox().inflate(10D, 15D, 10D)))
            if (player.containerMenu != player.inventoryMenu) return true;
        return false;
    }

    private void clearMovingCargoCache() {
        movingCargoSlots.clear();
        movingCargoHeat.clear();
        loadingMovingCargo.clear();
    }

    /** Stage new bodies only after validating the real hull and all cargo. */
    private boolean beginContinuousMotion(net.minecraft.server.level.ServerLevel server) {
        if (!smoothEnabled || geometryVersion < 5 || quarantinedModules != null
                || movingBody != null) return false;
        for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class,
                new AABB(blockAnchor).inflate(isVoxelShip() ? 38D : 10D, 28D,
                        isVoxelShip() ? 38D : 10D)))
            if (player.containerMenu != player.inventoryMenu) return false;
        ShipMovingContraption prepared = geometryVersion == 9 && superstructure != null
                ? ShipMovingContraption.prepare(server, blockAnchor, quarterTurns, superstructure)
                : ShipMovingContraption.prepare(server, blockAnchor,
                        quarterTurns, rigLevel, fabricQuality, geometryVersion);
        if (prepared == null || !prepared.detach(server, blockAnchor)) return false;
        // Install the durable moving state before any attempt at restoration:
        // even an unexpected blocked restore must NEVER orphan the cargo.
        movingBody = prepared;
        clearMovingCargoCache();
        entityData.set(MOVING, true);
        syncMovingCargo();
        helmControlTick = -100L;
        if (!ShipMovingClearance.clearPose(server, prepared.pose(),
                prepared.waterlineY(), prepared.palette())) {
            if (prepared.park(server)) {
                movingBody = null;
                clearMovingCargoCache();
                entityData.set(MOVING, false);
                syncMovingCargo();
                return false;
            }
            // A failed restore is recoverable: retain its saved palette/NBT
            // rather than abandoning the detached structure.
            helmControl = ShipRigidMotion.Control.IDLE;
        }
        return true;
    }

    private boolean continuousHelm(ServerPlayer driver, int action) {
        if (movingBody == null) return false;
        int throttle = 0, rudder = 0;
        switch (action) {
            case FirstShipNetwork.FORWARD -> throttle = 1;
            case FirstShipNetwork.BACKWARD -> throttle = -1;
            case FirstShipNetwork.LEFT -> rudder = -1;
            case FirstShipNetwork.RIGHT -> rudder = 1;
            case FirstShipNetwork.FORWARD_LEFT -> { throttle = 1; rudder = -1; }
            case FirstShipNetwork.FORWARD_RIGHT -> { throttle = 1; rudder = 1; }
            case FirstShipNetwork.BACKWARD_LEFT -> { throttle = -1; rudder = -1; }
            case FirstShipNetwork.BACKWARD_RIGHT -> { throttle = -1; rudder = 1; }
            case FirstShipNetwork.HELM_IDLE -> {}
            default -> { return false; }
        }
        helmControl = new ShipRigidMotion.Control(throttle, rudder);
        helmControlTick = level().getGameTime();
        return true;
    }

    /**
     * One step per helm command. Validates an actual rider, cooldown, source
     * integrity and destination BEFORE moving any vanilla block.
     */
    public boolean helmStep(ServerPlayer driver, int action) {
        if (!isNativeHull() || driver.getVehicle() != this || getFirstPassenger() != driver
                || !(level() instanceof net.minecraft.server.level.ServerLevel server)
                || action < FirstShipNetwork.FORWARD || action > FirstShipNetwork.HELM_IDLE)
            return false;
        if (isMoving()) return continuousHelm(driver, action);
        if (smoothEnabled) {
            if (beginContinuousMotion(server)) return continuousHelm(driver, action);
            driver.displayClientMessage(Component.literal(
                    "Сборка движущегося корпуса не прошла проверку: движение запрещено для сохранности груза"), true);
            return false;
        }
        if (action > FirstShipNetwork.RIGHT
                || level().getGameTime() + 1e-6D < nextHelmAllowedTick) return false;
        int nextTurn = quarterTurns;
        BlockPos nextAnchor = blockAnchor;
        if (action == FirstShipNetwork.LEFT) nextTurn = Math.floorMod(quarterTurns - 1, 4);
        else if (action == FirstShipNetwork.RIGHT) nextTurn = (quarterTurns + 1) % 4;
        else {
            int signed = action == FirstShipNetwork.FORWARD ? 1 : -1;
            int dx = quarterTurns == 1 ? -signed : quarterTurns == 3 ? signed : 0;
            int dz = quarterTurns == 0 ? signed : quarterTurns == 2 ? -signed : 0;
            nextAnchor = blockAnchor.offset(dx, 0, dz);
        }
        if (nextTurn == quarterTurns && nextAnchor.equals(blockAnchor)) return false;
        BlockPos oldAnchor = blockAnchor;
        int oldTurn = quarterTurns;
        java.util.List<ServerPlayer> walkers = new java.util.ArrayList<>();
        java.util.Map<ServerPlayer, FirstShipHull.Point> localPositions = new java.util.HashMap<>();
        AABB nearby = new AABB(oldAnchor).inflate(9D, 9D, 9D);
        for (ServerPlayer p : server.getEntitiesOfClass(ServerPlayer.class, nearby)) {
            if (p.containerMenu != p.inventoryMenu) {
                driver.displayClientMessage(Component.literal(
                        "Закройте все окна сундуков/печей перед перемещением корабля"), true);
                return false;
            }
            if (p == driver || p.isPassenger() || p.isSpectator() || p.getAbilities().flying) continue;
            FirstShipHull.Point local = FirstShipHull.toLocal(p.getX(), p.getZ(),
                    getX(), getZ(), getYRot());
            boolean onShip = FirstShipHull.withinHull(local.x(), local.z(), .1D)
                    && ((p.getY() >= holdY() - .25D && p.getY() <= holdY() + 2.9D)
                    || (p.getY() >= deckY() - .25D && p.getY() <= deckY() + 2.3D));
            if (onShip) { walkers.add(p); localPositions.put(p, local); }
        }
        if (!FirstShipBlockHull.move(server, oldAnchor, oldTurn, nextAnchor, nextTurn,
                rigLevel, fabricQuality, rigLevel, fabricQuality, geometryVersion)) {
            driver.displayClientMessage(Component.literal(
                    "Ход заблокирован: мелководье, препятствие, изменённый корпус или груз"), true);
            nextHelmAllowedTick = level().getGameTime() + 3D;
            return false;
        }
        initializeNativeHull(nextAnchor, nextTurn);
        // The captain was intentionally excluded from free-walker transfers;
        // update the MOUNTED seat explicitly to keep the captain on deck.
        positionRider(driver);
        // Fractional tick scheduling produces 10/8.70/7.69 tick steps
        // (tier I fabrics). Do not round every step or the bonuses vanish.
        nextHelmAllowedTick = new ShipRigging(rigLevel, fabricQuality)
                .nextAvailableTick(nextHelmAllowedTick, level().getGameTime());
        for (ServerPlayer p : walkers) {
            FirstShipHull.Point oldLocal = localPositions.get(p);
            FirstShipHull.Point target = FirstShipHull.toWorld(
                    oldLocal.x(), oldLocal.z(), getX(), getZ(), getYRot());
            // A server-player teleport emits an authoritative position packet;
            // Entity.setPos alone only moves the server and rubber-bands clients.
            p.teleportTo(target.x(), p.getY(), target.z());
            p.fallDistance = 0F;
        }
        return true;
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("FirstShipSurface", entityData.get(SURFACE));
        tag.putBoolean("FirstShipNative", nativeHull);
        tag.putBoolean("FirstShipSmoothEnabled", smoothEnabled);
        tag.putBoolean("FirstShipSailUpgradeChecked", !legacySailUpgradePending);
        if (movingBody != null) tag.put("FirstShipMovingBody", movingBody.save());
        if (nativeHull) {
            tag.putLong("FirstShipAnchor", blockAnchor.asLong());
            tag.putInt("FirstShipTurns", quarterTurns);
            tag.putInt("FirstShipRigLevel", rigLevel);
            tag.putInt("FirstShipFabricQuality", fabricQuality);
            tag.putInt("FirstShipGeometryVersion", geometryVersion);
        }
        tag.put("FirstShipDesign", design.save());
        if (geometryVersion == 9 && superstructure != null)
            tag.put("FirstShipSuperstructure", ShipSuperstructureData.save(superstructure));
        tag.put("FirstShipModules", quarantinedModules != null ? quarantinedModules.copy() : storeModules(registryAccess()));
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        clearMovingCargoCache();
        nativeHull = tag.getBoolean("FirstShipNative");
        smoothEnabled = tag.getBoolean("FirstShipSmoothEnabled");
        if (nativeHull) {
            initializeNativeHull(BlockPos.of(tag.getLong("FirstShipAnchor")),
                    tag.getInt("FirstShipTurns"));
            // Old pre-sail saves physically contain the original single wool sail.
            int rig = tag.contains("FirstShipRigLevel", Tag.TAG_INT)
                    ? tag.getInt("FirstShipRigLevel") : 1;
            int fabric = tag.contains("FirstShipFabricQuality", Tag.TAG_INT)
                    ? tag.getInt("FirstShipFabricQuality") : 1;
            try { setRigging(rig, fabric); }
            catch (IllegalArgumentException invalid) { setRigging(1, 1); }
            setGeometryVersion(tag.contains("FirstShipGeometryVersion", Tag.TAG_INT)
                    ? tag.getInt("FirstShipGeometryVersion") : 1);
            // V5/V6 world-block hulls can be validated and detached by the
            // current contraption path. Older saves predate the opt-in flag.
            smoothEnabled |= geometryVersion >= 5;
        }
        if (tag.contains("FirstShipMovingBody", Tag.TAG_COMPOUND)) {
            movingBody = ShipMovingContraption.load(tag.getCompound("FirstShipMovingBody"));
            if (movingBody != null && movingBody.detached()) {
                entityData.set(MOVING, true);
                syncMovingCargo();
                ShipRigidMotion.Pose p = movingBody.pose();
                setPos(p.x(), movingBody.waterlineY(), p.z());
                setYRot((float)p.yaw());
                setYHeadRot((float)p.yaw());
                smoothEnabled = true;
            } else movingBody = null;
        }
        legacySailUpgradePending = nativeHull && geometryVersion == 5 && rigLevel <= 1
                && !tag.getBoolean("FirstShipSailUpgradeChecked") && movingBody == null;
        if (tag.contains("FirstShipSurface", Tag.TAG_FLOAT)) setWaterSurface(tag.getFloat("FirstShipSurface"));
        CompoundTag pack = new CompoundTag();
        pack.putInt("Version", 2);
        pack.putInt("RigLevel", rigLevel);
        pack.putInt("FabricQuality", fabricQuality);
        pack.putInt("GeometryVersion", geometryVersion);
        if (tag.contains("FirstShipDesign", Tag.TAG_COMPOUND))
            pack.put("Design", tag.getCompound("FirstShipDesign").copy());
        if (geometryVersion == 9 && tag.contains("FirstShipSuperstructure", Tag.TAG_COMPOUND))
            pack.put("Superstructure", tag.getCompound("FirstShipSuperstructure").copy());
        pack.put("Modules", tag.getList("FirstShipModules", Tag.TAG_COMPOUND));
        CompoundTag root = new CompoundTag();
        root.put("FirstShipPack", pack);
        ItemStack temporary = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        temporary.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        if (!loadPacked(temporary)) {
            // Preserve malformed raw ItemStack tags during autosave. The ship
            // stays in-world but the key must never pack a partial empty copy.
            quarantinedModules = tag.getList("FirstShipModules", Tag.TAG_COMPOUND).copy();
            inventory.clear();
            kind.clear();
            furnaces.clear();
            syncModules();
        }
    }
}
