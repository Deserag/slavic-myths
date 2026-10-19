package org.slavicmyths.underground;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slavicmyths.chud.*;
@EventBusSubscriber(modid="slavicmyths")
public final class UndergroundCommands {
 public static void register(org.slavicmyths.commands.CommandTree tree){var root=Commands.literal("underground").requires(s->s.hasPermission(2));var spawn=Commands.literal("spawn");for(String kind:java.util.List.of("podzemnik","miner","kladovik","large_podzemnik"))spawn.then(Commands.literal(kind).executes(c->{var source=c.getSource();var w=source.getLevel();EncounterMob mob=kind.equals("kladovik")?UndergroundMobs.KLADOVIK.get().create(w):kind.equals("large_podzemnik")?UndergroundMobs.LARGE.get().create(w):UndergroundMobs.PODZEMNIK.get().create(w);if(mob==null)return 0;var p=net.minecraft.core.BlockPos.containing(source.getPosition().add(source.getRotation().y==0?2:0,0,2));if(mob instanceof Podzemnik m)m.variant(kind.equals("miner")?1:0);mob.bind(p,"");mob.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,0,0);if(!w.noCollision(mob)){source.sendFailure(Component.literal("Нет свободного места для этого моба."));return 0;}return w.addFreshEntity(mob)?1:0;}));root.then(spawn);
 root.then(Commands.literal("status").executes(c->{c.getSource().sendSuccess(()->Component.literal(EncounterPopulation.data(c.getSource().getLevel()).status()),false);return 1;}));
 root.then(Commands.literal("populate").executes(c->{var s=c.getSource();var w=s.getLevel();var center=net.minecraft.core.BlockPos.containing(s.getPosition());int count=0;var seen=new java.util.HashSet<Object>();for(int x=(center.getX()>>4)-16;x<=(center.getX()>>4)+16;x++)for(int z=(center.getZ()>>4)-16;z<=(center.getZ()>>4)+16;z++){var chunk=w.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;for(var start:chunk.getAllStarts().values())if(seen.add(start))for(var p:start.getPieces()){if(p instanceof AbandonedPiece a){EncounterPopulation.data(w).plan(w,a.plan);count++;}else if(p instanceof ChudPiece a&&a.abandoned!=null){EncounterPopulation.data(w).plan(w,a.abandoned);count++;}}}int found=count;s.sendSuccess(()->Component.literal("Известных Б: "+found+"; "+EncounterPopulation.data(w).status()+". Новые чанки не запрашивались, блоки не менялись."),false);return count;}));
 tree.add(Commands.literal("slavicmyths").then(root));}
 private UndergroundCommands(){}
}
