from pathlib import Path
import json,random,zipfile,io
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data'
def write(p,j):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for face in ['side','top','front','front_on']:
 im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im);rng=random.Random(face)
 for y in range(16):
  for x in range(16):
   mortar=y%5==0 or (x+(8 if y//5%2 else 0))%12==0;v=23 if mortar else 43+rng.randrange(-7,8);im.putpixel((x,y),(v,v-4,v-2,255))
 dr.rectangle((0,0,15,15),outline='#806d44')
 if face.startswith('front'):
  dr.rectangle((3,2,12,5),fill='#151314',outline='#786343');dr.rectangle((2,8,13,14),fill='#161112',outline='#a08643')
  if face=='front_on':
   dr.rectangle((3,10,12,13),fill='#a83b1c');dr.rectangle((5,11,10,13),fill='#ef9c35')
  for x in [5,8,11]:dr.line((x,9,x,13),fill='#494044')
 if face=='top':dr.rectangle((4,4,11,11),outline='#aa8b48');dr.line((5,8,10,8),fill='#d56a2a')
 p=A/'textures/block'/f'infernal_furnace_{face}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
im=Image.new('RGBA',(16,16));dr=ImageDraw.Draw(im);dr.polygon([(3,12),(1,8),(4,3),(10,1),(14,5),(13,12),(9,15)],fill='#30272e',outline='#15121a');dr.line((3,7,7,8,9,4,12,5),fill='#eb7c28',width=2);dr.line((7,8,6,12,10,13),fill='#f2bd53');im.save(A/'textures/item/infernal_coal.png')
for on in [False,True]:write(A/'models/block'/('infernal_furnace'+('_on' if on else '')+'.json'),{'parent':'minecraft:block/orientable','textures':{'side':'slavicmyths:block/infernal_furnace_side','top':'slavicmyths:block/infernal_furnace_top','front':'slavicmyths:block/infernal_furnace_front'+('_on' if on else '')}})
write(A/'blockstates/infernal_furnace.json',{'variants':{f'facing={face},lit={str(on).lower()}':{'model':'slavicmyths:block/infernal_furnace'+('_on' if on else ''),'y':rotation} for face,rotation in [('north',0),('east',90),('south',180),('west',270)] for on in [False,True]}})
write(A/'models/item/infernal_furnace.json',{'parent':'slavicmyths:block/infernal_furnace'});write(A/'models/item/infernal_coal.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/infernal_coal'}})
write(D/'slavicmyths/loot_table/blocks/infernal_furnace.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:infernal_furnace','functions':[{'function':'minecraft:copy_components','source':'block_entity','include':['minecraft:custom_name']}]}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
write(D/'slavicmyths/recipe/infernal_coal.json',{'type':'minecraft:crafting_shaped','pattern':['CCC','CMC','CCC'],'key':{'C':{'item':'minecraft:coal'},'M':{'item':'slavicmyths:magma_clot'}},'result':{'id':'slavicmyths:infernal_coal','count':9}})
write(D/'slavicmyths/recipe/infernal_furnace.json',{'type':'minecraft:crafting_shaped','pattern':['IMI','CFB','IMI'],'key':{k:{'item':v} for k,v in dict(I='minecraft:iron_ingot',M='minecraft:magma_block',C='slavicmyths:magma_clot',F='minecraft:furnace',B='slavicmyths:bone_thorn').items()},'result':{'id':'slavicmyths:infernal_furnace','count':1}})
for n in ['infernal_coal','infernal_furnace']:write(D/'slavicmyths/advancement/recipes/nether'/f'{n}.json',{'parent':'minecraft:recipes/root','criteria':{'material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'slavicmyths:magma_clot'}]}}},'rewards':{'recipes':['slavicmyths:'+n]}})
for name in ['mineable/pickaxe','needs_stone_tool']:
 p=D/'minecraft/tags/block'/f'{name}.json';j=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'replace':False,'values':[]};j['values']=list(dict.fromkeys(j['values']+['slavicmyths:infernal_furnace']));write(p,j)
p=A/'creative_catalog.tsv';s=p.read_text(encoding='utf-8')
for n,cat in [('infernal_coal','resources'),('infernal_furnace','decor')]:
 if not any(l.split('\t')[0]==n for l in s.splitlines()):s+=n+'\t'+cat+'\tnether_station\n'
p.write_text(s,encoding='utf-8')
for lang,labels in [('ru_ru',['Адская печь','Адский уголь','Только Адский уголь']),('en_us',['Infernal Furnace','Infernal Coal','Infernal coal only'])]:
 p=A/'lang'/f'{lang}.json';j=json.loads(p.read_text(encoding='utf-8'));j.update(dict(zip(['block.slavicmyths.infernal_furnace','item.slavicmyths.infernal_coal','infernal_furnace.fuel'],labels)));write(p,j)
print('Infernal furnace prototype assets, fuel and recipes generated; BALANCE_OWNER_PENDING')
