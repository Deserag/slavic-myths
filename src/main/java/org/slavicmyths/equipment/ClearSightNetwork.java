package org.slavicmyths.equipment;

import java.util.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

/** Only helmet activation intent arrives from the client; target visibility is server-approved. */
public final class ClearSightNetwork {
    public static final TagKey<EntityType<?>> EXCLUDED=TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths","clear_sight_excluded"));
    public record Intent() implements CustomPacketPayload {
        public static final Type<Intent> TYPE=new Type<>(id("clear_sight_intent"));
        public static final StreamCodec<FriendlyByteBuf,Intent> CODEC=StreamCodec.of((b,p)->{},b->new Intent());
        public Type<Intent> type(){return TYPE;}
    }
    public record Target(int id,UUID uuid) { }
    public record Visible(ResourceLocation dimension,List<Target> targets) implements CustomPacketPayload {
        public static final Type<Visible> TYPE=new Type<>(id("clear_sight_visible"));
        public static final StreamCodec<FriendlyByteBuf,Visible> CODEC=StreamCodec.of((b,p)->{
            b.writeResourceLocation(p.dimension);b.writeVarInt(p.targets.size());for(var t:p.targets){b.writeVarInt(t.id);b.writeUUID(t.uuid);}
        },b->{var dimension=b.readResourceLocation();int size=b.readVarInt();if(size<0||size>512)throw new IllegalArgumentException("Sight target count");List<Target> targets=new ArrayList<>();for(int i=0;i<size;i++)targets.add(new Target(b.readVarInt(),b.readUUID()));return new Visible(dimension,List.copyOf(targets));});
        public Type<Visible> type(){return TYPE;}
    }
    public static final class Received extends Event { public final Visible payload;public Received(Visible payload){this.payload=payload;} }
    public static void register(RegisterPayloadHandlersEvent event) {
        var r=event.registrar("unique-equipment-1");
        r.playToServer(Intent.TYPE,Intent.CODEC,(payload,context)->{if(context.player() instanceof ServerPlayer p)activate(p);});
        r.playToClient(Visible.TYPE,Visible.CODEC,(payload,context)->NeoForge.EVENT_BUS.post(new Received(payload)));
    }
    public static boolean eligible(LivingEntity entity,ServerPlayer observer) {
        return entity!=observer && entity.isAlive() && !entity.isSpectator() && !(entity instanceof ArmorStand)
            && (entity instanceof Mob || entity instanceof net.minecraft.world.entity.player.Player)
            && !entity.getType().is(EXCLUDED) && (!(entity instanceof net.minecraft.world.entity.player.Player) || observer.server.isPvpAllowed());
    }
    public static void activate(ServerPlayer p) {
        if(!p.isAlive()||p.isSpectator()||!UniqueEquipmentEvents.wears(p,EquipmentSlot.HEAD,UniqueEquipment.CLEAR_SIGHT_HELMET.get()))return;
        var d=UniqueEquipmentEvents.saved(p);long now=UniqueEquipmentEvents.now(p);
        if(!UniqueEquipmentRules.ready(now,d.getLong("SightReady"))){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("unique.slavicmyths.sight_cooldown",(d.getLong("SightReady")-now+19)/20),true);return;}
        d.putLong("SightReady",now+UniqueEquipmentRules.SIGHT_COOLDOWN);
        var targets=p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(UniqueEquipmentRules.SIGHT_RADIUS),e->eligible(e,p)&&e.distanceToSqr(p)<=UniqueEquipmentRules.SIGHT_RADIUS*UniqueEquipmentRules.SIGHT_RADIUS)
            .stream().limit(512).map(e->new Target(e.getId(),e.getUUID())).toList();
        PacketDistributor.sendToPlayer(p,new Visible(p.level().dimension().location(),targets));
    }
    public static void send(){PacketDistributor.sendToServer(new Intent());}
    private static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
    private ClearSightNetwork() { }
}
