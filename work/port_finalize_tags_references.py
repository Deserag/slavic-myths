from pathlib import Path
import re
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8');s=s.replace('Heightmap.Type.','Heightmap.Types.').replace('net.minecraft.world.gen.ChunkGenerator','net.minecraft.world.level.chunk.ChunkGenerator').replace('net.minecraft.world.gen.feature.Feature;','net.minecraft.world.level.levelgen.feature.Feature;')
 if 'FeaturePlaceContext<' in s and 'import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;' not in s and 'import net.minecraft.world.level.levelgen.feature.*;' not in s:s=s.replace('\n','\nimport net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;\n',1)
 s=s.replace('.getMaterial().isSolid()','.isSolid()').replace('.getMaterial().isReplaceable()','.canBeReplaced()').replace('.getMaterial().isLiquid()','.liquid()')
 s=re.sub(r'net\.minecraftforge\.common\.Tags\.IOptionalNamedTag<(.+?)>(?=\s+\w)',r'net.minecraft.tags.TagKey<\1>',s)
 s=s.replace('net.minecraft.tags.ITag.INamedTag<Block>','net.minecraft.tags.TagKey<Block>')
 for name,key in [('EntityTypeTags','ENTITY_TYPE'),('ItemTags','ITEM'),('BlockTags','BLOCK')]:
  s=re.sub(r'(?:net\.minecraft\.tags\.)?'+name+r'\.createOptional\(', 'net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.'+key+',',s)
 if s!=old:p.write_text(s,encoding='utf-8')
# Target loot table references are typed keys, without changing their IDs or seed.
for p in Path('src/main/java').rglob('*.java'):
 s=p.read_text(encoding='utf-8');old=s;pos=0
 while m:=re.search(r'\.setLootTable\(',s[pos:]):
  start=pos+m.end();i=start;depth=0;quote=False;escape=False
  while i<len(s):
   c=s[i]
   if quote:
    if escape:escape=False
    elif c=='\\':escape=True
    elif c=='"':quote=False
   elif c=='"':quote=True
   elif c=='(':depth+=1
   elif c==')':depth-=1
   elif c==',' and depth==0:break
   i+=1
  arg=s[start:i]
  if 'ResourceLocation.' in arg and 'ResourceKey.create' not in arg:
   new='net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,'+arg+')';s=s[:start]+new+s[i:];i=start+len(new)
  pos=i+1
 if s!=old:p.write_text(s,encoding='utf-8')
# Additional data categories introduced by the port must not escape the static audit.
p=Path('tools/verify_static_data_1211.py');s=p.read_text(encoding='utf-8').replace("'tags/entity_type']}","'tags/entity_type','tags/enchantment','tags/damage_type','enchantment','damage_type']}")
s=s.replace("    else:\n        registry=kind.split('/')[1]",'''    elif kind=='enchantment':
        reference('item',value['supported_items'],file)
        require(value.get('max_level',0)>0 and bool(value.get('slots')),f'{file}: invalid enchantment levels/slots')
        require(isinstance(value.get('effects'),dict),f'{file}: missing enchantment effects map')
    elif kind=='damage_type':
        require(isinstance(value.get('message_id'),str) and value.get('exhaustion',-1)>=0,f'{file}: invalid damage definition')
        require(value.get('scaling') in ['never','always','when_caused_by_living_non_player'],f'{file}: invalid damage scaling')
    else:
        registry=kind.split('/')[1]''');p.write_text(s,encoding='utf-8')
