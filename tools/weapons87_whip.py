"""#180 static tapered Poloz lash, five source poses; #190 owns later dynamics."""
from pathlib import Path
import argparse,copy,hashlib,json,math,zipfile
from PIL import Image,ImageDraw
import weapons87_flails as f
from weapons87_grips import centered
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/poloz-whip'
ANGLES=[
 [0,22.5,45,67.5,90,112.5,135,157.5,180,180,180,180,180,180,180,180,180,180,180,180],
 [0,-22.5,-45,-67.5,-90,-112.5,-135,-157.5,-180,-180,-180,-180,-180,-180,-180,-180,-180,-180,-180,-180],
 [0,0,0,22.5,22.5,45,45,67.5,67.5,90,90,112.5,112.5,135,135,157.5,157.5,180,180,180],
 [0,0,22.5,22.5,45,45,67.5,67.5,90,90,90,90,90,90,90,90,90,90,90,90],
 [0,22.5,45,67.5,90,112.5,135,157.5,180,180,180,180,180,180,180,180,180,180,157.5,157.5]]

def box(lo,hi,mat='leather'):
 return f.box(lo,hi,mat)

def segment(a,b,radius):
 mid=[(a[i]+b[i])/2 for i in range(3)];dx,dy=b[0]-a[0],b[1]-a[1];length=math.hypot(dx,dy);angle=math.degrees(math.atan2(dy,dx))
 if abs(dx)>=abs(dy):
  rotation=angle if dx>=0 else angle-180;lo=[mid[0]-length/2,mid[1]-radius,mid[2]-radius];hi=[mid[0]+length/2,mid[1]+radius,mid[2]+radius]
 else:
  rotation=angle-90 if dy>=0 else angle+90;lo=[mid[0]-radius,mid[1]-length/2,mid[2]-radius];hi=[mid[0]+radius,mid[1]+length/2,mid[2]+radius]
 rotation=round((rotation+180)%360-180,6);part=box(lo,hi)
 if rotation:part['rotation']={'origin':mid,'axis':'z','angle':rotation,'rescale':False}
 assert rotation in [-45,-22.5,0,22.5,45],rotation
 return part

def held(pose):
 parts=[box([7.2,-4,7.2],[8.8,5,8.8],'wood'),box([7,-4,7],[9,-3.3,9],'gold'),box([6.9,4.2,6.9],[9.1,5.2,9.1],'gold')]
 for y in [-2.6,-1.2,.2,1.6,3]:parts.append(box([7.1,y,7.1],[8.9,y+.45,8.9]))
 points=[[8.,5.,8.]]
 for i,angle in enumerate(ANGLES[pose]):
  a=points[-1];rad=math.radians(angle);b=[a[0]+math.sin(rad)*1.15,a[1]+math.cos(rad)*1.15,8.];radius=.36-i*.011;parts.append(segment(a,b,radius));parts.append(box([x-radius for x in b],[x+radius for x in b]));points.append(b)
  if i in [3,9,15]:parts.append(box([b[0]-.45,b[1]-.28,7.55],[b[0]+.45,b[1]+.28,8.45],'gold'))
 # A small tapered dark end, not an iron weight or a flail ball.
 tip=points[-1];parts.append(box([tip[0]-.12,tip[1]-.6,7.88],[tip[0]+.12,tip[1]+.05,8.12]))
 display={}
 for left in [False,True]:
  side='lefthand' if left else 'righthand';display['thirdperson_'+side]=centered([8,0,8],[75,180 if left else 0,0],[.75]*3,left);display['firstperson_'+side]=centered([8,0,8],[0,135 if left else -45,-15],[.6]*3,left,target=(0,4,-2))
 return {'gui_light':'front','textures':{'surface':'slavicmyths:item/weapons87_materials','particle':'slavicmyths:item/poloz_whip_segments'},'elements':parts,'display':display},points

