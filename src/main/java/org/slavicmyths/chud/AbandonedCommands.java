package org.slavicmyths.chud;
import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.mojang.brigadier.arguments.StringArgumentType;
/** Read-only loaded-start diagnostics; never requests generation or force-loads a complex. */
@EventBusSubscriber(modid="slavicmyths")
public final class AbandonedCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        var root=Commands.literal("abandoned").requires(s->s.hasPermission(2));var locate=Commands.literal("locate").executes(c->locate(c.getSource(),null));
        for(var kind:AbandonedPlan.Kind.values())locate.then(Commands.literal(label(kind)).executes(c->locate(c.getSource(),kind)));root.then(locate);
        for(var name:List.of("info","inspect","debug","validate")){var action=Commands.literal(name).executes(c->info(c.getSource(),null,name.equals("validate")||name.equals("debug")));action.then(Commands.argument("abandoned_id",StringArgumentType.word()).executes(c->info(c.getSource(),StringArgumentType.getString(c,"abandoned_id"),name.equals("validate")||name.equals("debug"))));root.then(action);}
        var tp=Commands.literal("tp");var identified=Commands.argument("abandoned_id",StringArgumentType.word());
        for(String landmark:List.of("entrance","quarter","gallery","shaft","labyrinth","gate")){
            tp.then(Commands.literal(landmark).executes(c->teleport(c.getSource(),null,landmark)));
            identified.then(Commands.literal(landmark).executes(c->teleport(c.getSource(),StringArgumentType.getString(c,"abandoned_id"),landmark)));
        }
        root.then(tp.then(identified));event.getDispatcher().register(Commands.literal("slavicmyths").then(root));
    }
    private record Known(AbandonedPlan plan,ChudPlan city){}
    private static String label(AbandonedPlan.Kind kind){return switch(kind){case ATTACHED_SMALL->"attached";case INDEPENDENT_MEDIUM->"medium";case INDEPENDENT_GREAT->"great";};}
    private static List<Known> loaded(ServerLevel level,BlockPos center){
        var found=new ArrayList<Known>();var seen=new HashSet<String>();var registry=level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var city=registry.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));var independent=registry.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_abandoned"));
        int cx=center.getX()>>4,cz=center.getZ()>>4;
        for(int x=cx-32;x<=cx+32;x++)for(int z=cz-32;z<=cz+32;z++){
            var chunk=level.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;
            for(var structure:Arrays.asList(city,independent)){if(structure==null)continue;var start=chunk.getStartForStructure(structure);if(start==null||!start.isValid())continue;
                for(var piece:start.getPieces()){
                    AbandonedPlan p=null;ChudPlan parent=null;if(piece instanceof AbandonedPiece b)p=b.plan;else if(piece instanceof ChudPiece a&&a.abandoned!=null){p=a.abandoned;parent=a.plan;}
                    if(p!=null&&seen.add(p.id()))found.add(new Known(p,parent));
                }
            }
        }
        found.sort(Comparator.comparingDouble(k->k.plan.anchor.distSqr(center)));return found;
    }
    private static Known nearest(CommandSourceStack source,String id){var entries=loaded(source.getLevel(),BlockPos.containing(source.getPosition()));if(id!=null)entries.removeIf(k->!k.plan.id().equals(id)&&!k.plan.rootId.equals(id));if(entries.isEmpty()){source.sendFailure(Component.literal("Заброшенный комплекс не найден в загруженных чанках радиуса512; /locate structure slavicmyths:chud_abandoned ищет естественные кандидаты. Чанки не создавались."));return null;}return entries.getFirst();}
    private static int locate(CommandSourceStack source,AbandonedPlan.Kind kind){var entries=loaded(source.getLevel(),BlockPos.containing(source.getPosition()));if(kind!=null)entries.removeIf(k->k.plan.kind!=kind);if(entries.isEmpty()){source.sendSuccess(()->Component.literal("В загруженных чанках комплекса нет. Запускается ограниченный поиск кандидатов новых чанков."),false);return ChudSearchCommands.searchAbandoned(source,kind);}var p=entries.getFirst().plan;source.sendSuccess(()->Component.literal("dimension="+source.getLevel().dimension().location()+" root="+p.rootId+" abandoned="+p.id()+" type="+p.kind+" anchor="+p.anchor.toShortString()+" levels="+p.levels()+" union="+p.envelope()),false);return 1;}
    private static int info(CommandSourceStack source,String id,boolean validate){var known=nearest(source,id);if(known==null)return 0;var p=known.plan;
        source.sendSuccess(()->Component.literal("rootComplexId="+p.rootId+"; abandonedId="+p.id()+"; type="+p.kind+"; generationVersion=2; shaftProfile="+p.shaftProfile+"; levels="+p.levels()+"; union="+p.envelope()+"; same-root components exempt from spacing="+(known.city!=null)+"; independent gap >=2298; rooms="+p.chambers+"; mazeNodes="+p.mazeNodes.size()+"; mazeEdges="+p.mazeEdges+"; sealedGate="+p.sealedGate+"; futureGateId="+p.futureGateId()+"; futureGateFloor="+(p.sealedGate?p.futureGateFloor():"none")+"; legacyGateSealed=false; joins="+(known.city==null?"none":known.city.mineGate())+"; routes="+p.routes.stream().map(AbandonedPlan.Route::id).toList()),false);
        if(!validate)return 1;var targets=new LinkedHashMap<String,BlockPos>();p.targets.forEach((name,pos)->targets.put(name,p.world(pos)));
        if(known.city!=null)known.city.points.forEach((role,pos)->targets.put("CITY_A:"+role,known.city.world(pos)));
        var result=ChudNavigation.inspect(source.getLevel(),p.nativeBounds(),p.world(p.targets.get("entrance")),targets);source.sendSuccess(()->Component.literal("Actual paths="+result.status()+"; visited="+result.visited()+"; missing="+result.unreachable()+"; unloaded chunks were not requested"),false);return result.status().equals("PASS")?1:0;
    }
    private static String target(AbandonedPlan p,String name){if(name.equals("entrance"))return name;var role=switch(name){case "quarter"->AbandonedPlan.Role.QUARTER;case "gallery"->AbandonedPlan.Role.GALLERY;case "shaft"->AbandonedPlan.Role.SHAFT;case "labyrinth"->AbandonedPlan.Role.LABYRINTH;default->AbandonedPlan.Role.SEALED_GATE;};if(role==AbandonedPlan.Role.LABYRINTH)return p.mazeNodes.isEmpty()?null:"maze_0";return p.chambers.stream().filter(h->h.role()==role).map(AbandonedPlan.Chamber::id).findFirst().orElse(null);}
    private static int teleport(CommandSourceStack source,String id,String landmark)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var known=nearest(source,id);if(known==null)return 0;var name=target(known.plan,landmark);if(name==null){source.sendFailure(Component.literal("У этого типа нет landmark "+landmark));return 0;}
        var p=known.plan.world(known.plan.targets.get(name));var world=source.getLevel();
        if(!ChudCommands.safe(world,p)){source.sendFailure(Component.literal("Точка не загружена или небезопасна; TP отклонён."));return 0;}
        source.getPlayerOrException().teleportTo(world,p.getX()+.5,p.getY(),p.getZ()+.5,Set.of(),0,0);return 1;
    }
    private AbandonedCommands(){}
}
