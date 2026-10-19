package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit operator excavation, with persisted starts and portal-room protection. */
@EventBusSubscriber(modid="slavicmyths")
public final class ChudGenerateCommands {
    @SubscribeEvent public static void register(RegisterCommandsEvent event){
        var city=Commands.literal("city").requires(s->s.hasPermission(2));
        city.then(Commands.literal("generate").executes(c->generate(c.getSource(),"city"))
            .then(Commands.literal("attached").executes(c->generate(c.getSource(),"attached"))));
        var abandoned=Commands.literal("abandoned").requires(s->s.hasPermission(2));
        var generate=Commands.literal("generate").executes(c->generate(c.getSource(),"medium"));
        for(String kind:List.of("medium","great"))generate.then(Commands.literal(kind).executes(c->generate(c.getSource(),kind)));
        abandoned.then(generate);
        event.getDispatcher().register(Commands.literal("slavicmyths").then(city).then(abandoned));
    }
    private static int generate(CommandSourceStack source,String kind){
        var level=source.getLevel();var owner=new ChunkPos(BlockPos.containing(source.getPosition()));
        var generator=level.getChunkSource().getGenerator();boolean above=generator instanceof FlatLevelSource;
        var c=new Structure.GenerationContext(level.registryAccess(),generator,generator.getBiomeSource(),level.getChunkSource().randomState(),level.getStructureManager(),level.getSeed(),owner,level,b->true);
        boolean city=kind.equals("city")||kind.equals("attached");
        StructurePiece piece=city?ChudSimplePlacement.city(c,above,kind.equals("attached")):ChudSimplePlacement.abandoned(c,above,kind.equals("great"));
        if(piece==null){source.sendFailure(Component.translatable("command.slavicmyths.chud.generate.no_space"));return 0;}
        var structure=level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",city?"chud_city":"chud_abandoned"));
        var box=piece.getBoundingBox();var chunks=new ArrayList<net.minecraft.world.level.chunk.LevelChunk>();
        // Only this explicit command loads its finite footprint. Preflight finishes before any writes.
        for(int x=box.minX()>>4;x<=box.maxX()>>4;x++)for(int z=box.minZ()>>4;z<=box.maxZ()>>4;z++){
            var chunk=level.getChunk(x,z);chunks.add(chunk);
            for(var start:chunk.getAllStarts().values())if(start.isValid())for(var other:start.getPieces())
                if(other instanceof net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces.PortalRoom&&box.intersects(other.getBoundingBox())){
                    source.sendFailure(Component.translatable("command.slavicmyths.chud.generate.portal_room"));return 0;
                }
        }
        if(!level.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),box.minY(),box.minZ()))||!level.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),box.maxY(),box.maxZ()))){source.sendFailure(Component.translatable("command.slavicmyths.chud.generate.world_border"));return 0;}
        var existing=level.getChunk(owner.x,owner.z).getStartForStructure(structure);
        if(existing!=null&&existing.isValid()){source.sendFailure(Component.translatable("command.slavicmyths.chud.generate.existing"));return 0;}
        // Protect custom/manual portal frames as well as native portal rooms, including a surrounding room margin.
        var cursor=new BlockPos.MutableBlockPos();
        for(var chunk:chunks)for(int y=Math.max(level.getMinBuildHeight(),box.minY()-16);y<=Math.min(level.getMaxBuildHeight()-1,box.maxY()+16);y++)for(int z=0;z<16;z++)for(int x=0;x<16;x++){
            cursor.set(chunk.getPos().getMinBlockX()+x,y,chunk.getPos().getMinBlockZ()+z);
            var state=chunk.getBlockState(cursor);
            if(state.is(Blocks.END_PORTAL)||state.is(Blocks.END_PORTAL_FRAME)){source.sendFailure(Component.translatable("command.slavicmyths.chud.generate.portal_frame"));return 0;}
        }
        var start=new StructureStart(structure,owner,0,new PiecesContainer(List.of(piece)));
        for(var chunk:chunks){var pos=chunk.getPos();var clip=new BoundingBox(pos.getMinBlockX(),level.getMinBuildHeight(),pos.getMinBlockZ(),pos.getMaxBlockX(),level.getMaxBuildHeight()-1,pos.getMaxBlockZ());
            piece.postProcess(level,level.structureManager(),generator,level.random,clip,pos,BlockPos.ZERO);
        }
        level.getChunk(owner.x,owner.z).setStartForStructure(structure,start);
        for(var chunk:chunks){chunk.addReferenceForStructure(structure,owner.toLong());chunk.setUnsaved(true);}
        if(piece instanceof AbandonedPiece a)org.slavicmyths.underground.EncounterPopulation.data(level).plan(level,a.plan);
        else if(piece instanceof ChudPiece cityPiece&&cityPiece.abandoned!=null)org.slavicmyths.underground.EncounterPopulation.data(level).plan(level,cityPiece.abandoned);
        source.sendSuccess(()->Component.translatable("command.slavicmyths.chud.generate.success", kind,
            Component.translatable(above ? "command.slavicmyths.chud.generate.above" : "command.slavicmyths.chud.generate.below"), box.toString()), true);
        return 1;
    }
    private ChudGenerateCommands(){}
}
