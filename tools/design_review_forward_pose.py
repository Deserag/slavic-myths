"""Second review: four heroes, elbow bend and forward weapon alignment.
Only creates offline previews. Production models and animations are untouched.
"""
from pathlib import Path
import json,math,copy
import numpy as np
from PIL import Image
import design_review_180 as base

R=base.R;O=R/'docs/design-review/1.8.0-proposal/revision-2';O.mkdir(parents=True,exist_ok=True)
FILES={'ilya':'work/bogatyr/meshes.json','alyosha':'work/skim/meshes.json','dobrynya':'work/marinka/meshes.json','finist':'work/finist/meshes.json'}
TEXTURES={'ilya':'ilya_muromets.png','alyosha':'alyosha_popovich.png','dobrynya':'dobrynya_nikitich.png','finist':'finist.png'}
def transform(cube,pivot,axis,degrees):
 out=copy.deepcopy(cube);a=math.radians(degrees);c,s=math.cos(a),math.sin(a)
 m=np.array([[1,0,0],[0,c,-s],[0,s,c]]) if axis=='x' else np.array([[c,-s,0],[s,c,0],[0,0,1]])
 for face in out['faces']:
  for v in face:v[:3]=(np.array(pivot)+m@(np.array(v[:3])-pivot)).tolist()
 return out
def pose(who):
 old=json.loads((R/FILES[who]).read_text())[who+'_IDLE'];hands=[c for c in old if c['part'].endswith('/right_hand')]
 pts=np.array([v[:3] for c in hands for f in c['faces'] for v in f]);low,high=pts.min(0),pts.max(0)
 x,z=((low+high)/2)[[0,2]];y=high[1]-(1.8 if who=='ilya' else 1.2 if who in ('alyosha','dobrynya') else 1.5)
 elbow=[x,low[1],z];palm=[x,y,z]
 mesh=copy.deepcopy(old)
 if who in ('ilya','alyosha'):
  mesh=[c for c in mesh if not c['part'].endswith(('/mace','/saber'))]
  if who=='ilya':
   new=[base.cube([x,y+4,z],[1.6,12,1.6],'#785238'),base.cube([x,y+9.8,z],[6.8,4.4,6.8],'#8D9693'),base.cube([x,y+7.1,z],[5,1.2,5],'#A9B0AB'),base.cube([x,y+12.6,z],[5,1.2,5],'#747D7B'),base.cube([x,y-2,z],[2,1,2],'#A29469')]
  else:
   new=[base.cube([x,y,z],[1.4,3.5,1.4],'#785238'),base.cube([x,y+2.15,z],[3.6,.8,1.6],'#B69B59'),base.cube([x,y-2.1,z],[1.7,.7,1.7],'#B69B59')]
   for i in range(7):new.append(base.cube([x+.025*i*i,y+3.1+i*1.5,z],[1.4 if i<5 else .9 if i==5 else .35,1.55,.6],'#B7C0BA'))
  mesh+=new
 elif who=='dobrynya':
  # The spear's tip originally points UP, unlike the other three weapons.
  # Neutralize its -12-degree idle tilt, then reverse it at the grip so the
  # iron tip, rather than the wooden butt, points forward after the elbow bend.
  shaft=next(c for c in mesh if c['part'].endswith('/spear'))
  center=np.array([v[:3] for f in shaft['faces'] for v in f]).mean(0)
  a=math.radians(-12);pivot=center+np.array([0,math.cos(a)*4.64,math.sin(a)*4.64])
  mesh=[transform(transform(c,pivot,'x',12),palm,'x',180) if c['part'].endswith('/spear') else c for c in mesh]
 elif who=='finist':
  # Current FinistModel applies z(weapon,58) even in IDLE. Undo that twist.
  # Pivot from native hand joint: elbow + local weapon offset(0,6,-1).
  pivot=[x,low[1]+6,z-1]
  mesh=[transform(c,pivot,'z',-58) if c['part'].endswith('/weapon') else c for c in mesh]
 # Bend only the forearm/hand chain at the elbow, never rotate the entire hero
 # or change a blade independently of the gripping hand.
 mesh=[transform(c,elbow,'x',-90) if 'color' in c or '/right_hand' in c.get('part','') else c for c in mesh]
 return mesh,dict(elbow=elbow,palm_before_rotation=palm,forearm_pitch=-90,mace_head_width=6.8 if who=='ilya' else None)

# Reuse the inspected renderer, changing texture lookup only for the other heroes.
src=(R/'tools/design_review_180.py').read_text(encoding='utf8');begin=src.index('def render(');end=src.index('\nbook();book(True)',begin);src=src[begin:end]
src=src.replace("('ilya_muromets.png' if who=='ilya' else 'alyosha_popovich.png')","TEXTURES[who]")
scope=dict(base.__dict__,TEXTURES=TEXTURES);exec(src,scope);render=scope['render']
names={'ilya':'Илья Муромец','alyosha':'Алёша Попович','dobrynya':'Добрыня Никитич','finist':'Финист'}
sheet=Image.new('RGB',(1280,1040),'#252A27');metadata={}
for n,who in enumerate(FILES):
 mesh,meta=pose(who);metadata[who]=meta;x=(n%2)*640;y=(n//2)*520
 base.text(sheet,names[who]+' — оружие вперёд',x+18,y+12,'#F5E8C8')
 for offset,angle,label in [(0,-35,'Три четверти'),(320,-90,'Сбоку')]:
  base.text(sheet,label,x+offset+18,y+30,'#BDAE8C');sheet.paste(render(mesh,who,angle),(x+offset,y+48))
 base.text(sheet,'Локоть согнут; кисть и оружие повернуты вместе.',x+18,y+488,'#CCB990')
base.text(sheet,'OFFLINE PREVIEW / положение покоя, не проверка анимаций в игре',18,1025,'#CCB990');sheet.save(O/'four-heroes-forward.png')
(O/'pose-spec.json').write_text(json.dumps(metadata,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('FORWARD_POSE_REVIEW_ONLY four heroes, two views each')
