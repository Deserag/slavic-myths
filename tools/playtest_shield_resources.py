from pathlib import Path
import json, math
A=Path('src/main/resources/assets/slavicmyths/models/item')
# Silhouette: 14-unit stepped round disc, thin planks, metal rim/boss, rear grip.
def box(lo,hi,uv):
 return {'from':lo,'to':hi,'faces':{f:{'texture':'#0','uv':uv} for f in ['north','south','east','west','up','down']}}
e=[]
for row in range(14):
 y=1+row; w=2*math.floor(math.sqrt(49-(row-6.5)**2))
 e.append(box([8-w/2,y,7],[8+w/2,y+1,8],[8,0,12,4]))
 if row not in [0,13]:
  e.append(box([9-w/2,y,6.85],[7+w/2,y+1,7],[0,(13-row)/14*4,4,(14-row)/14*4]))
  e.append(box([9-w/2,y,8],[7+w/2,y+1,8.15],[4,(13-row)/14*4,8,(14-row)/14*4]))
e.append(box([6,6,6.2],[10,10,6.85],[12,0,16,4]))
e.append(box([7,5.5,8.15],[9,10.5,9.1],[4,0,8,4]))
for x,y in [(8,2),(2,8),(14,8),(8,14)]:e.append(box([x-.3,y-.3,6.6],[x+.3,y+.3,6.85],[12,0,16,4]))
m={'credit':'Slavic Myths RC1 round shield; normalized atlas UV','textures':{'0':'slavicmyths:item/retainer_shield','particle':'slavicmyths:item/retainer_shield'},'elements':e,'gui_light':'front','display':{}}
for hand in ['righthand','lefthand']:
 m['display']['thirdperson_'+hand]={'rotation':[0,-90,0],'translation':[2,3,2],'scale':[1,1,1]}
 m['display']['firstperson_'+hand]={'rotation':[0,0,5],'translation':[4,1,-2],'scale':[.9,.9,.9]}
m['display'].update(gui={'rotation':[0,180,0],'translation':[0,0,0],'scale':[1,1,1]},ground={'translation':[0,3,0],'scale':[.5,.5,.5]},fixed={'rotation':[0,180,0],'scale':[1,1,1]})
m['overrides']=[{'predicate':{'blocking':1},'model':'slavicmyths:item/retainer_shield_blocking'}]
(A/'retainer_shield.json').write_text(json.dumps(m,indent=2)+'\n')
(A/'retainer_shield_body.json').write_text(json.dumps({k:m[k] for k in ['textures','elements','gui_light']},indent=2)+'\n')
b={'parent':'slavicmyths:item/retainer_shield_body','display':{}}
for hand in ['righthand','lefthand']:
 b['display']['firstperson_'+hand]={'rotation':[0,0,-5],'translation':[0,3,0],'scale':[.9,.9,.9]}
 b['display']['thirdperson_'+hand]={'rotation':[45,-25,0],'translation':[1,4,2],'scale':[1,1,1]}
(A/'retainer_shield_blocking.json').write_text(json.dumps(b,indent=2)+'\n')
