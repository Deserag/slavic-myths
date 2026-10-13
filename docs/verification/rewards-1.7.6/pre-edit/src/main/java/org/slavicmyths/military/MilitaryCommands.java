package org.slavicmyths.military;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slavicmyths.village.VillageRoles;
@EventBusSubscriber(modid="slavicmyths")
public final class MilitaryCommands {
 @SubscribeEvent public static void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("slavicmyths")
  .then(Commands.literal("test").requires(s->s.hasPermission(2)).then(Commands.literal("guard").executes(c->{var s=c.getSource();BlockPos pos=BlockPos.containing(s.getPosition()).relative(s.getEntity()==null?Direction.NORTH:s.getEntity().getDirection(),3);s.getLevel().setBlock(pos,Military.TABLE.get().defaultBlockState(),3);var v=EntityType.VILLAGER.create(s.getLevel());v.moveTo(pos.getX()+1.5,pos.getY(),pos.getZ()+.5,0,0);v.setVillagerData(v.getVillagerData().setProfession(VillageRoles.ROLES.get("druzhinnik").get()));v.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.JOB_SITE,GlobalPos.of(s.getLevel().dimension(),pos));s.getLevel().getPoiManager().take(h->h.is(VillageRoles.JOBS.get("druzhinnik").getKey()),(h,p)->p.equals(pos),pos,1);v.refreshBrain(s.getLevel());return s.getLevel().addFreshEntity(v)?1:0;}))
   .then(Commands.literal("bandit_raid").executes(c->{var s=c.getSource();return BanditRaids.get(s.getLevel()).start(s.getLevel(),BlockPos.containing(s.getPosition()),UUID.randomUUID(),true)?1:0;})))
  .then(Commands.literal("place").requires(s->s.hasPermission(2)).then(Commands.literal("barracks").executes(c->{var s=c.getSource();var pos=BlockPos.containing(s.getPosition()).offset(4,-1,4);var template=s.getLevel().getStructureManager().getOrCreate(VillageRoles.id("military/barracks"));var settings=new StructurePlaceSettings().addProcessor(JigsawReplacementProcessor.INSTANCE);return template.placeInWorld(s.getLevel(),pos,pos,settings,s.getLevel().random,3)?1:0;})))
  .then(Commands.literal("locate").requires(s->s.hasPermission(2)).then(Commands.literal("barracks").executes(c->{var s=c.getSource();var registry=s.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);List<Structure> types=new ArrayList<>();for(String biome:List.of("plains","taiga","snowy","savanna","desert")){var type=registry.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace("village_"+biome));if(type!=null)types.add(type);}return org.slavicmyths.worldgen.BoundedStructureSearch.start(s,types,start->{for(var piece:start.getPieces())if(piece instanceof PoolElementStructurePiece p&&p.getElement().toString().matches(".*slavicmyths:(military/barracks|village/[^/]+/defense/druzhinnik_barracks_01).*" ))return p.getBoundingBox().getCenter();return null;},p->p!=null,p->s.sendSuccess(()->Component.translatable("military.slavicmyths.location",p.getX(),p.getY(),p.getZ()),false));})))
  .then(Commands.literal("debug").requires(s->s.hasPermission(2))
   .then(Commands.literal("guard").executes(c->{var s=c.getSource();var v=s.getLevel().getEntitiesOfClass(Villager.class,new net.minecraft.world.phys.AABB(s.getPosition(),s.getPosition()).inflate(16),Military::guard).stream().min(Comparator.comparingDouble(x->x.distanceToSqr(s.getPosition()))).orElse(null);if(v==null)return 0;String info="UUID="+v.getUUID()+" role=druzhinnik archetype="+v.getData(Military.ARCHETYPE)+" weapon="+GuardBehavior.weapon(Math.max(0,v.getData(Military.ARCHETYPE)))+" shield="+GuardBehavior.shield(v.getData(Military.ARCHETYPE))+" patrolRadius="+Military.PATROL_RADIUS+" hp="+v.getHealth()+" active="+v.getBrain().getActiveNonCoreActivity()+" pathDone="+v.getNavigation().isDone()+" state="+v.getPersistentData();s.sendSuccess(()->Component.translatable("military.slavicmyths.debug",info),false);return 1;}))
   .then(Commands.literal("military_quests").executes(c->{var p=c.getSource().getPlayerOrException();c.getSource().sendSuccess(()->Component.translatable("military.slavicmyths.debug",MilitaryQuests.snapshot(p,p.blockPosition())),false);return 1;}))
   .then(Commands.literal("bandit_raid").executes(c->{var s=c.getSource();s.sendSuccess(()->Component.translatable("military.slavicmyths.debug",BanditRaids.get(s.getLevel()).inspectNearest(BlockPos.containing(s.getPosition()))),false);return 1;}))));}
}
