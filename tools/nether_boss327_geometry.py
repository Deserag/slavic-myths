"""Objective geometry repairs from written boss locks, not an original-PNG match.
Called by the existing reproducible asset generators before UV packing.
"""
def ordered(parts):
 remaining=list(parts);result=[];known={'root'}
 while remaining:
  ready=[p for p in remaining if p['parent'] in known]
  if not ready:raise ValueError('Missing/cyclic boss ModelPart parent')
  for p in ready:result.append(p);known.add(p['name']);remaining.remove(p)
 return result
def cinder(name,parts):
 if name!='zharenitsa':return
 parts.clear()
 def box(n,parent,p,size,mat='coal',rot=None,off=None):
  d=dict(name=n,parent=parent,pivot=p,size=size,material=mat)
  if rot:d['rotation']=rot
  if off:d['offset']=off
  parts.append(d)
 box('body','root',[0,-18,0],[15,15,10]);box('waist','root',[0,-6,0],[9,10,7]);box('hips','root',[0,1,0],[16,7,10])
 box('collar','body',[0,-9,0],[13,4,10]);box('heart','body',[0,-1,-5],[2,6,1],'ember')
 for i,side in enumerate([-1,1]):
  box('bust'+str(i),'body',[side*4,-2,-4],[6,6,5],rot=[-.12,0,side*.12])
  box('shoulder'+str(i),'body',[side*8,-5,0],[6,7,7],rot=[0,0,side*.2])
  box('arm'+str(i),'root',[side*11,-22,0],[5,12,5],rot=[-.12,0,-side*.16],off=[-2.5,0,-2.5])
  box('forearm'+str(i),'arm'+str(i),[0,12,0],[4,13,4],rot=[-.35,0,-side*.15],off=[-2,0,-2])
  box('hand'+str(i),'forearm'+str(i),[0,12,0],[4,5,3],off=[-2,0,-1.5])
  for j in range(3):box('finger'+str(i)+str(j),'hand'+str(i),[(j-1)*1.3,4,-.4],[1,4+j%2,1], 'coal',rot=[-.12,0,0])
 # Overlapping, flared skirt plates create a continuous floating dress, not spokes.
 for layer,(y,width,depth,height) in enumerate([(5,16,11,11),(13,22,14,12),(23,28,18,10)]):
  box('gown'+str(layer),'root',[0,y,1],[width,height,depth],rot=[0,(layer%2-.5)*.1,0])
  for i,side in enumerate([-1,1]):box('fold'+str(layer)+str(i),'gown'+str(layer),[side*(width/2-2),1,-depth/2],[4,height+2,3],rot=[0,0,side*.18])
 for i in range(6):
  side=-1 if i<3 else 1;z=(i%3-1)*5
  box('ribbon'+str(i),'root',[side*10,10,z],[4,23-i%3*2,3],rot=[z*.013,0,-side*.18],off=[-2,0,-1.5]);box('tip'+str(i),'ribbon'+str(i),[0,20-i%3*2,0],[2,2,2],'ember')
 box('head','root',[0,-34,0],[9,12,8]);box('jaw','head',[0,4,-1],[7,3,6]);box('nose','head',[0,0,-4],[2,4,2]);box('lower_lip','jaw',[0,0,-3],[3,1,1],'ash')
 for i,side in enumerate([-1,1]):
  box('cheek'+str(i),'head',[side*4,1,-3],[3,5,3],rot=[0,0,-side*.15]);box('brow'+str(i),'head',[side*2,-2,-4],[4,1,2],rot=[0,0,-side*.18]);box('eye'+str(side*2),'head',[side*2,-1,-4.5],[2,1,1],'ember')
  box('crown_side'+str(i),'head',[side*5,-6,1],[3,10,4],rot=[0,0,side*.35]);box('crown_tip'+str(i),'crown_side'+str(i),[0,-6,0],[2,7+(i*3),2],rot=[0,0,side*.2])
 box('crown_front','head',[0,-6,-3],[8,3,3]);box('crown_gem','head',[0,-8,-4],[2,3,1],'ember');box('crown_spire','head',[0,-13,1],[3,11,3],rot=[.12,0,0])
 box('hair_back','head',[0,6,5],[10,18,5],'ash',rot=[.12,0,0])
 for i in range(5):
  side=(i-2);box('hair'+str(i),'head',[side*2.5,4,4],[3,19+(i%2)*6,4],'ash',rot=[.1,0,side*.12])
 for i,side in enumerate([-1,1]):box('ash_tress'+str(i),'head',[side*6,1,1],[4,24,5],'coal',rot=[.08,0,side*.18])
 parts[:]=ordered(parts)

