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
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModBlocks;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.registry.ModTiles;
import net.minecraft.world.phys.AABB;

public final class StratimBirdContent {
    public static final DeferredHolder<EntityType<?>,EntityType<StratimBirdEntity>> BOSS=
        ModEntities.ENTITIES.register("stratim_bird",()->
            EntityType.Builder.of(StratimBirdEntity::new,MobCategory.MONSTER)
                .sized(4.5F,4.0F).clientTrackingRange(48).updateInterval(1)
                .build("slavicmyths:stratim_bird"));
    public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register(
        "stratim_bird_spawn_egg",()->
            new DeferredSpawnEggItem(BOSS,0x244858,0xC7D5CC,new Item.Properties()){
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack){
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "item.slavicmyths.stratim_bird_spawn_egg","Яйцо призыва Стратим-птицы");
                }
            });
    public static final DeferredHolder<Block,Block> HEAD=ModBlocks.BLOCKS.register(
        "stratim_bird_head",()->new StratimBirdHeadBlock());
    public static final DeferredHolder<Item,Item> HEAD_ITEM=ModItems.ITEMS.register(
        "stratim_bird_head",()->
            new BlockItem(HEAD.get(),new Item.Properties().rarity(Rarity.EPIC)){
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack){
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "block.slavicmyths.stratim_bird_head","Голова Стратим-птицы");
                }
            });
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<StratimBirdHeadBlock.Tile>>
        HEAD_TILE=ModTiles.TILES.register("stratim_bird_head",()->
            BlockEntityType.Builder.of(StratimBirdHeadBlock.Tile::new,HEAD.get()).build(null));
    public static void init(IEventBus bus){
        bus.addListener(StratimBirdContent::attributes);
        bus.addListener(StratimBirdContent::placements);
        bus.addListener(StratimBirdContent::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(StratimBirdContent::drops);
    }
    private static void attributes(EntityAttributeCreationEvent e){
        e.put(BOSS.get(),StratimBirdEntity.attributes().build());
    }
    private static void creative(BuildCreativeModeTabContentsEvent e){
        if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());

    }
    private static void placements(RegisterSpawnPlacementsEvent e){
        e.register(BOSS.get(),SpawnPlacementTypes.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type,world,reason,pos,random)->{
                Level level=world.getLevel();
                if(level.dimension()!=Level.OVERWORLD||level.getDifficulty()==Difficulty.PEACEFUL)return false;
                if(!world.getBiome(pos).is(BiomeTags.IS_OCEAN))return false;
                // Require an ocean surface and an actual navigable air corridor.
                boolean surface=world.getFluidState(pos).is(FluidTags.WATER)
                    ||world.getFluidState(pos.below()).is(FluidTags.WATER);
                if(!surface || !level.canSeeSky(pos.above(2)))return false;
                // Scan the full wingspan and upper air column, including loaded-neighbor safety.
                // Avoid spawning in caves, ship cabins, under bridges or beside cliffs.
                for(int x=-16;x<=16;x+=4)
                    for(int z=-16;z<=16;z+=4)
                        for(int y=2;y<=18;y+=4){
                            BlockPos p=pos.offset(x,y,z);
                            if(!level.hasChunkAt(p)
                                ||!world.getBlockState(p).getCollisionShape(world,p).isEmpty()
                                ||!world.getFluidState(p).isEmpty())return false;
                        }
                if(reason==MobSpawnType.NATURAL){
                    if(random.nextInt(24)!=0)return false;
                    if(!level.getEntitiesOfClass(StratimBirdEntity.class,
                        new AABB(pos).inflate(256)).isEmpty())return false;
                }
                return true;
            },RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static void drops(LivingDropsEvent e){
        if(!(e.getEntity() instanceof StratimBirdEntity bird)
            ||bird.level().isClientSide||bird.isAlive())return;
        Item head=HEAD_ITEM.get();
        e.getDrops().removeIf(drop->drop.getItem().is(head));
        e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
            bird.level(),bird.getX(),bird.getY()+1,bird.getZ(),new ItemStack(head)));
    }
    private StratimBirdContent(){}
}
