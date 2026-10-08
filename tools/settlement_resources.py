"""Deterministic 1.2.2 overlay, preserving 1.2.1 visuals and existing recipe IDs."""
from pathlib import Path
import json,re
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources'
def write(rel,obj):
 p=ROOT/rel;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def tag(group,role,values):write(f'data/slavicmyths/tags/item/villager_{group}/{role}.json',{'replace':False,'values':list(dict.fromkeys(values))})
def mod(*names):return ['slavicmyths:'+n for n in names]
def main():
 a='assets/slavicmyths/'
 im=Image.new('RGB',(16,16),'#35251b');d=ImageDraw.Draw(im)
 for y in range(16):
  for x in range(16):
   n=(x*17+y*31)%9;im.putpixel((x,y),(58+n,40+n//2,27+n//3))
 for y in (0,5,10,15):d.line((0,y,15,y),fill='#21180f')
 for x,y in [(5,2),(12,7),(4,12)]:d.line((x,y,x+2,y),fill='#25190f')
 d.rectangle((0,0,1,15),fill='#594230');d.rectangle((14,0,15,15),fill='#594230')
 p=ROOT/a/'textures/block/settlement_chest.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
 faces={f:{'texture':'#wood','uv':[0,0,16,16]} for f in ['north','south','east','west','up','down']}
 elements=[{'from':[1,0,1],'to':[15,10,15],'faces':faces},{'from':[1,10.25,1],'to':[15,15,15],'faces':faces}, {'from':[7,7,0.5],'to':[9,12,1.5],'faces':{f:{'texture':'#metal','uv':[0,0,4,8]} for f in faces}}]
 write(a+'models/block/settlement_chest.json',{'parent':'minecraft:block/block','textures':{'wood':'slavicmyths:block/settlement_chest','metal':'minecraft:block/iron_block','particle':'slavicmyths:block/settlement_chest'},'elements':elements})
 write(a+'models/item/settlement_chest.json',{'parent':'slavicmyths:block/settlement_chest','display':{'gui':{'rotation':[30,225,0],'scale':[.8,.8,.8]},'ground':{'translation':[0,3,0],'scale':[.3,.3,.3]}}})
 write(a+'blockstates/settlement_chest.json',{'variants':{f'facing={f}':{'model':'slavicmyths:block/settlement_chest','y':y} for f,y in [('north',0),('east',90),('south',180),('west',270)]}})
 write('data/slavicmyths/loot_table/blocks/settlement_chest.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:settlement_chest'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 write('data/slavicmyths/recipe/settlement_chest.json',{'type':'minecraft:crafting_shaped','pattern':['PPP','PCP','PPP'],'key':{'P':{'item':'minecraft:dark_oak_planks'},'C':{'item':'minecraft:chest'}},'result':{'id':'slavicmyths:settlement_chest','count':1}})
 for lang,name in [('ru_ru','Сундук поселения'),('en_us','Settlement Chest')]:
  p=ROOT/a/'lang'/f'{lang}.json';obj=json.loads(p.read_text(encoding='utf-8'));obj['block.slavicmyths.settlement_chest']=name;write(str(p.relative_to(ROOT)),obj)
 p=ROOT/'data/minecraft/tags/block/mineable/axe.json';obj=json.loads(p.read_text(encoding='utf-8'));obj['values']=list(dict.fromkeys(obj['values']+['slavicmyths:settlement_chest']));write(str(p.relative_to(ROOT)),obj)
 seeds=mod('rye_seeds','barley_seeds','oat_seeds','turnip_seeds','cabbage_seeds','pea_seeds','flax_seeds')
 write('data/minecraft/tags/item/villager_plantable_seeds.json',{'replace':False,'values':seeds})
 grains=['minecraft:wheat']+mod('rye_grain','barley_grain','oat_grain')
 flour=mod('wheat_flour','rye_flour','oat_groats','barley_groats')
 crops=grains+seeds+['minecraft:carrot','minecraft:potato','minecraft:beetroot','minecraft:wheat_seeds','minecraft:beetroot_seeds']+mod('turnip','cabbage','pea_pod','flax_stalk')
 fish=['minecraft:cod','minecraft:salmon','minecraft:pufferfish','minecraft:tropical_fish']+mod('raw_pike','raw_carp','raw_crayfish')
 inputs={'farmer':crops,'miller':grains,'shepherd':['minecraft:shears','minecraft:wheat','#minecraft:wool'],'fisherman':['minecraft:fishing_rod'],
 'weaver':mod('flax_stalk','flax_fiber','linen_thread'), 'herder':['minecraft:wheat','minecraft:carrot','minecraft:wheat_seeds']+grains,
 'hunter':['#slavicmyths:berries','minecraft:brown_mushroom','minecraft:red_mushroom']}
 # Exact existing player recipe ingredients/tools, not a second NPC crafting catalog.
 for role,folder in [('cook','kitchen'),('brewer','vat')]:
  values=[]
  for p in sorted((ROOT/'data/slavicmyths/recipe'/folder).glob('*.json')):
   obj=json.loads(p.read_text(encoding='utf-8'))
   def collect(x):
    if isinstance(x,list):
     for a in x:collect(a)
    elif isinstance(x,dict):
     if 'item' in x:values.append(x['item'])
     if 'tag' in x:values.append('#'+x['tag'])
     for k,v in x.items():
      if k not in ['result','remainders','catalyst']:collect(v)
   collect(obj)
  inputs[role]=values+(['minecraft:bowl'] if role=='cook' else [])
 outputs={'farmer':crops,'miller':flour,'shepherd':['#minecraft:wool'],'fisherman':fish,'weaver':mod('flax_fiber','linen_thread','linen_cloth'), 'herder':mod('goose_egg','duck_egg','goat_milk_bucket')+['minecraft:egg'], 'hunter':mod('dried_berries','dried_mushrooms')}
 for role,folder in [('cook','kitchen'),('brewer','vat')]:outputs[role]=[json.loads(p.read_text(encoding='utf-8'))['result']['id'] for p in (ROOT/'data/slavicmyths/recipe'/folder).glob('*.json')]
 # Critical logistics intermediates stop being SELL offers; levels/listing counts stay native.
 for role,conflicts in [('miller',flour),('weaver',mod('flax_fiber','linen_thread','linen_cloth'))]:
  p=ROOT/'data/slavicmyths/village_trades'/f'{role}.json';obj=json.loads(p.read_text(encoding='utf-8'))
  for level,offers in obj.items():
   for offer in offers:
    if offer['input']=='minecraft:emerald' and offer['output'] in conflicts:
     old=offer['output'];offer['input']=old;offer['output']='minecraft:emerald';offer['cost']=max(4,offer['count']*4);offer['count']=1
  write(str(p.relative_to(ROOT)),obj)
 for role,values in inputs.items():tag('inputs',role,values)
 for role,values in outputs.items():tag('outputs',role,values)
 for p in (ROOT/'data/slavicmyths/village_trades').glob('*.json'):
  obj=json.loads(p.read_text(encoding='utf-8'));tag('trade_outputs',p.stem,[v['output'] for offers in obj.values() for v in offers if v['input']=='minecraft:emerald'])
 tag('trade_outputs','farmer',['minecraft:bread','minecraft:pumpkin_pie','minecraft:apple','minecraft:cookie','minecraft:cake','minecraft:golden_carrot','minecraft:glistering_melon_slice','minecraft:suspicious_stew'])
 tag('trade_outputs','shepherd',['#minecraft:wool','#minecraft:wool_carpets','#minecraft:beds','#minecraft:banners','minecraft:shears','minecraft:painting'])
 tag('trade_outputs','fisherman',['minecraft:cooked_cod','minecraft:cooked_salmon','minecraft:cod_bucket','minecraft:campfire','minecraft:fishing_rod'])
if __name__=='__main__':main()
