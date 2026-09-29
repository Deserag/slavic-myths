package org.slavicmyths.smoke;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slavicmyths.entity.DomovoyEntity;
import org.slavicmyths.entity.LeshyEntity;
import org.slavicmyths.registry.ModEntities;

/** Opt-in local integration test, excluded from the production JAR. */
@Mod("slavicmyths_smoke")
public final class SmokeProbe {
    private static final Logger LOG = LogManager.getLogger();
    private boolean loaded;
    private volatile boolean sceneReady;
    private int frames;
    private int debugFrames;
    public SmokeProbe() { MinecraftForge.EVENT_BUS.register(this); LOG.info("SMOKE: probe registered"); }
    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException("SMOKE FAIL: " + message);
        LOG.info("SMOKE PASS: {}", message);
    }
    @SubscribeEvent public void client(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (!loaded && mc.screen instanceof MainMenuScreen) {
            loaded = true;
            LOG.info("SMOKE PASS: main menu reached, JEI={}", ModList.get().isLoaded("jei"));
            mc.loadLevel("SlavicMyths-Smoke-040");
        }
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        ServerWorld world = player.getLevel();
        for (String id : new String[]{"story/root", "adventure/root", "husbandry/root", "nether/root", "end/root"}) {
            require(world.getServer().getAdvancements().getAdvancement(new ResourceLocation("minecraft",id)) != null, "vanilla advancement " + id);
        }
        for (String id : new String[]{"birch_bark","birch_bark_scroll","linen_thread","warding_charm","thunder_stone"}) {
            require(world.getServer().getRecipeManager().byKey(new ResourceLocation("slavicmyths",id)).isPresent(), "recipe " + id);
        }
        for (int x=-1;x<=3;x++) for (int z=-1;z<=1;z++) world.getChunk(x,z);
        for (DomovoyEntity old : world.getEntitiesOfClass(DomovoyEntity.class, new net.minecraft.util.math.AxisAlignedBB(-10,145,-10,10,160,10))) {
            if (old.hasRestriction()) require(old.reputation(player)==80,"bound Domovoy trust survived real restart");
            old.remove();
        }
        for (LeshyEntity old : world.getEntitiesOfClass(LeshyEntity.class, new net.minecraft.util.math.AxisAlignedBB(-10,145,-10,10,160,10))) old.remove();
        world.getServer().getCommands().performCommand(world.getServer().createCommandSourceStack(), "fill -10 150 -10 10 150 10 minecraft:grass_block");
        world.getServer().getCommands().performCommand(world.getServer().createCommandSourceStack(), "fill -10 151 -10 10 158 10 minecraft:air");
        world.setDayTime(6000);
        player.setGameMode(GameType.CREATIVE);
        player.inventory.clearContent();
        DomovoyEntity dom = ModEntities.DOMOVOY.get().create(world);
        LeshyEntity leshy = ModEntities.LESHY.get().create(world);
        require(dom != null && leshy != null, "entity factories");
        dom.moveTo(-2,151,0,0,0); leshy.moveTo(2,151,0,0,0);
        require(world.addFreshEntity(dom) && world.addFreshEntity(leshy), "spawn both spirits");
        require(dom.getMaxHealth() == 12 && leshy.getMaxHealth() == 32, "registered attributes");
        player.teleportTo(world,-2,151,1.5,180,0);
        player.connection.handleInteract(new CUseEntityPacket(dom, Hand.MAIN_HAND, false));
        player.teleportTo(world,2,151,1.5,180,0);
        player.connection.handleInteract(new CUseEntityPacket(leshy, Hand.MAIN_HAND, false));
        for (String id : new String[]{"meet_domovoy","meet_leshy"}) {
            require(player.getAdvancements().getOrStartProgress(world.getServer().getAdvancements()
                    .getAdvancement(new ResourceLocation("slavicmyths",id))).isDone(), "actual interaction advancement " + id);
        }
        require(dom.getTarget() == null && leshy.getTarget() == null, "neutral initial targets");
        milestone(player, world, dom);
        dom.playAmbientSound();leshy.playAmbientSound();
        player.teleportTo(world,0,151,7,180,0);
        sceneReady = true;
        LOG.info("SMOKE PASS: scene ready; production mod has no test hooks");
    }
    private void milestone(ServerPlayerEntity player, ServerWorld world, DomovoyEntity dom) {
        net.minecraft.util.math.BlockPos altarPos = new net.minecraft.util.math.BlockPos(0,151,0);
        // On the second real launch verify the prior run's saved altar before replacing it.
        net.minecraft.tileentity.TileEntity prior = world.getBlockEntity(new net.minecraft.util.math.BlockPos(12,151,0));
        if (prior instanceof org.slavicmyths.ritual.AltarTileEntity)
            require(((org.slavicmyths.ritual.AltarTileEntity) prior).getStep() == 1, "altar persisted across real client restart");
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(Hand.MAIN_HAND, new net.minecraft.item.ItemStack(net.minecraft.item.Items.MILK_BUCKET));
        player.interactOn(dom, Hand.MAIN_HAND);
        require(dom.reputation(player) == 10, "offering adds reputation");
        require(player.inventory.countItem(net.minecraft.item.Items.BUCKET) == 1, "milk returns bucket");
        player.setItemInHand(Hand.MAIN_HAND, new net.minecraft.item.ItemStack(net.minecraft.item.Items.BREAD, 8));
        player.interactOn(dom, Hand.MAIN_HAND);
        require(dom.reputation(player) == 10 && player.getMainHandItem().getCount() == 8, "spam blocked without consumption");
        // Fast-forward only this test entity's stored gift deadline, without changing world time.
        for (int i = 0; i < 7; i++) {
            net.minecraft.nbt.CompoundNBT tag = new net.minecraft.nbt.CompoundNBT(); dom.addAdditionalSaveData(tag);
            tag.getList("Relations", 10).getCompound(0).putLong("NextGift", 0); dom.readAdditionalSaveData(tag);
            player.interactOn(dom, Hand.MAIN_HAND);
        }
        require(dom.reputation(player) == 80 && player.getMainHandItem().getCount() == 1, "seven further offerings consumed exactly once");
        net.minecraft.util.math.BlockPos hearth = new net.minecraft.util.math.BlockPos(-3,151,0);
        world.setBlockAndUpdate(hearth, org.slavicmyths.registry.ModBlocks.HEARTH.get().defaultBlockState());
        dom.bind(player, hearth);
        net.minecraft.nbt.CompoundNBT saved = new net.minecraft.nbt.CompoundNBT(); dom.addAdditionalSaveData(saved);
        DomovoyEntity restored = ModEntities.DOMOVOY.get().create(world); restored.readAdditionalSaveData(saved);
        require(restored.reputation(player) == 80 && restored.hasRestriction() && !restored.removeWhenFarAway(99999), "entity NBT preserves trust, home and persistence");
        restored.setHealth(12); restored.hurt(net.minecraft.util.DamageSource.playerAttack(player),1);
        require(restored.reputation(player)==65,"attack reduces reputation");
        restored.invulnerableTime=0; restored.hurt(net.minecraft.util.DamageSource.playerAttack(player),1);
        require(restored.reputation(player)==45,"repeat attack causes larger penalty");
        net.minecraft.nbt.CompoundNBT hostile = new net.minecraft.nbt.CompoundNBT(); restored.addAdditionalSaveData(hostile);
        hostile.getList("Relations",10).getCompound(0).putInt("Reputation",-50);
        hostile.getList("Relations",10).getCompound(0).putLong("NextGift",0); restored.readAdditionalSaveData(hostile);
        player.setItemInHand(Hand.MAIN_HAND,new net.minecraft.item.ItemStack(net.minecraft.item.Items.BREAD,2));
        player.interactOn(restored,Hand.MAIN_HAND);
        require(restored.reputation(player)==-50 && player.getMainHandItem().getCount()==2,"hostile spirit refuses without consuming gift");
        restored.readAdditionalSaveData(saved);
        net.minecraft.nbt.CompoundNBT creative = new net.minecraft.nbt.CompoundNBT(); restored.addAdditionalSaveData(creative);
        creative.getList("Relations",10).getCompound(0).putLong("NextGift",0); restored.readAdditionalSaveData(creative);
        player.setGameMode(GameType.CREATIVE);
        player.setItemInHand(Hand.MAIN_HAND,new net.minecraft.item.ItemStack(net.minecraft.item.Items.HONEY_BOTTLE));
        player.interactOn(restored,Hand.MAIN_HAND);
        require(player.getMainHandItem().getCount()==1 && restored.reputation(player)==90,"creative offering keeps item");
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(Hand.MAIN_HAND, net.minecraft.item.ItemStack.EMPTY); player.interactOn(dom, Hand.MAIN_HAND);
        require(player.hasEffect(net.minecraft.potion.Effects.REGENERATION), "home blessing");
        player.removeEffect(net.minecraft.potion.Effects.REGENERATION); player.interactOn(dom, Hand.MAIN_HAND);
        require(!player.hasEffect(net.minecraft.potion.Effects.REGENERATION), "blessing cooldown");
        world.setBlockAndUpdate(altarPos, org.slavicmyths.registry.ModBlocks.ALTAR.get().defaultBlockState());
        org.slavicmyths.ritual.AltarTileEntity altar = (org.slavicmyths.ritual.AltarTileEntity) world.getBlockEntity(altarPos);
        player.setItemInHand(Hand.MAIN_HAND, new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIRT,3));
        altar.offer(player, Hand.MAIN_HAND);
        require(altar.getStep()==0 && player.getMainHandItem().getCount()==3, "wrong ritual ingredient unchanged");
        for (int i=0;i<3;i++) {
            player.setItemInHand(Hand.MAIN_HAND, new net.minecraft.item.ItemStack(org.slavicmyths.ritual.FirstRitual.ingredient(i),2));
            altar.offer(player, Hand.MAIN_HAND);
            require(player.getMainHandItem().getCount()==1, "ritual consumes one ingredient at step "+i);
            if (i==0) {
                net.minecraft.nbt.CompoundNBT state = altar.save(new net.minecraft.nbt.CompoundNBT());
                org.slavicmyths.ritual.AltarTileEntity copy = new org.slavicmyths.ritual.AltarTileEntity();
                copy.load(altar.getBlockState(),state); require(copy.getStep()==1,"altar NBT roundtrip");
            }
        }
        require(altar.getStep()==0 && player.inventory.countItem(org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get())==1, "ritual grants unique output and resets");
        require(org.slavicmyths.progression.Knowledge.knows(player,"first_ritual"),"ritual knowledge persists as advancement");
        // Leave a partial altar outside the cleared display scene for the next launch.
        net.minecraft.util.math.BlockPos persistent = new net.minecraft.util.math.BlockPos(12,151,0);
        world.setBlockAndUpdate(persistent, net.minecraft.block.Blocks.AIR.defaultBlockState());
        world.setBlockAndUpdate(persistent, org.slavicmyths.registry.ModBlocks.ALTAR.get().defaultBlockState());
        player.setItemInHand(Hand.MAIN_HAND,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.WORMWOOD.get()));
        ((org.slavicmyths.ritual.AltarTileEntity)world.getBlockEntity(persistent)).offer(player,Hand.MAIN_HAND);
        player.setGameMode(GameType.CREATIVE);
        player.setItemInHand(Hand.MAIN_HAND,new net.minecraft.item.ItemStack(org.slavicmyths.registry.ModItems.LORE_BOOK.get()));
        world.setBlockAndUpdate(new net.minecraft.util.math.BlockPos(0,151,-2),org.slavicmyths.registry.ModBlocks.ANCIENT_IDOL.get().defaultBlockState());
        org.slavicmyths.progression.Knowledge.award(player,"shrine");
        for (String id : new String[]{"altar","hearth","ancient_idol"})
            require(world.getServer().getLootTables().get(new ResourceLocation("slavicmyths","blocks/"+id)) != net.minecraft.loot.LootTable.EMPTY,"block loot "+id);
        require(world.getServer().getLootTables().get(new ResourceLocation("slavicmyths","chests/ancient_shrine")) != net.minecraft.loot.LootTable.EMPTY,"shrine loot parses");
        world.getServer().getCommands().performCommand(world.getServer().createCommandSourceStack(),"fill 35 150 3 45 150 13 minecraft:grass_block");
        world.getServer().getCommands().performCommand(world.getServer().createCommandSourceStack(),"fill 35 151 3 45 160 13 minecraft:air");
        long start = System.nanoTime();
        net.minecraft.world.gen.Heightmap.primeHeightmaps(world.getChunk(2,0), java.util.EnumSet.of(net.minecraft.world.gen.Heightmap.Type.WORLD_SURFACE_WG));
        boolean generated = org.slavicmyths.registry.ModFeatures.SHRINE.get().place(world, world.getChunkSource().getGenerator(),
                new java.util.Random(40),new net.minecraft.util.math.BlockPos(40,151,8),net.minecraft.world.gen.feature.NoFeatureConfig.INSTANCE);
        require(generated, "shrine feature placement on valid ground");
        LOG.info("SMOKE PERF: single shrine placement {} ms (controlled footprint, not a world benchmark)",(System.nanoTime()-start)/1000000.0);
        require(world.getBlockState(new net.minecraft.util.math.BlockPos(40,151,8)).is(org.slavicmyths.registry.ModBlocks.ANCIENT_IDOL.get()),"shrine central idol");
        require(world.getBlockEntity(new net.minecraft.util.math.BlockPos(40,151,10)) instanceof org.slavicmyths.ritual.AltarTileEntity,"generated altar tile");
        require(world.getBlockEntity(new net.minecraft.util.math.BlockPos(42,151,6)) instanceof net.minecraft.tileentity.ChestTileEntity,"generated loot chest");
        require(!org.slavicmyths.registry.ModFeatures.SHRINE.get().place(world,world.getChunkSource().getGenerator(),new java.util.Random(40),
                new net.minecraft.util.math.BlockPos(40,151,8),net.minecraft.world.gen.feature.NoFeatureConfig.INSTANCE),"shrine refuses occupied footprint");
        LeshyEntity fighter = ModEntities.LESHY.get().create(world);
        net.minecraft.entity.monster.ZombieEntity dummy = net.minecraft.entity.EntityType.ZOMBIE.create(world);
        fighter.getRandom().setSeed(123);
        for (int i=0;i<20 && !dummy.hasEffect(net.minecraft.potion.Effects.MOVEMENT_SLOWDOWN);i++) {
            dummy.setHealth(dummy.getMaxHealth()); dummy.invulnerableTime=0; fighter.doHurtTarget(dummy);
        }
        require(dummy.hasEffect(net.minecraft.potion.Effects.MOVEMENT_SLOWDOWN),"Leshy forest attack applies slowness");
        fighter.moveTo(0,151,4,0,0); fighter.getRandom().setSeed(123);
        fighter.hurt(net.minecraft.util.DamageSource.GENERIC,22);
        net.minecraft.nbt.CompoundNBT escape = new net.minecraft.nbt.CompoundNBT(); fighter.addAdditionalSaveData(escape);
        require(escape.getLong("NextEscape") == world.getGameTime()+600,"Leshy escape cooldown saved");
        fighter.invulnerableTime=0; fighter.hurt(net.minecraft.util.DamageSource.GENERIC,1);
        net.minecraft.nbt.CompoundNBT second = new net.minecraft.nbt.CompoundNBT(); fighter.addAdditionalSaveData(second);
        require(second.getLong("NextEscape")==escape.getLong("NextEscape"),"Leshy escape cannot repeat during cooldown");
    }
    @SubscribeEvent public void render(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ++debugFrames == 120) {
            Minecraft mc = Minecraft.getInstance();
            LOG.info("SMOKE: initial screen {}", mc.screen);
            try (NativeImage picture = ScreenShotHelper.takeScreenshot(mc.getWindow().getWidth(),mc.getWindow().getHeight(),mc.getMainRenderTarget())) {
                picture.writeToFile(new File("../docs/verification/startup-0.4.png"));
            } catch (Exception ex) { LOG.error("SMOKE startup capture failed",ex); }
        }
        if (event.phase != TickEvent.Phase.END || !sceneReady) return;
        frames++;
        Minecraft mc = Minecraft.getInstance();
        if (frames == 180) {
            mc.getSingleplayerServer().execute(() -> {
                ServerPlayerEntity player = mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                org.slavicmyths.registry.ModItems.LORE_BOOK.get().use(player.level,player,Hand.MAIN_HAND);
            });
        }
        if (frames == 320) {
            mc.setScreen(null);
            mc.getSingleplayerServer().execute(() -> {
                ServerPlayerEntity player=mc.getSingleplayerServer().getPlayerList().getPlayers().get(0);
                player.abilities.flying=true; player.onUpdateAbilities();
                player.teleportTo(worldFor(mc),40,157,18,180,35);
            });
        }
        if (frames == 460 && ModList.get().isLoaded("jei")) showJei();
        if (frames == 600) { sceneReady = false; mc.level.disconnect(); mc.clearLevel(); mc.stop(); return; }
        if (frames != 120 && frames != 300 && frames != 440 && frames != 560) return;
        try (NativeImage picture = ScreenShotHelper.takeScreenshot(mc.getWindow().getWidth(),mc.getWindow().getHeight(),mc.getMainRenderTarget())) {
            File output = new File("../docs/verification/" + (frames==120 ? "spirits-0.4-ingame.png" : frames==300 ? "book-0.4.png" : frames==440 ? "shrine-0.4.png" : "jei-0.4.png"));
            picture.writeToFile(output);
            LOG.info("SMOKE PASS: captured in-game frame {}",output.getAbsolutePath());
        } catch (Exception ex) { LOG.error("SMOKE screenshot failed",ex); }
        if (frames==300) require(mc.screen instanceof org.slavicmyths.client.LoreScreen,"server book packet opens client GUI");
    }
    private static ServerWorld worldFor(Minecraft mc) { return mc.getSingleplayerServer().overworld(); }
    private static void showJei() {
        mezz.jei.api.runtime.IJeiRuntime runtime = mezz.jei.Internal.getRuntime();
        require(runtime.getRecipeManager().getRecipeCategory(new ResourceLocation("slavicmyths","rituals"),false)!=null,"optional JEI ritual category registered");
        runtime.getRecipesGui().showCategories(java.util.Collections.singletonList(new ResourceLocation("slavicmyths","rituals")));
    }
}
