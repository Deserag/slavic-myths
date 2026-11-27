import json
from pathlib import Path
p=Path('src/main/resources/assets/slavicmyths/models/item/polkan_spear.json')
d=json.loads(p.read_text());e=[]
def b(a,z,t,rot=None):
 x={'from':a,'to':z,'faces':{f:{'texture':'#'+t} for f in ('north','south','east','west','up','down')}}
 if rot:x['rotation']={'origin':rot[0],'axis':'z','angle':rot[1]}
 e.append(x)
b([7.35,-12,7.35],[8.65,17,8.65],'wood');b([7.1,0,7.1],[8.9,7,8.9],'leather')
for y in (-11,0,7,12,16):b([7,y,7],[9,y+.65,9],'gold')
b([6.8,15,6.8],[9.2,20,9.2],'metal')
for y,w in [(20,3),(22,4.4),(24,4.8),(26,4),(28,3.2),(30,2.2),(30.5,.8)]:b([8-w/2,y,7.6],[8+w/2,min(32,y+2),8.4],'edge')
b([7.7,20,7.3],[8.3,32,8.7],'metal')
for side in (-1,1):
 x=8+side*3
 b([min(8,x),18,7.2],[max(8,x),19,8.8],'gold')
 b([x-.45,18,7.4],[x+.45,22,8.6],'edge',([x,18,8],-22.5*side))
b([6.8,17.4,6.65],[9.2,19.5,7.15],'gold');b([7.4,18,6.4],[8.6,19.2,6.7],'gem')
b([7,-13,7],[9,-11,9],'metal')
d['textures']['gem']='slavicmyths:item/burial_red';d['elements']=e;p.write_text(json.dumps(d,indent=2)+'\n')

