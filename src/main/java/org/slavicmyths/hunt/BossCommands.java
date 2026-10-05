package org.slavicmyths.hunt;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BossCommands{
 @SubscribeEvent public static void register(RegisterCommandsEvent e){com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> root=Commands.literal("boss");for(BossKind k:BossKind.values())root.then(Commands.literal(k.id).then(Commands.literal("summon").executes(c->BossRitualItem.summon(c.getSource().getPlayerOrException(),k,true)?1:0)).then(Commands.literal("locate").requires(source->source.hasPermission(2)).executes(c->locate(c.getSource(),k))));root.then(Commands.literal("clear").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();ServerLevel w=p.server.getLevel(Level.OVERWORLD);HuntRecords d=HuntRecords.get(w);HuntRecords.Boss b=d.bosses.get(d.bossOwners.get(p.getUUID()));if(b!=null&&p.getUUID().equals(b.owner)){b.active=false;d.setDirty();net.minecraft.world.entity.Entity mob=b.target==null?null:w.getEntity(b.target);if(mob instanceof WorldBoss&&p.getUUID().equals(((WorldBoss)mob).bossOwner))mob.discard();}c.getSource().sendSuccess(()->Component.translatable("boss.cleared"),false);return 1;}));e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("dev").requires(source->source.hasPermission(2)).then(root)));}
 private static int locate(CommandSourceStack s,BossKind kind)throws com.mojang.brigadier.exceptions.CommandSyntaxException{ServerPlayer p=s.getPlayerOrException();ServerLevel w=p.server.getLevel(Level.OVERWORLD);HuntRecords d=HuntRecords.get(w);HuntRecords.Boss nearest=null;double best=Double.MAX_VALUE;for(HuntRecords.Boss b:d.bosses.values())if(b.natural&&b.kind==kind){double dx=b.anchor.getX()-p.getX(),dz=b.anchor.getZ()-p.getZ(),distance=dx*dx+dz*dz;if(distance<best){best=distance;nearest=b;}}if(nearest==null){s.sendFailure(Component.translatable("boss.not_found"));return 0;}HuntRecords.Boss selected=nearest;s.sendSuccess(()->Component.translatable("boss.located",Component.translatable("entity.slavicmyths."+kind.id),selected.anchor.toShortString(),selected.active),false);return 1;}
}
