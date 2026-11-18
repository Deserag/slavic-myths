package org.slavicmyths.hellqa;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.worldgen.StructureCandidates;
import org.slavicmyths.netherboss.NetherBossItems;

/** Opens real native new-chunk containers; no template/manual structure placement. */
@GameTestHolder("slavicmyths_hellqa") @PrefixGameTestTemplate(false)
public final class BossComponentsNaturalQaTests {
 @GameTest(template="empty",timeoutTicks=12000)
 public static void componentsFromFiveNaturalStructures(GameTestHelper t){
  var world=t.getLevel().getServer().getLevel(Level.NETHER);var generator=world.getChunkSource().getGenerator();var found=new HashSet<String>();
  var pairs=Map.of("nether_watchpost",List.of("broken_watch_horn","charred_signal_ribbon"),"wayfarer_camp",List.of("morova_thread","darkened_waymark"),"forgotten_shrine",List.of("faded_rite_seal","nameless_ashes"),"ancestor_shrine",List.of("ancestor_bone_charm","mourning_circlet"),"trizna_pyre",List.of("nameless_ashes","charred_signal_ribbon"));
  for(var row:pairs.entrySet()){
   var structure=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",row.getKey()));
   var placement=StructureCandidates.placement(world,structure);boolean opened=false;
   search:for(int ring=0;ring<=12;ring++)for(int gx=-ring;gx<=ring;gx++)for(int gz=-ring;gz<=ring;gz++){
    if(Math.max(Math.abs(gx),Math.abs(gz))!=ring)continue;
    var cp=placement.getPotentialStructureChunk(world.getSeed(),gx*placement.spacing(),gz*placement.spacing());
    if(!placement.applyAdditionalChunkRestrictions(cp.x,cp.z,world.getSeed()))continue;
    var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(world.registryAccess(),generator,generator.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),world.getSeed(),cp,world,structure.biomes()::contains);
    if(structure.findValidGenerationPoint(context).isEmpty())continue;
    var chunk=world.getChunk(cp.x,cp.z);var start=chunk.getAllStarts().get(structure);if(start==null||!start.isValid())continue;
    var box=start.getBoundingBox();
    for(int cx=box.minX()>>4;cx<=box.maxX()>>4;cx++)for(int cz=box.minZ()>>4;cz<=box.maxZ()>>4;cz++)world.getChunk(cx,cz);
    for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++)for(int y=box.minY();y<=box.maxY();y++){
     var pos=new BlockPos(x,y,z);
     if(!(world.getBlockEntity(pos) instanceof ChestBlockEntity chest)||chest.getLootTable()==null||!chest.getLootTable().location().toString().equals("slavicmyths:chests/"+row.getKey()))continue;
     for(String component:row.getValue()){
      int count=0;for(int slot=0;slot<chest.getContainerSize();slot++)if(chest.getItem(slot).is(NetherBossItems.item(component)))count+=chest.getItem(slot).getCount();
      t.assertTrue(count==1,"Native component count "+component+"="+count+" structure="+row.getKey()+" seed="+world.getSeed()+" pos="+pos);found.add(component);
     }
     t.assertTrue(chest.getLootTable()==null,"Loot marker retained after opening");
     System.out.println("NETHERBOSS_NATURAL_COMPONENTS seed="+world.getSeed()+" structure="+row.getKey()+" chunk="+cp+" chest="+pos+" biome="+world.getBiome(pos).unwrapKey().get().location()+" pair="+row.getValue());
     opened=true;break search;
    }
   }
   t.assertTrue(opened,"No natural component chest found for "+row.getKey()+" seed="+world.getSeed());
  }
  t.assertTrue(found.size()==8,"Not all eight components acquired "+found);t.succeed();
 }
}
