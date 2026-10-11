"""Production gates for the wolf/flight hotfix; no client launch."""
from pathlib import Path
import hashlib,json,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources';OUT=ROOT/'docs/verification/wolf-flight-hotfix'
version=next(line.split('=',1)[1] for line in (ROOT/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
checks=0
def check(ok,label):
 global checks
 checks+=1
 if not ok:raise AssertionError(label)
jar=ROOT/'build/libs'/f'slavicmyths-{version}.jar'
with zipfile.ZipFile(jar) as z:
 check(f'version="{version}"' in z.read('META-INF/neoforge.mods.toml').decode(),'version')
 for name in z.namelist():
  check(not ('/verify/' in name or name.startswith('org/slavicmyths/smoke/') or name.startswith('data/slavicmyths_wolf/')),'fixture leaked '+name)
  if name.endswith('.class') and name.startswith('org/slavicmyths/'):
   check(int.from_bytes(z.read(name)[6:8],'big')==65,'Java 21 '+name)
 for p in RES.rglob('*'):
  if not p.is_file() or p.suffix not in {'.json','.png','.ogg'}:continue
  if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
  if p.suffix=='.png':
   with Image.open(p) as im:im.verify()
  name=p.relative_to(RES).as_posix()
  check(z.read(name)==p.read_bytes(),'source/JAR '+name)
for lang in ['ru_ru','en_us']:
 text=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))['flight.slavicmyths.controls']
 check(text.count('%s')==5,'control placeholders '+lang)
 check(text in (ROOT/'tools/finalize_065.py').read_text(encoding='utf-8'),'generator controls '+lang)
report={'version':version,'checks':checks,'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'resources':'PASS','production':'PASS','client_launches':0}
OUT.mkdir(parents=True,exist_ok=True);(OUT/'resource-gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
