"""Black Cinder production resource/UV/vanilla-preservation and JAR audit.
No game/server/world launch. Source checks never substitute for behavioral QA.
"""
from pathlib import Path
import hashlib,json,zipfile,subprocess
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';RES=R/'src/main/resources';O=R/'docs/implementation/black-cinder-production'
def read(p):return json.loads(p.read_text(encoding='utf-8'))
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 m=read(O/'asset-manifest.json');assert len(m['blocks'])==26 and len(set(m['blocks']))==26;assert len(m['residents'])==4
 for n in m['blocks']:
  for p in (A/f'blockstates/{n}.json',A/f'models/item/{n}.json',RES/f'data/slavicmyths/loot_table/blocks/{n}.json'):assert p.is_file(),p
 for lang in ('ru_ru','en_us'):
  data=read(A/f'lang/{lang}.json')
  for n in m['blocks']:assert data.get('block.slavicmyths.'+n),n
  for n in m['residents']+[m['boss']]:assert data.get('entity.slavicmyths.'+n) and data.get('item.slavicmyths.'+n+'_spawn_egg'),n
  # Every pre-existing translation remains unchanged.
  old=json.loads(subprocess.check_output(['git','show','HEAD:src/main/resources/assets/slavicmyths/lang/'+lang+'.json'],cwd=R).decode('utf-8'));assert all(data.get(k)==v for k,v in old.items()),'old translations changed'
 for n in m['residents']+[m['boss']]:
  rig=read(A/f'cinder_rigs/{n}.json');occupied=set();names={'root'}
  for b in rig['parts']:
   assert b['parent'] in names and b['name'] not in names;names.add(b['name']);u,v=b['uv'];sx,sy,sz=b['size'];pixels={(x,y) for x in range(u,u+2*(sx+sz)) for y in range(v,v+sy+sz)};assert not occupied.intersection(pixels);occupied.update(pixels)
  assert all(0<=x<rig['atlas'] and 0<=y<rig['atlas'] for x,y in occupied)
  assert Image.open(A/f'textures/entity/{n}.png').size==(rig['atlas'],rig['atlas'])
 oldsounds=json.loads(subprocess.check_output(['git','show','HEAD:src/main/resources/assets/slavicmyths/sounds.json'],cwd=R).decode('utf-8'));sounds=read(A/'sounds.json');assert all(sounds.get(k)==v for k,v in oldsounds.items()),'old sounds changed'
 index=read(Path.home()/'.gradle/caches/neoformruntime/assets/indexes/17.json')['objects'];cache=Path.home()/'.gradle/caches/neoformruntime/assets/objects'
 soundhash=index['minecraft/sounds.json']['hash'];vanilla=read(cache/soundhash[:2]/soundhash)
 def files(event,seen=None):
  seen=set() if seen is None else set(seen)
  assert event not in seen,event;seen.add(event);assert event in vanilla,event
  result=[]
  for entry in vanilla[event]['sounds']:
   if isinstance(entry,str):result.append(entry)
   elif entry.get('type')=='event':result+=files(entry['name'].removeprefix('minecraft:'),seen)
   else:result.append(entry['name'])
  return result
 routes={}
 for event,value in sounds.items():
  if not event.startswith('cinder.'):continue
  source=value['sounds'][0]['name'].removeprefix('minecraft:');actual=[]
  for f in files(source):
   key='minecraft/sounds/'+f.removeprefix('minecraft:')+'.ogg';assert key in index,key;h=index[key]['hash'];p=cache/h[:2]/h
   actual.append({'asset':key,'sha1':h,'cached':p.is_file(),'bytes_verified':p.is_file() and hashlib.sha1(p.read_bytes()).hexdigest()==h})
  routes[event]={'vanilla_event':source,'actual_files':actual,'subtitles':value['subtitle']}
 write=lambda p,d:p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 write(O/'audio-routing.json',{'source':'Minecraft1.21.1 aliases; no copied/generated audio','listening':'NOT_RUN','events':routes,'callsites':['CinderMob#begin/impact/bite/getHurtSound/getDeathSound','CinderMantle#use/damage']})
 biome=read(RES/'data/slavicmyths/worldgen/biome/black_cinder.json');assert set(x['type'] for x in biome['spawners']['monster'] if x['type'].startswith('slavicmyths:'))=={'slavicmyths:coal_crawler','slavicmyths:ash_spirit','slavicmyths:pekelnik'}
 placement=read(RES/'data/slavicmyths/worldgen/structure_set/zharenitsa_arena.json')['placement'];assert placement['frequency']==1 and placement['spacing']-placement['separation']==1
 # Preserve tracked payload exactly, allowing only Git's Windows checkout CRLF.
 for rel in ('data/minecraft/dimension_type/the_nether.json','data/minecraft/worldgen/noise_settings/nether.json','data/slavicmyths/worldgen/biome/navya_grove.json'):
  assert (RES/rel).read_text(encoding='utf-8')==subprocess.check_output(['git','show','HEAD:src/main/resources/'+rel],cwd=R).decode('utf-8').replace('\r\n','\n'),rel
 build=read(R/'build/current-resources.json');jar=Path(build['jar'])
 with zipfile.ZipFile(jar) as z:
  assert not any('verify/Cinder' in n or n.startswith('tools/') for n in z.namelist())
  for n in m['residents']+[m['boss']]:assert z.read('assets/slavicmyths/cinder_rigs/'+n+'.json')==(A/f'cinder_rigs/{n}.json').read_bytes()
 report={'source_and_JAR':'PASS','jar':str(jar),'sha256':sha(jar),'blocks':len(m['blocks']),'residents':len(m['residents']),'bosses':1,'auxiliary_entities':2,'sound_aliases':len(routes),'native_model_frames':sum(len(r['frames']) for r in read(O/'native-rigs.json').values()),'runtime_launches':0,'gameplay':'NOT_RUN','original_art':'ART_ORIGINAL_COMPARE_NOT_RUN','owner':'OWNER_PENDING'}
 write(O/'gates.json',report);print(json.dumps(report))
if __name__=='__main__':main()
