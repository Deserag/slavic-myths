"""#115 third-person stock-grip calibration, all12 states; no art/FP/gameplay edit."""
from pathlib import Path
import argparse,copy,hashlib,json,zipfile
from weapons87_grips import centered
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/crossbow-grips';IDS=['serpent_crossbow','three_breath_crossbow'];SUFFIXES=['','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework'];GRIP=[8,5.5,12.5]

def proposal():
 assets={}
 for id in IDS:
  for suffix in SUFFIXES:
   key='models/item/'+id+suffix+'.json';model=json.loads((A/key).read_text());display=model.setdefault('display',{})
   for left in [False,True]:display['thirdperson_'+('lefthand' if left else 'righthand')]=centered(GRIP,[-90,0,0],[.55]*3,left)
   assets[key]=model
 return assets

def apply():return main(True)

def main(apply=False):
 D.mkdir(parents=True,exist_ok=True);assets=proposal()
 if not apply:
  from PIL import Image,ImageDraw
  archive=O/'before-crossbow-tp-grips.zip'
  if archive.exists():raise RuntimeError('Preserve before-production stock blueprint')
  paths=['src/main/resources/assets/slavicmyths/'+key for key in assets]+['src/main/java/org/slavicmyths/client/WeaponHands.java','src/main/java/org/slavicmyths/gorynych/SerpentCrossbow.java','src/main/java/org/slavicmyths/gorynych/CrossbowVolley.java']
  with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
   for p in paths:z.write(R/p,p)
  import weapons87_geometry as g
  import armor88_preview as painter
  from weapons87_grip_blueprints import scene
  painter.texture=lambda ref:g.texture(ref.replace(':textures/',':').removesuffix('.png'));samples=[s for s in json.loads((O/'player-native.json').read_text()) if s['pose']=='idle']
  for id in IDS:
   old=g.model('slavicmyths:item/'+id,'thirdperson_righthand');new=assets['models/item/'+id+'.json'];board=Image.new('RGB',(1600,1100),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),id+' #115/#192 / BEFORE production stock-grip calibration; actual native player anchors+JSON/UV, fixed scale; NOT game',fill='white')
   for row,sample in enumerate(samples):
    for left in [False,True]:
     for after,m in [(False,old),(True,new)]:
      rendered=scene(sample,id,'LEFT' if left else 'RIGHT',m)
      for view,yaw in enumerate([0,90]):
       x=(int(left)*4+int(after)*2+view)*200;y=35+row*285;board.paste(painter.render(rendered,yaw),(x,y));draw.text((x+3,y+3),sample['body']+' '+('L' if left else 'R')+' '+('DRAFT' if after else 'BEFORE'),fill='white')
   for index,(label,m) in enumerate([('ACTUAL geometry KEEP',old),('DRAFT geometry KEEP',new)]):
    im=g.project(g.faces(m),245,32,(15,35,0));board.paste(im,(index*280,640),im);draw.text((index*280+5,620),label,fill='white')
   lines=['Actual stock grip box: [7,5,10]..[9,6,15]; center8,5.5,12.5','First-person WeaponHands model: scale.85 *translate(0,2.5,-4.5)/16 =>this authored grip maps to wrist origin','OLD TP rotation-90,translation0,2,0,scale.55 =>palm offset0,4.475,1.375 units (~4.68)','NEW same rotation/scale;translation0,-2.475,-1.375 =>native palm zero','Both physical hands; applies equally to empty,pull0/1/2,arrow,firework models','GUI/FP/ground/fixed/head,geometry/UV/PNG/server damage/ammo/charge/volley KEEP','Player height32 and palm approx4 units; no automatic rescale of either stage']
   for i,line in enumerate(lines):draw.text((600,650+i*40),line,fill='white')
   board.save(D/(id+'.png'))
  (D/'plan.json').write_text(json.dumps({'issue':115,'ids':IDS,'state':'BEFORE_PRODUCTION','decision':'KEEP_ART_FP_FIX_TP_STOCK_PALM_ALL12_STATES','grip':GRIP,'source_grip_box':[[7,5,10],[9,6,15]],'before_offset_units':[0,4.475,1.375],'new_rotation':[-90,0,0],'new_scale':.55,'new_translation':[0,-2.475,-1.375],'source_fp_proof':'WeaponHands.crossbowModel scales.85,translates0,2.5,-4.5/16; authored grip maps exactly to native wrist','png_changes':0,'gameplay_changes':0},indent=2)+'\n')
 manifest={}
 for key,model in assets.items():
  path=A/key if apply else D/'draft-assets'/key;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(model,indent=2)+'\n');manifest[key]=hashlib.sha256(path.read_bytes()).hexdigest()
 (D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n');print(('APPLIED' if apply else 'DRAFT'),'12 crossbow TP stock-grip metadata; actual grip8,5.5,12.5; geometry/FP/PNG/server KEEP');return manifest

if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');main(p.parse_args().apply)
