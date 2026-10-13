package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

/** Read-only commands inspect native starts in already loaded chunks. They never generate a city. */
@EventBusSubscriber(modid="slavicmyths")
public final class ChudCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        var city=Commands.literal("city").requires(s->s.hasPermission(2));
        city.then(Commands.literal("locate").executes(c->locate(c.getSource(),128)).then(Commands.argument("radius",IntegerArgumentType.integer(16,512)).executes(c->locate(c.getSource(),IntegerArgumentType.getInteger(c,"radius")))));
        for(String action:List.of("info","debug_plan"))city.then(Commands.literal(action).executes(c->info(c.getSource(),null)).then(Commands.argument("city_id",StringArgumentType.word()).executes(c->info(c.getSource(),StringArgumentType.getString(c,"city_id")))));
        city.then(Commands.literal("validate").executes(c->validate(c.getSource(),null)).then(Commands.argument("city_id",StringArgumentType.word()).executes(c->validate(c.getSource(),StringArgumentType.getString(c,"city_id")))));
        var tp=Commands.literal("tp");var identified=Commands.argument("city_id",StringArgumentType.word()).executes(c->teleport(c.getSource(),ChudPlan.Role.ENTRANCE,StringArgumentType.getString(c,"city_id")));
        for(var role:ChudPlan.Role.values()){tp.then(Commands.literal(role.name().toLowerCase(Locale.ROOT)).executes(c->teleport(c.getSource(),role,null)));identified.then(Commands.literal(role.name().toLowerCase(Locale.ROOT)).executes(c->teleport(c.getSource(),role,StringArgumentType.getString(c,"city_id"))));}city.then(tp.then(identified));
        var root=e.getDispatcher().register(Commands.literal("slavicmyths").then(city));
        e.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("debug").then(Commands.literal("chud_city").requires(s->s.hasPermission(2)).redirect(root.getChild("city")))));
    }
    private static List<ChudPiece> loaded(ServerLevel level,BlockPos center,int radius){
        var result=new ArrayList<ChudPiece>();var seen=new HashSet<String>();
        Structure type=level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));
        if(type==null)return result;
        int cx=center.getX()>>4,cz=center.getZ()>>4,r=(radius+15)>>4;
        for(int x=cx-r;x<=cx+r;x++)for(int z=cz-r;z<=cz+r;z++){
            var chunk=level.getChunkSource().getChunkNow(x,z);if(chunk==null)continue;
            var start=chunk.getStartForStructure(type);if(start==null||!start.isValid())continue;
            for(var piece:start.getPieces())if(piece instanceof ChudPiece p&&seen.add(p.plan.cityId()))result.add(p);
        }
        result.sort(Comparator.comparingDouble(p->p.plan.anchor.distSqr(center)));return result;
    }
    private static ChudPiece nearest(CommandSourceStack source,String cityId){var list=loaded(source.getLevel(),BlockPos.containing(source.getPosition()),512);if(cityId!=null)list.removeIf(p->!p.plan.cityId().equals(cityId));if(list.isEmpty()){source.sendFailure(Component.literal("Город Чуди не найден в загруженных чанках радиуса 512; новые чанки не генерировались."));return null;}return list.getFirst();}
    private static int locate(CommandSourceStack s,int radius){var ps=loaded(s.getLevel(),BlockPos.containing(s.getPosition()),radius);if(ps.isEmpty()){s.sendSuccess(()->Component.literal("В загруженных чанках города нет. Запускается ограниченный поиск кандидатов новых чанков."),false);return ChudSearchCommands.searchCity(s);}var p=ps.getFirst().plan;var entry=p.world(p.points.get(ChudPlan.Role.ENTRANCE));s.sendSuccess(()->Component.literal(p.cityId()+" вход "+entry.toShortString()+"; радиус загруженных стартов "+radius),false);return 1;}
    private static int info(CommandSourceStack s,String cityId){var piece=nearest(s,cityId);if(piece==null)return 0;var p=piece.plan;s.sendSuccess(()->Component.literal("CityId="+p.cityId()+"; world_seed="+s.getLevel().getSeed()+"; dimension="+s.getLevel().dimension().location()+"; anchor="+p.anchor.toShortString()+"; halls="+p.majorHalls()+"; layout="+p.layout()+"; levels="+(p.variant==0?2:3)+"; homes="+p.homes.size()+"; bounds="+p.bounds()+"; connectors="+p.connectors.size()+"; unique roles=1 each; futureGate="+p.world(p.points.get(ChudPlan.Role.GATE)).toShortString()+"; mineGateStatus="+(piece.abandoned==null?"LEGACY_GATE_SEALED":"ATTACHED_OPEN")+"; mineGate="+p.mineGate()+"; generationVersion=1; rooms="+p.halls+"; входной поворот="+p.entranceBend),false);return 1;}
    private static int validate(CommandSourceStack s,String cityId){var piece=nearest(s,cityId);if(piece==null)return 0;piece.plan.validate();var result=ChudNavigation.inspect(s.getLevel(),piece.plan);s.sendSuccess(()->Component.literal("Plan invariants=PASS; conservative boundary gap >=2298; actual paths="+result.status()+"; nodes="+result.visited()+"; unreachable_targets="+result.unreachable()+"; неизвестные чанки не загружались."),false);return result.status().equals("PASS")?1:0;}
    static boolean safe(ServerLevel level,BlockPos p){return level.hasChunkAt(p)&&level.getWorldBorder().isWithinBounds(p)&&level.getBlockState(p).getCollisionShape(level,p).isEmpty()&&level.getBlockState(p.above()).getCollisionShape(level,p.above()).isEmpty()&&!level.getBlockState(p.below()).getCollisionShape(level,p.below()).isEmpty()&&level.getBlockState(p.below()).getCollisionShape(level,p.below()).max(net.minecraft.core.Direction.Axis.Y)<=1.01&&level.getBlockState(p).getFluidState().isEmpty()&&level.getBlockState(p.above()).getFluidState().isEmpty()&&level.getBlockState(p.below()).getFluidState().isEmpty();}
    private static int teleport(CommandSourceStack s,ChudPlan.Role role,String cityId)throws com.mojang.brigadier.exceptions.CommandSyntaxException{var piece=nearest(s,cityId);if(piece==null)return 0;var p=piece.plan.world(piece.plan.points.get(role));if(!safe(s.getLevel(),p)){s.sendFailure(Component.literal("Цель не загружена или небезопасна; телепортация отклонена."));return 0;}s.getPlayerOrException().teleportTo(s.getLevel(),p.getX()+.5,p.getY(),p.getZ()+.5,Set.of(),0,0);return 1;}
    private ChudCommands(){}
}