def proposal():
 assets={};plans=[]
 for i in range(5):
  model,points=held(i);assets[f'models/item/poloz_whip_held87_{i}.json']=model;plans.append({'pose':i,'points':points,'radii':[.36-j*.011 for j in range(20)]})
 # Repo-native pixel profile: minimum one GUI pixel for the tapered cord.
 # It samples existing atlas cells; no PNG creation/editing or supersampling.
 pixels={}
 for y in range(7,14):
  pixels[4,y]='wood';pixels[5,y]='leather'
 for p in [(4,6),(5,6),(4,13),(5,13)]:pixels[p]='gold'
 for p in [(4,5),(5,4),(6,3),(7,3),(8,3),(9,3),(10,4),(11,5),(11,6),(11,7),(11,8),(11,9),(11,10),(11,11),(10,12)]:pixels[p]='wood'
 for p in [(5,4),(11,7)]:pixels[p]='gold'
 cells=[]
 for (x,y),mat in pixels.items():
  cell=box([x,15-y,7.75],[x+1,16-y,8.25],mat);tx,ty=f.TILE[mat]
  for face in cell['faces'].values():face['uv']=[tx+2,ty+2,tx+3,ty+3]
  cells.append(cell)
 assets['models/item/poloz_whip_icon87.json']={'gui_light':'front','textures':{'surface':'slavicmyths:item/weapons87_materials','particle':'slavicmyths:item/poloz_whip_segments'},'elements':cells,'display':{'gui':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}}}
 for i in range(5):assets[f'models/item/poloz_whip_held87_{i}.json']['display'].update({'ground':{'translation':[0,3,0],'scale':[.35]*3},'fixed':centered([11,1,8],[0,0,-25],[.8]*3)})
 def wrapper(i):return {'loader':'neoforge:separate_transforms','gui_light':'front','textures':{'particle':'slavicmyths:item/poloz_whip_segments'},'base':{'parent':f'slavicmyths:item/poloz_whip_held87_{i}'},'perspectives':{ctx:{'parent':'slavicmyths:item/'+('poloz_whip_icon87' if ctx=='gui' else 'poloz_whip_held87_0')} for ctx in ['gui','ground','fixed']}}
 for i in range(5):assets[f'models/item/poloz_whip_pose_{i}.json']=wrapper(i)
 root=wrapper(0);root['overrides']=[{'predicate':{'slavicmyths:whip_pose':v},'model':f'slavicmyths:item/poloz_whip_pose_{i}'} for i,v in enumerate([.1,.35,.5,.7,1])];assets['models/item/poloz_whip.json']=root
 return assets,plans

def apply():return main(True)

