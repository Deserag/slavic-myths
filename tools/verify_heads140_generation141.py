"""Verify production bytes and canonical resources without starting Minecraft."""
from pathlib import Path
import hashlib,json,tempfile,zipfile
from PIL import Image
from trophy140_resources import ROOT,HEADS,apply

OUT=ROOT/'docs/implementation/heads140-generation141'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 checks=[]
 def check(ok,name):
  checks.append({'requirement':name,'status':'PASS' if ok else 'FAIL'});assert ok,name
 with tempfile.TemporaryDirectory() as td:
  dest=Path(td);apply(dest)
  for p in dest.rglob('*'):
   if not p.is_file():continue
   rel=p.relative_to(dest);actual=ROOT/'src/main/resources'/rel
   if rel.as_posix().endswith(('lang/en_us.json','lang/ru_ru.json')):
    a=json.loads(actual.read_text(encoding='utf8'));b=json.loads(p.read_text(encoding='utf8'));check(all(a[k]==v for k,v in b.items()),'canonical head translations '+str(rel))
   elif p.name=='creative_catalog.tsv':
    check(all(line in actual.read_text(encoding='utf8').splitlines() for line in p.read_text(encoding='utf8').splitlines() if line),'canonical catalog entries')
   else:check(actual.read_bytes()==p.read_bytes(),'canonical head resource '+str(rel))
 baseline=json.loads((OUT/'baseline.json').read_text())
 changed=[p for p,h in baseline.items() if not (ROOT/p).is_file() or sha(ROOT/p)!=h]
 allowed={'gradle.properties','docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md','src/main/java/org/slavicmyths/SlavicMyths.java','src/main/java/org/slavicmyths/client/WorldBossModel.java','src/main/java/org/slavicmyths/client/CursedVolkolakModel.java','src/main/java/org/slavicmyths/commands/CommandTree.java','tools/command_reference.py','tools/command-tests/org/slavicmyths/verify/CommandCatalogOffline.java','tools/create_resources.py','src/main/resources/assets/slavicmyths/creative_catalog.tsv','src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/lang/en_us.json'}
 check(set(changed)<=allowed,'all unrelated existing source/resources/tools preserved')
 (OUT/'changed-existing.json').write_text(json.dumps(changed,indent=2)+'\n')
 jar=ROOT/'build/libs/slavicmyths-1.9.7.51.jar';resources=jsons=pngs=0
 with zipfile.ZipFile(jar) as z:
  for p in (ROOT/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   rel=p.relative_to(ROOT/'src/main/resources').as_posix();expected=ROOT/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else p
   check(z.read(rel)==expected.read_bytes(),'production resource parity '+rel);resources+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'));jsons+=1
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    pngs+=1
  for name in ['TrophyHeads','TrophyHeadBlock','TrophyHeadDrops']:check('org/slavicmyths/trophy/'+name+'.class' in z.namelist(),'production class '+name)
  check('org/slavicmyths/client/TrophyHeadsClient.class' in z.namelist(),'native head renderer packaged')
  check(not any('Offline' in n or n.startswith(('tools/','org/slavicmyths/verify/')) for n in z.namelist()),'offline fixtures excluded')
 for name in HEADS:
  with Image.open(ROOT/f'src/main/resources/assets/slavicmyths/textures/item/{name}.png') as im:check(im.size==(16,16) and im.mode=='RGBA','16px transparent GUI icon '+name)
 result={'status':'OWNER_PENDING','resources':resources,'json':jsons,'png':pngs,'checks':checks,'runtime_launches':0,'base_commit':'32d1733457e7e90360ce16c23860304ece664c64','source_state':'uncommitted working tree; see source-manifest.json','jar':{'path':str(jar),'sha256':sha(jar),'bytes':jar.stat().st_size}}
 (OUT/'verification.json').write_text(json.dumps(result,indent=2)+'\n')
 manifest={p.relative_to(ROOT).as_posix():sha(p) for folder in ['src','tools'] for p in (ROOT/folder).rglob('*') if p.is_file() and '__pycache__' not in p.parts}
 manifest['gradle.properties']=sha(ROOT/'gradle.properties');(OUT/'source-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
 print(json.dumps({k:v for k,v in result.items() if k!='checks'},indent=2))
if __name__=='__main__':main()
