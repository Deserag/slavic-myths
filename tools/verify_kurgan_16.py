"""Offline approved-asset/model/UV and production parity checks. No game launch."""
from pathlib import Path
import hashlib, io, json, struct, subprocess, zipfile
from collections import Counter
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
OUT=ROOT/'docs/implementation/kurgan-16-v2'
SCOPE_BASE='722eabc203cd56452c6767044e7f2c3a72527a34'
BASE='75786e6a3c589c2683dae89dd3cff0917ce6ca2d'
checks=0
def need(ok,msg):
 global checks
 checks+=1
 if not ok:raise AssertionError(msg)
def sha(data):return hashlib.sha256(data).hexdigest()
def main():
 OUT.mkdir(parents=True,exist_ok=True)
 manifest=json.loads((ROOT/'art/kurgan-16-approved/manifest.json').read_text())
 names=list(manifest['textures']);targets={'slavicmyths:block/'+n for n in names}
 archive=ROOT/'art/kurgan-16-approved/approved.zip'
 need(sha(archive.read_bytes())==manifest['archive_sha256']=='98a01d34281371c40edfb5928a37f2538fbae78e140fa97bd148b348a9ae457b','V2 archive SHA256')
 with zipfile.ZipFile(archive) as z:
  need(len(z.infolist())==12 and z.testzip() is None,'11 PNGs + embedded manifest, CRC')
  embedded=json.loads(z.read('manifest.json'))
  need(set(z.namelist())=={n+'.png' for n in names}|{'manifest.json'},'exact archive members')
  need(manifest['art_revision']=='V2' and embedded['version']=='kurgan-v2-art-redesign','V2 revision')
  for n in names:
   p=RES/f'assets/slavicmyths/textures/block/{n}.png';data=p.read_bytes()
   need(data==z.read(p.name) and sha(data)==manifest['textures'][n]['sha256']==embedded['files'][n+'.png']['sha256'],'exact approved bytes '+n)
   im=Image.open(io.BytesIO(data));im.verify();need(im.size==(16,16) and im.mode=='RGB','native RGB16 '+n)
   old=ROOT/f'art/kurgan-0.9.6/runtime-256-backup/{n}.png'
   need(sha(old.read_bytes())==manifest['textures'][n]['old_sha256'],'old PNG preserved '+n)
   need(old.read_bytes()==subprocess.check_output(['git','show',BASE+':'+p.relative_to(ROOT).as_posix()],cwd=ROOT),'exact prior runtime backup '+n)
 # Full production resource/class parity, including unrelated resources preserved by Git scope below.
 version=dict(line.split('=',1) for line in (ROOT/'gradle.properties').read_text().splitlines() if '=' in line)['mod_version']
 jar=ROOT/f'build/libs/slavicmyths-{version}.jar'
 build=json.loads((OUT/'BUILD.json').read_text())
 need(build['status']=='PASS' and build['version']==version and build['jar_sha256']==sha(jar.read_bytes()),'recorded clean build matches artifact')
 vanilla=zipfile.ZipFile(ROOT/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar')
 cache={}
 def load(ref):
  if ref in cache:return cache[ref]
  ns,path=ref.split(':') if ':' in ref else ('minecraft',ref)
  rel=f'assets/{ns}/models/{path}.json';p=RES/rel
  value=json.loads(p.read_bytes() if p.exists() else vanilla.read(rel));cache[ref]=value;return value
 def merged(ref,seen=None):
  seen=set() if seen is None else seen;need(ref not in seen,'model cycle '+ref);seen.add(ref)
  v=load(ref);parent=v.get('parent');base=merged(parent,seen) if parent and not parent.startswith('builtin/') else {}
  result={**base,**v};result['textures']={**base.get('textures',{}),**v.get('textures',{})};return result
 affected=[];usage={n:[] for n in names}
 for p in (RES/'assets/slavicmyths/models/block').glob('*.json'):
  ref='slavicmyths:block/'+p.stem;v=merged(ref)
  if not targets.intersection(v.get('textures',{}).values()):continue
  affected.append(p.stem)
  for tex in targets.intersection(v['textures'].values()):usage[tex.split('/')[-1]].append(p.stem)
  for el in v.get('elements',[]):
   for face in el.get('faces',{}).values():
    uv=face.get('uv',[0,0,16,16]);need(len(uv)==4 and all(0<=x<=16 for x in uv),'normalized UV '+ref)
    tex=face['texture'];visited=set()
    while tex.startswith('#'):
     need(tex not in visited,'texture cycle '+ref);visited.add(tex);need(tex[1:] in v['textures'],'unresolved texture '+ref);tex=v['textures'][tex[1:]]
    ns,path=tex.split(':') if ':' in tex else ('minecraft',tex);rel=f'assets/{ns}/textures/{path}.png'
    need((RES/rel).exists() or rel in vanilla.namelist(),'missing texture '+rel)
 for name,models in usage.items():need(bool(models),'texture unused '+name)
 # Static Creative audit: preserve distinct registered items, do not hide similar-looking materials.
 catalog=(RES/'assets/slavicmyths/creative_catalog.tsv').read_text('utf8')
 ids=[line.split('\t')[0] for line in catalog.splitlines() if line and not line.startswith('#')]
 counts=Counter(ids);need(all(n==1 for n in counts.values()),'no repeated Creative catalog IDs')
 source=(ROOT/'src/main/java/org/slavicmyths/registry/ModItemGroup.java').read_text('utf8')
 need('BuiltInRegistries.ITEM.stream()' in source and 'for (var item : items(category))' in source,'Creative iterates unique registry entries')
 creative=[]
 for name in ['pale_blue_kurgan_cobblestone','cracked_pale_blue_kurgan_cobblestone','mossy_pale_blue_kurgan_cobblestone']:
  need(counts[name]==1,'one Creative catalog entry '+name)
  item=load('slavicmyths:item/'+name);need(item['parent']=='slavicmyths:block/'+name,'exact item parent '+name)
  v=merged(item['parent'])
  need(v['textures']['side']=='slavicmyths:block/'+name,'distinct side texture '+name)
  need(v['textures']['top']==v['textures']['bottom']=='slavicmyths:block/burial_stone_top','shared burial top/bottom '+name)
  labels={lang:json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_bytes())['block.slavicmyths.'+name] for lang in ['ru_ru','en_us']}
  creative.append(dict(item_id='slavicmyths:'+name,model=item['parent'],side=v['textures']['side'],labels=labels))
 need('pale_blue_kurgan_stone_variant' not in counts and not (RES/'assets/slavicmyths/models/item/pale_blue_kurgan_stone_variant.json').exists(),'weighted variant is not an item')
 for lang in ['ru_ru','en_us']:
  labels=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_bytes())
  blue_labels=[labels['block.slavicmyths.'+n] for n in ids if 'pale_blue_kurgan' in n]
  need(len(blue_labels)==len(set(blue_labels)),'no duplicated blue-family display names '+lang)
 need(len({Image.open(RES/f'assets/slavicmyths/textures/block/{n}.png').tobytes() for n in names})==11,'all11 V2 pixel designs distinct')
 (OUT/'CREATIVE.json').write_text(json.dumps(dict(scope='offline catalog/source/model inspection; no client',duplicate_catalog_ids=[],items=creative,variant='weighted model only, not a block/item ID',same_top_bottom='burial_stone_top',observed_pair='Cannot identify exact player-observed stacks without their tooltip/IDs; no blocks removed'),ensure_ascii=False,indent=2)+'\n',encoding='utf8')
 def walk(node):
  if isinstance(node,dict):
   for k,v in node.items():
    if k=='model' and isinstance(v,str):load(v);need(True,'blockstate model')
    elif isinstance(v,(dict,list)):walk(v)
  elif isinstance(node,list):
   for v in node:walk(v)
 for p in (RES/'assets/slavicmyths/blockstates').glob('*.json'):walk(json.loads(p.read_bytes()))
 for name in affected:
  base=name.removesuffix('_top').removesuffix('_inner').removesuffix('_outer').removesuffix('_inventory').removesuffix('_post').removesuffix('_side_tall').removesuffix('_side')
  item=RES/f'assets/slavicmyths/models/item/{base}.json'
  if item.exists():merged('slavicmyths:item/'+base);need(True,'item parent '+base)
 changed=subprocess.check_output(['git','diff','--name-only',SCOPE_BASE,'--','src/main'],cwd=ROOT).decode().splitlines()
 need(set(changed)=={f'src/main/resources/assets/slavicmyths/textures/block/{n}.png' for n in names},'only11 PNGs changed in production; models/registry/worldgen/gameplay unchanged')
 resources=[p for p in RES.rglob('*') if p.is_file()]
 with zipfile.ZipFile(jar) as z:
  need(z.testzip() is None,'JAR CRC')
  for p in resources:
   rel=p.relative_to(RES).as_posix()
   if p.suffix=='.json':json.loads(p.read_bytes());need(True,'JSON')
   if p.suffix=='.png':
    with Image.open(p) as im:im.verify()
    need(True,'PNG')
   need(version in z.read(rel).decode() if rel=='META-INF/neoforge.mods.toml' else z.read(rel)==p.read_bytes(),'JAR resource parity '+rel)
  expected={p.relative_to(ROOT/'build/classes/java/main').as_posix() for p in (ROOT/'build/classes/java/main').rglob('*.class')}
  actual={n for n in z.namelist() if n.endswith('.class')};need(expected==actual,'only production classes; no fixtures')
  for n in actual:need(struct.unpack('>H',z.read(n)[6:8])[0]==65,'Java21')
 # Pixel-preserving atlas of installed files, never a substitute for a game screenshot.
 atlas=Image.new('RGB',(1200,490),'#20252a');d=ImageDraw.Draw(atlas);font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',14)
 for i,n in enumerate(names):
  x=12+(i%6)*200;y=12+(i//6)*240
  im=Image.open(RES/f'assets/slavicmyths/textures/block/{n}.png');atlas.paste(im.resize((160,160),Image.Resampling.NEAREST),(x,y))
  label=n.replace('_',' ');d.text((x,y+168),label[:24],font=font,fill='white');d.text((x,y+186),label[24:48],font=font,fill='white');d.text((x,y+205),'16x16 - installed PNG',font=font,fill='#b6c6c9')
 atlas.save(OUT/'installed-atlas.png')
 report=dict(status='PASS',checks=checks,textures=11,affected_models=len(affected),model_usage=usage,resources=len(resources),jar_sha256=sha(jar.read_bytes()),version=version,production_diff='11 PNG files only',art_revision='V2',creative_duplicate_catalog_ids=[],runtime='NOT_RUN',clean_build='PASS',client_starts_this_delivery=0,game_test_starts=0,dedicated_server_starts=0)
 (OUT/'GATES.json').write_text(json.dumps(report,indent=2)+'\n')
 print(json.dumps({k:v for k,v in report.items() if k!='model_usage'}))
if __name__=='__main__':main()
