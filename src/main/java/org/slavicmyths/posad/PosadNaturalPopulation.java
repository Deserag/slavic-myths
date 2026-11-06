package org.slavicmyths.posad;

import java.util.HashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "slavicmyths")
public final class PosadNaturalPopulation {
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (server.getTickCount() % 100 != 0) return;
        // Retry only indexed completed manual sites near real players; no world/chunk scan.
        for(var level:server.getAllLevels()){
            var seen=new java.util.HashSet<net.minecraft.core.BlockPos>();int budget=4;
            for(var player:players(level,server.getTickCount()/100)){
                if(budget<=0)break;
                var area=new net.minecraft.world.level.levelgen.structure.BoundingBox(player.blockPosition()).inflatedBy(96);
                for(var site:org.slavicmyths.worldgen.ManualStructureRecords.get(level).overlapping(area)){
                    if(budget<=0)break;
                    if(!site.family().equals("nether_posad")||!seen.add(site.arrival()))continue;
                    var anchor=site.arrival();
                    if(PosadResidentSpawner.populated(level,anchor))continue;
                    PosadResidentSpawner.populate(level,anchor.getX(),anchor.getY(),anchor.getZ(),site.tier());budget--;
                }
            }
        }
        var world = server.getLevel(Level.NETHER);
        if (world == null || world.players().isEmpty()) return;

        var structure = world.registryAccess().registryOrThrow(Registries.STRUCTURE)
            .get(ResourceLocation.fromNamespaceAndPath("slavicmyths", "nether_posad"));
        if (structure == null) return;

        var discovered=new HashSet<PosadKnownSites.Layout>();
        int budget=4;
        var players=players(world,server.getTickCount()/100);
        for(var player:players){
            for(var layout:PosadKnownSites.get(world).nearby(player.blockPosition())){
                if(budget<=0)break;
                if(!discovered.add(layout)||PosadResidentSpawner.populated(world,layout.anchor()))continue;
                var anchor=layout.anchor();
                PosadResidentSpawner.populateNatural(world,anchor.getX(),anchor.getY(),anchor.getZ(),layout.turns());budget--;
            }
            if(budget<=0)break;
        }
        // Older saves may not have the persistent layout index yet. Inspect only
        // references already present in loaded chunks, under the same four-city budget.
        var visited = new HashSet<Long>();
        var chunks = world.getChunkSource();
        for (var player : players) {
            if(budget<=0)break;
            var pos = player.chunkPosition();
            var chunk = chunks.getChunkNow(pos.x, pos.z);
            if (chunk == null) continue;
            for (long reference : chunk.getReferencesForStructure(structure)) {
                if(budget<=0)break;
                if (!visited.add(reference)) continue;
                var owner = new ChunkPos(reference);
                var startChunk = chunks.getChunkNow(owner.x, owner.z);
                if (startChunk == null) continue;
                var start = startChunk.getStartForStructure(structure);
                if (start == null || !start.isValid()) continue;
                for (var piece : start.getPieces()) {
                    if(budget<=0)break;
                    if (!(piece instanceof PosadPiece posad)) continue;
                    var anchor = posad.anchor();
                    if(!discovered.add(new PosadKnownSites.Layout(anchor,posad.turns()))||PosadResidentSpawner.populated(world,anchor))continue;
                    PosadResidentSpawner.populateNatural(world,anchor.getX(),anchor.getY(),anchor.getZ(),posad.turns());budget--;
                }
            }
        }
    }
    private static java.util.List<net.minecraft.server.level.ServerPlayer> players(net.minecraft.server.level.ServerLevel world,int cycle){
        var players=new java.util.ArrayList<>(world.players());
        if(!players.isEmpty())java.util.Collections.rotate(players,-Math.floorMod(cycle,players.size()));
        return players;
    }

    private PosadNaturalPopulation() {}
}
