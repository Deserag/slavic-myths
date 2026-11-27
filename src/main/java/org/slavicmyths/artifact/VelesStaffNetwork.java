package org.slavicmyths.artifact;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public final class VelesStaffNetwork {
 public record SteppeSummon() implements CustomPacketPayload {
  public static final Type<SteppeSummon> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("slavicmyths","steppe_summon"));
  public static final StreamCodec<RegistryFriendlyByteBuf,SteppeSummon> CODEC=StreamCodec.unit(new SteppeSummon());
  @Override public Type<SteppeSummon> type(){return TYPE;}
 }
 public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToServer(SteppeSummon.TYPE,SteppeSummon.CODEC,(intent,c)->{if(c.player() instanceof ServerPlayer p)VelesStaffItem.steppeSkill(p);});}
 private VelesStaffNetwork(){}
}
