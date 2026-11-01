package org.slavicmyths.morovoy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;

/** Arena-only registration. Boss entity, loot and quests are deliberately absent. */
public final class MorovoyArenaWorldgen {
 public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
 public static final TagKey<Biome> BIOMES=TagKey.create(Registries.BIOME,id("morovoy_raven_arena_biomes"));
 public static final DeferredHolder<StructureType<?>,StructureType<MorovoyArenaStructure>> STRUCTURE=
  SwampStructures.STRUCTURES.register("morovoy_raven_arena",()->()->MorovoyArenaStructure.CODEC);
 public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE=
  SwampStructures.PIECES.register("morovoy_raven_arena",()->(context,tag)->new MorovoyArenaPiece(tag));
 public static void init(){}
 private MorovoyArenaWorldgen(){}
}
