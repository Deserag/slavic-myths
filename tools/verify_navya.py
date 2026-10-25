"""Verify the implemented asset scope; explicitly report unfinished dependencies."""
from pathlib import Path
import hashlib,json,subprocess,sys,zipfile
from PIL import Image

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths'
REPORT=ROOT/'docs/implementation/navya-grove'
PRESERVATION_REF=sys.argv[sys.argv.index('--preservation-ref')+1] if '--preservation-ref' in sys.argv else '275ecaa5'

def baseline(path):
 return subprocess.check_output(['git','show',PRESERVATION_REF+':'+path],cwd=ROOT)

def check_model(data):
 if isinstance(data,dict):
  parent=data.get('parent','')
  if parent.startswith('slavicmyths:'):assert (A/'models'/(parent.split(':')[1]+'.json')).exists(),parent
  for texture in data.get('textures',{}).values():
   if texture.startswith('slavicmyths:'):assert (A/'textures'/(texture.split(':')[1]+'.png')).exists(),texture
  for face in data.get('faces',{}).values():
   if 'uv' in face:assert all(0<=v<=16 for v in face['uv'])
  for element in data.get('elements',[]):
   assert all(0<=v<=16 for v in element['from']+element['to'])
   assert all(a<b for a,b in zip(element['from'],element['to']))
  for value in data.values():check_model(value)
 elif isinstance(data,list):
  for value in data:check_model(value)

