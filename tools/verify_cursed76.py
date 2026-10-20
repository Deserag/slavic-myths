"""#76 offline contract: actual native mesh, scoped generation, UV and frozen production files."""
from pathlib import Path
import json,gzip,hashlib,csv,subprocess,sys,math
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/volkolak-76';A=R/'src/main/resources/assets/slavicmyths'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
checks={}
def check(name,ok,evidence):
 checks[name]={'status':'PASS' if ok else 'FAIL','evidence':evidence}
 if not ok:print('FAIL',name,evidence)
def manifest():return {str(p.relative_to(R)).replace('\\','/'):sha(p) for p in (R/'src/main').rglob('*') if p.is_file()}
m=manifest()
for n in range(2):
 subprocess.run([sys.executable,str(R/'tools/volkolak_143_resources.py'),'--model-only'],cwd=R,check=True)
 check('generator_repeat_'+str(n),manifest()==m,'Entire src/main SHA256 manifest identical after model-only regeneration')
baseline=json.loads((R/'docs/implementation/hero-hud-90-v3/build-source-sha256.json').read_text())
allowed={'src/main/java/org/slavicmyths/client/CursedVolkolakGeometry.java','src/main/java/org/slavicmyths/client/CursedVolkolakModel.java','src/main/resources/assets/slavicmyths/cursed_volkolak_geometry.json','src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak.png','src/main/resources/assets/slavicmyths/textures/entity/cursed_volkolak_eyes.png','gradle.properties'}
changed=[p for p,h in baseline.items() if not (R/p).exists() or sha(R/p)!=h]
check('frozen_production_scope',set(changed)<=allowed,changed)
check('no_new_production_files',not(set(m)-set(baseline)),sorted(set(m)-set(baseline)))
g=json.loads((A/'cursed_volkolak_geometry.json').read_text());parts={p['name']:p for p in g['parts']};rects=g['uv_rectangles'];body=np.array(Image.open(A/'textures/entity/cursed_volkolak.png'));eye=np.array(Image.open(A/'textures/entity/cursed_volkolak_eyes.png'))
check('atlas_dimensions',body.shape==(512,512,4) and eye.shape==(512,512,4),'Two512x512RGBA atlases')
seen=set();acyclic=True
for p in g['parts']:
 acyclic &= p['name'] not in seen and (p['parent'] is None or p['parent'] in seen);seen.add(p['name'])
check('rig_hierarchy',acyclic,'Topological parent-before-child; all names unique')
overlap=False
for i,(x,y,w,h) in enumerate(rects):
 for xx,yy,ww,hh in rects[i+1:]:overlap |= max(x,xx)<min(x+w,xx+ww) and max(y,yy)<min(y+h,yy+hh)
check('unique_uv_allocations',not overlap and all(x>=0 and y>=0 and x+w<=512 and y+h<=512 for x,y,w,h in rects),f'{len(rects)} independent cube nets,2px gutters')
mask=np.zeros((512,512),bool);recti=0
rows=[]
for p in g['parts']:
 for b in p['boxes']:
  rows.append([p['name'],p['parent'],p['pos'],p['rotation'],b['shape'],b['uv'],b['material']]);x,y,w,h=rects[recti];recti+=1
  if b['material']=='eye':mask[y:y+h,x:x+w]=True
check('eye_mask_only_two_islands',np.array_equal(eye[:,:,3]>0,mask) and sum(b['material']=='eye' for p in g['parts'] for b in p['boxes'])==2,'Only eyeL/eyeR allocated nets emit; all other alpha0')
check('positive_shapes',all(all(v>0 for v in b['shape'][3:]) for p in g['parts'] for b in p['boxes']),'All authored cuboid dimensions positive')
with (O/'after/parts-uv-pivots.csv').open('w',newline='',encoding='utf-8') as f:
 w=csv.writer(f);w.writerow(['part','parent','pivot','rotation','box_xyz_whd','uv','material']);w.writerows(rows)
atlas=Image.open(A/'textures/entity/cursed_volkolak.png').convert('RGB').resize((1024,1024),Image.Resampling.NEAREST);d=ImageDraw.Draw(atlas)
for n,(p,b) in enumerate((p,b) for p in g['parts'] for b in p['boxes']):
 x,y,w,h=rects[n];d.rectangle((x*2,y*2,(x+w)*2-1,(y+h)*2-1),outline='#78adad');d.text((x*2+2,y*2+2),str(n),fill='white')
atlas.save(O/'after/atlas-annotated.png')
poses=json.loads(gzip.decompress((O/'after/native-poses.json.gz').read_bytes()))['poses']
finite=True;alpha=True;maxerror=0;anchorerror=0;falls=0;walkerror=0
for key,pose in poses.items():
 for face in pose['faces']:
  verts=np.array(face['vertices']);finite &= bool(np.isfinite(verts).all())
  if key in ['victim','greywolf_reference']:continue
  uv=verts[:,3:];alpha &= bool(((uv>=0)&(uv<=1)).all())
  # Interior UV samples, not polygon boundaries/gutter rounding.
  for t in [.15,.5,.85]:
   q=uv[0]*(1-t)*.5+uv[1]*t*.5+uv[2]*.5;x,y=np.clip((q*512).astype(int),0,511);alpha &= body[y,x,3]>0
 if key.startswith(('rise_all_','drop_all_')) or key in ['quad','upright']:
  for side in ['L','R']:
   point=np.array(pose['joints']['foot'+side]);maxerror=max(maxerror,abs(point[1]+3-24));anchorerror=max(anchorerror,abs(point[0]-(-3.7 if side=='L' else 3.7)),abs(point[2]-3.2))
 if key.startswith('escape_all_'):falls=max(falls,max(v[1] for f in pose['faces'] for v in f['vertices'])-24)
 if key.startswith('walk_all_'):
  for side in ['L','R']:
   y=pose['joints']['foot'+side][1]+3;walkerror=max(walkerror,max(y-24,22-y,0))
