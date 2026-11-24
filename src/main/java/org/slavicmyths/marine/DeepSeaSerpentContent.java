package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.BiomeTags;
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
import org.slavicmyths.registry.*;
import net.minecraft.world.phys.AABB;

/** Isolated marine boss. No existing serpent entities or IDs are modified. */
public final class DeepSeaSerpentContent {
    public static final DeferredHolder<EntityType<?>, EntityType<DeepSeaSerpentEntity>> BOSS =
        ModEntities.ENTITIES.register("deep_sea_serpent", () ->
            EntityType.Builder.of(DeepSeaSerpentEntity::new, MobCategory.MONSTER)
                .sized(3.0F, 3.4F).clientTrackingRange(48).updateInterval(1)
                .build("slavicmyths:deep_sea_serpent"));

    public static final DeferredHolder<Item, Item> EGG = ModItems.ITEMS.register(
        "deep_sea_serpent_spawn_egg", () ->
            new DeferredSpawnEggItem(BOSS, 0x102E3B, 0x61C8B3, new Item.Properties()) {
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "item.slavicmyths.deep_sea_serpent_spawn_egg",
                        "Яйцо призыва Морского змея");
                }
            });

    public static final DeferredHolder<Block, Block> HEAD =
        ModBlocks.BLOCKS.register("deep_sea_serpent_head", () -> new DeepSeaSerpentTrophyBlock(true));
    public static final DeferredHolder<Block, Block> CREST =
        ModBlocks.BLOCKS.register("abyssal_crest", () -> new DeepSeaSerpentTrophyBlock(false));

    public static final DeferredHolder<Item, Item> HEAD_ITEM = ModItems.ITEMS.register(
        "deep_sea_serpent_head", () ->
            new BlockItem(HEAD.get(), new Item.Properties().rarity(Rarity.EPIC)) {
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "block.slavicmyths.deep_sea_serpent_head","Голова Морского змея");
                }
            });
    public static final DeferredHolder<Item, Item> CREST_ITEM = ModItems.ITEMS.register(
        "abyssal_crest", () ->
            new BlockItem(CREST.get(), new Item.Properties().rarity(Rarity.RARE)) {
                @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
                    return net.minecraft.network.chat.Component.translatableWithFallback(
                        "block.slavicmyths.abyssal_crest","Гребень Бездны");
                }
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepSeaSerpentTrophyBlock.Tile>>
        TROPHY_TILE = ModTiles.TILES.register("deep_sea_serpent_trophy",
            () -> BlockEntityType.Builder.of(DeepSeaSerpentTrophyBlock.Tile::new,
                HEAD.get(), CREST.get()).build(null));

    public static void init(IEventBus bus) {
        bus.addListener(DeepSeaSerpentContent::attributes);
        bus.addListener(DeepSeaSerpentContent::placements);
        bus.addListener(DeepSeaSerpentContent::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(DeepSeaSerpentContent::drops);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        event.put(BOSS.get(), DeepSeaSerpentEntity.attributes().build());
    }
    private static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) event.accept(EGG.get());
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {

            event.accept(CREST_ITEM.get());
        }
    }
    private static void placements(RegisterSpawnPlacementsEvent event) {
        event.register(BOSS.get(), SpawnPlacementTypes.IN_WATER,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (type, world, reason, pos, random) -> {
                Level level = world.getLevel();
                if (level.dimension() != Level.OVERWORLD || level.getDifficulty() == Difficulty.PEACEFUL)
                    return false;
                // Deep sea needs 9 blocks; a river needs a 5-block wide, 4-deep pool.
                boolean river = world.getBiome(pos).is(BiomeTags.IS_RIVER);
                int clearance = river ? 2 : 4;
                int depth = river ? 4 : 9;
                for (int x = -clearance; x <= clearance; x += 2)
                    for (int z = -clearance; z <= clearance; z += 2)
                        for (int y = -depth; y <= 2; y += 2) {
                            BlockPos p = pos.offset(x,y,z);
                            if (!world.getFluidState(p).is(FluidTags.WATER)
                                || !world.getBlockState(p).getCollisionShape(world,p).isEmpty())
                                return false;
                        }
                if (reason == MobSpawnType.NATURAL) {
                    if (random.nextInt(river ? 16 : 24) != 0) return false;
                    if (!level.getEntitiesOfClass(DeepSeaSerpentEntity.class,
                        new AABB(pos).inflate(192)).isEmpty()) return false;
                }
                return true;
            }, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof DeepSeaSerpentEntity boss)
            || boss.level().isClientSide || boss.isAlive()) return;
        // Exactly one head on each genuine death. Never duplicate a loot-table head.
        Item head = HEAD_ITEM.get();
        event.getDrops().removeIf(entry -> entry.getItem().is(head));
        event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
            boss.level(),boss.getX(),boss.getY()+1,boss.getZ(),new ItemStack(head)));
        // Crest is a separate, optional secondary trophy; not a replacement for the head.
        if (boss.getRandom().nextInt(4) == 0) {
            Item crest = CREST_ITEM.get();
            event.getDrops().removeIf(entry -> entry.getItem().is(crest));
            event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                boss.level(),boss.getX(),boss.getY()+1,boss.getZ(),new ItemStack(crest)));
        }
    }
    private DeepSeaSerpentContent() {}
}
