"""#174/#192: Perunite/thunder-stone focus replaces duplicate wooden fork. No texture edits."""
from pathlib import Path
import argparse,copy,hashlib,json,zipfile
from PIL import Image,ImageDraw
import weapons87_flails as f
from weapons87_grips import centered
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/storm-staff';KEY='models/item/storm_staff.json'
def box(lo,hi,material='perunite',texture=None,uv=(0,0,4,4)):
 p=f.box(lo,hi,material)
 if texture:
  for face in p['faces'].values():face.update({'texture':'#'+texture,'uv':list(uv)})
 return p

def proposal():
 old=json.loads((A/KEY).read_text());display=copy.deepcopy(old['display']);display['gui']=centered([8,15,8],[0,0,-25],[.45]*3)
 parts=[box([7.25,.5,7.25],[8.75,22.5,8.75],texture='wood',uv=(0,0,2,16)),box([7,0,7],[9,.8,9])]
 for y in [9,10.5,12]:parts.append(box([6.9,y,6.9],[9.1,y+.65,9.1],texture='binding'))
 parts += [box([6.8,20.8,6.8],[9.2,22.3,9.2],'iron'),box([4.4,22.3,7],[11.6,23.2,9]),box([4.4,23.2,7],[5.2,28.2,9]),box([10.8,23.2,7],[11.6,28.2,9]),box([4.4,28.2,7],[11.6,29.1,9]),box([7.5,29.1,7.5],[8.5,30,8.5],'silver'),box([6.9,23.2,6.9],[9.1,24,9.1],texture='crystal',uv=(4,4,8,8)),box([6.4,24,6.4],[9.6,27.5,9.6],texture='crystal',uv=(4,4,8,8)),box([6.9,27.5,6.9],[9.1,28.2,9.1],texture='crystal',uv=(4,4,8,8))]
 return {'gui_light':'front','textures':{'surface':'slavicmyths:item/weapons87_materials','wood':'slavicmyths:item/staff_wood','binding':'slavicmyths:item/staff_binding','crystal':'slavicmyths:item/staff_crystal','particle':'slavicmyths:item/staff_wood'},'elements':parts,'display':display}

def apply():return main(True)
def main(apply=False,review=False):
 D.mkdir(parents=True,exist_ok=True);model=proposal();path=A/KEY if apply else D/'draft-assets'/KEY
 if not apply and (D/'draft-manifest.json').exists() and not review:raise RuntimeError('Preserve existing before-production source design.')
 if review:
  with zipfile.ZipFile(O/'before-polearms174.zip') as z:
   if (A/KEY).read_bytes()!=z.read('src/main/resources/assets/slavicmyths/'+KEY):raise RuntimeError('Review redraw allowed only before production.')
  probe=D/'storm_staff-probe-cropped.png'
  if not probe.exists():probe.write_bytes((D/'storm_staff.png').read_bytes())
 if not apply:
  import weapons87_geometry as g
  before=g.model('slavicmyths:item/storm_staff');carved=g.model('slavicmyths:item/carved_staff');board=Image.new('RGB',(1500,720),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'storm_staff #174/#192 / PROPOSAL before production / actual draft JSON+UV, same scale/camera, fixed view offsetY-7',fill='white')
  for j,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   for after,m in [(False,before),(True,model)]:
    x=(j*2+int(after))*250;image=g.project(g.faces(m),245,36,angle,translation=(0,-7,0));board.paste(image,(x,55),image);draw.text((x+3,35),('DRAFT' if after else 'ACTUAL BEFORE')+[' front',' profile',' 3-4'][j],fill='white')
  for j,(label,m) in enumerate([('CARVED KEEP',carved),('STORM BEFORE',before),('STORM PROPOSED',model)]):
   ui=m['display']['gui'];icon=g.project(g.faces(m),16,16,ui.get('rotation',[0,0,0]),ui.get('scale',[1,1,1]),ui.get('translation',[0,0,0]));x=j*180;board.paste(icon,(x+10,370),icon);big=icon.resize((112,112),Image.Resampling.NEAREST);board.paste(big,(x+40,335),big);draw.text((x+4,465),label+' GUI1x/7x',fill='white')
  lines=['Rituals.STORM_STAFF: carved staff +perunite +thunder stone +ancient sign; output unchanged','Head becomes perunite conducting frame +existing blue thunder focus; no shared PNG edits','Extents7.2 x30 x3.2; previous10 x30 x7 wooden fork belongs to carved KEEP','Wood shaft1.5, compact wrap at gripY10; same actual TP/FP/ground/fixed matrices','Gem touches lower and upper mounts; no floating ornament, new connected profile','GUI front-lit, centered, same16px inventory; no VFX/numbers/cooldown/recipe/ID changes']
  for k,line in enumerate(lines):draw.text((570,345+k*37),line,fill='white')
  board.save(D/'storm_staff.png');(D/'plan.json').write_text(json.dumps({'id':'storm_staff','issue':174,'decision':'FIX_DUPLICATE_FORK_KEEP_GAMEPLAY','grip':[8,10,8],'extents':[7.2,30,3.2],'materials':'existing wood/binding, ritual perunite frame, existing blue thunder-stone focus','source_acquisition':'Rituals.java ingredient(STORM_STAFF) and result(STORM_STAFF),4 ordered inputs','hand_displays':{k:v for k,v in model['display'].items() if k!='gui'},'gui':model['display']['gui'],'png_edits':0,'state':'BEFORE_PRODUCTION'},indent=2)+'\n')
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(model,indent=2)+'\n');manifest={KEY:hashlib.sha256(path.read_bytes()).hexdigest()};(D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n');print(('APPLIED' if apply else 'DRAFT'),'storm focus14 connected boxes, zero shared PNG/gameplay/hand-display edits');return manifest
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');p.add_argument('--review',action='store_true');args=p.parse_args();main(args.apply,args.review)
