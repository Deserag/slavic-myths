"""Ordinary mob material/anatomy changes only. Boss resources are excluded."""
def cinder_parts(name,parts):
 def box(n,parent,p,size,mat='coal',off=None):
  d=dict(name=n,parent=parent,pivot=p,size=size,material=mat)
  if off:d['offset']=off
  parts.append(d)
 if name=='coal_crawler':
  # Four legs remain until the unavailable original confirms another anatomy.
  parts[:]=[p for p in parts if not p['name'].startswith('leg')]
  for i,(side,z) in enumerate([(-1,-8),(1,-8),(-1,9),(1,9)]):
   box('leg'+str(i),'body',[side*9,1,z],[3,3,3]);box('upper'+str(i),'leg'+str(i),[0,0,0],[8,4,4],off=[0 if side>0 else -8,-2,-2])
   box('knee'+str(i),'upper'+str(i),[side*8,0,0],[5,5,5]);box('shin'+str(i),'knee'+str(i),[0,0,0],[3,11,3],off=[-1.5,0,-1.5]);box('foot'+str(i),'shin'+str(i),[0,11,0],[5,2,5])
  for i in range(3):box('coal_plate'+str(i),'body',[0,-6,-7+i*8],[17-i*2,4,7])
 if name=='pekelnik':
  for p in parts:
   if p['name'].startswith('leg'):p['size'][1]=10;p['offset']=[-4,-9,-4.5]
  for i,side in enumerate([-1,1]):box('furnace_shoulder'+str(i),'body',[side*10,-8,0],[7,7,10]);box('shin'+str(i),'leg'+str(i),[0,6,0],[7,10,8])
  for i in range(3):box('brick_belt'+str(i),'body',[0,-6+i*6,-6],[17,3,3]);box('vent'+str(i),'body',[-5+i*5,-15,3],[3,8,4],'ash')
 if name=='sparkling':box('gold_core','face',[0,0,-.2],[4,4,6],'ember')

def halls_parts(name,parts):
 def box(n,parent,p,size,mat='stone'):
  parts.append(dict(name=n,parent=parent,pivot=p,size=size,material=mat,offset=[-v/2 for v in size],rotation=[0,0,0]))
 if name=='sheptun':
  for i in range(4):box('variant_hood'+str(i),'head',[-3+i*2,-7,0],[3,5+i%2*3,4],'cloth')
 if name in ['ekhovik','griboshmyg']:
  for p in list(parts):
   if p['name'].startswith('leg'):
    x,y,z=p['size'];p['size']=[x,max(2,y//2),z];p['offset']=[-x/2,-y/2,-z/2]
    box('knee'+p['name'][3:],p['name'],[0,0,0],[x,max(2,y//2),z])
 if name=='gulokryl':
  for p in parts:
   if p['name'].startswith('wing'):p['material']='cloth'
 if name=='oskolnik':
  for p in parts:
   if p['name'].startswith('leg'):p['size'][1]=10;p['offset']=[-4.5,-9,-4.5]
  for i in range(2):box('knee'+str(i),'leg'+str(i),[0,6,0],[8,9,8])

def islands_parts(name,parts):
 if name=='magma_lord':return
 if name=='slag_crab':
  for p in parts:
   if p['name'].startswith('claw'):p['size']=[9,7,10];p['offset']=[-4.5,-3.5,-5]
 if name=='vent_golem':
  for p in parts:
   if p['name'].startswith('leg'):p['size'][1]=10;p['offset']=[-5,-8.5,-6]
  for i in range(2):parts.append(dict(name='knee'+str(i),parent='leg'+str(i),pivot=[0,5,0],size=[9,7,10],offset=[-4.5,-3.5,-5],hot=False))

def palette(name,part,default):
 n=part if isinstance(part,str) else part['name']
 if name=='heat_salamander':return (132,42,51) if n.startswith('ridge') else (80,30,48) if 'tail' in n else (60,22,39)
 if name=='slag_crab':return (122,93,55) if n.startswith(('claw','pincer')) else (62,81,64) if n in ['body','carapace'] else (82,65,46)
 if name=='vent_golem':return (99,111,125) if n.startswith(('body','head','fist')) else (54,68,84)
 if name=='coal_crawler':return (18,22,27) if not n.startswith('knee') else (46,37,34)
 if name=='pekelnik':return (121,55,37) if n.startswith(('body','furnace','brick')) else (68,42,36)
 if name=='sparkling':return (167,102,23)
 if name=='ash_spirit':return (119,132,148) if n.startswith('cloud') else (66,75,98)
 if name=='oskolnik':return (80,100,143) if n.startswith('crystal') else (50,68,96)
 if name=='ekhovik':return (49,129,128) if n.startswith('crystal') else (73,91,82)
 if name=='gulokryl':return (81,49,94) if 'wing' in n else (39,34,54)
 if name=='griboshmyg':return (176,74,44) if n=='cap' else (151,128,86) if n.startswith('sidefungus') else (76,85,49)
 if name=='navka':return (74,82,114) if n.startswith(('hair','rag')) else default
 if name=='rootling':return (76,88,72) if n.startswith(('body','bark','arm')) else default
 if name=='bone_crawler':return (201,188,153) if n.startswith(('upper','shin','head','abdomen','spine','body')) else default
 if name=='navya_wisp':return (102,145,211) if n=='body' else default
 return default

def pattern(name,part,x,y):
 if name=='pekelnik':return -25 if y%5==0 or (x+(y//5%2)*3)%9==0 else 0
 if name=='heat_salamander':return 16 if (x//3+y//2)%4==0 else -3
 if name=='slag_crab':return -18 if (x*3+y*5)%19<3 else 0
 if name=='vent_golem':return 13 if x%8==0 or y%9==0 else -4
 if name=='gulokryl':return 18 if x%7==y%7 else -5
 if name=='griboshmyg' and part=='cap':return 30 if (x//3+y//2)%5==0 else 0
 return 0
