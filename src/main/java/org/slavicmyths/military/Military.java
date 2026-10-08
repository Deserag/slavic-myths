package org.slavicmyths.military;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
import org.slavicmyths.village.VillageRoles;
public final class Military {
 public static final int PATROL_RADIUS=32, RESPONSE_RADIUS=48, LOST_TABLE_GRACE=1200, ACTIVE_LIMIT=3, RAID_SEARCH=224;
 public static final long RAID_COOLDOWN=48000;
 public static final int RAID_CHANCE=8,RAID_DURATION=24000;
 public static final int[] ARCHETYPE_WEIGHTS={30,25,20,15,10};
 public static final DeferredHolder<Block,MilitaryTable> TABLE=ModBlocks.BLOCKS.register("druzhinnik_table",MilitaryTable::new);
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<MilitaryTableTile>> TILE=ModTiles.TILES.register("druzhinnik_table",()->BlockEntityType.Builder.of(MilitaryTableTile::new,TABLE.get()).build(null));
 public static final DeferredHolder<Item,Item> TOKEN=ModItems.ITEMS.register("bandit_token",()->new Item(new Item.Properties()));
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<CompoundTag>> QUESTS=VillageRoles.ATTACHMENTS.register("military_quests",()->AttachmentType.builder(()->new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().build());
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> ARCHETYPE=VillageRoles.ATTACHMENTS.register("guard_archetype",()->AttachmentType.builder(()->-1).serialize(com.mojang.serialization.Codec.INT).sync(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
 public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> ACTION=VillageRoles.ATTACHMENTS.register("guard_action",()->AttachmentType.builder(()->0).sync(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
 public static final TagKey<EntityType<?>> BANDITS=TagKey.create(Registries.ENTITY_TYPE,VillageRoles.id("bandits")),HOSTILES=TagKey.create(Registries.ENTITY_TYPE,VillageRoles.id("village_hostiles"));
 public static void init(){ModItems.ITEMS.register("druzhinnik_table",()->new BlockItem(TABLE.get(),new Item.Properties()));MilitaryMenu.register();}
 public static boolean guard(net.minecraft.world.entity.npc.Villager v){return !v.isBaby()&&v.getVillagerData().getProfession()==VillageRoles.ROLES.get("druzhinnik").get();}
 private Military(){}
}
