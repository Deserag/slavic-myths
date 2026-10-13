package org.slavicmyths.gorodishche;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Administrator-only explicit generation; no natural rarity predicates or permanent tickets. */
@EventBusSubscriber(modid="slavicmyths")
public final class CityCommands {
 private static final TicketType<BlockPos> TICKET=TicketType.create("slavicmyths_gorodishche_showcase",Comparator.comparingLong(BlockPos::asLong));
 private static final Map<ServerLevel,String> LAST=new WeakHashMap<>();
 @SubscribeEvent public static void register(RegisterCommandsEvent e){var test=Commands.literal("gorodishche_showcase").executes(c->showcase(c.getSource(),climate(c.getSource())));for(String p:List.of("temperate","cold","warm"))test.then(Commands.literal(p).executes(c->showcase(c.getSource(),p)));e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("test").requires(s->s.hasPermission(2)).then(test)).then(Commands.literal("debug").requires(s->s.hasPermission(2)).then(Commands.literal("gorodishche").executes(c->{c.getSource().sendSuccess(()->Component.literal(LAST.getOrDefault(c.getSource().getLevel(),"В этом сеансе городище ещё не создавалось; /locate structure slavicmyths:gorodishche")),false);return 1;}))));if(e.getDispatcher().getRoot().getChild("sm")==null)e.getDispatcher().register(Commands.literal("sm").redirect(e.getDispatcher().getRoot().getChild("slavicmyths")));}
 private static String climate(CommandSourceStack s){float temperature=s.getLevel().getBiome(BlockPos.containing(s.getPosition())).value().getBaseTemperature();return temperature<.25F?"cold":temperature>1F?"warm":"temperate";}
 public static int showcase(CommandSourceStack source,String climate){var level=source.getLevel();var start=BlockPos.containing(source.getPosition()).offset(8,0,8);Set<ChunkPos>held=new LinkedHashSet<>();try{
  if(!List.of("temperate","cold","warm").contains(climate))throw new IllegalStateException("INVALID_PALETTE "+climate);
  level.getChunk(start.getX()>>4,start.getZ()>>4);int floor=org.slavicmyths.worldgen.LandTerrain.ground(level,start.getX(),start.getZ());
  var plan=CityPlan.make(climate,125176,start.getX(),start.getZ(),(x,z)->floor,true);CityPlacement.validate(plan,level.getStructureManager(),level,level.getServer().getResourceManager());
  if(!(level.getWorldBorder().isWithinBounds(new BlockPos(plan.bounds().minX(),floor,plan.bounds().minZ())) && level.getWorldBorder().isWithinBounds(new BlockPos(plan.bounds().maxX(),floor,plan.bounds().maxZ()))))throw new IllegalStateException("OUT_OF_WORLD_BORDER "+plan.bounds());
  // Chunk preparation happens after immutable plan validation and before the first block edit.
  for(int cx=plan.bounds().minX()>>4;cx<=plan.bounds().maxX()>>4;cx++)for(int cz=plan.bounds().minZ()>>4;cz<=plan.bounds().maxZ()>>4;cz++){var chunk=new ChunkPos(cx,cz);held.add(chunk);level.getChunkSource().addRegionTicket(TICKET,chunk,2,start);level.getChunk(cx,cz);}
  CityPlacement.validateDestination(level,plan);
  var population=CityPlacement.place(level,plan,125176);int villagers=population.villagers(),guards=population.guards();
  org.slavicmyths.worldgen.ManualStructureRecords.get(level).add(UUID.randomUUID(),"gorodishche",0,plan.origin().offset(87,1,175),plan.bounds());
  long walls=plan.placements().stream().filter(p->p.building().name().startsWith("wall_")).count();long inner=plan.placements().stream().filter(p->!p.building().category().equals("defense")).count();
  String summary="Городище создано: "+inner+"/"+inner+" внутренних построек, стен "+walls+", жилых домов "+plan.residential()+", рабочих домов "+plan.professions()+", жителей "+villagers+", дружинников "+guards+", палитра "+climate+", план "+plan.layout()+", начало "+plan.origin().toShortString()+", границы "+plan.bounds()+", кроватей "+plan.beds()+", предупреждений нет.";LAST.put(level,summary);source.sendSuccess(()->Component.literal(summary),true);return (int)inner;
 }catch(Exception ex){String reason="Городище: "+ex.getMessage();LAST.put(level,reason);source.sendFailure(Component.literal(reason));return 0;}finally{for(var chunk:held)level.getChunkSource().removeRegionTicket(TICKET,chunk,2,start);}}
 private CityCommands(){}
}
