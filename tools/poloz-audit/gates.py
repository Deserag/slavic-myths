from pathlib import Path
import json,zipfile,hashlib,subprocess,collections
from PIL import Image
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/poloz-pressure'; checks=collections.Counter()
for p in (R/'src/main/resources').rglob('*.json'):json.loads(p.read_text(encoding='utf-8-sig'));checks['json']+=1
for p in (R/'src/main/resources').rglob('*.png'):
 with Image.open(p) as im:im.verify()
 checks['png']+=1
jar=R/'build/libs/slavicmyths-1.9.7.5.jar'
with zipfile.ZipFile(jar) as z:
 assert not any(any(x in n.lower() for x in ['/audit/','/verify/','/smoke/','cubeaudit','polozaudit','referencecube','pressureobserver']) for n in z.namelist())
 for p in (R/'src/main/resources').rglob('*'):
  if p.is_file() and p.name!='neoforge.mods.toml':assert z.read(p.relative_to(R/'src/main/resources').as_posix())==p.read_bytes();checks['resource_parity']+=1
 for p in (R/'build/classes/java/main').rglob('*.class'):
  b=z.read(p.relative_to(R/'build/classes/java/main').as_posix());assert b==p.read_bytes();assert int.from_bytes(b[6:8],'big')==65;checks['class_parity_java21']+=1
 assert '1.9.7.5' in z.read('META-INF/neoforge.mods.toml').decode()
result=dict(status='PASS',checks=dict(checks),jar=jar.name,bytes=jar.stat().st_size,sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),fixture_in_production=False)
(O/'gates.json').write_text(json.dumps(result,indent=2));print(json.dumps(result))
