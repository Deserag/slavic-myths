import json
from pathlib import Path
R=Path('src/main/resources');A=R/'assets/slavicmyths';D=R/'data/slavicmyths'
def put(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
# Brick end grain is authored separately, and construction models use it as well.
for family,top in [('pale_blue','burial_stone_top'),('bog_green','bog_green_kurgan_stone')]:
 for p in (A/'models/block').glob('*'+family+'_kurgan_cobblestone*.json'):
  v=json.loads(p.read_text(encoding='utf-8-sig'));t=v.get('textures',{})
  if t.get('all'):
   side=t.pop('all');v['parent']='minecraft:block/cube_bottom_top';t.update({'side':side,'top':'slavicmyths:block/'+top,'bottom':'slavicmyths:block/'+top})
  elif 'top' in t:t['top']=t['bottom']='slavicmyths:block/'+top
  put(p,v)
# New recipes are confined to burial materials, without changing combat stats.
for family in ['pale_blue','bog_green']:
 base=family+'_kurgan_stone';brick=family+'_kurgan_cobblestone'
 put(D/'recipe'/f'{brick}.json',{'type':'minecraft:crafting_shaped','pattern':['##','##'],'key':{'#':{'item':'slavicmyths:'+base}},'result':{'id':'slavicmyths:'+brick,'count':4}})
 put(D/'recipe'/f'cracked_{brick}.json',{'type':'minecraft:smelting','ingredient':{'item':'slavicmyths:'+brick},'result':{'id':'slavicmyths:cracked_'+brick},'experience':0.1,'cookingtime':200})
 put(D/'recipe'/f'mossy_{brick}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'slavicmyths:'+brick},{'item':'minecraft:moss_block'}],'result':{'id':'slavicmyths:mossy_'+brick,'count':1}})
put(D/'recipe/pale_blue_kurgan_stone_stonecutting.json',{'type':'minecraft:stonecutting','ingredient':{'item':'minecraft:stone'},'result':{'id':'slavicmyths:pale_blue_kurgan_stone','count':1}})
put(D/'recipe/bog_green_kurgan_stone.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'minecraft:stone'}]*4+[{'item':'minecraft:moss_block'}],'result':{'id':'slavicmyths:bog_green_kurgan_stone','count':4}})
p=R/'data/minecraft/tags/block/mineable/pickaxe.json';tag=json.loads(p.read_text(encoding='utf-8-sig'));bases=[f+'_kurgan_'+k if k in ['stone','cobblestone'] else k+'_'+f+'_kurgan_cobblestone' for f in ['pale_blue','bog_green'] for k in ['stone','cobblestone','cracked','mossy']];tag['values']=list(dict.fromkeys(tag['values']+['slavicmyths:'+n for n in bases]));put(p,tag)
# Guaranteed baseline rolls, with low-weight existing rare objects. No new reward items.
def item(name,weight,count=None):
 v={'type':'minecraft:item','name':name if ':' in name else 'slavicmyths:'+name,'weight':weight}
 if count:v['functions']=[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':count[0],'max':count[1]}}]
 return v
common=[item('pottery_fragment',12,(1,3)),item('old_arrowhead',10,(1,3)),item('minecraft:iron_nugget',12,(2,7)),item('minecraft:gold_nugget',6,(1,4)),item('grave_cloth_scrap',7),item('ancient_comb',5),item('old_buckle',5),item('minecraft:bone',8,(1,3))]
burial=[item('ancient_fibula',8),item('ancient_beads',8),item('lunula',2),item('grivna',2),item('grave_ward',2),item('ancient_carolingian_sword',1),item('ancient_spear',2),item('ancient_chekan',1)]
warrior=[item('shield_boss_fragment',8),item('druzhinnik_blade_fragment',8),item('old_arrowhead',10,(2,4)),item('ancient_spear',3),item('ancient_chekan',2),item('ancient_carolingian_sword',2)]
ritual=[item('torn_burial_ribbon',10),item('nav_essence',4),item('volkhv_amulet',1),item('grave_ward',2),item('lunula',2)]
treasury=[item('minecraft:gold_nugget',14,(3,8)),item('ancient_fibula',8),item('ancient_beads',8),item('grivna',2),item('lunula',2),item('old_buckle',5),item('minecraft:iron_ingot',4,(1,2))]
for name,rolls,entries in [('common_cache',(2,4),common),('burial',(2,5),common+burial),('warrior',(2,5),common+warrior),('ritual',(2,5),common+ritual),('treasury',(4,7),treasury),('secret',(2,4),burial+ritual+treasury),('great_special',(3,6),burial+warrior+treasury)]:
 put(D/'loot_table/kurgan'/f'{name}.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':rolls[0],'max':rolls[1]},'entries':entries}]})
print('KURGAN lootTables=7 oldTablesPreserved=15 masonryRecipes=6')
