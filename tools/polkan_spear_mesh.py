import json,math
from pathlib import Path
P=Path('src/main/resources/assets/slavicmyths/models/item');D=P/'steppe_mesh';D.mkdir(exist_ok=True)
d=json.loads((P/'polkan_spear.json').read_text());lines=['# Faceted symmetric royal spear; authored in item model units.','mtllib polkan_spear.mtl'];idx=0
# Flat face normals are intentional: metal facets catch light individually.
def face(points,material):
 global idx
 lines.append('usemtl '+material)
 for x,y,z in points:lines.append(f'v {x/16:.6f} {y/16:.6f} {z/16:.6f}')
 for u,v in [(0,0),(1,0),(1,1),(0,1)][:len(points)]:lines.append(f'vt {u} {v}')
 lines.append('f '+' '.join(f'{idx+i+1}/{idx+i+1}' for i in range(len(points))));idx+=len(points)
for e in d['elements']:
 if e['from'][1]>=20:continue
 a,b=e['from'],e['to'];x,y,z=a;X,Y,Z=b
 pts=[(x,y,z),(X,y,z),(X,Y,z),(x,Y,z),(x,y,Z),(X,y,Z),(X,Y,Z),(x,Y,Z)]
 if 'rotation' in e:
  r=e['rotation'];ox,oy,oz=r['origin'];angle=math.radians(r['angle']);pts=[(ox+(px-ox)*math.cos(angle)-(py-oy)*math.sin(angle),oy+(px-ox)*math.sin(angle)+(py-oy)*math.cos(angle),pz) for px,py,pz in pts]
 for f in [(0,3,2,1),(4,5,6,7),(0,4,7,3),(1,2,6,5),(3,7,6,2),(0,1,5,4)]:face([pts[i] for i in f],next(iter(e['faces'].values()))['texture'][1:])
# Diamond cross-section: broad lower leaf, narrow neck, single exact apex.
rings=[(19.8,1.1,.65),(22.5,2.5,.8),(26,1.65,.55),(29, .85,.3)]
verts=[[(8-w,y,8),(8,y,8-t),(8+w,y,8),(8,y,8+t)] for y,w,t in rings]
for lo,hi in zip(verts,verts[1:]):
 for i in range(4):face([lo[i],lo[(i+1)%4],hi[(i+1)%4],hi[i]],'edge')
for i in range(4):face([verts[-1][i],verts[-1][(i+1)%4],(8,32,8)],'edge')
(D/'polkan_spear.obj').write_text('\n'.join(lines)+'\n')
(D/'polkan_spear.mtl').write_text(''.join(f'newmtl {k}\nKa 0.1 0.1 0.1\nKd 1 1 1\nKs 0 0 0\nillum 1\nmap_Kd #{k}\n\n' for k in d['textures'] if k!='particle'))
model={'loader':'neoforge:obj','model':'slavicmyths:models/item/steppe_mesh/polkan_spear.obj','mtl_override':'slavicmyths:models/item/steppe_mesh/polkan_spear.mtl','textures':d['textures'],'automatic_culling':False,'shade_quads':True,'flip_v':False,'display':d['display'],'gui_light':'front'}
(P/'polkan_spear.json').write_text(json.dumps(model,indent=2)+'\n');print('PASS: symmetric diamond point,',idx,'vertices; OBJ/MTL emitted')
