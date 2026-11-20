package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
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

/** Independent ocean content. No other boss's registries, AI, or loot rules are modified. */
public final class TsarPikeContent {
    public static final DeferredHolder<EntityType<?>, EntityType<TsarPikeEntity>> BOSS =
        ModEntities.ENTITIES.register("tsar_pike", () ->
            EntityType.Builder.of(TsarPikeEntity::new, MobCategory.MONSTER)
                .sized(2.5F, 2.5F).clientTrackingRange(24).updateInterval(1)
                .build("slavicmyths:tsar_pike"));

    public static final DeferredHolder<Item, Item> EGG =
        ModItems.ITEMS.register("tsar_pike_spawn_egg", () ->
            new DeferredSpawnEggItem(BOSS, 0x405C3D, 0xC4BB78, new Item.Properties()));

    public static final DeferredHolder<Block, Block> HEAD =
        ModBlocks.BLOCKS.register("tsar_pike_head", () -> new TsarPikeHeadBlock());

    public static final DeferredHolder<Item, Item> HEAD_ITEM =
        ModItems.ITEMS.register("tsar_pike_head", () ->
            new BlockItem(HEAD.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TsarPikeHeadBlock.Tile>> HEAD_TILE =
        ModTiles.TILES.register("tsar_pike_head", () ->
            BlockEntityType.Builder.of(TsarPikeHeadBlock.Tile::new, HEAD.get()).build(null));

    public static void init(IEventBus bus) {
        bus.addListener(TsarPikeContent::attributes);
        bus.addListener(TsarPikeContent::spawnPlacements);
        bus.addListener(TsarPikeContent::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(TsarPikeContent::drops);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(BOSS.get(), TsarPikeEntity.attributes().build());
    }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) event.accept(EGG.get());
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(HEAD_ITEM.get());
    }
    private static void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(BOSS.get(), SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type, world, reason, pos, random) -> {
                if (world.getLevel().dimension() != net.minecraft.world.level.Level.OVERWORLD
                    || world.getLevel().getDifficulty() == Difficulty.PEACEFUL) return false;
                if (!world.getFluidState(pos).is(FluidTags.WATER)
                    || !world.getFluidState(pos.above()).is(FluidTags.WATER)
                    || !world.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
                // A large fish must have a navigable channel; no spawning inside stone or boats.
                for (int x = -2; x <= 2; x += 2)
                    for (int z = -2; z <= 2; z += 2)
                        for (int y = 0; y <= 2; y++) {
                            BlockPos p = pos.offset(x, y, z);
                            if (!world.getFluidState(p).is(FluidTags.WATER)
                                || !world.getBlockState(p).getCollisionShape(world, p).isEmpty())
                                return false;
                        }
                // Natural generation must not accumulate multiple persistent bosses in one waterway.
                if (reason == MobSpawnType.NATURAL) {
                    if (!world.getLevel().getEntitiesOfClass(TsarPikeEntity.class,
                        new net.minecraft.world.phys.AABB(pos).inflate(96)).isEmpty()) return false;
                    return random.nextInt(8) == 0;
                }
                return true;
            }, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    /** The head is a guaranteed one-per-kill trophy; duplicate loot entries are removed. */
    private static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof TsarPikeEntity boss)
            || boss.level().isClientSide || boss.isAlive()) return;
        Item item = HEAD_ITEM.get();
        event.getDrops().removeIf(drop -> drop.getItem().is(item));
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
            boss.level(), boss.getX(), boss.getY() + 0.8, boss.getZ(), new ItemStack(item)));
    }
    private TsarPikeContent() {}
}
