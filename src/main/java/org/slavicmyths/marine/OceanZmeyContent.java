package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModTiles;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;

/** Independent ocean boss, registered without touching the existing Deep Sea Serpent. */
public final class OceanZmeyContent {
    public static final DeferredHolder<EntityType<?>,EntityType<OceanZmeyEntity>> BOSS =
        ModEntities.ENTITIES.register("ocean_zmey",()->
            EntityType.Builder.of(OceanZmeyEntity::new,MobCategory.MONSTER)
                .sized(5.2F,4.4F).clientTrackingRange(64).updateInterval(1)
                .build("slavicmyths:ocean_zmey"));
    public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register(
        "ocean_zmey_spawn_egg",()->
            new DeferredSpawnEggItem(BOSS,0x163847,0x83C7D4,new Item.Properties()){
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack){
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "item.slavicmyths.ocean_zmey_spawn_egg","Яйцо призыва Змея морских бурь");
                }
            });
    public static final DeferredHolder<Block,Block> HEAD=ModBlocks.BLOCKS.register(
        "ocean_zmey_head",()->new OceanZmeyHeadBlock(true));
    public static final DeferredHolder<Item,Item> HEAD_ITEM=ModItems.ITEMS.register(
        "ocean_zmey_head",()->new BlockItem(HEAD.get(),new Item.Properties().rarity(Rarity.EPIC)){
            @Override public net.minecraft.network.chat.Component getName(ItemStack stack){
                return net.minecraft.network.chat.Component.translatableWithFallback(
                    "block.slavicmyths.ocean_zmey_head","Голова Змея морских бурь");
            }
        });
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<OceanZmeyHeadBlock.Tile>>
        HEAD_TILE=ModTiles.TILES.register("ocean_zmey_head",()->
            BlockEntityType.Builder.of(OceanZmeyHeadBlock.Tile::new,HEAD.get()).build(null));

    public static void init(IEventBus bus){
        bus.addListener(OceanZmeyContent::attributes);
        bus.addListener(OceanZmeyContent::placements);
        bus.addListener(OceanZmeyContent::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(OceanZmeyContent::drops);
    }
    private static void attributes(EntityAttributeCreationEvent e){
        e.put(BOSS.get(),OceanZmeyEntity.attributes().build());
    }
    private static void creative(BuildCreativeModeTabContentsEvent e){
        if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());
        if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)e.accept(HEAD_ITEM.get());
    }
    private static void placements(RegisterSpawnPlacementsEvent e){
        e.register(BOSS.get(),SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,world,reason,pos,random)->{
                Level level=world.getLevel();
                if(level.dimension()!=Level.OVERWORLD||level.getDifficulty()==Difficulty.PEACEFUL)return false;
                if(!world.getBiome(pos).is(BiomeTags.IS_OCEAN))return false;
                for(int x=-3;x<=3;x+=3)for(int z=-3;z<=3;z+=3)
                    for(int y=-7;y<=2;y+=3){
                        BlockPos p=pos.offset(x,y,z);
                        if(!level.hasChunkAt(p)||!world.getFluidState(p).is(FluidTags.WATER)
                            ||!world.getBlockState(p).getCollisionShape(world,p).isEmpty())return false;
                    }
                if(reason==MobSpawnType.NATURAL){
                    if(random.nextInt(45)!=0)return false;
                    if(!level.getEntitiesOfClass(OceanZmeyEntity.class,
                        new AABB(pos).inflate(300)).isEmpty())return false;
                }
                return true;
            },RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static void drops(LivingDropsEvent e){
        if(!(e.getEntity() instanceof OceanZmeyEntity boss)||boss.level().isClientSide||boss.isAlive())return;
        e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
            boss.level(),boss.getX(),boss.getY()+1,boss.getZ(),new ItemStack(HEAD_ITEM.get())));
    }
    private OceanZmeyContent(){}
}
