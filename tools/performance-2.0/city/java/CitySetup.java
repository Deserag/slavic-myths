package org.slavicmyths.perf2;

import java.io.PrintWriter;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/** One finite native search/setup, never a generated replacement or per-tick locate. */
final class CitySetup {
    static void beforeFixture(ServerLevel world, ServerPlayer player, PrintWriter out) {
        var event=org.slavicmyths.poloz.PolozEvent.get(world);
        var old=event.loaded(world);
        out.println("LEGACY_PLAYER_PRE_FIXTURE game_time="+world.getGameTime()
                +" mainhand="+net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())
                +" runes="+org.slavicmyths.rpg.Runes.list(player.getMainHandItem())
                +" class="+org.slavicmyths.rpg.classes.ClassState.data(player).getString("Base")
                +" event="+event.state+" loaded_boss="+(old!=null)
                +" hp="+(old==null?"none":old.getHealth()));
        out.flush();
    }
    static void locate(ServerLevel world, ServerPlayer player, PrintWriter out) {
        long began=System.nanoTime();
        if(System.getProperty("perf.openWorld")!=null) {
            var event=org.slavicmyths.poloz.PolozEvent.get(world);
            var previous=event.loaded(world);
            out.println("LEGACY_NATIVE_LOAD_VERIFY seed="+world.getSeed()+" event="+event.state+" loaded_boss="+(previous!=null)+" hp="+(previous==null?"none":previous.getHealth()));
            // Only after the two-minute legacy-load observation; not a natural boss victory.
            if(previous!=null)previous.cancel();
            if(event.running())event.finish(world,false);
            out.println("LEGACY_FIXTURE_ENCOUNTER_CANCELED_BEFORE_CITY tickets_and_state_cleanup_native");
            out.flush();
        }
        var key=ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath("slavicmyths","gorodishche"));
        var holder=world.registryAccess().registryOrThrow(Registries.STRUCTURE).getHolderOrThrow(key);
        var result=world.getChunkSource().getGenerator().findNearestMapStructure(world,HolderSet.direct(holder),player.blockPosition(),4,false);
        if(result==null) {
            out.println("NATIVE_CITY_NOT_FOUND bounded_radius4 setup_ms="+(System.nanoTime()-began)/1e6+"; NOT_EXERCISED, no further search");
            out.flush();return;
        }
        var position=result.getFirst();
        var chunk=world.getChunk(position.getX()>>4,position.getZ()>>4);
        StructureStart start=chunk.getStartForStructure(holder.value());
        if(start==null||!start.isValid()) {
            // At most the native references of this one chunk, solely during setup.
            for(long reference:chunk.getReferencesForStructure(holder.value())) {
                var owner=new ChunkPos(reference);
                var candidate=world.getChunk(owner.x,owner.z).getStartForStructure(holder.value());
                if(candidate!=null&&candidate.isValid()){start=candidate;break;}
            }
        }
        var arrival=world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,position);
        float heading=0;
        if(start!=null&&start.isValid()) {
            out.println("NATIVE_CITY_START bounds="+start.getBoundingBox()+" pieces="+start.getPieces().size());
            for(var piece:start.getPieces()) {
                if(piece instanceof org.slavicmyths.gorodishche.CityBuildingPiece building&&building.gateApproach().isPresent()) {
                    var approach=building.gateApproach().orElseThrow();
                    var inside=approach.relative(building.facing().getOpposite(),18);
                    world.getChunk(inside.getX()>>4,inside.getZ()>>4);
                    // Prefer the actual ground passage, not a heightmap position on its roof.
                    boolean found=false;
                    for(int dz=-2;dz<=2&&!found;dz++)for(int dx=-2;dx<=2&&!found;dx++)for(int dy=-3;dy<=3&&!found;dy++) {
                        var q=inside.offset(dx,dy,dz);
                        if(!world.hasChunkAt(q)||!world.getFluidState(q).isEmpty())continue;
                        if(!world.getBlockState(q.below()).getCollisionShape(world,q.below()).isEmpty()
                                &&world.getBlockState(q).getCollisionShape(world,q).isEmpty()
                                &&world.getBlockState(q.above()).getCollisionShape(world,q.above()).isEmpty()) {
                            arrival=q;found=true;
                        }
                    }
                    heading=building.facing().getOpposite().toYRot();
                    out.println("CITY_GATE_GROUND_PASSAGE_FOUND="+found+" gate_approach="+approach);
                    break;
                }
            }
        }
        player.teleportTo(world,arrival.getX()+.5,arrival.getY(),arrival.getZ()+.5,heading,12);
        out.println("NATIVE_CITY_LOCATE_FOUND "+position+" radius=4 arrival="+arrival+" setup_ms="+(System.nanoTime()-began)/1e6);
        out.flush();
    }
}
