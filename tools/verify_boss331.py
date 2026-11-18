"""Current scope integrity: distinct native rigs/UV, actual legal sound bytes and production isolation."""
from pathlib import Path
import json,hashlib,zipfile,subprocess
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/boss331-rework'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
def main():
 rigs={}
 for rel in ['cinder_rigs/zharenitsa','burning_islands_rigs/magma_lord','lost_halls_rigs/posten_trophy','lost_halls_rigs/zharenitsa_trophy']:
  d=read(A/(rel+'.json'));names={'root'};rects=[]
  for p in d['parts']:
   assert p['parent'] in names and p['name'] not in names,(rel,p['name']);names.add(p['name']);x,y,z=p['size'];u,v=p['uv'];b=(u,v,u+2*(x+z),v+y+z)
   assert 0<=b[0]<b[2]<=d['atlas'] and 0<=b[1]<b[3]<=d['atlas']
   assert all(max(b[0],a[0])>=min(b[2],a[2]) or max(b[1],a[1])>=min(b[3],a[3]) for a in rects),(rel,p['name'],'UV overlap')
   rects.append(b)
  rigs[rel]=len(rects)
 masks=[p for p in read(A/'lost_halls_rigs/posten_trophy.json')['parts'] if p['name'].startswith('mask_') and p['name']!='mask_ring']
 assert len(masks)==4 and all(p['size'][1]==19 for p in masks)
 index=read(Path.home()/'.gradle/caches/neoformruntime/assets/indexes/17.json')['objects'];cache=Path.home()/'.gradle/caches/neoformruntime/assets/objects'
 h=index['minecraft/sounds.json']['hash'];vanilla=read(cache/h[:2]/h)
 def files(event,seen=()):
  assert event not in seen and event in vanilla,event;result=[]
  for e in vanilla[event]['sounds']:
   if isinstance(e,str):result.append(e)
   elif e.get('type')=='event':result+=files(e['name'].removeprefix('minecraft:'),(*seen,event))
   else:result.append(e['name'])
  return result
 sounds=read(A/'sounds.json');old=json.loads(subprocess.check_output(['git','show','HEAD:src/main/resources/assets/slavicmyths/sounds.json']).decode('utf-8'))
 assert all(sounds[k]==v for k,v in old.items()),'Existing audio routes changed'
 routes={}
 for k,event in sounds.items():
  if not k.startswith('islands.magma_lord.'):continue
  source=event['sounds'][0]['name'].removeprefix('minecraft:');actual=[]
  for file in files(source):
   key='minecraft/sounds/'+file.removeprefix('minecraft:')+'.ogg';h=index[key]['hash'];p=cache/h[:2]/h
   assert p.is_file() and hashlib.sha1(p.read_bytes()).hexdigest()==h,key
   actual.append({'asset':key,'sha1':h,'bytes':p.stat().st_size})
  for lang in ['ru_ru','en_us']:assert event['subtitle'] in read(A/f'lang/{lang}.json')
  routes[k]={'vanilla_event':source,'files':actual}
 assert len(routes)==17
 build=read(R/'build/current-resources.json');jar=Path(build['jar'])
 with zipfile.ZipFile(jar) as z:
  assert not any('Boss331Capture' in n or n.startswith('org/slavicmyths/verify/') for n in z.namelist())
  assert z.read('assets/slavicmyths/lost_halls_rigs/posten_trophy.json')==(A/'lost_halls_rigs/posten_trophy.json').read_bytes()
 result={'rig_uv_islands':rigs,'audio_routes':routes,'audio_source':'Minecraft vanilla aliases; no new synthetic or original voice recordings','jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'result':'PASS static/native-resource scope only'}
 (O/'static-gates.json').write_text(json.dumps(result,indent=2)+'\n');print('PASS: UV, 17 vanilla event to actual cached OGG routes, old audio preserved, production test isolation')
if __name__=='__main__':main()

