"""#170 source-owned silver mace head/material/flat GUI; preserves all hand transforms."""
from pathlib import Path
import argparse,copy,hashlib,json
from PIL import Image,ImageDraw
import weapons87_flails as f
from weapons87_grips import centered
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/silver-mace';KEY='models/item/silver_mace.json'
def proposal():
 before=json.loads((A/KEY).read_text());display=copy.deepcopy(before['display']);display['gui']=centered([8,9.25,8],[0,0,-35],[.8]*3)
 b=lambda lo,hi,m='silver':f.box(lo,hi,m)
 parts=[b([7.35,.5,7.35],[8.65,12.5,8.65],'wood'),b([7.1,0,7.1],[8.9,.9,8.9])]
 for y in [2,4,6]:parts.append(b([7.2,y,7.2],[8.8,y+.7,8.8],'leather'))
 parts += [b([6.85,11.5,6.85],[9.15,12.2,9.15],'iron'),b([6.6,12.2,6.6],[9.4,16.4,9.4]),b([5.3,12.8,7.6],[10.7,15.9,8.4]),b([7.6,12.8,5.3],[8.4,15.9,10.7]),b([6.9,16.4,6.9],[9.1,17.5,9.1]),b([7.5,17.5,7.5],[8.5,18.5,8.5])]
 return {'gui_light':'front','textures':{'surface':'slavicmyths:item/weapons87_materials','particle':'minecraft:block/iron_block'},'elements':parts,'display':display}
def apply():return main(True)
def main(apply=False):
 D.mkdir(parents=True,exist_ok=True);model=proposal();path=A/KEY if apply else D/'draft-assets'/KEY
 if not apply and (D/'draft-manifest.json').exists():raise RuntimeError('Preserve existing before-production design evidence.')
 if not apply:
  import weapons87_geometry as g
  before=g.model('slavicmyths:item/silver_mace');board=Image.new('RGB',(1500,650),'#253039');draw=ImageDraw.Draw(board);draw.text((8,8),'silver_mace #192/#170 / PROPOSAL before production / actual JSON+UV, fixed camera and scale',fill='white')
  for j,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
   for after,m in [(False,before),(True,model)]:
    x=(j*2+int(after))*250;image=g.project(g.faces(m),240,24,angle);board.paste(image,(x,55),image);draw.text((x+3,35),('DRAFT' if after else 'ACTUAL BEFORE')+[' front',' profile',' 3-4'][j],fill='white')
  for after,m in [(False,before),(True,model)]:
   ui=m['display']['gui'];icon=g.project(g.faces(m),16,16,ui.get('rotation',[0,0,0]),ui.get('scale',[1,1,1]),ui.get('translation',[0,0,0]));x=int(after)*220;board.paste(icon,(x+15,365),icon);large=icon.resize((112,112),Image.Resampling.NEAREST);board.paste(large,(x+55,335),large);draw.text((x+10,465),'GUI1x/7x '+('proposed front' if after else 'actual old'),fill='white')
  for k,line in enumerate(['Old silver=light_gray_terracotta (pink/brown), floating dark strips in front of head','New connected stepped4-flange silver head, socket/crown/pommel, no detached face ornaments','Height18.5 / head5.4x5.4 / shaft1.3; authored grip(8,4,8), same TP/FP matrices','Real existing16px material atlas: cold silver range, wood shaft and leather wraps','GUI front-lit orthographic front angle-35, centered bbox, readable pixel profile','Zero PNG edits; no damage/speed/reach/recipe/ID changes; chekan orientation untouched']):draw.text((465,345+k*38),line,fill='white')
  board.save(D/'silver_mace.png');(D/'plan.json').write_text(json.dumps({'id':'silver_mace','issue':170,'state':'BEFORE_PRODUCTION','grip':[8,4,8],'decision':'FIX_CONNECTED_HEAD_SILVER_MATERIAL_GUI','reason':'Wrong terracotta metal and detached dark strips; substantive new connected flanged head, not recolor','hand_display_preserved':{k:v for k,v in model['display'].items() if k!='gui'},'gui':model['display']['gui'],'png_edits':0},indent=2)+'\n')
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(model,indent=2)+'\n');manifest={KEY:hashlib.sha256(path.read_bytes()).hexdigest()};(D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n');print(('APPLIED' if apply else 'DRAFT'),'silver_mace11 connected boxes; no PNG/gameplay/hand-display changes');return manifest
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');main(p.parse_args().apply)
