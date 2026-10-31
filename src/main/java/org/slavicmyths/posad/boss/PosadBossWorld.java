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

/** Bounded, non-retrogen Nether discovery: one boss candidate per 3072x3072 region.
 * No extra chunks are requested. Regions with successful boss creation persist as consumed.
 */
@EventBusSubscriber(modid="slavicmyths")
public final class PosadBossWorld extends SavedData {
    private static final int REGION = 3072;
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
    private static boolean safe(ServerLevel level, BlockPos pos) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            BlockPos ground = pos.offset(x, -1, z);
            if (!level.hasChunkAt(ground) || !naturalGround(level.getBlockState(ground))) return false;
            for (int y = 0; y < 5; y++) {
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
        int x = rx * REGION + 256 + Math.floorMod((int)mixed, REGION - 512);
        int z = rz * REGION + 256 + Math.floorMod((int)(mixed >>> 32), REGION - 512);
        if (Math.abs(player.getX() - x) > 96 || Math.abs(player.getZ() - z) > 96) return;
        String key = rx + ":" + rz;
        PosadBossWorld data = get(level);
        if (data.consumed.contains(key)) return;
        PosadBoss.Kind kind = (mixed & 1) == 0 ? PosadBoss.Kind.MYTAR : PosadBoss.Kind.BAUK;
        for (int y = Math.min(level.getMaxBuildHeight() - 8, player.getBlockY() + 16); y >= Math.max(level.getMinBuildHeight() + 5, player.getBlockY() - 16); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!safe(level, pos)) continue;
            PosadBoss boss = (kind == PosadBoss.Kind.MYTAR ? ModEntities.CURSED_MYTAR : ModEntities.BAUK).get().create(level);
            if (boss == null) return;
            boss.markNaturalEncounter();
            boss.moveTo(x + .5D, y, z + .5D, level.random.nextFloat() * 360F, 0F);
            if (!level.noCollision(boss) || !level.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().inflate(32D), p -> !p.isSpectator()).isEmpty()) return;
            if (level.addFreshEntity(boss)) { data.consumed.add(key); data.setDirty(); }
            return;
        }
    }
}
