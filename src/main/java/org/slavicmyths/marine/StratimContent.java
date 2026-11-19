package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Separate IDs, strict open-sky ocean spawning and one guaranteed head trophy. */
public final class StratimContent {
    public static final DeferredHolder<EntityType<?>,EntityType<StratimEntity>> BOSS=
        ModEntities.ENTITIES.register("stratim",()->EntityType.Builder.of(StratimEntity::new,MobCategory.MONSTER)
            .sized(4.5F,4.5F).clientTrackingRange(64).updateInterval(1).build("slavicmyths:stratim"));
    public static final DeferredHolder<Item,Item> EGG=ModItems.ITEMS.register("stratim_spawn_egg",
        ()->new DeferredSpawnEggItem(BOSS,0x243749,0xD4B379,new Item.Properties()));
    public static final DeferredHolder<Block,Block> HEAD=ModBlocks.BLOCKS.register("stratim_head",
        StratimHeadBlock::new);
    public static final DeferredHolder<Item,Item> HEAD_ITEM=ModItems.ITEMS.register("stratim_head",
        ()->new BlockItem(HEAD.get(),new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<StratimHeadBlock.Tile>> HEAD_TILE=
        ModTiles.TILES.register("stratim_head",()->BlockEntityType.Builder.of(StratimHeadBlock.Tile::new,HEAD.get()).build(null));
    public static void init(IEventBus bus){
        bus.addListener(StratimContent::attributes);
        bus.addListener(StratimContent::placements);
        bus.addListener(StratimContent::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(StratimContent::drops);
    }
    private static void attributes(EntityAttributeCreationEvent e){e.put(BOSS.get(),StratimEntity.attributes().build());}
    private static void creative(BuildCreativeModeTabContentsEvent e){
        if(e.getTabKey()==CreativeModeTabs.SPAWN_EGGS)e.accept(EGG.get());
        if(e.getTabKey()==CreativeModeTabs.FUNCTIONAL_BLOCKS)e.accept(HEAD_ITEM.get());
    }
    private static void placements(RegisterSpawnPlacementsEvent e){
        e.register(BOSS.get(),SpawnPlacementTypes.NO_RESTRICTIONS,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type,world,reason,pos,random)->{
                Level level=world.getLevel();
                if(level.dimension()!=Level.OVERWORLD||level.getDifficulty()==Difficulty.PEACEFUL)return false;
                if(reason==MobSpawnType.NATURAL){
                    if(random.nextInt(32)!=0)return false;
                    if(!level.getEntitiesOfClass(StratimEntity.class,new AABB(pos).inflate(256)).isEmpty())return false;
                }
                // The ocean biome modifier restricts the actual biome. Require broad open air,
                // a sea surface below and no roofs or cliffs around the 32-block wing span.
                int surface=world.getHeight(Heightmap.Types.WORLD_SURFACE,pos.getX(),pos.getZ());
                if(pos.getY()<surface+12||pos.getY()>surface+65)return false;
                boolean water=false;
                for(int y=surface;y>=surface-4;y--){
                    if(world.getFluidState(new BlockPos(pos.getX(),y,pos.getZ())).is(net.minecraft.tags.FluidTags.WATER)){water=true;break;}
                }
                if(!water)return false;
                for(int x=-18;x<=18;x+=6)for(int z=-18;z<=18;z+=6)
                    for(int y=-3;y<=5;y+=4){
                        BlockPos check=pos.offset(x,y,z);
                        if(!level.hasChunkAt(check)||
                            !world.getBlockState(check).getCollisionShape(world,check).isEmpty())return false;
                    }
                return true;
            },RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static void drops(LivingDropsEvent e){
        if(!(e.getEntity() instanceof StratimEntity bird)||bird.level().isClientSide||bird.isAlive())return;
        Item trophy=HEAD_ITEM.get();
        e.getDrops().removeIf(drop->drop.getItem().is(trophy));
        e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(bird.level(),
            bird.getX(),bird.getY()+1,bird.getZ(),new ItemStack(trophy)));
    }
    private StratimContent(){}
}
