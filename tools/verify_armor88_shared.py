"""Read-only shared47 JAR gate against frozen armor46 and the other agent build snapshot."""
from pathlib import Path
import json,hashlib,zipfile,tomllib,struct,shutil
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor88-full'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 prior=json.loads((O/'build-source-sha256.json').read_text());scope=json.loads((O/'integration-scope.json').read_text());assert all(sha(R/n)==prior[n] for n in scope['changed']+scope['added'])
 changed=[n for n,h in prior.items() if sha(R/n)!=h];assert set(changed)=={'gradle.properties','src/main/resources/assets/slavicmyths/shaders/core/rune_glint.fsh','src/main/java/org/slavicmyths/client/runeglow/RuneGlowRenderTypes.java','src/main/java/org/slavicmyths/client/runeglow/RuneGlowStyle.java'}
 other=R/'docs/implementation/rune-glow-visible-47';built=json.loads((other/'source-before.json').read_text());assert all(sha(R/n)==h for n,h in built.items());assert 'BUILD SUCCESSFUL' in (other/'build.log').read_text(encoding='utf-8-sig')
 jar=R/'build/libs/slavicmyths-1.9.7.47.jar';nr=nc=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None;meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['version']=='1.9.7.47' for m in meta['mods'])
  for root in [R/'build/resources/main',R/'build/classes/java/main']:
   for p in root.rglob('*'):
    if not p.is_file():continue
    assert z.read(p.relative_to(root).as_posix())==p.read_bytes()
    if p.suffix=='.class':assert struct.unpack('>H',p.read_bytes()[6:8])[0]==65;nc+=1
    else:nr+=1
  assert not any(any(tag in n for tag in ['Armor88FullMeshes','RuneGlow104Checks','/smoke/']) for n in z.namelist())
 archive=R/'.tools/armor88-artifacts'/jar.name;archive.parent.mkdir(exist_ok=True);shutil.copy2(jar,archive);assert sha(archive)==sha(jar)
 result=dict(status='PASS_SHARED47_JAR_WITH_IDENTICAL_ARMOR_SCOPE',version='1.9.7.47',jar=str(jar),archived_jar=str(archive),sha256=sha(jar),resources=nr,java21_classes=nc,armor_source_unchanged_since_verified46=True,parallel_only_changes=changed,build_log='../rune-glow-visible-47/build.log',clean_build_by='parallel effects agent; armor task independently ran clean46',source_base_commit='69a2db12de4baac9cef7c43e6c4b0ae717d3b644',source_commit='UNCOMMITTED',minecraft_launches=0,runtime='NOT_RUN',owner_acceptance='PENDING',polymc='47 NOT_INSTALLED by this task; observed active45')
 (O/'shared47-verification.json').write_text(json.dumps(result,indent=2)+'\n');print('ARMOR88_SHARED47_PASS',result['sha256'])
if __name__=='__main__':main()
