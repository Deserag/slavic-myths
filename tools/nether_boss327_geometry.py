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
 def box(n,parent,p,size,material='coal',rotation=None):
  data=dict(name=n,parent=parent,pivot=p,size=size,material=material)
  if rotation:data['rotation']=rotation
  parts.append(data)
 for p in parts:
  n=p['name']
  if n.startswith('arm'):
   side=-1 if n.endswith('0') else 1;p['pivot']=[side*10,-24,0];p['size']=[6,12,6];p['offset']=[-3,0,-3];p['rotation']=[-.15,0,side*.24]
  if n.startswith('hand'):p['parent']='forearm'+n[-1];p['pivot']=[0,10,0];p['size']=[4,6,3];p['material']='coal'
  if n.startswith('crown'):p['material']='ash' if int(n[5:])%3 else 'coal';p['rotation']=[.12,0,(int(n[5:])%2-.5)*.24]
  if n.startswith('hair'):p['size']=[3,p['size'][1],4];p['rotation']=[.12,0,(int(n[4:])-2)*.13]
 for i,s in enumerate([-1,1]):
  box('forearm'+str(i),'arm'+str(i),[0,10,0],[5,12,5],rotation=[-.35,0,-s*.12]);box('shoulder'+str(i),'body',[s*7,-5,0],[7,6,7],rotation=[0,0,s*.3]);box('cheek'+str(i),'head',[s*4,2,-4],[3,5,3],rotation=[0,0,-s*.25]);box('brow'+str(i),'head',[s*2,-2,-5],[4,1,2],'ash',rotation=[0,0,-s*.17])
  for j in range(3):box('finger'+str(i)+str(j),'hand'+str(i),[(j-1)*1.5,4,-1],[1,5+j%2,1],'ember')
 box('nose','head',[0,0,-5],[1,3,2],'ash');box('lower_lip','head',[0,3,-5],[3,1,1],'ash')
 for i in range(4):box('ash_tress'+str(i),'head',[(i-1.5)*3,12,5],[4,8,4],'ash',rotation=[-.18,0,(i-1.5)*.15])
 parts[:]=ordered(parts)

def islands(name,parts):
 if name!='magma_lord':return
 def box(n,parent,p,size,hot=False,rotation=None):
  data=dict(name=n,parent=parent,pivot=p,size=size,offset=[-v/2 for v in size],hot=hot)
  if rotation:data['rotation']=rotation
  parts.append(data)
 for p in parts:
  n=p['name']
  if n=='body':p['size']=[13,25,10];p['offset']=[-6.5,-12.5,-5]
  if n.startswith('arm') and len(n)==4:p['size']=[8,13,8];p['offset']=[-4,-4,-4];p['rotation']=[.12,0,(-1 if n.endswith('0') else 1)*.2]
  if n.startswith('claw') and len(n)==5:p['parent']='forearm'+n[-1];p['pivot']=[0,9,-2]
  if n.startswith('leg') and len(n)==4:p['size']=[8,13,9];p['offset']=[-4,-6.5,-4.5];p['pivot'][1]=21
  if n=='head':p['size']=[12,12,11];p['offset']=[-6,-6,-5.5]
 for i,s in enumerate([-1,1]):
  box('forearm'+str(i),'arm'+str(i),[0,11,0],[10,14,9],rotation=[-.3,0,-s*.2]);box('elbow_hot'+str(i),'forearm'+str(i),[0,-5,-5],[4,3,2],True)
  box('shin'+str(i),'leg'+str(i),[s*1,10,-1],[7,13,8],rotation=[.15,0,-s*.12]);box('foot'+str(i),'shin'+str(i),[s*1,7,-3],[12,5,15])
  for j in range(3):box('shoulder_rock'+str(i)+str(j),'body',[s*(12+j*3),-12+j*3,j*2],[10-j*2,7,9],rotation=[j*.1,0,-s*(.15+j*.2)])
  box('brow'+str(i),'head',[s*4,-3,-6],[7,3,3],rotation=[0,0,-s*.25]);box('cheek'+str(i),'head',[s*6,2,-3],[4,7,6],rotation=[0,0,s*.2]);box('jaw_tusk'+str(i),'jaw',[s*5,-2,-5],[2,5,2])
  for j in range(3):box('rib'+str(i)+str(j),'body',[s*7,-7+j*7,0],[6,5,13],rotation=[0,0,s*(.15-j*.1)])
 for j in range(4):box('back_crest'+str(j),'body',[0,-15+j*9,8],[5,6,7],rotation=[.3,0,0])
 parts[:]=ordered(parts)
