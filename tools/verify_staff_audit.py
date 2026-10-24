"""Final staff gate: real runtime logs, client captures, source/JAR parity, replay."""
from pathlib import Path
import hashlib,json,subprocess,sys,zipfile,re
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources';O=R/'docs/implementation/staff-audit-20261024'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def resources():return {p.relative_to(A).as_posix():sha(p) for p in A.rglob('*') if p.is_file()}
before=resources();subprocess.run([sys.executable,str(R/'tools/staff_resources.py')],check=True);assert resources()==before,'staff generator drift'
for name in ['staff-build.log','staff-offline.log','staff-runtime.log','staff-client.log','staff-client-inventory-3.log']:
 assert 'BUILD SUCCESSFUL' in (R/name).read_text(errors='replace'),name
runtime=(R/'staff-runtime.log').read_text(errors='replace');assert 'All 11 required tests passed' in runtime
assert len(list((O/'client').glob('*.png')))==36 and len(list((O/'client-inventory').glob('*.png')))==9
props=dict(v.split('=',1) for v in (R/'gradle.properties').read_text().splitlines() if '=' in v and not v.startswith('#'));jar=R/'build/libs'/('slavicmyths-'+props['mod_version']+'.jar')
with zipfile.ZipFile(jar) as z:
 for name,digest in before.items():
  if not name.startswith('META-INF/'):assert hashlib.sha256(z.read(name)).hexdigest()==digest,name
 assert not any(any(s in name for s in ['StaffAuditTests','StaffVisualProbe','FeedbackChecks','FrostChecks']) for name in z.namelist())
 for name in ['StaffStats','CombatStaffItem']:assert 'org/slavicmyths/combat/'+name+'.class' in z.namelist()
damage=[dict(id=m[0],base=float(m[1]),sharpness_v=float(m[2]),strength_i=float(m[3])) for m in re.findall(r'STAFF_ACTUAL_DAMAGE (\w+) base=([\d.]+) sharpnessV=([\d.]+) strengthI=([\d.]+)',runtime)]
assert len(damage)==3
artifact=dict(jar=str(jar),sha256=sha(jar),source_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),source_state='HEAD + preserved existing changes + uncommitted staff patch',resources_equal_to_jar=len(before),generator_replay='all resource bytes identical',runtime_tests='11/11 PASS in final server process',minecraft_processes=7,gametest_server_processes=3,client_processes=4,successful_client_capture_runs=2,client_onboarding_attempts_stopped=2,client_frames=45,polymc='NOT_INSTALLED',owner_acceptance='PENDING',actual_damage=damage)
(O/'artifact.json').write_text(json.dumps(artifact,indent=2));print(json.dumps(artifact,indent=2))