def islands(name,parts):
 if name!='magma_lord':return
 parts.clear()
 def box(n,parent,p,size,hot=False,rot=None):
  d=dict(name=n,parent=parent,pivot=p,size=size,offset=[-v/2 for v in size],hot=hot)
  if rot:d['rotation']=rot
  parts.append(d)
 box('body','root',[0,-20,0],[7,29,7]);box('core','body',[0,-3,-2],[6,9,6],True,rot=[0,0,.65]);box('core_inner','core',[0,0,0],[4,12,4],True,rot=[0,.6,-.65])
 box('pelvis','body',[0,16,0],[12,6,9],rot=[0,0,.12])
 for i,side in enumerate([-1,1]):
  box('shell'+str(i),'body',[side*7,-3,-4],[5,17,5],rot=[0,0,-side*.16])
  for j in range(3):box('rib'+str(i)+str(j),'body',[side*(6+j%2),-11+j*8,1],[6,4,10],rot=[0,0,side*(.18-j*.18)])
  box('shoulder_rock'+str(i),'body',[side*13,-16,1],[10,8,11],rot=[.15,0,-side*.45])
  box('shoulder_spire'+str(i),'shoulder_rock'+str(i),[side*2,-6,1],[4,15+(i*3),5],rot=[0,0,side*.35])
  box('arm'+str(i),'body',[side*17,-8,1],[6,17,6],rot=[.1,0,side*.2])
  box('forearm'+str(i),'arm'+str(i),[0,17,-1],[8,14,8],rot=[-.25,0,-side*.15])
  box('claw'+str(i),'forearm'+str(i),[0,12,-2],[7,8,6])
  for j in range(3):box('finger'+str(i)+'_'+str(j),'claw'+str(i),[(j-1)*3,7,-1],[2,10-j,2],rot=[-.15,0,(j-1)*.15])
  box('backarm'+str(i),'body',[side*10,-12,8],[4,24,4],rot=[.4,0,side*.9]);box('backtalon'+str(i),'backarm'+str(i),[side*2,17,-2],[3,12,3],rot=[.4,0,0])
  # Detached tapered lower segments; no humanoid boots or continuous legs.
  box('leg'+str(i),'body',[side*6,27,1],[6,14,7],rot=[.2,0,-side*.15]);box('shin'+str(i),'leg'+str(i),[side*2,14,0],[4,12,5],rot=[-.1,0,side*.32])
  box('shard'+str(i),'body',[side*15,18,2],[4,12,5],rot=[0,0,side*.4]);box('shard_back'+str(i),'body',[side*9,-20,11],[4,14,4],rot=[.3,0,side*.3])
 box('head','body',[0,-25,-1],[11,13,9]);box('jaw','head',[0,6,-1],[10,4,8]);box('mouth','jaw',[0,0,-4.3],[7,1,1],True)
 for i,side in enumerate([-1,1]):
  box('brow'+str(i),'head',[side*3,-2,-4],[6,3,3],rot=[0,0,-side*.27]);box('eye'+str(i),'head',[side*3,0,-4.6],[3,1,1],True)
  box('cheek'+str(i),'head',[side*5,2,-1],[3,7,6],rot=[0,0,side*.18]);box('horn'+str(i),'head',[side*6,-11,1],[3,15+(i*4),4],rot=[.2,0,side*.3]);box('horn_back'+str(i),'head',[side*4,-8,5],[3,10,3],rot=[.35,0,0]);box('shard_head'+str(i),'head',[side*10,1,0],[3,5,4])
 box('crown','head',[0,-10,2],[3,7,3]);box('back_crest','body',[0,-16,7],[4,16,6],rot=[.5,0,0])
 parts[:]=ordered(parts)
