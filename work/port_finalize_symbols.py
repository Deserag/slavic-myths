from pathlib import Path
import re,zipfile
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar');index={n[:-5].replace('/','.') for n in z.namelist() if n.endswith('.java')}
extra={'BlockState':'net.minecraft.world.level.block.state.BlockState','AbstractBlock':'net.minecraft.world.level.block.state.BlockBehaviour','BlockItemUseContext':'net.minecraft.world.item.context.BlockPlaceContext','ModifiableAttributeInstance':'net.minecraft.world.entity.ai.attributes.AttributeInstance','MutableAttribute':'net.minecraft.world.entity.ai.attributes.Attribute','SwimGoal':'net.minecraft.world.entity.ai.goal.FloatGoal','LookRandomlyGoal':'net.minecraft.world.entity.ai.goal.RandomLookAroundGoal','LookAtGoal':'net.minecraft.world.entity.ai.goal.LookAtPlayerGoal','WaterAvoidingRandomWalkingGoal':'net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal','RandomWalkingGoal':'net.minecraft.world.entity.ai.goal.RandomStrollGoal','NoFeatureConfig':'net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration','TemplateManager':'net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager','MutableBoundingBox':'net.minecraft.world.level.levelgen.structure.BoundingBox','PlacementSettings':'net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings','Template':'net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate','IServerWorld':'net.minecraft.world.level.ServerLevelAccessor','ILivingEntityData':'net.minecraft.world.entity.SpawnGroupData','ChunkPos':'net.minecraft.world.level.ChunkPos'}
for a,b in extra.items():assert b in index,b
# Pull the validated mapping table from the previous transformation without running it again.
source=Path('work/port_finalize_types.py').read_text(encoding='utf-8-sig');ns={};exec(source[:source.index('changed=[]')],ns)
classes=ns['classes']|extra;prefix=ns['prefix'];exact=ns['exact']
lex=re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/|\b(?:net\.[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+|[A-Za-z_$][\w$]*)\b')
def replace(m):
 t=m.group()
 if t.startswith('net.minecraft.'):
  last=t.rsplit('.',1)[-1]
  if t in index:return t
  if last in classes:return classes[last]
  pre=t.rsplit('.',1)[0];mapped=prefix.get(pre,pre)+'.'+last
  if mapped in index:return mapped
 elif t in extra:return extra[t].rsplit('.',1)[-1]
 return t
for p in Path('src/main/java').rglob('*.java'):
 original=s=p.read_text(encoding='utf-8');s=lex.sub(replace,s)
 if 'import net.minecraft.world.level.block.*;' in s and 'BlockState' in s:
  s=s.replace('import net.minecraft.world.level.block.*;','import net.minecraft.world.level.block.*;\nimport net.minecraft.world.level.block.state.BlockState;\nimport net.minecraft.world.level.block.state.StateDefinition;\nimport net.minecraft.world.level.block.state.BlockBehaviour;')
 if 'import net.minecraft.world.entity.ai.goal.*;' in s:s=s.replace('import net.minecraft.world.entity.ai.goal.*;','import net.minecraft.world.entity.ai.goal.*;\nimport net.minecraft.world.entity.ai.goal.target.*;')
 # Names used through old wildcard imports need explicit modern imports.
 for a,b in extra.items():
  if re.search(r'\b'+b.rsplit('.',1)[-1]+r'\b',s) and b.rsplit('.',1)[-1] not in {'BlockState','BlockBehaviour','StateDefinition'}:
   if 'import '+b+';' not in s:s=s.replace('\n','\nimport '+b+';\n',1)
 # Constructor migration, respecting nested calls and Java strings.
 pattern=re.compile(r'new\s+(?:net\.minecraft\.resources\.)?ResourceLocation\s*\(')
 pos=0
 while m:=pattern.search(s,pos):
  start=m.end();i=start;depth=1;commas=[];quote=False;escape=False
  while i<len(s) and depth:
   c=s[i]
   if quote:
    if escape:escape=False
    elif c=='\\':escape=True
    elif c=='"':quote=False
   elif c=='"':quote=True
   elif c=='(':depth+=1
   elif c==')':depth-=1
   elif c==',' and depth==1:commas.append(i)
   i+=1
  if len(commas)>1:raise ValueError((p,s[m.start():i]))
  factory='fromNamespaceAndPath' if commas else 'parse'
  name='net.minecraft.resources.ResourceLocation' if 'net.minecraft.resources.' in m.group() else 'ResourceLocation'
  replacement=name+'.'+factory+'('+s[start:i]
  s=s[:m.start()]+replacement+s[i:];pos=m.start()+len(replacement)
 s=s.replace('.getEntityLiving()', '.getEntity()').replace('.isOnGround()', '.onGround()').replace('.canSee(', '.hasLineOfSight(')
 s=s.replace('ClipContext.BlockMode','ClipContext.Block').replace('ClipContext.FluidMode','ClipContext.Fluid')
 if s!=original:p.write_text(s,encoding='utf-8')
print('Applied checked remaining type/FQN names, ResourceLocation factories and entity method renames')
