package org.slavicmyths.flight;
import java.util.Optional;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.*;
import net.minecraftforge.fml.network.simple.SimpleChannel;
public final class FlightNetwork {
 private static final SimpleChannel NET=NetworkRegistry.newSimpleChannel(new ResourceLocation("slavicmyths","flight"),()->"1","1"::equals,"1"::equals);
 public static void register(){NET.registerMessage(0,Input.class,(m,b)->{b.writeFloat(m.f);b.writeFloat(m.s);b.writeFloat(m.u);b.writeFloat(m.yaw);b.writeBoolean(m.brake);b.writeBoolean(m.cargo);},b->new Input(b.readFloat(),b.readFloat(),b.readFloat(),b.readFloat(),b.readBoolean(),b.readBoolean()),(m,c)->{ServerPlayerEntity p=c.get().getSender();c.get().enqueueWork(()->{if(p!=null&&p.getVehicle() instanceof FlyingVessel){FlyingVessel v=(FlyingVessel)p.getVehicle();if(v.pilot()!=p)return;long now=p.level.getGameTime();if(p.getPersistentData().getLong("SlavicFlightPacket")>now)return;p.getPersistentData().putLong("SlavicFlightPacket",now+2);v.input(p,m.f,m.s,m.u,m.yaw,m.brake);if(m.cargo)v.openCargo(p);}});c.get().setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_SERVER));}
 public static void send(float f,float s,float up,float yaw,boolean brake,boolean cargo){NET.sendToServer(new Input(f,s,up,yaw,brake,cargo));}
 private static final class Input{final float f,s,u,yaw;final boolean brake,cargo;Input(float f,float s,float u,float y,boolean b,boolean c){this.f=f;this.s=s;this.u=u;yaw=y;brake=b;cargo=c;}}
}
