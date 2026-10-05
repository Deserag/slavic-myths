from pathlib import Path
import re
base=Path('src/main/java/org/slavicmyths')
for package,name,key,folder,manager,var,pos,reserve in [('swamp','SwampPiece','SwampTemplate','swamp','manager','name','pos',8),('bandit','CampPiece','CampTemplate','bandit','manager','name','pos',8),('bandit','LargeCampPiece','StrongholdTemplate','stronghold','tm','n','p',10)]:
 p=base/package/(name+'.java');s=p.read_text(encoding='utf-8')
 s=re.sub(r'import net.minecraft.world.gen(?:\.[\w*]+)+;\n','',s)
 s=s.replace('import net.minecraft.world.level.levelgen.structure.BoundingBox;', 'import net.minecraft.world.level.levelgen.structure.*;\nimport net.minecraft.world.level.chunk.ChunkGenerator;\nimport net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;\nimport net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;\nimport net.minecraft.util.RandomSource;')
 holder='SwampStructures.PIECE'if name=='SwampPiece'else 'CampStructures.PIECE'if name=='CampPiece'else 'CampStructures.LARGE_PIECE'
 s=s.replace('super('+holder+',0)', 'super('+holder+'.get(),0,'+manager+',ResourceLocation.fromNamespaceAndPath("slavicmyths","'+folder+'/"+'+var+'),"slavicmyths:'+folder+'/"+'+var+',settings(),'+pos+')')
 # Existing NBT names remain readable while supplying the modern base Template field.
 nbt='nbt'if name=='SwampPiece'else 'tag'if name=='CampPiece'else 'n'
 s=s.replace('super('+holder+','+nbt+')', 'super('+holder+'.get(),modernTag('+nbt+'),'+manager+',id->settings())')
 if name=='LargeCampPiece':s=s.replace('name=n.getString("Template")','name=n.getString("StrongholdTemplate");if(name.isEmpty())name=n.getString("Template")').replace('n.putString("Template",name)', 'n.putString("StrongholdTemplate",name)')
 start=s.index('    private void load(')if name=='SwampPiece'else s.index('    private void load(')if name=='CampPiece'else s.index(' private void load(')
 b=s.index('{',start);depth=1;i=b+1
 while depth:
  if s[i]=='{':depth+=1
  elif s[i]=='}':depth-=1
  i+=1
 load='''private void load(StructureTemplateManager ignored){boundingBox=new BoundingBox(boundingBox.minX(),boundingBox.minY()-%d,boundingBox.minZ()-%d,boundingBox.maxX(),boundingBox.maxY(),boundingBox.maxZ());}
 private static StructurePlaceSettings settings(){return new StructurePlaceSettings().setIgnoreEntities(true).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);}
 private static CompoundTag modernTag(CompoundTag original){CompoundTag n=original.copy();String name=n.getString("%s");%s n.putString("Template","slavicmyths:%s/"+name);return n;}
'''%(reserve,3if name=='LargeCampPiece'else 0,key,'if(name.isEmpty())name=n.getString("Template");'if name=='LargeCampPiece'else '',folder)
 s=s[:start]+load+s[i:]
 s=re.sub(r'addAdditionalSaveData\(CompoundTag (\w+)\)',r'addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag \1)',s)
 s=re.sub(r'super.addAdditionalSaveData\((\w+)\)',r'super.addAdditionalSaveData(context,\1)',s)
 s=s.replace('public boolean postProcess(', 'public void postProcess(').replace('return true;','return;')
 # Only callback RNG changes; deterministic java.util.Random choices stay unchanged.
 s=re.sub(r'(postProcess\([^\n]*?)\bRandom (random|r),',r'\1RandomSource \2,',s)
 s=re.sub(r'(handleDataMarker\([^\n]*?)\bRandom (random|r),',r'\1RandomSource \2,',s)
 s=s.replace('super.postProcess(world,structures,generator,new Random(seed),', 'super.postProcess(world,structures,generator,RandomSource.create(seed),')
 s=s.replace('StructureTemplate.BlockInfo','StructureTemplate.StructureBlockInfo').replace('info.pos','info.pos()').replace('info.state','info.state()').replace('info.nbt','info.nbt()')
 s=s.replace('net.minecraft.tileentity.LockableLootTileEntity','net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity').replace('.getTileData()', '.getPersistentData()').replace('Blocks.GRASS_PATH','Blocks.DIRT_PATH')
 # Height queries use the current generation region's height and RandomState, without loading chunks.
 for gen,world in [('g','w'),('generator','world')]:
  s=re.sub(r'\b'+gen+r'\.getBaseHeight\(([^;]*?),((?:net.minecraft.world.level.levelgen.)?Heightmap.Types.\w+)\)',gen+r'.getBaseHeight(\1,\2,'+world+','+world+r'.getLevel().getChunkSource().randomState())',s)
 p.write_text(s,encoding='utf-8')