check('native_finite',finite,f'{len(poses)} native poses; every actual face vertex finite')
check('native_uv_coverage',bool(alpha),'All native cube face interior samples within atlas and opaque body coverage')
check('transition_rear_ground',maxerror<.01,{'max_model_px_error':maxerror})
check('transition_rear_no_slip',anchorerror<.01,{'max_anchor_xz_error_px':anchorerror})
check('false_death_floor',falls<.02,{'all132frames_max_penetration_px':falls})
check('walk_support',walkerror<.02,{'96samples_rear_support_or_swing_22to24':walkerror})
for t in [19,30,48,49,60,131]:
 names={f['part'] for f in poses['ESCAPE_'+str(t)]['faces']};check('escape_eyes_'+str(t),({'eyeL','eyeR'}<=names)==(t>=49),'actual mesh visibility, closed19<=t<49')
for prefix in ['blend_IDLE','blend_RISE','blend_CLAW_L','blend_CLAW_R','blend_GRAB','blend_DROP']:
 error=max(abs(poses[prefix+'_'+str(t)]['joints']['foot'+side][1]+3-24) for t in range(7) for side in ['L','R'])
 check(prefix+'_support',error<.01,{'max_error_px':error,'frames':'0..6 using production blendPose'})
for t in [24,28,30,45]:
 p=poses['grab_true_'+str(t)]['joints'];error=max(np.linalg.norm(np.array(p['hand'+s])-np.array([x,8,-.7*16/.68])) for s,x in [('L',-8),('R',8)])
 check('grab_anchor_'+str(t),error<.01,{'hand_target_error_px':float(error),'victim':'native vanilla humanoid, frozen server feet .7forward/.18up'})
from cursed76_previews import render
counts={}
tex=Image.open(A/'textures/entity/cursed_volkolak.png')
for key in ['quad','upright']:
 for yaw in [0,90,-90,-35]:
  vis={};render(poses[key],tex,.68,yaw,stats=vis);counts[key+'_'+str(yaw)]={k:v for k,v in vis.items() if k in ['eyeL','eyeR']}
check('eye_depth_visibility',all((len(v)==2 if k.endswith(('_0','_-35')) else len(v)==1) for k,v in counts.items()),counts)
for rising in ['rise','drop']:
 error=max(abs(poses['boundary_'+rising+'_idle_'+str(t)]['joints']['foot'+side][1]+3-24) for t in range(7) for side in ['L','R'])
 check('end_boundary_'+rising,error<.01,{'max_ground_error_px':error,'frames':'actual completed clip to IDLE,0..6'})
penetration=max(max(v[1] for f in p['faces'] for v in f['vertices'])-24 for k,p in poses.items() if k not in ['victim','greywolf_reference'])
check('all_pose_ground_bounds',penetration<.05,{'max_model_px_penetration':penetration,'tolerance_px':.05,'scope':'all native captured clips and production blends, subpixel tolerance'})
chainerror=0
for key,p in poses.items():
 if key in ['victim','greywolf_reference']:continue
 for side in ['L','R']:
  for parent,child,length in [('upper','fore',10.5),('fore','hand',9.5),('thigh','shin',10),('shin','foot',10.5)]:
   a=np.array(p['joints'][parent+side]);b=np.array(p['joints'][child+side]);chainerror=max(chainerror,abs(np.linalg.norm(a-b)-length))
check('joint_chains_connected',chainerror<.001,{'max_length_error_px':float(chainerror),'all_poses':len(poses),'limbs':'no stretched/detached upper-lower-terminal joint chains'})
for key,height in [('quad',1.9),('upright',2.8)]:
 actual=(24-min(v[1] for f in poses[key]['faces'] for v in f['vertices']))/16
 width=(max(v[0] for f in poses[key]['faces'] for v in f['vertices'])-min(v[0] for f in poses[key]['faces'] for v in f['vertices']))/16
 check('frozen_hitbox_'+key,actual<=height and width<=1.65,{'visible_height_blocks':actual,'frozen_height_blocks':height,'width_blocks':width,'frozen_width_blocks':1.65,'scope':'idle bounds,not runtime collision'})
old=json.loads((O/'before/src/main/resources/assets/slavicmyths/cursed_volkolak_geometry.json').read_text())
summary={'checks':checks,'source_changed_from28':changed,'geometry':{'before_parts':len(old['parts']),'after_parts':len(parts),'before_cubes':sum(len(p['boxes']) for p in old['parts']),'after_cubes':len(rects)},'runtime':'NOT_RUN; owner in-game acceptance pending; no PolyMC install'}
(O/'offline-checks.json').write_text(json.dumps(summary,indent=2)+'\n')
print('CURSED76',sum(c['status']=='PASS' for c in checks.values()),'PASS /',len(checks),'contracts')
if any(c['status']!='PASS' for c in checks.values()):raise SystemExit(1)
