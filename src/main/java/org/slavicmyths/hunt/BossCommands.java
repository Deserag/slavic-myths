package org.slavicmyths.hunt;
import net.minecraft.command.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class BossCommands{
 @SubscribeEvent public static void register(RegisterCommandsEvent e){com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> root=Commands.literal("boss");for(BossKind k:BossKind.values())root.then(Commands.literal(k.id).then(Commands.literal("summon").executes(c->BossRitualItem.summon(c.getSource().getPlayerOrException(),k,true)?1:0)).then(Commands.literal("locate").executes(c->locate(c.getSource(),k))));root.then(Commands.literal("clear").executes(c->{ServerPlayerEntity p=c.getSource().getPlayerOrException();ServerWorld w=p.server.getLevel(World.OVERWORLD);HuntRecords d=HuntRecords.get(w);HuntRecords.Boss b=d.bosses.get(d.bossOwners.get(p.getUUID()));if(b!=null&&p.getUUID().equals(b.owner)){b.active=false;d.setDirty();net.minecraft.entity.Entity mob=b.target==null?null:w.getEntity(b.target);if(mob instanceof WorldBoss&&p.getUUID().equals(((WorldBoss)mob).bossOwner))mob.remove();}c.getSource().sendSuccess(new TranslationTextComponent("boss.cleared"),false);return 1;}));e.getDispatcher().register(Commands.literal("slavicmyths").requires(s->s.hasPermission(2)).then(Commands.literal("dev").then(root)));}
 private static int locate(CommandSource s,BossKind kind)throws com.mojang.brigadier.exceptions.CommandSyntaxException{ServerPlayerEntity p=s.getPlayerOrException();ServerWorld w=p.server.getLevel(World.OVERWORLD);HuntRecords d=HuntRecords.get(w);HuntRecords.Boss nearest=null;double best=Double.MAX_VALUE;for(HuntRecords.Boss b:d.bosses.values())if(b.natural&&b.kind==kind){double dx=b.anchor.getX()-p.getX(),dz=b.anchor.getZ()-p.getZ(),distance=dx*dx+dz*dz;if(distance<best){best=distance;nearest=b;}}if(nearest==null){s.sendFailure(new TranslationTextComponent("boss.not_found"));return 0;}s.sendSuccess(new TranslationTextComponent("boss.located",new TranslationTextComponent("entity.slavicmyths."+kind.id),nearest.anchor.toShortString(),nearest.active),false);return 1;}
}
