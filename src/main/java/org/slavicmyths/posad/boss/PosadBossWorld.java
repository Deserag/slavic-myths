package org.slavicmyths.posad.boss;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.registry.ModEntities;

/** Bounded, non-retrogen Nether discovery: one boss candidate per 640x640 region.
 * No extra chunks are requested. Regions with successful boss creation persist as consumed.
 */
@EventBusSubscriber(modid="slavicmyths")
public final class PosadBossWorld extends SavedData {
    private static final int REGION = 640;
    private final Set<String> consumed = new HashSet<>();
    public static PosadBossWorld get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PosadBossWorld::new, (tag, lookup) -> {
            PosadBossWorld data = new PosadBossWorld();
            for (Tag entry : tag.getList("Consumed", 8)) data.consumed.add(entry.getAsString());
            return data;
        }), "slavicmyths_posad_boss_regions");
    }
    @Override public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        for (String key : consumed) list.add(StringTag.valueOf(key));
        tag.put("Consumed", list);
        return tag;
    }
    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
    private static boolean naturalGround(BlockState state) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.BLACKSTONE) || state.is(Blocks.BASALT) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.CRIMSON_NYLIUM) || state.is(Blocks.WARPED_NYLIUM);
    }
    private static boolean safe(ServerLevel level, BlockPos pos, PosadBoss.Kind kind) {
        // The 7-block beast extends beyond its vanilla 3.5-wide hitbox: validate its entire ground support.
        int radius = kind == PosadBoss.Kind.BAUK ? 4 : 2;
        for (int x = -radius; x <= radius; x++) for (int z = -radius; z <= radius; z++) {
            BlockPos ground = pos.offset(x, -1, z);
            if (!level.hasChunkAt(ground) || !naturalGround(level.getBlockState(ground))) return false;
            for (int y = 0; y < 6; y++) {
                BlockPos air = pos.offset(x, y, z);
                if (!level.hasChunkAt(air) || !level.getBlockState(air).isAir()) return false;
            }
        }
        return true;
    }
    @SubscribeEvent public static void explore(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 200 != 0) return;
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.NETHER || level.getDifficulty() == Difficulty.PEACEFUL || player.isSpectator() || player.isCreative()) return;
        int rx = Math.floorDiv(player.getBlockX(), REGION);
        int rz = Math.floorDiv(player.getBlockZ(), REGION);
        long mixed = mix(level.getSeed() ^ (rx * 0x9e3779b97f4a7c15L) ^ (rz * 0xc2b2ae3d27d4eb4fL));
        // Probe different loaded directions around the explorer; a failed spot must not prevent natural encounters.
        String key = rx + ":" + rz;
        PosadBossWorld data = get(level);
        if (data.consumed.contains(key)) return;
        int roll = (int) Math.floorMod(mixed >>> 3, 100L);
        // A deterministic regional encounter: 35% Mytar, 25% Bauk, 40% no boss.
        if (roll >= 60) return;
        PosadBoss.Kind kind = roll < 35 ? PosadBoss.Kind.MYTAR : PosadBoss.Kind.BAUK;
        for (int probe = 0; probe < 16; probe++) {
            double angle = ((probe * 5 + (mixed & 7L)) % 16) * Math.PI / 8D;
            int radius = 56 + (probe % 4) * 12;
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * radius);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * radius);
            if (Math.floorDiv(x, REGION) != rx || Math.floorDiv(z, REGION) != rz) continue;
            if (player.distanceToSqr(x + .5D, player.getY(), z + .5D) < 48D * 48D) continue;
            BlockPos top = new BlockPos(x, player.getBlockY(), z);
            if (!level.hasChunkAt(top)) continue;
            for (int y = Math.min(level.getMaxBuildHeight() - 8, player.getBlockY() + 24); y >= Math.max(level.getMinBuildHeight() + 5, player.getBlockY() - 24); y--) {
                BlockPos pos = new BlockPos(x, y, z);
                // Cheap column test before checking the full 3x3 or 5x5 collision footprint.
                if (!level.hasChunkAt(pos) || !naturalGround(level.getBlockState(pos.below())) || !level.getBlockState(pos).isAir()) continue;
                if (!safe(level, pos, kind)) continue;
                PosadBoss boss = (kind == PosadBoss.Kind.MYTAR ? ModEntities.CURSED_MYTAR : ModEntities.BAUK).get().create(level);
                if (boss == null) return;
                boss.markNaturalEncounter();
                boss.moveTo(x + .5D, y, z + .5D, level.random.nextFloat() * 360F, 0F);
                if (!level.noCollision(boss) || !level.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().inflate(32D), p -> !p.isSpectator()).isEmpty()) continue;
                if (level.addFreshEntity(boss)) { data.consumed.add(key); data.setDirty(); return; }
            }
        }
    }
}
