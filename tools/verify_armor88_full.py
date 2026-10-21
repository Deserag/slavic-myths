"""#88 A1-A8 source/resource gates. Never starts a game or grants acceptance."""
from pathlib import Path
import json,hashlib,zipfile,tomllib,struct,subprocess,sys
from PIL import Image
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[1];O=R/'docs/implementation/armor88-full'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 base=json.loads((O/'baseline-sha256.json').read_text());scope=json.loads((O/'integration-scope.json').read_text());a=json.loads((O/'before-native.json').read_text());b=json.loads((O/'after-native.json').read_text());assert len(a)==len(b)==232
 ids=list(dict.fromkeys(s['id'] for s in b));assert len(ids)==29
 excluded=['invisibility_cap','woven_belt','hunter_belt','volchiy_poyas','poyas_tugarina','gift_alyosha_belt','underground_guard_belt','perunite_shield','retainer_shield','ward_shield','gift_ilya_shield','miners_goggles','finist_quiver']
 catalog=[line.split('\t')[0] for line in (R/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text().splitlines() if '\tarmor\t' in line];assert len(catalog)==42 and set(catalog)==set(ids+excluded)
 changed=[n for n,h in base.items() if sha(R/n)!=h];added=[p.relative_to(R).as_posix() for p in (R/'src').rglob('*') if p.is_file() and p.relative_to(R).as_posix() not in base]
 own=set(scope['changed']+scope['added'])
 foreign=lambda n:n.startswith('src/main/java/org/slavicmyths/client/runeglow/') or n.startswith('src/main/resources/data/minecraft/tags/item/enchantable/') or n in ['src/main/resources/assets/slavicmyths/shaders/core/rune_glint.fsh','src/main/resources/assets/slavicmyths/shaders/core/rune_glint.vsh','src/main/resources/assets/slavicmyths/textures/misc/rune_glint.png']
 assert all(n in own or foreign(n) for n in changed+added),[n for n in changed+added if n not in own and not foreign(n)]
 other=json.loads((R/'docs/implementation/polymc-19745/source-before.json').read_text());assert all(sha(R/n)==other[n] for n in changed+added if foreign(n)), 'parallel agent source changed since installed45'
 for id in excluded:
  for n,h in base.items():
   if id in Path(n).name:assert sha(R/n)==h,('excluded accessory modified',n)
 # Woven-belt geometry/render path is untouched, including the later44 clothing-only wrapper.
 source=(R/'src/main/java/org/slavicmyths/client/TextileClothingLayer.java').read_text();old=(O/'before/src/main/java/org/slavicmyths/client/TextileClothingLayer.java').read_text()
 section=lambda s:s[s.index('}else if(id.equals("woven_belt"))'):s.index('        root.addOrReplaceChild("head"')]
 assert section(source)==section(old) and 'RuneGlowBuffers.clothing(stack,buffers,pose)' in source
 unchanged_meshes=[]
 for x,y in zip(a,b):
  assert (x['id'],x['body'],x['pose'])==(y['id'],y['body'],y['pose'])
  assert x['layers'][0]==y['layers'][0],('skin/pivots mismatch',x['id'],x['pose'])
  if x['id'] in ['perunite_helmet','perunite_chestplate','dragon_chestplate','poloz_helmet','clear_sight_helmet']:
   assert x['layers'][1]['faces']==y['layers'][1]['faces'],('unexpected KEEP mesh change',x['id']);unchanged_meshes.append(x['id'])
 # Real geometric spacing of trousers and bast shoes, in the same idle native world coordinates.
 for body in ['Steve','Alex']:
  layer=lambda id:next(s['layers'][1] for s in b if s['id']==id and s['body']==body and s['pose']=='idle')
  ports=layer('linen_ports');shoes=layer('bast_shoes');max_y=max(v[1] for f in ports['faces'] for v in f);min_y=min(v[1] for f in shoes['faces'] for v in f);assert min_y-max_y>.4
 # Atlas files/JSON linkage, nearest integer texture dimensions and all native face UV ranges.
 for s in b:
  p=R/'src/main/resources/assets/slavicmyths'/s['layers'][1]['texture'];assert p.exists()
  with Image.open(p) as im:assert im.mode=='RGBA' and im.width in [64,128] and im.height in [32,64,128]
  for f in s['layers'][1]['faces']:
   assert len(f)==4
   for v in f:assert len(v)==5 and all(__import__('math').isfinite(n) for n in v) and 0<=v[3]<=1 and 0<=v[4]<=1
 for id in ids:
  model=json.loads((R/f'src/main/resources/assets/slavicmyths/models/item/{id}.json').read_text());assert model['textures']['layer0']=='slavicmyths:item/'+id
  with Image.open(R/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png') as im:im.verify()
 # Explicit scoped generator is idempotent; it cannot alter unrelated resources.
 import armor88_art as art
 art.A=R/'src/main/resources/assets/slavicmyths'
 before={p:sha(p) for p in (R/'src/main/resources').rglob('*') if p.is_file()};art.apply();assert all(sha(p)==h for p,h in before.items())
 log=(R/'.tools/armor88-full-build.log').read_text(encoding='utf-8-sig');assert 'BUILD SUCCESSFUL' in log and 'ARMOR88_FULL_NATIVE_PASS' in log and 'RUNE104_PLAIN_JVM_PASS' in log
 version=next(l.split('=',1)[1] for l in (R/'gradle.properties').read_text().splitlines() if l.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar';nr=np=nc=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None;meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']==version for m in meta['mods'])
  for p in (R/'build/resources/main').rglob('*'):
   if not p.is_file():continue
   assert z.read(p.relative_to(R/'build/resources/main').as_posix())==p.read_bytes();nr+=1
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    np+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   data=p.read_bytes();assert struct.unpack('>H',data[6:8])[0]==65 and z.read(p.relative_to(R/'build/classes/java/main').as_posix())==data;nc+=1
  assert not any(any(k in n for k in ['Armor88FullMeshes','Armor88LinenMeshes','RuneGlow104Checks','/smoke/']) for n in z.namelist())
  assert 'org/slavicmyths/client/BasicArmorModels.class' in z.namelist() and 'org/slavicmyths/client/ArmorStandClothingLayer.class' in z.namelist()
 # Proofs must match the exact current inputs, not a render of earlier staging data.
 inputs=json.loads((O/'preview-input-sha256.json').read_text());assert all(sha(R/n)==h for n,h in inputs.items()),'stale source preview'
 assert all((O/(id+'.png')).exists() for id in ids)
 outputs=json.loads((O/'preview-output-sha256.json').read_text());assert all(sha(O/n)==h for n,h in outputs.items())
 original=json.loads((R/'.tools/armor88-final-inputs.json').read_text());assert all(sha(R/n)==h for n,h in original.items()),'concurrent production edits during build'
 overlap=json.loads((O/'coplanar-intersections.json').read_text());assert all(not row['after'] for row in overlap['rows'])
 result=dict(status='PASS_SOURCE_RESOURCES_BUILD_ONLY',version=version,jar=str(jar),sha256=sha(jar),ids=ids,excluded_unchanged=excluded,unchanged_mesh_ids=sorted(set(unchanged_meshes)),native_samples=232,source_views_per_id=40,resources=nr,png=np,java21_classes=nc,armor_changed=scope['changed'],armor_added=scope['added'],parallel44_preserved=[n for n in changed+added if foreign(n)],source_base_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R).decode().strip(),source_commit='UNCOMMITTED working tree; no automatic commit',minecraft_launches=0,runtime='NOT_RUN',owner_acceptance='PENDING',polymc=version+' NOT_INSTALLED; other agent installed45; exact receipt in ../polymc-19745/installed.json')
 (O/'verification.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
 manifest={p.relative_to(R).as_posix():sha(p) for p in (R/'src').rglob('*') if p.is_file()};manifest['gradle.properties']=sha(R/'gradle.properties');(O/'build-source-sha256.json').write_text(json.dumps(manifest,indent=2)+'\n')
if __name__=='__main__':main()
