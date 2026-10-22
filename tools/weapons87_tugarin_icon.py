"""#182/#192 restore unsquashed paired swords; source PNG and held remain unchanged."""
from pathlib import Path
import argparse,copy,hashlib,json,zipfile
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/tugarin-icon';KEY='models/item/tugarin_sword.json'

def proposal():
 # Extrude the actual 16px sprite at its native aspect ratio. Each pixel maps to
 # its own atlas cell; parallel duplicates use depth separation, never coplanar.
 pixels=Image.open(A/'textures/item/tugarin_sword.png').convert('RGBA');elements=[];w,h=pixels.size
 for dx,dz in [(-2,-.6),(2,.6)]:
  elements.append({'from':[dx,0,7.5+dz],'to':[16+dx,16,8.5+dz],'faces':{'north':{'texture':'#blade','uv':[16,0,0,16]},'south':{'texture':'#blade','uv':[0,0,16,16]}}})
  for py in range(h):
   for px in range(w):
    if pixels.getpixel((px,py))[3]<=127:continue
    uv=[px*16/w,py*16/h,(px+1)*16/w,(py+1)*16/h];faces={}
    for side,sx,sy in [('west',-1,0),('east',1,0),('up',0,-1),('down',0,1)]:
     nx,ny=px+sx,py+sy
     if nx<0 or nx>=w or ny<0 or ny>=h or pixels.getpixel((nx,ny))[3]<=127:faces[side]={'texture':'#blade','uv':uv}
    if faces:elements.append({'from':[px*16/w+dx,(h-py-1)*16/h,7.5+dz],'to':[(px+1)*16/w+dx,(h-py)*16/h,8.5+dz],'faces':faces})
 return {'gui_light':'front','textures':{'blade':'slavicmyths:item/tugarin_sword','particle':'slavicmyths:item/tugarin_sword'},'elements':elements,'display':{'gui':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[.75]*3},'ground':{'scale':[.35]*3}}}

def apply():return main(True)

def main(apply=False,review=False):
 D.mkdir(parents=True,exist_ok=True);archive=O/'before-tugarin-icon.zip'
 paths=['src/main/resources/assets/slavicmyths/'+KEY,'src/main/resources/assets/slavicmyths/models/item/tugarin_sword_held.json','src/main/resources/assets/slavicmyths/textures/item/tugarin_sword.png','src/main/java/org/slavicmyths/client/TugarinPairClient.java','src/main/java/org/slavicmyths/combat/TugarinPair.java']
 if not apply:
  if review:
   with zipfile.ZipFile(archive) as z:assert (A/KEY).read_bytes()==z.read(paths[0]),'Production already changed'
   if not (D/'probe-wrong32px-sampling.png').exists():(D/'probe-wrong32px-sampling.png').write_bytes((D/'tugarin_sword.png').read_bytes())
  else:
   if archive.exists() or (D/'draft-manifest.json').exists():raise RuntimeError('Preserve existing before evidence')
   with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
    for p in paths:z.write(R/p,p)
 model=proposal()
 if not apply:
  import weapons87_geometry as g
  before=g.model('slavicmyths:item/tugarin_sword');held=g.model('slavicmyths:item/tugarin_sword','thirdperson_righthand');board=Image.new('RGB',(1500,840),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'tugarin_sword #182/#192 / SOURCE PROPOSAL BEFORE production / existing PNG, no invented historical canon',fill='white')
  for i,angles in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   for after,m in [(False,before),(True,model)]:
    x=(i*2+int(after))*250;im=g.project(g.faces(m),245,24,angles);board.paste(im,(x,55),im);draw.text((x+3,35),('DRAFT' if after else 'ACTUAL BEFORE')+[' front',' side',' 3-4'][i],fill='white')
  for i,(label,m) in enumerate([('OLD squeezed V',before),('PROPOSED parallel pair',model),('HELD KEEP single blade',held)]):
   ui=m.get('display',{}).get('gui',{});icon=g.project(g.faces(m),16,16,ui.get('rotation',[0,0,0]),ui.get('scale',[1,1,1]),ui.get('translation',[0,0,0]));x=i*230;board.paste(icon,(x+10,355),icon);board.paste(icon.resize((128,128),Image.Resampling.NEAREST),(x+45,335));draw.text((x+3,480),label+' GUI1x/8x',fill='white')
  tex=Image.open(A/'textures/item/tugarin_sword.png').convert('RGBA');board.paste(tex.resize((128,128),Image.Resampling.NEAREST),(730,335));draw.text((730,480),'Actual 32px palette/UV',fill='white')
  for i,line in enumerate(['Variant0: old squeezed and counter-rotated planes (rejected defect baseline)','Variant1 SELECTED: two parallel native-aspect blades, spacing4, depth2.2, GUI scale.75','Variant2 single blade: existing held KEEP; GUI pair conveys virtual second blade without inventory duplication','Original point/straight edge, warm inner blade line, gold crossguard and dark grip retained','Native canvas16x16 per sword; held1px thickness; paired source bounds20x16x2.2','Player reference32 model units; hand source is SAME original generated held, no new grip transform','Pair actual owner renderer remains TugarinPairClient; inventory model never replaces held','No damage/speed/reach/cooldown/IDs/recipe/boss edits. Runtime not run.']):draw.text((8,535+i*31),line,fill='white')
  board.save(D/'tugarin_sword.png');(D/'plan.json').write_text(json.dumps({'issue':182,'id':'tugarin_sword','state':'BEFORE_PRODUCTION','decision':'FIX_GUI_ASPECT_PAIR_KEEP_HELD_PNG','selected_variant':'parallel native-aspect pair','grip':'unchanged generated handheld and TugarinPairClient native wrist; see package3 FP source','player_height_model_units':32,'native_sprite_canvas':[16,16],'pair_offset_x':[-2,2],'pair_offset_z':[-.6,.6],'png_edits':0,'preserved':paths[1:]},indent=2)+'\n')
 path=A/KEY if apply else D/'draft-assets'/KEY;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(model,indent=2)+'\n');manifest={KEY:hashlib.sha256(path.read_bytes()).hexdigest()};(D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n');print(('APPLIED' if apply else 'DRAFT'),len(model['elements']),'native sprite cells, two unsquashed blades, no PNG/held/gameplay changes');return manifest

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');p.add_argument('--review',action='store_true');args=p.parse_args();main(args.apply,args.review)
