"""Focused artifact/preservation gates; runtime mechanics are tested separately by GameTest."""
from pathlib import Path
import hashlib,json,zipfile,re,subprocess
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/creature-feedback';O.mkdir(parents=True,exist_ok=True)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
checks=[]
def require(ok,name):
 if not ok:raise AssertionError(name)
 checks.append(name)
jar=R/'build/libs/slavicmyths-1.9.7.jar'
with zipfile.ZipFile(jar) as z:
 require(z.testzip() is None,'JAR ZIP CRC')
 names=z.namelist();require('META-INF/neoforge.mods.toml' in names,'NeoForge metadata')
 require('org/slavicmyths/combat/CreatureHealthBalance.class' in names,'previous HP migration helper included')
 require('org/slavicmyths/combat/CreatureSafety.class' in names,'new safety events included')
 require('org/slavicmyths/gorynych/GorynychPose.class' in names,'shared head kinematics included')
 require(not any('CreatureFeedbackTests' in n or 'CreatureHealthChecks' in n or n.startswith(('tools/','smoke/')) for n in names),'test fixtures excluded')
 count=0
 for name in names:
  if name.endswith('.json'):
   json.loads(z.read(name).decode('utf-8-sig'));count+=1
 require(count>1000,'all packaged JSON parses as UTF-8')
 for lang in ['ru_ru','en_us']:
  path=f'assets/slavicmyths/lang/{lang}.json';require(z.read(path)==(R/'src/main/resources'/path).read_bytes(),lang+' matches source byte for byte')
  data=json.loads(z.read(path));require(not any('\ufffd' in v for v in data.values() if isinstance(v,str)),lang+' no replacement characters')
  require(not any(re.search(r'(?:Р[џђќЃљњ]|С[‚ѓљќ])',v) for v in data.values() if isinstance(v,str)),lang+' no common CP1251 mojibake markers')
  (O/(lang+'-samples.json')).write_text(json.dumps({k:v for k,v in data.items() if k in ['entity.slavicmyths.great_poloz','entity.slavicmyths.zmey_gorynych','entity.slavicmyths.chud_guard','entity.slavicmyths.karachun','entity.slavicmyths.morozko','entity.slavicmyths.firebird']},ensure_ascii=False,indent=2),encoding='utf-8')
 # Keep every production resource in this shared checkout, including the parallel rune patch.
 files=list((R/'src/main/resources').rglob('*'));verified=0
 for f in files:
  if not f.is_file():continue
  rel=f.relative_to(R/'src/main/resources').as_posix()
  if rel=='META-INF/neoforge.mods.toml':continue # Gradle expands the mod version.
  require(rel in names and z.read(rel)==f.read_bytes(),'resource '+rel);verified+=1
 # Every compiled production class, not just tracked paths, must reach the JAR.
 for f in (R/'build/classes/java/main').rglob('*.class'):
  rel=f.relative_to(R/'build/classes/java/main').as_posix();require(rel in names and z.read(rel)==f.read_bytes(),'class '+rel)
 require(b'GorynychBalance1982' in z.read('org/slavicmyths/gorynych/GorynychRules.class'),'Gorynych migration marker retained')
 require(b'AdvancedRuneRules' in z.read('org/slavicmyths/rpg/AdvancedRunes.class'),'parallel rune implementation included')
owned=json.loads((R/'docs/implementation/runes-patch/OWNED_HASHES.json').read_text(encoding='utf-8-sig'))
preserved={p:digest(R/p)==h for p,h in owned.items() if not p.startswith('docs/') and p!='src/main/resources/assets/slavicmyths/lang/ru_ru.json'}
locale_path='src/main/resources/assets/slavicmyths/lang/ru_ru.json'
before=R/'.tools/creature-feedback-before'/locale_path
require(digest(before)==owned[locale_path],'Russian locale baseline matches parallel rune patch')
old=json.loads(before.read_text(encoding='utf-8'));new=json.loads((R/locale_path).read_text(encoding='utf-8'));fixes=json.loads((O/'LOCALIZATION_FIXES.json').read_text(encoding='utf-8'))
require(old.keys()==new.keys() and {k for k in old if old[k]!=new[k]}==set(fixes),'only documented 13 localization values changed')
require(all(old[k]==v['before'] and new[k]==v['after'] for k,v in fixes.items()),'localization changes exactly match reversible repairs')
require(all(preserved.values()),'parallel rune production/tests hashes unchanged')
result={'status':'PASS','sha256':digest(jar),'jar':str(jar),'gates':len(checks),'packaged_json':count,'resources':verified,'parallel_hashes':preserved,'client_starts':0}
(O/'ARTIFACT_GATES.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
(O/'GATE_NAMES.txt').write_text('\n'.join(checks)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k!='parallel_hashes'},ensure_ascii=False))
