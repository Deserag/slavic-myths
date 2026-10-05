from pathlib import Path
p=Path('src/main/java/org/slavicmyths/worldgen/StructureCandidates.java');p.parent.mkdir(parents=True,exist_ok=True)
p.write_text('''package org.slavicmyths.worldgen;

import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/** Bounded candidate checks using registry placements and biome noise only; never loads chunks. */
public final class StructureCandidates {
    public static boolean clear(Structure.GenerationContext c, ToIntFunction<ResourceLocation> margin) {
        var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for(var set:c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)) {
            if(!(set.placement() instanceof RandomSpreadStructurePlacement placement))continue;
            for(var entry:set.structures()) {
                Structure other=entry.structure().value();
                int radius=margin.applyAsInt(structures.getKey(other));
                if(radius<=0)continue;
                Set<Long> seen=new HashSet<>();
                for(int dx=-radius;dx<=radius;dx+=radius)for(int dz=-radius;dz<=radius;dz+=radius) {
                    ChunkPos q=placement.getPotentialStructureChunk(c.seed(),c.chunkPos().x+dx,c.chunkPos().z+dz);
                    if(!seen.add(q.toLong())||Math.abs(q.x-c.chunkPos().x)>radius||Math.abs(q.z-c.chunkPos().z)>radius)continue;
                    var biome=c.biomeSource().getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2,c.randomState().sampler());
                    if(other.biomes().contains(biome))return false;
                }
            }
        }
        return true;
    }
    public static boolean vanilla(ResourceLocation id,String... families) {
        if(id==null||!id.getNamespace().equals("minecraft"))return false;
        for(String family:families)if(id.getPath().equals(family)||id.getPath().startsWith(family+"_"))return true;
        return false;
    }
    private StructureCandidates(){}
}
''',encoding='utf-8')
# Retain each authored generatePieces body, validation and RNG order. Accept a stub only after the full plan validates.
import re
spec=[('swamp','SwampStructure','String id','this.id=id;','id','SwampStructures.TYPES.get(id).get()', 'SwampStructure(String id)'),('bandit','CampStructure','boolean medium','this.medium=medium;','medium','medium?CampStructures.MEDIUM.get():CampStructures.SMALL.get()', 'CampStructure(boolean medium)'),('bandit','LargeCampStructure','','','','CampStructures.LARGE.get()', 'public LargeCampStructure()'),('kurgan','KurganStructure','int kind','this.kind=kind;','kind','KurganStructures.type(kind)', 'public KurganStructure(int k)')]
for package,name,arg,assignment,field,typeexpr,unused in spec:
 p=Path('src/main/java/org/slavicmyths')/package/(name+'.java');s=p.read_text(encoding='utf-8')
 start=s.index('public void generatePieces(');brace=s.index('{',start);depth=1;i=brace+1
 while depth:
  if s[i]=='{':depth+=1
  elif s[i]=='}':depth-=1
  i+=1
 body=s[brace+1:i-1]
 body=body.replace('String id=((SwampStructure)getFeature()).id;','').replace('boolean medium=((CampStructure)getFeature()).medium;','')
 body=body.replace('calculateBoundingBox();','').replace('pieces.clear();return;','pieces.clear();return;')
 body=re.sub(r'\b(generator|g)\.getBaseHeight\(([^;]*?),Heightmap.Types.(\w+)\)',r'\1.getBaseHeight(\2,Heightmap.Types.\3,context.heightAccessor(),context.randomState())',body)
 if name=='LargeCampStructure':body=body.replace('biome.getDepth()>.3F', 'context.biomeSource().getNoiseBiome((cx<<2)+2,16,(cz<<2)+2,context.randomState().sampler()).is(net.neoforged.neoforge.common.Tags.Biomes.IS_MOUNTAIN)').replace('biome.getBiomeCategory()==Biome.Category.PLAINS', 'context.biomeSource().getNoiseBiome((cx<<2)+2,16,(cz<<2)+2,context.randomState().sampler()).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","worldgen/core_plains")))')
 if name=='KurganStructure':body=body.replace('terrainValid(g,plan,origin)', 'terrainValid(g,plan,origin,context.heightAccessor(),context.randomState())')
 constructorarg=(','+arg)if arg else ''
 codec='simpleCodec(settings->new '+name+'(settings'+(','+field if field else '')+'))'
 if field:codec='static MapCodec<'+name+'> codec('+arg+'){return '+codec+';}'
 else:codec='static final MapCodec<'+name+'> CODEC='+codec+';'
 header='''package org.slavicmyths.%s;
import java.util.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slavicmyths.worldgen.StructureCandidates;
public final class %s extends Structure {
'''%(package,name)
 if field:header+=' public final '+arg+';\n'
 header+=' public '+name+'(StructureSettings settings'+constructorarg+'){super(settings);'+assignment+'}\n public '+codec+'\n @Override public StructureType<?> type(){return '+typeexpr+';}\n'
 if name=='SwampStructure':check='''List<String> priority=Arrays.asList("abandoned_settlement","flooded_shrine","fishing_camp","swamp_hut","underwater_ruins","bog_causeway","swamp_remnants");
 return StructureCandidates.clear(c,other->{if(other==null)return 0;boolean ours=other.getNamespace().equals("slavicmyths")&&priority.contains(other.getPath());if(ours){if(other.getPath().equals(id)||priority.indexOf(other.getPath())>priority.indexOf(id))return 0;return 5;}if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 12;return StructureCandidates.vanilla(other,"pillager_outpost","swamp_hut","ruined_portal","monument","ocean_ruin","shipwreck")?6:0;});'''
 elif name=='CampStructure':check='''return StructureCandidates.clear(c,other->{if(other==null)return 0;if(other.getNamespace().equals("slavicmyths")){String id=other.getPath();if(id.equals(medium?"bandit_camp_medium":"bandit_camp_small")||medium&&id.equals("bandit_camp_small"))return 0;if(id.startsWith("bandit_camp_")||org.slavicmyths.swamp.SwampStructures.TYPES.containsKey(id))return 4;return 0;}if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 10;return StructureCandidates.vanilla(other,"pillager_outpost","ruined_portal")?4:0;});'''
 elif name=='LargeCampStructure':check='''return StructureCandidates.clear(c,other->{if(other==null)return 0;if(other.getNamespace().equals("slavicmyths"))return org.slavicmyths.swamp.SwampStructures.TYPES.containsKey(other.getPath())?7:0;if(StructureCandidates.vanilla(other,"village","woodland_mansion"))return 12;return StructureCandidates.vanilla(other,"pillager_outpost","ruined_portal")?7:0;});'''
 else:check='''if(KurganShape.kind(c.seed(),c.chunkPos().x,c.chunkPos().z)!=kind)return false;
 if(!StructureCandidates.clear(c,other->{if(other==null||other.getNamespace().equals("slavicmyths")&&other.getPath().startsWith("kurgan_")||StructureCandidates.vanilla(other,"mineshaft","stronghold","buried_treasure"))return 0;return (KurganShape.RADIUS[kind]+96+15)/16;}))return false;
 if(kind==2){var placement=new net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement(64,24,net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType.LINEAR,841973);for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){if(dx==0&&dz>=0||dx>0)continue;ChunkPos q=placement.getPotentialStructureChunk(c.seed(),c.chunkPos().x+dx*64,c.chunkPos().z+dz*64);if(KurganShape.kind(c.seed(),q.x,q.z)==2)return false;}}
 return true;'''
 header+=' private boolean candidate(GenerationContext c){'+check+'}\n'
 header+=''' @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext context){
 if(!candidate(context))return Optional.empty();List<StructurePiece> pieces=new ArrayList<>();plan(context,pieces);if(pieces.isEmpty())return Optional.empty();
 BlockPos locate=pieces.getFirst().getBoundingBox().getCenter();return Optional.of(new GenerationStub(locate,builder->pieces.forEach(builder::addPiece)));
 }
 private void plan(GenerationContext context,List<StructurePiece> pieces){
 ChunkGenerator g=context.chunkGenerator(),generator=g;StructureTemplateManager templates=context.structureTemplateManager(),t=templates;int cx=context.chunkPos().x,cz=context.chunkPos().z;var random=context.random();
'''+body+'\n }\n'
 if name=='KurganStructure':
  a=s.index(' public static boolean terrainValid(');brace=s.index('{',a);depth=1;i=brace+1
  while depth:
   if s[i]=='{':depth+=1
   elif s[i]=='}':depth-=1
   i+=1
  terrain=s[a:i].replace('BlockPos origin)', 'BlockPos origin,net.minecraft.world.level.LevelHeightAccessor height,net.minecraft.world.level.levelgen.RandomState randomState)').replace('origin.getY()+bounds.y0<2||origin.getY()+bounds.y1>=250','origin.getY()+bounds.y0<height.getMinBuildHeight()+2||origin.getY()+bounds.y1>=height.getMaxBuildHeight()-6')
  terrain=re.sub(r'g.getBaseHeight\(([^;]*?),Heightmap.Types.(\w+)\)',r'g.getBaseHeight(\1,Heightmap.Types.\2,height,randomState)',terrain)
  header+=terrain+'\n'
 p.write_text(header+'}\n',encoding='utf-8')
