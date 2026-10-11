package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.armortrim.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.equipment.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.item.ItemState;
import org.slavicmyths.armorer.*;
import net.neoforged.neoforge.event.entity.living.*;

@GameTestHolder("slavicmyths_unique") @PrefixGameTestTemplate(false)
public final class UniqueEquipmentGameTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { }
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) { }
    }
    private static ServerPlayer player(GameTestHelper t){var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ArmorProbe"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,2,3)));p.setNoGravity(true);for(int i=0;i<61;i++)p.tick();return p;}
    private static void equip(ServerPlayer p,EquipmentSlot slot,Item item){p.setItemSlot(slot,new ItemStack(item));p.doTick();}
    private static List<Item> equipment(){return List.of(UniqueEquipment.WIND_BOOTS.get(),UniqueEquipment.BOGATYR_CHESTPLATE.get(),UniqueEquipment.CLEAR_SIGHT_HELMET.get(),UniqueEquipment.INDOMITABILITY_LEGGINGS.get(),UniqueEquipment.WARD_SHIELD.get());}
    private static ItemStack marked(GameTestHelper t,Item item){var s=new ItemStack(item);ItemState.runes(s,1,List.of("heat"));s.enchant(t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),2);s.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Donor"));s.set(DataComponents.REPAIR_COST,7);s.setDamageValue(s.getMaxDamage()/2);var tag=new net.minecraft.nbt.CompoundTag();tag.putString("arbitrary","kept");s.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));if(item instanceof ArmorItem)s.set(DataComponents.TRIM,new ArmorTrim(t.getLevel().registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.IRON),t.getLevel().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.SENTRY)));return s;}
    @GameTest(template="empty") public static void upgradesPreserveComponentsAndRejectWrongBases(GameTestHelper t){
        int recipes=0;
        for(var holder:t.getLevel().getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE.get())){
            if(!holder.id().getPath().startsWith("unique/"))continue;recipes++;var recipe=holder.value();var grid=new ArrayList<ItemStack>();for(var ingredient:recipe.getIngredients())grid.add(ingredient.getItems()[0].copy());while(grid.size()<16)grid.add(ItemStack.EMPTY);
            var donor=marked(t,grid.getFirst().getItem());grid.set(0,donor);var input=new ArmorerInput(4,4,grid);t.assertTrue(recipe.matches(input,t.getLevel()),"correct base rejected "+holder.id());var out=recipe.assemble(input,t.getLevel().registryAccess());
            for(var type:List.of(DataComponents.ENCHANTMENTS,DataComponents.CUSTOM_NAME,DataComponents.REPAIR_COST,DataComponents.CUSTOM_DATA,DataComponents.TRIM,ItemState.RUNES.get()))t.assertTrue(Objects.equals(donor.get(type),out.get(type)),"lost component "+type+" "+holder.id());
            t.assertTrue((double)out.getDamageValue()/out.getMaxDamage()+1e-8>=(double)donor.getDamageValue()/donor.getMaxDamage(),"free repair "+holder.id());
            t.assertTrue(Runes.capacity(out)==3&&Runes.slots(out)==1&&Runes.list(out).equals(List.of("heat")),"rune transfer/capacity");
            var expectedBase=switch(holder.id().getPath().substring(7)){case "wind_boots"->ModItems.SEVEN_LEAGUE_BOOTS.get();case "bogatyr_chestplate"->Items.IRON_CHESTPLATE;case "clear_sight_helmet"->Items.IRON_HELMET;case "indomitability_leggings"->Items.IRON_LEGGINGS;default->Items.SHIELD;};t.assertTrue(donor.is(expectedBase),"wrong recipe base");
            grid.set(0,new ItemStack(Items.DIAMOND_CHESTPLATE));t.assertTrue(!recipe.matches(new ArmorerInput(4,4,grid),t.getLevel()),"wrong base accepted");
            grid.set(0,donor.copy());grid.set(1,ItemStack.EMPTY);t.assertTrue(!recipe.matches(new ArmorerInput(4,4,grid),t.getLevel()),"missing material accepted");
        }t.assertTrue(recipes==5,"missing upgrades "+recipes);t.succeed();
    }
    @GameTest(template="empty") public static void registryCreativeAndStackCodec(GameTestHelper t){for(var item:equipment()){var stack=new ItemStack(item);t.assertTrue(Runes.capacity(stack)==3,"capacity");t.assertTrue(ModItemGroup.items(ModItemGroup.category(item)).contains(item),"creative");var s=marked(t,item);var restored=ItemStack.parseOptional(t.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)s.save(t.getLevel().registryAccess()));t.assertTrue(ItemStack.matches(s,restored),"codec");}t.assertTrue(ModItemGroup.items(1).contains(UniqueEquipment.HARDENED_IRON.get()),"material creative");t.succeed();}
    @GameTest(template="empty") public static void bootsNativeAttributesJumpFallAndNoFlight(GameTestHelper t){var p=player(t);double base=p.getAttributeValue(Attributes.MOVEMENT_SPEED),jump=p.getAttributeValue(Attributes.JUMP_STRENGTH);equip(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());t.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)/base-1.4)<.0001,"speed");t.assertTrue(Math.abs(p.getAttributeValue(Attributes.JUMP_STRENGTH)-.82)<.0001,"jump");p.setSprinting(true);p.setDeltaMovement(.3,0,.2);p.jumpFromGround();t.assertTrue(p.getDeltaMovement().x>.32&&Math.abs(p.getDeltaMovement().y-.82)<.0001,"running leap");var fall=new LivingFallEvent(p,12,1);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(fall);t.assertTrue(!fall.isCanceled()&&Math.abs(fall.getDamageMultiplier()-.6)<.0001,"fall reduction/immunity");t.assertTrue(!p.getAbilities().mayfly&&!p.getAbilities().flying,"flight enabled");equip(p,EquipmentSlot.FEET,Items.AIR);p.setSprinting(false);t.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)/base-1)<.0001,"speed survived unequip");t.succeed();}
    @GameTest(template="empty") public static void chestHealthThresholdAndIdempotentModifiers(GameTestHelper t){var p=player(t);equip(p,EquipmentSlot.CHEST,UniqueEquipment.BOGATYR_CHESTPLATE.get());t.assertTrue(p.getArmorValue()==8&&p.getAttributeValue(Attributes.ARMOR_TOUGHNESS)==3,"armor stats");p.setHealth(7);t.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.35)<.0001,"low threshold hook");for(int i=0;i<100;i++)UniqueEquipmentEvents.healthChanged(p);t.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.35)<.0001,"stacking");p.setHealth(7.01F);t.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.2)<.0001,"not removed immediately");p.setHealth(7);equip(p,EquipmentSlot.CHEST,Items.AIR);t.assertTrue(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)==0,"unequip modifier");t.succeed();}
    @GameTest(template="empty") public static void chestActualDamageReduction(GameTestHelper t){var a=player(t);var b=player(t);equip(a,EquipmentSlot.CHEST,UniqueEquipment.BOGATYR_CHESTPLATE.get());equip(b,EquipmentSlot.CHEST,UniqueEquipment.BOGATYR_CHESTPLATE.get());a.setHealth(7);b.setHealth(10);a.hurt(a.damageSources().generic(),4);b.hurt(b.damageSources().generic(),4);t.assertTrue(Math.abs((7-a.getHealth())/(10-b.getHealth())-.85)<.001,"15% actual reduction");t.succeed();}
    @GameTest(template="empty") public static void controlNativeEffectsAndUntaggedEffects(GameTestHelper t){var p=player(t);equip(p,EquipmentSlot.LEGS,UniqueEquipment.INDOMITABILITY_LEGGINGS.get());for(var effect:List.of(MobEffects.MOVEMENT_SLOWDOWN,ModEffects.CLASS_ROOT,ModEffects.DISORIENTATION)){p.addEffect(new MobEffectInstance(effect,100,0));t.assertTrue(p.hasEffect(effect)&&p.getEffect(effect).getDuration()==65,"control duration "+effect);p.removeEffect(effect);}p.addEffect(new MobEffectInstance(MobEffects.POISON,100,0));t.assertTrue(p.getEffect(MobEffects.POISON).getDuration()==100,"poison altered");t.succeed();}
    @GameTest(template="empty",timeoutTicks=150) public static void leggingsCooldownAndExpiration(GameTestHelper t){var p=player(t);equip(p,EquipmentSlot.LEGS,UniqueEquipment.INDOMITABILITY_LEGGINGS.get());p.knockback(.1,1,0);t.assertTrue(!p.getPersistentData().contains("UniqueKbUntil"),"weak knockback proc");p.knockback(.5,1,0);long until=p.getPersistentData().getLong("UniqueKbUntil"),cd=UniqueEquipmentEvents.saved(p).getLong("KbReady");t.assertTrue(until==UniqueEquipmentEvents.now(p)+100&&cd==UniqueEquipmentEvents.now(p)+300,"timings");t.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.45)<.0001,"temporary KB");p.knockback(.7,1,0);t.assertTrue(p.getPersistentData().getLong("UniqueKbUntil")==until,"refresh bypass");t.runAfterDelay(101,()->{UniqueEquipmentEvents.timer(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));t.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.1)<.0001,"KB did not expire");p.knockback(.7,1,0);t.assertTrue(!p.getPersistentData().contains("UniqueKbUntil"),"cooldown bypass");t.succeed();});}
    @GameTest(template="empty") public static void sightNativeIntentCooldownAndExclusions(GameTestHelper t) throws Exception {var p=player(t);ClearSightNetwork.activate(p);t.assertTrue(UniqueEquipmentEvents.saved(p).getLong("SightReady")==0,"no-helmet activation");equip(p,EquipmentSlot.HEAD,UniqueEquipment.CLEAR_SIGHT_HELMET.get());ClearSightNetwork.activate(p);long ready=UniqueEquipmentEvents.saved(p).getLong("SightReady");t.assertTrue(ready==UniqueEquipmentEvents.now(p)+800,"cooldown");equip(p,EquipmentSlot.HEAD,Items.AIR);equip(p,EquipmentSlot.HEAD,UniqueEquipment.CLEAR_SIGHT_HELMET.get());ClearSightNetwork.activate(p);t.assertTrue(UniqueEquipmentEvents.saved(p).getLong("SightReady")==ready,"equip reset");for(var type:List.of(EntityType.COW,EntityType.ZOMBIE,EntityType.WOLF,EntityType.VILLAGER))t.assertTrue(ClearSightNetwork.eligible(type.create(t.getLevel()),p),"living excluded");t.assertTrue(!ClearSightNetwork.eligible(EntityType.ARMOR_STAND.create(t.getLevel()),p),"armor stand included");var copy=UniqueEquipmentEvents.saved(p).copy();var bytes=new java.io.ByteArrayOutputStream();net.minecraft.nbt.NbtIo.write(copy,new java.io.DataOutputStream(bytes));var restored=net.minecraft.nbt.NbtIo.read(new java.io.DataInputStream(new java.io.ByteArrayInputStream(bytes.toByteArray())),net.minecraft.nbt.NbtAccounter.unlimitedHeap());t.assertTrue(restored.getLong("SightReady")==ready,"cooldown NBT");t.succeed();}
    @GameTest(template="empty") public static void shieldDeterministicEdges(GameTestHelper t){for(int damage=1;damage<=1000;damage++){t.assertTrue(UniqueEquipmentRules.reflected(true,damage,true,false,.249F)==damage*1.25F,"reflection");t.assertTrue(UniqueEquipmentRules.reflected(false,damage,true,false,0)==0,"nonblock");t.assertTrue(UniqueEquipmentRules.reflected(true,damage,false,false,0)==0,"environment");t.assertTrue(UniqueEquipmentRules.reflected(true,damage,true,true,0)==0,"recursion");t.assertTrue(UniqueEquipmentRules.reflected(true,damage,true,false,.25F)==0,"chance boundary");}t.assertTrue(UniqueEquipmentRules.reflected(true,0,true,false,0)==0,"zero absorption");t.succeed();}
    @GameTest(template="empty") public static void shieldActualBlocksAndReflection(GameTestHelper t){var p=player(t);equip(p,EquipmentSlot.OFFHAND,UniqueEquipment.WARD_SHIELD.get());p.setYRot(0);p.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);for(int i=0;i<6;i++)p.doTick();t.assertTrue(p.isBlocking(),"fixture shield startup "+p.getTicksUsingItem());var mob=EntityType.COW.create(t.getLevel());mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.moveTo(p.position().add(0,0,2));t.getLevel().addFreshEntity(mob);int procs=0;for(int i=0;i<200;i++){p.invulnerableTime=0;mob.invulnerableTime=0;p.getUseItem().setDamageValue(0);float before=mob.getHealth();p.hurt(p.damageSources().mobAttack(mob),4);if(mob.getHealth()<before){procs++;t.assertTrue(Math.abs(before-mob.getHealth()-5)<.001,"wrong reflected damage");}t.assertTrue(p.getHealth()==20,"block failed i="+i+" hp="+p.getHealth()+" use="+p.getUseItem()+" blocking="+p.isBlocking());}t.assertTrue(procs>20&&procs<90,"proc distribution "+procs);float health=mob.getHealth();p.invulnerableTime=0;mob.invulnerableTime=0;p.hurt(org.slavicmyths.combat.MythDamageSources.caused("ward_reflection",mob),4);t.assertTrue(mob.getHealth()==health,"reflection loop");t.succeed();}
    @GameTest(template="empty") public static void hardenedIronNativeLoot(GameTestHelper t){for(var type:List.of(ModEntities.VOEVODA.get(),ModEntities.VOLKHV.get(),ModEntities.PRINCE.get())){var entity=(LivingEntity)type.create(t.getLevel());var table=t.getLevel().getServer().reloadableRegistries().getLootTable(entity.getLootTable());var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(t.getLevel()).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,entity).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,entity.position()).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,entity.damageSources().generic()).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);var drops=table.getRandomItems(params);int count=drops.stream().filter(s->s.is(UniqueEquipment.HARDENED_IRON.get())).mapToInt(ItemStack::getCount).sum();t.assertTrue(count==(type==ModEntities.PRINCE.get()?2:1),"iron guarantee "+type);}t.succeed();}
    @GameTest(template="empty") public static void nativeWorkbenchConsumption(GameTestHelper t){
        var p=player(t);t.setBlock(new net.minecraft.core.BlockPos(3,2,3),ModBlocks.ARMORER_TABLE.get());int count=0;
        for(var holder:t.getLevel().getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE.get()))if(holder.id().getPath().startsWith("unique/")){
            var recipe=holder.value();var menu=new ArmorerMenu(10,p.getInventory(),t.absolutePos(new net.minecraft.core.BlockPos(3,2,3)));
            for(int i=0;i<recipe.getIngredients().size();i++)menu.input.setItem(i,recipe.getIngredients().get(i).getItems()[0].copy());
            menu.input.setItem(0,marked(t,menu.input.getItem(0).getItem()));menu.slotsChanged(menu.input);
            var expected=recipe.assemble(new ArmorerInput(4,4,menu.input.getItems()),t.getLevel().registryAccess());var taken=menu.quickMoveStack(p,0);
            t.assertTrue(ItemStack.matches(taken,expected),"native craft output");t.assertTrue(menu.input.getItems().stream().allMatch(ItemStack::isEmpty),"craft duplicated donors");t.assertTrue(menu.quickMoveStack(p,0).isEmpty(),"double craft");p.getInventory().clearContent();count++;
        }t.assertTrue(count==5,"native upgrades missing");t.succeed();
    }
    @GameTest(template="empty") public static void nativePlayerStorage(GameTestHelper t) throws Exception {
        boolean verify=System.getProperty("slavicmyths.uniqueRestartPhase").equals("verify");var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.fromString("8b7c7342-365e-4a99-bab2-000013332001"),"UniqueStorage"),ClientInformation.createDefault());p.connection=new Listener(p);var expected=java.nio.file.Path.of("unique-storage.nbt");
        if(!verify){var items=new net.minecraft.nbt.ListTag();for(var item:equipment()){var stack=marked(t,item);p.getInventory().add(stack.copy());items.add(stack.save(w.registryAccess()));}UniqueEquipmentEvents.saved(p).putLong("SightReady",UniqueEquipmentEvents.now(p)+800);UniqueEquipmentEvents.saved(p).putLong("KbReady",UniqueEquipmentEvents.now(p)+300);var data=new net.minecraft.nbt.CompoundTag();data.put("Items",items);data.put("Cooldowns",UniqueEquipmentEvents.saved(p).copy());net.minecraft.nbt.NbtIo.writeCompressed(data,expected);w.getServer().getPlayerList().remove(p);}
        else {t.assertTrue(w.getServer().getPlayerList().load(p).isPresent(),"player dat absent");var data=net.minecraft.nbt.NbtIo.readCompressed(expected,net.minecraft.nbt.NbtAccounter.unlimitedHeap());for(var tag:data.getList("Items",10)){var stack=ItemStack.parse(w.registryAccess(),tag).orElseThrow();t.assertTrue(p.getInventory().items.stream().anyMatch(x->ItemStack.matches(x,stack)),"stored equipment changed");}t.assertTrue(UniqueEquipmentEvents.saved(p).equals(data.getCompound("Cooldowns")),"stored cooldown lost");UniqueEquipmentEvents.login(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));t.assertTrue(UniqueEquipmentEvents.saved(p).equals(data.getCompound("Cooldowns")),"login resets cooldown");System.out.println("UNIQUE_NATIVE_PLAYER_STORAGE_NEW_PROCESS_PASS equipment=5 cooldowns=2");}
        t.succeed();
    }
    @GameTest(template="empty") public static void customDurabilityAndRepair(GameTestHelper t){
        for(var item:equipment()){var source=marked(t,item);source.set(DataComponents.MAX_DAMAGE,1000);source.setDamageValue(400);var anvil=new net.minecraft.world.inventory.AnvilMenu(1,player(t).getInventory());anvil.getSlot(0).set(source.copy());anvil.getSlot(1).set(new ItemStack(item==UniqueEquipment.WIND_BOOTS.get()?Items.LEATHER:UniqueEquipment.HARDENED_IRON.get()));var out=anvil.getSlot(2).getItem();t.assertTrue(!out.isEmpty()&&out.getDamageValue()<400,"repair unavailable");t.assertTrue(Objects.equals(out.get(DataComponents.TRIM),source.get(DataComponents.TRIM))&&Objects.equals(out.get(DataComponents.CUSTOM_DATA),source.get(DataComponents.CUSTOM_DATA))&&ItemState.runes(out).equals(ItemState.runes(source)),"repair state loss");}
        for(int max=1;max<=100;max++)for(int damage=1;damage<=max;damage++)t.assertTrue((double)UniqueEquipmentRules.upgradedDamage(damage,max,640)/640+1e-9>=(double)damage/max,"free repair boundary");t.succeed();
    }

    @GameTest(template="empty") public static void shieldProjectileOwnerSelfAndCanceledBlocks(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.OFFHAND,UniqueEquipment.WARD_SHIELD.get());p.setYRot(0);p.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);for(int i=0;i<6;i++)p.doTick();var mob=EntityType.COW.create(t.getLevel());mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.moveTo(p.position().add(0,0,2));t.getLevel().addFreshEntity(mob);var projectile=new net.minecraft.world.entity.projectile.Snowball(t.getLevel(),mob);projectile.setPos(mob.position());int procs=0;
        for(int i=0;i<100;i++){p.invulnerableTime=0;mob.invulnerableTime=0;p.getUseItem().setDamageValue(0);float hp=mob.getHealth();p.hurt(p.damageSources().thrown(projectile,mob),4);if(mob.getHealth()<hp){procs++;t.assertTrue(hp-mob.getHealth()==5,"projectile owner reflect amount");}}
        t.assertTrue(procs>5&&procs<50,"projectile owner not reflected");float hp=mob.getHealth();p.invulnerableTime=0;p.hurt(p.damageSources().thrown(projectile,null),4);t.assertTrue(mob.getHealth()==hp,"ownerless projectile reflected");
        java.util.function.Consumer<LivingShieldBlockEvent> cancel=e->{if(e.getEntity()==p)e.setBlocked(false);};net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,cancel);
        try{p.invulnerableTime=0;mob.invulnerableTime=0;p.hurt(p.damageSources().mobAttack(mob),4);t.assertTrue(mob.getHealth()==hp,"canceled block reflected");}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(cancel);}
        t.succeed();
    }
    @GameTest(template="empty") public static void cooldownSurvivesNativeDimensionAndClone(GameTestHelper t){
        var p=player(t);var root=UniqueEquipmentEvents.saved(p);root.putLong("SightReady",UniqueEquipmentEvents.now(p)+800);root.putLong("KbReady",UniqueEquipmentEvents.now(p)+300);var expected=root.copy();var world=t.getLevel();var origin=p.position();world.addNewPlayer(p);
        try{var nether=world.getServer().getLevel(net.minecraft.world.level.Level.NETHER);p.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(nether,new Vec3(0,80,0),Vec3.ZERO,0,0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));t.assertTrue(UniqueEquipmentEvents.saved(p).equals(expected),"nether cooldown");p.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(world,origin,Vec3.ZERO,0,0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));t.assertTrue(UniqueEquipmentEvents.saved(p).equals(expected),"dimension cooldown");p.hasChangedDimension();p.kill();t.assertTrue(!p.isAlive(),"native death");var next=player(t);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(next,p,true));t.assertTrue(UniqueEquipmentEvents.saved(next).equals(expected),"death clone cooldown");UniqueEquipmentEvents.respawn(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent(next,false));t.assertTrue(UniqueEquipmentEvents.saved(next).equals(expected),"respawn cleanup cooldown");}finally{world.getServer().getPlayerList().remove(p);}t.succeed();
    }

    @GameTest(template="empty") public static void environmentalOwnerNeverReflects(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.OFFHAND,UniqueEquipment.WARD_SHIELD.get());p.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);for(int i=0;i<6;i++)p.doTick();var owner=EntityType.COW.create(t.getLevel());owner.setHealth(10);var source=p.damageSources().explosion(owner,owner);var container=new net.neoforged.neoforge.common.damagesource.DamageContainer(source,4);var block=new LivingShieldBlockEvent(p,container,true);container.setBlockedDamage(block);
        for(int i=0;i<100;i++)UniqueEquipmentEvents.reflected(p,block);t.assertTrue(owner.getHealth()==10,"environmental explosion owner reflected");t.succeed();
    }

    @GameTest(template="empty") public static void windBootsNativeFourBlockArc(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());
        for(int x=2;x<=4;x++)for(int z=2;z<=15;z++){t.setBlock(new net.minecraft.core.BlockPos(x,20,z),net.minecraft.world.level.block.Blocks.STONE);for(int y=21;y<=29;y++)t.setBlock(new net.minecraft.core.BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);}
        p.moveTo(t.absoluteVec(new Vec3(3.5,21,3.5)));p.setNoGravity(false);p.setSprinting(true);p.setYRot(0);p.setXRot(0);p.setOnGround(true);
        double speed=p.getAttributeValue(Attributes.MOVEMENT_SPEED);double groundFriction=.6*.91;double groundVelocity=speed*.98*groundFriction/(1-groundFriction);
        p.setDeltaMovement(0,0,groundVelocity);double startY=p.getY(),startZ=p.getZ();p.jumpFromGround();double peak=0;int ticks=0;boolean descended=false;
        for(int i=0;i<40;i++){p.travel(new Vec3(0,0,.98));ticks++;peak=Math.max(peak,p.getY()-startY);if(p.getDeltaMovement().y<0)descended=true;if(p.onGround())break;}
        double distance=p.getZ()-startZ;t.assertTrue(peak>=4.1&&peak<=4.2,"jump apex "+peak);t.assertTrue(distance>=6.9&&distance<=7.3,"arc range "+distance);t.assertTrue(descended&&p.onGround()&&ticks>=20&&ticks<=22,"not a landing arc");t.assertTrue(!p.getAbilities().mayfly&&!p.getAbilities().flying,"flight enabled");
        System.out.println("WIND_BOOTS_NATIVE_ARC apex="+peak+" range="+distance+" ticks="+ticks);t.succeed();
    }

    @GameTest(template="empty") public static void windBootsNativeFourBlockLedge(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());
        for(int x=2;x<=4;x++)for(int z=2;z<=15;z++){for(int y=20;y<=29;y++)t.setBlock(new net.minecraft.core.BlockPos(x,y,z),y==20||(z>=7&&y<=24)?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR);}
        p.moveTo(t.absoluteVec(new Vec3(3.5,21,3.5)));p.setNoGravity(false);p.setSprinting(true);p.setYRot(0);p.setXRot(0);p.setOnGround(true);double speed=p.getAttributeValue(Attributes.MOVEMENT_SPEED);p.setDeltaMovement(0,0,speed*.98*.546/(1-.546));double startY=p.getY(),startZ=p.getZ();p.jumpFromGround();
        for(int i=0;i<30;i++){p.travel(new Vec3(0,0,.98));if(p.onGround())break;}
        t.assertTrue(p.onGround()&&Math.abs(p.getY()-startY-4)<.001,"cannot land on four-block ledge "+(p.getY()-startY));t.assertTrue(p.getZ()-startZ>3.5,"did not reach ledge");System.out.println("WIND_BOOTS_NATIVE_LEDGE height="+(p.getY()-startY)+" range="+(p.getZ()-startZ));t.succeed();
    }

    private static void windTick(ServerPlayer p){WindBootsMomentum.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));}
    // Synthetic received movement samples exercise the server charge gate; arcs below use native travel.
    private static void windCharge(ServerPlayer p,int ticks){
        p.setOnGround(true);p.setSprinting(true);windTick(p);if(WindBootsMomentum.charge(p)==0)windTick(p);
        for(int i=0;i<ticks;i++){p.setPos(p.getX()+(i%2==0?.2:-.2),p.getY(),p.getZ());windTick(p);}
    }
    @GameTest(template="empty") public static void windChargeDecayCollisionAndUnequip(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());windCharge(p,80);
        t.assertTrue(WindBootsMomentum.charge(p)==80,"half charge");double half=p.getAttributeValue(Attributes.JUMP_STRENGTH);
        t.assertTrue(Math.abs(UniqueEquipmentRules.windApex(half)/UniqueEquipmentRules.windApex(.82)-2)<.001,"height interpolation");
        windCharge(p,80);t.assertTrue(WindBootsMomentum.charge(p)==155,"stationary sample must decay");
        p.setPos(p.getX()+.2,p.getY(),p.getZ());for(int i=0;i<10;i++){p.setPos(p.getX()+.2,p.getY(),p.getZ());windTick(p);}
        t.assertTrue(WindBootsMomentum.charge(p)==160,"charge cap");long started=System.nanoTime();
        for(int i=0;i<1000;i++){p.setPos(p.getX()+(i%2==0?.2:-.2),p.getY(),p.getZ());windTick(p);}
        System.out.println("WIND_ACTIVE_OWNER_SAMPLE samples=1000 total_ns="+(System.nanoTime()-started)+" attribute_sync=only_on_step_change no_world_scans=true");p.setSprinting(false);for(int i=0;i<32;i++)windTick(p);
        t.assertTrue(WindBootsMomentum.charge(p)==0&&Math.abs(p.getAttributeValue(Attributes.JUMP_STRENGTH)-.82)<.0001,"decay left modifier");
        windCharge(p,160);p.horizontalCollision=true;windTick(p);t.assertTrue(WindBootsMomentum.charge(p)==0,"wall charge");p.horizontalCollision=false;
        windCharge(p,160);equip(p,EquipmentSlot.FEET,Items.AIR);t.assertTrue(WindBootsMomentum.charge(p)==0&&p.getAttributeValue(Attributes.JUMP_STRENGTH)<.43,"unequip charge");t.succeed();
    }
    @GameTest(template="empty") public static void windChargeAirborneAndLifecycle(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());windCharge(p,40);p.setOnGround(false);
        for(int i=0;i<8;i++){p.setPos(p.getX()+.2,p.getY(),p.getZ());windTick(p);}t.assertTrue(WindBootsMomentum.charge(p)==0,"walking off cliff charged");
        windCharge(p,40);p.jumpFromGround();p.setOnGround(false);for(int i=0;i<8;i++){p.setPos(p.getX()+.2,p.getY()+.1,p.getZ());windTick(p);}
        t.assertTrue(WindBootsMomentum.charge(p)==48,"jump lost sprint continuity");
        WindBootsMomentum.dimension(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(p,p.level().dimension(),net.minecraft.world.level.Level.NETHER));t.assertTrue(WindBootsMomentum.charge(p)==0,"dimension charge");
        windCharge(p,160);p.getAbilities().flying=true;windTick(p);t.assertTrue(WindBootsMomentum.charge(p)==0,"flight charge");p.getAbilities().flying=false;
        windCharge(p,160);WindBootsMomentum.login(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));t.assertTrue(WindBootsMomentum.charge(p)==0,"login charge");
        windCharge(p,160);p.setPos(p.getX()+10,p.getY(),p.getZ());windTick(p);t.assertTrue(WindBootsMomentum.charge(p)==0,"teleport charge");
        windCharge(p,160);p.kill();t.assertTrue(WindBootsMomentum.charge(p)==0,"death charge");t.succeed();
    }
    @GameTest(template="empty") public static void windChargedNativeArcAndFallCredit(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());
        for(int x=2;x<=4;x++)for(int z=2;z<=18;z++)for(int y=20;y<=36;y++)t.setBlock(new net.minecraft.core.BlockPos(x,y,z),y==20?net.minecraft.world.level.block.Blocks.STONE:net.minecraft.world.level.block.Blocks.AIR);
        p.moveTo(t.absoluteVec(new Vec3(3.5,21,3.5)));windCharge(p,160);t.assertTrue(WindBootsMomentum.charge(p)==160,"fixture charge");
        p.setNoGravity(false);p.setYRot(0);p.setOnGround(true);double speed=p.getAttributeValue(Attributes.MOVEMENT_SPEED);p.setDeltaMovement(0,0,speed*.98*.546/(1-.546));double y=p.getY(),z=p.getZ();p.jumpFromGround();
        double peak=0;int ticks=0;boolean checked=false;
        for(int i=0;i<60;i++){p.travel(new Vec3(0,0,.98));ticks++;peak=Math.max(peak,p.getY()-y);
            if(p.onGround()){
                // ServerPlayer's network movement normally raises this event at landing.
                var fall=new LivingFallEvent(p,32,1);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(fall);
                t.assertTrue(fall.getDistance()>23&&fall.getDistance()<24,"cliff credit/immunity "+fall.getDistance());t.assertTrue(Math.abs(fall.getDamageMultiplier()-.6)<.0001,"fall multiplier");
                var second=new LivingFallEvent(p,32,1);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(second);t.assertTrue(second.getDistance()==32,"landing credit reused");checked=true;break;
            }windTick(p);
        }
        t.assertTrue(peak>12.4&&peak<12.6,"max apex "+peak);t.assertTrue(p.getZ()-z>11.3&&p.getZ()-z<11.7,"max range "+(p.getZ()-z));t.assertTrue(ticks==36&&checked,"charged arc did not land");
        System.out.println("WIND_CHARGED_NATIVE_ARC apex="+peak+" range="+(p.getZ()-z)+" ticks="+ticks);t.succeed();
    }
    @GameTest(template="empty") public static void windStoppedMidairRetainsOnlyLandingCredit(GameTestHelper t){
        var p=player(t);equip(p,EquipmentSlot.FEET,UniqueEquipment.WIND_BOOTS.get());windCharge(p,160);p.jumpFromGround();p.setOnGround(false);p.setSprinting(false);
        for(int i=0;i<32;i++){p.setPos(p.getX(),p.getY()+(i<16?.78:-.78),p.getZ());windTick(p);}
        t.assertTrue(WindBootsMomentum.charge(p)==0,"stop charge");var fall=new LivingFallEvent(p,12.46F,1);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(fall);
        t.assertTrue(fall.getDistance()>4.1&&fall.getDistance()<4.3,"stop erased landing credit "+fall.getDistance());
        windCharge(p,160);p.jumpFromGround();p.setOnGround(false);p.setPos(p.getX(),p.getY()+1,p.getZ());windTick(p);var ceiling=new LivingFallEvent(p,20,1);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(ceiling);t.assertTrue(ceiling.getDistance()==20,"low ceiling grants full landing credit");t.succeed();
    }

}
