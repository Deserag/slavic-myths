package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.combat.*;
import org.slavicmyths.registry.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.item.ItemState;
import org.slavicmyths.armorer.*;

@GameTestHolder("slavicmyths_rare") @PrefixGameTestTemplate(false)
public final class RareWeaponGameTests {
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
    private static ServerPlayer player(GameTestHelper t){var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"RareProbe"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,2,3)));p.setNoGravity(true);for(int i=0;i<61;i++)p.tick();return p;}
    private static Zombie zombie(GameTestHelper t,ServerPlayer p,double distance){var z=EntityType.ZOMBIE.create(t.getLevel());z.moveTo(p.position().add(0,0,distance));z.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);z.setHealth(100);z.setNoAi(true);z.setNoGravity(true);t.getLevel().addFreshEntity(z);return z;}
    private static void equip(ServerPlayer p,Item item){p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));p.doTick();}
    private static void hit(ServerPlayer p,LivingEntity target){p.attackStrengthTicker=1000;target.invulnerableTime=0;p.attack(target);}
    private static ItemStack marked(GameTestHelper t,Item item,String rune){var s=new ItemStack(item);ItemState.runes(s,3,List.of(rune));s.enchant(t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),2);s.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal(rune));return s;}
    @GameTest(template="empty") public static void capacityAndItemCodec(GameTestHelper t){
        for(Item item:List.of(RareWeapons.TUGARIN.get(),RareWeapons.LIKHO.get(),RareWeapons.ATAMAN.get(),RareWeapons.VICTOR.get())){
            var s=marked(t,item,"heat");t.assertTrue(Runes.capacity(s)==3,"capacity");t.assertTrue(ModItemGroup.items(ModItemGroup.category(item)).contains(item),"creative");
            var restored=ItemStack.parseOptional(t.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)s.save(t.getLevel().registryAccess()));t.assertTrue(ItemStack.matches(s,restored),"item codec loses state");
        }t.succeed();
    }
    @GameTest(template="empty") public static void singleAndPairBleed(GameTestHelper t){
        var p=player(t);equip(p,RareWeapons.TUGARIN.get());var z=EntityType.COW.create(t.getLevel());z.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);z.setHealth(100);z.setNoAi(true);z.moveTo(p.position().add(0,0,2));t.getLevel().addFreshEntity(z);hit(p,z);t.assertTrue(!z.hasEffect(ModEffects.RARE_BLEED),"single blade bleeds");
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(RareWeapons.TUGARIN.get()));
        for(int i=0;i<5;i++)hit(p,z);
        t.assertTrue(z.getEffect(ModEffects.RARE_BLEED).getAmplifier()==2,"bleed cap");t.assertTrue(z.getEffect(ModEffects.RARE_BLEED).getDuration()==120,"bleed duration");
        float hp=z.getHealth();z.invulnerableTime=0;z.getPersistentData().putLong("RareBleedPulse",RareCombat.now(z));ModEffects.RARE_BLEED.get().applyEffectTick(z,2);t.assertTrue(z.getHealth()<hp,"bleed pulse missing");
        t.succeed();
    }
    @GameTest(template="empty") public static void offhandRunesDoNotDuplicate(GameTestHelper t){
        var p=player(t);equip(p,RareWeapons.TUGARIN.get());p.setItemSlot(EquipmentSlot.OFFHAND,marked(t,RareWeapons.TUGARIN.get(),"heat"));var z=zombie(t,p,2);hit(p,z);
        t.assertTrue(!z.isOnFire(),"offhand rune triggered");p.setItemSlot(EquipmentSlot.MAINHAND,marked(t,RareWeapons.TUGARIN.get(),"heat"));p.doTick();hit(p,z);t.assertTrue(z.isOnFire(),"main rune missing");t.succeed();
    }
    @GameTest(template="empty") public static void swingFourthAndPause(GameTestHelper t){
        var p=player(t);equip(p,RareWeapons.ATAMAN.get());var z=zombie(t,p,2);
        for(int i=1;i<=3;i++){hit(p,z);t.assertTrue(p.getPersistentData().getInt("RareSwingStacks")==i,"stack "+i);}
        float before=z.getHealth();hit(p,z);float boosted=before-z.getHealth();t.assertTrue(p.getPersistentData().getInt("RareSwingStacks")==0,"boost reset");
        before=z.getHealth();hit(p,z);t.assertTrue(boosted>before-z.getHealth(),"boost damage absent");
        p.getPersistentData().putLong("RareSwingUntil",RareCombat.now(p)-1);hit(p,z);t.assertTrue(p.getPersistentData().getInt("RareSwingStacks")==1,"expired stacks survived");t.succeed();
    }
    @GameTest(template="empty") public static void secondNativeAttack(GameTestHelper t){
        var p=player(t);equip(p,RareWeapons.VICTOR.get());var z=zombie(t,p,3.8);hit(p,z);
        t.assertTrue(p.getPersistentData().getLong("RareSecondUntil")>RareCombat.now(p),"far hit no window");float hp=z.getHealth();
        p.attack(z);float damage=hp-z.getHealth();t.assertTrue(damage>5,"second native hit still cooled or invulnerable: "+damage);
        t.assertTrue(p.getPersistentData().getLong("RareSecondUntil")==0,"second chained window");
        t.assertTrue(PathData.data(p).getLong("CD_rare_second")>RareCombat.now(p),"cooldown absent");
        equip(p,Items.IRON_SWORD);equip(p,RareWeapons.VICTOR.get());hit(p,z);t.assertTrue(p.getPersistentData().getLong("RareSecondUntil")==0,"swap bypass");t.succeed();
    }
    @GameTest(template="empty") public static void nearHitDoesNotOpenWindow(GameTestHelper t){var p=player(t);equip(p,RareWeapons.VICTOR.get());hit(p,zombie(t,p,2));t.assertTrue(p.getPersistentData().getLong("RareSecondUntil")==0,"near hit window");t.succeed();}
    @GameTest(template="empty") public static void staffHealHeldAndCapped(GameTestHelper t){
        var p=player(t);var staff=(LikhoStaffItem)RareWeapons.LIKHO.get();equip(p,staff);p.setHealth(10);staff.inventoryTick(p.getMainHandItem(),p.level(),p,0,true);t.assertTrue(p.getHealth()==11,"held heal");
        staff.inventoryTick(p.getMainHandItem(),p.level(),p,0,true);t.assertTrue(p.getHealth()==11,"double heal");
        p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);PathData.data(p).remove("CD_rare_likho_heal");staff.inventoryTick(new ItemStack(staff),p.level(),p,1,false);t.assertTrue(p.getHealth()==11,"inventory heal");
        p.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(staff));p.setHealth(p.getMaxHealth()-.5F);staff.inventoryTick(p.getOffhandItem(),p.level(),p,40,false);t.assertTrue(p.getHealth()==p.getMaxHealth(),"heal overflow");t.succeed();
    }
    @GameTest(template="empty") public static void staffControlAndProtectedTargets(GameTestHelper t){
        var p=player(t);equip(p,RareWeapons.LIKHO.get());var z=zombie(t,p,4);var other=zombie(t,p,6);p.setYRot(0);p.setXRot(0);
        p.getMainHandItem().getItem().use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        t.assertTrue(z.hasEffect(ModEffects.DISORIENTATION),"cast missed");t.assertTrue(DisorientationEffect.stunned(z),"stun absent");z.setTarget(p);t.assertTrue(z.getTarget()==null,"owner target while stunned");
        float hp=p.getHealth();p.hurt(z.damageSources().mobAttack(z),4);t.assertTrue(p.getHealth()==hp,"stunned mob attacks");
        z.getPersistentData().putLong("RareStunUntil",RareCombat.now(z)-1);z.tickCount=20;ModEffects.DISORIENTATION.get().applyEffectTick(z,0);t.assertTrue(z.getTarget()==other,"alternative hostile target absent");
        z.setTarget(p);t.assertTrue(z.getTarget()==null,"owner reacquired");
        var villager=EntityType.VILLAGER.create(t.getLevel());villager.moveTo(z.position());float health=villager.getHealth();villager.hurt(z.damageSources().mobAttack(z),5);t.assertTrue(villager.getHealth()==health,"protected NPC harmed");
        var cd=PathData.data(p).getLong("CD_rare_likho");p.getMainHandItem().getItem().use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);t.assertTrue(PathData.data(p).getLong("CD_rare_likho")==cd,"cooldown changed on rejected use");t.succeed();
    }
    @GameTest(template="empty") public static void benchPairAndUpgrades(GameTestHelper t){
        var p=player(t);t.setBlock(new BlockPos(3,2,3),ModBlocks.ARMORER_TABLE.get());int count=0;
        for(var holder:t.getLevel().getRecipeManager().getAllRecipesFor(ArmorerRecipe.TYPE.get()))if(holder.id().getPath().startsWith("rare_")){
            var recipe=holder.value();var input=new ArrayList<ItemStack>(Collections.nCopies(16,ItemStack.EMPTY));for(int i=0;i<recipe.getIngredients().size();i++)input.set(i,recipe.getIngredients().get(i).getItems()[0].copy());
            input.set(0,marked(t,input.getFirst().getItem(),"heat"));if(holder.id().getPath().equals("rare_tugarin_sword"))input.set(1,marked(t,Items.DIAMOND_SWORD,"wind"));
            var menu=new ArmorerMenu(1,p.getInventory(),t.absolutePos(new BlockPos(3,2,3)));for(int i=0;i<16;i++)menu.input.setItem(i,input.get(i).copy());
            var taken=menu.quickMoveStack(p,0);t.assertTrue(!taken.isEmpty()&&ItemState.runes(taken).equals(ItemState.runes(input.getFirst())),"bench primary state");
            if(taken.is(RareWeapons.TUGARIN.get())){var second=menu.input.getItem(1);t.assertTrue(second.is(RareWeapons.TUGARIN.get())&&ItemState.runes(second).equals(ItemState.runes(input.get(1))),"second donor state");t.assertTrue(menu.quickMoveStack(p,0).isEmpty(),"repeat craft dupes");}
            count++;
        }t.assertTrue(count==4,"four recipes missing");t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=180) public static void controlActuallyExpires(GameTestHelper t){
        var p=player(t);equip(p,RareWeapons.LIKHO.get());var z=zombie(t,p,4);p.setYRot(0);p.setXRot(0);
        p.getMainHandItem().getItem().use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        t.runAfterDelay(30,()->t.assertTrue(!DisorientationEffect.stunned(z)&&z.hasEffect(ModEffects.DISORIENTATION),"stun failed to end"));
        t.runAfterDelay(150,()->{t.assertTrue(!z.hasEffect(ModEffects.DISORIENTATION),"morok failed to expire");z.setTarget(p);t.assertTrue(z.getTarget()==p,"native target never restored");t.succeed();});
    }
    @GameTest(template="empty") public static void chargedShieldPressure(GameTestHelper t){
        var p=player(t);p.getServer().setPvpAllowed(true);equip(p,RareWeapons.ATAMAN.get());var defender=player(t);defender.moveTo(p.position().add(0,0,2));defender.setYRot(180);defender.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));defender.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
        for(int i=0;i<61;i++){defender.tick();defender.doTick();}t.assertTrue(defender.isBlocking(),"fixture shield not raised");p.getPersistentData().putInt("RareSwingStacks",3);p.getPersistentData().putLong("RareSwingUntil",RareCombat.now(p)+80);hit(p,defender);
        t.assertTrue(defender.getCooldowns().isOnCooldown(Items.SHIELD)&&!defender.isBlocking(),"shield not disabled; pvp="+p.getServer().isPvpAllowed()+" health="+defender.getHealth()+" boost="+p.getPersistentData().getBoolean("RareSwingBoost")+" charge="+p.getPersistentData().getFloat("RareAttackCharge")+" blocking="+defender.isBlocking()+" position="+defender.position()+" look="+defender.getLookAngle());t.assertTrue(p.getPersistentData().getInt("RareSwingStacks")==0,"blocked boost not consumed");t.succeed();
    }
    @GameTest(template="empty") public static void nativeDeathAndDimension(GameTestHelper t){
        var p=player(t);var expected=new ArrayList<ItemStack>();for(var item:List.of(RareWeapons.TUGARIN.get(),RareWeapons.LIKHO.get(),RareWeapons.ATAMAN.get(),RareWeapons.VICTOR.get())){var s=marked(t,item,"heat");expected.add(s);p.getInventory().add(s.copy());}
        var world=t.getLevel();var origin=p.position();world.addNewPlayer(p);
        try{
            var nether=world.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
            p.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(nether,new Vec3(0,80,0),Vec3.ZERO,0,0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
            t.assertTrue(p.serverLevel()==nether,"nether transition");p.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(world,origin,Vec3.ZERO,0,0,net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));t.assertTrue(p.serverLevel()==world,"return transition");
            for(var s:expected)t.assertTrue(p.getInventory().items.stream().anyMatch(x->ItemStack.matches(s,x)),"dimension state lost");
            p.hasChangedDimension();p.kill();t.assertTrue(!p.isAlive(),"native death failed");for(var s:expected){var drops=world.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(5),x->ItemStack.matches(s,x.getItem()));t.assertTrue(drops.size()==1,"death loss or duplication");}
        }finally{world.getServer().getPlayerList().remove(p);}t.succeed();
    }
    @GameTest(template="empty") public static void actualBossLoot(GameTestHelper t){
        var p=player(t);int total=0;
        for(var type:List.of(ModEntities.ATAMAN.get(),ModEntities.LIKHO_ONE_EYED.get(),ModEntities.TUGARIN_ZMEY.get())){
            var entity=(LivingEntity)type.create(t.getLevel());entity.moveTo(p.position().add(0,0,2));t.getLevel().addFreshEntity(entity);entity.hurt(p.damageSources().playerAttack(p),10000);
            Item material=type==ModEntities.ATAMAN.get()?RareWeapons.SIGN.get():type==ModEntities.LIKHO_ONE_EYED.get()?ModItems.OKO_LIKHA.get():ModItems.TUGARINOVA_KOZHA.get();
            t.assertTrue(!t.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(5),x->x.getItem().is(material)).isEmpty(),"boss material missing "+material);total++;
        }t.assertTrue(total==3,"loot tests absent");t.succeed();
    }
    @GameTest(template="empty") public static void nativePlayerStorage(GameTestHelper t){
        String phase=System.getProperty("slavicmyths.rareRestartPhase","none");var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.fromString("e1526377-c2a8-4f4f-a162-000001322132"),"RareRestart"),ClientInformation.createDefault());p.connection=new Listener(p);
        var expected=java.nio.file.Path.of("rare-restart-1322.nbt");
        try{
            if(!phase.equals("verify")){
                var stacks=new net.minecraft.nbt.ListTag();for(var item:List.of(RareWeapons.TUGARIN.get(),RareWeapons.LIKHO.get(),RareWeapons.ATAMAN.get(),RareWeapons.VICTOR.get())){var s=marked(t,item,"heat");p.getInventory().add(s.copy());stacks.add(s.save(w.registryAccess()));}
                PathData.data(p).putLong("CD_rare_likho",RareCombat.now(p)+560);PathData.data(p).putLong("CD_rare_second",RareCombat.now(p)+160);
                p.getPersistentData().putLong("RareSecondUntil",RareCombat.now(p)+25);
                var data=new net.minecraft.nbt.CompoundTag();data.put("Weapons",stacks);data.putLong("LikhoCooldown",PathData.data(p).getLong("CD_rare_likho"));data.putLong("SecondCooldown",PathData.data(p).getLong("CD_rare_second"));
                net.minecraft.nbt.NbtIo.writeCompressed(data,expected);w.getServer().getPlayerList().remove(p);
            }
            if(!phase.equals("write")){
                t.assertTrue(w.getServer().getPlayerList().load(p).isPresent(),"native player .dat absent");var data=net.minecraft.nbt.NbtIo.readCompressed(expected,net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                for(var tag:data.getList("Weapons",10)){var s=ItemStack.parse(w.registryAccess(),tag).orElseThrow();t.assertTrue(p.getInventory().items.stream().anyMatch(x->ItemStack.matches(s,x)),"saved item state lost");}
                t.assertTrue(PathData.data(p).getLong("CD_rare_likho")==data.getLong("LikhoCooldown")&&PathData.data(p).getLong("CD_rare_second")==data.getLong("SecondCooldown"),"saved cooldown lost");
                RareCombat.join(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(p));t.assertTrue(p.getPersistentData().getLong("RareSecondUntil")==0,"login retained attack window");
                System.out.println("RARE_NATIVE_STORAGE_"+(phase.equals("verify")?"NEW_PROCESS":"SAME_PROCESS")+"_PASS items=4 cooldowns=2");
            }t.succeed();
        }catch(Exception ex){throw new RuntimeException(ex);}
    }
    @GameTest(template="empty") public static void nativeRepairAndPickup(GameTestHelper t){
        var p=player(t);p.experienceLevel=100;
        for(var item:List.of(RareWeapons.TUGARIN.get(),RareWeapons.LIKHO.get(),RareWeapons.ATAMAN.get(),RareWeapons.VICTOR.get())){
            var source=marked(t,item,"heat");source.setDamageValue(150);var anvil=new net.minecraft.world.inventory.AnvilMenu(1,p.getInventory());anvil.getSlot(0).set(source.copy());anvil.getSlot(1).set(new ItemStack(Items.DIAMOND));var repaired=anvil.getSlot(2).getItem();
            t.assertTrue(!repaired.isEmpty()&&repaired.getDamageValue()<150&&ItemState.runes(repaired).equals(ItemState.runes(source))&&repaired.get(DataComponents.ENCHANTMENTS).equals(source.get(DataComponents.ENCHANTMENTS)),"repair state lost "+item);
            var drop=new net.minecraft.world.entity.item.ItemEntity(t.getLevel(),p.getX(),p.getY(),p.getZ(),repaired.copy());drop.setNoPickUpDelay();t.getLevel().addFreshEntity(drop);drop.playerTouch(p);
            t.assertTrue(p.getInventory().items.stream().filter(s->ItemStack.matches(s,repaired)).count()==1,"pickup loss or duplication "+item);
        }t.succeed();
    }
    @GameTest(template="empty") public static void mobShieldCannotImmediatelyReturn(GameTestHelper t){
        var p=player(t);equip(p,RareWeapons.ATAMAN.get());var z=zombie(t,p,2);z.setYRot(180);z.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));z.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);for(int i=0;i<6;i++)z.tick();
        z.setYRot(180);z.setYHeadRot(180);t.assertTrue(z.isBlocking(),"fixture mob shield not raised");p.getPersistentData().putInt("RareSwingStacks",3);p.getPersistentData().putLong("RareSwingUntil",RareCombat.now(p)+80);hit(p,z);
        t.assertTrue(z.getPersistentData().getLong("RareShieldUntil")>RareCombat.now(z),"mob shield disable absent; look="+z.getLookAngle()+" health="+z.getHealth()+" stacks="+p.getPersistentData().getInt("RareSwingStacks"));
        z.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);for(int i=0;i<6;i++)z.tick();float hp=z.getHealth();hit(p,z);t.assertTrue(z.getHealth()<hp,"mob shield bypassed disable");t.succeed();
    }
}
