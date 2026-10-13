"""Review-only arm proportion comparison using existing native IDLE geometry.
Does not touch models, textures, generators, game version or PolyMC.
"""
from pathlib import Path
import copy,json,math
import numpy as np
from PIL import Image,ImageDraw
import design_review_forward_pose as view
base=view.base;R=view.R;O=R/'docs/design-review/1.8.0-proposal/revision-3';O.mkdir(parents=True,exist_ok=True)
CHANGES={'ilya':(0,0),'alyosha':(2,3),'dobrynya':(.5,2),'finist':(.5,.5)}
def bounds(cubes):
 p=np.array([v[:3] for c in cubes for face in c['faces'] for v in face]);return p.min(0),p.max(0)
def body(who):
 mesh=json.loads((R/view.FILES[who]).read_text())[who+'_IDLE']
 return [c for c in mesh if '/right_hand/' not in c['part'] and '/left_hand/' not in c['part']]
def revised(who):
 mesh=copy.deepcopy(body(who));upper_delta,lower_delta=CHANGES[who];data={}
 for side in ['right','left']:
  hand=[c for c in mesh if c['part'].endswith('/'+side+'_hand')];low,high=bounds(hand);upper=side+('_shoulder' if who=='alyosha' else '_arm')
  wrist=high[1]-1.5;elbow=low[1];span=wrist-elbow
  # Lengthen the upper arm below its shoulder; rigid shoulder armor is preserved.
  for c in mesh:
   if not c['part'].endswith('/'+upper):continue
   cl,ch=bounds([c])
   if ch[1]-cl[1]<4:continue
   for face in c['faces']:
    for v in face:v[1]+=upper_delta*(v[1]-cl[1])/(ch[1]-cl[1])
  # Lengthen the forearm before the palm, leaving palm length unchanged.
  for c in hand:
   for face in c['faces']:
    for v in face:v[1]+=upper_delta+lower_delta*max(0,min(1,(v[1]-elbow)/span))
  after_low,after_high=bounds(hand);data[side]=dict(elbow_before=elbow,elbow_after=after_low[1],fingertips_before=high[1],fingertips_after=after_high[1],extension=upper_delta+lower_delta)
 return mesh,data

# Frame the body at consistent scale. Weapons are hidden only in these anatomy views.
src=(R/'tools/design_review_180.py').read_text(encoding='utf8');begin=src.index('def render(');end=src.index('\nbook();book(True)',begin);src=src[begin:end].replace("('ilya_muromets.png' if who=='ilya' else 'alyosha_popovich.png')","view.TEXTURES[who]")
scope=dict(base.__dict__,view=view);exec(src,scope);render=scope['render']
sheet=Image.new('RGB',(1280,1040),'#252A27');measurements={}
for n,who in enumerate(view.FILES):
 x=n%2*640;y=n//2*520;old=body(who);new,measure=revised(who);hips=[c for c in old if c['part']=='root/hips'];hip_low,hip_high=bounds(hips[:1]);pelvis_bottom=hip_high[1]
 for side,d in measure.items():d.update(pelvis_bottom=pelvis_bottom,below_pelvis_before=d['fingertips_before']-pelvis_bottom,below_pelvis_after=d['fingertips_after']-pelvis_bottom)
 measurements[who]=measure
 base.text(sheet,view.names[who],x+16,y+13,'#F5E8C8')
 for offset,cubes,label in [(0,old,'Сейчас: руки опущены'),(320,new,'Предложение: руки опущены')]:
  sheet.paste(render(cubes,who,0),(x+offset,y+48));base.text(sheet,label,x+offset+16,y+31,'#CCB990')
 total=sum(CHANGES[who]);base.text(sheet,'Длина сохранена' if total==0 else 'Удлинение: '+str(total)+' модельных пикселей',x+16,y+485,'#CCB990')
base.text(sheet,'OFFLINE / сравнение пропорций; оружие и щиты временно скрыты только в этом макете',16,1025,'#CCB990');sheet.save(O/'arms-before-after.png')
checks=0
for who,data in measurements.items():
 for side,d in data.items():
  assert d['below_pelvis_after']>=0;checks+=1
  assert abs(d['extension']-sum(CHANGES[who]))<1e-6;checks+=1
 assert abs(data['right']['extension']-data['left']['extension'])<1e-6;checks+=1
(O/'arm-measurements.json').write_text(json.dumps(dict(status='OFFLINE_GEOMETRY_PASS',checks=checks,changes=CHANGES,measurements=measurements,production_integration='NOT_IMPLEMENTED'),ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('ARM_REVIEW_ONLY checks='+str(checks))

# Bow geometry sketch: connected wooden limbs and two string sections. Both
# tip anchors stay fixed while only the nock moves. This is not a BOW pose test.
def rod(a,b,width,color):
 a,b=np.array(a),np.array(b);vec=b-a;length=np.linalg.norm(vec);center=(a+b)/2
 result=base.cube(center,[width,length,width],color)
 # These rods lie in the y/z plane. Turn the local y axis onto their vector.
 angle=math.degrees(math.atan2(vec[2],vec[1]))
 return view.transform(result,center,'x',angle)
points=[[0,-7,-2.2],[0,-4,-.8],[0,0,0],[0,4,-.8],[0,7,-2.2]]
wood=[rod(a,b,.8,'#785238') for a,b in zip(points,points[1:])]+[base.cube([0,0,0],[1.1,2.5,1.1],'#B39A63')]
bow=Image.new('RGB',(640,480),'#252A27')
for n,(label,pull) in enumerate([('Тетива в покое',0),('Натяжение: концы закреплены',3)]):
 nock=[0,0,-2.2-pull];mesh=wood+[rod(points[0],nock,.14,'#E2D6B9'),rod(nock,points[-1],.14,'#E2D6B9')];bow.paste(render(mesh,'alyosha',-90),(n*320,30));base.text(bow,label,n*320+12,12,'#F5E8C8')
base.text(bow,'Схема геометрии лука, не проверка анимации/выстрела.',12,463,'#CCB990');bow.save(O/'bow-string-proposal.png')
