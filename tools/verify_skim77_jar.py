"""#77 production NeoForge artifact integrity; never boots a client or server."""
from pathlib import Path
import json,hashlib,tomllib,zipfile,subprocess
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/skim-77'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text(encoding='utf-8-sig').splitlines() if '=' in l)
jar=R/f"build/libs/slavicmyths-{props['mod_version']}.jar";resources=classes=pngs=0
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 assert len(z.namelist())==len(set(z.namelist()))
 meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']==props['mod_version'] for m in meta['mods'])
 assert props['minecraft_version']=='1.21.1' and props['neo_version']=='21.1.255'
 assert not any('/verify/' in n or n.startswith(('tools/','smoke/')) or 'Skim77Native' in n or 'SkimChecks' in n for n in z.namelist())
 for p in (R/'src/main/resources').rglob('*'):
  if not p.is_file():continue
  key=p.relative_to(R/'src/main/resources').as_posix()
  if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
  if p.suffix=='.png':
   with Image.open(p) as im:im.verify()
   pngs+=1
  if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
  resources+=1
 for p in (R/'build/classes/java/main').rglob('*.class'):
  key=p.relative_to(R/'build/classes/java/main').as_posix();assert z.read(key)==p.read_bytes(),key
  assert int.from_bytes(p.read_bytes()[6:8],'big')==65,key
  classes+=1
inputs={p.relative_to(R).as_posix():sha(p) for p in (R/'src/main').rglob('*') if p.is_file()}
for name in ['build.gradle','settings.gradle','gradle.properties']:inputs[name]=sha(R/name)
(O/'build-source-sha256.json').write_text(json.dumps(inputs,indent=2)+'\n')
report={'status':'PASS_OFFLINE','version':props['mod_version'],'jar':str(jar),'jar_sha256':sha(jar),'resources':resources,'png_verified':pngs,'production_java21_classes':classes,'baseline_head':json.loads((O/'before/source.json').read_text())['head'],'merged_main':subprocess.check_output(['git','rev-parse','MERGE_HEAD'],cwd=R,text=True).strip(),'consulted_main':json.loads((O/'before/source.json').read_text())['fetched_main'],'source_fingerprint':hashlib.sha256(json.dumps(inputs,sort_keys=True).encode()).hexdigest(),'source_commit':'UNCOMMITTED working tree, including preserved user modifications; no automatic commit per AGENTS.md','runtime_launches':0,'polymc':'NOT_UPDATED_BY_THIS_TASK','owner_acceptance':'PENDING'}
(O/'jar-check.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
