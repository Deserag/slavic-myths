import json
from pathlib import Path
R=Path('src/main/resources'); A=R/'assets/slavicmyths'; D=R/'data/slavicmyths'
def put(p,v):
 p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def recipe(name,v): put(D/'recipe'/f'{name}.json',v)
def cutting(base,out,count=1): recipe(out+'_stonecutting',{'type':'minecraft:stonecutting','ingredient':{'item':'slavicmyths:'+base},'result':{'id':'slavicmyths:'+out,'count':count}})
new=[];walls=[]
for family in ['pale_blue','bog_green']:
 for kind in ['stone','cobblestone','cracked','mossy']:
  base=family+'_kurgan_'+kind if kind in ['stone','cobblestone'] else kind+'_'+family+'_kurgan_cobblestone'
  for shape in ['slab','stairs','wall']:
   name=base+'_'+shape;new.append(name);model='slavicmyths:block/'+name; tex='slavicmyths:block/'+base
   textures={'bottom':tex,'top':tex,'side':tex}
   if shape=='slab':
    put(A/'models/block'/f'{name}.json',{'parent':'minecraft:block/slab','textures':textures});put(A/'models/block'/f'{name}_top.json',{'parent':'minecraft:block/slab_top','textures':textures})
    state={'variants':{'type=bottom':{'model':model},'type=top':{'model':model+'_top'},'type=double':{'model':'slavicmyths:block/'+base}}}
    pattern=['###'];count=6
   elif shape=='stairs':
    for suffix,parent in [('', 'stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:put(A/'models/block'/f'{name}{suffix}.json',{'parent':'minecraft:block/'+parent,'textures':textures})
    native=json.loads(Path('tools/templates/minecraft-1.21.1-stone-brick-stairs.json').read_text(encoding='utf-8'))
    variants={key:{**value,'model':value['model'].replace('minecraft:block/stone_brick_stairs',model)} for key,value in native['variants'].items()}
    state={'variants':variants};pattern=['#  ','## ','###'];count=4
   else:
    walls.append(name)
    for suffix,parent in [('_post','template_wall_post'),('_side','template_wall_side'),('_side_tall','template_wall_side_tall'),('_inventory','wall_inventory')]:put(A/'models/block'/f'{name}{suffix}.json',{'parent':'minecraft:block/'+parent,'textures':{'wall':tex}})
    parts=[{'when':{'up':'true'},'apply':{'model':model+'_post'}}]
    for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:
     for value,suffix in [('low','_side'),('tall','_side_tall')]:parts.append({'when':{facing:value},'apply':{'model':model+suffix,'y':angle,'uvlock':True}})
    state={'multipart':parts};pattern=['###','###'];count=6
   put(A/'blockstates'/f'{name}.json',state);put(A/'models/item'/f'{name}.json',{'parent':model+('_inventory' if shape=='wall' else '')})
   entry={'type':'minecraft:item','name':'slavicmyths:'+name}
   if shape=='slab':entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'type':'double'}}]}]
   put(D/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
   recipe(name,{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{'#':{'item':'slavicmyths:'+base}},'result':{'id':'slavicmyths:'+name,'count':count}});cutting(base,name,2 if shape=='slab' else 1)
# Carved blocks retain their own authored face.
for name,parent in [('carved_burial_stone','cube_all'),('carved_burial_stone_slab','slab'),('carved_burial_stone_slab_top','slab_top')]:
 tex='slavicmyths:block/carved_burial_stone';put(A/'models/block'/f'{name}.json',{'parent':'minecraft:block/'+parent,'textures':{'all':tex} if parent=='cube_all' else {'bottom':tex,'top':tex,'side':tex}})
for name in ['carved_burial_stone','carved_burial_stone_slab']:
 new.append(name);put(A/'models/item'/f'{name}.json',{'parent':'slavicmyths:block/'+name})
 put(A/'blockstates'/f'{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}} if not name.endswith('slab') else {'variants':{'type=bottom':{'model':'slavicmyths:block/'+name},'type=top':{'model':'slavicmyths:block/'+name+'_top'},'type=double':{'model':'slavicmyths:block/carved_burial_stone'}}})
 entry={'type':'minecraft:item','name':'slavicmyths:'+name}
 if name.endswith('slab'):entry['functions']=[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'slavicmyths:'+name,'properties':{'type':'double'}}]}]
 put(D/'loot_table/blocks'/f'{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[entry],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
cutting('pale_blue_kurgan_stone','carved_burial_stone');cutting('carved_burial_stone','carved_burial_stone_slab',2)
recipe('carved_burial_stone',{'type':'minecraft:crafting_shaped','pattern':['#','#'],'key':{'#':{'item':'slavicmyths:pale_blue_kurgan_stone_slab'}},'result':{'id':'slavicmyths:carved_burial_stone','count':1}})
recipe('carved_burial_stone_slab',{'type':'minecraft:crafting_shaped','pattern':['###'],'key':{'#':{'item':'slavicmyths:carved_burial_stone'}},'result':{'id':'slavicmyths:carved_burial_stone_slab','count':6}})
for path,ids in [('tags/block/mineable/pickaxe',new),('tags/block/walls',walls)]:
 p=R/'data/minecraft'/f'{path}.json';v=json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['slavicmyths:'+n for n in ids]));put(p,v)
for shape in ['slab','stairs','wall']:
 ids=[n for n in new if n.endswith('_'+shape)]
 for registry in ['block','item']:
  p=R/'data/minecraft/tags'/registry/(shape+'s.json');v=json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['slavicmyths:'+n for n in ids]));put(p,v)
for lang in ['en_us','ru_ru']:
 p=A/'lang'/f'{lang}.json';v=json.loads(p.read_text(encoding='utf-8-sig'))
 for name in new:
  base,shape=(name.rsplit('_',1) if name.endswith(('_slab','_stairs','_wall')) else (name,''))
  title=v.get('block.slavicmyths.'+base,'Carved Burial Stone' if lang=='en_us' else 'Резной погребальный камень')
  title=title+(' '+{'slab':'Slab','stairs':'Stairs','wall':'Wall'}[shape] if lang=='en_us' and shape else ' — '+{'slab':'плита','stairs':'ступени','wall':'ограда'}[shape] if shape else '')
  v['block.slavicmyths.'+name]=title
 put(p,v)
put(A/'models/block/pale_blue_kurgan_stone_variant.json',{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/pale_blue_kurgan_stone_variant'}})
put(A/'blockstates/pale_blue_kurgan_stone.json',{'variants':{'':[{'model':'slavicmyths:block/pale_blue_kurgan_stone','weight':3},{'model':'slavicmyths:block/pale_blue_kurgan_stone_variant','weight':1}]}})
put(Path('docs/kurgan/registry-additions-0.9.6.json'),{'entries':[{'kind':kind,'target_id':'slavicmyths:'+n} for n in new for kind in ['block','item']]})
print('KURGAN_RESOURCES newBlocks='+str(len(new))+' recipes=explicit shapes, stonecutting, carved')
