package org.slavicmyths.auka;

import java.lang.reflect.Field;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.gametest.framework.GameTest;
import org.slavicmyths.registry.ModEffects;

@GameTestHolder("slavicmyths_auka") @PrefixGameTestTemplate(false)
public final class AukaGameTests {
    private static final class ProbeConnection extends Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        ProbeConnection(){super(PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class ProbeListener extends ServerGamePacketListenerImpl {
        ProbeListener(ServerPlayer p){super(p.getServer(),new ProbeConnection(),p,CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p){}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
    }
    private static ServerPlayer player(GameTestHelper helper){
        var p=new ServerPlayer(helper.getLevel().getServer(),helper.getLevel(),
            new com.mojang.authlib.GameProfile(UUID.randomUUID(),"AukaProbe"),ClientInformation.createDefault());
        p.connection=new ProbeListener(p);
        p.setGameMode(GameType.SURVIVAL);
        p.moveTo(helper.absoluteVec(new Vec3(5,2,5)));
        p.setNoGravity(true);
        for(int i=0;i<61;i++)p.tick();
        helper.getLevel().addNewPlayer(p);
        return p;
    }
    private static void ground(GameTestHelper helper){
        for(int x=-4;x<15;x++)for(int z=-4;z<15;z++){
            helper.setBlock(new BlockPos(x,1,z),Blocks.STONE);
            for(int y=2;y<=7;y++)helper.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
    }
    private static AukaBoss boss(GameTestHelper helper){
        var a=AukaContent.AUKA.get().create(helper.getLevel());
        a.moveTo(helper.absoluteVec(new Vec3(5,2,8)));
        a.setNoGravity(true);a.setOnGround(true);
        helper.getLevel().addFreshEntity(a);
        return a;
    }
    private static void field(Object obj,String name,Object value)throws Exception{
        Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,value);
    }
    private static UUID invite(GameTestHelper helper,AukaBoss boss,ServerPlayer p)throws Exception{
        UUID token=UUID.randomUUID();
        field(boss,"invited",p.getUUID());
        field(boss,"invitationToken",token);
        field(boss,"invitationEnds",helper.getLevel().getGameTime()+400);
        return token;
    }
    private static void idle(GameTestHelper helper,AukaBoss boss)throws Exception{
        var move=AukaBoss.class.getDeclaredField("MOVE");
        move.setAccessible(true);
        @SuppressWarnings("unchecked") var key=(EntityDataAccessor<Integer>)move.get(null);
        boss.getEntityData().set(key,AukaBoss.Move.IDLE.ordinal());
        field(boss,"nextAttack",helper.getLevel().getGameTime()-1);
    }
    @GameTest(template="empty") public static void registryAndNeutralState(GameTestHelper helper){
        var boss=boss(helper);
        helper.assertTrue(boss.getMaxHealth()==1300F,"1300 HP attribute");
        helper.assertTrue(boss.form()==AukaBoss.Form.HUT_SPIRIT&&!boss.active(),"unprovoked aggression");
        helper.assertTrue(AukaContent.AUKA_EGG.get() instanceof SpawnEggItem,"test egg");
        helper.assertTrue(ModEffects.AUKA_FROST.get()!=null&&ModEffects.AUKA_FROST_BLOOD.get()!=null,"missing effects");
        helper.succeed();
    }
    @GameTest(template="empty") public static void declineAndNonceValidation(GameTestHelper helper)throws Exception{
        var p=player(helper);var boss=boss(helper);var token=invite(helper,boss,p);
        helper.assertTrue(!boss.decide(p,UUID.randomUUID(),true),"forged token accepted");
        helper.assertTrue(boss.decide(p,token,false),"decline rejected");
        helper.assertTrue(!boss.active()&&boss.getTarget()==null,"decline caused aggression");
        token=invite(helper,boss,p);
        helper.assertTrue(boss.decide(p,token,true),"accept rejected");
        helper.assertTrue(boss.active()&&boss.victim()==p,"challenge owner incorrect");
        helper.assertTrue(!boss.decide(p,token,true),"double-accept");
        helper.succeed();
    }
    @GameTest(template="empty") public static void phaseThresholdAndFixedWolfTimer(GameTestHelper helper)throws Exception{
        var p=player(helper);var boss=boss(helper);
        helper.assertTrue(boss.decide(p,invite(helper,boss,p),true),"trial start");
        boss.setHealth(710);
        boss.tick();
        helper.assertTrue(boss.form()==AukaBoss.Form.STALKER,"phase threshold");
        idle(helper,boss);
        field(boss,"wolfReady",helper.getLevel().getGameTime()-1);
        boss.tick();
        helper.assertTrue(boss.form()==AukaBoss.Form.WHITE_WOLF,"wolf did not transform");
        helper.assertTrue(Math.abs(boss.getBbWidth()-2.05F)<.02,"wolf hitbox");
        field(boss,"wolfUntil",helper.getLevel().getGameTime()+30);
        boss.setHealth(400);boss.tick();
        helper.assertTrue(boss.wolf(),"health hit ended wolf form early");
        field(boss,"wolfUntil",helper.getLevel().getGameTime()-1);
        boss.tick();
        helper.assertTrue(boss.form()==AukaBoss.Form.STALKER,"timer did not restore stalker");
        helper.succeed();
    }
    @GameTest(template="empty") public static void frostIsCapped(GameTestHelper helper){
        var p=player(helper);
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.AUKA_FROST,120));
        helper.assertTrue(p.hasEffect(ModEffects.AUKA_FROST),"frost not registered");
        p.removeEffect(ModEffects.AUKA_FROST);
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.AUKA_FROST_BLOOD,120));
        helper.assertTrue(p.hasEffect(ModEffects.AUKA_FROST_BLOOD),"frostblood not applied");
        helper.assertTrue(ModEffects.AUKA_FROST_BLOOD.get().shouldApplyEffectTickThisTick(80,0),"wrong pulse cadence");
        helper.assertTrue(!ModEffects.AUKA_FROST_BLOOD.get().shouldApplyEffectTickThisTick(79,0),"damage repeated every tick");
        helper.succeed();
    }
    @GameTest(template="empty") public static void encounterSaveAndLoad(GameTestHelper helper)throws Exception{
        var p=player(helper);var boss=boss(helper);
        helper.assertTrue(boss.decide(p,invite(helper,boss,p),true),"trial did not start");
        boss.setHealth(690);boss.tick();
        CompoundTag tag=new CompoundTag();boss.saveWithoutId(tag);
        var loaded=AukaContent.AUKA.get().create(helper.getLevel());
        loaded.load(tag);
        helper.assertTrue(loaded.active()&&loaded.form()==AukaBoss.Form.STALKER,"encounter lost after reload");
        helper.assertTrue(loaded.victim()==p,"challenge player was not restored");
        helper.assertTrue(loaded.getHealth()==690,"health changed across reload");
        helper.succeed();
    }
    private AukaGameTests(){}
}
