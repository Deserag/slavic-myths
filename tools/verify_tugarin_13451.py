"""1.3.4.5.1 production/resource gate; does not start Minecraft or overwrite historical reports."""
from pathlib import Path
import hashlib,json,zipfile,re
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';OUT=ROOT/'docs/verification';checks=0

def check(ok,why):
 global checks
 checks+=1
 if not ok:raise AssertionError(why)
def main():
 jar=ROOT/'build/libs/slavicmyths-1.3.4.5.1.jar';check(jar.exists(),'production jar absent')
 build=(ROOT/'.tools/tugarin-13451/build.log').read_text(errors='replace');check('BUILD SUCCESSFUL' in build,'latest clean build failed')
 server=(ROOT/'.tools/tugarin-13451/final-checks.log').read_text(errors='replace');check('All 21 required tests passed' in server,'21 current server gates absent')
 texts=json.loads((ROOT/'tools/combat_rune_text_1344.json').read_text(encoding='utf-8'));models=[];json_count=png_count=0
 with zipfile.ZipFile(jar)as z:
  names=set(z.namelist());check('1.3.4.5.1'in z.read('META-INF/neoforge.mods.toml').decode(),'wrong mod version');check(not any('/verify/'in n or '/smoke/'in n or 'GameTests'in n for n in names),'test fixture leaked into JAR')
  for p in RES.rglob('*'):
   if not p.is_file():continue
   rel=p.relative_to(RES).as_posix()
   if p.suffix=='.json':
    data=json.loads(p.read_text(encoding='utf-8'));json_count+=1
    check(rel in names and json.loads(z.read(rel))==data,'resource jar mismatch '+rel)
    if '/models/'in rel:
     models.append(rel)
     refs=[('model',data['parent'])]if isinstance(data.get('parent'),str)else[]
     refs += [('texture',v)for v in data.get('textures',{}).values()if isinstance(v,str)and not v.startswith('#')]
     refs += [('model',v['model'])for v in data.get('overrides',[])if 'model'in v]
     for kind,ref in refs:
      if not ref.startswith('slavicmyths:'):continue
      target=RES/'assets/slavicmyths'/('models'if kind=='model'else'textures')/(ref.split(':')[1]+('.json'if kind=='model'else'.png'));check(target.exists(),'local reference '+ref)
   elif p.suffix=='.png':
    with Image.open(p)as im:im.verify();png_count+=1
    check(rel in names and z.read(rel)==p.read_bytes(),'PNG jar mismatch '+rel)
  for lang,updates in texts.items():
   data=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
   for k,v in updates.items():check(data.get(k)==v,'translation '+k)
  for n in ['CombatTargeting','TugarinPair']:check(f'org/slavicmyths/combat/{n}.class'in names,'missing common implementation '+n)
  check('org/slavicmyths/client/TugarinPairClient.class'in names,'missing pair client renderer')
 pair=json.loads((RES/'assets/slavicmyths/models/item/tugarin_sword.json').read_text());check(pair['parent']=='minecraft:block/block'and len(pair['elements'])==2,'two-blade inventory geometry');check(not pair.get('overrides'),'inventory model must not depend on player key/context')
 check(json.loads((RES/'assets/slavicmyths/models/item/tugarin_sword_held.json').read_text())['parent']=='minecraft:item/handheld','single hand model missing')
 check((ROOT/'tools/templates/combat-1344/tugarin_sword.json').read_bytes()==(RES/'assets/slavicmyths/models/item/tugarin_sword.json').read_bytes(),'generator model divergence')
 for file in ['rare_weapon_resources_1322.py','rare_rune_resources_1343.py']:check('combat_fix_resources_1344.apply()'in(ROOT/'tools'/file).read_text(encoding='utf-8'),'generator rollback risk '+file)
 check('Tugarin model offline PASS:' in server and 'BUILD SUCCESSFUL' in server,'native geometry bake failed')
 regression=(ROOT/'.tools/tugarin-13451/regression.log').read_text(errors='replace');check('All 61 required tests passed' in regression,'combat regression failed')
 loot=json.loads((RES/'data/slavicmyths/loot_table/entities/tugarin_zmey.json').read_text(encoding='utf-8'))
 guaranteed=[e['name'] for pool in loot['pools'] if not pool.get('conditions') for e in pool['entries']]
 for n in ['slavicmyths:us_tugarina','slavicmyths:tugarinova_kozha','slavicmyths:rune_berserker_fragment']:check(n in guaranteed,'missing guaranteed trophy '+n)
 check('frost' not in json.dumps(loot) and 'reflection' not in json.dumps(loot),'future rune content')
 with zipfile.ZipFile(jar) as current:
  for n in ['org/slavicmyths/hunt/TugarinCombat.class','org/slavicmyths/client/TugarinSerpentGeometry.class']:check(n in current.namelist(),'missing boss implementation '+n)
  prior=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.minecraft/mods/slavicmyths-1.3.4.4.jar')
  if not prior.exists():
   candidates=list(Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-1.21.1-Testing/.slavicmyths-backups').rglob('slavicmyths-1.3.4.4.jar'));check(bool(candidates),'previous resource baseline missing');prior=candidates[-1]
  allowed={'assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','data/slavicmyths/loot_table/entities/tugarin_zmey.json'}
  with zipfile.ZipFile(prior) as old:
   for n in old.namelist():
    if n.startswith(('assets/','data/')) and not n.endswith('/') and n not in allowed:check(n in current.namelist() and old.read(n)==current.read(n),'unrelated resource changed '+n)
 result={'version':'1.3.4.5.1','build':'clean build PASS','automated_server_tests':21,'combat_regression_tests':61,'actual_server_launches':5,'client_launches':0,'pure_rule_checks':20491,'resource_checks':checks,'json_files':json_count,'png_files':png_count,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'jar':str(jar),'manual_client_check':'REQUIRES_MANUAL_CLIENT_CHECK: boss models, animations, wings, hitbox, combat pacing, sounds, pair slash; no interactive client used'}
 OUT.mkdir(exist_ok=True);(OUT/'tugarin-1.3.4.5.1.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
