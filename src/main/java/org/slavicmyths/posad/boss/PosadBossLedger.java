package org.slavicmyths.posad.boss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.slavicmyths.registry.ModItems;

/** Nether-wide escrow, pending refunds and genuine claim IDs; not owned by a transient boss entity. */
public final class PosadBossLedger extends SavedData {
    private final Map<UUID, Map<UUID, Integer>> escrow = new HashMap<>();
    private final Map<UUID, Integer> pending = new HashMap<>();
    private final Map<UUID, Integer> sickleMisses = new HashMap<>();
    private final Set<UUID> issued = new HashSet<>();
    private final Set<UUID> redeemed = new HashSet<>();
    public static PosadBossLedger get(ServerLevel level) {
        ServerLevel nether = level.getServer().getLevel(Level.NETHER);
        if (nether == null) throw new IllegalStateException("Nether escrow dimension not loaded");
        return nether.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PosadBossLedger::new, (tag, lookup) -> {
            PosadBossLedger result = new PosadBossLedger();
            for (Tag raw : tag.getList("Escrow", 10)) {
                CompoundTag row = (CompoundTag) raw;
                result.escrow.computeIfAbsent(row.getUUID("Boss"), key -> new HashMap<>()).put(row.getUUID("Player"), row.getInt("Count"));
            }
            for (Tag raw : tag.getList("Players", 10)) {
                CompoundTag row = (CompoundTag) raw;
                UUID player = row.getUUID("Player");
                if (row.getInt("Pending") > 0) result.pending.put(player, row.getInt("Pending"));
                if (row.getInt("Misses") > 0) result.sickleMisses.put(player, row.getInt("Misses"));
            }
            for (Tag raw : tag.getList("Claims", 10)) {
                CompoundTag row = (CompoundTag) raw;
                UUID id = row.getUUID("Id");
                result.issued.add(id);
                if (row.getBoolean("Redeemed")) result.redeemed.add(id);
            }
            return result;
        }), "slavicmyths_posad_boss_ledger");
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag escrowList = new ListTag();
        for (var boss : escrow.entrySet()) for (var player : boss.getValue().entrySet()) {
            CompoundTag row = new CompoundTag();
            row.putUUID("Boss", boss.getKey());
            row.putUUID("Player", player.getKey());
            row.putInt("Count", player.getValue());
            escrowList.add(row);
        }
        tag.put("Escrow", escrowList);
        ListTag players = new ListTag();
        Set<UUID> all = new HashSet<>(pending.keySet());
        all.addAll(sickleMisses.keySet());
        for (UUID id : all) {
            CompoundTag row = new CompoundTag();
            row.putUUID("Player", id);
            row.putInt("Pending", pending.getOrDefault(id, 0));
            row.putInt("Misses", sickleMisses.getOrDefault(id, 0));
            players.add(row);
        }
        tag.put("Players", players);
        ListTag claims = new ListTag();
        for (UUID id : issued) {
            CompoundTag row = new CompoundTag();
            row.putUUID("Id", id);
            row.putBoolean("Redeemed", redeemed.contains(id));
            claims.add(row);
        }
        tag.put("Claims", claims);
        return tag;
    }
    public int stolen(UUID boss, UUID player) { return escrow.getOrDefault(boss, Map.of()).getOrDefault(player, 0); }
    /** Only a successful, server-confirmed melee hit calls this method. */
    public int steal(PosadBoss boss, ServerPlayer player, int requested) {
        if (boss.level().isClientSide || requested <= 0) return 0;
        int allowed = Math.min(requested, 24 - stolen(boss.getUUID(), player.getUUID()));
        int removed = 0;
        for (int slot = 0; slot < 36 && removed < allowed; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (!item.is(ModItems.ANCIENT_COIN.get())) continue;
            int take = Math.min(item.getCount(), allowed - removed);
            item.shrink(take);
            removed += take;
        }
        if (removed > 0) {
            escrow.computeIfAbsent(boss.getUUID(), key -> new HashMap<>()).merge(player.getUUID(), removed, Integer::sum);
            player.getInventory().setChanged();
            setDirty();
        }
        return removed;
    }
    /** Resolve every victim once, whether alive, offline, or in a different dimension. */
    public void settle(ServerLevel level, UUID boss, boolean victory) {
        Map<UUID, Integer> entries = escrow.remove(boss);
        if (entries == null) return;
        for (var entry : entries.entrySet()) {
            int amount = entry.getValue();
            int bonus = victory ? Math.min(4, Math.max(1, (amount + 9) / 10)) : 0;
            pending.merge(entry.getKey(), amount + bonus, Integer::sum);
        }
        setDirty();
        for (UUID id : entries.keySet()) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null) collect(player);
        }
    }
    public void collect(ServerPlayer player) {
        Integer available = pending.get(player.getUUID());
        if (available == null || available <= 0) return;
        pending.remove(player.getUUID());
        setDirty();
        while (available > 0) {
            int batch = Math.min(64, available);
            ItemStack refund = new ItemStack(ModItems.ANCIENT_COIN.get(), batch);
            player.getInventory().add(refund);
            if (!refund.isEmpty()) player.drop(refund, false);
            available -= batch;
        }
    }
    public boolean sickle(ServerPlayer player, net.minecraft.util.RandomSource random) {
        int misses = sickleMisses.getOrDefault(player.getUUID(), 0);
        boolean awarded = misses >= 4 || random.nextFloat() < .20F;
        if (awarded) sickleMisses.remove(player.getUUID());
        else sickleMisses.put(player.getUUID(), misses + 1);
        setDirty();
        return awarded;
    }
    public ItemStack mint(net.minecraft.world.item.Item trophy, UUID boss, UUID player) {
        ItemStack item = new ItemStack(trophy);
        UUID id = UUID.randomUUID();
        CompoundTag tag = new CompoundTag();
        tag.putUUID("PosadClaimId", id);
        tag.putUUID("PosadBossId", boss);
        tag.putUUID("PosadWinnerId", player);
        item.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        issued.add(id);
        setDirty();
        return item;
    }
    public boolean redeem(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.hasUUID("PosadClaimId")) return false;
        UUID id = tag.getUUID("PosadClaimId");
        if (!issued.contains(id) || !redeemed.add(id)) return false;
        setDirty();
        return true;
    }
    private PosadBossLedger() {}
}
