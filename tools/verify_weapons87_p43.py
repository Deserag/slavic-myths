"""P43: five related issue regression checks (#188/#174/#117/#168/#121). No Minecraft acceptance."""
from pathlib import Path
import hashlib,json,shutil,sys,tempfile,zipfile
import weapons87_p43_overlay as p43
import weapons87_p42_models as p42
ROOT=Path(__file__).resolve().parents[1]
MODELS=ROOT/'src/main/resources/assets/slavicmyths/models/item'
p43.check()
p42.check()  # P42 must survive a new batch in the same branch
assert p43.BASE=={'thunder_spear':30,'berdysh':21,'carved_staff':11}
assert {n:len(parts) for n,parts in p43.ADDITIONS.items()}=={'thunder_spear':7,'berdysh':2,'carved_staff':3}
spear=p43.load('thunder_spear')
berdysh=p43.load('berdysh')
carved=p43.load('carved_staff')
# #188: slender point is not a wide axe; existing original tip remains untouched.
assert max(e['to'][0] for e in spear['elements'][:30])<=10
assert spear['elements'][30]['from'][1]>=18
assert max(e['to'][1] for e in spear['elements'])==30
assert all((e['to'][0]-e['from'][0])<=2.5 for e in p43.ADDITIONS['thunder_spear'])
# #174: broad original berdysh head / compact carved-staff wraps; geometry is really changed.
assert min(e['from'][0] for e in berdysh['elements'][:21])<=1.5
assert all(e['from'][1]>=19 for e in p43.ADDITIONS['berdysh'])
assert max(e['to'][1] for e in carved['elements'][:11])==30
assert all(16<=e['from'][1]<21 for e in p43.ADDITIONS['carved_staff'])
# #117: both physical arms retain the centered third-person grip, and FP untouched by overlay.
from weapons87_grips import plan
expected=plan()['carved_staff']['display']
for side in ('righthand','lefthand'):
 for view in ('thirdperson','firstperson'):
  key=view+'_'+side
  assert carved['display'][key]==expected[key],key
# #168: collars, mounts and bands are physical elements, never 14 other spear-tip IDs.
assert len(p43.ADDITIONS['thunder_spear'][:2])==2
assert all(e['to'][0]-e['from'][0]>2.0 for e in p43.ADDITIONS['thunder_spear'][:2])
# #121: P43 overlay can be applied twice in an isolated copy with identical resource hashes.
with tempfile.TemporaryDirectory(prefix='weapons87-p43-') as td:
 sandbox=Path(td)
 paths=[MODELS/(name+'.json') for name in p43.BASE]
 for path in paths:shutil.copy2(path,sandbox/path.name)
 old=p43.MODELS
 try:
  p43.MODELS=sandbox
  originals={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sandbox.iterdir()}
  p43.apply()
  p43.apply()
  assert originals=={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sandbox.iterdir()}
 finally:p43.MODELS=old
if '--jar' in sys.argv:
 jars=list((ROOT/'build/libs').glob('slavicmyths-*.jar'))
 assert len(jars)==1,jars
 with zipfile.ZipFile(jars[0]) as z:
  for name in p43.BASE:
   rel='assets/slavicmyths/models/item/'+name+'.json'
   assert z.read(rel)==(ROOT/'src/main/resources'/rel).read_bytes(),rel
print('PASS P43: real art models, pivots, safe socket details, two-pass canonical overlay, unchanged P42; Minecraft NOT_RUN')
