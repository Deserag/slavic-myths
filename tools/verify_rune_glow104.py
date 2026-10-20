"""Production resource/JAR/source-scope gates for #104. Never launches Minecraft."""
from pathlib import Path
import json,hashlib,zipfile,tomllib,struct,subprocess
from PIL import Image
import rune_glow104_texture as art
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/rune-glow-104'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 baseline=json.loads((O/'baseline-source-sha256.json').read_text());current={p.relative_to(R).as_posix() for root in ['src/main/java','src/main/resources'] for p in (R/root).rglob('*') if p.is_file()}|{'gradle.properties'}
 modified={'gradle.properties','src/main/java/org/slavicmyths/client/TextileClothingLayer.java','src/main/resources/META-INF/neoforge.mods.toml'}
 added={p.relative_to(R).as_posix() for p in (R/'src/main/java/org/slavicmyths/client/runeglow').rglob('*.java')}|{'src/main/resources/slavicmyths.rune_glow.mixins.json'}|{f'src/main/resources/assets/slavicmyths/shaders/core/rune_glint.{ext}' for ext in ['json','vsh','fsh']}|{'src/main/resources/assets/slavicmyths/textures/misc/rune_glint.png'}
 assert len(added)==12 and current-set(baseline)==added and not set(baseline)-current
 assert {n for n,h in baseline.items() if sha(R/n)!=h}==modified
 config=json.loads((R/'src/main/resources/slavicmyths.rune_glow.mixins.json').read_text());assert not config.get('mixins') and len(config['client'])==3
 assert Image.open(R/'src/main/resources/assets/slavicmyths/textures/misc/rune_glint.png').tobytes()==art.image().tobytes()
 gpu=json.loads((O/'shader-compile.json').read_text());assert gpu['status']=='PASS_STANDALONE_SHADER_COMPILE_LINK'
 for ext in ['vsh','fsh']:assert gpu[ext+'_sha256']==sha(R/f'src/main/resources/assets/slavicmyths/shaders/core/rune_glint.{ext}')
 log=(R/'.tools/rune104-build.log').read_text(encoding='utf-8-sig');assert 'BUILD SUCCESSFUL' in log and 'RUNE104_PLAIN_JVM_PASS' in log and 'RUNE104_NATIVE_MESH_PASS' in log
 jar=R/'build/libs/slavicmyths-1.9.7.42.jar';resources=R/'build/resources/main';classes=R/'build/classes/java/main';nr=np=nc=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']=='1.9.7.42' for m in meta['mods']);assert any(m['config']=='slavicmyths.rune_glow.mixins.json' for m in meta['mixins'])
  for p in resources.rglob('*'):
   if not p.is_file():continue
   assert z.read(p.relative_to(resources).as_posix())==p.read_bytes();nr+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    np+=1
  for p in classes.rglob('*.class'):
   data=p.read_bytes();assert struct.unpack('>H',data[6:8])[0]==65 and z.read(p.relative_to(classes).as_posix())==data;nc+=1
  assert not any(any(tag in n for tag in ['RuneGlow104Checks','RuneGlow104Meshes','/smoke/','GameTests.class']) for n in z.namelist())
  for name in config['client']:assert (config['package'].replace('.','/')+'/'+name+'.class') in z.namelist()
 manifest={p.relative_to(R).as_posix():sha(p) for root in ['src/main/java','src/main/resources'] for p in (R/root).rglob('*') if p.is_file()}
 for n in ['gradle.properties','build.gradle','settings.gradle']:manifest[n]=sha(R/n)
 (O/'build-source-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n')
 result=dict(status='PASS_OFFLINE_AND_STANDALONE_GL_ONLY',version='1.9.7.42',jar=str(jar),sha256=sha(jar),resources=nr,png_verified=np,java21_classes=nc,source_base_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),source_commit='UNCOMMITTED working tree; no automatic commit',source_fingerprint=hashlib.sha256(json.dumps(manifest,sort_keys=True).encode()).hexdigest(),modified=sorted(modified),added=sorted(added),minecraft_launches=0,mixin_application='NOT_RUN_IN_MINECRAFT; pinned bytecode target ABI checked',runtime='NOT_RUN',owner_acceptance='PENDING',polymc='1.9.7.40 unchanged;42 NOT_INSTALLED')
 (O/'jar-check.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
if __name__=='__main__':main()
