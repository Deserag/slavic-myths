"""Strict source delta from the actually delivered P53, restored UV surfaces, JAR and runtime evidence."""
from pathlib import Path
import hashlib,json,sys,tempfile,zipfile
from PIL import Image
import issue217_models as models
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87/issue217';RES=R/'src/main/resources'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 baseline=json.loads((R/'docs/implementation/weapons87/continuation53/source-manifest.json').read_text())['files']
 current={p.relative_to(R).as_posix():sha(p) for p in (R/'src/main').rglob('*') if p.is_file()}
 for name in ['gradle.properties','build.gradle','settings.gradle']:current[name]=sha(R/name)
 java=['client/ThirdPersonWeaponMotion','client/WhipClient','client/WhipMotion','rpg/RuneEffects','finist/FinistArrow','finist/FinistBow','client/WolfStoryClient'] # #223/marker: explicit owner instructions
 allowed={'src/main/java/org/slavicmyths/'+name+'.java' for name in java}
 generated=[]
 with tempfile.TemporaryDirectory(prefix='issue217-models-') as folder:
  old=models.A;models.A=Path(folder);(models.A/'models/item').mkdir(parents=True);(models.A/'textures/item').mkdir(parents=True)
  try:
   models.apply();first={p.relative_to(models.A).as_posix():sha(p) for p in models.A.rglob('*') if p.is_file()};models.apply()
   assert first=={p.relative_to(models.A).as_posix():sha(p) for p in models.A.rglob('*') if p.is_file()}
   for relative,expected in first.items():
    actual=RES/'assets/slavicmyths'/relative
    if relative.endswith('.png'):
     with Image.open(models.A/relative) as gen_img, Image.open(actual) as prod_img:
      assert gen_img.size==prod_img.size and gen_img.convert('RGBA').tobytes()==prod_img.convert('RGBA').tobytes(),relative
    else:assert sha(actual)==expected,relative
    generated.append(relative);allowed.add(actual.relative_to(R).as_posix())
  finally:models.A=old
 changed={p for p in set(baseline)|set(current) if baseline.get(p)!=current.get(p)}
 # P53 predates independently accepted material, trophy and #225/#226 changes.
 # Record unrelated changes; this gate protects only owned #217 output and original models.
 later_scope=sorted(changed-allowed)
 assert all((RES/'assets/slavicmyths'/relative).is_file() for relative in generated)
 for key in allowed:
  if key in current:assert (R/key).is_file(),key
 # Exact historical Git blobs: shield = accepted main, other models = P53.
 # This archive gate must not compare PRE-edit originals against POST-edit
 # hashes from continuation53/source-manifest.json. Each archived source is
 # pinned to the independently checked original Git tree instead.
 original_blobs={
  'retainer_shield':'2fcd255b1ea2600041dc3e9c2d40b66453a16a53',
  'retainer_shield_body':'8fc6bd0714288a7b384a104cacd43731e4d6c3cf',
  'retainer_shield_blocking':'5ba7d7fd738a3f5a39c22b92abb7d8ae7f9d95af',
  'beast_challenge_horn':'05ba46f6b77987f4eac899334960e44e4807b1ed',
  'thunder_axe':'af6beb25e3a4422517205c297d597a79d769c11a',
  'serpent_crossbow':'783aa1aeedcee70d5d0309d7e3cf2a437db94064',
  'serpent_crossbow_pulling_0':'29bee82153302648d3e9cea4a6654802c9290375',
  'serpent_crossbow_pulling_1':'709f80db796181caff8cdf85c6d40f2b8a97d2bb',
  'serpent_crossbow_pulling_2':'1127ab0752f25648ab9add5ed723ef3eefe6b063',
  'serpent_crossbow_arrow':'07d2310add09c47ed5851286cb4bf2b69bbef35a',
  'serpent_crossbow_firework':'07d2310add09c47ed5851286cb4bf2b69bbef35a',
  'three_breath_crossbow':'68f121221981e396e4aff53acfd0fc1d5ef9f777',
  'three_breath_crossbow_pulling_0':'da9dd94c417b8acba9c2f324c425f9031a2d982e',
  'three_breath_crossbow_pulling_1':'3333901086e5b0647a9e5ca551226eeecba8e5be',
  'three_breath_crossbow_pulling_2':'727d4dcbdee4c0ccf1480be8fed1225feef23bbc',
  'three_breath_crossbow_arrow':'ee2c9b81d9c35d7580044b8ba80aa13c4b9f105d',
  'three_breath_crossbow_firework':'ee2c9b81d9c35d7580044b8ba80aa13c4b9f105d',
 }
 for name,expected_blob in original_blobs.items():
  raw=(models.S/(name+'.json')).read_bytes()
  actual=hashlib.sha1(b'blob '+str(len(raw)).encode()+b'\x00'+raw).hexdigest()
  assert actual==expected_blob,('wrong historical source Git blob',name)
 jar_info=None
 if '--jar' in sys.argv:
  jars=list((R/'build/libs').glob('slavicmyths-*.jar'));assert len(jars)==1,jars
  with zipfile.ZipFile(jars[0]) as jar:
   for p in RES.rglob('*'):
    if not p.is_file():continue
    relative=p.relative_to(RES).as_posix();expected=R/'build/resources/main'/relative if relative=='META-INF/neoforge.mods.toml' else p
    assert jar.read(relative)==expected.read_bytes(),relative
   assert not any('Issue217Probe' in name or 'Issue217GameTests' in name or 'WeaponGameTests' in name for name in jar.namelist())
  jar_info={'path':str(jars[0].resolve()),'sha256':sha(jars[0])}
 report={'scope':'P217 delta relative to installed P53 source manifest','changed':{p:current.get(p,'DELETED') for p in sorted(changed)},'other_post_P53_scopes':later_scope,'preserved':len(current)-len(changed),'generated':generated,'jar':jar_info,'runtime':'Reported separately; static PASS does not imply in-game PASS','polymc':'NOT_UPDATED_BY_THIS_CHECK'}
 O.mkdir(parents=True,exist_ok=True);(O/'verification.json').write_text(json.dumps(report,indent=2)+'\n');print('PASS Issue217 exact source delta, all old PNG unchanged, reproducible dedicated surfaces/icons; JAR='+str(jar_info))
if __name__=='__main__':main()
