"""Current #273 scope, graph of actual ritual sources, and packaged artifact evidence."""
from pathlib import Path
import json,subprocess,hashlib,zipfile
from rituals273_text import expected
R=Path(__file__).resolve().parents[1];BASE='feccd53167d5d4de1368a6636106d3494725fa35';J='src/main/java/org/slavicmyths/'
OWNED=['sanctuary/SanctuaryHeart.java','sanctuary/SanctuaryRecords.java','sanctuary/SanctuaryDecor.java','guardians/GuardianWorld.java','guardians/GuardianLedger.java']
LANG=['src/main/resources/assets/slavicmyths/lang/'+loc+'.json' for loc in ['ru_ru','en_us']]
def old(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R).decode('utf-8').replace('\r\n','\n')
def scope():
 changed=subprocess.check_output(['git','diff',BASE,'--name-only','--','src/main'],cwd=R,text=True).splitlines()
 added=subprocess.check_output(['git','ls-files','--others','--exclude-standard','src/main'],cwd=R,text=True).splitlines()
 assert set(changed+added)<=set(J+p for p in OWNED)|set(LANG),(changed,added)
 for index,path in enumerate(LANG):
  before=json.loads(old(path));after=json.loads((R/path).read_text(encoding='utf-8'));before.update(expected(before,index));assert before==after,('unexpected translation',path)
 for p in OWNED:assert 'net.minecraft.client' not in (R/J/p).read_text(encoding='utf-8')
 return True
def source_graph():
 d=R/'src/main/resources/data/slavicmyths'
 for table in ['fire_guaranteed','fire_regular']:
  pools=json.loads((d/f'loot_table/chests/{table}.json').read_text())['pools']
  guaranteed=[p for p in pools if not p.get('conditions') and p['rolls']==1 and len(p['entries'])==1 and p['entries'][0].get('name')=='slavicmyths:sacred_ember']
  assert guaranteed,('ember not guaranteed',table)
 recipes={'purifying_ash':(['sacred_ember','minecraft:paper'],'purifying_ash',2),'cleansing_powder_from_ash':(['purifying_ash','minecraft:bone_meal'],'cleansing_powder',4)}
 for name,(ingredients,result,count) in recipes.items():
  recipe=json.loads((d/f'recipe/{name}.json').read_text());assert recipe['type']=='minecraft:crafting_shapeless';assert recipe['ingredients']==[{'item':i if ':' in i else 'slavicmyths:'+i} for i in ingredients];assert recipe['result']=={'id':'slavicmyths:'+result,'count':count}
 ward=json.loads((d/'recipe/mor_ward.json').read_text());assert ward['type']=='slavicmyths:gorynych_upgrade' and ward['result']['id']=='slavicmyths:mor_ward'
 assert [e['item'] for e in ward['ingredients']]==['slavicmyths:mor_fire_amulet_fragment','slavicmyths:mor_water_amulet_fragment','slavicmyths:eye_of_mor']
 heart=(R/J/'sanctuary/SanctuaryHeart.java').read_text(encoding='utf-8')
 assert 'restore(instance,()->activate(w))' in heart and 'pool(instance,pool,()->activate(w))' in heart
 assert heart.index('restore(instance,()->activate(w))')<heart.index('stack.shrink(1)')
 decor=(R/J/'sanctuary/SanctuaryDecor.java').read_text(encoding='utf-8');assert 'useItemOn' in decor and '"sealed_spring"' in decor
 ledger=(R/J/'guardians/GuardianLedger.java').read_text(encoding='utf-8')
 for token in ['rewardAvailable(instance)','getFreeSlot()<0','receipt.getBoolean(key)','receipt.putBoolean(key,true)','slavicmyths$saveFrost(p)','claimed(p.getUUID(),spring)']:assert token in ledger,token
 # These hooks already implemented the intended surviving guardian, completion and delivery.
 entity=(R/J/'guardians/GuardianEntity.java').read_text(encoding='utf-8')
 for token in ['beginPurification()','GuardianRules.threshold(water)','finishPurification(ServerPlayer p)','stage(instance,"CLEARED")','sceneAge>=GuardianRules.PURIFY_SCENE']:assert token in entity,token
 assert entity==old(J+'guardians/GuardianEntity.java'),'combat/save controller changed unexpectedly'
 print('PASS real marker/table/recipe/interaction/reward graph; actual drops and MP NOT_RUN')
if __name__=='__main__':
 scope();source_graph()
 version=dict(line.split('=',1) for line in (R/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))['mod_version'];jar=R/'build/libs'/f'slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  assert not any('Offline' in n or n.startswith('tools/') for n in z.namelist())
  for p in OWNED:assert 'org/slavicmyths/'+p.replace('.java','.class') in z.namelist(),p
  for p in LANG:assert z.read(p.removeprefix('src/main/resources/'))==(R/p).read_bytes()
 receipt={'baseline':BASE,'branch':'hell','version':version,'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'production_sources':{J+p:hashlib.sha256((R/J/p).read_bytes()).hexdigest() for p in OWNED},'minecraft_launches':0,'runtime':'NOT_RUN','polymc':'POLYMC_NOT_INSTALLED','issue':'OPEN / IN_PROGRESS / OWNER_PENDING'}
 (R/'docs/implementation/rituals273-20261025/receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
 print('RITUALS273_SOURCE_JAR_PASS',receipt['sha256'],'GAMEPLAY_NOT_RUN')
