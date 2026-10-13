package org.slavicmyths.verify;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.skim.*;
@GameTestHolder("slavicmyths_skim_difficulty") @PrefixGameTestTemplate(false)
public final class SkimDifficulty {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}
    }
    @SuppressWarnings("unchecked") private static Map<UUID,ServerPlayer> online(ServerPlayer p){try{var f=net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");f.setAccessible(true);return (Map<UUID,ServerPlayer>)f.get(p.server.getPlayerList());}catch(Exception e){throw new RuntimeException(e);}}
    private static ServerPlayer player(GameTestHelper t){var w=t.getLevel();var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BogatyrTest"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);try{var f=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");f.setAccessible(true);f.setInt(p,0);}catch(Exception e){throw new RuntimeException(e);}p.setNoGravity(true);p.moveTo(t.absoluteVec(new Vec3(16,2,18)));p.setOnGround(true);p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(24);p.setHealth(24);w.addNewPlayer(p);online(p).put(p.getUUID(),p);return p;}
    private static void diamond(ServerPlayer p){p.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET));p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));p.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS));p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));p.doTick();if(p.getArmorValue()!=20)throw new AssertionError("Fixture armor not applied: "+p.getArmorValue());}
    private static void floor(GameTestHelper t){for(int x=0;x<48;x++)for(int z=0;z<48;z++)t.setBlock(new BlockPos(x,1,z),Blocks.STONE);}

 private static SkimBeastEntity beast(GameTestHelper t,ServerPlayer p){var b=SkimContent.SKIM.get().create(t.getLevel());b.moveTo(p.position().add(0,0,-4));b.setNoGravity(true);t.getLevel().addFreshEntity(b);b.setTarget(p);b.face(p);b.begin(SkimAction.EXHAUSTED);return b;}
 private static AlyoshaEntity hero(GameTestHelper t,ServerPlayer p){var a=SkimContent.ALYOSHA.get().create(t.getLevel());a.owner=p.getUUID();a.moveTo(p.position().add(3,0,0));a.setNoGravity(true);t.getLevel().addFreshEntity(a);HeroCalls.get(t.getLevel()).alyosha.put(p.getUUID(),a.getUUID());a.begin(SkimAction.ARRIVE);return a;}
 private static void clean(ServerPlayer p,Entity... entities){online(p).remove(p.getUUID());HeroCalls.get(p.serverLevel()).alyosha.remove(p.getUUID());org.slavicmyths.bogatyr.BogatyrLedger.get(p.serverLevel()).heroes.remove(p.getUUID());org.slavicmyths.marinka.WitchYard.get(p.serverLevel()).heroes.remove(p.getUUID());SkimValley.get(p.serverLevel()).valleys.values().removeIf(e->p.getUUID().equals(e.owner));p.discard();for(var e:entities)e.discard();}
 private static void damage(GameTestHelper t,String kind,double targetHp,net.minecraft.world.Difficulty difficulty,boolean resist){var p=player(t);diamond(p);var w=t.getLevel();w.getServer().setDifficulty(difficulty,true);var b=beast(t,p);if(resist)p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,200));float raw=SkimBeastEntity.raw(targetHp);p.hurt(b.damage(kind),raw);float actual=24-p.getHealth();double expected=targetHp*(resist?.8:1);if(difficulty==net.minecraft.world.Difficulty.EASY){double scaled=Math.min(raw/2+1,raw);expected=scaled*(.2+.01*scaled);}if(difficulty==net.minecraft.world.Difficulty.HARD){double scaled=raw*1.5;expected=scaled*(.2+.01*scaled);}System.out.println("SKIM_DAMAGE kind="+kind+" difficulty="+difficulty+" raw="+raw+" net="+actual+" resistance="+resist);t.assertTrue(Math.abs(actual-expected)<.035,"Damage "+actual+" expected "+expected);w.getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);clean(p,b);t.succeed();}
 @GameTest(template="empty",timeoutTicks=80) public static void scaledArmor(GameTestHelper t){var p=player(t);diamond(p);var b=beast(t,p);for(var difficulty:List.of(net.minecraft.world.Difficulty.EASY,net.minecraft.world.Difficulty.NORMAL,net.minecraft.world.Difficulty.HARD)){p.server.setDifficulty(difficulty,true);p.invulnerableTime=0;p.setHealth(24);p.hurt(b.damage("leap"),SkimBeastEntity.raw(7));double actual=24-p.getHealth();System.out.println("SKIM_DIFFICULTY "+difficulty+" diamondLeap="+actual);t.assertTrue(difficulty==net.minecraft.world.Difficulty.EASY?actual<7:difficulty==net.minecraft.world.Difficulty.HARD?actual>7:Math.abs(actual-7)<.03,"Difficulty not respected");}p.server.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);clean(p,b);t.succeed();}
}
