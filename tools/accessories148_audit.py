"""Read-only factual union/catalog/recipe/loot/source audit. No game or generators."""
from pathlib import Path
import csv,hashlib,json,re
from PIL import Image

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';OUT=ROOT/'docs/implementation/accessories-148/audit'
BEFORE=ROOT/'docs/implementation/accessories-148/before'
BASELINE=json.loads((ROOT/'docs/implementation/accessories-148/baseline.json').read_text())
def original(p):
 q=BEFORE/p.relative_to(ROOT)
 return q if q.exists() else p
sha=lambda p:hashlib.sha256(original(p).read_bytes()).hexdigest()
# Reviewed production behavior, not tooltip-derived values. Stages are progression estimates.
FACTS={
'amber_charm':('offhand','early','fire damage x.85; charm_power x.80','item/StormProtection.java','perun_charm; shield','KEEP'),
'amulet_two_winters':('inventory/use','late: Morozko+Karachun','inventory passive +.2 knockback resistance in defensive mode; use in Nether only:10HP shield160ticks or r5 wave6/3 boss damage, CD2400; shift-use switches mode','winters/WintersEvents.java;winters/WintersRules.java;winters/WintersRecipe.java','necklace slot remains free','KEEP'),
'black_wing_amulet':('curio','late: Karachun trial','two allied ravens; default400ticks; successful activation CD3000ticks (configurable)','karachun/NightCombat.java;karachun/NightConfig.java;karachun/NightLedger.java','frost_mirror; gift_finist_amulet','KEEP'),
'depth_amulet':('curio','early/mid: aquatic materials','+1air every4ticks in water while air>0, capped at max; no swim speed','item/FolkAccessoryItem.java','pool_ring proposal uses a different slot/role','ART_REVIEW'),
'fading_flame_charm':('consumable/use','quest: see indexed source','consumes one item when GUARD absent; GUARD duration WARD_SECONDS config x20 ticks','sanctuary/SanctuaryItem.java;sanctuary/SanctuaryConfig.java;sanctuary/SanctuaryContent.java','mor_ward; not a Curios slot','KEEP'),
'finist_quiver':('curio','mid/late: Finist quest','12 magic-arrow charges; +1charge/160ticks equipped, used by FinistBow; persists ItemState','finist/FinistQuiver.java;finist/FinistBow.java','other belt items','KEEP'),
'forest_charm':('offhand','early','forest_spirits incoming damage x.85; charm_power x.80','item/StormProtection.java','veles_amulet uses necklace','KEEP'),
'frost_mirror':('curio','late: Morozko trial','player ice double15HP, life600ticks, CD3600; safe-space check','morozko/FrostSkills.java;morozko/FrostRules.java;morozko/FrostLedger.java','black_wing_amulet; necklaces','KEEP'),
'gift_alyosha_belt':('curio','mid/late: Skim victory with Alyosha participation','safe-side dodge from selected heavy windup within4blocks targeting player; CD800ticks','rewards/RewardEvents.java;rewards/RewardLedger.java','hunter_belt; finist_quiver','KEEP'),
'gift_dobrynya_ring':('curio','mid/late: Marinka victory + Dobrynya released','successful shield block opens80tick counter window; next hostile melee x1.75; CD240ticks','rewards/RewardEvents.java;rewards/RewardLedger.java','perun_ring; GreyPack ring','KEEP'),
'gift_finist_amulet':('curio','late: Kartaus victory with Finist','every80ticks detects up to30 visible hostile entities within18; windup warning CD20ticks','rewards/RewardEvents.java;rewards/RewardLedger.java','other necklaces','KEEP'),
'grave_ward':('curio','early/mid: burial chest','incoming damage from undead LivingEntity x.91; bypass_invulnerability excluded','kurgan/BurialAccessories.java','retribution_charm; bath_ward proposal','BALANCE_REVIEW'),
'grivna':('curio','early/mid: burial chest','incoming damage not bypassing armor x.96; bypass_invulnerability excluded','kurgan/BurialAccessories.java','lunula; utility necklaces','BALANCE_REVIEW'),
'hunter_belt':('curio','early/mid','projectile damage x1.10 only against WildlifeEntity','item/FolkEquipmentEffects.java','underground_guard_belt; finist_quiver','BALANCE_REVIEW'),
'hunter_charm':('use','early','right-click invokes HuntItems.detect; item cooldown400ticks','item/SimpleCharmItem.java;hunt/HuntItems.java','obereg_ohotnika','KEEP'),
'invisibility_cap':('curio','mid/late','20tick fade; duration160ticks; broken by hurt/swing/sprint/use for100ticks; after expiry cooldown600ticks','item/FolkAccessoryItem.java;client/FolkEquipmentLayer.java','miners_goggles','ART_REVIEW'),
'lunula':('curio','early/mid: burial chest','night-only LivingEntity source incoming damage x.95; bypass_invulnerability excluded','kurgan/BurialAccessories.java','grivna; necklaces','BALANCE_REVIEW'),
'miners_goggles':('curio','mid: underground','night vision refreshed520ticks when remaining<500; external night vision preserved/restored','industry/IndustryAccessories.java','invisibility_cap','KEEP'),
'mor_ward':('quest/use','quest: Mor ritual','right-click consumes ordinary copy, applies24000tick Mor ward: mor_block damage x.5; special GuardianWard196 copy not consumed','quest185/Quest185Item.java;quest185/Quest185Events.java;quest185/Quest185Story.java','ritual tools; no equipment slot','KEEP'),
'obereg_ohotnika':('use','hunt progression','HuntItems.Role.DETECTOR; right-click bounded detection; no Curios passive','hunt/HuntItems.java','hunter_charm','BALANCE_REVIEW'),
'obereg_padayuschey_zvezdy':('curio','mid: fire serpent ingredients','fallDistance>=5.5 and verticalSpeed<-.15 -> SlowFalling100ticks, clears fall distance; CD1200ticks; excludes water/flying/passenger','hunt/StarCharmEffects.java;hunt/ElementRules.java','stone_path_amulet; proposed podvey_belt','KEEP'),
'odnoglazyy_obereg':('curio','late: Likho ingredients','20% chance halve finite harmful duration1..72000; CD600ticks; excludes BAD_OMEN and protected burial curses; proc recursion guard','hunt/BossEffects.java','grivna; bath_ward proposal','KEEP'),
'perun_charm':('offhand','early/mid: ritual','lightning damage x.70; charm_power x.65; separate armor set multiplier retained','item/StormProtection.java;ritual/Rituals.java','perun_ring','KEEP'),
'perun_ring':('curio','mid','direct melee +2 raw damage CD60ticks; lightning incoming x.65','item/FolkEquipmentEffects.java','resin_ring; spark_ring proposal','ART_REVIEW'),
'poyas_tugarina':('curio','late: Tugarin ingredients','after40ticks sprinting +.2 knockback resistance, refreshed20ticks; melee knockback x1.15; disabled swimming/flying','hunt/BossEffects.java','volchiy_poyas; other belts','KEEP'),
'prospectors_ring':('curio','mid: underground','mining speed x1.12 for correct pickaxe on c:ores, multiplied with existing mining runes','industry/IndustryAccessories.java;rpg/AdvancedRunes.java','other rings','KEEP'),
'resin_ring':('curio','early/mid','heal1HP/100ticks at food>=18 when hurt, exhaustion2; duplicate copies do not multiply callback','item/FolkAccessoryItem.java','perun_ring; other rings','ART_REVIEW'),
'retribution_charm':('curio','mid/late','consumed on otherwise lethal non-void death:1HP,40invuln ticks, ResistanceII60ticks; attributed50 damage to living non-allied killer','item/FolkEquipmentEffects.java','grave_ward; bath_ward proposal','ART_REVIEW'),
'ring_of_grey_pack':('curio','quest: Grey Wolf','two wolves20HP/6attack; arrival10ticks; expiry810ticks from activation; player-owned CD1800ticks; no unsafe spawn','finist/GreyPackRing.java;military/GreyWolfStory.java','two physical ring slots','KEEP'),
'stone_path_amulet':('curio','mid: underground','final fall damage x.75','industry/IndustryAccessories.java','falling-star charm; proposed podvey_belt','KEEP'),
'traveler_charm':('offhand','early','fall damage x.85; charm_power x.80','item/StormProtection.java','stone_path_amulet uses necklace','KEEP'),
'trophy_skim_necklace':('curio','mid/late: Skim fang trophy craft','hostile melee/projectile damage x1.10 if target HP<30%; no cooldown','rewards/RewardEvents.java;rewards/RewardLedger.java','perun damage ring; other necklaces','BALANCE_REVIEW'),
'underground_guard_belt':('curio','mid: underground','+1armor and +.10 knockback resistance while equipped; independent modifier IDs','industry/IndustryAccessories.java','hunter_belt; volchiy_poyas','KEEP'),
'veles_amulet':('curio','early/mid','Wildlife aggression threshold x.55; Rusalka charm buildup x.5','entity/WildlifeEntity.java;water/RusalkaSong.java','forest_charm; necklaces','ART_REVIEW'),
'volchiy_poyas':('curio','mid: wolf trophy craft','night movement +8%, full moon+11%, none in daytime','hunt/HuntEffects.java;hunt/HuntRules.java','hunter_belt; Tugarin belt','KEEP'),
'volkhv_amulet':('component','see indexed recipes','plain Item; no dedicated equipment/use hook found in all Java source; not a wearable passive','registry/ModItems.java','ritual components','INVESTIGATE'),
'warding_charm':('component','early ritual','plain Item; ritual/path offering ingredient and ritual result; no wearable passive','registry/ModItems.java;ritual/Rituals.java;rpg/PathData.java','ritual components','KEEP'),
'zolny_obereg':('curio','mid: Ovinnik ingredients','fire damage x.8; newly observed burning duration ceil(x.6)','hunt/HuntEffects.java','amber_charm; necklaces','KEEP')}

