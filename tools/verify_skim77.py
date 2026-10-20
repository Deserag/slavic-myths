"""#77 precise offline contracts. No client/server bootstrap, game or PolyMC changes."""
from pathlib import Path
import hashlib,json,gzip,subprocess,sys,csv
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/skim-77'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
C={}
def check(name,ok,evidence):
 C[name]={'status':'PASS' if bool(ok) else 'FAIL','evidence':evidence}
 if not ok:print('FAIL',name,evidence)
def production():return {p.relative_to(R).as_posix():sha(p) for p in (R/'src/main').rglob('*') if p.is_file()}
baseline=json.loads((O/'before-production-sha256.json').read_text(encoding='utf-8'))
allowed={'src/main/java/org/slavicmyths/client/SkimMesh.java','src/main/java/org/slavicmyths/client/SkimModel.java','src/main/java/org/slavicmyths/client/SkimBeastPose.java','src/main/java/org/slavicmyths/client/SkimBeastGlow.java','src/main/java/org/slavicmyths/client/SkimClient.java','src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png','src/main/resources/assets/slavicmyths/textures/entity/skim_beast_eyes.png','src/main/resources/assets/slavicmyths/textures/entity/skim_beast_rage.png','gradle.properties'}
changed=[p for p,h in baseline.items() if not (R/p).is_file() or sha(R/p)!=h];new=set(production())-set(baseline)
check('production_allowlist',set(changed)|new<=allowed,{'changed':changed,'new':sorted(new)})
original=production()
for i in range(2):
 subprocess.run([sys.executable,str(R/'tools/skim_assets.py'),'--model-only'],cwd=R,check=True)
 check('generator_repeat_'+str(i),production()==original,'Entire src/main SHA256 unchanged after regeneration, no hero/item/lang/audio writes')
source=json.loads((O/'before/source.json').read_text(encoding='utf-8'))
for p in ['docs/design/ISSUE_77_SKIM_MODEL_SPEC.svg','docs/specs/1.7.3/06_References/Approved/APPROVED_Skim_Beast_4_views_and_attacks.png']:
 check('frozen_'+Path(p).stem,sha(R/p)==source['files'][p],source['files'][p])
g=json.loads((O/'after/authored-rig.json').read_text());parts={p['name']:p for p in g['parts']};rects=g['rectangles'];texpath=R/'src/main/resources/assets/slavicmyths/textures/entity/skim_beast.png';body=np.asarray(Image.open(texpath));check('atlas512',body.shape==(512,512,4),body.shape)
from skim_77_model import PAL
palette={tuple(bytes.fromhex(c[1:])) for group in PAL.values() for c in group}
colors=set(map(tuple,body[body[:,:,3]>0,:3]));check('purposeful_palette',colors<=palette,{'opaque_colors':len(colors),'palette':'approved1.7.3,4shadebands,no random noise'})
seen=set();good=True;positive=True;rows=[]
for p in g['parts']:
 good &= p['name'] not in seen and (p['parent'] is None or p['parent'] in seen);seen.add(p['name'])
 for b in p['boxes']:
  positive &= all(x>0 for x in b['shape'][3:]);rows.append([p['name'],p['parent'],p['pivot'],p['rotation'],b['shape'],b['uv'],b['material']])
check('connected_authored_hierarchy',good,str(len(parts))+'bones unique and parent-before-child')
check('positive_cubes',positive,len(rows))
check('mane24_tail1',sum(p.startswith('mane_') for p in parts)==24 and sum(p.startswith('tail_') for p in parts)==5 and parts['tail_0']['parent']=='hips' and all(parts['tail_'+str(i)]['parent']=='tail_'+str(i-1) for i in range(1,5)),'24layered mane groups, one5bone tail chain')
nooverlap=all(max(x,a)>=min(x+w,a+c) or max(y,b)>=min(y+h,b+d) for i,(x,y,w,h) in enumerate(rects) for a,b,c,d in rects[i+1:])
check('cube_net_UV_unique',nooverlap and all(x>=0 and y>=0 and x+w<=512 and y+h<=512 for x,y,w,h in rects),{'independent_nets':len(rects),'gutter':3})
with (O/'after/parts-uv-pivots.csv').open('w',encoding='utf-8',newline='') as f:
 w=csv.writer(f);w.writerow(['part','parent','pivot','rotation','box_xyz_whd','uv','material']);w.writerows(rows)
