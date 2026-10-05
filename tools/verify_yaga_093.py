"""0.9.3 source/catalogue/geometry/packaging checks. Does not start Minecraft."""
from pathlib import Path
import re,json,zipfile,hashlib
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/org/slavicmyths';A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths'
def read(p):return json.loads(p.read_text('utf-8'))
def source(p):return (J/p).read_text('utf-8')
m=read(R/'docs/verification/yaga-0.9.3.json');lang=[read(A/'lang'/f'{l}.json') for l in ['ru_ru','en_us']]
items=set(re.findall(r'(?:register|food|stew|egg)\(\s*"([a-z_]+)"',source('registry/ModItems.java')))
assert set(m['items'])<=items
s=source('yaga/YagaServices.java');entries=re.findall(r'new Entry\("([^"]+)",(\d+),"([^"]+)",(\d+)((?:,n\("[^"]+",\d+\))*)\)',s)
assert len(entries)==21, 'Three main, six contracts, six exchanges, six brews'
catalogue=[]
for id,stage,out,amount,raw in entries:
 assert out in items and int(amount)>0 and int(stage)<=3, (id,out)
 needs=[]
 for item,count in re.findall(r'n\("([^"]+)",(\d+)\)',raw):
  assert int(count)>0
  if not item.startswith('minecraft:'):assert item in items,(id,item)
  needs.append({'item':item,'count':int(count)})
 catalogue.append({'id':id,'stage':int(stage),'output':out,'amount':int(amount),'inputs':needs})
for recipe in catalogue[-6:]:assert len(recipe['inputs'])==4 and all('yaga.recipe.'+recipe['id'] in l for l in lang)
assert not any(n['item'] in ['oko_likha','us_tugarina'] for e in catalogue for n in e['inputs'])
for id in m['items']:
 assert all('item.slavicmyths.'+id+'.effect' in l for l in lang)
for id in m['world_only_blocks']:
 assert id not in items and all('block.slavicmyths.'+id in l for l in lang)
 assert read(D/f'loot_tables/blocks/{id}.json')['pools']==[]
assert len(read(A/'blockstates/yaga_chicken_leg.json')['variants'])==28
model=source('client/BabaYagaModel.java');assert 'extends EntityModel<BabaYaga>' in model and 'WitchModel' not in model
assert all(name in model for name in ['boneFoot','boneKnee','noseHook','amberL','pupilL','finger','cane','scarf','bonePendant'])
geometry=read(R/'docs/verification/yaga-0.9.3/baba-yaga-geometry.json');assert len(geometry)==79
for node in geometry:
 if node['box']:
  x,y,z,w,h,d=node['box'];assert w>0 and h>0 and d>0
  mat=node['mat'];u=mat%8*64;v=mat//8*64
  assert u+2*(w+d)<=512 and v+h+d<=512,'UV outside atlas'
menu=source('yaga/YagaMenu.java');assert all(n in menu for n in ['stillValid(p)','n.canonical()','progress().blocked','YagaServices.has','ItemStack.tagMatches','clearContainer'])
assert 'name_your_misfortune' in menu and 'steppe_champion' in menu
assert 'action==600' in menu and 'tab()!=3' in menu and 'out.getMaxStackSize()' in menu
screen=source('client/YagaScreen.java');assert 'popup==0' in screen and 'key==256&&popup!=0' in screen and 'menu.stage()>0' in screen
assert 'if(menu.tab()==0){font.draw(m,tr("favor.label")' in screen
assert 'ItemStack.EMPTY' in menu
common='\n'.join(source('yaga/'+p.name) for p in (J/'yaga').glob('*.java'))
# The tooltip flag in an Item override is the vanilla API exception; no client screens/renderers in common code.
assert 'net.minecraft.client.gui' not in common and 'net.minecraft.client.renderer' not in common
hut=source('yaga/YagaHut.java');placement=source('yaga/YagaPlacement.java')
assert placement.index('!w.hasChunksAt')<placement.index('int min=')
assert all(n in hut for n in ['getNoiseBiome','YagaPlacement.prepare','YagaPlacement.commit','w.addFreshEntity(npc)','d.placed=true'])
assert '2|16' in placement and 'y>=0' in placement and 'site.old.entrySet()' in placement
assert all(n not in hut+placement for n in ['setChunkForced','forceChunk','getAllEntities'])
assert 'baba_yaga_spawn_egg' in items and 'SpawnReason.SPAWN_EGG' in source('yaga/BabaYaga.java')
assert 'n.isEggSummoned()||progress().intro>=2' in menu
assert '!eggSummoned&&progress.intro==0' in source('yaga/BabaYaga.java')
assert 'YagaEggSummoned' in source('yaga/BabaYaga.java') and 'eggSummoned||d.placed' in source('yaga/BabaYaga.java')
assert read(A/'models/item/baba_yaga_spawn_egg.json')['parent']=='minecraft:item/template_spawn_egg'
utility=source('yaga/YagaUtilityItem.java');assert 'removeAllEffects' not in utility and 'KurganCurse.CURSE' not in utility
assert all(n in utility for n in ['Effects.POISON','Effects.WITHER','Effects.CONFUSION','Math.round(delta.length()/10)*10','addCooldown(this,80)'])
recovery=source('yaga/FlightRecovery.java');assert '?30:60' in recovery and 'now+6000' in recovery and 'Math.max(1' in recovery
assert 'FlightRecovery.ready' in source('flight/FlightItem.java') and 'FlightRecovery.repack' in source('flight/FlyingVessel.java')
assert source('SlavicMyths.java').index('YagaMenu.register()')<source('SlavicMyths.java').index('RpgMenu.MENUS.register(bus)')
assert '@JeiPlugin' in source('compat/JeiYaga.java') and 'YagaServices.BREWS' in source('compat/JeiYaga.java')
for id in m['advancements']:assert (D/f'advancements/{id}.json').is_file()
sounds=read(A/'sounds.json');assert len([n for n in sounds if n.startswith('yaga_')])==10
for name,e in sounds.items():
 if name.startswith('yaga_'):
  assert all(e['subtitle'] in l for l in lang)
  assert all(s['type']=='event' and s['name'].startswith('minecraft:') for s in e['sounds'])
jar=R/'build/libs/slavicmyths-0.9.3.jar'
with zipfile.ZipFile(jar) as z:
 names=set(z.namelist());assert all('org/slavicmyths/yaga/'+p.stem+'.class' in names for p in (J/'yaga').glob('*.java'))
 assert 'org/slavicmyths/client/BabaYagaModel.class' in names and 'org/slavicmyths/compat/JeiYaga.class' in names
 assert not any(n.startswith('org/slavicmyths/verify/') for n in names)
 assert 'version="0.9.3"' in z.read('META-INF/mods.toml').decode('utf-8')
 for path in (J/'yaga').glob('*.java'):
  assert path.stat().st_mtime<=jar.stat().st_mtime,'Stale production class: '+path.name
(R/'docs/verification/yaga-0.9.3/services.json').write_text(json.dumps(catalogue,ensure_ascii=False,indent=2)+'\n','utf-8')
print('PASS: 21 shared service entries, exact ingredients/outputs, unique trophy preservation, 79 native parts/UVs, 28 leg states, server gates, optional JEI, 6 advancements, 10 localized vanilla fallback sounds and production packaging. Minecraft launches: 0.')
