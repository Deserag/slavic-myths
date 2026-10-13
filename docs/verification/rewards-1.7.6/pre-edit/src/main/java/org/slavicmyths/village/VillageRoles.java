package org.slavicmyths.village;

import com.google.common.collect.ImmutableSet;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.*;

public final class VillageRoles {
    public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE,"slavicmyths");
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION,"slavicmyths");
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"slavicmyths");
    // A tiny synchronized, saved visual index also survives conversion, which creates a new UUID.
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> OUTFIT = ATTACHMENTS.register("villager_outfit",()->AttachmentType.builder(()->-1).serialize(Codec.INT).sync(ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<WorkState>> WORK_STATE=ATTACHMENTS.register("villager_work_state",()->AttachmentType.builder(WorkState::new).build());
    public static final DeferredHolder<Block,Block> MILLSTONE = ModBlocks.BLOCKS.register("millstone",MillstoneBlock::new);
    public static final Map<String,DeferredHolder<PoiType,PoiType>> JOBS = new LinkedHashMap<>();
    public static final Map<String,DeferredHolder<VillagerProfession,VillagerProfession>> ROLES = new LinkedHashMap<>();
    public static final Map<String,Supplier<Block>> WORKSTATIONS = new LinkedHashMap<>();
    public static void init(IEventBus bus) {
        SettlementStorage.init(bus);
        org.slavicmyths.military.Military.init();
        ModItems.ITEMS.register("millstone",()->new BlockItem(MILLSTONE.get(),new Item.Properties()));
        role("druzhinnik",org.slavicmyths.military.Military.TABLE::get);
        role("miller",MILLSTONE::get);
        role("brewer",()->org.slavicmyths.brewing.Brewing.BLOCKS.get("fermentation_vat").get());
        role("weaver",()->org.slavicmyths.textile.Textiles.BLOCKS.get("loom_table").get());
        role("herder",()->org.slavicmyths.husbandry.Husbandry.FEEDER.get());
        role("hunter",()->org.slavicmyths.storage.Household.BLOCKS.get(org.slavicmyths.storage.Household.Kind.DRY).get());
        role("cook",ModBlocks.KITCHEN_TABLE::get);
        POIS.register(bus); PROFESSIONS.register(bus); ATTACHMENTS.register(bus);
    }
    private static void role(String id,Supplier<Block> block) {
        WORKSTATIONS.put(id,block);
        var poi=POIS.register(id,()->new PoiType(block.get().getStateDefinition().getPossibleStates().stream()
            .filter(VillageRoles::isWorkstationMaster)
            .collect(java.util.stream.Collectors.toUnmodifiableSet()),1,1));
        JOBS.put(id,poi);
        ROLES.put(id,PROFESSIONS.register(id,()->new VillagerProfession("slavicmyths:"+id,
            h->h.is(poi.getKey()),h->h.is(poi.getKey()),ImmutableSet.of(),ImmutableSet.of(),
            switch(id){case "weaver"->SoundEvents.VILLAGER_WORK_SHEPHERD; case "hunter"->SoundEvents.VILLAGER_WORK_BUTCHER; case "brewer","cook"->SoundEvents.VILLAGER_WORK_FISHERMAN; default->SoundEvents.VILLAGER_WORK_FARMER;})));
    }
    public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    public static boolean isWorkstationMaster(net.minecraft.world.level.block.state.BlockState s){
        if(s.getBlock() instanceof org.slavicmyths.storage.StorageBlock storage)return storage.part(s)==0;
        if(s.getBlock() instanceof org.slavicmyths.block.KitchenTableBlock)return s.getValue(org.slavicmyths.block.KitchenTableBlock.PART)==org.slavicmyths.block.KitchenTableBlock.Part.LEFT;
        return true;
    }
    private VillageRoles() {}
}
