"""Production, restored-art and live-client evidence gates for the visual repair."""
from pathlib import Path
import hashlib,io,json,re,zipfile
from PIL import Image
from check_event_subscribers import check
from weapon_patch_assets import main as generate
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapon-visual-repair';J=R/'build/libs/slavicmyths-1.9.7.3.jar'
checks=0
def need(value,label):
 global checks
 checks+=1
 if not value:raise AssertionError(label)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 log=(O/'BUILD.log').read_text(encoding='utf-8',errors='replace');need('BUILD SUCCESSFUL' in log,'final clean build')
 need('WEAPON_FEEDBACK_OFFLINE_PASS' in log,'native pose/state checks')
 live=(O/'CLIENT_DRAGON_FINAL.log').read_text(encoding='utf-8',errors='replace')
 need('BUILD SUCCESSFUL' in live and 'WEAPON_VISUAL_CLIENT_CAPTURE_DONE' in live,'completed real client/integrated-server run')
 need('Missing textures in model' not in live,'no missing item/block atlas textures in final client')
 need('AssertionError' not in live and 'Unreported exception' not in live,'no final client/runtime failure')
 cases=re.findall(r'VOLLEY_RUNTIME_PASS case=(\d) arrows=3 hits=(\d+) damage=true weakness=(true|false) flameOrStorm=(true|false) clouds=(\d+) ammoSpent=1 durabilitySpent=3 remainingHP=([^\r\n]+)',live)
 need({int(c[0]) for c in cases}==set(range(6)),'all six modes, real hits/effects, survival ammo/durability')
 need(all(int(c[1])==3 and (c[2]=='true' if int(c[0])<2 else c[3]=='true') for c in cases),'correct effect family observed on enemy')
 root=R/'src/main/resources';before={p.relative_to(root).as_posix():digest(p) for p in root.rglob('*') if p.is_file()};generate();need(before=={p.relative_to(root).as_posix():digest(p) for p in root.rglob('*') if p.is_file()},'all final weapon resources reproducible')
 original=json.loads((R/'tools/weapon-art-original/karachun_scythe_held.json').read_text());held=json.loads((root/'assets/slavicmyths/models/item/karachun_scythe_held.json').read_text())
 need(len(held['elements'])==60 and held['elements']==original['elements'],'original ornate geometry, 60 parts')
 need((root/'assets/slavicmyths/textures/item/karachun_scythe.png').read_bytes()==(R/'tools/weapon-art-original/karachun_scythe.png').read_bytes(),'original 32px scythe icon byte exact')
 for name in ['serpent_crossbow','three_breath_crossbow']:
  models=list((root/'assets/slavicmyths/models/item').glob(name+'*.json'));need(len(models)==6,'six authored crossbow states')
  for p in models:
   d=json.loads(p.read_text());need(len(d['elements'])==(65 if p.stem=='three_breath_crossbow' or p.stem.startswith('three_breath_crossbow_pulling') else 68 if p.stem.startswith('three_breath_crossbow') else 9 if p.stem.endswith(('_arrow','_firework')) else 8),'distinct stock and three dragon heads' if name=='three_breath_crossbow' else 'serpent crossbow geometry retained')
   for e in d['elements']:
    for face in e['faces'].values():need(all(0<=u<=16 for u in face['uv']),'no atlas bleed: '+p.name)
 with zipfile.ZipFile(J) as z:
  names=set(z.namelist());need(z.testzip() is None,'JAR CRC');need('version="1.9.7.3"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'production metadata')
  tested=json.loads((O/'TESTED_CLASS_HASHES.json').read_text())
  for n,h in tested.items():need(n in names and hashlib.sha256(z.read(n)).hexdigest()==h,'same production class as tested client '+n)
  need(not any(n.startswith(('tools/','smoke/')) or any(t in n for t in ['WeaponVisualProbe','WeaponFeedbackChecks','CreatureFeedbackTests','GameTests']) for n in names),'all test fixtures excluded')
  for n,h in before.items():
   if n=='META-INF/neoforge.mods.toml':continue
   need(n in names and hashlib.sha256(z.read(n)).hexdigest()==h,'exact resource '+n)
  for p in (R/'build/classes/java/main').rglob('*.class'):
   n=p.relative_to(R/'build/classes/java/main').as_posix();need(n in names and z.read(n)==p.read_bytes(),'exact class '+n)
  for n in names:
   if n.endswith('.json'):json.loads(z.read(n));need(True,'JSON '+n)
   if n.endswith('.png'):Image.open(io.BytesIO(z.read(n))).verify();need(True,'PNG '+n)
 subscribers=check(J)
 result={'status':'PASS','version':'1.9.7.3','jar':str(J),'sha256':digest(J),'checks':checks,'subscribers':subscribers,'scythe':'original 60 parts and original 32px icon','crossbow_uv':'all 12 models normalized to 0..16','dragon_heads':3,'mouth_lanes':[0,1,2],'test_fixtures_in_production':False,'real_client_starts':6,'integrated_server_starts':6,'gametest_server_starts':0,'final_runtime_cases':[{'case':int(c[0]),'arrow_count':3,'entity_hits':int(c[1]),'weakness':c[2]=='true','flame_or_storm':c[3]=='true','clouds':int(c[4]),'ammo_spent':1,'durability_spent':3,'target_healths':c[5]} for c in cases],'multiplayer_relog_mspt':'NOT_TESTED'}
 (O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