for name in ['KurganDungeonPiece','KurganPiece']:
 p=base/'kurgan'/(name+'.java');s=p.read_text(encoding='utf-8');s=re.sub(r'import net.minecraft.world.gen(?:\.[\w*]+)+;\n','',s)
 s=s.replace('import net.minecraft.world.level.levelgen.structure.BoundingBox;', 'import net.minecraft.world.level.levelgen.structure.*;\nimport net.minecraft.world.level.chunk.ChunkGenerator;\nimport net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;\nimport net.minecraft.util.RandomSource;')
 h='KurganStructures.DUNGEON'if name=='KurganDungeonPiece'else 'KurganStructures.PIECE'
 s=s.replace('super('+h+',0)', 'super('+h+'.get(),0,'+('box(p,origin)'if name=='KurganDungeonPiece'else 'box(k,x,y,z)')+')').replace('super('+h+',n)','super('+h+'.get(),n)')
 if name=='KurganDungeonPiece':s=s.replace('    private void bounds()', '    private static BoundingBox box(KurganPlan p,BlockPos origin){KurganPlan.Box b=p.bounds();return new BoundingBox(origin.getX()+b.x0,origin.getY()+b.y0,origin.getZ()+b.z0,origin.getX()+b.x1,origin.getY()+b.y1,origin.getZ()+b.z1);}\n    private void bounds()')
 else:s=s.replace(' private void bounds()', ' private static BoundingBox box(int kind,int x,int y,int z){int r=KurganShape.RADIUS[kind]+8;return new BoundingBox(x-r,y-8,z-r,x+r,y+KurganShape.HEIGHT[kind]+22,z+r);}\n private void bounds()')
 s=re.sub(r'addAdditionalSaveData\(CompoundTag (\w+)\)',r'addAdditionalSaveData(StructurePieceSerializationContext context,CompoundTag \1)',s)
 s=s.replace('public synchronized boolean postProcess(', 'public synchronized void postProcess(').replace('public boolean postProcess(', 'public void postProcess(')
 s=re.sub(r'(postProcess\([^\n]*?)\bRandom (random|rand),',r'\1RandomSource \2,',s)
 # Return true belongs exclusively to postProcess here; preserve boolean helpers.
 a=s.index('void postProcess');b=s.index('{',a);depth=1;i=b+1
 while depth:
  if s[i]=='{':depth+=1
  elif s[i]=='}':depth-=1
  i+=1
 s=s[:b]+s[b:i].replace('return true;','return;')+s[i:]
 s=re.sub(r'clip\.([xyz])([01])', lambda m:'clip.'+('min'if m[2]=='0'else 'max')+m[1].upper()+'()',s)
 s=s.replace('Blocks.GRASS.defaultBlockState()', 'Blocks.SHORT_GRASS.defaultBlockState()')
 if name=='KurganPiece':s=s.replace('surface(ChunkGenerator g,int dx,int dz)', 'surface(WorldGenLevel w,ChunkGenerator g,int dx,int dz)').replace('surface(g,','surface(w,g,')
 s=re.sub(r'g\.getBaseHeight\(([^;]*?),Heightmap.Types.(\w+)\)',r'g.getBaseHeight(\1,Heightmap.Types.\2,w,w.getLevel().getChunkSource().randomState())',s)
 p.write_text(s,encoding='utf-8')
