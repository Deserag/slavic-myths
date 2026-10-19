package org.slavicmyths.kuzya;

import java.util.UUID;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slavicmyths.yaga.YagaData;

/** Persistent canonical UUID, no respawn loop. Repair is an explicit administrator operation. */
@EventBusSubscriber(modid="slavicmyths")
public final class KuzyaResidence {
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(!(e.getLevel() instanceof ServerLevel w)||!(e.getEntity() instanceof KuzyaEntity k)||k.homeHutId==null)return;YagaData d=YagaData.get(w);if(k.homeHutId.equals(d.hutId)&&d.kuzya!=null&&!d.kuzya.equals(k.getUUID()))e.setCanceled(true);}
    public static void commands(org.slavicmyths.commands.CommandTree tree){tree.add(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(s->s.hasPermission(2)).then(Commands.literal("yaga").then(Commands.literal("kuzya")
        .then(Commands.literal("status").executes(c->{ServerLevel w=c.getSource().getServer().getLevel(Level.OVERWORLD);YagaData d=YagaData.get(w);String info="home="+d.anchor+" hut="+d.hutId+" resident="+d.kuzya+" interior="+d.interiorRevision;var found=d.kuzya==null?null:w.getEntity(d.kuzya);if(found instanceof KuzyaEntity k)info+=" clip="+k.clip()+" pos="+k.blockPosition()+" bounds="+k.homeBounds();String text=info;c.getSource().sendSuccess(()->Component.literal(text),false);return 1;}))
        .then(Commands.literal("repair").executes(c->{ServerLevel w=c.getSource().getServer().getLevel(Level.OVERWORLD);YagaData d=YagaData.get(w);if(!d.placed||d.anchor==null){c.getSource().sendFailure(Component.translatable("yaga.fail.home"));return 0;}if(!w.hasChunksAt(d.anchor.offset(-5,0,-6),d.anchor.offset(5,8,6))){c.getSource().sendFailure(Component.translatable("kuzya.repair.unloaded"));return 0;}if(d.kuzya!=null&&w.getEntity(d.kuzya)!=null){c.getSource().sendFailure(Component.translatable("kuzya.repair.exists"));return 0;}
            var p=d.anchor.offset(0,4,0);if(!w.noCollision(new net.minecraft.world.phys.AABB(p.getX()+.24,p.getY(),p.getZ()+.24,p.getX()+.76,p.getY()+.7,p.getZ()+.76))||w.getBlockState(p.below()).getCollisionShape(w,p.below()).isEmpty()||!w.getFluidState(p).isEmpty()){c.getSource().sendFailure(Component.translatable("kuzya.repair.blocked"));return 0;}
            KuzyaEntity k=KuzyaContent.ENTITY.get().create(w);if(k==null)return 0;if(d.hutId==null)d.hutId=UUID.nameUUIDFromBytes((w.dimension().location()+":"+d.anchor.asLong()).getBytes(java.nio.charset.StandardCharsets.UTF_8));UUID previous=d.kuzya;d.kuzya=k.getUUID();k.bind(d.anchor,d.hutId);k.renovated=d.interiorRevision==1;k.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,180,0);if(!w.addFreshEntity(k)){d.kuzya=previous;return 0;}d.setDirty();c.getSource().sendSuccess(()->Component.translatable("kuzya.repair.done",p.toShortString()),false);return 1;}
        ))))));}
    private KuzyaResidence(){}
}
