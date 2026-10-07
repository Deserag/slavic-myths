"""Native Household 1.1.6 resources; no Minecraft launch, no unrelated resources."""
from pathlib import Path
import json,copy
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];R=ROOT/'src/main/resources';A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
def js(p,o):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(o,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def cube(a,b,t):
 dx,dy,dz=[b[i]-a[i] for i in range(3)]
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+t,'uv':[0,0,dx if f in ['north','south','up','down'] else dz,dz if f in ['up','down'] else dy]} for f in ['north','south','east','west','up','down']}}
names={'storage_sack':('Мешок','Storage Sack'),'basket':('Корзина','Basket'),'large_basket':('Большая корзина','Large Basket'),'produce_crate':('Ящик','Produce Crate'),'household_chest':('Ларь','Household Chest'),'wooden_barrel':('Деревянная бочка','Wooden Barrel'),'wall_shelf':('Настенная полка','Wall Shelf'),'storage_shelf':('Хозяйственная полка','Storage Shelf'),'drying_rack':('Сушилка','Drying Rack'),'haystack':('Стог','Haystack'),'rye_sheaf':('Сноп ржи','Rye Sheaf'),'barley_sheaf':('Сноп ячменя','Barley Sheaf'),'oat_sheaf':('Сноп овса','Oat Sheaf'),'dried_berries':('Сушёные ягоды','Dried Berries'),'dried_mushrooms':('Сушёные грибы','Dried Mushrooms')}
for name,base in [('household_wood','#976c43'),('household_weave','#a4804f'),('household_cloth','#cbbb96'),('household_ornament','#cbbb96'),('household_metal','#454349'),('household_hay','#c7a252'),('rye_sheaf','#a68944'),('barley_sheaf','#d3b76b'),('oat_sheaf','#dccb94')]:
 im=Image.new('RGBA',(16,16),base);d=ImageDraw.Draw(im)
 if name in ['household_cloth','household_ornament']:
  d.line((2,0,2,15),fill='#8c7958');d.line((13,0,13,15),fill='#8c7958')
  if name=='household_ornament':
   d.rectangle((0,10,15,12),fill='#884133')
   for x in range(1,15,4):d.polygon([(x,11),(x+1,9),(x+3,11),(x+1,13)],fill='#b95542')
 elif name=='household_weave':
  for y in [1,5,9,13]:d.line((0,y,15,y),fill='#65482c',width=1)
  for x,y in [(2,2),(7,6),(12,10)]:d.rectangle((x,y,x+2,y+2),fill='#c5a16c')
 elif name=='household_metal':d.line((0,2,15,2),fill='#747177');d.rectangle((4,5,5,6),fill='#928b85');d.rectangle((11,11,12,12),fill='#928b85')
 elif name=='household_wood':
  for x in [3,8,13]:d.line((x,0,x,15),fill='#67452d');d.line((x+1,3,x+1,10),fill='#b28a58')
 else:
  for x in [2,6,10,14]:d.line((x,0,x-1,15),fill='#8d773d');d.line((x+1,2,x+1,7),fill='#eed89a')
 im.save(A/f'textures/block/{name}.png')
