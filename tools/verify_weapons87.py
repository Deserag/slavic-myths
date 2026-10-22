"""Scoped production/resource/preservation gates; optional historical generator sandbox."""
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess,sys,tempfile,zipfile
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87';A=R/'src/main/resources/assets/slavicmyths'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 parser=argparse.ArgumentParser();parser.add_argument('--generators',action='store_true');parser.add_argument('--jar',action='store_true');args=parser.parse_args();checks=[]
 def check(value,label):checks.append({'requirement':label,'result':'PASS' if value else 'FAIL'});assert value,label
 manifest=json.loads((O/'blueprints/flails/applied-manifest.json').read_text());draft=json.loads((O/'blueprints/flails/draft-manifest.json').read_text());check(manifest==draft,'all69 static asset bytes match pre-production draft')
 animation=O/'blueprints/flail-motion/applied-manifest.json'
 if animation.exists():manifest.update(json.loads(animation.read_text()))
 spears=O/'blueprints/spears/applied-manifest.json'
 if spears.exists():
  spear_assets=json.loads(spears.read_text());check(spear_assets==json.loads((O/'blueprints/spears/draft-manifest.json').read_text()),'five spear production models exactly match prior15-model draft');manifest.update(spear_assets)
 artassets={}
 for name in ['silver-mace','storm-staff','tugarin-icon','poloz-whip','balance-b','likho-staff']:
  art=O/'blueprints'/name/'applied-manifest.json'
  if art.exists():
   added=json.loads(art.read_text());check(added==json.loads((art.parent/'draft-manifest.json').read_text()),name+' art exactly matches pre-production draft');artassets.update(added);manifest.update(added)
 for path,digest in manifest.items():
  p=A/path;check(p.exists() and sha(p)==digest,'owned asset '+path)
  if p.suffix=='.png':
   im=Image.open(p);check(im.size==(16,16) and im.mode=='RGBA','16px RGBA '+path)
  else:
   data=json.loads(p.read_text(encoding='utf-8'))
   def visit(v):
    if isinstance(v,dict):
     for k,x in v.items():
      if k in ['parent','model'] and isinstance(x,str) and x.startswith('slavicmyths:'):check((A/'models'/(x.split(':')[1]+'.json')).exists(),'resolved '+x)
      if k=='textures':
       for x in v[k].values():
        if x.startswith('slavicmyths:'):check((A/'textures'/(x.split(':')[1]+'.png')).exists(),'resolved texture '+x)
      visit(x)
    elif isinstance(v,list):
     for x in v:visit(x)
   visit(data)
   for e in data.get('elements',[]):
    check(all(-16<=lo<hi<=32 for lo,hi in zip(e['from'],e['to'])),'valid nonzero cube '+path)
    for face in e['faces'].values():check(all(0<=x<=16 for x in face['uv']),'UV within atlas '+path)
 if animation.exists():
  with zipfile.ZipFile(O/'before-flail-animation.zip') as prior:
   for id in ['stone_flail','gold_flail','flail','silver_flail','diamond_flail','perunite_flail','netherite_flail','ataman_flail']:
    root=json.loads((A/'models/item'/(id+'.json')).read_text());check(root.get('parent')=='builtin/entity' and root.get('gui_light')=='front','custom renderer and original flat GUI lighting '+id)
    for suffix in ['_held','_icon']:
     path='src/main/resources/assets/slavicmyths/models/item/'+id+suffix+'.json';check((R/path).read_bytes()==prior.read(path),'static drop geometry / GUI source unchanged '+id+suffix)
   for name in ['FlailItem','RareCombat']:
    path='src/main/java/org/slavicmyths/combat/'+name+'.java';check((R/path).read_bytes()==prior.read(path),'no server mechanics changes for animation '+name)
 if (O/'before-thrown-orientation.zip').exists():
  with zipfile.ZipFile(O/'before-thrown-orientation.zip') as prior:
   for name in prior.namelist():
    if name.endswith('/client/WeaponProjectileRenderer.java'):continue
    check((R/name).read_bytes()==prior.read(name),'thrown art/physics unchanged '+name)
 baseline=json.loads((O/'baseline.json').read_text());allow={('src/main/resources/assets/slavicmyths/'+p) for p in manifest}|{'tools/weapon_patch_assets.py','tools/weapon_feedback_assets.py','tools/weapon_visual_repair_assets.py','tools/poloz_full_assets.py','src/main/java/org/slavicmyths/client/WeaponProjectileRenderer.java','src/main/java/org/slavicmyths/registry/ModItems.java','src/main/java/org/slavicmyths/item/StormStaffItem.java'}
 gripfiles=list((O/'blueprints/grips').glob('*.json'))
 if gripfiles:
  import zipfile as zipmodule
  archived=zipmodule.ZipFile(O/'before-grip-source.zip')
  for f in gripfiles:
   plan=json.loads(f.read_text(encoding='utf-8'));path=plan['source_model'];allow.add(path);keys=[n for n in archived.namelist() if n==path or n.endswith('/'+path)];check(len(keys)==1,'unique archived before source '+path);old=json.loads(archived.read(keys[0]));new=json.loads((R/path).read_text(encoding='utf-8'));artpath=path.removeprefix('src/main/resources/assets/slavicmyths/')
   if artpath in artassets:
    check({k:v for k,v in new['display'].items() if k!='gui'}=={k:v for k,v in plan['display'].items() if k!='gui'},'all hand/ground/fixed transforms preserved across later approved art '+path)
    check(new.get('gui_light')=='front','new flat art GUI lighting')
   else:
    check({k:v for k,v in old.items() if k!='display'}=={k:v for k,v in new.items() if k!='display'},'geometry UV textures preserved '+path);check(new['display']==plan['display'],'actual display matches prior technical blueprint '+path)
  allow|={'src/main/java/org/slavicmyths/client/WeaponHands.java','src/main/java/org/slavicmyths/client/NightClient.java','src/main/java/org/slavicmyths/client/TugarinPairClient.java'}
 changed=[p for p,h in baseline.items() if not (R/p).exists() or sha(R/p)!=h];unexpected=[p for p in changed if p not in allow];check(not unexpected,'preserve all pre-existing files outside exact weapon whitelist')
 before={x['id']:x for x in json.loads((O/'native-attributes.json').read_text())};after={x['id']:x for x in json.loads((O/'native-attributes-after.json').read_text())};expected={'nightingale_dagger':700,'ancient_carolingian_sword':90,'restored_carolingian_sword':450,'ancient_spear':90,'restored_spear':380,'ancient_chekan':90,'chekan':400,'pool_spear':420}
 check(len(before)==len(after)==68,'68 native constructor/event snapshots')
 for id,old in before.items():
  new=after[id];check(new['max_damage']==expected.get(id,old['max_damage']),'approved durability '+id)
  normalized=json.loads(json.dumps(new))
  if id=='perunite_mace':
   check(abs(new['attack_speed_total']-.8)<1e-6 and new['attack_damage_total']==9.5 and abs(new['unarmored_full_charge_melee_dps_ceiling']-7.6)<1e-5,'explicitly approvedB mace speed only')
   for field in ['attack_speed_total','unarmored_full_charge_melee_dps_ceiling']:normalized[field]=old[field]
   for index,modifier in enumerate(normalized['modifiers']):
    if modifier['attribute']=='minecraft:generic.attack_speed':
     check(abs(modifier['amount']+3.2)<1e-6,'approvedB native speed modifier');modifier['amount']=old['modifiers'][index]['amount']
  check({k:v for k,v in old.items() if k!='max_damage'}=={k:v for k,v in normalized.items() if k!='max_damage'},'all remaining native weapon fields unchanged '+id)
 core=[]
 for m in ['stone','gold','iron','silver','diamond','perunite','netherite']:
  d=json.loads((A/'models/item'/(m+'_flail_ball_held.json')).read_text());core.append(hashlib.sha256(json.dumps(d['elements'],sort_keys=True).encode()).hexdigest())
 check(len(set(core))==7,'seven different ball/eyelet geometry signatures')
 report={'checks':checks,'baseline_changed':changed,'unexpected':unexpected,'game_launches':0,'runtime':'NOT_RUN / OWNER_PENDING','polymc':'NOT_MODIFIED'}
 if args.generators:
  work=R/'work';work.mkdir(exist_ok=True);sandbox=Path(tempfile.mkdtemp(prefix='weapons87-generators-',dir=work));check(sandbox.resolve().is_relative_to(work.resolve()),'sandbox inside workspace work directory');shutil.copytree(R/'src/main/resources',sandbox/'src/main/resources');(sandbox/'tools').mkdir()
  scripts=['weapon_patch_assets.py','weapon_feedback_assets.py','weapon_visual_repair_assets.py','poloz_full_assets.py','weapons87_flails.py','weapons87_grips.py','weapons87_flail_motion.py','weapons87_spears.py','weapons87_silver_mace.py','weapons87_storm_staff.py','weapons87_tugarin_icon.py','weapons87_whip.py','weapons87_balance_b.py','weapons87_likho_gui.py']
  for name in scripts:shutil.copy2(R/'tools'/name,sandbox/'tools'/name)
  shutil.copytree(R/'tools/weapon-art-original',sandbox/'tools/weapon-art-original');src=sandbox/'src/main/resources';initial={p.relative_to(src).as_posix():sha(p) for p in src.rglob('*') if p.is_file()};runs=[]
  for name in ['weapons87_flails.py','weapon_visual_repair_assets.py','weapon_feedback_assets.py','weapon_patch_assets.py','poloz_full_assets.py','weapons87_flails.py']:
   flags=['--apply'] if name=='weapons87_flails.py' else ['--weapon-only'] if name=='poloz_full_assets.py' else []
   command=[sys.executable,str(sandbox/'tools'/name)]+flags;result=subprocess.run(command,cwd=sandbox,capture_output=True,text=True,timeout=120);check(result.returncode==0,'sandbox generator '+name+' '+str(flags))
   generated={p:sha(sandbox/'src/main/resources/assets/slavicmyths'/p) for p in manifest};check(generated==manifest,'canonical asset reproduction after '+name);runs.append({'generator':name,'exit':result.returncode,'owned_match':generated==manifest,'stdout':result.stdout[-1000:],'stderr':result.stderr[-1000:]})
  outside=[p for p,h in initial.items() if sha(src/p)!=h and ('src/main/resources/'+p) not in allow];report['generators']={'sandbox':str(sandbox),'runs':runs,'nonowned_changes':outside};check(not outside,'historical generators do not roll back other production resources');(O/'generator-checks.json').write_text(json.dumps(report['generators'],indent=2)+'\n')
 elif (O/'generator-checks.json').exists():report['generators']=json.loads((O/'generator-checks.json').read_text())
 if args.jar:
  jars=list((R/'build/libs').glob('*.jar'));check(len(jars)==1,'one production JAR');jar=jars[0]
  with zipfile.ZipFile(jar) as z:
   for p,h in manifest.items():check(hashlib.sha256(z.read('assets/slavicmyths/'+p)).hexdigest()==h,'packaged actual asset '+p)
   for plan_file in gripfiles:
    path=json.loads(plan_file.read_text(encoding='utf-8'))['source_model'];key=path.removeprefix('src/main/resources/');check(hashlib.sha256(z.read(key)).hexdigest()==sha(R/path),'packaged actual grip JSON '+key)
   for name in ['combat/WeaponDefaults','client/ScytheGrip','client/WeaponHands','client/NightClient','client/TugarinPairClient','client/FlailClient','client/FlailMotion','client/WeaponProjectileRenderer','client/BowDrawClient','item/StormStaffItem']:
    key='org/slavicmyths/'+name+'.class';check(key in z.namelist(),'required production class '+key);check(hashlib.sha256(z.read(key)).hexdigest()==sha(R/'build/classes/java/main'/key),'JAR matches compiled class '+key)
   fixtures=R/'build/classes/java/weapons87Offline';check(not any(p.relative_to(fixtures).as_posix() in z.namelist() for p in fixtures.rglob('*.class')),'all offline classes excluded including client-package FP exporter');check(not any(n.startswith(('org/slavicmyths/verify/','tools/')) for n in z.namelist()),'offline classes excluded from production')
  report['jar']={'path':str(jar.resolve()),'sha256':sha(jar),'bytes':jar.stat().st_size}
 (O/'package-checks.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print('WEAPONS87 GATES:',len(checks),'PASS; game launches0; PolyMC unchanged')
if __name__=='__main__':main()
