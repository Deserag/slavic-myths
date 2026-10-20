"""Production resource/class parity for #79, never a runtime claim."""
from pathlib import Path
import hashlib,json,zipfile,tomllib,struct
from PIL import Image
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/creatures-79'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 jar=R/'build/libs/slavicmyths-1.9.7.32.jar';resources=R/'build/resources/main';classes=R/'build/classes/java/main'
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.32' for m in meta['mods'])
  n=png=java=0
  for p in resources.rglob('*'):
   if not p.is_file():continue
   name=p.relative_to(resources).as_posix();assert z.read(name)==p.read_bytes(),name;n+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    png+=1
  for p in classes.rglob('*.class'):
   b=p.read_bytes();assert struct.unpack('>H',b[6:8])[0]==65,p
   assert z.read(p.relative_to(classes).as_posix())==b;java+=1
  assert not any(any(k in name for k in ['Creature79Checks','Bogatyr78Checks','Skim77Native','SkimModelBefore78','/smoke/']) for name in z.namelist())
  audio_files=[name for name in z.namelist() if name.endswith('.ogg')]
  baseline=json.loads((O/'baseline-source-sha256.json').read_text(encoding='utf-8'))
  for name,digest in baseline.items():
   if '/sounds/' in name or name.endswith(('.png','.ogg')):
    assert sha(R/name)==digest, name
  config=json.loads(z.read('slavicmyths.physiology.mixins.json'))
  assert 'PoisonPhysiologyMixin' in config['mixins']
  assert any(m['config']=='slavicmyths.physiology.mixins.json' for m in meta['mixins'])
  assert len([n for n in z.namelist() if n.startswith('data/slavicmyths/tags/entity_type/nature_')])==8
 manifest={p.relative_to(R).as_posix():sha(p) for root in [R/'src/main/java',R/'src/main/resources'] for p in root.rglob('*') if p.is_file()}
 for name in ['build.gradle','settings.gradle','gradle.properties']:manifest[name]=sha(R/name)
 (O/'build-source-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
 result=dict(status='PASS_OFFLINE_ONLY',version='1.9.7.32',jar=str(jar),jar_sha256=sha(jar),resources=n,png_verified=png,java21_classes=java,audio_files_in_jar=len(audio_files),source_fingerprint=hashlib.sha256(json.dumps(manifest,sort_keys=True).encode()).hexdigest(),source_commit='UNCOMMITTED #79 working tree on 7e6d64d4; no automatic commit',runtime_launches=0,polymc='1.9.7.30 preserved',issue79='IN_PROGRESS / OWNER_PENDING')
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result))
if __name__=='__main__':main()
