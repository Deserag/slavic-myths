package org.slavicmyths.sanctuary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Explicit operator placement; natural generation rules remain unchanged. */
public final class SanctuaryCommands {
    public static void register(org.slavicmyths.commands.CommandTree tree) {
        var root=Commands.literal("slavicmyths");
        for(boolean spring:List.of(false,true)) {
            root.then(Commands.literal(spring?"generate_pure_spring":"generate_fire_sanctuary")
                .requires(source->source.hasPermission(2))
                .executes(c->place(c.getSource(),BlockPos.containing(c.getSource().getPosition()).below(spring?20:1),spring))
                .then(Commands.argument("pos",BlockPosArgument.blockPos())
                    .executes(c->place(c.getSource(),BlockPosArgument.getBlockPos(c,"pos"),spring))));
        }
        tree.add(root);
    }

    private static int place(CommandSourceStack source,BlockPos origin,boolean spring) {
        var world=source.getLevel();
        if(world.dimension()!=Level.OVERWORLD)return fail(source,"command.overworld");
        int width=SanctuaryGeometry.width(spring),depth=SanctuaryGeometry.depth(spring);
        var bounds=new BoundingBox(origin.getX(),origin.getY()-(spring?0:8),origin.getZ(),
            origin.getX()+width-1,origin.getY()+26,origin.getZ()+depth-1);
        if(bounds.minY()<world.getMinBuildHeight()||bounds.maxY()>=world.getMaxBuildHeight())return fail(source,"command.height");
        if(!world.getWorldBorder().isWithinBounds(new BlockPos(bounds.minX(),origin.getY(),bounds.minZ()))
            ||!world.getWorldBorder().isWithinBounds(new BlockPos(bounds.maxX(),origin.getY(),bounds.maxZ())))return fail(source,"command.border");
        var chunks=ChunkPos.rangeClosed(new ChunkPos(origin),new ChunkPos(new BlockPos(bounds.maxX(),origin.getY(),bounds.maxZ()))).toList();
        for(var chunk:chunks)if(!world.hasChunk(chunk.x,chunk.z))return fail(source,"command.loaded");
        var type=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(SanctuaryContent.id(SanctuaryGeometry.id(spring)));
        if(type==null)return fail(source,"command.missing");
        for(var chunk:chunks) {
            var existing=world.getChunk(chunk.x,chunk.z).getStartForStructure(type);
            if(existing!=null&&existing.isValid())return fail(source,"command.occupied");
        }
        var manager=world.getStructureManager();
        var pieces=new ArrayList<StructurePiece>();
        UUID instance=UUID.randomUUID();
        for(int x=0;x<width;x+=16)for(int z=0;z<depth;z+=16) {
            String name=(spring?"spring":"fire")+"/"+x+"_"+z;
            var template=manager.get(SanctuaryContent.id("sanctuaries/"+name));
            if(template.isEmpty()||template.get().getSize().getY()==0)return fail(source,"command.missing");
            pieces.add(new SanctuaryPiece(manager,name,origin.offset(x,0,z),origin,Rotation.NONE,instance,spring));
        }
        var owner=new ChunkPos(origin);
        var start=new StructureStart(type,owner,0,new PiecesContainer(pieces));
        for(var chunk:chunks)start.placeInChunk(world,world.structureManager(),world.getChunkSource().getGenerator(),world.getRandom(),
            new BoundingBox(chunk.getMinBlockX(),world.getMinBuildHeight(),chunk.getMinBlockZ(),chunk.getMaxBlockX(),world.getMaxBuildHeight()-1,chunk.getMaxBlockZ()),chunk);
        world.getChunk(owner.x,owner.z).setStartForStructure(type,start);
        for(var pos:chunks){var chunk=world.getChunk(pos.x,pos.z);chunk.addReferenceForStructure(type,owner.toLong());chunk.setUnsaved(true);}
        SanctuaryRecords.get(world).ensure(instance,origin,Rotation.NONE,spring);
        source.sendSuccess(()->SanctuaryContent.text("command.placed",SanctuaryGeometry.id(spring),origin.getX(),origin.getY(),origin.getZ()),true);
        return 1;
    }
    private static int fail(CommandSourceStack source,String key){source.sendFailure(SanctuaryContent.text(key));return 0;}
    private SanctuaryCommands(){}
}