for name in ['dried_berries','dried_mushrooms']:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 if name=='dried_berries':
  for x,y,c in [(3,8,'#642333'),(7,5,'#8b3540'),(10,9,'#45355c'),(6,11,'#733145')]:d.rectangle((x,y,x+2,y+2),fill=c);d.point((x,y),fill='#a15357')
  d.line((10,5,12,2),fill='#8e7145');d.line((10,5,14,4),fill='#8e7145')
 else:
  for x,y in [(2,8),(6,4),(9,10)]:d.polygon([(x,y),(x+3,y-1),(x+4,y+1),(x+2,y+3),(x,y+2)],fill='#6c4936');d.line((x+1,y+1,x+3,y+1),fill='#d2b08a')
 im.save(A/f'textures/item/{name}.png');js(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
tex={k:'slavicmyths:block/'+v for k,v in [('wood','household_wood'),('weave','household_weave'),('cloth','household_cloth'),('ornament','household_ornament'),('metal','household_metal'),('hay','household_hay')]};tex['particle']=tex['wood']
def model(name,els,extra={}):js(A/f'models/block/{name}.json',{'textures':dict(tex,**extra),'elements':els})
def states(name,variants):js(A/f'blockstates/{name}.json',{'variants':variants})
def facingVariants(modelname,extra=''):return {f'facing={f}'+(','+extra if extra else ''):{'model':'slavicmyths:block/'+modelname,'y':y} for f,y in [('north',0),('east',90),('south',180),('west',270)]}
def static(name,els):model(name,els);states(name,facingVariants(name))
vs={}
for fill in range(5):
 h=[7,9,11,13,15][fill];width=[10,11,12,13,13][fill];lo=(16-width)/2
 els=[cube([lo,0,lo],[16-lo,h-2,16-lo],'ornament')]
 if fill<3:els += [cube([lo,h-2,lo],[lo+1,h,16-lo],'cloth'),cube([15-lo,h-2,lo],[16-lo,h,16-lo],'cloth'),cube([lo,h-2,lo],[16-lo,h,lo+1],'cloth'),cube([lo,h-2,15-lo],[16-lo,h,16-lo],'cloth')]
 else:els += [cube([5,h-2,5],[11,h,11],'cloth'),cube([4.8,h-1.7,4.8],[11.2,h-1.1,11.2],'metal')]
 model('storage_sack_'+str(fill),els);vs.update(facingVariants('storage_sack_'+str(fill),'fill='+str(fill)))
states('storage_sack',vs)
for name,height in [('basket',7),('large_basket',11),('produce_crate',11)]:
 t='weave' if name!='produce_crate' else 'wood';els=[cube([1,0,1],[15,1,15],t)]
 if name=='produce_crate':
  for x,z in [(1,1),(13,1),(1,13),(13,13)]:els.append(cube([x,0,z],[x+2,12,z+2],t))
  for y in [1,5,9]:
   for a,b in [([1,y,1],[15,y+2,2]),([1,y,14],[15,y+2,15]),([1,y,2],[2,y+2,14]),([14,y,2],[15,y+2,14])]:els.append(cube(a,b,t))
 else:
  for a,b in [([1,1,1],[15,height,2]),([1,1,14],[15,height,15]),([1,1,2],[2,height,14]),([14,1,2],[15,height,14])]:els.append(cube(a,b,t))
  if name=='basket':els += [cube([1,7,7],[3,12,9],t),cube([13,7,7],[15,12,9],t),cube([3,12,7],[13,14,9],t)]
 static(name,els)
vs={}
for opened in [False,True]:
 els=[cube([1,0,1],[15,2,15],'wood'),cube([1,2,1],[15,9,2],'ornament'),cube([1,2,14],[15,9,15],'wood'),cube([1,2,2],[2,9,14],'wood'),cube([14,2,2],[15,9,14],'wood')]
 lid=cube([1,9,1],[15,11,15],'wood')
 if opened:lid['rotation']={'origin':[8,9,15],'axis':'x','angle':45,'rescale':False}
 els.append(lid)
 for x in [3,11]:
  els.append(cube([x,0,.8],[x+1.2,9,1.2],'metal'));strap=cube([x,9,.8],[x+1.2,11.1,15],'metal');
  if opened:strap['rotation']=lid['rotation'].copy()
  els.append(strap)
 els.append(cube([7,6,.5],[9,9,1.3],'metal'));m='household_chest_'+str(opened).lower();model(m,els);vs.update(facingVariants(m,'open='+str(opened).lower()))
states('household_chest',vs)
static('wooden_barrel',[cube([2,0,2],[14,3,14],'wood'),cube([1,3,1],[15,13,15],'wood'),cube([2,13,2],[14,16,14],'wood')]+[cube([.8,y,.8],[15.2,y+1.2,15.2],'metal') for y in [2,8,13]])
static('wall_shelf',[cube([0,7,10],[16,9,16],'wood'),cube([2,2,13],[4,7,16],'wood'),cube([12,2,13],[14,7,16],'wood')])
vs={}
for half in ['lower','upper']:
 h=16 if half=='lower' else 13;board=5 if half=='lower' else 7
 els=[cube([1,0,3],[3,h,14],'wood'),cube([13,0,3],[15,h,14],'wood'),cube([1,board,3],[15,board+2,14],'wood')]
 if half=='lower':els.append(cube([1,0,11],[15,2,14],'wood'))
 name='storage_shelf_'+half;model(name,els);vs.update(facingVariants(name,'half='+half))
states('storage_shelf',vs)
vs={}
for part in range(4):
 x=1 if part%2==0 else 13;y=2 if part<2 else 12
 els=[cube([x,0,6],[x+2,16,10],'wood'),cube([0,y,6],[16,y+2,10],'wood')]
 if part>=2:
  for xpos in [4,12]:els.append(cube([xpos,7,7.8],[xpos+.5,12,8.2],'metal'))
 name='drying_rack_'+str(part);model(name,els);vs.update(facingVariants(name,'part='+str(part)))
states('drying_rack',vs)
vs={}
for level in range(1,5):
 full=[9,14,22,29][level-1]
 # Four stepped rings give a rounded/conical silhouette, clipped to each physical half.
 rings=[(0,full*.4,2,14),(full*.4,full*.7,3,13),(full*.7,full*.9,4,12),(full*.9,full,6,10)]
 for half,offset in [('lower',0),('upper',16)]:
  els=[]
  for low,high,a,b in rings:
   low=max(0,low-offset);high=min(16,high-offset)
   if high>low:els.append(cube([a,low,a],[b,high,b],'hay'))
  name=f'haystack_{half}_{level}';model(name,els);vs.update(facingVariants(name,f'half={half},level={level}'))
states('haystack',vs)
for grain in ['rye','barley','oat']:
 els=[cube([5,0,5],[11,8,11],grain),cube([4,5,4],[12,6,12],'cloth')]
 if grain=='rye':
  for x,z in [(4,5),(7,4),(10,6),(5,10),(9,10)]:els.append(cube([x,6,z],[x+1,14,z+1],grain))
 elif grain=='barley':
  for x,z in [(3,5),(6,3),(10,4),(11,8),(7,11)]:els.append(cube([x,7,z],[x+2,13,z+2],grain));els.append(cube([x+.8,13,z+.8],[x+1.1,15,z+1.1],grain))
 else:
  for x,z,h in [(3,5,12),(6,3,14),(10,4,11),(11,8,13),(7,11,12)]:els.append(cube([x,6,z],[x+.7,h,z+.7],grain));els.append(cube([x-1,h-2,z],[x+2,h,z+1.8],grain))
 model(grain+'_sheaf',els,{grain:'slavicmyths:block/'+grain+'_sheaf'});states(grain+'_sheaf',facingVariants(grain+'_sheaf'))
# Inventory models: full multiblock silhouettes with explicit UV retained from local parts.
first={'storage_sack':'storage_sack_2','household_chest':'household_chest_false','storage_shelf':'storage_shelf_lower','drying_rack':'drying_rack_0','haystack':'haystack_lower_1'}
for name in names:
 if name.startswith('dried_'):continue
 m=json.loads((A/f'models/block/{first.get(name,name)}.json').read_text());m=copy.deepcopy(m)
 if name=='storage_shelf':
  upper=json.loads((A/'models/block/storage_shelf_upper.json').read_text())
  for e in upper['elements']:
   e=copy.deepcopy(e);e['from'][1]+=16;e['to'][1]+=16;m['elements'].append(e)
 if name=='drying_rack':
  for part in [1,2,3]:
   other=json.loads((A/f'models/block/drying_rack_{part}.json').read_text())
   for e in other['elements']:
    e=copy.deepcopy(e)
    for axis,off in [(0,(part%2)*16),(1,(part//2)*16)]:e['from'][axis]+=off;e['to'][axis]+=off
    m['elements'].append(e)
 m['display']={'gui':{'rotation':[30,225,0],'scale':[.35,.35,.35] if name in ['storage_shelf','drying_rack'] else [.65,.65,.65]},'ground':{'translation':[0,3,0],'scale':[.25,.25,.25]}}
 js(A/f'models/item/{name}.json',m)
# Exact crafting and single-owner multiblock loot.
patterns={'storage_sack':['CCC','C C','CTC'],'basket':['S S','SPS','SSS'],'large_basket':['S S','SPS','PSP'],'produce_crate':['P P','S S','PPP'],'household_chest':['PPP','PIP','PPP'],'wooden_barrel':['PNP','P P','PNP'],'wall_shelf':['PPP','S S'],'storage_shelf':['SPS','PPP','SPS'],'drying_rack':['STS','S S','PPP']}
keys={'P':{'tag':'minecraft:planks'},'S':{'item':'minecraft:stick'},'C':{'item':'slavicmyths:linen_cloth'},'T':{'item':'slavicmyths:linen_thread'},'I':{'item':'minecraft:iron_ingot'},'N':{'item':'minecraft:iron_nugget'}}
for name,pattern in patterns.items():js(D/f'recipe/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{c:keys[c] for c in sorted(set(''.join(pattern))) if c!=' '},'result':{'id':'slavicmyths:'+name,'count':2 if name=='wall_shelf' else 1}})
for grain in ['rye','barley','oat']:
 js(D/f'recipe/{grain}_sheaf.json',{'type':'minecraft:crafting_shaped','pattern':['GGG']*3,'key':{'G':{'item':'slavicmyths:'+grain+'_grain'}},'result':{'id':'slavicmyths:'+grain+'_sheaf','count':1}});js(D/f'recipe/{grain}_sheaf_unpack.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'slavicmyths:'+grain+'_sheaf'}],'result':{'id':'slavicmyths:'+grain+'_grain','count':9}})
js(D/'recipe/haystack.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:hay_block'}],'result':{'id':'slavicmyths:haystack','count':1}})
for name in names:
 if name.startswith('dried_'):continue
 pools=[] if name in ['storage_shelf','drying_rack','haystack'] else [{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]
 js(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':pools})
for name,ingredient,out in [('berries',{'tag':'slavicmyths:berries'},'dried_berries'),('red_mushroom',{'item':'minecraft:red_mushroom'},'dried_mushrooms'),('brown_mushroom',{'item':'minecraft:brown_mushroom'},'dried_mushrooms')]:js(D/f'recipe/drying/{name}.json',{'type':'slavicmyths:drying','ingredient':ingredient,'result':{'id':'slavicmyths:'+out,'count':1},'drying_time':2400})
def tag(name,values):js(D/f'tags/item/storage/{name}.json',{'replace':False,'values':values})
mod=lambda names:['slavicmyths:'+n for n in names]
sack=mod(['rye_grain','barley_grain','oat_grain','rye_seeds','barley_seeds','oat_seeds','flax_seeds','turnip_seeds','cabbage_seeds','pea_seeds','wheat_flour','rye_flour','oat_groats','barley_groats','pea_pod'])
tag('sack_items',sack);tag('basket_items',['#slavicmyths:berries','minecraft:apple','minecraft:egg','minecraft:red_mushroom','minecraft:brown_mushroom']+mod(['turnip','cabbage','pea_pod','goose_egg','duck_egg']))
tag('large_basket_items',['#slavicmyths:storage/basket_items','#slavicmyths:cereal_grains']+mod(['flax_stalk','dried_berries','dried_mushrooms']))
tag('crate_items',['#slavicmyths:berries','minecraft:apple','minecraft:red_mushroom','minecraft:brown_mushroom']+mod(['turnip','cabbage','pea_pod','flax_stalk','baked_turnip','dried_berries','dried_mushrooms']))
tag('barrel_items',['#slavicmyths:cereal_grains','#slavicmyths:berries','minecraft:apple','minecraft:red_mushroom','minecraft:brown_mushroom']+mod(['wheat_flour','rye_flour','oat_groats','barley_groats','dried_berries','dried_mushrooms']))
for path in [R/'data/minecraft/tags/block/mineable/axe.json']:
 data=json.loads(path.read_text());data['values']=list(dict.fromkeys(data['values']+['slavicmyths:'+n for n in names if not n.startswith('dried_')]));js(path,data)
js(D/'advancement/winter_stores.json',{'parent':'minecraft:husbandry/root','display':{'icon':{'id':'slavicmyths:dried_berries'},'title':{'translate':'advancement.slavicmyths.winter_stores.title'},'description':{'translate':'advancement.slavicmyths.winter_stores.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'dry':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':['slavicmyths:dried_berries']}]}}},'requirements':[['dry']]})
for lang,index in [('ru_ru',0),('en_us',1)]:
 path=A/f'lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'))
 for name,v in names.items():data['item.slavicmyths.'+name]=v[index];data['block.slavicmyths.'+name]=v[index]
 data['advancement.slavicmyths.winter_stores.title']=['Запасы на зиму','Winter Stores'][index];data['advancement.slavicmyths.winter_stores.description']=['Высуши ягоды для долгого хранения','Dry berries for long-term storage'][index];js(path,data)
print('Household resources: 13 blocks, 2 products, 3 drying recipes, actual inventory display only.')
