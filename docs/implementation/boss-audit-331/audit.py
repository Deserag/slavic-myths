"""Read-only source/JAR/native-export audit; writes only this report directory."""
from pathlib import Path
import json,hashlib,subprocess,zipfile,io
from PIL import Image,ImageChops
R=Path(__file__).resolve().parents[3];O=Path(__file__).resolve().parent
P=R/'docs/implementation/p0-bosses-327'
sha=lambda b:hashlib.sha256(b).hexdigest()
def git(path,rev):return subprocess.check_output(['git','show',rev+':'+path],cwd=R)
data={};before=json.loads((P/'native-before/models.json').read_text());after=json.loads((P/'native-after/models.json').read_text())
for name in ['grove_guardian','zharenitsa','magma_lord']:
 a=before[name]['frames'][0];b=after[name]['frames'][0]
 ta=P/f'native-before/{name}.png';tb=P/f'native-after/{name}.png'
 ia=Image.open(ta).convert('RGBA');ib=Image.open(tb).convert('RGBA')
 vertices=lambda f:[v for face in f['faces'] for v in face]
 def bounds(f):
  v=vertices(f);return [min(p[i] for p in v) for i in range(3)]+[max(p[i] for p in v) for i in range(3)]
 item={'before_faces':len(a['faces']),'after_faces':len(b['faces']),'before_idle_bounds_model_units':bounds(a),'after_idle_bounds_model_units':bounds(b),'texture_dimensions':[ia.size,ib.size],'before_png_sha256':sha(ta.read_bytes()),'after_png_sha256':sha(tb.read_bytes()),'after_matches_current_png':tb.read_bytes()==(R/'src/main/resources/assets/slavicmyths'/after[name]['texture']).read_bytes(),'changed_pixels':sum(x!=y for x,y in zip(ia.getdata(),ib.getdata())) if ia.size==ib.size else 'size changed','same_idle_geometry_uv_payload':a['faces']==b['faces']}
 if name!='grove_guardian':
  path='src/main/resources/assets/slavicmyths/'+('cinder_rigs/zharenitsa.json' if name=='zharenitsa' else 'burning_islands_rigs/magma_lord.json')
  old=json.loads(git(path,'c0dafcac^'));new=json.loads((R/path).read_text());index=lambda d:{p['name']:p for p in d['parts']};x,y=index(old),index(new)
  item.update(added_parts=sorted(y.keys()-x.keys()),removed_parts=sorted(x.keys()-y.keys()),modified_parts={n:{'before':x[n],'after':y[n]} for n in x.keys()&y.keys() if x[n]!=y[n]},before_parts=len(x),after_parts=len(y))
 data[name]=item
(O/'model-differences.json').write_text(json.dumps(data,indent=2)+'\n')
instance=Path('C:/Users/pavel/AppData/Roaming/PolyMC/instances/Slavic-Myths-2.0.0-HELL')
jar=next((instance/'.minecraft/mods').glob('slavicmyths-*.jar'))
heads={};A='assets/slavicmyths/'
with zipfile.ZipFile(jar) as z:
 for name,texture in [('grove_guardian','entity/grove_guardian'),('zharenitsa','entity/lost_halls/zharenitsa_trophy'),('posten','entity/lost_halls/posten'),('magma_lord','entity/burning_islands/magma_lord')]:
  id=name+'_head';paths=[A+'models/item/'+id+'.json',A+'blockstates/'+id+'.json',A+'models/block/'+id+'.json',A+'textures/'+texture+'.png',A+'textures/'+texture+'_glow.png','data/slavicmyths/loot_table/blocks/'+id+'.json']
  heads[id]={'packaged':{p:p in z.namelist() for p in paths},'item_model':json.loads(z.read(paths[0])),'base_dimensions':Image.open(io.BytesIO(z.read(paths[3]))).size,'base_sha':sha(z.read(paths[3])),'glow_dimensions':Image.open(io.BytesIO(z.read(paths[4]))).size}
 report={'installed':str(jar),'sha256':sha(jar.read_bytes()),'metadata':z.read('META-INF/neoforge.mods.toml').decode(),'heads':heads,'game_launches_by_auditor':0}
(O/'jar-head-paths.json').write_text(json.dumps(report,indent=2)+'\n')
log=(instance/'.minecraft/logs/latest.log').read_text(encoding='utf-8',errors='replace')
lines=[line for line in log.splitlines() if any(k in line for k in ['slavicmyths-2.0.9','Slavic Myths 2.0.9','Starting integrated','Stopping server','trophy','zharenitsa','Failed to render','BlockEntity'])]
(O/'owner-log-excerpts.txt').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print(json.dumps({k:{n:v for n,v in x.items() if n not in ['modified_parts']} for k,x in data.items()},indent=2))
print('Installed JAR head paths verified; auditor gameplay launches=0')
