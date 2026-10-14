"""Resource/JAR gates and explicitly modelled (not measured) XP pacing."""
from pathlib import Path
import ast,csv,hashlib,json,re,runpy,zipfile
from PIL import Image

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';OUT=ROOT/'docs/implementation/classes3';OUT.mkdir(parents=True,exist_ok=True)
checks=0
def check(condition,message):
 global checks
 checks+=1
 if not condition:raise AssertionError(message)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
assets=RES/'assets/slavicmyths'
before={p.relative_to(assets).as_posix():digest(p) for p in assets.rglob('*') if p.is_file()}
data=runpy.run_path(str(ROOT/'tools/classes3_assets.py'))
after={p.relative_to(assets).as_posix():digest(p) for p in assets.rglob('*') if p.is_file()}
check(before==after,'Classes 3.0 generator did not reproduce current assets')
skills=[row[1] for row in data['names']]+[row[0] for row in data['passives']]
classes=[row[0] for row in data['names']]
hashes=[]
for name in skills+classes:
 p=assets/'textures/gui/classes'/f'{name}.png'
 with Image.open(p) as im:check(im.size==(32,32) and im.mode=='RGBA' and im.getbbox() is not None and im.getextrema()[-1][0]==0,'Invalid icon '+name)
 hashes.append(digest(p))
check(len(set(hashes))==30,'Identical new icons')
for language in ['ru_ru','en_us']:
 lang=json.loads((assets/'lang'/f'{language}.json').read_text(encoding='utf-8'))
 for skill in skills:
  for suffix in ['','.description']:check(bool(lang.get('classskill.slavicmyths.'+skill+suffix)),'Missing skill localization '+language+':'+skill+suffix)
 for cls in classes:check(bool(lang.get('class.slavicmyths.'+cls)),'Missing class localization '+cls)
 for branch,*_ in data['trials']:check(bool(lang.get('classtrial.slavicmyths.'+branch)),'Missing trial localization '+branch)
 for key in data['ui']:check(bool(lang.get('classes.slavicmyths.'+key)),'Missing UI key '+key)
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));checks+=1
for p in RES.rglob('*.png'):
 with Image.open(p) as im:im.verify();checks+=1
for p in (ROOT/'src/main/java/org/slavicmyths/rpg/classes').glob('*.java'):check('net.minecraft.client.' not in p.read_text(encoding='utf-8'),'Client import in common '+p.name)
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M).group(1)
jar=ROOT/'build/libs'/f'slavicmyths-{version}.jar'
with zipfile.ZipFile(jar) as z:
 names=set(z.namelist());check('META-INF/neoforge.mods.toml' in names,'Missing NeoForge metadata');check('Implementation-Version: '+version in z.read('META-INF/MANIFEST.MF').decode(),'Manifest mismatch')
 check(not any('/verify/' in n or '/smoke/' in n or 'GameTests' in n or 'slavicmyths_classes/' in n for n in names),'Test content in production JAR')
 for p in RES.rglob('*'):
  if not p.is_file():continue
  name=p.relative_to(RES).as_posix();check(name in names,'Unpackaged resource '+name)
  if name!='META-INF/neoforge.mods.toml':check(z.read(name)==p.read_bytes(),'Stale packaged resource '+name)
 for p in (ROOT/'build/classes/java/main').rglob('*.class'):
  name=p.relative_to(ROOT/'build/classes/java/main').as_posix();check(name in names and z.read(name)==p.read_bytes(),'Stale production class '+name)
thresholds={level:sum(100+50*(n-1) for n in range(1,level)) for level in [10,25,40,50]}
story_budget=sum(1 for p in (RES/'data/slavicmyths/advancement').glob('*.json') if p.stem not in ['own_path','root'] and not p.stem.startswith('group_'))
structure_budget=len(list((RES/'data/slavicmyths/worldgen/structure').glob('*.json')))
rows=[]
for base in ['druzhinnik','vedun','razboinik']:
 for profile,kills,roles,stories,structures in [('combat',90,30,3,2),('exploration',0,0,6,4)]:
  hourly=kills*12+roles*4+stories*200+structures*180
  # Finite first-time story content, plus first-time structure sites (ledger bounded at 2048). No invented measurements.
  for level,xp in thresholds.items():
   minutes=0;earned=0.0
   while minutes<60000 and earned<xp:
    minutes+=1;hour=minutes/60
    earned=(kills*12+roles*4)*hour+min(story_budget,stories*hour)*200+min(1800,structures*hour)*180
   rows.append(dict(base=base,profile=profile,target_level=level,xp_required=xp,assumed_kills_per_hour=kills,assumed_role_actions_per_hour=roles,assumed_story_events_per_hour=stories,assumed_new_structure_sites_per_hour=structures,model_hours=round(minutes/60,2) if earned>=xp else 'UNREACHABLE_WITH_THIS_FINITE_PROFILE',measured=False))
with (OUT/'xp-pacing-model.csv').open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,fieldnames=rows[0].keys());w.writeheader();w.writerows(rows)
with (OUT/'abilities.csv').open('w',newline='',encoding='utf-8') as f:
 w=csv.writer(f);w.writerow(['class','skill','cooldown_seconds','duration_seconds','range','power','balance_status']);source=(ROOT/'src/main/java/org/slavicmyths/rpg/classes/ClassUltimates.java').read_text()
 for match in re.finditer(r'add\(m,"([^"]+)","([^"]+)","([^"]+)","([^"]+)",(\d+),(\d+),([\d.]+),([\d.]+)\)',source):parent,cls,skill,base,cd,duration,radius,power=match.groups();w.writerow([cls,skill,cd,duration,radius,power,'INITIAL_SPEC_NOT_GAMEPLAY_CALIBRATED'])
report=dict(checks=checks,version=version,jar=str(jar),sha256=digest(jar),size=jar.stat().st_size,new_icons=30,new_passives=6,ultimates=12,trials=12,xp_thresholds=thresholds,finite_mod_story_budget=story_budget,finite_structure_type_budget=structure_budget,client_launches=0,gameplay_pacing_measured=False,mspt_measured=False)
(OUT/'RESOURCE_JAR_CHECKS.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))