def main(apply=False,review=False,revise=False):
 D.mkdir(parents=True,exist_ok=True);assets,plans=proposal()
 if not apply:
  archive=O/'before-whip180.zip'
  if revise:
   prior=json.loads((D/'applied-manifest.json').read_text())
   assert all(hashlib.sha256((A/key).read_bytes()).hexdigest()==digest for key,digest in prior.items()),'Production changed outside revision'
   target=D/'revision1-static-draft.zip'
   if target.exists():raise RuntimeError('Revision1 already archived')
   files=[p for p in D.rglob('*') if p.is_file()]
   with zipfile.ZipFile(target,'w',zipfile.ZIP_DEFLATED) as z:
    for p in files:z.write(p,p.relative_to(D).as_posix())
  elif review:
   with zipfile.ZipFile(archive) as z:assert (A/'models/item/poloz_whip.json').read_bytes()==z.read('src/main/resources/assets/slavicmyths/models/item/poloz_whip.json'),'Already changed production'
   if not (D/'probe-thin-gui.png').exists():(D/'probe-thin-gui.png').write_bytes((D/'poloz_whip.png').read_bytes())
  elif archive.exists():raise RuntimeError('Preserve prior design/source')
  paths=list(A.glob('models/item/poloz_whip*.json'))+list(A.glob('textures/item/poloz_whip*.png'))+[R/'src/main/java/org/slavicmyths/client/PolozClient.java',R/'src/main/java/org/slavicmyths/poloz/PolozWhip.java']
  if not review and not revise:
   with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
    for p in paths:z.write(p,p.relative_to(R).as_posix())
  import weapons87_geometry as g
  overrides={'slavicmyths:'+key.removeprefix('models/').removesuffix('.json'):v for key,v in assets.items()};old=g.model('slavicmyths:item/poloz_whip_pose_0','thirdperson_righthand');new=assets['models/item/poloz_whip_held87_0.json'];board=Image.new('RGB',(1500,1150),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'poloz_whip #180/#192 / STATIC ART SOURCE PROPOSAL BEFORE production; actual draft JSON/UV, motion stays #190',fill='white')
  for i,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   for after,m in [(False,old),(True,new)]:
    x=(i*2+int(after))*250;im=g.project(g.faces(m),245,40,angle,translation=(0,-2,0));board.paste(im,(x,55),im);draw.text((x+3,35),('DRAFT' if after else 'ACTUAL pose0')+[' front',' thickness',' 3-4'][i],fill='white')
  for i in range(5):
   m=assets[f'models/item/poloz_whip_held87_{i}.json'];im=g.project(g.faces(m),230,40,translation=(0,-2,0));board.paste(im,(i*290,350),im);draw.text((i*290+4,330),'Draft static pose'+str(i)+'; unchanged predicate',fill='white')
  for i,m in enumerate([g.model('slavicmyths:item/poloz_whip'),g.model('slavicmyths:item/poloz_whip','gui',overrides)]):
   ui=m.get('display',{}).get('gui',{});im=g.project(g.faces(m),16,16,ui.get('rotation',[0,0,0]),ui.get('scale',[1,1,1]),ui.get('translation',[0,0,0]));board.paste(im,(i*210+10,650),im);board.paste(im.resize((128,128),Image.Resampling.NEAREST),(i*210+45,620));draw.text((i*210+5,770),'GUI1x/8x '+('DRAFT static' if i else 'OLD null-owner'),fill='white')
  for i,line in enumerate(['Grip8,0,8; handle9 units, width1.6; palm approx4; player32 units','20 continuous sections x1.15; radius.36 to.151; no heavy spiked flail head','Dark leather / existing gold nodes / wood handle; existing shared atlas and Poloz PNG KEEP','Idle lash curls away and ends below hand; five static poses retained, no state/timing code edits','Native left/right FP/TP centered grips; GUI/ground/fixed stable static icon for every override','No new PNG/IDs/physics/attack/damage/cooldown/boss geometry; #190 owns gradual motion','Actual source data and fixed player-scale evidence saved alongside proposal']):draw.text((470,630+i*29),line,fill='white')
  board.save(D/'poloz_whip.png')
  import armor88_preview as painter
  from weapons87_grip_blueprints import scene
  painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'))
  playerboard=Image.new('RGB',(1600,680),'#253039');pd=ImageDraw.Draw(playerboard);pd.text((8,8),'PRE-PRODUCTION actual native32-unit player/hand anchors +old/draft JSON UV, fixed camera; NOT game',fill='white')
  for row,sample in enumerate(x for x in json.loads((O/'player-native.json').read_text()) if x['pose']=='idle'):
   for left in [False,True]:
    for after,m in [(False,old),(True,new)]:
     rendered=scene(sample,'poloz_whip','LEFT' if left else 'RIGHT',m)
     for view,yaw in enumerate([0,90]):
      x=(int(left)*4+int(after)*2+view)*200;y=35+row*285;playerboard.paste(painter.render(rendered,yaw),(x,y));pd.text((x+3,y+3),sample['body']+' '+('L' if left else 'R')+' '+('DRAFT' if after else 'BEFORE'),fill='white')
  pd.text((8,630),'Same native pose/body scale for both stages. Grip8,0,8 centered in both palms; long free end falls outside the torso.',fill='white');playerboard.save(D/'poloz_whip-player-scale.png')
  (D/'plan.json').write_text(json.dumps({'id':'poloz_whip','issue':180,'state':'BEFORE_PRODUCTION_REVISION2' if revise else 'BEFORE_PRODUCTION','revision2_reason':'left asymmetric lash must curve outward, FP targetY4; original gold source and first revision remain archived','grip':[8,0,8],'points':plans,'png_edits':0,'predicate_thresholds_unchanged':[.1,.35,.5,.7,1],'animation_code':'PolozClient unchanged; separate190'},indent=2)+'\n')
 manifest={}
 for key,model in assets.items():
  path=A/key if apply else D/'draft-assets'/key;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(model,indent=2)+'\n');manifest[key]=hashlib.sha256(path.read_bytes()).hexdigest()
 (D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n');print(('APPLIED' if apply else 'DRAFT'),len(assets),'static whip models; tapered connected lash; no PNG/server/predicate code edits');return manifest

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');p.add_argument('--review',action='store_true');p.add_argument('--revise',action='store_true');args=p.parse_args();main(args.apply,args.review,args.revise)
