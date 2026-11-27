import json
from pathlib import Path
P=Path('src/main/resources/assets/slavicmyths/models/item')
def box(a,b,t):
 return {'from':a,'to':b,'faces':{f:{'texture':'#'+t} for f in ('north','south','east','west','up','down')}}
for name,royal in [('steppe_spear',False),('polkan_spear',True)]:
 old=json.loads((P/(name+'.json')).read_text(encoding='utf-8-sig'))
 textures={'wood':'slavicmyths:item/burial_wood','metal':'slavicmyths:item/burial_iron','edge':'slavicmyths:item/burial_silver','leather':'slavicmyths:item/burial_leather','gold':'slavicmyths:item/burial_amber','particle':'slavicmyths:item/burial_wood'}
 e=[box([7.45,-12,7.45],[8.55,19,8.55],'wood'),box([7.3,1,7.3],[8.7,7,8.7],'leather'),box([7.2,17,7.2],[8.8,21,8.8],'metal'),box([7.1,-12.5,7.1],[8.9,-11,8.9],'metal')]
 # Tapered leaf blade made of shallow overlapping facets; a narrow spine catches light.
 for y,w in [(20,1.1),(22,1.8),(24,2.2),(26,1.7),(28,1.1),(30,.5)]:
  e.append(box([8-w/2,y,7.7],[8+w/2,y+2,8.3],'edge'))
 e.append(box([7.82,20,7.5],[8.18,30,8.5],'metal'))
 if royal:
  for y in (0,7,16,19):e.append(box([7.15,y,7.15],[8.85,y+.45,8.85],'gold'))
  e.extend([box([5.8,19,7.7],[7.3,20,8.3],'gold'),box([8.7,19,7.7],[10.2,20,8.3],'gold')])
 d={'gui_light':'front','textures':textures,'elements':e,'display':old['display']}
 (P/(name+'.json')).write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8')