def walk(value,path='$'):
 yield path,value
 if isinstance(value,dict):
  for k,v in value.items():yield from walk(v,path+'.'+k)
 elif isinstance(value,list):
  for i,v in enumerate(value):yield from walk(v,path+f'[{i}]')

def main():
 OUT.mkdir(parents=True,exist_ok=True)
 catalog={a[0]:a[1:] for line in original(A/'creative_catalog.tsv').read_text(encoding='utf8').splitlines() if line and not line.startswith('#') for a in [line.split('\t')]}
 # On subsequent runs preserve the original audit population after the tab transfer.
 initial=OUT/'population.json'
 if initial.exists():population=json.loads(initial.read_text())
 else:
  slots={}
  for s in ['head','necklace','ring','belt','charm']:
   for v in json.loads((RES/f'data/curios/tags/item/{s}.json').read_text())['values']:slots.setdefault(v.split(':')[1],[]).append(s)
  charms=[i for i,v in catalog.items() if v==['magic','charms']];population={'magic_charms':charms,'curios':slots,'ids':sorted(set(charms)|set(slots))};initial.write_text(json.dumps(population,indent=2)+'\n')
 ids=population['ids'];assert len(ids)==38 and set(ids)==set(FACTS)
 language=json.loads(original(A/'lang/ru_ru.json').read_text(encoding='utf8'))
 java={p.relative_to(ROOT/'src/main/java/org/slavicmyths').as_posix():original(p).read_text(encoding='utf8') for p in (ROOT/'src/main/java/org/slavicmyths').rglob('*.java') if p.relative_to(ROOT).as_posix() in BASELINE}
 resources=[]
 for p in (RES/'data/slavicmyths').rglob('*.json'):
  if p.relative_to(ROOT).as_posix() in BASELINE and ('/recipe/' in p.as_posix() or '/loot_table/' in p.as_posix()):resources.append((p,json.loads(p.read_text(encoding='utf8'))))
 entries=[];sources={};scan_counts={'recipe_json':sum('/recipe/' in p.as_posix() for p,_ in resources),'loot_json':sum('/loot_table/' in p.as_posix() for p,_ in resources),'entity_loot_json':sum('/loot_table/entities/' in p.as_posix() for p,_ in resources),'java_files':len(java)}
 for item in ids:
  kind,stage,effect,code,competitor,decision=FACTS[item];needle='slavicmyths:'+item;recipes=[];ingredients=[];loot=[];code_refs=[]
  for p,value in resources:
   rel=p.relative_to(ROOT).as_posix()
   for ptr,v in walk(value):
    if '/recipe/' in rel and isinstance(v,dict) and any(v.get(k)==needle for k in ['id','item']) and ('.result' in ptr or '.output' in ptr):recipes.append({'path':rel,'pointer':ptr,'data':value})
    if '/recipe/' in rel and v==needle and '.result' not in ptr and '.output' not in ptr:ingredients.append({'path':rel,'pointer':ptr})
    if '/loot_table/' in rel and isinstance(v,dict) and v.get('type') in ['minecraft:item','item'] and v.get('name')==needle:loot.append({'path':rel,'pointer':ptr,'entry':v,'pools':value.get('pools',[])})
  for path,text in java.items():
   for no,line in enumerate(text.splitlines(),1):
    if re.search(r'\b'+re.escape(item.upper())+r'\b',line) or '"'+item+'"' in line:code_refs.append({'path':'src/main/java/org/slavicmyths/'+path,'line':no,'text':line.strip()})
  for path in code.split(';'):
   assert path in java,(item,path)
  model=A/f'models/item/{item}.json';m=json.loads(original(model).read_text());gui=m.get('perspectives',{}).get('gui',m);chain=[];textures={}
  def resolve(obj,seen):
   chain.append(obj)
   for k,v in obj.get('textures',{}).items():textures.setdefault(k,v)
   parent=obj.get('parent','')
   if parent.startswith('slavicmyths:') and parent not in seen:
    p=A/'models'/(parent.split(':')[1]+'.json');resolve(json.loads(original(p).read_text()),seen|{parent})
  resolve(gui,set());pngs=[]
  for ref in set(textures.values()):
   if ref.startswith('slavicmyths:'):
    p=A/'textures'/(ref.split(':')[1]+'.png')
    if p.is_file():
     with Image.open(p) as im:pngs.append({'path':p.relative_to(ROOT).as_posix(),'size':list(im.size),'sha256':sha(p)})
  worn={'gift_alyosha_belt':'FolkEquipmentLayer body','gift_finist_amulet':'FolkEquipmentLayer body','trophy_skim_necklace':'FolkEquipmentLayer body','invisibility_cap':'FolkEquipmentLayer head + invisibility override','veles_amulet':'FolkEquipmentLayer body','hunter_belt':'FolkEquipmentLayer body','poyas_tugarina':'CuriosAccessories -> BossAccessoryRenderer BELT','odnoglazyy_obereg':'CuriosAccessories -> BossAccessoryRenderer EYE','obereg_padayuschey_zvezdy':'CuriosAccessories -> HuntAccessoryRenderer STAR','zolny_obereg':'CuriosAccessories -> HuntAccessoryRenderer CHARM','volchiy_poyas':'CuriosAccessories -> HuntAccessoryRenderer BELT','miners_goggles':'MiningGogglesRenderer','finist_quiver':'CuriosAccessories -> FinistQuiverRenderer'}.get(item,'NONE: no dedicated registration/layer found')
  move=kind not in ['component','quest/use','consumable/use']
  row={'id':item,'name_ru':language.get('item.slavicmyths.'+item),'type':kind,'curios_slot':','.join(population['curios'].get(item,[])),'stage_estimate':stage,'effect_and_cooldown':effect,'competitors':competitor,'decision':decision,'creative_before':'/'.join(catalog[item]),'move_to_accessories':move,'model':model.relative_to(ROOT).as_posix(),'model_sha256':sha(model),'gui_kind':'elements' if any('elements' in x for x in chain) else 'generated' if any(x.get('parent')=='minecraft:item/generated' for x in chain) else gui.get('parent',gui.get('loader','unknown')),'textures':pngs,'worn_renderer':worn,'effect_sources':['src/main/java/org/slavicmyths/'+p for p in code.split(';')],'recipes':recipes,'recipe_uses':ingredients,'loot_entries':loot,'java_refs':code_refs,'obtaining_note':'JSON receipts plus runtime reward/ritual/quest source references; probabilities are raw entries/conditions, not measured rates','runtime':'NOT_RUN / OWNER_PENDING'}
  entries.append(row)
  for p in row['effect_sources']+[row['model']]+[x['path'] for x in pngs]+[x['path'] for x in recipes+loot]:sources[p]=sha(ROOT/p)
 (OUT/'inventory.json').write_text(json.dumps(entries,ensure_ascii=False,indent=2)+'\n',encoding='utf8');(OUT/'source-hashes.json').write_text(json.dumps(sources,indent=2)+'\n')
 keys=['id','name_ru','type','curios_slot','stage_estimate','effect_and_cooldown','competitors','decision','move_to_accessories','model','gui_kind','worn_renderer','effect_sources','recipes','loot_entries','runtime']
 with (OUT/'INVENTORY.tsv').open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=keys,delimiter='\t',extrasaction='ignore');w.writeheader()
  for x in entries:
   y=dict(x)
   for k in ['effect_sources','recipes','loot_entries']:y[k]=json.dumps(y[k],ensure_ascii=False)
   w.writerow(y)
 summary={**scan_counts,'population':len(ids),'curios_ids':len(population['curios']),'magic_charms':len(population['magic_charms']),'transfer_ids':[x['id'] for x in entries if x['move_to_accessories']],'excluded':[x['id'] for x in entries if not x['move_to_accessories']],'direct_entity_drops':{x['id']:[e['path'] for e in x['loot_entries'] if '/entities/' in e['path']] for x in entries if any('/entities/' in e['path'] for e in x['loot_entries'])},'runtime_launches':0}
 (OUT/'summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n');print(json.dumps(summary,ensure_ascii=False,indent=2))
if __name__=='__main__':main()