atlas=Image.open(texpath).convert('RGB').resize((1024,1024),Image.Resampling.NEAREST);d=ImageDraw.Draw(atlas)
for i,(x,y,w,h) in enumerate(rects):d.rectangle((x*2,y*2,(x+w)*2-1,(y+h)*2-1),outline='#70a5ac');d.text((x*2+2,y*2+2),str(i),fill='white')
atlas.save(O/'after/atlas-annotated.png')
a=json.loads(gzip.decompress((O/'after/native-poses.json.gz').read_bytes()));b=json.loads(gzip.decompress((O/'before/native-poses.json.gz').read_bytes()))
check('full_native_fixture',a['native'] and not a['quick'],'Actual production ModelPart, sample plus low-ceiling/hurt/held fixture, not quick mode')
check('captured_source_hashes',all(sha(R/p)==h for p,h in a['files'].items()),a['files'])
check('Alyosha_pose_parity',a['alyosha']==b['alyosha'],{'native_pose_pairs':len(a['alyosha']),'scope':'all25SkimAction x6phase actual hero vertices+UV, exact equality'})
poses=a['poses'];maxground=-999;groundout=[];feeterror=0;lowerror=0;chainerror=0;anchors=0;uvok=True
for k,p in poses.items():
 if k=='victim':continue
 vertices=np.array([v for f in p['faces'] for v in f['vertices']]);bottom=float(vertices[:,1].max());maxground=max(maxground,bottom-24)
 if bottom>24.03:groundout.append((k,bottom))
 if k.startswith(('DEATH','all_DEATH')):continue
 for side in ['right','left']:
  q=np.array(p['joints'][side+'_back_paw']);y=q[1]+4.8
  if 'walk' not in k and 'LEAP' not in k:feeterror=max(feeterror,abs(y-24))
  for end,l1,l2 in [('front',12.5,10.5),('back',11,11)]:
   leg=side+'_'+end;origin=np.array(p['joints'][leg]);joint=np.array(p['joints'][leg+'_shin']);tip=np.array(p['joints'][leg+'_paw']);chainerror=max(chainerror,abs(float(np.linalg.norm(origin-joint))-l1),abs(float(np.linalg.norm(joint-tip))-l2))
  if k.startswith('low_ROAR'):
   tip=np.array(p['joints'][side+'_front_paw']);lowerror=max(lowerror,abs(tip[1]+5-24))
   anchors=max(anchors,abs(q[0]-(-10 if side=='right' else 10)),abs(q[2]-20))
check('all_sampled_geometry_floor',not groundout,{'max_penetration_model_px':maxground,'tolerance_px':.03,'outliers':groundout[:20]})
check('rear_planted_critical_actions',feeterror<.02,{'max_pad_contact_error_px':feeterror})
check('joint_lengths_not_stretched',chainerror<.001,{'max_error_px':chainerror})
check('low_roar_four_supports',lowerror<.02 and anchors<.02,{'front_contact_error_px':lowerror,'rear_anchor_drift_px':anchors,'canRear':False})
for k in ['IDLE_0','ROAR_28','low_ROAR_28']:
 p=poses[k];height=(24-min(v[1] for f in p['faces'] for v in f['vertices']))/16
 check('height_'+k,height<=(4.5 if k=='ROAR_28' else 3.05),{'visible_height':height,'collision_height_frozen':2.95,'rear_ceiling_gate_frozen':4.7})
lowmax=max((24-min(v[1] for f in p['faces'] for v in f['vertices']))/16 for k,p in poses.items() if k.startswith('low_ROAR_'))
check('low_roar_three_block_clearance',lowmax<=3.0,{'all59frames_max_visible_height':lowmax,'collision_frozen':2.95,'scope':'native static envelope,not world collision runtime'})
highanchor=max(abs(poses['all_ROAR_'+str(t)]['joints'][side+'_back_paw'][axis]-expected) for t in range(59) for side,x in [('right',-10),('left',10)] for axis,expected in [(0,x),(2,20)])
check('rear_roar_no_anchor_slip',highanchor<.02,{'all59frames_max_XZ_error_px':highanchor})
spill=0
for key,p in poses.items():
 if not key.startswith(('all_ROAR_','rear_look_','half_ROAR_')):continue
 vertices=np.array([v[:3] for f in p['faces'] for v in f['vertices']]);upper=vertices[vertices[:,1]<24-2.95*16]
 if len(upper):spill=max(spill,float(max(0,-41.6-upper[:,2].min(),upper[:,2].max()-41.6,abs(upper[:,0]).max()-22.4,24-4.7*16-upper[:,1].min())))
check('reared_mesh_inside_frozen_ceiling_guard',spill<.03,{'max_outside_px':spill,'server_check_frozen':'x+-1.4,z+-2.6,y2.95..4.7','poses':'59integer+58half-tickROAR frames plus yaw/pitch/hurt','scope':'native model-space diagnostic at heading0; world collision across headings NOT_RUN'})
for t in range(18,45):
 error=float(np.linalg.norm(np.array(poses['all_GRIP_'+str(t)]['joints']['mouth_contact'])-np.array([0,24-.55*16-7*.9375,-1.7*16+2*.9375])))
 if error>=.01:break
check('grip_hold_anchor18to44',error<.01,{'last_error_px':error,'frozen_victim_feet':'.55up,1.7forward','target':'upper/lower mouth midpoint at native upper-leg back surface; server feet remain unchanged'})
# Conservative per-cuboid AABBs: the native standing victim head/hat must remain outside the boss.
head_boxes=[]
for part in ['head','hat']:
 vv=np.array([v[:3] for f in poses['victim']['faces'] if f['part']==part for v in f['vertices']])
 if len(vv):head_boxes.append((vv.min(0),vv.max(0)))
