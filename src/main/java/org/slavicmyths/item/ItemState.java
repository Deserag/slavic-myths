package org.slavicmyths.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slavicmyths.hunt.HuntTarget;

/** Immutable, persistent and synchronized state owned by ItemStacks. */
public final class ItemState {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "slavicmyths");
    public record RuneState(int slots, List<String> runes) {
        public RuneState {
            runes = List.copyOf(runes);
            if (slots < 0 || slots > 3 || runes.size() > slots || new HashSet<>(runes).size() != runes.size())
                throw new IllegalArgumentException("Invalid sockets");
        }
        private record Stored(int slots,List<String> runes) { }
        public static final Codec<RuneState> CODEC = RecordCodecBuilder.<Stored>create(instance -> instance.group(
                Codec.intRange(0,3).fieldOf("slots").forGetter(Stored::slots),
                Codec.STRING.sizeLimitedListOf(3).fieldOf("runes").forGetter(Stored::runes)
        ).apply(instance,Stored::new)).comapFlatMap(value -> {
            try { return DataResult.success(new RuneState(value.slots,value.runes)); }
            catch(IllegalArgumentException error) { return DataResult.error(error::getMessage); }
        }, value -> new Stored(value.slots,value.runes));
    }
    public static final Codec<HuntTarget> TARGET_CODEC = HuntTarget.CODEC;
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RuneState>> RUNES = component("runes", RuneState.CODEC);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<HuntTarget>> HUNT_TARGET = component("hunt_target", TARGET_CODEC);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> CLOTH_READY = component("cloth_ready", Codec.LONG);
    private static final Codec<ItemContainerContents> NINE_SLOTS = ItemContainerContents.CODEC.validate(contents ->
            contents.getSlots() <= 9 ? DataResult.success(contents) : DataResult.error(() -> "Inventory exceeds nine slots"));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> TROPHIES = inventory("hunt_trophies");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> FLIGHT_CARGO = inventory("flight_cargo");
    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> component(String id, Codec<T> codec) {
        return COMPONENTS.register(id, () -> DataComponentType.<T>builder().persistent(codec)
                .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(codec)).build());
    }
    private static DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> inventory(String id) {
        StreamCodec<RegistryFriendlyByteBuf, ItemContainerContents> stream = ItemContainerContents.STREAM_CODEC.map(ItemState::nineSlots, ItemState::nineSlots);
        return COMPONENTS.register(id, () -> DataComponentType.<ItemContainerContents>builder().persistent(NINE_SLOTS).networkSynchronized(stream).build());
    }
    private static ItemContainerContents nineSlots(ItemContainerContents contents) {
        if (contents.getSlots() > 9) throw new IllegalArgumentException("Inventory exceeds nine slots");
        return contents;
    }
    private static CompoundTag legacy(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(); }
    private static void removeLegacy(ItemStack stack, String key) {
        CompoundTag tag = legacy(stack); tag.remove(key);
        if (tag.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    public static HuntTarget target(ItemStack stack) {
        HuntTarget current = stack.get(HUNT_TARGET.get());
        return current != null ? current : HuntTarget.read(legacy(stack), "HuntTarget");
    }
    public static void target(ItemStack stack, HuntTarget target) {
        stack.set(HUNT_TARGET.get(), target);
        // Legacy flags remain foreign data; the component is authoritative.
        removeLegacy(stack, "HuntTarget");
    }
    public static long clothReady(ItemStack stack) { return stack.getOrDefault(CLOTH_READY.get(), legacy(stack).getLong("SlavicClothReady")); }
    public static void clothReady(ItemStack stack, long time) { stack.set(CLOTH_READY.get(), time); removeLegacy(stack,"SlavicClothReady"); }
    public static RuneState runes(ItemStack stack) {
        RuneState current = stack.get(RUNES.get()); if (current != null) return current;
        CompoundTag old = legacy(stack).getCompound("SlavicRunes");
        int slots = Math.clamp(old.getInt("Slots"),0,3);
        List<String> ids = new ArrayList<>(); ListTag list = old.getList("Runes",Tag.TAG_STRING);
        for (int i=0;i<list.size() && ids.size()<slots;i++) if (List.of("thunder","heat","forest","midday","shadow","protection","life","wind").contains(list.getString(i)) && !ids.contains(list.getString(i))) ids.add(list.getString(i));
        return new RuneState(slots,ids);
    }
    public static void runes(ItemStack stack, int slots, List<String> ids) {
        stack.set(RUNES.get(),new RuneState(slots,ids));
        CompoundTag root=legacy(stack), old=root.getCompound("SlavicRunes"); old.remove("Slots");old.remove("Runes");
        if (old.isEmpty()) root.remove("SlavicRunes"); else root.put("SlavicRunes",old);
        if (root.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA); else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
    }
    /** Read existing shield/bow snapshots in entity/player NBT, including pre-port nested stacks. */
    public static ItemStack snapshot(HolderLookup.Provider registries,CompoundTag input){
        CompoundTag item=input.copy();
        if(item.contains("Count"))item=(CompoundTag)DataFixers.getDataFixer().update(References.ITEM_STACK,
            new Dynamic<>(NbtOps.INSTANCE,item),2586,SharedConstants.getCurrentVersion().getDataVersion().getVersion()).getValue();
        return ItemStack.parseOptional(registries,item);
    }
    public static NonNullList<ItemStack> inventory(ItemStack stack, boolean flight, HolderLookup.Provider registries) {
        var type = flight ? FLIGHT_CARGO.get() : TROPHIES.get();
        ItemContainerContents contents = stack.get(type);
        NonNullList<ItemStack> items = NonNullList.withSize(9,ItemStack.EMPTY);
        if (contents != null) { nineSlots(contents).copyInto(items); return items; }
        String key = flight ? "FlightCargo" : "HuntTrophies";
        CompoundTag old=legacy(stack);
        ListTag list = flight ? old.getCompound(key).getList("Items",Tag.TAG_COMPOUND) : old.getList(key,Tag.TAG_COMPOUND);
        for (Tag raw : list) {
            CompoundTag item = ((CompoundTag)raw).copy(); int slot=item.getByte("Slot") & 255;
            if (slot >= 9) throw new IllegalStateException("Legacy inventory slot exceeds nine slots; original data retained");
            item.remove("Slot");
            // Nested legacy stacks are opaque custom NBT to vanilla's outer-stack data fixer.
            if (item.contains("Count")) item = (CompoundTag)DataFixers.getDataFixer().update(References.ITEM_STACK,
                    new Dynamic<>(NbtOps.INSTANCE,item),2586,SharedConstants.getCurrentVersion().getDataVersion().getVersion()).getValue();
            items.set(slot,ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,registries),item).getOrThrow());
        }
        // Only commit after every nested stack decoded. Failed migration leaves the original key intact.
        if (old.contains(key)) inventory(stack,flight,items);
        return items;
    }
    public static void inventory(ItemStack stack, boolean flight, List<ItemStack> items) {
        if (items.size()!=9) throw new IllegalArgumentException("Expected nine inventory slots");
        stack.set(flight?FLIGHT_CARGO.get():TROPHIES.get(),ItemContainerContents.fromItems(items));
        removeLegacy(stack,flight?"FlightCargo":"HuntTrophies");
    }
    public static void removeCargo(ItemStack stack) { stack.remove(FLIGHT_CARGO.get());removeLegacy(stack,"FlightCargo"); }
    private ItemState() { }
}
