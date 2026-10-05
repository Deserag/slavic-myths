from pathlib import Path
import re
p=Path('src/main/java/org/slavicmyths/worldgen/StructureCandidates.java');s=p.read_text(encoding='utf-8');pos=s.index('    private StructureCandidates()')
s=s[:pos]+'''    public static RandomSpreadStructurePlacement placement(net.minecraft.server.level.ServerLevel world,Structure structure){
        if(structure==null)return null;
        var holder=world.registryAccess().registryOrThrow(Registries.STRUCTURE).wrapAsHolder(structure);
        return world.getChunkSource().getGeneratorState().getPlacementsForStructure(holder).stream().filter(RandomSpreadStructurePlacement.class::isInstance).map(RandomSpreadStructurePlacement.class::cast).findFirst().orElse(null);
    }
    public static boolean biome(net.minecraft.server.level.ServerLevel world,Structure structure,ChunkPos chunk){return structure.biomes().contains(world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((chunk.x<<2)+2,16,(chunk.z<<2)+2,world.getChunkSource().randomState().sampler()));}
    public static net.minecraft.core.BlockPos locate(net.minecraft.world.level.levelgen.structure.StructureStart start){var box=start.getBoundingBox();return new net.minecraft.core.BlockPos((box.minX()+box.maxX())/2,box.maxY()+1,(box.minZ()+box.maxZ())/2);}
'''+s[pos:];p.write_text(s,encoding='utf-8')
for name in ['SwampCommands','CampCommands','KurganCommands']:
 p=next(Path('src/main/java').rglob(name+'.java'));s=p.read_text(encoding='utf-8');s=re.sub(r'import net.minecraft.world.gen(?:\.[\w*]+)+;\n','',s)
 s=s.replace('import net.minecraft.world.level.ChunkPos;', 'import net.minecraft.world.level.ChunkPos;\nimport net.minecraft.world.level.levelgen.structure.*;\nimport net.minecraft.world.level.levelgen.placement.*;\nimport net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;\nimport org.slavicmyths.worldgen.StructureCandidates;')
 s=s.replace('Structure<?>','Structure').replace('StructureStart<?>','StructureStart').replace('net.minecraft.command.arguments.EntityArgument','net.minecraft.commands.arguments.EntityArgument')
 s=s.replace('StructureSeparationSettings','RandomSpreadStructurePlacement').replace('getStartForFeature(', 'getStartForStructure(').replace('start.getLocatePos()', 'StructureCandidates.locate(start)').replace('net.minecraft.world.chunk.IChunk','net.minecraft.world.level.chunk.ChunkAccess')
 if name=='KurganCommands':
  s=s.replace('KurganStructure type=KurganStructures.type(kind);RandomSpreadStructurePlacement cfg=w.getChunkSource().getGenerator().getSettings().getConfig(type);', 'Structure type=org.slavicmyths.swamp.SwampStructures.get(w,KurganStructures.id(kind));RandomSpreadStructurePlacement cfg=StructureCandidates.placement(w,type);')
  s=s.replace('type.getPotentialFeatureChunk(cfg,w.getSeed(),new SharedSeedRandom(),','cfg.getPotentialStructureChunk(w.getSeed(),')
  s=s.replace('w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2).getGenerationSettings().isValidStart(type)', 'StructureCandidates.biome(w,type,q)')
 elif name=='CampCommands':
  for const in ['SMALL','MEDIUM','LARGE']:s=s.replace('CampStructures.'+const+'.get()', 'org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_'+const.lower()+'")')
  s=s.replace('w.getChunkSource().getGenerator().getSettings().getConfig(type)', 'StructureCandidates.placement(w,type)').replace('type.getPotentialFeatureChunk(cfg,w.getSeed(),new SharedSeedRandom(),','cfg.getPotentialStructureChunk(w.getSeed(),')
  s=s.replace('type.getRegistryName()', 'w.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(type)')
  s=s.replace('w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((p.x<<2)+2,16,(p.z<<2)+2).getGenerationSettings().isValidStart(type)', 'StructureCandidates.biome(w,type,p)')
 else:
  s=s.replace('SwampStructure type=SwampStructures.TYPES.get(id).get();','Structure type=SwampStructures.get(world,id);').replace('world.getChunkSource().getGenerator().getSettings().getConfig(type)', 'StructureCandidates.placement(world,type)')
  s=s.replace('        net.minecraft.util.SharedSeedRandom random=new net.minecraft.util.SharedSeedRandom();\n','').replace('type.getPotentialFeatureChunk(settings,world.getSeed(),random,','settings.getPotentialStructureChunk(world.getSeed(),')
  s=s.replace('Biome biome=world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((p.x<<2)+2,16,(p.z<<2)+2);\n                if(!biome.getGenerationSettings().isValidStart(type))continue;', 'if(!StructureCandidates.biome(world,type,p))continue;')
 p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/kurgan/KurganDebug.java');s=p.read_text(encoding='utf-8').replace('plan,origin)','plan,origin,world,world.getChunkSource().randomState())')
# Avoid changing the safe(world,origin,plan) preflight or unrelated signatures.
s=s.replace('world.structureFeatureManager()', 'world.structureManager()').replace('new Random(seed),clip', 'net.minecraft.util.RandomSource.create(seed),clip').replace('s.hasTileEntity()', 's.hasBlockEntity()')
s=re.sub(r'\bb\.([xyz])([01])',lambda m:'b.'+('min'if m[2]=='0'else 'max')+m[1].upper()+'()',s)
s=s.replace('new BoundingBox(cx*16,0,cz*16,cx*16+15,255,cz*16+15)','new BoundingBox(cx*16,world.getMinBuildHeight(),cz*16,cx*16+15,world.getMaxBuildHeight()-1,cz*16+15)')
p.write_text(s,encoding='utf-8')
