"""#178 source staff shape KEEP, fix cropped/back-facing GUI only."""
from pathlib import Path
import argparse,copy,hashlib,json,zipfile
from PIL import Image,ImageDraw
from weapons87_grips import centered
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/likho-staff';KEY='models/item/likho_staff.json'

def proposal():
 model=json.loads((A/KEY).read_text(encoding='utf-8'));model['gui_light']='front';model['display']['gui']=centered([8.5,11.5,8],[0,180,30],[.5]*3);return model

def apply():return main(True)

def main(apply=False):
 D.mkdir(parents=True,exist_ok=True);model=proposal()
 if not apply:
  archive=O/'before-likho-gui.zip'
  if archive.exists():raise RuntimeError('Preserve source before/draft')
  paths=['src/main/resources/assets/slavicmyths/'+KEY,'src/main/resources/assets/slavicmyths/textures/item/weapon_scythe_surface.png','src/main/java/org/slavicmyths/combat/LikhoStaffItem.java']
  with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
   for path in paths:z.write(R/path,path)
  import weapons87_geometry as g
  import armor88_preview as painter
  from weapons87_grip_blueprints import scene
  old=g.model('slavicmyths:item/likho_staff');board=Image.new('RGB',(1500,1050),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'likho_staff #178/#192 / SOURCE PROPOSAL BEFORE production / actual JSON UV PNG;5-cube shape KEEP, GUI fix',fill='white')
  for index,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   for after,m in [(False,old),(True,model)]:
    x=(index*2+int(after))*250;im=g.project(g.faces(m),245,36,angle,translation=(0,-3.5,0));board.paste(im,(x,55),im);draw.text((x+3,35),('DRAFT KEEP' if after else 'ACTUAL BEFORE')+[' front',' thickness',' 3-4'][index],fill='white')
  back=copy.deepcopy(old);back['display']['gui']=centered([8.5,11.5,8],[0,0,-30],[.5]*3)
  for index,(name,m) in enumerate([('OLD cropped,back face',old),('center only,back face',back),('SELECTED eye face',model)]):
   ui=m['display']['gui'];im=g.project(g.faces(m),16,16,ui.get('rotation',[0,0,0]),ui.get('scale',[1,1,1]),ui.get('translation',[0,0,0]));x=index*230;board.paste(im,(x+10,350),im);board.paste(im.resize((128,128),Image.Resampling.NEAREST),(x+45,330));draw.text((x+3,480),name+' GUI1x/8x',fill='white')
  tex=Image.open(A/'textures/item/weapon_scythe_surface.png').convert('RGBA');board.paste(tex.resize((128,128),Image.Resampling.NEAREST),(750,330));draw.text((750,480),'Actual shared UV palette KEEP',fill='white')
  lines=['Authored bounds5 x29 x3.5 units; player height32, palm approx4; grip8,11,8','Crooked wood shaft/head/eye face retained, no new mythological or boss design','Selected GUI center8.5,11.5,8; scale.5; yaw180 and roll30 show existing eye','GUI front light only; all other display transforms, geometry/UV/PNG byte semantics KEEP','Held differs from carved fork, storm metal frame and Morozko ice crown already','No registry, stats, spells, damage, cooldown, boss93, casting animation or shared PNG edit']
  for i,line in enumerate(lines):draw.text((8,540+i*30),line,fill='white')
  painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'))
  samples=[x for x in json.loads((O/'player-native.json').read_text()) if x['pose']=='idle']
  for row,sample in enumerate(samples):
   for left in [False,True]:
    im=painter.render(scene(sample,'likho_staff','LEFT' if left else 'RIGHT',model),0);x=720+int(left)*300;y=550+row*230;board.paste(im.resize((160,228)),(x,y));draw.text((x+3,y+2),sample['body']+' '+('L' if left else 'R')+' same native hand/model',fill='white')
  board.save(D/'likho_staff.png');(D/'plan.json').write_text(json.dumps({'issue':178,'id':'likho_staff','state':'BEFORE_PRODUCTION','decision':'KEEP_SHAPE_UV_PNG_HANDS_FIX_GUI_CROP_EYE','grip':[8,11,8],'bounds':[5,29,3.5],'gui':model['display']['gui'],'geometry_changes':0,'png_changes':0,'source_palette':'weapon_scythe_surface.png shared, unchanged'},indent=2)+'\n')
 path=A/KEY if apply else D/'draft-assets'/KEY;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(model,indent=2)+'\n');manifest={KEY:hashlib.sha256(path.read_bytes()).hexdigest()};(D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n');print(('APPLIED' if apply else 'DRAFT'),'likho GUI centered,existing eye face; geometry/UV/PNG/non-GUI transforms KEEP');return manifest

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');main(p.parse_args().apply)
