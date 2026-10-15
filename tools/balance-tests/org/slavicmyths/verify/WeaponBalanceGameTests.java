package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.gorynych.*;
import org.slavicmyths.morozko.*;
import org.slavicmyths.karachun.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
@GameTestHolder("slavicmyths_balance") @PrefixGameTestTemplate(false)
public final class WeaponBalanceGameTests {
    static final Map<UUID,List<Float>> PROJECTILE_DAMAGE=new HashMap<>();
    @net.neoforged.bus.api.SubscribeEvent public static void recorded(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e){
        if(e.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow a&&a.getOwner()!=null&&PROJECTILE_DAMAGE.containsKey(a.getOwner().getUUID()))
            PROJECTILE_DAMAGE.get(a.getOwner().getUUID()).add(e.getNewDamage());
    }
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        final List<String> messages=new ArrayList<>();
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet){if(packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket chat)messages.add(chat.content().getString());}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
    }
    private static ServerPlayer player(GameTestHelper h,UUID uuid){
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(uuid,"BalanceQA"),ClientInformation.createDefault());
        p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);return p;
    }
    private static ServerPlayer player(GameTestHelper h){return player(h,UUID.randomUUID());}

    static boolean before(){return System.getProperty("slavicmyths.balancePhase").equals("before");}
    static ItemStack item(String id){return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id)));}
    static ServerPlayer equipped(GameTestHelper h,String id){var p=player(h);p.moveTo(h.absoluteVec(new Vec3(2,3,2)));p.setItemSlot(EquipmentSlot.MAINHAND,item(id));p.doTick();p.setOnGround(true);p.setDeltaMovement(Vec3.ZERO);p.attackStrengthTicker=1000;return p;}
    static net.minecraft.world.entity.monster.Spider target(GameTestHelper h,ServerPlayer p){var t=EntityType.SPIDER.create(h.getLevel());t.setNoAi(true);t.setPersistenceRequired();t.setNoGravity(true);t.moveTo(p.position().add(0,0,2));t.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);t.getAttribute(Attributes.ARMOR).setBaseValue(0);t.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);t.setHealth(1000);h.getLevel().addFreshEntity(t);return t;}
    static void near(GameTestHelper h,double actual,double expected,String msg){h.assertTrue(Math.abs(actual-expected)<.002,msg+" expected="+expected+" actual="+actual);}
    static final class HitArrow extends net.minecraft.world.entity.projectile.Arrow {
        HitArrow(GameTestHelper h,LivingEntity owner,ItemStack weapon){this(h,owner,weapon,new ItemStack(Items.ARROW));}
        HitArrow(GameTestHelper h,LivingEntity owner,ItemStack weapon,ItemStack ammo){super(h.getLevel(),owner,ammo,weapon);setBaseDamage(2*1.18);setDeltaMovement(0,0,3.15);setCritArrow(false);getPersistentData().putUUID(CrossbowDamage.VOLLEY,UUID.randomUUID());}
        void critical(long seed){random.setSeed(seed);setCritArrow(true);}
        void hit(LivingEntity t){onHitEntity(new EntityHitResult(t));}
    }
    static float strike(GameTestHelper h,ServerPlayer p,LivingEntity t,String id)throws Exception{
        float start=t.getHealth();
        if(id.equals("morozko_staff")){
            FrostSkills.shoot(p);var shard=h.getLevel().getEntitiesOfClass(FrostShard.class,p.getBoundingBox().inflate(3)).stream().filter(e->e.getOwner()==p).findFirst().orElseThrow();
            var m=FrostShard.class.getDeclaredMethod("onHitEntity",EntityHitResult.class);m.setAccessible(true);m.invoke(shard,new EntityHitResult(t));shard.discard();
        }else if(id.equals("three_breath_crossbow")){new HitArrow(h,p,p.getMainHandItem()).hit(t);}
        else if(id.equals("karachun_scythe")){NightCombat.damage(p,t,p.getMainHandItem(),1);}
        else p.attack(t);
        return start-t.getHealth();
    }
    @GameTest(template="empty") public static void measuredTenWeapons(GameTestHelper h)throws Exception{
        String[] ids={"ataman_flail","nightingale_dagger","likho_staff","tugarin_sword","victor_spear","morozko_staff","karachun_scythe","trophy_kalin_sabre","three_breath_crossbow","poloz_whip"};
        float[] old={12,10,10,10,12,7,12,9,14,13},now={12,12,13,14,15,16,20,18,16,24};
        var rows=new com.google.gson.JsonArray();
        for(int i=0;i<ids.length;i++){
            String id=ids[i];var p=equipped(h,id);var t=target(h,p);
            float raw=strike(h,p,t,id);float expected=(before()?old:now)[i]*(id.equals("tugarin_sword")?1.4f:1);near(h,raw,expected,id+" native HP incl inherent pair");
            var row=new com.google.gson.JsonObject();row.addProperty("id",id);row.addProperty("hp_before",1000);row.addProperty("hp_after",t.getHealth());row.addProperty("raw",raw);row.addProperty("attack_speed",p.getAttributeValue(Attributes.ATTACK_SPEED));
            rows.add(row);t.discard();
        }
        Files.writeString(Path.of(System.getProperty("slavicmyths.balanceRoot"),"docs/implementation/legendary-weapon-balance/"+(before()?"BEFORE":"AFTER")+".json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
        System.out.println("BALANCE_NATIVE_MEASURED_10_"+(before()?"BEFORE":"AFTER")+"_PASS "+rows);h.succeed();
    }
    static final String[] ALL={"ataman_flail","nightingale_dagger","likho_staff","tugarin_sword","victor_spear","morozko_staff","karachun_scythe","trophy_kalin_sabre","three_breath_crossbow","poloz_whip"};
    @GameTest(template="empty") public static void frostHeartbeatRegression(GameTestHelper h){FrostStreamIssueTests.heartbeatGrace(h);}
    @GameTest(template="empty") public static void frostDeadlineRegression(GameTestHelper h){FrostStreamIssueTests.fixedDeadline(h);}
    @GameTest(template="empty") public static void frostReleaseMenuRegression(GameTestHelper h){FrostStreamIssueTests.releaseAndMenu(h);}
    @GameTest(template="empty") public static void fireRegression(GameTestHelper h){GorynychFireIssueTests.damageAndResistanceUnchanged(h);}
    @GameTest(template="empty") public static void visualCodecRegression(GameTestHelper h){GorynychFireIssueTests.visualMessageCodec(h);}
    @GameTest(template="empty") public static void stormRegression(GameTestHelper h){GorynychStormIssueTests.oneAndThreeTargets(h);}
    @GameTest(template="empty") public static void stormLosRegression(GameTestHelper h){GorynychStormIssueTests.wallBlocksStorm(h);}
    @GameTest(template="empty") public static void defensesAndEnchantments(GameTestHelper h)throws Exception{
        if(before()){h.succeed();return;}
        for(String id:ALL){
            var p=equipped(h,id);var plain=target(h,p);float raw=strike(h,p,plain,id);plain.discard();
            var armored=target(h,p);armored.getAttribute(Attributes.ARMOR).setBaseValue(20);armored.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(8);
            p.attackStrengthTicker=1000;FrostSkills.data(p).remove("BoltReady");float armor=strike(h,p,armored,id);armored.discard();
            h.assertTrue(armor>0&&armor<raw,id+" must respect armor "+armor+" / "+raw);
            var resistant=target(h,p);resistant.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,200,1));
            p.attackStrengthTicker=1000;FrostSkills.data(p).remove("BoltReady");float resistance=strike(h,p,resistant,id);resistant.discard();
            h.assertTrue(resistance>0&&resistance<raw,id+" must respect resistance");
            if(!id.equals("morozko_staff")){
                var enchant=h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
                p.getMainHandItem().enchant(enchant,3);p.attackStrengthTicker=1000;var enchanted=target(h,p);float enhanced=strike(h,p,enchanted,id);enchanted.discard();
                h.assertTrue(enhanced>raw,id+" native enchantment bonus lost: "+enhanced+" / "+raw);
            }
            System.out.println("BALANCE_DEFENSE "+id+" raw="+raw+" armor20_tough8="+armor+" resistanceII="+resistance);
        }h.succeed();
    }
    @GameTest(template="empty") public static void crossbowExactAndScoped(GameTestHelper h){
        if(before()){h.succeed();return;}
        var p=equipped(h,"three_breath_crossbow");
        for(double speed:new double[]{.1,1,3.15,6}){var t=target(h,p);var a=new HitArrow(h,p,p.getMainHandItem());a.setDeltaMovement(0,0,speed);a.hit(t);near(h,1000-t.getHealth(),16,"special speed "+speed);t.discard();}
        var t=target(h,p);var volley=UUID.randomUUID();HitArrow first=null;
        for(int i=0;i<3;i++){var a=new HitArrow(h,p,p.getMainHandItem());a.getPersistentData().putUUID(CrossbowDamage.VOLLEY,volley);a.hit(t);if(i==0)first=a;}
        near(h,1000-t.getHealth(),48,"three distinct arrows");first.hit(t);near(h,1000-t.getHealth(),48,"duplicate arrow");
        new HitArrow(h,p,p.getMainHandItem()).hit(t);near(h,1000-t.getHealth(),48,"different volley keeps iframe");
        var normal=target(h,p);new HitArrow(h,p,item("serpent_crossbow")).hit(normal);near(h,1000-normal.getHealth(),8,"ordinary crossbow unchanged");
        normal.discard();t.discard();
        float maxCrit=16;for(int i=0;i<20;i++){var critical=target(h,p);var a=new HitArrow(h,p,p.getMainHandItem());a.critical(i);a.hit(critical);float damage=1000-critical.getHealth();h.assertTrue(damage>=16&&damage<=25,"native explicit critical bounds");maxCrit=Math.max(maxCrit,damage);critical.discard();}h.assertTrue(maxCrit>16,"Explicit crit bonus preserved");
        var ammo=new ItemStack(Items.TIPPED_ARROW);ammo.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.SLOWNESS));
        var poisoned=target(h,p);new HitArrow(h,p,p.getMainHandItem(),ammo).hit(poisoned);near(h,1000-poisoned.getHealth(),16,"tipped arrow direct16");h.assertTrue(poisoned.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Native tipped-arrow effect preserved");poisoned.discard();h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100) public static void frostActualStream(GameTestHelper h){
        if(before()){h.succeed();return;}
        var p=equipped(h,"morozko_staff");p.setXRot(13);p.setYRot(0);var t=target(h,p);
        t.moveTo(p.position().add(0,0,5));
        h.assertTrue(FrostSkills.startStream(p),"stream start");p.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        for(int tick=1;tick<=60;tick++){final int age=tick;h.runAfterDelay(tick,()->{
            if(age%3==0)FrostSkills.action(p,4);
            // Tick the native target cooldown, as this is a real server-loaded entity.
            FrostSkills.streamTick(p,60-age);
            if(age==60){near(h,1000-t.getHealth(),24,"six real stream pulses at4,14,24,34,44,54");h.assertTrue(!p.isUsingItem(),"stream stops at60");
                h.assertTrue(FrostSkills.data(p).getLong("StreamReady")==FrostSkills.now(p)+200,"CD200 unchanged");t.discard();h.succeed();}
        });}
    }
    @GameTest(template="empty") public static void scytheComboAndNoDouble(GameTestHelper h){
        if(before()){h.succeed();return;}
        var p=equipped(h,"karachun_scythe");p.setYRot(0);p.setXRot(0);var t=target(h,p);
        float hp=t.getHealth();p.attack(t);near(h,t.getHealth(),hp,"vanilla attack must be canceled during windup");
        var n=p.getPersistentData().getCompound(NightCombat.KEY);h.assertTrue(!n.isEmpty(),"native attack scheduled custom swing");
        n.putLong("Start",NightCombat.now(p)-n.getInt("Windup"));NightCombat.tick(p);NightCombat.tick(p);
        near(h,hp-t.getHealth(),20,"one delayed swing, no double damage");NightCombat.cancel(p);
        for(int combo=1;combo<3;combo++){t.invulnerableTime=0;NightLedger.get(p.serverLevel()).player(p.getUUID()).remove("SwingReady");p.attackStrengthTicker=1000;p.attack(t);n=p.getPersistentData().getCompound(NightCombat.KEY);n.putLong("Start",NightCombat.now(p)-n.getInt("Windup"));NightCombat.tick(p);NightCombat.cancel(p);}
        near(h,hp-t.getHealth(),60,"three combo swings");t.discard();h.succeed();
    }
    @GameTest(template="empty") public static void scytheAbilities(GameTestHelper h)throws Exception{
        if(before()){h.succeed();return;}
        var p=equipped(h,"karachun_scythe");p.setYRot(0);p.setXRot(0);var t=target(h,p);
        NightCombat.action(p,1);var n=p.getPersistentData().getCompound(NightCombat.KEY);n.putLong("Start",NightCombat.now(p)-n.getInt("Windup"));NightCombat.tick(p);NightCombat.cancel(p);
        near(h,1000-t.getHealth(),28,"G breath retains1.4 multiplier");t.invulnerableTime=0;
        NightCombat.action(p,2);n=p.getPersistentData().getCompound(NightCombat.KEY);n.putLong("Start",NightCombat.now(p)-n.getInt("Windup"));NightCombat.tick(p);
        var blades=h.getLevel().getEntitiesOfClass(NightBlade.class,p.getBoundingBox().inflate(3),b->b.getOwner()==p);
        h.assertTrue(blades.size()==3,"V three authored blade lanes");var hit=NightBlade.class.getDeclaredMethod("onHitEntity",EntityHitResult.class);hit.setAccessible(true);
        for(var b:blades){hit.invoke(b,new EntityHitResult(t));b.discard();}
        near(h,1000-t.getHealth(),50,"V dedupe one22 hit, not three");NightCombat.cancel(p);t.discard();h.succeed();
    }
    @GameTest(template="empty") public static void polozMultipart(GameTestHelper h){
        if(before()){h.succeed();return;}
        for(int phase=1;phase<=3;phase++){
            var p=equipped(h,"poloz_whip");var boss=org.slavicmyths.poloz.PolozContent.BOSS.get().create(h.getLevel());
            boss.setNoAi(true);boss.moveTo(p.position().add(0,0,10));h.getLevel().addFreshEntity(boss);boss.initializeBody();boss.setPhaseForTest(phase);
            p.moveTo(boss.parts[5].position().add(0,0,1));p.setOnGround(true);p.attackStrengthTicker=1000;
            float hp=boss.getHealth();p.attack(boss.parts[5]);float damage=hp-boss.getHealth();
            h.assertTrue(damage>0&&damage<=24,"Poloz native multipart strike phase"+phase+" damage="+damage);
            p.attackStrengthTicker=1000;p.attack(boss.parts[5]);near(h,hp-boss.getHealth(),damage,"multipart same actor cooldown");
            System.out.println("BALANCE_POLOZ_PHASE "+phase+" input24 actual="+damage);boss.discard();
        }h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100) public static void productionVolleys(GameTestHelper h){
        if(before()){h.succeed();return;}
        int[] completed={0};
        for(int index=0;index<12;index++){final int testCase=index;{
            int mode=testCase/4;boolean burst=testCase%4>=2;var hand=testCase%2==0?net.minecraft.world.InteractionHand.MAIN_HAND:net.minecraft.world.InteractionHand.OFF_HAND;
            var p=equipped(h,"three_breath_crossbow");p.setNoGravity(true);p.setPos(p.getX(),p.getY()+testCase*12,p.getZ());p.setYRot(0);p.setXRot(0);var weapon=p.getMainHandItem();
            if(hand==net.minecraft.world.InteractionHand.OFF_HAND){p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);p.setItemInHand(hand,weapon);}
            var state=new net.minecraft.nbt.CompoundTag();state.putInt("Mode",mode);state.putBoolean("Burst",burst);var root=new net.minecraft.nbt.CompoundTag();root.put("Gorynych196",state);
            weapon.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(root));
            p.getInventory().setItem(9,new ItemStack(Items.ARROW));var bow=(SerpentCrossbow)weapon.getItem();bow.use(h.getLevel(),p,hand);bow.releaseUsing(weapon,h.getLevel(),p,0);
            h.assertTrue(net.minecraft.world.item.CrossbowItem.isCharged(weapon)&&p.getInventory().getItem(9).isEmpty(),"Native charging must spend one arrow case"+testCase);
            var target=target(h,p);target.moveTo(p.position().add(0,0,6));PROJECTILE_DAMAGE.put(p.getUUID(),new ArrayList<>());
            bow.performShooting(h.getLevel(),p,hand,weapon,3.15f,0,null);
            for(int age=0;age<=18;age++){final int elapsed=age;h.runAfterDelay(age,()->{
                CrossbowVolley.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
                // Converge the fan in the fixture to test three real collisions on one ordinary hitbox.
                for(var a:h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.AbstractArrow.class,p.getBoundingBox().inflate(12),a->a.getOwner()==p)){
                    if(!a.getPersistentData().getBoolean("BalanceAimed")){a.getPersistentData().putBoolean("BalanceAimed",true);a.setPos(p.getEyePosition());var d=target.getBoundingBox().getCenter().subtract(a.position()).normalize();a.shoot(d.x,d.y,d.z,3.15f,0);}
                }
                if(elapsed==18){var damages=PROJECTILE_DAMAGE.remove(p.getUUID());h.assertTrue(damages.size()==3,"Native collisions three arrows case"+testCase+" actual="+damages+" durability="+weapon.getDamageValue()+" alive="+p.isAlive()+" pending="+CrossbowVolley.active(p)+" target="+target.isAlive()+" pos="+p.position());
                    for(float damage:damages)near(h,damage,16,"native ordinary arrow exact16 case"+testCase);
                    h.assertTrue(weapon.getDamageValue()==3,"Durability3 case"+testCase);
                    System.out.println("BALANCE_VOLLEY_PHYSICS case="+testCase+" mode="+mode+" burst="+burst+" hand="+hand+" direct="+damages+" serverHP="+target.getHealth()+" ammo1 durability3");
                    CrossbowVolley.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));target.discard();
                    for(var cloud:h.getLevel().getEntitiesOfClass(BreathCloud.class,p.getBoundingBox().inflate(12)))cloud.discard();
                    if(++completed[0]==12)h.succeed();
                }
            });}
        }}
    }
    @GameTest(template="empty") public static void preservationAndSafety(GameTestHelper h){
        if(before()){h.succeed();return;}
        var p=equipped(h,"karachun_scythe");
        // Vanilla stack serialization of an already existing item keeps components and gets new defaults.
        p.getMainHandItem().setDamageValue(42);var restored=ItemStack.parseOptional(p.registryAccess(),(net.minecraft.nbt.CompoundTag)p.getMainHandItem().save(p.registryAccess()));
        h.assertTrue(restored.is(NightContent.SCYTHE.get())&&restored.getDamageValue()==42,"Old stack identity/durability");
        var creative=player(h);creative.setGameMode(GameType.CREATIVE);float hp=creative.getHealth();NightCombat.damage(p,creative,p.getMainHandItem(),1);near(h,creative.getHealth(),hp,"creative immunity");
        var spectator=player(h);spectator.setGameMode(GameType.SPECTATOR);NightCombat.damage(p,spectator,p.getMainHandItem(),1);near(h,spectator.getHealth(),spectator.getMaxHealth(),"spectator immunity");
        var pet=EntityType.WOLF.create(h.getLevel());pet.setOwnerUUID(p.getUUID());h.assertTrue(!NightCombat.valid(pet,p),"Owned pet excluded");
        p.setGameMode(GameType.SPECTATOR);NightCombat.action(p,1);h.assertTrue(!p.getPersistentData().contains(NightCombat.KEY),"spectator cannot activate");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=240) public static void measuredDamageClock(GameTestHelper h){
        String[] ids={"ataman_flail","nightingale_dagger","likho_staff","tugarin_sword","victor_spear","morozko_staff","karachun_scythe","trophy_kalin_sabre","poloz_whip"};
        var players=new ArrayList<ServerPlayer>();var targets=new ArrayList<LivingEntity>();var anchors=new ArrayList<Vec3>();int[] intervals=new int[ids.length];
        for(int i=0;i<ids.length;i++){var p=equipped(h,ids[i]);p.setNoGravity(true);p.setPos(p.getX(),p.getY()+12*i,p.getZ());p.setYRot(0);p.setXRot(0);players.add(p);var t=target(h,p);targets.add(t);anchors.add(t.position());intervals[i]=ids[i].equals("morozko_staff")?16:(int)Math.ceil(20/p.getAttributeValue(Attributes.ATTACK_SPEED));}
        for(int tick=1;tick<=200;tick++){final int age=tick;h.runAfterDelay(tick,()->{
            try{
                for(int i=0;i<ids.length;i++){
                    var p=players.get(i);var t=targets.get(i);p.doTick();p.setOnGround(true);p.setDeltaMovement(Vec3.ZERO);t.setPos(anchors.get(i));t.setDeltaMovement(Vec3.ZERO);
                    NightCombat.tick(p);
                    if((age-1)%intervals[i]==0){if(ids[i].equals("karachun_scythe"))p.attack(t);else strike(h,p,t,ids[i]);}
                }
                if(age==200){var rows=new com.google.gson.JsonArray();for(int i=0;i<ids.length;i++){var row=new com.google.gson.JsonObject();float damage=1000-targets.get(i).getHealth();row.addProperty("id",ids[i]);row.addProperty("server_ticks",200);row.addProperty("attack_interval_ticks",intervals[i]);row.addProperty("hp_damage",damage);row.addProperty("dps_10_seconds",damage/10);rows.add(row);targets.get(i).discard();NightCombat.cancel(players.get(i));}
                    Files.writeString(Path.of(System.getProperty("slavicmyths.balanceRoot"),"docs/implementation/legendary-weapon-balance/"+(before()?"DPS_CLOCK_BEFORE":"DPS_CLOCK")+".json"),new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(rows));
                    System.out.println("BALANCE_NATIVE_DAMAGE_CLOCK "+rows);h.succeed();
                }
            }catch(Exception e){throw new RuntimeException(e);}
        });}
    }
    @GameTest(template="empty",timeoutTicks=150) public static void frostWaveAndIcicles(GameTestHelper h){
        var p=equipped(h,"morozko_staff");p.setNoGravity(true);h.getLevel().addNewPlayer(p);
        for(int x=0;x<7;x++)for(int z=0;z<7;z++)h.setBlock(x,1,z,net.minecraft.world.level.block.Blocks.STONE);
        p.moveTo(h.absoluteVec(new Vec3(2,2,2)));var t=target(h,p);t.moveTo(p.position().add(0,0,3));t.setNoGravity(false);t.setOnGround(true);
        FrostSkills.action(p,2);
        h.runAfterDelay(35,()->{
            float wave=1000-t.getHealth();near(h,wave,4,"wave unchanged");t.setHealth(1000);t.invulnerableTime=0;t.moveTo(p.position().add(0,0,3));t.setDeltaMovement(Vec3.ZERO);t.setNoGravity(true);FrostSkills.remember(p,t);FrostSkills.action(p,1);
            h.assertTrue(h.getLevel().getEntitiesOfClass(FrostHazard.class,t.getBoundingBox().inflate(8)).size()>=5,"Five native icicles were created; messages="+((Listener)p.connection).messages+" held="+p.getMainHandItem()+" target="+t.position()+" caster="+p.position()+" alive="+t.isAlive()+" los="+p.hasLineOfSight(t)+" casterAlive="+p.isAlive()+" spectator="+p.isSpectator()+" menu="+(p.containerMenu==p.inventoryMenu)+" ready="+p.getPersistentData().getLong("FrostActionReady")+" now="+FrostSkills.now(p));
        });
        h.runAfterDelay(110,()->{
            float icicles=1000-t.getHealth();h.assertTrue(icicles>0&&icicles<=25,"bounded native five-icicle burst "+icicles);
            System.out.println("BALANCE_FROST_ABILITIES phase="+(before()?"before":"after")+" bolt="+(before()?7:16)+" stream="+(before()?1:4)+" wave=4 icicles5_per_lane actualSingleTarget="+icicles);
            t.discard();h.getLevel().removePlayerImmediately(p,Entity.RemovalReason.DISCARDED);h.succeed();
        });
    }
    @GameTest(template="empty") public static void classAndRunePipeline(GameTestHelper h)throws Exception{
        if(before()){h.succeed();return;}
        for(String id:ALL){if(id.equals("morozko_staff")||id.equals("three_breath_crossbow"))continue;
            var p=equipped(h,id);var t=target(h,p);float raw=strike(h,p,t,id);t.discard();
            p.getPersistentData().putDouble("ClassAttackBonus",.25);p.attackStrengthTicker=1000;t=target(h,p);float classDamage=strike(h,p,t,id);t.discard();
            near(h,classDamage,raw*1.25,id+" existing class event bonus");p.getPersistentData().remove("ClassAttackBonus");
            org.slavicmyths.rpg.Runes.write(p.getMainHandItem(),1,List.of("strength"));org.slavicmyths.rpg.RuneRuntime.refresh(p);p.attackStrengthTicker=1000;t=target(h,p);float runeDamage=strike(h,p,t,id);t.discard();
            h.assertTrue(org.slavicmyths.rpg.RuneRework.supports(p.getMainHandItem(),"strength")?runeDamage>raw:Math.abs(runeDamage-raw)<.003,id+" strength compatibility/bonus "+runeDamage+" / "+raw);
        }h.succeed();
    }
    @GameTest(template="empty") public static void pvpDisabled(GameTestHelper h)throws Exception{
        if(before()){h.succeed();return;}
        var server=h.getLevel().getServer();boolean prior=server.isPvpAllowed();server.setPvpAllowed(false);
        try{for(String id:ALL){var p=equipped(h,id);var other=player(h);other.moveTo(p.position().add(0,0,2));float hp=other.getHealth();strike(h,p,other,id);near(h,other.getHealth(),hp,id+" respects disabled PvP");}}
        finally{server.setPvpAllowed(prior);}h.succeed();
    }
}
