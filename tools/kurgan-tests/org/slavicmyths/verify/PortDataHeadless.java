package org.slavicmyths.verify;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.depth.PoolIndex;
import org.slavicmyths.rpg.RpgNbt;
import org.slavicmyths.yaga.YagaHutPlan;

/** Actual selected production classes on target APIs; no mod/game bootstrap. */
public final class PortDataHeadless {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        Map<BlockPos,String> expected = new LinkedHashMap<>();
        try (java.io.Reader reader = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
                JsonObject row = element.getAsJsonObject();
                expected.put(new BlockPos(row.get("x").getAsInt(),row.get("y").getAsInt(),row.get("z").getAsInt()),
                        row.get("block").getAsString());
            }
        }
        check(expected.equals(new YagaHutPlan().blocks), "Hut blueprint changed during BlockPos migration");
        for (BlockPos spawn : Arrays.asList(BlockPos.ZERO,new BlockPos(-10000,64,-10000),new BlockPos(10000,64,10000))) {
            List<BlockPos> a = YagaHutPlan.candidates(72345,spawn);
            check(a.size()==64 && a.equals(YagaHutPlan.candidates(72345,spawn)), "Unbounded/nondeterministic hut candidates");
            for (BlockPos pos : a) {
                double distance = Math.sqrt(Math.pow(pos.getX()-spawn.getX(),2)+Math.pow(pos.getZ()-spawn.getZ(),2));
                check(distance>=819 && distance<=2471, "Hut candidate range changed");
            }
        }
        CompoundTag item = new CompoundTag();
        item.putString("ForeignData","preserve");
        RpgNbt.runes(item,2,Arrays.asList("forest","thunder"));
        CompoundTag copy = item.copy();
        RpgNbt.runes(copy,1,Collections.singletonList("life"));
        check(item.getCompound("SlavicRunes").getList("Runes",8).size()==2,
                "Rune NBT copy aliases original");
        check(copy.getString("ForeignData").equals("preserve"),"Rune edit erased unrelated data");
        try {
            RpgNbt.runes(item,2,Arrays.asList("forest","forest"));
            throw new AssertionError("Duplicate rune accepted");
        } catch (IllegalArgumentException expectedFailure) { }
        PoolIndex index = new PoolIndex();
        BlockPos near = new BlockPos(-100,50,-100), far = new BlockPos(400,50,400);
        index.record(near); index.record(far); index.defeat(near);
        PoolIndex loaded = new PoolIndex(); loaded.load(index.save(new CompoundTag()));
        check(loaded.defeated(near) && loaded.nearest(near).equals(far),
                "Defeated pool became an active target on reload");
        check(loaded.save(new CompoundTag()).equals(index.save(new CompoundTag())), "Pool NBT changed");
        System.out.println("PASS: exact old hut blueprint, 64 bounded seeded candidates at negative/positive coordinates, " +
                "rune NBT isolation, pool SavedData roundtrip. Target 1.21.1 API; no Minecraft launch. " +
                "ItemStack components, full mod boot and world placement remain unverified.");
    }
}
