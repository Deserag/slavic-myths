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
import net.minecraft.world.phys.AABB;
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

/** New encounter; no existing serpent IDs, world saves, heads or rewards are replaced. */
public final class ZhelvaContent {
    public static final DeferredHolder<EntityType<?>, EntityType<ZhelvaEntity>> BOSS =
        ModEntities.ENTITIES.register("zhelva", () ->
            EntityType.Builder.of(ZhelvaEntity::new, MobCategory.MONSTER)
                .sized(5.1F, 3.75F).clientTrackingRange(32).updateInterval(2)
                .build("slavicmyths:zhelva"));

    public static final DeferredHolder<Item, Item> EGG =
        ModItems.ITEMS.register("zhelva_spawn_egg", () ->
            new DeferredSpawnEggItem(BOSS, 0x344A42, 0xAFB591, new Item.Properties()) {
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "item.slavicmyths.zhelva_spawn_egg", "Яйцо призыва Желвы Каменного Дна");
                }
            });

    public static final DeferredHolder<Block, Block> HEAD =
        ModBlocks.BLOCKS.register("zhelva_head", ZhelvaTrophyBlock::new);
    public static final DeferredHolder<Item, Item> HEAD_ITEM =
        ModItems.ITEMS.register("zhelva_head", () ->
            new BlockItem(HEAD.get(), new Item.Properties().rarity(Rarity.EPIC)) {
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "block.slavicmyths.zhelva_head", "Голова Желвы Каменного Дна");
                }
            });
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ZhelvaTrophyBlock.Tile>>
        HEAD_TILE = ModTiles.TILES.register("zhelva_head", () ->
            BlockEntityType.Builder.of(ZhelvaTrophyBlock.Tile::new, HEAD.get()).build(null));

    public static final DeferredHolder<Item, Item> STONE_CHARM =
        ModItems.ITEMS.register("zhelva_stone_charm", () ->
            new ZhelvaStoneCharm(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static void init(IEventBus bus) {
        bus.addListener(ZhelvaContent::attributes);
        bus.addListener(ZhelvaContent::placements);
        bus.addListener(ZhelvaContent::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ZhelvaContent::drops);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(BOSS.get(), ZhelvaEntity.attributes().build());
    }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) event.accept(EGG.get());
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(HEAD_ITEM.get());
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) event.accept(STONE_CHARM.get());
    }
    private static void placements(RegisterSpawnPlacementsEvent event) {
        event.register(BOSS.get(), SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, world, reason, pos, random) -> {
                Level level = world.getLevel();
                if (level.dimension() != Level.OVERWORLD ||
                    level.getDifficulty() == Difficulty.PEACEFUL ||
                    !world.getBiome(pos).is(BiomeTags.IS_OCEAN)) return false;
                // Require a large, fully flooded deep seabed with room for the shell and fins.
                if (pos.getY() > level.getSeaLevel() - 13 ||
                    !world.getBlockState(pos.below()).isSolidRender(world, pos.below())) return false;
                for (int x = -3; x <= 3; x += 3)
                    for (int z = -3; z <= 3; z += 3)
                        for (int y = 0; y <= 4; y += 2) {
                            BlockPos p = pos.offset(x, y, z);
                            if (!level.hasChunkAt(p) ||
                                !world.getFluidState(p).is(FluidTags.WATER) ||
                                !world.getBlockState(p).getCollisionShape(world, p).isEmpty())
                                return false;
                        }
                if (reason == MobSpawnType.NATURAL) {
                    if (random.nextInt(24) != 0) return false;
                    if (!level.getEntitiesOfClass(ZhelvaEntity.class,
                        new AABB(pos).inflate(192)).isEmpty()) return false;
                }
                return true;
            }, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ZhelvaEntity boss)
            || boss.level().isClientSide || boss.isAlive()) return;
        Item head = HEAD_ITEM.get(), charm = STONE_CHARM.get();
        event.getDrops().removeIf(drop -> drop.getItem().is(head) || drop.getItem().is(charm));
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
            boss.level(), boss.getX(), boss.getY() + 1, boss.getZ(), new ItemStack(head)));
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
            boss.level(), boss.getX(), boss.getY() + 1, boss.getZ(), new ItemStack(charm)));
    }
    private ZhelvaContent() {}
}
