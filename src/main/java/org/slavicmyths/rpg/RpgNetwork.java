package org.slavicmyths.rpg;
import java.util.Optional;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.*;
import net.minecraftforge.fml.network.simple.SimpleChannel;
public final class RpgNetwork {
 private static final SimpleChannel NET=NetworkRegistry.newSimpleChannel(new ResourceLocation("slavicmyths","rpg"),()->"1","1"::equals,"1"::equals);
 public static void register(){
  NET.registerMessage(0,Sync.class,(m,b)->{b.writeNbt(m.data);b.writeVarInt(m.xp);},b->new Sync(b.readNbt(),b.readVarInt()),(m,c)->{c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->org.slavicmyths.client.RpgClient.sync(m.data,m.xp)));c.get().setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  NET.registerMessage(1,Activate.class,(m,b)->{},b->new Activate(),(m,c)->{ServerPlayerEntity p=c.get().getSender();c.get().enqueueWork(()->{if(p!=null&&p.isAlive()&&!p.isSpectator()&&PathData.ready(p,"request",4))Abilities.activate(p);});c.get().setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_SERVER));
 }
 public static void sync(ServerPlayerEntity p){NET.send(PacketDistributor.PLAYER.with(()->p),new Sync(PathData.data(p).copy(),p.experienceLevel));}
 public static void activate(){NET.sendToServer(new Activate());}
 private static final class Sync{final CompoundNBT data;final int xp;Sync(CompoundNBT d,int x){data=d==null?new CompoundNBT():d;xp=x;}}
 private static final class Activate{}
}