def main():
 manifest=json.loads((REPORT/'asset-manifest.json').read_text());blocks=manifest['geology']+manifest['wood']+manifest['flora']
 for name in blocks:
  for path in [A/'blockstates'/(name+'.json'),A/'models/item'/(name+'.json'),RES/'data/slavicmyths/loot_table/blocks'/(name+'.json')]:assert path.is_file(),path
  state=json.loads((A/'blockstates'/(name+'.json')).read_text())
  def refs(data):
   if isinstance(data,dict):
    if 'model' in data and data['model'].startswith('slavicmyths:'):assert (A/'models'/(data['model'].split(':')[1]+'.json')).is_file(),data['model']
    for v in data.values():refs(v)
   elif isinstance(data,list):
    for v in data:refs(v)
  refs(state)
 previous=set(subprocess.check_output(['git','ls-tree','-r','--name-only','275ecaa5','src/main/resources/assets/slavicmyths/models'],cwd=ROOT,text=True).splitlines())
 for path in (A/'models').rglob('*.json'):
  if path.relative_to(ROOT).as_posix() not in previous:check_model(json.loads(path.read_text(encoding='utf-8')))
 for path in (A/'textures/block').glob('*navya*.png'):
  with Image.open(path) as im:assert im.size==(16,16);im.verify()
 for locale in ['ru_ru','en_us']:
  path='src/main/resources/assets/slavicmyths/lang/'+locale+'.json';old=json.loads(baseline(path));new=json.loads((ROOT/path).read_text(encoding='utf-8'))
  for key,value in old.items():assert new[key]==value,('unrelated translation changed',key)
  for name in blocks:assert 'block.slavicmyths.'+name in new
 old=json.loads(baseline('src/main/resources/assets/slavicmyths/sounds.json'));new=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 for key,value in old.items():assert new[key]==value,('quarantine/old audio drift',key)
 for path in (RES/'data/minecraft/tags').rglob('*.json'):
  relative=path.relative_to(ROOT).as_posix()
  exists=subprocess.run(['git','cat-file','-e','275ecaa5:'+relative],cwd=ROOT,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode==0
  if exists:
   before=json.loads(baseline(relative));after=json.loads(path.read_text())
   assert all(v in after['values'] for v in before['values']),('removed vanilla integration tag value',relative)
 # Previous HELL terrain profile must remain byte-for-byte unchanged.
 inherited=json.loads((ROOT/'docs/implementation/hell-patch1/source-manifest.json').read_text())
 for path,digest in inherited['productionDelta'].items():
  raw=(ROOT/path).read_bytes();lf=raw.replace(b'\r\n',b'\n')
  assert digest in {hashlib.sha256(v).hexdigest() for v in [raw,lf,lf.replace(b'\n',b'\r\n')]},('inherited HELL drift',path)
 rigs=json.loads((REPORT/'native-rigs.json').read_text())
 for name,rig in rigs.items():
  tex=Image.open(A/'textures/entity'/(name+'.png')).convert('RGBA')
  assert tex.size==(rig['atlas'],rig['atlas'])
  assert Image.open(A/'textures/entity'/(name+'_glow.png')).size==tex.size
  occupied=set()
  for b in rig['boxes']:
   # Each actual Cube net owns a disjoint region of its source atlas.
   pixels={(x,y) for x in range(b['u'],b['u']+2*(b['sx']+b['sz'])) for y in range(b['v'],b['v']+b['sy']+b['sz'])}
   assert not occupied.intersection(pixels),('UV overlap',name,b['part'])
   occupied.update(pixels)
  for frame in rig['frames']:
   for face in frame['faces']:
    u=sum(p[3] for p in face)/4;v=sum(p[4] for p in face)/4
    assert tex.getpixel((min(tex.width-1,int(u*tex.width)),min(tex.height-1,int(v*tex.height))))[3],('empty native UV face',name,frame['action'])
 for mob in ['navka','rootling','bone_crawler']:
  table=json.loads((RES/'data/slavicmyths/loot_table/entities'/(mob+'.json')).read_text())
  children=table['pools'][0]['entries'][0]['children']
  assert [c['name'] for c in children]==['slavicmyths:soul_blight','slavicmyths:soul','slavicmyths:soul_fragment']
  assert children[0]['conditions'][0]['chance']==.005
  chance=children[1]['conditions'][0];assert chance['unenchanted_chance']==.02
  assert chance['enchanted_chance']=={'type':'minecraft:linear','base':.03,'per_level_above_first':.01}
  assert children[2]['functions'][0]['count']['min']==1 and children[2]['functions'][0]['count']['max']==3
 guardian=json.loads((RES/'data/slavicmyths/loot_table/entities/grove_guardian.json').read_text())
 assert [p['entries'][0]['name'] for p in guardian['pools']]==['slavicmyths:grove_core','slavicmyths:guardian_branch','slavicmyths:soul','slavicmyths:soul_blight']
 assert all(p['rolls']==1 and not p.get('conditions') and not p['entries'][0].get('conditions') for p in guardian['pools'])
 assert not json.loads((RES/'data/slavicmyths/loot_table/entities/navya_wisp.json').read_text())['pools']
 biome=json.loads((RES/'data/slavicmyths/worldgen/biome/navya_grove.json').read_text())
 assert biome['features'][2]==['slavicmyths:navya_geology'] and 'slavicmyths:navya_nature' in biome['features'][9]
 assert (RES/'slavicmyths.navya.mixins.json').is_file()
 result={'implementedBlocks':len(blocks),'sourceAssetLinks':'PASS','legacyTranslationsAudioTagsPreserved':'PASS','preservationRef':PRESERVATION_REF,
         'inheritedHellProfile':'PASS','gameLaunches':0,
         'nativeRigsAndUVTextureCoverage':len(rigs),'approvedLootTables':'PASS (source contract)',
         'naturalBiomeAndHeartIntegration':'IMPLEMENTED_SOURCE; runtime NOT_RUN',
         'requiredRemaining':{'originalChatPNGComparison':'NOT_RUN (originals unavailable in issues)',
                              'FallingTreeAndBiomeRuntime':'NOT_RUN','combatSaveReloadMultiplayer':'NOT_RUN',
                              'soundHearing':'NOT_RUN','ownerAcceptance':'NOT_RUN'}}
 if '--jar' in sys.argv:
  artifact=Path(sys.argv[sys.argv.index('--jar')+1]);
  with zipfile.ZipFile(artifact) as jar:
   for folder in ['assets/slavicmyths','data/slavicmyths']:
    for path in (RES/folder).rglob('*'):
     if path.is_file():assert jar.read(path.relative_to(RES).as_posix())==path.read_bytes(),path
   assert not any(n.startswith('org/slavicmyths/verify/') or n.endswith('NavyaGeometry.class') for n in jar.namelist())
  result['packagedResourceBytes']='PASS';result['jarSha256']=hashlib.sha256(artifact.read_bytes()).hexdigest()
 (REPORT/'gates.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,indent=2))

if __name__=='__main__':main()