intersections=[]
for key in ['all_GRIP_'+str(t) for t in range(18,45)]+[k for k in poses if k.startswith('held_look_')]:
 tick=key;faces=poses[key]['faces']
 for i in range(0,len(faces),6):
  cube=faces[i:i+6];vv=np.array([v[:3] for f in cube for v in f['vertices']]);lo,hi=vv.min(0),vv.max(0)
  for head_min,head_max in head_boxes:
   overlap=np.minimum(hi,head_max)-np.maximum(lo,head_min)
   if (overlap>.03).all():intersections.append({'tick':tick,'part':cube[0]['part'],'overlap_px':overlap.tolist()})
check('held_head_clear18to44',not intersections,{'native_fixture_head_hat_vs_all_boss_cubes':len(head_boxes),'tolerance_px':.03,'intersections':intersections[:10]})
# Actual baked native UV sample coverage; one static full mesh contains every cuboid face.
for face in poses['IDLE_0']['faces']:
 uv=np.array(face['vertices'])[:,3:];uvok &= bool(((uv>=0)&(uv<=1)).all())
 for weight in [.15,.5,.85]:
  q=uv[0]*(1-weight)*.5+uv[1]*weight*.5+uv[2]*.5;x,y=np.clip((q*512).astype(int),0,511);uvok &= body[y,x,3]>0
check('native_UV_surface_coverage',uvok,'Every baked quad interior maps to opaque authored body net')
# Depth-tested visibility of the two real eye cuboids; only face tags change for diagnostics.
from skim77_previews import render
ids=[];i=0
for p in g['parts']:
 for cube in p['boxes']:
  if cube['material']=='eye':ids.append((rects[i],'eyeL' if cube['shape'][0]<0 else 'eyeR'))
  i+=1
labeled=[]
for face in poses['IDLE_0']['faces']:
 center=np.array(face['vertices'])[:,3:].mean(0)*512;name=face['part']
 for (x,y,w,h),tag in ids:
  if x<=center[0]<=x+w and y<=center[1]<=y+h:name=tag
 labeled.append(dict(face,part=name))
visibility={}
with Image.open(texpath) as eye_texture:
 for yaw in [0,90,-90,-35]:
  stats={};render({'faces':labeled},eye_texture,1,yaw,size=(520,420),scale=4,stats=stats);visibility[str(yaw)]={k:int(stats[k]) for k in ['eyeL','eyeR'] if k in stats}
check('eyes_visible_depth_test',len(ids)==2 and all(len(v)==(2 if yaw in ['0','-35'] else 1) for yaw,v in visibility.items()),visibility)
before_client=(O/'before/src/main/java/org/slavicmyths/client/SkimClient.java').read_text(encoding='utf-8')
expected_client=before_client.replace('super(c,new SkimModel<>(SkimMesh.create(),true),1.25F);','super(c,new SkimModel<>(SkimMesh.create(),true),1.25F);addLayer(new SkimBeastGlow(this));')
check('renderer_only_beast_glow_added',(R/'src/main/java/org/slavicmyths/client/SkimClient.java').read_text(encoding='utf-8')==expected_client,'Only BeastRenderer constructor adds the local glow layer; hero/arrow/ring source unchanged')
eye_pixels=np.asarray(Image.open(texpath.with_name('skim_beast_eyes.png')));rage_pixels=np.asarray(Image.open(texpath.with_name('skim_beast_rage.png')))
expected_eye=np.zeros((512,512),bool)
for (x,y,w,h),tag in ids:expected_eye[y:y+h,x:x+w]=True
red_colors={tuple(bytes.fromhex(c[1:])) for c in PAL['scar']};expected_rage=np.array([[tuple(pixel[:3]) in red_colors and pixel[3]>0 for pixel in row] for row in body])
check('glow_masks_are_local',eye_pixels.shape==body.shape and rage_pixels.shape==body.shape and np.array_equal(eye_pixels[:,:,3]>0,expected_eye) and np.array_equal(rage_pixels[:,:,3]>0,expected_rage),{'eyes':'only two actual eye cube nets','rage':'only existing red scar/band pixels','unused':'transparent','no_whole_face_or_body_glow':True})
check('server_logic_exact',sha(R/'src/main/java/org/slavicmyths/skim/SkimBeastEntity.java')==source['files']['src/main/java/org/slavicmyths/skim/SkimBeastEntity.java'],'AI, all hit windows11/19/30,29,22,28,24,hold18..44/hits21/31/41/throw45,AABB,save/ID,audio callsites frozen')
result={'status':'PASS_OFFLINE' if all(x['status']=='PASS' for x in C.values()) else 'FAIL','contracts':C,'native_poses':len(poses),'alyosha_pairs':len(a['alyosha']),'changed':changed,'new':sorted(new),'runtime':'NOT_RUN,OWNER_PENDING','polymc':'NOT_UPDATED'}
(O/'offline-checks.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print('SKIM77',sum(v['status']=='PASS' for v in C.values()),'PASS /',len(C),'contracts; native poses',len(poses))
if result['status']!='PASS_OFFLINE':raise SystemExit(1)
