import json
from pathlib import Path
P=Path('src/main/resources/assets/slavicmyths/models/item')
# Replace flat atlas swatches with existing detailed material textures, retaining geometry and UV scale.
materials={(0,0):'burial_amber',(1,0):'burial_silver',(2,0):'burial_iron',(3,0):'burial_wood',(0,1):'burial_edge',(1,1):'burial_leather',(2,1):'sisters/tryaseya_glow',(3,1):'sisters/gneteya_glow',(0,2):'burial_bone',(1,2):'burial_red',(2,2):'sisters/ledeya_metal',(3,2):'burial_iron'}
for name in ['polkan_axe','polkan_bow','skarapeya_blade','skarapeya_blade_split','skarapeya_whip','skarapeya_gusli','skarapeya_needle']:
 p=P/(name+'.json');d=json.loads(p.read_text());
 for e in d.get('elements',[]):
  for face in e['faces'].values():
   uv=face.get('uv',[0,0,4,4]);tile=(int(uv[0]//4),int(uv[1]//4));key='material_'+str(tile[0])+'_'+str(tile[1]);tex=materials.get(tile,'burial_iron');d['textures'][key]='slavicmyths:item/'+tex;face['texture']='#'+key;face['uv']=[.25,.25,15.75,15.75]
 p.write_text(json.dumps(d,indent=2)+'\n')
# Symmetric spear point: each tier tapers in both X and Z about the same axis.
p=P/'polkan_spear.json';d=json.loads(p.read_text());d['elements']=[e for e in d['elements'] if e['from'][1]<20]
for y,w in [(20,4.8),(22,4.1),(24,3.4),(26,2.6),(28,1.8),(30,.8)]:
 d['elements'].append({'from':[8-w/2,y,8-w/2],'to':[8+w/2,y+2,8+w/2],'faces':{f:{'texture':'#edge'} for f in ['north','south','east','west','up','down']}})
p.write_text(json.dumps(d,indent=2)+'\n')
for p in P.glob('*.json'):
 if p.stem not in ['polkan_spear','polkan_axe','polkan_bow','skarapeya_blade','skarapeya_blade_split','skarapeya_whip','skarapeya_gusli','skarapeya_needle']:continue
 d=json.loads(p.read_text());assert all(-16<=v<=32 for e in d.get('elements',[]) for a in ['from','to'] for v in e[a]),p
 for tex in d['textures'].values():
  if tex.startswith('slavicmyths:'):assert (P.parents[1]/('textures/'+tex.split(':')[1]+'.png')).exists(),tex
 print('PASS',p.stem)
