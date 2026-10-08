"""Offline resources, production contents, additive loot and preservation audit."""
from pathlib import Path
import json,zipfile,re,hashlib
from PIL import Image
import rare_rune_resources_1343 as overlay
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';checks=0
def check(ok,why):
 global checks
 checks+=1
 if not ok:raise AssertionError(why)
def main():
 global checks
 log=(ROOT/'.tools/rune-1343/build.log').read_text(errors='replace');check('BUILD SUCCESSFUL' in log,'clean build')
 final=(ROOT/'.tools/rune-1343/final-build.log').read_text(errors='replace');check('BUILD SUCCESSFUL' in final and 'RARE_RUNE_OFFLINE_CHECKS=20491' in final,'latest clean production build and rare checks')
 counts={key:int(re.search(r'^'+key+r'=(\d+)',log,re.MULTILINE).group(1)) for key in ['RARE_RUNE_OFFLINE_CHECKS','RUNE_REWORK_OFFLINE_CHECKS','RUNE_OFFLINE_CHECKS','ARMOR_OFFLINE_CHECKS']}
 ids=['rune_'+id for id in overlay.RUNES]+['rune_'+id+'_fragment' for id in ['flight','rare_protection','death','berserker']]
 jar=ROOT/'build/libs/slavicmyths-1.3.4.3.jar';changed=[];preserved=0
 langs={f'assets/slavicmyths/lang/{l}.json' for l in ['ru_ru','en_us']};loot={f'data/slavicmyths/loot_table/entities/{e}.json' for e in overlay.SOURCES};tags={f'data/minecraft/tags/damage_type/{t}.json' for t in ['bypasses_armor','bypasses_shield','bypasses_cooldown']}
 with zipfile.ZipFile(ROOT/'.tools/rune-1343/baseline.jar') as old,zipfile.ZipFile(jar) as new:
  before=set(old.namelist());after=set(new.namelist());check('1.3.4.3' in new.read('META-INF/neoforge.mods.toml').decode(),'version')
  for n in before:
   check(n in after,'removed entry '+n)
   if n.startswith(('assets/','data/')):
    if n in langs:
     a=json.loads(old.read(n));b=json.loads(new.read(n));check(all(b.get(k)==v for k,v in a.items()),'old translations '+n)
    elif n in loot:check(json.loads(new.read(n))['pools'][:-3]==json.loads(old.read(n))['pools'],'old source loot '+n)
    elif n in tags:check(all(v in json.loads(new.read(n))['values'] for v in json.loads(old.read(n))['values']),'old damage tags '+n)
    else:check(old.read(n)==new.read(n),'unrelated resources '+n);preserved+=1
   elif n.endswith('.class') and old.read(n)!=new.read(n):changed.append(n)
  allowed=('org/slavicmyths/SlavicMyths','org/slavicmyths/rpg/Runes','org/slavicmyths/rpg/RuneDefinition','org/slavicmyths/rpg/RuneRework','org/slavicmyths/rpg/RpgMenu','org/slavicmyths/rpg/classes/ClassHudConfig','org/slavicmyths/client/RpgClient','org/slavicmyths/client/ClientSetup','org/slavicmyths/client/RuneAnvilScreen','org/slavicmyths/client/YagaScreen','org/slavicmyths/yaga/YagaMenu','org/slavicmyths/yaga/YagaServices','org/slavicmyths/combat/MythDamageSources')
  check(all(n.startswith(allowed) for n in changed),'unexpected code change '+str(changed))
  check(not any('/verify/' in n or '/smoke/' in n for n in after),'fixtures excluded')
  for n in after:
   if n.endswith('/'):continue
   if n.endswith('.json'):json.loads(new.read(n));checks+=1
   if n.startswith(('assets/','data/')):check(new.read(n)==(RES/n).read_bytes(),'stale resource '+n)
   if n.startswith(('org/slavicmyths/rpg/RareRune','org/slavicmyths/rpg/RuneVisual')) and n.endswith('.class'):check(b'net/minecraft/client' not in new.read(n) and b'mezz/jei' not in new.read(n),'client dependency '+n)
 generated=[RES/f'assets/slavicmyths/lang/{l}.json' for l in ['ru_ru','en_us']]+[RES/n for n in loot|tags]
 for id in ids:
  model=RES/f'assets/slavicmyths/models/item/{id}.json';texture=RES/f'assets/slavicmyths/textures/item/{id}.png';generated.extend([model,texture]);im=Image.open(texture)
  check(im.size==(32,32) and im.mode=='RGBA' and im.getchannel('A').getextrema()==(0,255),'sprite '+id);check(json.loads(model.read_text())['textures']['layer0']=='slavicmyths:item/'+id,'model '+id)
 check(len({(RES/f'assets/slavicmyths/textures/item/{id}.png').read_bytes() for id in ids})==10,'distinct full stones and fragments')
 for entity,(id,count) in overlay.SOURCES.items():
  pools=json.loads((RES/f'data/slavicmyths/loot_table/entities/{entity}.json').read_text())['pools'][-3:]
  check(pools[0]['entries'][0]['name']=='slavicmyths:rune_'+id+'_fragment' and pools[0]['entries'][0]['functions'][0]['count']==count and 'conditions' not in pools[0],'guaranteed source '+entity)
  check(pools[1]['conditions'][0]['chance']==.25 and pools[2]['conditions'][0]['chance']==.04,'extra/full chances '+entity)
 recipes=list((RES/'data/slavicmyths/recipe').glob('rare_rune_restore_*.json'));check(len(recipes)==4,'four restorations');generated.extend(recipes)
 rareOutputs={'slavicmyths:rune_'+id for id in overlay.RUNES}
 for p in (RES/'data/slavicmyths/recipe').glob('*.json'):
  value=json.loads(p.read_text());result=value.get('result',{});out=result.get('id',result.get('item')) if isinstance(result,dict) else result
  if out in rareOutputs:
   check(p in recipes,'no ordinary rare crafting '+p.name);check(value['type']=='slavicmyths:rune_crafting' and value['inputs']==[{'ingredient':{'item':out+'_fragment'},'count':3}] and value['result']['count']==1 and not value['chisel'],'matching fragments only '+p.name)
 for lang in ['ru_ru','en_us']:
  strings=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
  for id in ids:check('item.slavicmyths.'+id in strings,'localized '+id)
  for key in overlay.TEXT:check('rare_runes.slavicmyths.'+key in strings,'localized effect '+key)
 exchanges=(ROOT/'src/main/java/org/slavicmyths/yaga/YagaServices.java').read_text();check('new Entry("rare_sacrifice",3,"rune_sacrifice",1,n("soul",2),n("soul_fragment",8),n("perunite_dust",4),n("kost_likha",1))' in exchanges,'sacrifice actual exchange');check('new Entry("rare_floating_weapon",3,"rune_floating_weapon",1,n("empowered_soul",1),n("diamond_dust",4),n("tugarinova_kozha",1),n("ancient_sign",2))' in exchanges,'floating actual exchange')
 # Pure rectangle audit for the expanded 27-entry list, not an in-game GUI check.
 check(42+((27+4)//5)*24+8<=214 and 8+124<=202,'27 recipes fit original screen')
 generated.extend([RES/f'data/slavicmyths/damage_type/rune_{kind}.json' for kind in ['echo','execute']]+[RES/'data/slavicmyths/tags/entity_type/rune_death_immune.json',RES/'assets/slavicmyths/textures/entity/rune_shield.png',RES/'assets/slavicmyths/textures/entity/rune_shield_cracked.png'])
 hashes={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in generated};overlay.main()
 for p,d in hashes.items():check(hashlib.sha256(p.read_bytes()).hexdigest()==d,'reproducible '+p.name)
 report={'version':'1.3.4.3','resource_checks':checks,'preserved_old_resources':preserved,'changed_old_classes':changed,'offline_checks':counts,'real_client_runs':0,'real_server_runs':0,'runtime_verified':False,'jar_bytes':jar.stat().st_size,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest()}
 p=ROOT/'docs/verification/rare-runes-1.3.4.3.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
if __name__=='__main__':main()
